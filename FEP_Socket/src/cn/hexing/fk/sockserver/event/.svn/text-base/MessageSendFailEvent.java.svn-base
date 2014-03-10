/**
 * 当客户端AsyncSocketClient发送队列满了，或者client发送失败时，产生的事件。
 */
package cn.hexing.fk.sockserver.event;

import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.common.spi.socket.IChannel;
import cn.hexing.fk.message.IMessage;

/**
 *
 */
public class MessageSendFailEvent implements IEvent {
	private final EventType type = EventType.MSG_SEND_FAIL;
	private IChannel client;
	private IMessage message;
	
	public MessageSendFailEvent(IMessage msg,IChannel c){
		message = msg;
		client = c;
	}
	
	public EventType getType() {
		return type;
	}

	public void setType(EventType type) {
	}

	public IChannel getClient() {
		return client;
	}

	public IMessage getMessage() {
		return message;
	}

	public Object getSource() {
		return client.getServer();
	}

	public void setSource(Object src) {
	}

	public String toString(){
		StringBuffer sb = new StringBuffer(1024);
		sb.append("message send failed event. client=");
		sb.append(client).append(",server=").append(client.getServer().getPort());
		sb.append(",messge=").append(message);
		return sb.toString();
	}
}
