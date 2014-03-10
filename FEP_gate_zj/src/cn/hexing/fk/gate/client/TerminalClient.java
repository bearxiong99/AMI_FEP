package cn.hexing.fk.gate.client;

import cn.hexing.fk.clientmod.ClientModule;
import cn.hexing.fk.gate.RtuServerChannelMannager;
import cn.hexing.fk.sockclient.JSocket;

/**
 * 
 * @author gaoll
 *
 * @time 2013-3-15 下午12:29:21
 *
 * @info 终端连接对象，每一类消息对象都要从这里派生-->如DlmsTerminalClient
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
		//TODO:...这里要处理RtuServerChannelManager移走
		RtuServerChannelMannager.removeClient(this);
	}

	public String getLogicAddress() {
		return logicAddress;
	}

	public void setLogicAddress(String logicAddress) {
		this.logicAddress = logicAddress;
	}

}
