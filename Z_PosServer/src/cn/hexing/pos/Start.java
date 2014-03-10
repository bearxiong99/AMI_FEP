package cn.hexing.pos;


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
		server.setPort(3084);
		server.setMessageCreator(new MessageBytesCreator());
		server.setIoThreadSize(2);
		server.setClientClass("cn.hexing.fk.sockserver.AsyncPosSocketClient");
		server.setWriteFirstCount(100);
		server.setMaxContinueRead(10);
		server.setTimeout(36000);
		server.setName("pos.server");
		server.setIoHandler(new SimpleIoHandler());
		server.setBufLength(20480);
		PosServerEventHandler handler = new PosServerEventHandler();
		handler.setSource(server);
		handler.addHandler(EventType.ACCEPTCLIENT, new AcceptEventAdapt());
		handler.addHandler(EventType.MSG_RECV, new ReceiveMessageEventAdapt());
		handler.addHandler(EventType.MSG_SENT, new SendMessageEventAdapt());
		handler.addHandler(EventType.CLIENTCLOSE, new ClientCloseEventAdapt());
		handler.start();
		server.start();
	}
}
