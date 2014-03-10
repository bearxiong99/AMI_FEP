package cn.hexing.db.batch.event.adapt;

import cn.hexing.db.batch.BaseBpEventHandler;
import cn.hexing.db.batch.event.BpBatchDelayEvent;
import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.spi.IEvent;

public class BatchDelayHandler extends BaseBpEventHandler {
	private static final EventType type = EventType.BP_BATCH_DELAY;

	public void handleEvent(IEvent event) {
		assert(event.getType() == type );
		BpBatchDelayEvent ev = (BpBatchDelayEvent)event;
		ev.getDao().batchUpdate();
	}

	public EventType type() {
		return type;
	}

}
