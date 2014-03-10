/**
 * 
 */
package cn.hexing.db.batch;

import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.common.spi.IEventHandler;

/**
 *
 */
public abstract class BaseBpEventHandler implements IEventHandler {
	protected AsyncService service;
	protected int key = 0;
	
	public abstract EventType type();
	public abstract void handleEvent(IEvent event);
	
	public void setKey(int key){
		this.key = key;
	}

	public void setService(AsyncService s){
		service = s;
	}
	
}
