package cn.hexing.fk.monitor.client;

import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.IMessageCreator;
import cn.hexing.fk.monitor.message.MonitorMessage;

public class MonitorMessageCreator implements IMessageCreator {

	public IMessage createHeartBeat(int reqNum) {
		return null;
	}

	public IMessage create() {
		return new MonitorMessage();
	}

}
