package cn.hexing.fk.message.gate;

import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.IMessageCreator;

public class MessageGateCreator implements IMessageCreator {

	/**
	 * ǰ�û��ڲ���BP-FE-GATE��֮��������ͬʱҲ�������������ģ�
	 */
	public IMessage createHeartBeat(int reqNum) {
		return MessageGate.createHRequest(reqNum);
	}

	public IMessage create() {
		return new MessageGate();
	}

}
