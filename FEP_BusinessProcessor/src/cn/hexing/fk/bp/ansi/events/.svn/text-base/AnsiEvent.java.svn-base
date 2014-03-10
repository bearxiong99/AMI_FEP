package cn.hexing.fk.bp.ansi.events;

import java.util.ArrayList;
import java.util.List;

import cn.hexing.fas.model.AnsiRequest;
import cn.hexing.fas.model.FaalRequest;
import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.message.IMessage;

import com.hx.ansi.message.AnsiMessage;

/** 
 * @Description  xxxxx
 * @author  Rolinbor
 * @Copyright 2013 hexing Inc. All rights reserved
 * @time£º2013-3-14 ÏÂÎç03:11:25
 * @version 1.0 
 */

public class AnsiEvent implements IEvent{
	public static enum ANSIEventType { UNDEF,WEB_REQ,UP_MSG,LOGON,LOGOFF,TERMINATE,WAIT,DISCONNECT  };
	
	//WebRequest Event
	private FaalRequest request = null;
	public List<AnsiMessage> reqMessageList = new ArrayList<AnsiMessage>();
	public List<AnsiMessage> repMessageList = new ArrayList<AnsiMessage>();

	//ANSI up-link message event
	private AnsiMessage upMessage = null;
	

	private final long eventTime = System.currentTimeMillis();
	private Object source = null;
	private ANSIEventType eventType = ANSIEventType.UNDEF;
	public AnsiEvent(){ }
	
	public AnsiEvent(FaalRequest webReq){
		request = webReq;
		eventType = ANSIEventType.WEB_REQ;
	}
	
	public AnsiEvent(AnsiMessage msg){
		upMessage = msg;
		eventType = ANSIEventType.UP_MSG;
	}

	@Override
	public EventType getType() {
		return EventType.SYS_UNDEFINE;
	}

	@Override
	public Object getSource() {
		return source;
	}

	@Override
	public void setSource(Object src) {
		source = src;
	}

	@Override
	public IMessage getMessage() {
		return upMessage;
	}
	
	public void setMessage(AnsiMessage msg){
		upMessage = msg;
	}
	

	public final long eventTime() {
		return eventTime;
	}
	
	public ANSIEventType eventType(){
		return eventType;
	}
	public final AnsiMessage upMessage(){
		return upMessage;
	}
	
	public AnsiMessage getUpMessage() {
		return upMessage;
	}

	public void setUpMessage(AnsiMessage upMessage) {
		this.upMessage = upMessage;
	}

	public ANSIEventType getEventType() {
		return eventType;
	}

	public void setEventType(ANSIEventType eventType) {
		this.eventType = eventType;
	}

	public long getEventTime() {
		return eventTime;
	}

	public final FaalRequest getRequest() {
		return request;
	}

	public final void setRequest(FaalRequest request) {
		this.request = request;
	}
	
	public final AnsiRequest getAnsiRequest(){
		return (AnsiRequest)request;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("ANSIEvent--> EVENT_TYPE :"+eventType+",");
		if(request !=null && request instanceof AnsiRequest){
			AnsiRequest dr = (AnsiRequest) request;
			sb.append(" MeterId="+dr.getMeterId()+", Operator:"+dr.getOperator());
		}
		if(upMessage!=null){
			sb.append(" MSG:"+upMessage.toString());
		}
		return sb.toString();
	}
}
