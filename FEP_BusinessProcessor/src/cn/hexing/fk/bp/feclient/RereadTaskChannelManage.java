package cn.hexing.fk.bp.feclient;

import cn.hexing.fk.clientmod.ClientModule;
import cn.hexing.fk.message.IMessage;

public class RereadTaskChannelManage {
	private static RereadTaskChannelManage instance;
	//	可配置属性，通过SPRING配置。
	ClientModule client;
	
	private RereadTaskChannelManage(){
		instance = this;
	}
	public static RereadTaskChannelManage getInstance(){
		if( null == instance )
			instance = new RereadTaskChannelManage();
		return instance;
	}
	public ClientModule getClient() {
		return this.client;
	}

	public void setClient(ClientModule client) {
		this.client = client;
	}
	public boolean sendMessage(IMessage msg){
		return this.getClient().getSocket().send(msg);
	}
	public boolean getClientAlive(){
		return this.client.isActive();
	}
}
