package cn.hexing.fk.sockserver.event.adapt;

import org.apache.log4j.Logger;

import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.common.spi.IEventHandler;
import cn.hexing.fk.sockserver.event.ReceiveMessageEvent;

public class ReceiveMessageEventAdapt implements IEventHandler {
	private static final Logger log = Logger.getLogger(ReceiveMessageEventAdapt.class);

	public void handleEvent(IEvent ev) {
		ReceiveMessageEvent event = (ReceiveMessageEvent)ev;
		try{
			process(event);
		}catch(Exception exp){
			log.error("接收消息事件处理异常："+exp.getLocalizedMessage(),exp);
		}
	}

	protected void process(ReceiveMessageEvent event){
		if( log.isInfoEnabled() )
			log.debug(event);
	}
}
