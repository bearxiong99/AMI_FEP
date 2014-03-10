package cn.hexing.fk.sockserver.event.adapt;

import org.apache.log4j.Logger;

import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.common.spi.IEventHandler;
import cn.hexing.fk.sockserver.event.SendMessageEvent;

public class SendMessageEventAdapt implements IEventHandler {
	private static final Logger log = Logger.getLogger(SendMessageEventAdapt.class);
	private SendMessageEvent event;

	public void handleEvent(IEvent event) {
		this.event = (SendMessageEvent)event;
		process();
	}

	protected void process(){
		if( log.isInfoEnabled() )
			log.debug(event);
	}
}
