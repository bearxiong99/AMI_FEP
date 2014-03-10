package cn.hexing.fk.monitor.client.biz;

import java.nio.ByteBuffer;

import cn.hexing.fk.monitor.MonitorCommand;
import cn.hexing.fk.monitor.message.MonitorMessage;
import cn.hexing.fk.sockclient.JSocket;

public class ProfileCommand {

	public void getSystemProfile(JSocket client){
		getProfile(client,"system");
	}
	
	public void getModuleProfile(JSocket client){
		getProfile(client,"module");
	}
	
	public void getEventHookProfile(JSocket client){
		getProfile(client,"eventhook");
	}
	
	public void gatherProfile(JSocket client){
		getProfile(client,"gather");
	}
	
	public void getProfile(JSocket client, String type ){
		short cmd;
		if( "module".equalsIgnoreCase(type) )
			cmd = MonitorCommand.CMD_MODULE_PROFILE;
		else if( "eventhook".equalsIgnoreCase(type) )
			cmd = MonitorCommand.CMD_EVENT_HOOK_PROFILE;
		else if( "gather".equalsIgnoreCase(type))
			cmd = MonitorCommand.CMD_GATHER_PROFILE;
		else
			cmd = MonitorCommand.CMD_SYS_PROFILE;
		MonitorMessage msg = new MonitorMessage();
		msg.setCommand(cmd);
		ByteBuffer body = ByteBuffer.allocate(0);
		msg.setBody(body);
		client.sendMessage(msg);
	}
}
