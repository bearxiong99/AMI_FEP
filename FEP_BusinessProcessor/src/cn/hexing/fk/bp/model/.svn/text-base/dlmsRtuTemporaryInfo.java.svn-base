package cn.hexing.fk.bp.model;

import java.util.ArrayList;

import cn.hexing.fk.message.IMessage;

public class dlmsRtuTemporaryInfo {
	private int rtua;
	//下行报文发送时间
	private long sendTime=System.currentTimeMillis();
	//上行报文接收时间
	private long revTime=System.currentTimeMillis();
	//下行报文id，解析时要用到,握手和认证报文的id有可能是无效的，因为超时重发的握手报文是固定的，只有数据帧的id才是有效
	private long id;
	//dlms上行报文临时队列
	private ArrayList<IMessage> msgList=new ArrayList<IMessage>();
	
	public long getSendTime() {
		return sendTime;
	}
	public void setSendTime(long sendTime) {
		this.sendTime = sendTime;
	}
	public ArrayList<IMessage> getMsgList() {
		return msgList;
	}
	public void setMsgList(ArrayList<IMessage> msgList) {
		this.msgList = msgList;
	}
	public int getRtua() {
		return rtua;
	}
	public void setRtua(int rtua) {
		this.rtua = rtua;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getRevTime() {
		return revTime;
	}
	public void setRevTime(long revTime) {
		this.revTime = revTime;
	}

	
}
