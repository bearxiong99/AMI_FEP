/**
 * socket服务停止事件适配器
 */
package cn.hexing.fk.sockserver.event.adapt;

import org.apache.log4j.Logger;

import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.common.spi.IEventHandler;
import cn.hexing.fk.sockserver.event.ServerStoppedEvent;

/**
 *
 */
public class ServerStopEventAdapt implements IEventHandler {
	private static final Logger log = Logger.getLogger(ServerStopEventAdapt.class);
	private ServerStoppedEvent event;

	public void handleEvent(IEvent event) {
		this.event = (ServerStoppedEvent)event;
		process();
	}
	
	protected void process(){
		if(log.isInfoEnabled())
			log.info(event);
	}
}
