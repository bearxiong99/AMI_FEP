package cn.hexing.fk.bp.webapi;

import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.IMessageCreator;

public class MessageWebCreator implements IMessageCreator {

	/**
	 * ��վ��ҵ��������web-bp��֮������
	 */
	public IMessage createHeartBeat(int reqNum) {
		return MessageWeb.createHRequest(reqNum);
	}

	public IMessage create() {
		return new MessageWeb();
	}

}
