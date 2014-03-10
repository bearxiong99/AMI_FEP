/**
 * 用于网关与通信前置机之间报文收发事件处理。
 * 上行报文进入优先级队列，以便业务处理器处理。
 * 注意处理流量统计、工况等。
 */
package cn.hexing.fk.fe.gprs;

import org.apache.log4j.Logger;

import cn.hexing.db.batch.AsyncService;
import cn.hexing.db.bizprocess.MasterDbService;
import cn.hexing.fk.FasSystem;
import cn.hexing.fk.clientmod.ClientModule;
import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.common.spi.IEventHandler;
import cn.hexing.fk.fe.cluster.BatchSynchronizer;
import cn.hexing.fk.fe.cluster.RealtimeSynchronizer;
import cn.hexing.fk.fe.cluster.RtuState;
import cn.hexing.fk.fe.cluster.RtuWorkStateItem;
import cn.hexing.fk.fe.cluster.WorkState;
import cn.hexing.fk.fe.msgqueue.FEMessageQueue;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.MessageConst;
import cn.hexing.fk.message.MessageType;
import cn.hexing.fk.message.bengal.BengalMessage;
import cn.hexing.fk.message.gate.GateHead;
import cn.hexing.fk.message.gate.MessageGate;
import cn.hexing.fk.message.gw.MessageGw;
import cn.hexing.fk.message.zj.MessageZj;
import cn.hexing.fk.model.RtuConnectItem;
import cn.hexing.fk.sockserver.event.ReceiveMessageEvent;
import cn.hexing.fk.sockserver.event.SendMessageEvent;
import cn.hexing.fk.tracelog.TraceLog;
import cn.hexing.ws.logic.WsGlobalMap;

import com.hx.ansi.message.AnsiMessage;
import com.hx.dlms.message.DlmsMessage;
/**
 *
 */
public class GateMessageEventHandler implements IEventHandler {
	private static final Logger log = Logger.getLogger(GateMessageEventHandler.class);
	private static final TraceLog tracer = TraceLog.getTracer(GateMessageEventHandler.class);
//	private static HeartBeatMessage bate = new HeartBeatMessage();
	private FEMessageQueue msgQueue;	//spring 配置实现。
	private AsyncService asyncDbService;		//用于批量保存原始报文
//	private MasterDbService masterDbService;  //spring 配置实现。
	private ClientModule intfClient;
	
//	private HeartBeatMessage heartBeat;//spring 配置实现，心跳保存
	
	public void handleEvent(IEvent event) {
		if( event.getType().equals(EventType.MSG_RECV) ){
			onRecvMessage( (ReceiveMessageEvent)event);
		}
			
		else if( event.getType().equals(EventType.MSG_SENT) )
			onSendMessage( (SendMessageEvent)event );
	}
	/**
	 * 收到GPRS网关的上行报文。
	 * @param e
	 */
	private void onRecvMessage(ReceiveMessageEvent e){
		IMessage msg = e.getMessage();
		if( msg.getMessageType() == MessageType.MSG_GATE ){
			MessageGate mgate = (MessageGate)msg;
			//增加支持客户端请求报文功能。服务器不主动往client发送报文。HREQ还起到心跳报文作用。
			if( mgate.getHead().getCommand() == MessageGate.CMD_GATE_HREPLY ){
				//客户端请求的报文数量的应答
//				log.info(mgate);
				return;		//心跳处理结束
			}
			else if(mgate.getHead().getCommand() == MessageGate.CMD_GATE_REPLY || mgate.getHead().getCommand() == MessageGate.CMD_WRAP){
				IMessage imsg = mgate.getInnerMessage();
				
//				RtuInfoServer.getInstance().setCurrentMsg(MessageConst.DIR_UP, imsg);
				
				if(log.isDebugEnabled())
					log.debug("receive msg from gate:"+imsg);
				if( null != imsg ){
					if( imsg.getMessageType() == MessageType.MSG_ZJ ){
						MessageZj zjmsg = (MessageZj)imsg;
						_handleZjMessage(zjmsg,e);
					}
					else if( imsg.getMessageType() == MessageType.MSG_GW_10 ){
						MessageGw gwmsg = (MessageGw)imsg;
						_handleGwMessage(gwmsg,e);
					}else if( imsg.getMessageType() == MessageType.MSG_DLMS ){					
						DlmsMessage dlmsmsg = (DlmsMessage)imsg;
						try {
							_handleDlmsMessage(dlmsmsg,e);
						} catch (Exception e1) {
							log.error("_handleDlmsMessage error!! handleDlmsMessage again.",e1);
							_handleDlmsMessage(dlmsmsg, e);
						}
					}else if( imsg.getMessageType() == MessageType.MSG_ANSI){
						AnsiMessage amsg=(AnsiMessage)imsg;
						_handleAnsiMessage(amsg,e);
					}
					
					else if( imsg.getMessageType() == MessageType.MSG_BENGAL ){
						BengalMessage bmsg = (BengalMessage)imsg;
						bmsg.setTxfs( mgate.getTxfs());
						
						_handleBengalMessage(bmsg,e);
					}
				}
			}
			else if(mgate.getHead().getCommand() == MessageGate.CMD_GATE_SENDFAIL ){
				//GPRS网关下行失败，需要把请求通过短信通道下行到终端。
				IMessage imsg = mgate.getInnerMessage();
				if( imsg.getMessageType() == MessageType.MSG_ZJ ){
					MessageZj zjmsg = (MessageZj)imsg;
					//如不走短信:厂家自定义报文
					if( zjmsg.head.c_func == MessageConst.ZJ_FUNC_USER_DEFINE 
							|| zjmsg.head.c_func == MessageConst.ZJ_FUNC_HEART )
						return;

					//GPRS下行失败，需要转短信。制定短信下行策略.
					if( null != zjmsg && log.isDebugEnabled() )
						log.debug("网关下行失败报文,转短信通道:"+zjmsg);
					msgQueue.sendMessageByUms(zjmsg);
					return;
				}
				else if( imsg.getMessageType() == MessageType.MSG_GW_10 ){
					MessageGw gwmsg = (MessageGw)imsg;
					if( gwmsg.afn() == MessageConst.GW_FUNC_CONTROL ||
							gwmsg.afn() == MessageConst.GW_FUNC_GET_DATA1 ||
							gwmsg.afn() == MessageConst.GW_FUNC_GET_DATA2 ||
							gwmsg.afn() == MessageConst.GW_FUNC_GET_DATA3 ||
							gwmsg.afn() == MessageConst.GW_FUNC_GET_TASK ||
							gwmsg.afn() == MessageConst.GW_FUNC_GETPARAM ||
							gwmsg.afn() == MessageConst.GW_FUNC_RELAY_READ ||
							gwmsg.afn() == MessageConst.GW_FUNC_RESET ||
							gwmsg.afn() == MessageConst.GW_FUNC_SETPARAM ||
							gwmsg.afn() == MessageConst.GW_FUNC_REQ_CASCADE_UP ||
							gwmsg.afn() == MessageConst.GW_FUNC_REQ_RTU_CFG ){
						//GPRS下行失败，需要转短信。制定短信下行策略.
						msgQueue.sendMessageByUms(gwmsg);
						return;
					}
				}
			}
			else if( mgate.getHead().getCommand() == MessageGate.REP_MONITOR_RELAY_PROFILE ){
				String gateProfile = new String(mgate.getData().array());
				FasSystem.getFasSystem().addGprsGateProfile(e.getClient().getPeerAddr(), gateProfile);
				return;
			}
			else if( mgate.getHead().getCommand() == MessageGate.CMD_RTU_CLOSE ){
				String logicalAddress = mgate.getHead().getAttributeAsString(GateHead.ATT_LOGICALADDR);
				log.warn("RTU_CLOSED: "+logicalAddress );
				WorkState workState = BatchSynchronizer.getInstance().getWorkState(logicalAddress);
				if(workState!=null)  workState.setConnect(false);					
				RtuConnectItem rci = new RtuConnectItem();
				rci.setLogicAddress(logicalAddress);
				rci.setPeerAddress(mgate.getHead().getAttributeAsString(GateHead.ATT_SRCADDR));
				rci.setStatus(1);
				asyncDbService.addToDao(rci, 6003);
			}
			else {
				//其它类型命令
				log.error("other command!");//log.error("其它类型命令。");
			}
		}
		else if( msg.getMessageType() == MessageType.MSG_ZJ ){
			_handleZjMessage((MessageZj)msg,e);
		}
	}
	
	private void _handleAnsiMessage(AnsiMessage amsg, ReceiveMessageEvent event) {

		if( log.isDebugEnabled() )
			log.debug("网关上行报文:"+amsg);
		/** 通信前置机收到网关上行报文，需要特殊处理
		 *  （1）心跳报文存MySQL；
		 *  （2）终端工况：流量
		 *  （3）更新终端所属的网关IP：port，与主站配置不一致处理
		 */
		//1. 从终端通信参数对象管理器找到终端参数对象
		RtuState  state = RealtimeSynchronizer.getInstance().getRtuState(amsg.getLogicalAddress());
		boolean updateState = false;
		if( null == state ){
			state = RealtimeSynchronizer.getInstance().loadFromDb(amsg.getLogicalAddress());
			updateState = true;
		}
		
		//2 & 3. 更新工况 。流量统计。心跳自动回复，所以长度*2,登录回复
		int flow = amsg.length();
		
		RtuWorkStateItem rws = new RtuWorkStateItem();
		rws.setLastCommunicationIp(amsg.getPeerAddr());
		rws.setIoTime(System.currentTimeMillis());
		rws.setRtua(state.getRtua());
		rws.setLen(flow);
		
		if(amsg.isHeartbeat()){
			rws.setFunc(RtuWorkStateItem.FUNC_DLMS_HEART);
//		}else if(amsg.isEventNeedReply()){
//			rws.setFunc(RtuWorkStateItem.FUNC_DLMS_NEED_CFM);
		}else{
			rws.setFunc(RtuWorkStateItem.FUNC_REPLY);
		}
		BatchSynchronizer.getInstance().addWorkState(rws);
		
		//4. 终端归属网关 关系更新
		try{
			String gateAddr = event.getClient().getPeerAddr();	//网关的前置机接口地址。
			if( ! gateAddr.equals(state.getActiveGprs()) ){
				state.setActiveGprs(gateAddr);
				updateState = true;
			}
			if( updateState )
				RealtimeSynchronizer.getInstance().setRtuState(state);
		}catch(Exception err){
			log.error("update activeGprs exp:"+err.getLocalizedMessage(),err);
		}
		RealtimeSynchronizer.getInstance().saveCommState(state);
		//5.2 原始报文保存,心跳不入库
		if(isSaveHeartbeat(amsg.getLogicalAddress()) || !amsg.isHeartbeat()){
			//如果保存心跳或者不是心跳，才保存
			asyncDbService.log2Db(amsg);
		}
		
		
		msgQueue.offer(amsg);
//		//6 从数据查询该终端当前有效bp地址
//		IServerSideChannel bpChannel=Msg2BpByMod.getInstance().getIdleChannel();
//		if ( null == bpChannel )
//			bpChannel = Msg2BpByMod.getInstance().getBpChannel(0);
//		if (bpChannel!=null){
//			MessageGate gateMsg = new MessageGate();
//			gateMsg.setUpInnerMessage(dlmsmsg);
//			boolean success = bpChannel.send(gateMsg);
//			if( !success ){
//				log.warn("send DLMS message to BP failed. meterId="+dlmsmsg.getLogicalAddress());
//			}else{
//				if(log.isDebugEnabled())
//					log.debug("send to bp success."+"\n\t recvFrom:"+dlmsmsg.getPeerAddr()+"\t logicAddress:"+dlmsmsg.getLogicalAddress()+"\n\t msg:"+dlmsmsg);
//			}
//		}
//		else
//			log.warn("can not find  bp client of addr="+dlmsmsg.getLogicalAddress() );
	
		
		
		
		
		
	}
	private void _handleBengalMessage(BengalMessage bmsg, ReceiveMessageEvent event){
		if( log.isDebugEnabled() )
			log.debug("网关上行报文:"+bmsg);
		/** 通信前置机收到网关上行报文，需要特殊处理
		 *  （1）心跳报文存MySQL；
		 *  （2）终端工况：流量
		 *  （3）更新终端所属的网关IP：port，与主站配置不一致处理
		 */
		//1. 从终端通信参数对象管理器找到终端参数对象
		RtuState  state = RealtimeSynchronizer.getInstance().getRtuState(bmsg.getLogicalAddress());
		boolean updateState = false;
		if( null == state ){
			state = RealtimeSynchronizer.getInstance().loadFromDb(bmsg.getLogicalAddress());
			updateState = true;
		}

		//2 & 3. 更新工况 。流量统计。心跳自动回复，所以长度*2,登录回复
		int flow = bmsg.length();
		
		RtuWorkStateItem rws = new RtuWorkStateItem();
		rws.setIoTime(System.currentTimeMillis());
		rws.setRtua(state.getRtua());
		rws.setLen(flow);
		if(bmsg.getFuncCode()==1){
			rws.setFunc(RtuWorkStateItem.FUNC_HEART);
		}
		else if( bmsg.isTask() ){
			rws.setFunc(RtuWorkStateItem.FUNC_TASK);
		}
		else{
			rws.setFunc(RtuWorkStateItem.FUNC_REPLY);
		}
		BatchSynchronizer.getInstance().addWorkState(rws);
		
		//4. 终端归属网关 关系更新
		try{
			String gateAddr = event.getClient().getPeerAddr();	//网关的前置机接口地址。
			if( ! gateAddr.equals(state.getActiveGprs()) ){
				state.setActiveGprs(gateAddr);
				updateState = true;
			}
			if( updateState )
				RealtimeSynchronizer.getInstance().setRtuState(state);
		}catch(Exception err){
			log.error("update activeGprs exp:"+err.getLocalizedMessage(),err);
		}

		RealtimeSynchronizer.getInstance().saveCommState(state);
		//5.2 原始报文保存,心跳不入库
		if(isSaveHeartbeat(bmsg.getLogicalAddress()) || !bmsg.isHeartbeat()){
			//如果保存心跳或者不是心跳，才保存
			asyncDbService.log2Db(bmsg);
		}
		
		msgQueue.offer(bmsg);
//		//6 从数据查询该终端当前有效bp地址
//			IServerSideChannel bpChannel=Msg2BpByMod.getInstance().getIdleChannel();
//			if ( null == bpChannel )
//				bpChannel = Msg2BpByMod.getInstance().getBpChannel(0);
//			if (bpChannel!=null){
//				MessageGate gateMsg = new MessageGate();
//				gateMsg.setUpInnerMessage(bmsg);
//				boolean success = bpChannel.send(gateMsg);
//				if( !success ){
//					log.warn("send DLMS message to BP failed. meterId="+bmsg.getLogicalAddress());
//				}
//			}
//			else
//				log.warn("can not find  bp client of addr="+bmsg.getLogicalAddress() );
	}
	
	private void _handleZjMessage(MessageZj zjmsg,ReceiveMessageEvent event){
		if( log.isDebugEnabled() )
			log.debug("网关上行报文:"+zjmsg);
		/** 通信前置机收到网关上行报文，需要特殊处理
		 *  （1）心跳报文存MySQL；
		 *  （2）终端工况：流量
		 *  （3）更新终端所属的网关IP：port，与主站配置不一致处理
		 */
		//1. 从终端通信参数对象管理器找到终端参数对象
		RtuState  state = RealtimeSynchronizer.getInstance().getRtuState(zjmsg.getLogicalAddress());
		boolean updateState = false;
		if( null == state ){
			state = RealtimeSynchronizer.getInstance().loadFromDb(zjmsg.getLogicalAddress());
			updateState = true;
		}

		//2 & 3. 更新工况 。流量统计。心跳自动回复，所以长度*2,登录回复
		int flow = zjmsg.length();
		
		RtuWorkStateItem rws = new RtuWorkStateItem();
		rws.setIoTime(System.currentTimeMillis());
		rws.setRtua(state.getRtua());
		rws.setLen(flow);
		if( zjmsg.head.c_func == MessageConst.ZJ_FUNC_HEART ||
				zjmsg.head.c_func == MessageConst.ZJ_FUNC_LOGOUT ){
			rws.setFunc(RtuWorkStateItem.FUNC_HEART);
		}
		else if( zjmsg.head.c_func == MessageConst.ZJ_FUNC_LOGIN ){
			rws.setFunc(RtuWorkStateItem.FUNC_LOGIN);
		}
		else if( zjmsg.head.c_func == MessageConst.ZJ_FUNC_READ_TASK ){
			rws.setFunc(RtuWorkStateItem.FUNC_TASK);
		}
		else{
			if( zjmsg.head.c_func == MessageConst.ZJ_FUNC_EXP_ALARM )
				rws.setFunc(RtuWorkStateItem.FUNC_ALARM);
			else
				rws.setFunc(RtuWorkStateItem.FUNC_REPLY);
		}
		BatchSynchronizer.getInstance().addWorkState(rws);
		
		//4. 终端归属网关 关系更新
		try{
			String gateAddr = event.getClient().getPeerAddr();	//网关的前置机接口地址。
			if( ! gateAddr.equals(state.getActiveGprs()) ){
				state.setActiveGprs(gateAddr);
				updateState = true;
			}
			if( updateState )
				RealtimeSynchronizer.getInstance().setRtuState(state);
		}catch(Exception err){
			log.error("update activeGprs exp:"+err.getLocalizedMessage(),err);
		}

		
		/*//5.1 心跳报文保存
		if( zjmsg.head.c_func == MessageConst.ZJ_FUNC_HEART ){
			//产生心跳保存业务事件
			if(WsGlobalMap.getInstance().isSaveHeartbeat2Db(zjmsg.getRtua()))
				asyncDbService.log2Db(zjmsg);
			return;
		}*/
		//5.2 原始报文保存,心跳不入库
		RealtimeSynchronizer.getInstance().saveCommState(state);

		if(zjmsg.isHeartbeat() && !zjmsg.isLogin()){
			//如果是心跳帧，需要保存心跳
			if(isSaveHeartbeat(zjmsg.getLogicalAddress())){
				asyncDbService.log2Db(zjmsg);
			}
		}else{
			asyncDbService.log2Db(zjmsg);			
		}
		
		//6. 厂家自定义报文，需要直接发送给厂家。不能按照目前的主动取这个模式。
		if( zjmsg.head.c_func == MessageConst.ZJ_FUNC_USER_DEFINE ){
			MessageGate gatemsg = new MessageGate();
			gatemsg.setUpInnerMessage(zjmsg);
			intfClient.sendMessage(gatemsg);
		}
		else
			msgQueue.offer(zjmsg);
	}
	private void _handleDlmsMessage(DlmsMessage dlmsmsg,ReceiveMessageEvent event){
		if( log.isDebugEnabled() )
			log.debug("Gate Up Message:"+dlmsmsg);
		/** 通信前置机收到网关上行报文，需要特殊处理
		 *  （1）心跳报文存MySQL；
		 *  （2）终端工况：流量
		 *  （3）更新终端所属的网关IP：port，与主站配置不一致处理
		 */
		//1. 从终端通信参数对象管理器找到终端参数对象
		RtuState  state = RealtimeSynchronizer.getInstance().getRtuState(dlmsmsg.getLogicalAddress());
		boolean updateState = false;
		if( null == state ){
			state = RealtimeSynchronizer.getInstance().loadFromDb(dlmsmsg.getLogicalAddress());
			updateState = true;
		}
		
		//2 & 3. 更新工况 。流量统计。心跳自动回复，所以长度*2,登录回复
		int flow = dlmsmsg.length();
		
		RtuWorkStateItem rws = new RtuWorkStateItem();
		rws.setLastCommunicationIp(dlmsmsg.getPeerAddr());
		rws.setIoTime(System.currentTimeMillis());
		rws.setRtua(state.getRtua());
		rws.setLen(flow);
		
		if(dlmsmsg.isHeartbeat()){
			rws.setFunc(RtuWorkStateItem.FUNC_DLMS_HEART);
		}else if(dlmsmsg.isAA()){
			rws.setFunc(RtuWorkStateItem.FUNC_DLMS_AA);
		}else if(dlmsmsg.isEventNeedReply()){
			rws.setFunc(RtuWorkStateItem.FUNC_DLMS_NEED_CFM);
		}else{
			rws.setFunc(RtuWorkStateItem.FUNC_REPLY);
		}
		BatchSynchronizer.getInstance().addWorkState(rws);
		
		//4. 终端归属网关 关系更新
		try{
			String gateAddr = event.getClient().getPeerAddr();	//网关的前置机接口地址。
			if( ! gateAddr.equals(state.getActiveGprs()) ){
				state.setActiveGprs(gateAddr);
				updateState = true;
			}
			if( updateState )
				RealtimeSynchronizer.getInstance().setRtuState(state);
		}catch(Exception err){
			log.error("update activeGprs exp:"+err.getLocalizedMessage(),err);
		}
		
		RealtimeSynchronizer.getInstance().saveCommState(state);
		
		//5.2 原始报文保存,心跳不入库
		if(isSaveHeartbeat(dlmsmsg.getLogicalAddress()) || !dlmsmsg.isHeartbeat()){
			//如果保存心跳或者不是心跳，才保存
			asyncDbService.log2Db(dlmsmsg);
		}
		
		
		msgQueue.offer(dlmsmsg);
//		//6 从数据查询该终端当前有效bp地址
//		IServerSideChannel bpChannel=Msg2BpByMod.getInstance().getIdleChannel();
//		if ( null == bpChannel )
//			bpChannel = Msg2BpByMod.getInstance().getBpChannel(0);
//		if (bpChannel!=null){
//			MessageGate gateMsg = new MessageGate();
//			gateMsg.setUpInnerMessage(dlmsmsg);
//			boolean success = bpChannel.send(gateMsg);
//			if( !success ){
//				log.warn("send DLMS message to BP failed. meterId="+dlmsmsg.getLogicalAddress());
//			}else{
//				if(log.isDebugEnabled())
//					log.debug("send to bp success."+"\n\t recvFrom:"+dlmsmsg.getPeerAddr()+"\t logicAddress:"+dlmsmsg.getLogicalAddress()+"\n\t msg:"+dlmsmsg);
//			}
//		}
//		else
//			log.warn("can not find  bp client of addr="+dlmsmsg.getLogicalAddress() );
	}	
	private void _handleGwMessage(MessageGw gwmsg,ReceiveMessageEvent event){
		if( log.isDebugEnabled() )
			log.debug("网关上行报文:"+gwmsg);
		/** 通信前置机收到网关上行报文，需要特殊处理
		 *  （1）心跳报文存MySQL；
		 *  （2）终端工况：流量
		 *  （3）更新终端所属的网关IP：port，与主站配置不一致处理
		 */
		//1. 从终端对象管理器找到终端对象
		//1. 从终端通信参数对象管理器找到终端参数对象
		RtuState  state = RealtimeSynchronizer.getInstance().getRtuState(gwmsg.getLogicalAddress());
		boolean updateState = false;
		if( null == state ){
			state = RealtimeSynchronizer.getInstance().loadFromDb(gwmsg.getLogicalAddress());
			updateState = true;
		}
			
		//2 & 3. 更新工况 。流量统计。心跳自动回复，所以长度*2,登录回复
		int flow = gwmsg.length();
		if(gwmsg.isNeedConfirm()){
			flow+=(20+WorkState.tcpIpLen);//对于需要确认的帧，要将确认帧的长度加上
		}
		RtuWorkStateItem rws = new RtuWorkStateItem();
		rws.setIoTime(System.currentTimeMillis());
		rws.setRtua(state.getRtua());
		rws.setLen(flow);
		
		byte afn = gwmsg.afn();
		if( gwmsg.isLogin()){
			rws.setFunc(RtuWorkStateItem.FUNC_GW_LOGIN);
		}else if( gwmsg.isHeartbeat() )
			rws.setFunc(RtuWorkStateItem.FUNC_GW_HEART);
		else if( afn == MessageConst.GW_FUNC_GET_DATA2 )
			rws.setFunc(RtuWorkStateItem.FUNC_TASK );
		else if( gwmsg.isNeedConfirm() )
			rws.setFunc(RtuWorkStateItem.FUNC_GW_NEED_CFM);
		else
			rws.setFunc(RtuWorkStateItem.FUNC_REPLY);
		BatchSynchronizer.getInstance().addWorkState(rws);
		
		//4. 终端归属网关 关系更新
		try{
			String gateAddr = event.getClient().getPeerAddr();	//网关的前置机接口地址。
			if( ! gateAddr.equals(state.getActiveGprs()) ){
				state.setActiveGprs(gateAddr);
				updateState = true;
				
			}
			if( updateState ){				
				RealtimeSynchronizer.getInstance().setRtuState(state);
			}
		}catch(Exception err){
			log.error("update activeGprs exp:"+err.getLocalizedMessage(),err);
		}	
		
		/*//5.1 心跳报文保存
		if( gwmsg.afn() == MessageConst.GW_FUNC_HEART ){
			//产生心跳保存业务事件
			if(WsGlobalMap.getInstance().isSaveHeartbeat2Db(gwmsg.getRtua()))
				asyncDbService.log2Db(gwmsg);
			return;
		}*/
		RealtimeSynchronizer.getInstance().saveCommState(state);
		//5.2 原始报文保存,心跳不入库
		if(gwmsg.isHeartbeat() && !gwmsg.isLogin()){
			//如果是心跳帧，需要保存心跳
			if(isSaveHeartbeat(gwmsg.getLogicalAddress())){
				asyncDbService.log2Db(gwmsg);
			}
		}else{
			asyncDbService.log2Db(gwmsg);			
		}
		
		//6. 厂家自定义报文，需要直接发送给厂家。不能按照目前的主动取这个模式。
//		if( gwmsg.afn() == MessageConst.GW_FUNC_FILE ){
//			MessageGate gatemsg = new MessageGate();
//			gatemsg.setUpInnerMessage(gwmsg);
//			intfClient.sendMessage(gatemsg);
//		}
//		else
			msgQueue.offer(gwmsg);
	}
	private boolean isSaveHeartbeat(String logicalAddress) {
		return WsGlobalMap.getInstance().isSaveHeartbeat2Db(logicalAddress);
	}

	/**
	 * 是否保存报文
	 * @param gwmsg
	 * @return
	 */
//	private boolean isSaveHeartbeat() {
//		String isSave=System.getProperty("fe.saveHearBeat");
//		if(isSave==null || "".equals(isSave)) return true;
//		return Boolean.parseBoolean(isSave);
//	}
	private void onSendMessage(SendMessageEvent e){
		IMessage message = e.getMessage();
		IMessage rtuMsg = null;
		if( message.getMessageType() == MessageType.MSG_GATE ){
			MessageGate gateMsg = (MessageGate)message;
			//增加支持客户端请求报文功能。服务器不主动往client发送报文。HREQ还起到心跳报文作用。
			if( gateMsg.getHead().getCommand() == MessageGate.CMD_GATE_HREQ ){
				//客户端请求的报文数量的应答
				return;
			}
			else if(gateMsg.getHead().getCommand() == MessageGate.CMD_GATE_REQUEST ){
				rtuMsg = gateMsg.getInnerMessage();
				rtuMsg.setTxfs(gateMsg.getTxfs());
				rtuMsg.setIoTime(gateMsg.getIoTime());
				rtuMsg.setSource(gateMsg.getSource());
				rtuMsg.setServerAddress(gateMsg.getHead().getAttributeAsString(GateHead.ATT_DESTADDR));
			}
			else
				return;
		}
		else if( message.getMessageType() == MessageType.MSG_ZJ || message.getMessageType() == MessageType.MSG_GW_10|| message.getMessageType() == MessageType.MSG_DLMS ||message.getMessageType()==MessageType.MSG_BENGAL ){
			rtuMsg = message;
		}
		if( null == rtuMsg )
			return;
		
		//2. 流量统计。
		int flow = rtuMsg.length();
		RtuState  state = RealtimeSynchronizer.getInstance().getRtuState(rtuMsg.getLogicalAddress());
		if( null == state ){
			state = RealtimeSynchronizer.getInstance().loadFromDb(rtuMsg.getLogicalAddress());
			RealtimeSynchronizer.getInstance().setRtuState(state);
		}
		
		RtuWorkStateItem rws = new RtuWorkStateItem();
		rws.setIoTime(System.currentTimeMillis());
		rws.setRtua(state.getRtua());
		rws.setLen(flow);
		byte func = RtuWorkStateItem.FUNC_DOWN_REQ;
		if( rtuMsg instanceof MessageZj ){
			MessageZj zjmsg = (MessageZj)rtuMsg;
			if( zjmsg.head.c_func == MessageConst.ZJ_FUNC_ALARM_CONFIRM )
				func = RtuWorkStateItem.FUNC_DOWN_CFM;
		}
		
		rws.setFunc( func );
		BatchSynchronizer.getInstance().addWorkState(rws);
		
		//3. 原始报文保存，注意，如果数据库繁忙，原始报文可能会丢弃。
		if( null != asyncDbService ){
			if(rtuMsg instanceof DlmsMessage){
				((DlmsMessage) rtuMsg).setDirection(IMessage.DIRECTION_DOWN);
			}if(rtuMsg instanceof AnsiMessage){
				((AnsiMessage) rtuMsg).setDirection(IMessage.DIRECTION_DOWN);
			}
			asyncDbService.log2Db(rtuMsg);
		}
	}

	public void setMsgQueue(FEMessageQueue msgQueue) {
		this.msgQueue = msgQueue;
	}

	public void setHeartBeat(Object heartBeat) {
//		this.heartBeat = (HeartBeatMessage)heartBeat;
	}
	public final void setAsyncDbService(AsyncService asyncDbService) {
		this.asyncDbService = asyncDbService;
	}
	public void setIntfClient(ClientModule intfClient) {
		this.intfClient = intfClient;
	}
	public void setMasterDbService(MasterDbService masterDbService) {
//		this.masterDbService = masterDbService;
	}
	
}
