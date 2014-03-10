/**
 * 消息对象发送失败时间处理 适配器
 */
package cn.hexing.fk.sockserver.event.adapt;

import org.apache.log4j.Logger;

import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.common.spi.IEventHandler;
import cn.hexing.fk.sockserver.event.MessageSendFailEvent;

/**
 *
 */
public class MessageSendFailEventAdapt implements IEventHandler {
	private static final Logger log = Logger.getLogger(MessageSendFailEventAdapt.class);
	private MessageSendFailEvent event;

	public void handleEvent(IEvent event) {
		this.event = (MessageSendFailEvent)event;
		process();
	}
	
	protected void process(){
		if( log.isInfoEnabled() )
			log.info("event send failed。client ip="+event.getClient()+";message="+event.getMessage().toString() );
	}
}
