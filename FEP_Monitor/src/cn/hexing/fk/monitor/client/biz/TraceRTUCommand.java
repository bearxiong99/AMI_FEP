package cn.hexing.fk.monitor.client.biz;

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

import cn.hexing.fk.monitor.MonitorCommand;
import cn.hexing.fk.monitor.message.MonitorMessage;
import cn.hexing.fk.sockclient.JSocket;
import cn.hexing.fk.utils.HexDump;

public class TraceRTUCommand {
	private static final Logger log = Logger.getLogger(TraceRTUCommand.class);
	public void startTrace(JSocket client, int[]rtus ){
		short cmd = MonitorCommand.CMD_TRACE_RTU;
		MonitorMessage msg = new MonitorMessage();
		msg.setCommand(cmd);
		ByteBuffer body = ByteBuffer.allocate(rtus.length*4);
		for(int i=0; i<rtus.length; i++ ){
			log.debug("TRACE RTUA="+HexDump.toHex(rtus[i]));
			body.putInt(rtus[i]);
		}
		body.flip();
		msg.setBody(body);
		client.sendMessage(msg);
	}

	public void stopTrace(JSocket client){
		short cmd = MonitorCommand.CMD_TRACE_ABORT;
		MonitorMessage msg = new MonitorMessage();
		msg.setCommand(cmd);
		ByteBuffer body = ByteBuffer.allocate(0);
		msg.setBody(body);
		client.sendMessage(msg);
	}
}
