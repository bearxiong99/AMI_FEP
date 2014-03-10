package cn.hexing.fk.gate.client;

import cn.hexing.fk.clientmod.ClientModule;
import cn.hexing.fk.gate.RtuServerChannelMannager;
import cn.hexing.fk.sockclient.JSocket;

/**
 * 
 * @author gaoll
 *
 * @time 2013-3-15 ����12:29:21
 *
 * @info �ն����Ӷ���ÿһ����Ϣ����Ҫ����������-->��DlmsTerminalClient
 */
public class TerminalClient extends ClientModule {
	
	private String logicAddress ;
	
	@Override
	public boolean start(){
		super.setAutoReconnect(false);
		super.start();
		return true;	
	}
	
	@Override
	public void onTimer(int id){}
	
	public TerminalClient(){
		this.setBufLength(10240);
		this.setTimeout(4);
		this.setRequestNum(500);
	}

	@Override
	public void onClose(JSocket client) {
		super.onClose(client);
		this.stop();
		//TODO:...����Ҫ����RtuServerChannelManager����
		RtuServerChannelMannager.removeClient(this);
	}

	public String getLogicAddress() {
		return logicAddress;
	}

	public void setLogicAddress(String logicAddress) {
		this.logicAddress = logicAddress;
	}

}
