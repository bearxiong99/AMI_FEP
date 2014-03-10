package cn.hexing.fk.sockclient.async.event.adapt;

import org.apache.log4j.Logger;

import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.common.spi.IEventHandler;
import cn.hexing.fk.sockclient.async.JAsyncSocket;
import cn.hexing.fk.sockclient.async.simulator.SimulatorManager;
import cn.hexing.fk.sockserver.event.ClientCloseEvent;

public class OnClientClosed implements IEventHandler {
	private static final Logger log = Logger.getLogger(OnClientClosed.class);

	public void handleEvent(IEvent evt) {
		ClientCloseEvent event = (ClientCloseEvent)evt;
		JAsyncSocket client = (JAsyncSocket)event.getClient();
//		AsyncSocketPool pool = (AsyncSocketPool)event.getServer();
		SimulatorManager.onChannelClosed(client);
		log.info("async socket pool: client="+client.getPeerAddr()+" closed.");
	}

}
