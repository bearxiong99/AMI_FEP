package cn.hexing.fk.sockclient.async.event.adapt;

import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.common.spi.IEventHandler;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.sockclient.async.JAsyncSocket;
import cn.hexing.fk.sockclient.async.simulator.SimulatorManager;
import cn.hexing.fk.sockserver.event.SendMessageEvent;

public class OnClientSendMsg implements IEventHandler {

	public void handleEvent(IEvent evt) {
		SendMessageEvent event = (SendMessageEvent)evt;
		JAsyncSocket client = (JAsyncSocket)event.getClient();
		IMessage msg = event.getMessage();
		SimulatorManager.onChannelSend(client,msg);

	}

}
