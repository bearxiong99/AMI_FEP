/**
 * 侦听JSocket的收、发消息，以及Jsocket连接或者关闭的状态。
 */
package cn.hexing.fk.sockclient;

import cn.hexing.fk.message.IMessage;

/**
 *
 */
public interface JSocketListener {
	void onReceive(JSocket client,IMessage msg);
	void onSend(JSocket client,IMessage msg);
	void onConnected(JSocket client);
	void onClose(JSocket client);
}
