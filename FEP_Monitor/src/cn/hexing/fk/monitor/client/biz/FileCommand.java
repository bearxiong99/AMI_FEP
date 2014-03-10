package cn.hexing.fk.monitor.client.biz;

import java.nio.ByteBuffer;

import cn.hexing.fk.monitor.MonitorCommand;
import cn.hexing.fk.monitor.message.MonitorMessage;
import cn.hexing.fk.sockclient.JSocket;

public class FileCommand {
	
	public void listLog(JSocket client){
		fileList("log",client);
	}
	
	public void listConfig(JSocket client){
		fileList("config",client);
	}
	
	public void fileList(String type,JSocket client){
		short cmd;
		if( type.equalsIgnoreCase("config"))
			cmd = MonitorCommand.CMD_CONFIG_LIST;
		else
			cmd = MonitorCommand.CMD_LOG_LIST;
		MonitorMessage msg = new MonitorMessage();
		msg.setCommand(cmd);
		ByteBuffer body = ByteBuffer.allocate(0);
		msg.setBody(body);
		client.sendMessage(msg);
	}

	public void getFile(JSocket client,String path ){
		MonitorMessage msg = new MonitorMessage();
		msg.setCommand(MonitorCommand.CMD_GET_FILE);
		byte [] btPath = path.getBytes();
		ByteBuffer body = ByteBuffer.allocate(btPath.length+1+8);
		body.put(btPath).put((byte)0);
		body.putLong(0);
		body.flip();
		msg.setBody(body);
		client.sendMessage(msg);
	}

	public void putFile(JSocket client,String path ){
		MonitorMessage msg = new MonitorMessage();
		msg.setCommand(MonitorCommand.CMD_PUT_FILE);
		byte [] btPath = path.getBytes();
		ByteBuffer body = ByteBuffer.allocate(btPath.length+1+8);
		body.put(btPath).put((byte)0);
		body.putLong(0);
		body.flip();
		body = ClientHandleFile.getHandleFile().putFile(body);
		if( null == body )
			return;
		msg.setBody(body);
		client.sendMessage(msg);
	}
}
