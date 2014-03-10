/**
 * 服务器接受客户端连接事件
 */
package cn.hexing.fk.sockserver.event;

import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.common.spi.socket.IServerSideChannel;
import cn.hexing.fk.common.spi.socket.ISocketServer;
import cn.hexing.fk.message.IMessage;

/**
 *
 */
public class AcceptEvent implements IEvent {
	private final EventType type = EventType.ACCEPTCLIENT;
	private ISocketServer server;
	private IServerSideChannel client;

	public AcceptEvent(IServerSideChannel c){
		server = c.getServer();
		client = c;
	}
	
	public EventType getType() {
		return type;
	}

	public void setType(EventType type) {
	}

	public final ISocketServer getServer() {
		return server;
	}

	public final IServerSideChannel getClient() {
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
		return "AcceptEvent,server="+server.getPort()+",client="+client.getPeerAddr();
	}
}
