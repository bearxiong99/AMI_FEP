package cn.hexing.fk.clientmod;

import org.apache.log4j.Logger;

import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.common.spi.IEventHandler;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.gate.MessageGate;
import cn.hexing.fk.sockserver.event.ReceiveMessageEvent;
import cn.hexing.fk.sockserver.event.SendMessageEvent;
/**
 * Cluster-client event handler default implementation.
 *
 */

public class ClusterClientDefaultHandler implements IEventHandler {
	private static final Logger log = Logger.getLogger(ClusterClientDefaultHandler.class);
	
	public void handleEvent(IEvent event) {
		if( event.getType().equals(EventType.MSG_RECV) )
			onRecvMessage( (ReceiveMessageEvent)event);
		else if( event.getType().equals(EventType.MSG_SENT) )
			onSendMessage( (SendMessageEvent)event );
	}

	private void onRecvMessage(ReceiveMessageEvent e){
		IMessage msg = e.getMessage();
		if( !(msg instanceof MessageGate) )
			return;
		MessageGate mgate = (MessageGate)msg;
		if( mgate.isHeartbeat() )
			return;
		Object reply = FaalRequestMessage.getReply(mgate);
		if( log.isInfoEnabled() )
			log.info("receive Object:"+reply);
	}
	
	private void onSendMessage(SendMessageEvent e){
		IMessage msg = e.getMessage();
		if( !(msg instanceof MessageGate) )
			return;
		MessageGate mgate = (MessageGate)msg;
		if( mgate.isHeartbeat() )
			return;
		if( log.isInfoEnabled() )
			log.info("send Message:"+mgate);
	}
}
