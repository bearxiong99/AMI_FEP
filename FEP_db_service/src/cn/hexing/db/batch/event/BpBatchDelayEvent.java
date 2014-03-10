/**
 * 当某个DAO已经插入部分数据对象，个数不足一个批次。
 * 那么最多延迟一定时间后，就需要保存，防止无限等待。
 */
package cn.hexing.db.batch.event;

import cn.hexing.db.batch.dao.IBatchDao;
import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.message.IMessage;

/**
 */
public class BpBatchDelayEvent implements IEvent {
	private static final EventType type = EventType.BP_BATCH_DELAY;
	private IBatchDao dao;

	public BpBatchDelayEvent( IBatchDao dao ){
		this.dao = dao;
	}
	
	public IMessage getMessage() {
		return null;
	}

	public Object getSource() {
		return null;
	}

	public EventType getType() {
		return type;
	}

	public void setSource(Object src) {
	}

	public IBatchDao getDao() {
		return dao;
	}

	public void setDao(IBatchDao dao) {
		this.dao = dao;
	}

}
