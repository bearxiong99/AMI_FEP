/**
 * 发送简单（simpleMessage）消息事件适配器
 */
package cn.hexing.fk.sockserver.event.adapt;

import org.apache.log4j.Logger;

import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.common.spi.IEventHandler;
import cn.hexing.fk.sockserver.event.SendMessageEvent;

/**
 *
 */
public class SentSimpleMessageEventAdapt implements IEventHandler {
	private static final Logger log = Logger.getLogger(SentSimpleMessageEventAdapt.class);
	private SendMessageEvent event;

	public void handleEvent(IEvent event) {
		this.event = (SendMessageEvent)event;
		process();
	}

	protected void process(){
		if( log.isInfoEnabled() )
			log.info(event);
	}
}
