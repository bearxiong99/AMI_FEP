/**
 * 客户端长时间没有IO（超时）事件处理 适配器
 */
package cn.hexing.fk.sockserver.event.adapt;

import org.apache.log4j.Logger;

import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.common.spi.IEventHandler;
import cn.hexing.fk.sockserver.event.ClientTimeoutEvent;

/**
 *
 */
public class ClientTimeoutEventAdapt implements IEventHandler {
	private static final Logger log = Logger.getLogger(ClientTimeoutEventAdapt.class);
	private ClientTimeoutEvent event;

	public void handleEvent(IEvent event) {
		this.event = (ClientTimeoutEvent)event;
		process();
	}

	protected void process(){
		if( log.isInfoEnabled() )
			log.info("client["+event.getClient().getPeerIp()+"]长时间没有IO，被关闭。");
	}
}
