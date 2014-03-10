/**
 * 用于业务处理器与通信前置机之间报文收发事件处理。
 * 上行报文进入优先级队列。
 */
package cn.hexing.fk.bp.feclient;

import org.apache.log4j.Logger;

import cn.hexing.fk.FasSystem;
import cn.hexing.fk.bp.ansi.AnsiEventProcessor;
import cn.hexing.fk.bp.dlms.DlmsEventProcessor;
import cn.hexing.fk.bp.msgqueue.BPMessageQueue;
import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.common.spi.IEventHandler;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.MessageType;
import cn.hexing.fk.message.bengal.BengalMessage;
import cn.hexing.fk.message.gate.GateHead;
import cn.hexing.fk.message.gate.MessageGate;
import cn.hexing.fk.message.zj.MessageZj;
import cn.hexing.fk.sockserver.event.ReceiveMessageEvent;
import cn.hexing.fk.sockserver.event.SendMessageEvent;

import com.hx.ansi.message.AnsiMessage;
import com.hx.dlms.message.DlmsMessage;


/**
 *
 */
public class FEMessageEventHandler implements IEventHandler {
	private static final Logger log = Logger.getLogger(FEMessageEventHandler.class);	
	private BPMessageQueue msgQueue;	//spring 配置实现。
	
	public void handleEvent(IEvent event) {
		if( event.getType().equals(EventType.MSG_RECV) )
			onRecvMessage( (ReceiveMessageEvent)event);
		else if( event.getType().equals(EventType.MSG_SENT) )
			onSendMessage( (SendMessageEvent)event );
	}
	
	/**
	 * 收到通信前置机的上行报文。
	 * @param e
	 */
	private void onRecvMessage(ReceiveMessageEvent e){
		IMessage msg = e.getMessage();
		IMessage message = null;
//		if (log.isDebugEnabled())
//			log.debug("收到通信前置机的上行报文:"+msg.getRawPacketString());
		if( msg.getMessageType() == MessageType.MSG_GATE ){
			MessageGate mgate = (MessageGate)msg;
			//增加支持客户端请求报文功能。服务器不主动往client发送报文。HREQ还起到心跳报文作用。
			if( mgate.getHead().getCommand() == MessageGate.CMD_GATE_HREPLY ){
				//log.info("rev fe heartbeat msg");
				/*long time=System.currentTimeMillis()-msgQueue.getLastReceiveTime();
				long tasktime=System.currentTimeMillis()-msgQueue.getLastHandleDataTime();
				if(time>=1000*900){
					log.info(time+"ms have no upMessage,restart client!");
					msgQueue.getClient().restart();	
					msgQueue.setLastReceiveTime(System.currentTimeMillis());
					return;	
				}				
				if(tasktime>=1000*1800){
					log.info(time+"ms have no upTaskMessage,restart client!");
					msgQueue.getClient().restart();	
					msgQueue.setLastHandleDataTime(System.currentTimeMillis());
				}*/
				//客户端请求的报文数量的应答
				return;		//心跳处理结束
			}
			else if(mgate.getHead().getCommand() == MessageGate.CMD_GATE_REPLY ){
				message = mgate.getInnerMessage();
				if (message.getMessageType()==MessageType.MSG_DLMS){
					DlmsMessage dmsg = (DlmsMessage)message;
					String logicalAddress = mgate.getHead().getAttributeAsString(GateHead.ATT_LOGICALADDR);
					dmsg.setLogicalAddress(logicalAddress);
					if(dmsg.isHeartbeat()){
						DlmsEventProcessor.getInstance().postUpDlmsMessage(dmsg);
					}else{
						msgQueue.offer(dmsg);
					}
					return;
				}
				else if(message.getMessageType()==MessageType.MSG_ANSI){
					AnsiMessage ansimessage=(AnsiMessage)message;
					String logicalAddress = mgate.getHead().getAttributeAsString(GateHead.ATT_LOGICALADDR);
					ansimessage.setLogicalAddress(logicalAddress);
					AnsiEventProcessor.getInstance().postUpAnsiMessage(ansimessage);
					return;
				}
				else if( message.getMessageType() == MessageType.MSG_BENGAL ){
					//Bangladesh message.
					BengalMessage bmsg = (BengalMessage)message;
					DlmsEventProcessor.getInstance().postUpBengalMessage(bmsg);
					return;
				}
				_handleMessage(message,e);
			}
			else if(mgate.getHead().getCommand() == MessageGate.CMD_GATE_SENDFAIL ){
				//GPRS网关下行失败，需要把请求通过短信通道下行到终端。
				message = mgate.getInnerMessage();
				if (message.getMessageType()==MessageType.MSG_ZJ){
					//把内部协议的发送失败报文转换为浙江规约发送失败报文。
					MessageZj zjmsg=(MessageZj)message;
					zjmsg = zjmsg.createSendFailReply();
					_handleMessage(zjmsg,e);
				}			
			}
			else if( mgate.getHead().getCommand() == MessageGate.REQ_MONITOR_RELAY_PROFILE ){
				//前置机请求网关的profile
				String profile = FasSystem.getFasSystem().getProfile();
				MessageGate repMoniteProfile = MessageGate.createMoniteProfileReply(profile);
				msgQueue.sendMessage(repMoniteProfile);
				return;
			}
			else if( mgate.getHead().getCommand() == MessageGate.CMD_RTU_CLOSE ){
				String logicalAddress = mgate.getHead().getAttributeAsString(GateHead.ATT_LOGICALADDR);
				log.warn("RTU_CLOSED: "+logicalAddress );
				DlmsEventProcessor.getInstance().postMeterChannelClosed(logicalAddress);
			}
			else {
				//其它类型命令
			}
		}
		else if( msg.getMessageType() == MessageType.MSG_ZJ ){
			_handleMessage((MessageZj)msg,e);
		}
	}
	
	private void _handleMessage(IMessage msg,ReceiveMessageEvent event){	
		//报文进行上行队列，以便发送给业务处理器。
		msgQueue.offer(msg);
		msgQueue.setLastReceiveTime(System.currentTimeMillis());
	}
	
	private void onSendMessage(SendMessageEvent e){
		IMessage msg = e.getMessage();
		if( msg.getMessageType() == MessageType.MSG_GATE ){
			MessageGate mgate = (MessageGate)msg;
			//增加支持客户端请求报文功能。服务器不主动往client发送报文。HREQ还起到心跳报文作用。
			if( mgate.getHead().getCommand() == MessageGate.CMD_GATE_HREQ ){
				//客户端请求的报文数量的应答
				return;
			}
			else if(mgate.getHead().getCommand() == MessageGate.CMD_GATE_REQUEST ){
				//zjmsg = mgate.getInnerMessage();
				if(log.isDebugEnabled()){
					IMessage inmsg = mgate.getInnerMessage();
					log.debug("send Message to FEP:"+inmsg+", meterid="+inmsg.getLogicalAddress());//log.debug("往通讯前置机发送下行:"+inmsg+", meterid="+inmsg.getLogicalAddress());					
				}
			}
			else
				return;
		}
		else if( msg.getMessageType() == MessageType.MSG_ZJ ){
			//zjmsg = (MessageZj)msg;
		}
//		if( null == msg )
//			return;
//		if (log.isDebugEnabled())
//			log.debug("往通讯前置机发送下行:"+msg.getRawPacketString());				
	}

	public void setMsgQueue(BPMessageQueue msgQueue) {
		this.msgQueue = msgQueue;
	}
}
