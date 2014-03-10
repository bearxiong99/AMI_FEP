package cn.hexing.fk.monitor.eventHandler;

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

import cn.hexing.fk.FasSystem;
import cn.hexing.fk.monitor.MonitorCommand;
import cn.hexing.fk.monitor.biz.HandleFile;
import cn.hexing.fk.monitor.biz.HandleListFile;
import cn.hexing.fk.monitor.biz.HandleRtuTrace;
import cn.hexing.fk.monitor.message.MonitorMessage;
import cn.hexing.fk.sockserver.event.ReceiveMessageEvent;
import cn.hexing.fk.sockserver.event.adapt.ReceiveMessageEventAdapt;

/**
 * 当收到管理监控消息时，BasicEventHook对象通过spring配置自动调用OnReceiveMessage处理事件。
 * 直接集成了内部ReceiveMessageEvent对象。
 *
 */
public class OnReceiveMonitorMessage extends ReceiveMessageEventAdapt {
	private static final Logger log = Logger.getLogger(OnReceiveMonitorMessage.class);
	private static final byte[] Reply_Success = "成功".getBytes();
	private static final byte[] Reply_Failed = "失败".getBytes();
	private static final String configPath = ".";
	private static final String logPath = "log";

	@Override
	protected void process(ReceiveMessageEvent event) {
		MonitorMessage msg = (MonitorMessage)event.getMessage();
		msg.resetMessageState();		//以便消息可以再次读或者写。否则消息状态为读完毕。
		String name;
		byte[] result;
		switch(msg.getCommand()){
		case MonitorCommand.CMD_INVALID:
			log.error("OnReceiveMonitorMessage: CMD_INVALID");
			break;
		case MonitorCommand.CMD_CONFIG_LIST:		//配置文件列表
			result = HandleListFile.getListFile().list(configPath,"*.xml,*.properties").getBytes();
			_reply(event,result);
			break;
		case MonitorCommand.CMD_LOG_LIST:
			result = HandleListFile.getListFile().list(logPath).getBytes();
			_reply(event,result);
			break;
		case MonitorCommand.CMD_GET_FILE:
			_reply(event,HandleFile.getHandleFile().getFile(msg.getBody()));
			break;
		case MonitorCommand.CMD_PUT_FILE:
			_reply(event,HandleFile.getHandleFile().putFile(msg.getBody()));
			break;
		case MonitorCommand.CMD_SYS_PROFILE:
			onProfileEvent(event,"system");
			break;
		case MonitorCommand.CMD_MODULE_PROFILE:
			onProfileEvent(event,"module");
			break;
		case MonitorCommand.CMD_EVENT_HOOK_PROFILE:
			onProfileEvent(event,"eventhook");
			break;
		case MonitorCommand.CMD_GATHER_PROFILE:
			onProfileEvent(event,"gather");
			break;
		case MonitorCommand.CMD_MODULE_START:
			name = new String(msg.getBody().array());
			onBooleanReply(event,FasSystem.getFasSystem().startModule(name));
			break;
		case MonitorCommand.CMD_MODULE_STOP:
			name = new String(msg.getBody().array());
			onBooleanReply(event,FasSystem.getFasSystem().stopModule(name));
			break;
		case MonitorCommand.CMD_SYS_START:
			break;
		case MonitorCommand.CMD_SYS_STOP:
			FasSystem.getFasSystem().stopSystem();
			break;
		case MonitorCommand.CMD_TRACE_RTU:
			onBooleanReply(event,HandleRtuTrace.getHandleRtuTrace().startTraceRtu(event, msg.getBody()));
			break;
		case MonitorCommand.CMD_TRACE_ABORT:
			onBooleanReply(event,HandleRtuTrace.getHandleRtuTrace().stopTrace(event));
			break;
		}
	}
	
	private void onProfileEvent(ReceiveMessageEvent event,String eType){
		FasSystem fas = FasSystem.getFasSystem();
		String profile = fas.getProfile(eType);
		if( null == profile )
			return;
		byte[] ret = profile.getBytes();
//		byte[] ret = FasSystem.getFasSystem().getProfile(eType).getBytes();
		_reply(event,ret);
	}

	private void onBooleanReply(ReceiveMessageEvent event,boolean ret){
		byte[] result;
		if ( ret )
			result = Reply_Success;
		else
			result = Reply_Failed;
		_reply(event,result);
	}
	
	private void _reply(ReceiveMessageEvent event,byte[] result){
		MonitorMessage msg = (MonitorMessage)event.getMessage();
		ByteBuffer body = ByteBuffer.wrap(result);
		msg.setBody(body);
		event.getClient().send(msg);
	}
	
	private void _reply(ReceiveMessageEvent event,ByteBuffer result){
		MonitorMessage msg = (MonitorMessage)event.getMessage();
		msg.setBody(result);
		event.getClient().send(msg);
	}
}
