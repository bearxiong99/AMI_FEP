package cn.hexing.db.batch.event.adapt;

import cn.hexing.db.batch.AsyncService;
import cn.hexing.db.batch.BaseBpEventHandler;
import cn.hexing.db.batch.event.BpReadTaskEvent;
import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.message.IMessage;

public class BaseReadTaskHandler extends BaseBpEventHandler {
	protected static final EventType type = EventType.BP_READ_TASK;
	
	@Override
	public void handleEvent(IEvent event) {
		assert(event.getType() == type );
		BpReadTaskEvent e = (BpReadTaskEvent)event;
		handleReadTask(e.getService(),e.getMessage() );
	}
	
	public void handleReadTask(AsyncService service,IMessage msg){
		
	}

	@Override
	public EventType type() {
		return type;
	}

}
