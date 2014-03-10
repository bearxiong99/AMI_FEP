/**
 * ����ҵ��������ͨ��ǰ�û�֮�䱨���շ��¼�����
 * ���б��Ľ������ȼ����С�
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
	private BPMessageQueue msgQueue;	//spring ����ʵ�֡�
	
	public void handleEvent(IEvent event) {
		if( event.getType().equals(EventType.MSG_RECV) )
			onRecvMessage( (ReceiveMessageEvent)event);
		else if( event.getType().equals(EventType.MSG_SENT) )
			onSendMessage( (SendMessageEvent)event );
	}
	
	/**
	 * �յ�ͨ��ǰ�û������б��ġ�
	 * @param e
	 */
	private void onRecvMessage(ReceiveMessageEvent e){
		IMessage msg = e.getMessage();
		IMessage message = null;
//		if (log.isDebugEnabled())
//			log.debug("�յ�ͨ��ǰ�û������б���:"+msg.getRawPacketString());
		if( msg.getMessageType() == MessageType.MSG_GATE ){
			MessageGate mgate = (MessageGate)msg;
			//����֧�ֿͻ��������Ĺ��ܡ���������������client���ͱ��ġ�HREQ���������������á�
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
				//�ͻ�������ı���������Ӧ��
				return;		//�����������
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
				//GPRS��������ʧ�ܣ���Ҫ������ͨ������ͨ�����е��նˡ�
				message = mgate.getInnerMessage();
				if (message.getMessageType()==MessageType.MSG_ZJ){
					//���ڲ�Э��ķ���ʧ�ܱ���ת��Ϊ�㽭��Լ����ʧ�ܱ��ġ�
					MessageZj zjmsg=(MessageZj)message;
					zjmsg = zjmsg.createSendFailReply();
					_handleMessage(zjmsg,e);
				}			
			}
			else if( mgate.getHead().getCommand() == MessageGate.REQ_MONITOR_RELAY_PROFILE ){
				//ǰ�û��������ص�profile
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
				//������������
			}
		}
		else if( msg.getMessageType() == MessageType.MSG_ZJ ){
			_handleMessage((MessageZj)msg,e);
		}
	}
	
	private void _handleMessage(IMessage msg,ReceiveMessageEvent event){	
		//���Ľ������ж��У��Ա㷢�͸�ҵ��������
		msgQueue.offer(msg);
		msgQueue.setLastReceiveTime(System.currentTimeMillis());
	}
	
	private void onSendMessage(SendMessageEvent e){
		IMessage msg = e.getMessage();
		if( msg.getMessageType() == MessageType.MSG_GATE ){
			MessageGate mgate = (MessageGate)msg;
			//����֧�ֿͻ��������Ĺ��ܡ���������������client���ͱ��ġ�HREQ���������������á�
			if( mgate.getHead().getCommand() == MessageGate.CMD_GATE_HREQ ){
				//�ͻ�������ı���������Ӧ��
				return;
			}
			else if(mgate.getHead().getCommand() == MessageGate.CMD_GATE_REQUEST ){
				//zjmsg = mgate.getInnerMessage();
				if(log.isDebugEnabled()){
					IMessage inmsg = mgate.getInnerMessage();
					log.debug("send Message to FEP:"+inmsg+", meterid="+inmsg.getLogicalAddress());//log.debug("��ͨѶǰ�û���������:"+inmsg+", meterid="+inmsg.getLogicalAddress());					
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
//			log.debug("��ͨѶǰ�û���������:"+msg.getRawPacketString());				
	}

	public void setMsgQueue(BPMessageQueue msgQueue) {
		this.msgQueue = msgQueue;
	}
}
