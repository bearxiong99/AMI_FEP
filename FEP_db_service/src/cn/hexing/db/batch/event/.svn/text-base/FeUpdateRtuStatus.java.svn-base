package cn.hexing.db.batch.event;

import cn.hexing.db.batch.AsyncService;
import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.message.IMessage;

/**
 * 更新前置机的通道、流量等信息。
 *
 */
public class FeUpdateRtuStatus implements IEvent {
	private static final EventType type = EventType.FE_RTU_CHANNEL;
	private AsyncService service;
	private Object rtu;
	
	public FeUpdateRtuStatus(AsyncService service,Object rtu){
		this.service = service;
		this.rtu = rtu;
	}
	
	public AsyncService getService(){
		return service;
	}

	public Object getRtu(){
		return rtu;
	}
	
	public IMessage getMessage() {
		return null;
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
