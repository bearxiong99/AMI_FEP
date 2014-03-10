package cn.hexing.fk.gate.handler;

import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.common.spi.IEventHandler;
import cn.hexing.fk.gate.MessageQueue;

/**
 * 
 * @author gaoll
 *
 * @time 2013-3-15 下午1:54:34
 *
 * @info 主站作为客户端，终端作为服务端消息处理
 */
public class ActiveMessageHandler implements IEventHandler{
	protected MessageQueue queue;
	@Override
	public void handleEvent(IEvent event) {
		
		if(event.getType()==EventType.CLIENTCLOSE){
			System.out.println("Client Close");
		}
	}
	public final MessageQueue getQueue() {
		return queue;
	}
	public final void setQueue(MessageQueue queue) {
		this.queue = queue;
	}

}
