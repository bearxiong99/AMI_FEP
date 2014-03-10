/**
 * 终端模拟器接口定义
 */
package cn.hexing.fk.sockclient.async.simulator;

import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.sockclient.async.JAsyncSocket;

/**
 *
 */
public interface IRtuSimulator {
	int  getRtua();
	void setRtua(int rtua);
	void onConnect(JAsyncSocket client);
	void onClose(JAsyncSocket client);
	void onReceive(JAsyncSocket client,IMessage message);
	void onSend(JAsyncSocket client,IMessage message);
	void sendLogin();
	void sendHeart();
	void sendTask();
}
