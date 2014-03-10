package serial.client;

import cn.hexing.fk.clientmod.ClientModule;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.sockclient.JSocket;


/**
 * 
 * @author gaoll
 *
 * @time 2013-4-10 下午03:28:28
 *
 * @info 串口tcpClient,用于连接网关,并且发送心跳
 */
public class SerialTcpClient extends ClientModule{

	private SerialCommClient commClient;
	
	@Override
	public void onReceive(JSocket client, IMessage msg) {
		commClient.offerDownMessage(msg);
	}

	public void setCommClient(SerialCommClient commClient) {
		this.commClient = commClient;
	}
	
}