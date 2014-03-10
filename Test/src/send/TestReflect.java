package send;

import java.lang.reflect.Constructor;
import java.nio.channels.SocketChannel;

import cn.hexing.fk.common.spi.socket.ISocketServer;
import cn.hexing.fk.sockclient.csd.CSDSyncTcpSocketClient;

public class TestReflect {
	public static void main(String[] args) throws SecurityException, NoSuchMethodException, ClassNotFoundException {
		Class<CSDSyncTcpSocketClient> clazz = (Class<CSDSyncTcpSocketClient>) Class.forName("cn.hexing.fk.sockclient.csd.CSDSyncTcpSocketClient");
		Constructor<CSDSyncTcpSocketClient> constructor = clazz.getConstructor(SocketChannel.class,ISocketServer.class);
//		 constructor.newInstance(channel,SyncTcpServer.this);
	}
}
