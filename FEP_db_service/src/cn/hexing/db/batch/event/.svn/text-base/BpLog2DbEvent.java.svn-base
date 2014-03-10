package cn.hexing.db.batch.event;

import cn.hexing.db.batch.AsyncService;
import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.message.IMessage;

public class BpLog2DbEvent implements IEvent {
	private static final EventType type = EventType.BP_LOG_DB;
	private IMessage message;
	private AsyncService service;
	
	public BpLog2DbEvent(AsyncService service,IMessage msg){
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
