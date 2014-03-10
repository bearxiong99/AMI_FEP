/**
 * 客户端关闭事件处理器适配器
 */
package cn.hexing.fk.sockserver.event.adapt;

import org.apache.log4j.Logger;

import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.common.spi.IEventHandler;
import cn.hexing.fk.sockserver.event.ClientCloseEvent;

/**
 *
 */
public class ClientCloseEventAdapt implements IEventHandler {
	private static final Logger log = Logger.getLogger(ClientCloseEventAdapt.class);
	protected ClientCloseEvent event;

	public void handleEvent(IEvent ev) {
		event = (ClientCloseEvent)ev;
		process(event);
	}

	protected void process(ClientCloseEvent event){
		if( log.isInfoEnabled() )
			log.info("server["+event.getServer().getPort()+"] close client["+event.getClient().getPeerIp()+"]");
	}

}
