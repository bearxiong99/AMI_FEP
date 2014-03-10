/**
 * 通用的发送消息事件定义
 */
package cn.hexing.fk.sockserver.event;

import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.common.spi.socket.IChannel;
import cn.hexing.fk.common.spi.socket.ISocketServer;
import cn.hexing.fk.message.IMessage;

/**
 */
public class SendMessageEvent implements IEvent {
	private final EventType type = EventType.MSG_SENT;
	private IMessage message;
	private IChannel client;
	private ISocketServer server;
	
	public SendMessageEvent(IMessage m,IChannel c){
		message = m;
		client = c;
		server = c.getServer();
	}
	
	public Object getSource() {
		return server;
	}

	public EventType getType() {
		return type;
	}

	public void setSource(Object src) {
	}

	public final IMessage getMessage() {
		return message;
	}

	public final IChannel getClient() {
		return client;
	}

	public final ISocketServer getServer() {
		return server;
	}

	public String toString(){
		StringBuffer sb = new StringBuffer(1024);
		sb.append("send event. server=").append(server.getPort()).append(",client=");
		sb.append(client).append(",发送:");
		sb.append(message);
		return sb.toString();
	}
}
