package cn.hexing.fk.bp.dlms.events;

import java.util.ArrayList;
import java.util.List;

import cn.hexing.fas.model.FaalRequest;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.message.IMessage;

import com.hx.dlms.message.DlmsMessage;

public class DlmsEvent implements IEvent {
	public static enum DlmsEventType { UNDEF,WEB_REQ,UP_DLMS_MSG, READ_ALARM };
	
	//WebRequest Event
	private FaalRequest request = null;
	public List<DlmsMessage> reqMessageList = new ArrayList<DlmsMessage>();
	public List<DlmsMessage> repMessageList = new ArrayList<DlmsMessage>();

	//DLMS up-link message event
	private DlmsMessage upMessage = null;
	
	private final long eventTime = System.currentTimeMillis();
	private Object source = null;
	private DlmsEventType eventType = DlmsEventType.UNDEF;

	public DlmsEvent(){ }
	
	public DlmsEvent(FaalRequest webReq){
		request = webReq;
		eventType = DlmsEventType.WEB_REQ;
	}
	
	public DlmsEvent(DlmsMessage msg){
		upMessage = msg;
		eventType = DlmsEventType.UP_DLMS_MSG;
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
	
	public void setMessage(DlmsMessage dlmsMsg){
		upMessage = dlmsMsg;
	}
	
	public final DlmsMessage upMessage(){
		return upMessage;
	}

	public final long eventTime() {
		return eventTime;
	}
	
	public DlmsEventType eventType(){
		return eventType;
	}

	public final FaalRequest getRequest() {
		return request;
	}

	public final void setRequest(FaalRequest request) {
		this.request = request;
	}
	
	public final DlmsRequest getDlmsRequest(){
		return (DlmsRequest)request;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("DlmsEvent--> EVENT_TYPE :"+eventType+",");
		if(request !=null && request instanceof DlmsRequest){
			DlmsRequest dr = (DlmsRequest) request;
			sb.append(" MeterId="+dr.getMeterId()+", Operator:"+dr.getOperator());
		}
		if(upMessage!=null){
			sb.append(" MSG:"+upMessage.toString());
		}
		return sb.toString();
	}
}
