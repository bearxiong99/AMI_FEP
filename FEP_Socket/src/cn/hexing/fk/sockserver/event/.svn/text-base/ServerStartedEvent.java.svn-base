/**
 * TCP服务器启动成功事件
 */
package cn.hexing.fk.sockserver.event;

import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.common.spi.socket.ISocketServer;
import cn.hexing.fk.message.IMessage;

/**
 *
 */
public class ServerStartedEvent implements IEvent {
	private final EventType type = EventType.SERVERSTARTED;
	private ISocketServer server = null;
	
	public ServerStartedEvent(ISocketServer s){
		server = s;
	}
	
	public EventType getType() {
		return type;
	}

	public void setType(EventType type) {
	}
	
	public final ISocketServer getServer(){
		return server;
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
		sb.append("server started event. server=").append(server);
		return sb.toString();
	}
}
