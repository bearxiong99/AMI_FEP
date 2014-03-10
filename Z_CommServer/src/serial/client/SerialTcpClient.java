package serial.client;

import cn.hexing.fk.clientmod.ClientModule;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.sockclient.JSocket;


/**
 * 
 * @author gaoll
 *
 * @time 2013-4-10 ����03:28:28
 *
 * @info ����tcpClient,������������,���ҷ�������
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