import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import cn.hexing.fk.clientmod.ClusterClientModule;
import cn.hexing.fk.exception.MessageParseException;
import cn.hexing.fk.message.gate.MessageGate;
import cn.hexing.fk.utils.HexDump;

import com.hx.dlms.message.DlmsMessage;


public class TestSocketClient {
	public static void main(String[] args) throws MessageParseException, InterruptedException, UnknownHostException, IOException {
		
		Socket socket = new Socket();
		InetSocketAddress ar = new InetSocketAddress("127.0.0.1", 1111);
		
		socket.connect(ar, 1000);
		
		socket.getOutputStream().write(new byte[100000]);
		
		socket.getOutputStream().write(new byte[1000000]);

	}
}
