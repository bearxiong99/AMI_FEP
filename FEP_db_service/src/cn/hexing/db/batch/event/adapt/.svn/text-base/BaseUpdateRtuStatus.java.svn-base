package cn.hexing.db.batch.event.adapt;

import cn.hexing.db.batch.BaseBpEventHandler;
import cn.hexing.db.batch.event.FeUpdateRtuStatus;
import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.spi.IEvent;

public class BaseUpdateRtuStatus extends BaseBpEventHandler {
	private static final EventType type = EventType.FE_RTU_CHANNEL;
	
	@Override
	public void handleEvent(IEvent event) {
		assert(event.getType() == type );
		FeUpdateRtuStatus ev = (FeUpdateRtuStatus)event;
		service.addToDao(ev.getRtu(),key);
	}

	@Override
	public EventType type() {
		return type;
	}

}
