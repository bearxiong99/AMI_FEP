/**
 * 功能概述：
 * 		侦听收到终端上行报文、下行报文发送成功事件。
 *    终端上行报文进入MessageQueue队列，以便发送给前置机；
 *    终端下行报文成功事件，简单打印日志。无进一步处理需求。
 * 技术实现：
 * SimpleEventHandler派生类。
 * override handleEvent方法，针对ReceiveMessageEvent和SendMessageEvent特别处理。
 * 注意事项：在spring配置文件中，source对象必须是网关终端接口的SocketServer对象。
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
	 * 重载该方法。
	 */
	public void handleEvent(IEvent e) {
		/** 网关终端服务收到报文，必须尽快发送给前置机。由于异步原因，不能直接调用
		 *  网关前置机服务Accept的client发送。需要放到前置机上行队列。
		 *  	1）当前置机连接到网关，通知上行队列发送上行报文；
		 *  	2）当前置机对应client成功发送上行报文，通知上行队列继续发送；
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
