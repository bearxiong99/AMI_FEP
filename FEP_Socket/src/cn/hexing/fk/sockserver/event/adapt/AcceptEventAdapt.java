/**
 * SocketServer接受客户端连接事件的适配器
 */
package cn.hexing.fk.sockserver.event.adapt;

import org.apache.log4j.Logger;

import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.common.spi.IEventHandler;
import cn.hexing.fk.sockserver.event.AcceptEvent;

/**
 *
 */
public class AcceptEventAdapt implements IEventHandler {
	private static final Logger log = Logger.getLogger(AcceptEventAdapt.class);
	private AcceptEvent event;

	public void handleEvent(IEvent event) {
		this.event = (AcceptEvent)event;
		process();
	}
	
	protected void process(){
		if( log.isInfoEnabled() )
			log.info("server["+event.getServer().getPort()+"] accept client["+event.getClient().getPeerIp()+"]");
	}
}
