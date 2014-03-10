/**
 * 具体业务处理实现。
 */
package cn.hexing.fk.bp.businessprocess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import cn.hexing.db.batch.AsyncService;
import cn.hexing.db.bizprocess.MasterDbService;
import cn.hexing.db.managertu.ManageRtu;
import cn.hexing.fk.bp.dlms.DlmsEventProcessor;
import cn.hexing.fk.bp.dlms.events.DlmsEvent;
import cn.hexing.fk.bp.msgqueue.BPMessageQueue;
import cn.hexing.fk.bp.processor.GwUpdateKeyHandler;
import cn.hexing.fk.bp.processor.HostCommandHandler;
import cn.hexing.fk.bp.processor.gg.GgUpgradeProcessor;
import cn.hexing.fk.common.spi.IModule;
import cn.hexing.fk.common.spi.abstra.BaseModule;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.KillThreadMessage;
import cn.hexing.fk.message.MessageConst;
import cn.hexing.fk.message.MessageType;
import cn.hexing.fk.message.gate.MessageGate;
import cn.hexing.fk.message.gw.MessageGw;
import cn.hexing.fk.message.zj.MessageZj;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.model.Operator;
import cn.hexing.fk.model.RtuCmdItem;
import cn.hexing.fk.model.RtuConnectItem;
import cn.hexing.fk.model.RtuManage;
import cn.hexing.fk.utils.Counter;
import cn.hexing.fk.utils.HexDump;
import cn.hexing.fk.utils.State;

import com.hx.dlms.message.DlmsMessage;

/**
 *
 */
public class BProcessor extends BaseModule{
	private static final Logger log = Logger.getLogger(BProcessor.class);
	private static int poolSeq = 1;
	//可配置属性
	private String name = "bp";	
	private int minSize = 5;			//线程池最小个数
	private int maxSize = 20;			//线程池最大个数
	
	//引用的外部功能模块
	private AsyncService asycService;	//spring 配置实现。
	private MasterDbService masterDbService;  //spring 配置实现。
	private ManageRtu manageRtu;		//spring 配置实现。
	private HostCommandHandler hostCommandHandler=new HostCommandHandler();//主站请求数据处理类
	private BPMessageQueue msgQueue;	//spring 配置实现。
	
	//对象内部状态
	private volatile State state = new State();	
	private List<WorkThread> works = Collections.synchronizedList( new ArrayList<WorkThread>() );
	private int threadPriority = Thread.NORM_PRIORITY;
	private final IMessage killThread = new KillThreadMessage();
	private Counter counter=new Counter(100,"messageHandler");
	
	public String getName() {
		return name;
	}

	public boolean isActive() {
		return state.isActive();
	}

	public boolean start() {
		if( !state.isStopped() )
			return false;
		state = State.STARTING;
		
		forkThreads(minSize);
		while( works.size()< minSize ){
			Thread.yield();
			try{
				Thread.sleep(100);
			}catch(Exception exp){}
		}
		state = State.RUNNING;
		if( log.isDebugEnabled() )
			log.debug("Thread Pool【"+name+"】Start Success。,size="+minSize); //线程池启动成功
		return true;
	}

	public void setAsycService(AsyncService asycService) {
		this.asycService = asycService;
	}

	public void setManageRtu(ManageRtu manageRtu) {
		this.manageRtu = manageRtu;
	}

	public void setMasterDbService(MasterDbService masterDbService) {
		this.masterDbService = masterDbService;
	}

	public void stop() {
		state = State.STOPPING;
		for (int i=0; i<works.size() ; i++ ) {
			msgQueue.offer(this.killThread);
		}
		
		synchronized(works){
			for(WorkThread work: works){
				work.interrupt();
			}
		}
		int cnt = 100;
		while(cnt-->0 && works.size()>0 ){
			Thread.yield();
			try{
				Thread.sleep(50);
			}
			catch(Exception e){}
			if( cnt< 20 )
				continue;
		}
		if( log.isDebugEnabled() )
			log.debug("Thread Pool【"+name+"】Stop。,Dead thread count="+works.size());//线程池停止，僵死线程数
		works.clear();
		//把AsyncService数据回收
		for(IMessage msg : this.asycService.revokeEventQueue() )
			msgQueue.offer(msg);
		state = State.STOPPED;
	}
	
	public String getModuleType() {
		return IModule.MODULE_TYPE_BP;
	}
	
	
	private void forkThreads(int delta) {
		if (delta == 0)
			return;

		if (delta > 0) {
			//不能超过最大值
			int maxDelta = this.maxSize - works.size();
			delta = Math.min(maxDelta, delta);
			for (; delta > 0; delta--) {
				new WorkThread();
			}
		} else {
			//不能小于1
			delta = -delta;
			int n = works.size() - minSize;		//最多允许减少的线程数
			delta = Math.min(delta, n);
			for (; delta > 0; delta--) {
				msgQueue.offer(this.killThread);
			}
		}
	}
	
	private void justThreadSize(){
		int n = msgQueue.size();
		if( n> 1000 ){
			if( log.isDebugEnabled() )
				log.debug("adjustment thread pool size(+1)");
			forkThreads(1);
		}
		else if( n< 2 ){
			if( log.isDebugEnabled() )
				log.debug("adjustment thread pool size(-1)");
			forkThreads(-1);
		}
	}
	
	private class WorkThread extends Thread{
		long beginTime;
		boolean busy = false;		//判断实现是否处于工作状态
		IMessage currentMessage = null;
		public WorkThread(){
			super(name+"."+poolSeq++);
			super.start();
		}

		public void run() {
			synchronized(works){
				works.add(this);
			}
			this.setPriority(threadPriority);
			int count=0;		//每处理1000个事件检测线程池是否需要调整
			log.info("threadpool.work running:"+this.getName());
			while( !BProcessor.this.state.isStopping() && !BProcessor.this.state.isStopped() ){
				try{
					busy = false;
					currentMessage = msgQueue.take();
					if( null == currentMessage ){		//如果数据库连接异常，返回NULL。
						Thread.sleep(100);
						continue;
					}
					if (log.isDebugEnabled())
						log.debug("Process up Message:"+currentMessage.getRawPacketString());//log.debug("处理上行报文:"+currentMessage.getRawPacketString());
					if( currentMessage.getMessageType() == MessageType.MSG_KILLTHREAD )
						break;
					else if ( currentMessage.getMessageType() == MessageType.MSG_GATE ){
						MessageGate mgate = (MessageGate)currentMessage;
						if(mgate.getHead().getCommand() == MessageGate.CMD_GATE_REPLY ){
							IMessage msg = mgate.getInnerMessage();
							if(!handleMessage(msg)){//后续数据库保存速度不足
								Thread.sleep(500);
							}
						}
					}
					else if( currentMessage.getMessageType() == MessageType.MSG_ZJ ||
							currentMessage.getMessageType() == MessageType.MSG_GW_10 ){
						if (!handleMessage(currentMessage)){//后续数据库保存速度不足
							Thread.sleep(500);
						}
					}
					else if(currentMessage.getMessageType() == MessageType.MSG_DLMS){
						DlmsMessage msg = (DlmsMessage) currentMessage;
						DlmsEvent evt = new DlmsEvent(msg);
						evt.setSource(msg.getSource());
						DlmsEventProcessor.getInstance().handleEvent(evt);
					}
					//检测队列中事件个数。如果太多，增加线程。如果为0，减少线程
					count++;
					if( count>500 ){
						justThreadSize();
						count = 0;
					}
				}catch(Exception exp){
					log.error("BP WorkThread occur error", exp);//log.error("业务处理器报文处理线程处理浙规报文出错", exp);
					continue;
				}
			}
			synchronized(works){
				works.remove(this);
			}
			log.info("Thread pool work thread exit"+this.getName());//线程池的工作线程退出:
		}
		private boolean handleMessage(IMessage msg){						
			/** 业务处理器收到通信前置机上行报文，需要预先处理,
			 * 先检查是否存在此终端档案,存在才进行业务处理,不然只保存原始记录
			 * 任务,异常,主站请求返回处理:包括解析及回写数据库
			 * 原始报文保存
			 */
			MessageZj zjmsg=null;
			MessageGw gwmsg=null;
			int rtua=0;
			boolean result=true;
			if (msg.getMessageType()==MessageType.MSG_ZJ){
				zjmsg=(MessageZj)msg;
				rtua=zjmsg.head.rtua;
			}				
			else if (msg.getMessageType()==MessageType.MSG_GW_10){
				gwmsg=(MessageGw)msg;
				rtua=gwmsg.head.rtua;
			}							
			BizRtu rtu=(RtuManage.getInstance().getBizRtuInCache(rtua));
			if (rtu==null){
				//找不到终端则刷新数据库档案
				boolean refreshTag=manageRtu.refreshBizRtu(rtua);
				if (!refreshTag)
					log.warn("not find rtu in db:"+HexDump.toHex(rtua));
				else
					rtu=(RtuManage.getInstance().getBizRtuInCache(rtua));
			}
			if (rtu!=null){
				if (zjmsg!=null){

					//任务   异常
					if(zjmsg.head.c_func==MessageConst.ZJ_FUNC_READ_TASK||zjmsg.head.c_func==MessageConst.ZJ_FUNC_EXP_ALARM
							||zjmsg.head.c_func==MessageConst.GG_FUNC_READ_TASK2||zjmsg.head.c_func==MessageConst.GG_FUNC_Event){
						
						msgQueue.setLastHandleDataTime(System.currentTimeMillis());
						counter.add();
						//如果超过处理队列最大值则放回缓存队列，以免丢失数据
						if( !asycService.addMessage(zjmsg) ){
							msgQueue.offer(zjmsg);
							result=false;
							log.warn("asycService.addMessage failed");
						}
						if(zjmsg.head.msta != 0x00 && zjmsg.head.c_func==MessageConst.ZJ_FUNC_EXP_ALARM){ //zj事件主动召测进入这里,与主站交互.
							hostCommandHandler.handleExpNormalMsg(asycService,manageRtu,masterDbService,zjmsg);
						}
					}else if (zjmsg.head.c_func==MessageConst.GG_UPGRADE){
						GgUpgradeProcessor.getInstance().onUpgradeReturn(zjmsg);
						return true;
					}else if (zjmsg.head.c_func==MessageConst.ZJ_FUNC_RELAY||zjmsg.head.c_func==MessageConst.ZJ_FUNC_READ_CUR
							||zjmsg.head.c_func==MessageConst.ZJ_FUNC_WRITE_ROBJ||zjmsg.head.c_func==MessageConst.ZJ_FUNC_WRITE_OBJ
							||zjmsg.head.c_func==MessageConst.ZJ_FUNC_READ_PROG||zjmsg.head.c_func==MessageConst.GG_FUNC_Action
							||zjmsg.head.c_func==MessageConst.GG_FUNC_READ_TASK1 ||zjmsg.head.c_func==MessageConst.GG_FUNC_AutoRegistered
							||zjmsg.head.c_func==MessageConst.GG_Pay_token){//主站请求返回						
						hostCommandHandler.handleExpNormalMsg(asycService,manageRtu,masterDbService,zjmsg);
					}else if(zjmsg.isLogin()){
						saveConnectSituation(zjmsg);
					}
				}
				else if(gwmsg!=null){					
					if (gwmsg.getAFN()==MessageConst.GW_FUNC_GET_DATA1||
						gwmsg.getAFN()==MessageConst.GW_FUNC_GET_DATA2){
						if (gwmsg.head.c_prm==1){//主动上送的一、二数据报文都认为是任务
							gwmsg.setTask(true);//设置任务状态						
						}
						else{//一、二类数据回复数据需要判断是否为任务漏点补招返回
							int fseq=gwmsg.getFseq();
							List<RtuCmdItem> rcis=masterDbService.getRtuComdItem(HexDump.toHex(rtua), fseq);
							for(RtuCmdItem rci:rcis){
								if (rci.getZdzjbz()==Operator.GWLDBZ||rci.getZdzjbz()==Operator.ZZRW_SJCJ){//国网任务漏点补招请求返回或者主站任务轮招数据采集返回								
									gwmsg.setTask(true);//设置任务状态
									break;
								}
							}
						}
					}else if(gwmsg.getAFN()==MessageConst.GW_FUNC_AUTH){
						GwUpdateKeyHandler.getInstance().processUpdateKeyMsg(asycService, manageRtu, masterDbService, gwmsg);
						return true;
					}	
					else if(gwmsg.getAFN()==MessageConst.GW_FUNC_RELAY_READ){
						if (msgQueue.isDlmsRelay(gwmsg.getLogicalAddress())){
							//国网规约接dlms规约表中继
							if(DlmsEventProcessor.getInstance().postUpRelayMessage(gwmsg)){
								return result;								
							}
						}
					}
					//如果是任务或异常事件则放入相应队列
					if(gwmsg.isTask()||gwmsg.getAFN()==MessageConst.GW_FUNC_GET_DATA3){
						msgQueue.setLastHandleDataTime(System.currentTimeMillis());
						counter.add();
						//如果超过处理队列最大值则放回缓存队列，以免丢失数据
						if( !asycService.addMessage(gwmsg) ){
							msgQueue.offer(gwmsg);
							result=false;
							log.warn("asycService.addMessage failed");
						}
						if(gwmsg.head.c_prm==MessageConst.DIR_DOWN) //主站召测
							hostCommandHandler.handleExpNormalMsg(asycService,manageRtu,masterDbService,gwmsg);
					}
					else if (gwmsg.getAFN()==MessageConst.GW_FUNC_HEART){
						//当是登陆帧的时候，存储。
						if(gwmsg.isLogin()){
							saveConnectSituation(gwmsg);
						}
					}
					else 
						hostCommandHandler.handleExpNormalMsg(asycService,manageRtu,masterDbService,gwmsg);
				}
			}	
			return result;
		}

		/**
		 * 保存状况
		 * @param gwmsg
		 */
		private void saveConnectSituation(IMessage gwmsg) {
			RtuConnectItem rci = new RtuConnectItem();
			rci.setLogicAddress(gwmsg.getLogicalAddress());
			rci.setPeerAddress(gwmsg.getPeerAddr());
			rci.setStatus(0);
			asycService.addToDao(rci, 6003);
		}
		
		public String toString(){
			String busyStatus = "idle";
			if( busy ){
				long timeConsume = System.currentTimeMillis()-beginTime;
				busyStatus = "process time(millisecond):"+timeConsume;//busyStatus = "当前处理时间(毫秒):"+timeConsume;
			}
			return "["+getName()+","+ busyStatus + "];";
		}
	}

	public void setMsgQueue(BPMessageQueue msgQueue) {
		this.msgQueue = msgQueue;
	}

	public int getMinSize() {
		return minSize;
	}

	public void setMinSize(int minSize) {
		this.minSize = minSize;
	}

	public int getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}
}
