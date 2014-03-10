package cn.hexing.db.batch.event.adapt;

import cn.hexing.db.batch.AsyncService;
import cn.hexing.db.batch.BaseBpEventHandler;
import cn.hexing.db.batch.event.BpExpAlarmEvent;
import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.message.IMessage;

public class BaseExpAlarmHandler extends BaseBpEventHandler {
	protected static final EventType type = EventType.BP_EXP_ALARM;

	public EventType type(){
		return type;
	}
	
	@Override
	public void handleEvent(IEvent event) {
		assert(event.getType() == type );
		BpExpAlarmEvent e = (BpExpAlarmEvent)event;
		handleExpAlarm(e.getService(),e.getMessage() );
	}

	public void handleExpAlarm(AsyncService service,IMessage msg){
		
	}
}
