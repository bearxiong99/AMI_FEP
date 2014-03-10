package cn.hexing.dp.bpserver;

import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.IMessageCreator;
import cn.hexing.fk.message.gate.GateHead;
import cn.hexing.fk.message.gate.MessageGate;

public class TPMessageCreator implements IMessageCreator {

	public IMessage create() {
		return new TPMessage();
	}

	public IMessage createHeartBeat(int reqNum) {
		MessageGate gm = MessageGate.createHRequest(reqNum);
		gm.getHead().setAttribute(GateHead.ATT_MSGSEQ, gm.hashCode());
		return gm;
	}

}
