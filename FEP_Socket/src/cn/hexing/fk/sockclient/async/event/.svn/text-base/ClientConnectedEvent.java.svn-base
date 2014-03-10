package cn.hexing.fk.sockclient.async.event;

import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.common.spi.socket.IChannel;
import cn.hexing.fk.common.spi.socket.ISocketServer;
import cn.hexing.fk.message.IMessage;

public class ClientConnectedEvent implements IEvent {
	private final EventType type = EventType.CLIENT_CONNECTED;
	private ISocketServer server;
	private IChannel client;

	public ClientConnectedEvent(ISocketServer s,IChannel c){
		server = s;
		client = c;
	}
	
	public IMessage getMessage() {
		return null;
	}

	public Object getSource() {
		return server;
	}

	public EventType getType() {
		return type;
	}

	public void setSource(Object src) {
	}

	public ISocketServer getServer() {
		return server;
	}

	public IChannel getClient() {
		return client;
	}

	public String toString(){
		return "ClientConnectedEvent,client="+client.getPeerAddr();
	}
}
