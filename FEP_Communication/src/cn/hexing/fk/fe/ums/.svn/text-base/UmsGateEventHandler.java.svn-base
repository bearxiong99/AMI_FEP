package cn.hexing.fk.fe.ums;

import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import cn.hexing.db.batch.AsyncService;
import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.common.spi.IEventHandler;
import cn.hexing.fk.fe.cluster.BatchSynchronizer;
import cn.hexing.fk.fe.cluster.RealtimeSynchronizer;
import cn.hexing.fk.fe.cluster.RtuState;
import cn.hexing.fk.fe.cluster.RtuWorkStateItem;
import cn.hexing.fk.fe.msgqueue.FEMessageQueue;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.MessageConst;
import cn.hexing.fk.message.MessageType;
import cn.hexing.fk.message.gate.GateHead;
import cn.hexing.fk.message.gate.MessageGate;
import cn.hexing.fk.message.gw.MessageGw;
import cn.hexing.fk.message.zj.MessageZj;
import cn.hexing.fk.sockserver.event.ReceiveMessageEvent;
import cn.hexing.fk.sockserver.event.SendMessageEvent;

/**
 * UMS Gate Client Module's event-handler
 *
 */
public class UmsGateEventHandler implements IEventHandler {
	private static final Logger log = Logger.getLogger(UmsGateEventHandler.class);
	private FEMessageQueue msgQueue;	//spring 配置实现。
	private AsyncService asyncDbService;		//用于批量保存原始报文
	
	public void handleEvent(IEvent event) {
		try{
			if( event.getType().equals(EventType.MSG_RECV) )
				onRecvMessage( (ReceiveMessageEvent)event);
			else if( event.getType().equals(EventType.MSG_SENT) )
				onSendMessage( (SendMessageEvent)event );
		}catch(Throwable e){
			log.error("UmsGateEventHandler exp",e);
		}
	}
	
	/**
	 * Receive UMS up-coming message.
	 * @param e
	 */
	private void onRecvMessage(ReceiveMessageEvent e){
		IMessage msg0 = e.getMessage();
		if( !( msg0 instanceof MessageGate ))
			return;
		MessageGate gatemsg = (MessageGate)msg0;
		if( gatemsg.isHeartbeat() ){
			if( log.isDebugEnabled() )
				log.debug(gatemsg);
			return;
		}
		else if( gatemsg.getHead().getCommand() == MessageGate.CMD_GATE_REPLY ){
			IMessage msg = gatemsg.getInnerMessage();
			String umsId = gatemsg.getHead().getAttributeAsString(GateHead.ATT_DESTADDR);
			String simNo = gatemsg.getHead().getAttributeAsString(GateHead.ATT_SRCADDR);
			if( log.isDebugEnabled() )
				log.debug("UMS Gate receive: "+msg);

			RtuState  state = RealtimeSynchronizer.getInstance().getRtuState(msg.getLogicalAddress());
			boolean updateState = false;
			if( null == state ){
				state = RealtimeSynchronizer.getInstance().loadFromDb(msg.getLogicalAddress());
				updateState = true;
			}
			//更新UMS有效通道。
			if( StringUtils.hasText(umsId) ){
				try{
					String appid = umsId.substring(5, 9);
					if( !appid.equals(state.getActiveUms())){
						updateState = true;
						state.setActiveUms(appid);
					}
					String subid = null;
					if( umsId.length()>9 )
						subid = umsId.substring(9).trim();
					if( StringUtils.hasText(subid) && ! subid.equals(state.getActiveSubAppId())){
						updateState = true;
						state.setActiveSubAppId(subid);
					}
					if( StringUtils.hasText(simNo) && ! simNo.equals(state.getSimNum())){
						updateState = true;
						state.setSimNum(simNo);
					}
				}catch(Throwable exp){
					log.error("parse up-coming ums channel id exp:",exp);
				}
			}
			if( updateState )
				RealtimeSynchronizer.getInstance().setRtuState(state);

			if( msg instanceof MessageZj)
				_handleZjMessage((MessageZj)msg,umsId,simNo);
			else if( msg instanceof MessageGw )
				_handleGwMessage((MessageGw)msg,umsId,simNo);
			else
				log.warn("UmsGate receive un-supported message type.");
		}
		else{
			log.warn("UmsGateEventHandler receive unsupported message:"+msg0);
		}
	}
	
	private void _handleZjMessage(MessageZj zjmsg, String umsId, String simNo){
		//流量统计
		int flow = zjmsg.length();
		
		RtuWorkStateItem rws = new RtuWorkStateItem();
		rws.setIoTime(System.currentTimeMillis());
		rws.setRtua(zjmsg.getLogicalAddress());
		rws.setTxfs(RtuWorkStateItem.TXFS_SMS);
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
		
		// 原始报文保存,心跳不入库
		if( null != asyncDbService )
			asyncDbService.log2Db(zjmsg);
		msgQueue.offer(zjmsg);
	}
	
	private void _handleGwMessage(MessageGw gwmsg, String umsId, String simNo){
		int flow = gwmsg.length();
		
		RtuWorkStateItem rws = new RtuWorkStateItem();
		rws.setIoTime(System.currentTimeMillis());
		rws.setTxfs(RtuWorkStateItem.TXFS_SMS);
		rws.setRtua(gwmsg.getLogicalAddress());
		rws.setLen(flow);
		
		byte afn = gwmsg.afn();
		if( afn == MessageConst.GW_FN_HEART )
			rws.setFunc(RtuWorkStateItem.FUNC_GW_HEART);
		else if( afn == MessageConst.GW_FUNC_GET_DATA2 )
			rws.setFunc(RtuWorkStateItem.FUNC_TASK );
		else if( gwmsg.isNeedConfirm() )
			rws.setFunc(RtuWorkStateItem.FUNC_GW_NEED_CFM);
		else
			rws.setFunc(RtuWorkStateItem.FUNC_REPLY);
		BatchSynchronizer.getInstance().addWorkState(rws);
		
		// 原始报文保存,心跳不入库
		if( null != asyncDbService )
			asyncDbService.log2Db(gwmsg);
		msgQueue.offer(gwmsg);
	}

	/**
	 * send message to UMS gate successfully.
	 * @param e
	 */
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
				rtuMsg.setServerAddress(gateMsg.getHead().getAttributeAsString(GateHead.ATT_SRCADDR));
			}
			else
				return;
		}
		else if( message.getMessageType() == MessageType.MSG_ZJ ){
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
		rws.setTxfs(RtuWorkStateItem.TXFS_SMS);
		rws.setFunc(RtuWorkStateItem.FUNC_DOWN_REQ);
		BatchSynchronizer.getInstance().addWorkState(rws);
		
		//3. 原始报文保存，注意，如果数据库繁忙，原始报文可能会丢弃。
		if( null != asyncDbService )
			asyncDbService.log2Db(rtuMsg);
	}

	public void setMsgQueue(FEMessageQueue msgQueue) {
		this.msgQueue = msgQueue;
	}

	public void setAsyncDbService(AsyncService asyncDbService) {
		this.asyncDbService = asyncDbService;
	}
}
