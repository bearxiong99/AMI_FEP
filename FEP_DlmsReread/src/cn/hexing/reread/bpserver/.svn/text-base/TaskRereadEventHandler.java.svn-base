package cn.hexing.reread.bpserver;

import cn.hexing.fk.clientmod.ClusterClientModule;
import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.events.BasicEventHook;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.sockserver.event.ReceiveMessageEvent;

public class TaskRereadEventHandler extends BasicEventHook {
	private ClusterClientModule com=null;
	//内部属性	
	
	public void setCom(ClusterClientModule com) {
		this.com = com;
	}

	public ClusterClientModule getCom() {
		return com;
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
		//IMessage msg0 = e.getMessage();
		
		
	}
}
