package cn.hexing.fk.sockclient.async.event.adapt;

import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.common.spi.IEventHandler;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.sockclient.async.JAsyncSocket;
import cn.hexing.fk.sockclient.async.simulator.SimulatorManager;
import cn.hexing.fk.sockserver.event.ReceiveMessageEvent;

public class OnClientRecvMsg implements IEventHandler {

	public void handleEvent(IEvent evt) {
		ReceiveMessageEvent event = (ReceiveMessageEvent)evt;
		JAsyncSocket client = (JAsyncSocket)event.getClient();
		IMessage msg = event.getMessage();
		SimulatorManager.onChannelReceive(client,msg);
	}

}
