package cn.hexing.fk.sockclient.async.eventhandler;

import java.util.HashMap;

import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.events.BasicEventHook;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.common.spi.IEventHandler;
import cn.hexing.fk.sockclient.async.event.adapt.OnClientClosed;
import cn.hexing.fk.sockclient.async.event.adapt.OnClientConnected;
import cn.hexing.fk.sockclient.async.event.adapt.OnClientRecvMsg;
import cn.hexing.fk.sockclient.async.event.adapt.OnClientSendMsg;

public class AsyncSocketEventHandler extends BasicEventHook {
	private IEventHandler listener;
	public void init(){
		// set include
		if( null == include ){
			include = new HashMap<EventType,IEventHandler>();
			include.put(EventType.CLIENT_CONNECTED, new OnClientConnected());
			include.put(EventType.CLIENTCLOSE, new OnClientClosed());
			include.put(EventType.MSG_RECV, new OnClientRecvMsg());
			include.put(EventType.MSG_SENT, new OnClientSendMsg());
		}
		super.init();
	}
	
	@Override
	public void handleEvent(IEvent event) {
		super.handleEvent(event);
		if( null != listener )
			listener.handleEvent(event);
	}

	public IEventHandler getListener() {
		return listener;
	}

	public void setListener(IEventHandler listener) {
		this.listener = listener;
	}
	
}
