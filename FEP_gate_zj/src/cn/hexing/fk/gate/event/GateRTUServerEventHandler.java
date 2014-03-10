/**
 * ���ܸ�����
 * 		�����յ��ն����б��ġ����б��ķ��ͳɹ��¼���
 *    �ն����б��Ľ���MessageQueue���У��Ա㷢�͸�ǰ�û���
 *    �ն����б��ĳɹ��¼����򵥴�ӡ��־���޽�һ����������
 * ����ʵ�֣�
 * SimpleEventHandler�����ࡣ
 * override handleEvent���������ReceiveMessageEvent��SendMessageEvent�ر���
 * ע�������spring�����ļ��У�source��������������ն˽ӿڵ�SocketServer����
 */
package cn.hexing.fk.gate.event;

import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.common.spi.IEventHandler;
import cn.hexing.fk.gate.MessageQueue;
import cn.hexing.fk.gate.RtuServerChannelMannager;
import cn.hexing.fk.gate.client.DlmsTerminalClient;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.MessageType;
import cn.hexing.fk.sockclient.JSocket;
import cn.hexing.fk.sockclient.async.event.ClientConnectedEvent;

import com.hx.dlms.message.DlmsMessage;

/**
 */
public class GateRTUServerEventHandler implements IEventHandler {
	private MessageQueue queue;

	public void setQueue(MessageQueue queue) {
		this.queue = queue;
	}

	/**
	 * ���ظ÷�����
	 */
	public void handleEvent(IEvent e) {
		/** �����ն˷����յ����ģ����뾡�췢�͸�ǰ�û��������첽ԭ�򣬲���ֱ�ӵ���
		 *  ����ǰ�û�����Accept��client���͡���Ҫ�ŵ�ǰ�û����ж��С�
		 *  	1����ǰ�û����ӵ����أ�֪ͨ���ж��з������б��ģ�
		 *  	2����ǰ�û���Ӧclient�ɹ��������б��ģ�֪ͨ���ж��м������ͣ�
		 */
		if( e.getType() == EventType.MSG_RECV ){
			
			IMessage msg = e.getMessage();
			if ( msg.getMessageType() == MessageType.MSG_DLMS ){
				DlmsMessage dmsg = (DlmsMessage)msg;
				String meterId = RtuServerChannelMannager.getMeterId(dmsg.getPeerAddr());
				dmsg.setLogicalAddress(meterId);
				queue.offerUpMessageInQueue(dmsg);
			}
			
		}else if(e.getType() == EventType.CLIENT_CONNECTED){
			ClientConnectedEvent cce = (ClientConnectedEvent) e;
			DlmsTerminalClient client=(DlmsTerminalClient)((JSocket)cce.getClient()).getListener();
			DlmsMessage msg = client.realMessage;
			client.sendMessage(msg);
			RtuServerChannelMannager.setClientChannel(msg.getLogicalAddress(), client);
			RtuServerChannelMannager.setDlmsPeerAddr(msg.getPeerAddr(), msg.getLogicalAddress());
		}else if(e.getType() == EventType.MSG_SENT){
			DlmsTerminalClient client=(DlmsTerminalClient)((JSocket)e.getSource()).getListener();
			client.realMessage = null;
		}else if(e.getType() == EventType.MSG_SEND_FAIL){
			DlmsTerminalClient client=(DlmsTerminalClient)((JSocket)e.getSource()).getListener();
			client.realMessage = null;
		}
			
	}

}
