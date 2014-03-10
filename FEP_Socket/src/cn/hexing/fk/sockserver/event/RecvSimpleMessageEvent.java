/**
 * 收到简单类型的消息对象事件
 */
package cn.hexing.fk.sockserver.event;

import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.sockserver.AsyncSocketClient;
import cn.hexing.fk.sockserver.message.SimpleMessage;

/**
 *
 */
public class RecvSimpleMessageEvent implements IEvent {
	private EventType type = EventType.MSG_SIMPLE_RECV;
	private SimpleMessage message;
	private AsyncSocketClient client;
	
	public RecvSimpleMessageEvent(IMessage m){
		message = (SimpleMessage)m;
		client = (AsyncSocketClient)m.getSource();
	}
	
	public Object getSource() {
		return client.getServer();
	}

	public EventType getType() {
		return type;
	}

	public void setSource(Object src) {
	}

	public void setType(EventType type) {
	}

	public SimpleMessage getMessage() {
		return message;
	}

	public void setMessage(SimpleMessage message) {
		this.message = message;
	}

	public AsyncSocketClient getClient() {
		return client;
	}

	public void setClient(AsyncSocketClient client) {
		this.client = client;
	}

}
