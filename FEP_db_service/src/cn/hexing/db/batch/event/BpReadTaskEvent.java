/**
 * 任务数据上行业务处理事件
 */
package cn.hexing.db.batch.event;

import cn.hexing.db.batch.AsyncService;
import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.message.IMessage;

/**
 */
public class BpReadTaskEvent implements IEvent {
	private static final EventType type = EventType.BP_READ_TASK;
	private IMessage message;
	private AsyncService service;
	
	public BpReadTaskEvent(AsyncService service,IMessage msg){
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
