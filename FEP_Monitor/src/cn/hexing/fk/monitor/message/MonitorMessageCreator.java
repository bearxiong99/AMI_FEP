package cn.hexing.fk.monitor.message;

import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.IMessageCreator;

public class MonitorMessageCreator implements IMessageCreator {

	public IMessage createHeartBeat(int reqNum) {
		return null;
	}

	public IMessage create() {
		return new MonitorMessage();
	}

}
