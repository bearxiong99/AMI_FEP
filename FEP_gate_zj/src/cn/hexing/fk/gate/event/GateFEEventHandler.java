/**
 * ǰ�û����Ӷ�Tcp Socket�������¼�������
 * ���ܸ�����
 * 		�����յ�ǰ�û����б��ġ��ն����б��ķ��ͳɹ��¼���
 *    ǰ�û����б���ͨ��MessageQueue���󷽷�ֱ�ӷ��͸��նˣ�
 *    �ն����б��ĳɹ��¼����򵥴�ӡ��־���޽�һ����������
 * ����ʵ�֣�
 * BasicEventHook�����ࡣ
 * override handleEvent���������ReceiveMessageEvent��SendMessageEvent�ر���
 * ע�������spring�����ļ��У�source�������������ǰ�û��˷���ӿڵ�SocketServer����
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
	//private boolean noConvert = false;		//�Ƿ�ֱ�������㽭��Լԭʼ����.
	
	public boolean start() {
		return super.start();
	}

	public void setQueue(MessageQueue queue) {
		this.queue = queue;
	}
	
	/**
	 * ���ظ÷�����
	 */
	public void handleEvent(IEvent e) {
		/** ����ǰ�û��˷����յ����ģ�����ֱ�ӷ��͸��նˡ�
		 *  ����ǰ�û�����Accept��client���͡���Ҫ�ŵ�ǰ�û����ж��С�
		 *  	1����ǰ�û����ӵ����أ�֪ͨ���ж��з������б��ģ�
		 *  	2����ǰ�û���Ӧclient�ɹ��������б��ģ�֪ͨ���ж��м������ͣ�
		 */
		boolean processed = false;
		MessageGate msgGate =null;
		if( e.getType() == EventType.MSG_RECV ){
			//�������ع�Լ���ģ���Ҫת�����㽭��Լ���ſ��Է��͸��㽭�նˡ�
			IMessage msg = e.getMessage();
			if( msg.getMessageType() == MessageType.MSG_GATE ){
				MessageGate mgate = (MessageGate)msg;
				msgGate=mgate;
				//����֧�ֿͻ��������Ĺ��ܡ���������������client���ͱ��ġ�HREQ���������������á�
				short cmd = mgate.getHead().getCommand();
				if( cmd == MessageGate.CMD_GATE_HREQ ){
					//��ȡ�ͻ�������ı�������
					ByteBuffer data = mgate.getData();
					int numPackets = data.remaining()<4 ? -1 : data.getInt()+1;
					IServerSideChannel client = (IServerSideChannel)msg.getSource();
					synchronized(client){
						client.setRequestNum(numPackets);
					}
					//Ӧ������
					MessageGate hreply = MessageGate.createHReply();
					client.send(hreply);
					if( trace.isEnabled() )
						trace.trace( "�յ��ͻ��������ģ�requestNum="+numPackets );
					return;		//�����������
				}
				else if( cmd == MessageGate.CMD_GATE_REQUEST || cmd == MessageGate.CMD_WRAP ){
					msg = mgate.getInnerMessage();
					if( null != msg && log.isDebugEnabled() )
						log.debug("FEP Down Message:"+msg);
				}
				else if( cmd == MessageGate.REQ_MONITOR_RELAY_PROFILE ){
					//ǰ�û��������ص�profile
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
							//�Ⲧģʽ
							if( msg.getMessageType() == MessageType.MSG_DLMS ){
								queue.sendDownMessage(msg, msgGate);
							}
						}else if(msgGate.linkMode==IMessage.COMMUNICATION_TYPE_CSD){
							//CSDģʽ
							CSDMessageQueue.getInstance().offerDownMessage(msgGate);
							if(CSDMessageQueue.getInstance().feQueue==null)
								CSDMessageQueue.getInstance().feQueue=queue;
						}
					}else{
						if( msg.getMessageType() == MessageType.MSG_ZJ ){
							//���������ļ��ĸ߿��ն�������Ҫ����ǰ���ַ�
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
			//���ԣ������Զ�Ӧ��ԭ��Ϣ���ء�
			processed = ! processed ;
		}
		else if( e.getType() == EventType.MSG_SENT ){
			IMessage msg = e.getMessage();
			if( log.isDebugEnabled() )
				log.debug("Send Msg To Fe Success:"+msg);
			IServerSideChannel client = (IServerSideChannel)msg.getSource();
			//���client�����������Ƿ�ݼ���0��
			int numReq = client.getRequestNum();
			if( numReq == 0 ){
				//���ܷ��͡�
				if( trace.isEnabled() )
					trace.trace( "client requestNum==0, msg="+msg );
				return;
			}
			msg = queue.pollUpMessage();
			
			if(trace.isEnabled()){
				if( null != msg )
					trace.trace("remaining sendable message��"+ numReq+",current msg="+msg );
				else 
					trace.trace("remaining sendable message��"+ numReq+",no up message." );				
			}

			if( null != msg ){
				if( queue.isNoConvert() ){ //�Ƿ���Ҫת��Ϊ��汨��
					if( !client.send(msg) )
						queue.offerUpMessageInQueue(msg);
				}
				else{
					//���㽭��Լ����ת�������ع�Լ�����͸�ǰ�û���
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
