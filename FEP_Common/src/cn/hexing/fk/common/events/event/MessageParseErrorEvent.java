/**
 * Message�����ڶ�ȡ���������ݣ�������Ϣ���ʱ�����¼���
 * ���ڴ�ӡ��Щ��Ϣ���ݲ��ܽ�����sourceΪclient����toString�����ṩIP��ַ��Ϣ��
 */
package cn.hexing.fk.common.events.event;

import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.message.IMessage;

/**
 */
public class MessageParseErrorEvent implements IEvent {
	private final EventType type = EventType.MSG_PARSE_ERROR;
	private IMessage message;
	private Object source;		//��Ϣ�����Ӧ����ԴClientChannel
	private String info;

	public MessageParseErrorEvent(IMessage msg){
		message = msg;
		source = msg.getSource();
	}

	public MessageParseErrorEvent(IMessage msg,String info){
		message = msg;
		source = msg.getSource();
		this.info = info;
	}
	
	public IMessage getMessage() {
		return message;
	}

	public Object getSource() {
		return source;
	}

	public EventType getType() {
		return type;
	}

	public void setSource(Object src) {
		source = src;
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer(512);
		sb.append("MessageParseErrorEvent,source=").append(source);
		sb.append(",packet=").append(message.getRawPacketString());
		if( null != info )
			sb.append(",info=").append(info);
		return sb.toString();
	}
}
