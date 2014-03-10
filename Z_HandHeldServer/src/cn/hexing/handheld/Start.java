package cn.hexing.handheld;


import cn.hexing.fk.common.EventType;
import cn.hexing.fk.message.msgbytes.MessageBytesCreator;
import cn.hexing.fk.sockserver.TcpSocketServer;
import cn.hexing.fk.sockserver.event.adapt.AcceptEventAdapt;
import cn.hexing.fk.sockserver.event.adapt.ClientCloseEventAdapt;
import cn.hexing.fk.sockserver.event.adapt.ReceiveMessageEventAdapt;
import cn.hexing.fk.sockserver.event.adapt.SendMessageEventAdapt;
import cn.hexing.fk.sockserver.io.SimpleIoHandler;

public class Start {
	public static void main(String[] args) {
		TcpSocketServer server = new TcpSocketServer();
		server.setPort(3071);
		server.setMessageCreator(new MessageBytesCreator());
		server.setIoThreadSize(2);
		server.setClientClass("cn.hexing.fk.sockserver.AsyncHandheldSocketClient");
		server.setWriteFirstCount(100);
		server.setMaxContinueRead(10);
		server.setTimeout(36000);
		server.setName("handheld.server");
		server.setIoHandler(new SimpleIoHandler());
		server.setBufLength(20480);
		HandHeldEventHandler handler = new HandHeldEventHandler();
		handler.setSource(server);
		handler.addHandler(EventType.ACCEPTCLIENT, new AcceptEventAdapt());
		handler.addHandler(EventType.MSG_RECV, new ReceiveMessageEventAdapt());
		handler.addHandler(EventType.MSG_SENT, new SendMessageEventAdapt());
		handler.addHandler(EventType.CLIENTCLOSE, new ClientCloseEventAdapt());
		handler.start();
		server.start();
	}
}
