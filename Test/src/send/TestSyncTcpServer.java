package send;

import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.spi.socket.abstra.BaseSocketServer;
import cn.hexing.fk.message.msgbytes.MessageBytesCreator;
import cn.hexing.fk.sockserver.event.adapt.AcceptEventAdapt;
import cn.hexing.fk.sockserver.event.adapt.ClientCloseEventAdapt;
import cn.hexing.fk.sockserver.event.adapt.ReceiveMessageEventAdapt;
import cn.hexing.fk.sockserver.event.adapt.SendMessageEventAdapt;


public class TestSyncTcpServer {
	public static void main(String[] args) {
		BaseSocketServer socketServer = new SyncTcpServer();
		socketServer.setPort(8000);
		socketServer.setBufLength(10240);
		socketServer.setIoThreadSize(2);
		socketServer.setMessageCreator(new MessageBytesCreator());
		socketServer.setTimeout(180);
		socketServer.start();
		CSDServerEventHandler handler = new CSDServerEventHandler();
		handler.setSource(socketServer);
		handler.addHandler(EventType.ACCEPTCLIENT, new AcceptEventAdapt());
		handler.addHandler(EventType.MSG_RECV, new ReceiveMessageEventAdapt());
		handler.addHandler(EventType.MSG_SENT, new SendMessageEventAdapt());
		handler.addHandler(EventType.CLIENTCLOSE, new ClientCloseEventAdapt());
		handler.start();
	}
}
