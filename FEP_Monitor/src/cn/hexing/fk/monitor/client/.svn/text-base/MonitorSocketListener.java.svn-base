package cn.hexing.fk.monitor.client;

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.monitor.MonitorCommand;
import cn.hexing.fk.monitor.client.biz.ClientHandleFile;
import cn.hexing.fk.monitor.message.MonitorMessage;
import cn.hexing.fk.sockclient.JSocket;
import cn.hexing.fk.sockclient.JSocketListener;

public class MonitorSocketListener implements JSocketListener {
	private static final Logger log = Logger.getLogger(MonitorSocketListener.class);

	public IMonitorReplyListener replyListener = null;

	public void onClose(JSocket client) {
		if( null != replyListener )
			replyListener.onClose();
		log.info("监控服务连接断开:"+client);
	}

	public void onConnected(JSocket client) {
		if( null != replyListener )
			replyListener.onConnect();
		log.info("监控服务连接成功:"+client);
	}

	public void onReceive(JSocket client,IMessage message) {
		MonitorMessage msg = (MonitorMessage)message;
		msg.resetMessageState();		//以便消息可以再次读或者写。否则消息状态为读完毕。
		ByteBuffer body = null;
		switch(msg.getCommand()){
		case MonitorCommand.CMD_CONFIG_LIST:		//配置文件列表
			body = msg.getBody();
			{
				String result = new String(body.array());
				if( null != replyListener )
					replyListener.onListConfig(result);
			}
			break;
		case MonitorCommand.CMD_LOG_LIST:		//配置文件列表
			body = msg.getBody();
			{
				String result = new String(body.array());
				if( null != replyListener )
					replyListener.onListLog(result);
			}
			break;
		case MonitorCommand.CMD_GET_FILE:	//下载文件
			body = ClientHandleFile.getHandleFile().getFile(msg.getBody());
			if( null == body ){
				if( null != replyListener )
					replyListener.onGetFile();
				return;
			}
			msg.setBody(body);
			client.sendMessage(msg);
			break;
		case MonitorCommand.CMD_PUT_FILE:
			body = ClientHandleFile.getHandleFile().putFile(msg.getBody());
			if( null == body ){
				if( null != replyListener )
					replyListener.onPutFile();
				return;
			}
			msg.setBody(body);
			client.sendMessage(msg);
			break;
		case MonitorCommand.CMD_SYS_PROFILE:
			body = msg.getBody();
			{
				String result = new String(body.array());
				if( null != replyListener )
					replyListener.onSystemProfile(result);
			}
			break;
		case MonitorCommand.CMD_MODULE_PROFILE:
			body = msg.getBody();
			{
				String result = new String(body.array());
				if( null != replyListener )
					replyListener.onModuleProfile(result);
			}
			break;
		case MonitorCommand.CMD_EVENT_HOOK_PROFILE:
			body = msg.getBody();
			{
				String result = new String(body.array());
				if( null != replyListener )
					replyListener.onEventHookProfile(result);
			}
			break;
		case MonitorCommand.CMD_GATHER_PROFILE:
			body = msg.getBody();
			{
				String profile = new String(body.array());
				if( null != replyListener )
					replyListener.onMultiSysProfile(profile);
			}
			break;
		case MonitorCommand.CMD_TRACE_IND:
			body = msg.getBody();
			{
				String result = new String(body.array());
				if( null != replyListener )
					replyListener.onRtuMessageInd(result);
			}
			break;
		}
	}

	public void onSend(JSocket client,IMessage msg) {

	}

}
