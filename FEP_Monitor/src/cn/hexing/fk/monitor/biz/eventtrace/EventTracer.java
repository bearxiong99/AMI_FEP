package cn.hexing.fk.monitor.biz.eventtrace;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.common.spi.IEventTrace;
import cn.hexing.fk.common.spi.socket.IChannel;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.MessageType;
import cn.hexing.fk.message.gate.MessageGate;
import cn.hexing.fk.message.zj.MessageZj;
import cn.hexing.fk.monitor.MonitorCommand;
import cn.hexing.fk.monitor.message.MonitorMessage;
import cn.hexing.fk.utils.CalendarUtil;
/**
 * 浙江网关事件跟踪接口实现。
 *
 */
public class EventTracer implements IEventTrace {
	private ArrayList<IChannel> monitorClients = new ArrayList<IChannel>();
	private int[] rtus = null;

	public EventTracer(IChannel client){
		monitorClients.add(client);
	}
	
	public void addClient(IChannel client){
		synchronized(monitorClients){
			monitorClients.remove(client);
			monitorClients.add(client);
		}
	}
	
	public int removeClient(IChannel client){
		synchronized(monitorClients){
			monitorClients.remove(client);
			if( monitorClients.size() == 0 )
				rtus = null;
			return monitorClients.size();
		}
	}
	
	public void traceRtus(int [] tobeTraced ){
		if( null == tobeTraced )
			return;
		if( null == rtus ){
			rtus = tobeTraced;
			return;
		}
		int count = 0;	//总计个数。下面去掉重复RTUA
		int [] tp = new int[tobeTraced.length];
		for(int i=0; i<tobeTraced.length; i++ ){
			boolean found = false;
			for(int j=0; j<rtus.length; j++ ){
				if( rtus[j] == tobeTraced[i] ){
					found = true;
					break;
				}
			}
			if( !found ){
				tp[count] = tobeTraced[i];
				count++;
			}
		}
		if( 0 == count )
			return;
		int[] newRtus = new int[rtus.length+count];
		System.arraycopy(rtus, 0, newRtus, 0, rtus.length);
		System.arraycopy(tp, 0, newRtus, rtus.length, count);
		rtus = newRtus;
	}
	
	/**
	 * 目前支持跟踪消息的接收与发送事件。其它事件对于监控管理服务器没有意义。
	 */
	public void traceEvent(IEvent e) {
		if( e.getType() == EventType.MSG_RECV ){
			IMessage msg = e.getMessage();
			IChannel client = (IChannel)msg.getSource();
			//对于网关消息，需要从中抽取浙江规约消息。
			MessageZj message = null;
			if( msg instanceof MessageZj ){
				message = (MessageZj)msg;
			}
			else if( msg.getMessageType() == MessageType.MSG_GATE ){
				message = (MessageZj)((MessageGate)msg).getInnerMessage();
				if( null == message )
					return;
				message.setIoTime(msg.getIoTime());
			}
			else
				return;
			//匹配监控的RTUA
			if( ! _isMonited(message.head.rtua) )
				return;
			StringBuffer sb = new StringBuffer(400);
			sb.append("收到:【").append(client.getServer().getName()).append("】,时间=");
			sb.append(CalendarUtil.getMilliDateTimeString(message.getIoTime()));
			sb.append(", 报文=").append(message.getRawPacketString()).append("\r\n");
			_sendIndication(sb.toString());
		}
		else if( e.getType() == EventType.MSG_SENT ){
			IMessage msg = e.getMessage();
			IChannel client = (IChannel)msg.getSource();
			//对于网关消息，需要从中抽取浙江规约消息。
			MessageZj message = null;
			if( msg instanceof MessageZj ){
				message = (MessageZj)msg;
			}
			else if( msg.getMessageType() == MessageType.MSG_GATE ){
				message = (MessageZj)((MessageGate)msg).getInnerMessage();
				if( null == message )
					return;
				message.setIoTime(msg.getIoTime());
			}
			else
				return;
			//匹配监控的RTUA
			if( ! _isMonited(message.head.rtua) )
				return;
			
			StringBuffer sb = new StringBuffer(400);
			sb.append("发送:【").append(client.getServer().getName()).append("】,时间=");
			sb.append(CalendarUtil.getMilliDateTimeString(message.getIoTime()));
			sb.append(", 报文=").append(message.getRawPacketString()).append("\r\n");
			_sendIndication(sb.toString());
		}
	}
	
	/**
	 * 向监控管理客户端发送监控跟踪信息。
	 * @param sb
	 */
	private void _sendIndication(String info){
		MonitorMessage msg = new MonitorMessage();
		msg.setCommand(MonitorCommand.CMD_TRACE_IND);
		ByteBuffer body = ByteBuffer.wrap(info.getBytes());
		msg.setBody(body);
		synchronized(monitorClients){
			for( IChannel client: monitorClients )
				client.send(msg);
		}
	}
	
	private boolean _isMonited(int rtua){
		try{
			for(int i=0; i<rtus.length; i++ ){
				if( rtus[i] == rtua )
					return true;
			}
		}catch(Exception e){}
		return false;
	}

}
