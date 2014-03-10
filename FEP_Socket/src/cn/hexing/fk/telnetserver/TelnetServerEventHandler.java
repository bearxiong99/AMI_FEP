package cn.hexing.fk.telnetserver;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.events.BasicEventHook;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.common.spi.socket.IChannel;
import cn.hexing.fk.message.msgbytes.MessageBytes;
import cn.hexing.fk.sockserver.event.AcceptEvent;
import cn.hexing.fk.sockserver.event.ClientCloseEvent;

public class TelnetServerEventHandler extends BasicEventHook {
	private static final Logger log = Logger.getLogger(TelnetServerEventHandler.class);
	private Map<IChannel,TelnetSession> clients = Collections.synchronizedMap(new HashMap<IChannel,TelnetSession>());

	public TelnetServerEventHandler(){
		super.addHandler(EventType.ACCEPTCLIENT, this);
		super.addHandler(EventType.CLIENTCLOSE, this);
		super.addHandler(EventType.MSG_RECV, this);
		super.addHandler(EventType.MSG_SENT, this);
	}
	
	@Override
	public void handleEvent(IEvent e) {
		if( e.getType() == EventType.MSG_RECV ){
			//���յ�telnet client ����
			MessageBytes msg = (MessageBytes)e.getMessage();
//			log.info("telnet server recv:"+msg.getRawPacketString());
			TelnetSession ts = clients.get(msg.getSource());
			ts.onReceive(msg.getRawPacket());
		}
		else if( e.getType() == EventType.MSG_SENT ){
			//���ɹ��ѱ��ķ��͸�telnet client
			MessageBytes msg = (MessageBytes)e.getMessage();
			if( log.isDebugEnabled() )
				log.debug("send to telnet client: " + new String(msg.getRawPacket()));
		}
		else if( e.getType() == EventType.ACCEPTCLIENT ){
			AcceptEvent ae = (AcceptEvent)e;
			if (ae.getClient().getChannel().isConnected()){
				TelnetSession session = new TelnetSession(ae.getClient());
				//ͨ�����÷�ʽ������TelnetSession��������...
				clients.put(ae.getClient(), session);
				//���ͻ��˷���Welcome��Ϣ
			}
		}
		else if( e.getType() == EventType.CLIENTCLOSE ){
			ClientCloseEvent ce = (ClientCloseEvent)e;
			clients.remove(ce.getClient());
		}
		else
			super.handleEvent(e);
	}

}
