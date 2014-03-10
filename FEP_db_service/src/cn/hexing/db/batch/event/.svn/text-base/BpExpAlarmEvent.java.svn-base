package cn.hexing.db.batch.event;

import cn.hexing.db.batch.AsyncService;
import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.message.IMessage;

public class BpExpAlarmEvent implements IEvent {
	private static final EventType type = EventType.BP_EXP_ALARM;
	private IMessage message;
	private AsyncService service;
	
	public BpExpAlarmEvent(AsyncService service,IMessage msg){
		this.service = service;
		this.message = msg;
	}
	
	public IMessage getMessage() {
		return message;
	}

	public AsyncService getService(){
		return service;
	}
	
	public AsyncService getSource() {
		return service;
	}

	public EventType getType() {
		return type;
	}

	public void setSource(Object src) {
	}

}
