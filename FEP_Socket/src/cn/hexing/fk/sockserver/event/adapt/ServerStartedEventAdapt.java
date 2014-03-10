/**
 * Socket服务启动成功事件 适配器
 */
package cn.hexing.fk.sockserver.event.adapt;

import org.apache.log4j.Logger;

import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.common.spi.IEventHandler;
import cn.hexing.fk.sockserver.event.ServerStartedEvent;

/**
 *
 */
public class ServerStartedEventAdapt implements IEventHandler {
	private static final Logger log = Logger.getLogger(ServerStartedEventAdapt.class);
	private ServerStartedEvent event;

	public void handleEvent(IEvent event) {
		this.event = (ServerStartedEvent)event;
		process();
	}
	
	protected void process(){
		if( log.isInfoEnabled() )
			log.info(event);
	}
}
