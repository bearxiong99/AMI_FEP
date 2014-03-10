/**
 * 前置机连接端Tcp Socket服务器事件处理器
 * 功能概述：
 * 		侦听收到前置机下行报文、终端上行报文发送成功事件。
 *    前置机下行报文通过MessageQueue对象方法直接发送给终端；
 *    终端上行报文成功事件，简单打印日志。无进一步处理需求。
 * 技术实现：
 * BasicEventHook派生类。
 * override handleEvent方法，针对ReceiveMessageEvent和SendMessageEvent特别处理。
 * 注意事项：在spring配置文件中，source对象必须是网关前置机端服务接口的SocketServer对象。
 */
package cn.hexing.fk.gate.event;

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

import cn.hexing.fk.FasSystem;
import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.events.BasicEventHook;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.common.spi.socket.IServerSideChannel;
import cn.hexing.fk.gate.CSDMessageQueue;
import cn.hexing.fk.gate.MessageQueue;
import cn.hexing.fk.gate.PrefixRtuManage;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.MessageType;
import cn.hexing.fk.message.gate.GateHead;
import cn.hexing.fk.message.gate.MessageGate;
import cn.hexing.fk.message.zj.MessageZj;
import cn.hexing.fk.sockserver.event.AcceptEvent;
import cn.hexing.fk.sockserver.event.ClientCloseEvent;
import cn.hexing.fk.tracelog.TraceLog;

/**
 */
public class GateFEEventHandler extends BasicEventHook {
	private static final Logger log = Logger.getLogger(GateFEEventHandler.class);
	private static final TraceLog trace = TraceLog.getTracer(GateFEEventHandler.class);
	private MessageQueue queue;
	//private boolean noConvert = false;		//是否直接上行浙江规约原始报文.
	
	public boolean start() {
		return super.start();
	}

	public void setQueue(MessageQueue queue) {
		this.queue = queue;
	}
	
	/**
	 * 重载该方法。
	 */
	public void handleEvent(IEvent e) {
		/** 网关前置机端服务收到报文，必须直接发送给终端。
		 *  网关前置机服务Accept的client发送。需要放到前置机上行队列。
		 *  	1）当前置机连接到网关，通知上行队列发送上行报文；
		 *  	2）当前置机对应client成功发送上行报文，通知上行队列继续发送；
		 */
		boolean processed = false;
		MessageGate msgGate =null;
		if( e.getType() == EventType.MSG_RECV ){
			//对于网关规约报文，需要转换成浙江规约，才可以发送给浙江终端。
			IMessage msg = e.getMessage();
			if( msg.getMessageType() == MessageType.MSG_GATE ){
				MessageGate mgate = (MessageGate)msg;
				msgGate=mgate;
				//增加支持客户端请求报文功能。服务器不主动往client发送报文。HREQ还起到心跳报文作用。
				short cmd = mgate.getHead().getCommand();
				if( cmd == MessageGate.CMD_GATE_HREQ ){
					//读取客户端请求的报文数量
					ByteBuffer data = mgate.getData();
					int numPackets = data.remaining()<4 ? -1 : data.getInt()+1;
					IServerSideChannel client = (IServerSideChannel)msg.getSource();
					synchronized(client){
						client.setRequestNum(numPackets);
					}
					//应答请求
					MessageGate hreply = MessageGate.createHReply();
					client.send(hreply);
					if( trace.isEnabled() )
						trace.trace( "收到客户端请求报文，requestNum="+numPackets );
					return;		//心跳处理结束
				}
				else if( cmd == MessageGate.CMD_GATE_REQUEST || cmd == MessageGate.CMD_WRAP ){
					msg = mgate.getInnerMessage();
					if( null != msg && log.isDebugEnabled() )
						log.debug("FEP Down Message:"+msg);
				}
				else if( cmd == MessageGate.REQ_MONITOR_RELAY_PROFILE ){
					//前置机请求网关的profile
					String profile = FasSystem.getFasSystem().getProfile();
					MessageGate repMoniteProfile = MessageGate.createMoniteProfileReply(profile);
					queue.offerUpMessageInQueue(repMoniteProfile);
					return;
				}
				else {
					processed = ! processed ;
				}
			}
			try{
				if( null != msg ){
					boolean isNormalLink = false;
					if(msgGate==null){
						isNormalLink = true;
					}else{
						int txfs=msgGate.getHead().getAttributeAsInt(GateHead.ATT_TXFS);
						if(txfs==0){
							isNormalLink =true;
						}else{
							msgGate.linkMode=txfs;
							isNormalLink =false;
						}
						
					}
					if(!isNormalLink){
						if(msgGate.linkMode==IMessage.COMMUNICATION_TYPE_EXTERNAL){
							//外拨模式
							if( msg.getMessageType() == MessageType.MSG_DLMS ){
								queue.sendDownMessage(msg, msgGate);
							}
						}else if(msgGate.linkMode==IMessage.COMMUNICATION_TYPE_CSD){
							//CSD模式
							CSDMessageQueue.getInstance().offerDownMessage(msgGate);
							if(CSDMessageQueue.getInstance().feQueue==null)
								CSDMessageQueue.getInstance().feQueue=queue;
						}
					}else{
						if( msg.getMessageType() == MessageType.MSG_ZJ ){
							//本地配置文件的高科终端下行需要增加前导字符
							MessageZj zjmsg = (MessageZj)msg;
							zjmsg.setPrefix(PrefixRtuManage.getInstance().getRtuPrefix(zjmsg.head.rtua));
							queue.sendDownMessage(msg);
						}
						else if( msg.getMessageType() == MessageType.MSG_GW_10 )
							queue.sendDownMessage(msg);
						else if( msg.getMessageType() == MessageType.MSG_DLMS ){													
							queue.sendDownMessage(msg);									
						}
						else if( msg.getMessageType() == MessageType.MSG_ANSI ){
							queue.sendDownMessage(msg);
						}
						else if( msg.getMessageType() == MessageType.MSG_BENGAL ){
							if(queue.sendDownMessage(msg) && log.isInfoEnabled()){
								log.info("send Bengal msg success");
							}
						}
					}
				}
			}catch(Exception exp){
				log.warn(exp.getLocalizedMessage(),exp);
			}
			//测试：生成自动应答，原消息返回。
			processed = ! processed ;
		}
		else if( e.getType() == EventType.MSG_SENT ){
			IMessage msg = e.getMessage();
			if( log.isDebugEnabled() )
				log.debug("Send Msg To Fe Success:"+msg);
			IServerSideChannel client = (IServerSideChannel)msg.getSource();
			//检测client的请求数量是否递减至0？
			int numReq = client.getRequestNum();
			if( numReq == 0 ){
				//不能发送。
				if( trace.isEnabled() )
					trace.trace( "client requestNum==0, msg="+msg );
				return;
			}
			msg = queue.pollUpMessage();
			
			if(trace.isEnabled()){
				if( null != msg )
					trace.trace("remaining sendable message＝"+ numReq+",current msg="+msg );
				else 
					trace.trace("remaining sendable message＝"+ numReq+",no up message." );				
			}

			if( null != msg ){
				if( queue.isNoConvert() ){ //是否需要转换为浙规报文
					if( !client.send(msg) )
						queue.offerUpMessageInQueue(msg);
				}
				else{
					//把浙江规约报文转换成网关规约，发送给前置机。
					MessageGate gateMsg = new MessageGate();
					gateMsg.setUpInnerMessage(msg);
					if( !client.send(gateMsg))
						queue.offerUpMessageInQueue(msg);				
				}
			}
			processed = true;
		}
		else if( e.getType() == EventType.ACCEPTCLIENT ){
			AcceptEvent ae = (AcceptEvent)e;
			queue.onFrontEndConnected(ae.getClient());
			processed = true;
		}
		else if( e.getType() == EventType.CLIENTCLOSE ){
			ClientCloseEvent ce = (ClientCloseEvent)e;
			queue.onFrontEndClosed(ce.getClient());
		}
		if( !processed )
			super.handleEvent(e);
	}
}
