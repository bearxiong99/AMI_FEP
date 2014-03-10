/**
 * 成功发送简单类型的消息对象事件
 */
package cn.hexing.fk.sockserver.event;

import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.sockserver.AsyncSocketClient;
import cn.hexing.fk.sockserver.message.SimpleMessage;

/**
 */
public class SentSimpleMessageEvent implements IEvent {
	private EventType type = EventType.MSG_SIMPLE_SENT;
	private SimpleMessage message;
	private AsyncSocketClient client;
	
	public SentSimpleMessageEvent(IMessage msg){
		message = (SimpleMessage)msg;
		client = (AsyncSocketClient)msg.getSource();
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
