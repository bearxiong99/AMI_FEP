/**
 * 客户端socket长时间没有IO通讯的事件。
 */
package cn.hexing.fk.sockserver.event;

import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.common.spi.socket.IChannel;
import cn.hexing.fk.common.spi.socket.ISocketServer;
import cn.hexing.fk.message.IMessage;

/**
 *
 */
public class ClientTimeoutEvent implements IEvent {
	private final EventType type = EventType.CLIENTTIMEOUT;
	private ISocketServer server;
	private IChannel client;

	public ClientTimeoutEvent(IChannel c){
		client = c;
		server = c.getServer();
	}
	
	public EventType getType() {
		return type;
	}

	public void setType(EventType type) {
	}

	public final ISocketServer getServer() {
		return server;
	}

	public final IChannel getClient() {
		return client;
	}

	public Object getSource() {
		return server;
	}

	public void setSource(Object src) {
	}
	
	public IMessage getMessage(){
		return null;
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer(128);
		sb.append("client timeout event. client=").append(client);
		sb.append(",server=").append(server.getPort());
		return sb.toString();
	}
}
