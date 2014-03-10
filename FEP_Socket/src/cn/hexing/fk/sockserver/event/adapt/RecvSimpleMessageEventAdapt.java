/**
 * �յ�����Ϣ��SimpleMessage���¼�������
 */
package cn.hexing.fk.sockserver.event.adapt;

import org.apache.log4j.Logger;

import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.common.spi.IEventHandler;
import cn.hexing.fk.sockserver.event.ReceiveMessageEvent;

/**
 *
 */
public class RecvSimpleMessageEventAdapt implements IEventHandler {
	private static final Logger log = Logger.getLogger(RecvSimpleMessageEventAdapt.class);
	private ReceiveMessageEvent event;

	public void handleEvent(IEvent event) {
		this.event = (ReceiveMessageEvent)event;
		process();
	}

	protected void process(){
		if( log.isInfoEnabled() )
			log.info(event);
	}
}
