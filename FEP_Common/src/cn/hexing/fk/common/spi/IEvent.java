package cn.hexing.fk.common.spi;

import cn.hexing.fk.common.EventType;
import cn.hexing.fk.message.IMessage;

public interface IEvent {
	EventType getType();
	
	/**
	 * ������Ϣ������Դ������AsyncSocketClient����
	 * @return
	 */
	Object getSource();
	
	void setSource(Object src);
	IMessage getMessage();
}
