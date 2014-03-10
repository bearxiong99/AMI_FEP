package cn.hexing.fk.sockclient.async.event.adapt;

import org.apache.log4j.Logger;

import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.common.spi.IEventHandler;
import cn.hexing.fk.sockclient.async.JAsyncSocket;
import cn.hexing.fk.sockclient.async.event.ClientConnectedEvent;
import cn.hexing.fk.sockclient.async.simulator.SimulatorManager;
import cn.hexing.fk.sockclient.async.simulator.ZjSimulator;

public class OnClientConnected implements IEventHandler {
	private static final Logger log = Logger.getLogger(OnClientConnected.class);

	public void handleEvent(IEvent evt) {
		ClientConnectedEvent event = (ClientConnectedEvent)evt;
		JAsyncSocket client = (JAsyncSocket)event.getClient();
		client.setLocalIp(client.getChannel().socket().getLocalAddress().getHostAddress());
		client.setLocalPort(client.getChannel().socket().getLocalPort());
		if( null == client.attachment() ){
			ZjSimulator simulator = new ZjSimulator();
			client.attach(simulator);
		}
		SimulatorManager.onChannelConnected(client);
		log.info("async socket pool: client="+client.getPeerAddr()+" connected.");
	}

}
