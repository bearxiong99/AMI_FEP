package cn.hexing.fk.bp.webapi;

import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.IMessageCreator;

public class MessageWebCreator implements IMessageCreator {

	/**
	 * 主站与业务处理器（web-bp）之间心跳
	 */
	public IMessage createHeartBeat(int reqNum) {
		return MessageWeb.createHRequest(reqNum);
	}

	public IMessage create() {
		return new MessageWeb();
	}

}
