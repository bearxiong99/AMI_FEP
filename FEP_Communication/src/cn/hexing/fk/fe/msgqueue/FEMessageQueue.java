/**
 * 通信前置机的报文队列。
 * 报文队列的生产者（插入）：GateMessageEventHandle，UmsMessageEventHandle
 * 报文队列的消费者（取走）：BpServerEventHandle
 * 所有终端下行，都通过通信前置机的消息队列，统一下行，以便进行通道管理。
 * 
 * 异步上行到业务处理器的算法：
 * (1)   BP－>ReqNum到FE－>FE从队列取msg－>BP；
 * 		如果队列为空，则等到队列消息;
 * (2)   (2.1)FE从队列取消息成功发送给BP－>(2.2.1)ReqNum允许继续发送->(2.3)从队列取继续发送
 * 		 (2.2.2) 等待新的ReqNum；
 * 
 * 难点：上行BP(业务处理器)发送需要等待2件事情：ReqNumber和上行消息.
 * 对于多业务处理器情况：
 * 按照地市代码，动态分配到BP client连接对象。2.2版本提供，实现类：MessageDispatch2Bp
 */
package cn.hexing.fk.fe.msgqueue;

import cn.hexing.fk.FasSystem;
import cn.hexing.fk.common.queue.CacheQueue;
import cn.hexing.fk.common.simpletimer.ITimerFunctor;
import cn.hexing.fk.common.simpletimer.TimerData;
import cn.hexing.fk.common.simpletimer.TimerScheduler;
import cn.hexing.fk.common.spi.IMessageQueue;
import cn.hexing.fk.common.spi.IProfile;
import cn.hexing.fk.common.spi.socket.IChannel;
import cn.hexing.fk.common.spi.socket.IServerSideChannel;
import cn.hexing.fk.fe.ChannelManage;
import cn.hexing.fk.fe.cluster.RealtimeSynchronizer;
import cn.hexing.fk.fe.cluster.RtuState;
import cn.hexing.fk.fe.userdefine.UserDefineMessageQueue;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.MessageConst;
import cn.hexing.fk.message.MessageType;
import cn.hexing.fk.message.gate.GateHead;
import cn.hexing.fk.message.gate.MessageGate;
import cn.hexing.fk.message.gw.MessageGw;
import cn.hexing.fk.message.zj.MessageZj;
import cn.hexing.fk.model.RtuManage;
import cn.hexing.fk.utils.HexDump;

import com.hx.ansi.message.AnsiMessage;
import com.hx.dlms.message.DlmsMessage;

import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

/**
 * (1)Master station initiate to send heart beat packet to RTU. 
 */
public class FEMessageQueue implements IMessageQueue,ITimerFunctor, IProfile{
	private static final Logger log = Logger.getLogger(FEMessageQueue.class);	
	private CacheQueue cacheQueue;		//spring 配置实现。
	private int rtuHeartbeatInterval = 15*60;	//15分钟心跳间隔。
	//在漏点补召完成之前，需要缓存任务报文。王卫峰提出的功能。 2008－12－24
	private CacheQueue taskCacheQueue;
	//内部属性
	private final int heartbeatTimer = 0;
	private TimerData td = null;
	//辅助属性，以便提高系统性能
	private boolean dispatchRandom = true;
	private boolean noConvert = false;		//是否直接上行浙江规约原始报文.
	
	//当系统退出时候，需要把队列数据写到缓存文件。
	private Runnable shutdownHook = new Runnable(){
		public void run(){
			FEMessageQueue.this.dispose();
		}
	};

	public void setCacheQueue( CacheQueue queue ){
		cacheQueue = queue;
		if( null == td )
			initialize();
		FasSystem.getFasSystem().addShutdownHook(shutdownHook);
	}
	
	public void setRtuHeartbeatInterval(int interval){
		rtuHeartbeatInterval = interval;
//		hbInterval = rtuHeartbeatInterval * 1000; 
		initialize();
	}

	public void initialize(){
		if( null != td ){
			TimerScheduler.getScheduler().removeTimer(this, heartbeatTimer);
			td = null;
		}
		if( this.rtuHeartbeatInterval > 10 ){
			td = new TimerData(this,heartbeatTimer,this.rtuHeartbeatInterval);
			TimerScheduler.getScheduler().addTimer(td);
		}
	}

	public void onTimer(final int timerID ){
		if( timerID == heartbeatTimer ){
/*			for( ComRtu rtu: RtuManage.getInstance().getAllComRtu() ){
				//只有上行过GPRS报文的终端，才进行主站主动心跳尝试。
				if( null == rtu.getActiveGprs() )
					continue;
				long distance = Math.abs(System.currentTimeMillis() - rtu.getLastIoTime());
				if( distance > hbInterval ){
					//超过间隔时间没有收到上行报文
					//主站（通信前置机）主动向终端发起心跳检测。
					if("01".equals(rtu.getRtuProtocol()) || "07".equals(rtu.getRtuProtocol()) ){
						MessageZj heartbeat = messageCreator.createHeartBeat(rtu.getRtua());
						rtu.setLastIoTime(System.currentTimeMillis());
						sendMessage(heartbeat);						
					}
				}
			}
*/		}
	}
	
	//消息队列统一管理终端下行消息
	public boolean sendMessage(IMessage msg){
		if( msg.getMessageType() == MessageType.MSG_ZJ ){
			MessageZj zjmsg = (MessageZj)msg;
			IChannel channel = null;
			boolean result = false;
			if( zjmsg.head.c_func == MessageConst.ZJ_FUNC_HEART ){
				//如果是主站主动发起心跳，不走短信。
				channel = ChannelManage.getInstance().getGPRSChannel(zjmsg.getLogicalAddress());
				if( null != channel )
					result = channel.send(zjmsg);
			}
			else if( zjmsg.head.c_func == MessageConst.ZJ_FUNC_USER_DEFINE ){
				result = UserDefineMessageQueue.getInstance().sendMessageDown(zjmsg);
			}
			else{
				channel = ChannelManage.getInstance().getGPRSChannel(zjmsg.getLogicalAddress());
				if( null == channel ){
					//GPRS不在线，走SMS
					channel = ChannelManage.getInstance().getUmsGateChannel(zjmsg.getLogicalAddress());
					if( null == channel ){
						log.warn("GPRS不在线，UMSGate未定义，该终端无可用通道下行,RTUA="+HexDump.toHex(zjmsg.head.rtua));
						return false;
					}
					//设置APPID SUBID SIMNUM
					RtuState  state = RealtimeSynchronizer.getInstance().getRtuState(zjmsg.getLogicalAddress());
					MessageGate gatemsg = new MessageGate();
					gatemsg.setDownInnerMessage(zjmsg);
					gatemsg.getHead().setAttribute(GateHead.ATT_DESTADDR, state.getUmsAddress());
					gatemsg.getHead().setAttribute(GateHead.ATT_SRCADDR, state.simNumber());
					msg = gatemsg;
				}
				//可能发送zjmsg或者GateMessage（UMSGate通道）
				result = channel.send(msg);
			}
			return result;
		}
		else if( msg.getMessageType() == MessageType.MSG_GATE ){
			MessageGate gatemsg = (MessageGate)msg;
			//如果指定短信通道
			String appstring = gatemsg.getHead().getAttributeAsString(GateHead.ATT_DESTADDR);
			int peerAddrIndex = appstring.indexOf('@');
			if( peerAddrIndex<0 )
				peerAddrIndex = appstring.indexOf('.');
			String sims = gatemsg.getHead().getAttributeAsString(GateHead.ATT_SRCADDR);
			int txfs = gatemsg.getHead().getAttributeAsInt(GateHead.ATT_DOWN_CHANNEL);
			if( peerAddrIndex<0 && null != appstring && appstring.length()>=9 ){
				String appid = appstring.substring(5, 9);
				IChannel channel = ChannelManage.getInstance().getUmsGateChannel(null);
				if( null == channel ){
					log.warn("UMSGate Channel未启动：appid="+appid);
					handleSendFail(gatemsg.getInnerMessage());
					return false;
				}
				IMessage rtuMsg = gatemsg.getInnerMessage();
//				rtuMsg.setPeerAddr(appstring);	//例如：955983401 95598340101
				//设置APPID SUBID SIMNUM
				if( null != sims && sims.length()>10 ){
					gatemsg = new MessageGate();
					gatemsg.setDownInnerMessage(rtuMsg);
					gatemsg.getHead().setAttribute(GateHead.ATT_DESTADDR, appstring );
					gatemsg.getHead().setAttribute(GateHead.ATT_SRCADDR, sims);
				}
				else{
					RtuState  state = RealtimeSynchronizer.getInstance().getRtuState(rtuMsg.getLogicalAddress());
					if( null == state ){
						state = RealtimeSynchronizer.getInstance().loadFromDb(rtuMsg.getLogicalAddress());
						RealtimeSynchronizer.getInstance().setRtuState(state);
					}
					gatemsg = new MessageGate();
					gatemsg.setDownInnerMessage(rtuMsg);
					gatemsg.getHead().setAttribute(GateHead.ATT_DESTADDR, appstring );
					gatemsg.getHead().setAttribute(GateHead.ATT_SRCADDR, state.simNumber());
				}
				return channel.send(gatemsg);
			}
			//普通浙江或国网规约下行报文
			IMessage rtuMsg = gatemsg.getInnerMessage();
			if( null == rtuMsg ){
				log.error("下行的网关消息没有包含浙江或国网规约帧。gatemsg="+gatemsg.getRawPacketString());
				return false;
			}
			//检测是否短信发送请求。
			if( rtuMsg.getMessageType() == MessageType.MSG_ZJ ){
				MessageZj zjmsg = (MessageZj)rtuMsg;
				if( zjmsg.head.c_func == MessageConst.ZJ_FUNC_REQ_SMS ){
					IChannel channel = ChannelManage.getInstance().getUmsGateChannel(null);
					if( null == channel ){
						log.warn("ZJ_FUNC_REQ_SMS: UMSGate Channel未启动");
						handleSendFail(rtuMsg);
						return false;
					}
					gatemsg = new MessageGate();
					gatemsg.setDownInnerMessage(rtuMsg);
					gatemsg.getHead().setAttribute(GateHead.ATT_DESTADDR, "" );
					gatemsg.getHead().setAttribute(GateHead.ATT_SRCADDR, "");
					return channel.send(gatemsg);
				}
			}
			else if( rtuMsg.getMessageType() == MessageType.MSG_DLMS ){
				gatemsg.linkMode=gatemsg.getHead().getAttributeAsInt(GateHead.ATT_TXFS);
				txfs=1;
				if(gatemsg.linkMode!=0){
					txfs = gatemsg.linkMode;
				}
			}
			else if(rtuMsg.getMessageType()==MessageType.MSG_ANSI){
				txfs=1;
			}
			/**
			 * 针对主站下行请求，需要考虑UDP不在线情况。
			 */
			IChannel gprsChannel = null;
			IChannel umsGate = null;
			if( txfs == 0 || txfs == 1 )
				gprsChannel = ChannelManage.getInstance().getGPRSChannel(rtuMsg.getLogicalAddress());
			if( txfs == 2 || (txfs == 0 && null==gprsChannel) )
				umsGate = ChannelManage.getInstance().getUmsGateChannel();
			if( txfs ==IMessage.COMMUNICATION_TYPE_CSD || txfs ==IMessage.COMMUNICATION_TYPE_EXTERNAL){//外连和csd模式
				gprsChannel=ChannelManage.getInstance().getActiveGprsChannel();
			}
			if( null == gprsChannel && null == umsGate ){
				log.warn("No Available Down Channel For Terminal,RTUA="+rtuMsg.getLogicalAddress());//log.warn("该终端无可用通道下行,RTUA="+rtuMsg.getLogicalAddress());
				handleSendFail(rtuMsg);
				return false;
			}
			RtuState  state = RealtimeSynchronizer.getInstance().getRtuState(rtuMsg.getLogicalAddress());

			//更新主站命令下行时间
			if( rtuMsg.getMessageType() == MessageType.MSG_GW_10 ){
				MessageGw gwmsg = (MessageGw)rtuMsg;
				//国网规约终端升级
				if( gwmsg.afn() == MessageConst.GW_FUNC_FILE ){
					boolean result = false;
					if( null != gprsChannel )
						result = UserDefineMessageQueue.getInstance().sendMessageDown(gwmsg);
					return result;
				}
			}
			if( null != gprsChannel ){
				if (rtuMsg.getMessageType()==MessageType.MSG_DLMS){//dlms规约终端地址需要传递
					return gprsChannel.send(gatemsg);
				}else if(rtuMsg.getMessageType()==MessageType.MSG_ANSI){
					return gprsChannel.send(gatemsg);
				}
				else return gprsChannel.send(rtuMsg);
			}else if( null != umsGate ) {
				gatemsg = new MessageGate();
				gatemsg.setDownInnerMessage(rtuMsg);
				gatemsg.getHead().setAttribute(GateHead.ATT_DESTADDR, state.getUmsAddress());
				gatemsg.getHead().setAttribute(GateHead.ATT_SRCADDR, state.simNumber());
				return umsGate.send(gatemsg);
			}
		}
		else if(msg.getMessageType() == MessageType.MSG_GW_10){
			IChannel gprsChannel = ChannelManage.getInstance().getGPRSChannel(msg.getLogicalAddress());
			IChannel umsGate = null;
			if( null == gprsChannel ){
				umsGate = ChannelManage.getInstance().getUmsGateChannel();
				if( null == umsGate ){
					log.warn("GPRS不在线，UMSGate未定义，该终端无可用通道下行,RTUA="+msg.getLogicalAddress());
					handleSendFail(msg);
					return false;
				}
			}
			RtuState  state = RealtimeSynchronizer.getInstance().getRtuState(msg.getLogicalAddress());

			MessageGw gwmsg = (MessageGw)msg;
			//国网规约终端升级
			if( gwmsg.afn() == MessageConst.GW_FUNC_FILE ){
				return UserDefineMessageQueue.getInstance().sendMessageDown(gwmsg);
			}
			boolean result = false;
			if( null != gprsChannel )
				result = gprsChannel.send(gwmsg);
			else{
				boolean umsReady = StringUtils.hasText(state.getActiveUms()) || StringUtils.hasText(state.getSimNum());
				if( umsReady ){
					MessageGate gatemsg = new MessageGate();
					gatemsg.setDownInnerMessage(gwmsg);
					gatemsg.getHead().setAttribute(GateHead.ATT_DESTADDR, state.getUmsAddress());
					gatemsg.getHead().setAttribute(GateHead.ATT_SRCADDR, state.simNumber());
					return umsGate.send(gatemsg);
				}
			}
			return result;
		}
		else if(msg.getMessageType() == MessageType.MSG_DLMS){
			IChannel gprsChannel = ChannelManage.getInstance().getGPRSChannel(msg.getLogicalAddress());
			IChannel umsGate = null;
			if( null == gprsChannel ){
				umsGate = ChannelManage.getInstance().getUmsGateChannel();
				if( null == umsGate ){
					log.warn("GPRS不在线，UMSGate未定义，该终端无可用通道下行,RTUA="+HexDump.toHex(msg.getRtua()));
					handleSendFail(msg);
					return false;
				}
			}
			RtuState  state = RealtimeSynchronizer.getInstance().getRtuState(msg.getLogicalAddress());
			DlmsMessage dlmsmsg = (DlmsMessage)msg;
			boolean result = false;
			if( null != gprsChannel )
				result = gprsChannel.send(dlmsmsg);
			else{
				boolean umsReady = StringUtils.hasText(state.getActiveUms()) || StringUtils.hasText(state.getSimNum());
				if( umsReady ){
					MessageGate gatemsg = new MessageGate();
					gatemsg.setDownInnerMessage(dlmsmsg);
					gatemsg.getHead().setAttribute(GateHead.ATT_DESTADDR, state.getUmsAddress());
					gatemsg.getHead().setAttribute(GateHead.ATT_SRCADDR, state.simNumber());
					return umsGate.send(gatemsg);
				}
			}
			return result;
		}
		log.error("FEMessageQueue只支持MessageGate,MessageZj,MessageGw消息下行。程序错啦！");
		return false;
	}
	
	private void handleSendFail(IMessage msg ){
		try{
			IChannel channel = msg.getSource();
			if( msg.getMessageType() == MessageType.MSG_ZJ ){
				MessageZj zjmsg = (MessageZj)msg;
				MessageZj repSendFail = zjmsg.createSendFailReply();
				MessageGate gatemsg = new MessageGate();
				gatemsg.setUpInnerMessage(repSendFail);
				channel.send(gatemsg);
			}
		}catch(Exception exp){
			log.error(exp.getLocalizedMessage(),exp);
		}
	}
	
	/**
	 * 对于Gprs发送失败，转UMS通道的情况。
	 * @param msg
	 * @return
	 */
	public boolean sendMessageByUms(IMessage msg){
		IChannel umsGate = ChannelManage.getInstance().getUmsGateChannel();
		if( null == umsGate ){
			log.warn("UMSGate未定义，该终端无可用UMS通道下行,RTUA="+HexDump.toHex(msg.getRtua()));
			return false;
		}
		RtuState  state = null;
		if(msg instanceof MessageZj || msg instanceof MessageGw ){
			state = RealtimeSynchronizer.getInstance().getRtuState(msg.getLogicalAddress());
		}
		else if( msg instanceof MessageGate ){
			msg = ((MessageGate)msg).getInnerMessage();
			state = RealtimeSynchronizer.getInstance().getRtuState(msg.getLogicalAddress());
		}
		MessageGate gatemsg = new MessageGate();
		gatemsg.setDownInnerMessage(msg);
		gatemsg.getHead().setAttribute(GateHead.ATT_DESTADDR, state.getUmsAddress());
		gatemsg.getHead().setAttribute(GateHead.ATT_SRCADDR, state.simNumber());
		return umsGate.send(gatemsg);
	}
	
	//下面定义消息队列的方法
	public IMessage take(){
		return cacheQueue.take();
	}
	
	public IMessage poll(){
		return cacheQueue.poll();
	}
	
	/**
	 * 当通信前置机收到网关上行报文时，调用此函数，把上行消息放入队列，
	 * 以便发送给业务处理器。
	 * @param msg
	 */
	public void offer(IMessage msg0){
		if( msg0.getMessageType() == MessageType.MSG_GATE ){
			RuntimeException re = new RuntimeException();
			log.warn("出现插入gate 消息",re);
			return;
		}
		if (msg0 instanceof MessageZj){
			MessageZj zjmsg = (MessageZj)msg0;
			if( null != taskCacheQueue ){
				try{
					if( zjmsg.head.c_func == MessageConst.ZJ_FUNC_READ_TASK )
						taskCacheQueue.offer(zjmsg);
				}catch(Exception e){
					log.warn(e.getLocalizedMessage(),e);
				}
			}
		}		
		//必须先放到cacheQueue，然后取出来进行发送选择。
		cacheQueue.offer(msg0);
		
		//按照优先级取需要发送的报文
		msg0 = cacheQueue.poll();
		//可能被别的线程取走，需要判断是否为null
		if( null == msg0 )
			return;
		
		IServerSideChannel bpChannel = null;
		//检测是否有bpclient允许发送报文。
		boolean success = false;
		for(int i=0; i<2; i++){
			if( this.dispatchRandom ){
				//往业务处理器随机发送机制。
				bpChannel = MessageDispatch2Bp.getInstance().getIdleChannel();
				if( null == bpChannel )
					bpChannel = Msg2BpByMod.getInstance().getIdleChannel();
			}
			else{
				//按照地市码往业务处理器发送
				byte districtCode = BpBalanceFactor.getInstance().getDistrictCode(RtuManage.getInstance().getComRtuInCache(msg0.getRtua()));
				bpChannel = MessageDispatch2Bp.getInstance().getBpChannel(districtCode);
				if( null == bpChannel )
					bpChannel = Msg2BpByMod.getInstance().getBpChannel(msg0.getRtua());
			}
			if( null == bpChannel ){
				if(msg0 instanceof DlmsMessage){
					log.warn("can't find bp for logicAddress "+((DlmsMessage)msg0).getLogicalAddress());
				}else if(msg0 instanceof AnsiMessage){
					log.warn("can't find bp for logicAddress "+((AnsiMessage)msg0).getLogicalAddress());
				}
				else{
					log.warn("can't not find bp for rtu "+Integer.toHexString(msg0.getRtua()));
				}
				pushBack(msg0);
				return;
			}
			if( noConvert )
				success = bpChannel.send(msg0);
			else{
				//把浙江规约报文转换成网关规约，发送给前置机。
				MessageGate gateMsg = new MessageGate();
				gateMsg.setUpInnerMessage(msg0);
				if(msg0 instanceof DlmsMessage && gateMsg.getData().limit()==60){
					//将DlmsMessage里的  getRawFrame 添加为 synchronized 之后，应该不会出现这个问题了。
					//为了保险起见，这里不删除
					log.warn("dlms setUpInnerMessage error, try again.");
					gateMsg.setUpInnerMessage(msg0);
				}
				success = bpChannel.send(gateMsg);
			}
			if(success){
				break;				
			}
		}
		if( !success ){
			pushBack(msg0);
		}
	}
	
	/**
	 * 当消息从client的发送队列回收时，调用putback。
	 * @param msg
	 */
	public void pushBack(IMessage msg){
		cacheQueue.offer(msg);
	}
	
	public int size(){
		return cacheQueue.size();
	}

	@Override
	public String toString() {
		return "FEMessageQueue";
	}

	public CacheQueue getTaskCacheQueue() {
		return taskCacheQueue;
	}

	public void setTaskCacheQueue(CacheQueue taskCacheQueue) {
		this.taskCacheQueue = taskCacheQueue;
	}
	
	public void onBpClientConnected(IServerSideChannel bpClient){
//		MessageDispatch2Bp.getInstance().onBpClientConnected(bpClient);
		Msg2BpByMod.getInstance().onBpClientConnected(bpClient);
	}
	
	public void onBpClientClosed(IServerSideChannel bpClient){
//		MessageDispatch2Bp.getInstance().onBpClientClosed(bpClient);
		Msg2BpByMod.getInstance().onBpClientClosed(bpClient);
	}
	
	public void setDispatchRandom(boolean dispatchRandom){
		this.dispatchRandom = dispatchRandom;
	}
	
	public void setNoConvert(boolean noConvert) {
		this.noConvert = noConvert;
	}

	public String profile() {
		StringBuffer sb = new StringBuffer(256);
		sb.append("\r\n    <message-queue type=\"fe\">");
		sb.append("\r\n        <size>").append(size()).append("</size>");
		sb.append("\r\n    </message-queue>");
		return sb.toString();
	}
	
	public void dispose(){
		cacheQueue.dispose();
	}

}
