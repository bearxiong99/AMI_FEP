package cn.hexing.dp.bpserver;

import org.apache.log4j.Logger;

import cn.hexing.fk.clientmod.ClusterClientModule;
import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.events.BasicEventHook;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.sockserver.event.ReceiveMessageEvent;
import cn.hexing.fk.tracelog.TraceLog;

public class TaskPollingEventHandler extends BasicEventHook {
	private static final Logger log = Logger.getLogger(TaskPollingEventHandler.class);
	private static final TraceLog trace = TraceLog.getTracer(TaskPollingEventHandler.class);

	private ClusterClientModule com=null;
	//内部属性	
	
	public void setCom(ClusterClientModule com) {
		this.com = com;
	}

	@Override
	public void handleEvent(IEvent event) {
		if( event.getType() == EventType.MSG_RECV ){
			//当收到业务处理器下行报文
			onRecvMessage( (ReceiveMessageEvent)event);
		}		
		else
			super.handleEvent(event);
	}
	
	//receive BP up coming message.
	private void onRecvMessage(ReceiveMessageEvent e){
		IMessage msg0 = e.getMessage();
		
		
	}
}
