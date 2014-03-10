/**
 * ���첽ͨѶģʽ�£�client����ֱ�ӷ������ݡ���Ҫ���͵����ݣ�Ԥ�Ȼ��浽client�����У�
 * Ȼ���첽����Ƿ����д��������д������£��ſ���ִ��socketChannelд������
 */
package cn.hexing.fk.sockserver.event;

import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.common.spi.socket.IServerSideChannel;
import cn.hexing.fk.message.IMessage;

/**
 *
 */
public class ClientWriteReqEvent implements IEvent {

	private final EventType type = EventType.CLIENT_WRITE_REQ;
	private IServerSideChannel client;

	public ClientWriteReqEvent(IServerSideChannel c){
		client = c;
	}
	
	public EventType getType() {
		return type;
	}

	public void setType(EventType type) {
	}

	public final IServerSideChannel getClient() {
		return client;
	}

	public Object getSource() {
		return client.getServer();
	}

	public void setSource(Object src) {
	}
	
	public IMessage getMessage(){
		return null;
	}
}
