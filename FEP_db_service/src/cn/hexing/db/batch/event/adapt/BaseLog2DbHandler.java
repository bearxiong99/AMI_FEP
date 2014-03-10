package cn.hexing.db.batch.event.adapt;

import cn.hexing.db.batch.AsyncService;
import cn.hexing.db.batch.BaseBpEventHandler;
import cn.hexing.db.batch.event.BpLog2DbEvent;
import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.message.IMessage;

public class BaseLog2DbHandler extends BaseBpEventHandler {
	protected static final EventType type = EventType.BP_LOG_DB;
	
	@Override
	public void handleEvent(IEvent event) {
		assert(event.getType() == type );
		BpLog2DbEvent e = (BpLog2DbEvent)event;
		handleLog2Db(e.getService(),e.getMessage() );
	}
	
	public void handleLog2Db(AsyncService service,IMessage msg){
		
	}

	@Override
	public EventType type() {
		return type;
	}

}
