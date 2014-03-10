/**
 * 应用系统级别命令。
 */
package cn.hexing.fk.monitor.client.biz;

import java.nio.ByteBuffer;

import cn.hexing.fk.monitor.MonitorCommand;
import cn.hexing.fk.monitor.message.MonitorMessage;
import cn.hexing.fk.sockclient.JSocket;

/**
 *
 */
public class SystemCommand {
	public void shutdown(JSocket client){
		MonitorMessage msg = new MonitorMessage();
		msg.setCommand(MonitorCommand.CMD_SYS_STOP);
		ByteBuffer body = ByteBuffer.allocate(0);
		msg.setBody(body);
		client.sendMessage(msg);
	}
}
