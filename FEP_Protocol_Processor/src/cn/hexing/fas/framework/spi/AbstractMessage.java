package cn.hexing.fas.framework.spi;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.MessageType;


public abstract class AbstractMessage implements IMessage{
	
	public MessageType msgType=MessageType.MSG_INVAL;
	protected int state = IMessage.STATE_INVALID;		//消息处理状态
	private Object attachment;	//消息对象附加信息
	/**
	 * 设置属性： key="client"：表示客户端socket连接对象；
	 */
	protected HashMap hmAttributes = new HashMap();
	protected int priority = IMessage.PRIORITY_LOW;
	/**
	 * 特定消息对象自己定义报文头，与协议有关。
	 * 这里不定义消息报文头，主要是防止对象类型转换错误。
	 * 业务处理模块部分，需要判断消息对象的具体类型（ instanceof ），
	 * 转换到具体消息对象，这样协议头对象类型也就明确。
	 */
	public ByteBuffer dataOut; 	//消息请求数据域
	public ByteBuffer dataIn;	//消息应答数据域
//	public ByteBuffer rawData;
	public String upRawString = "";	//应答原始报文
	public String downRawString="";	//请求原始报文
	public AbstractMessage nextMessage; //应答消息可能包含多个帧。这里指向后续帧
	//每个消息的唯一key
	public long key = 0;	//key = rtua | fseq<<32
	public long reqTime = System.currentTimeMillis();
	public long repTime = 0;
	public int accesstime=0;	/*访问次数*/
	private boolean multiReply=false;	/*分帧回应标识*/
	
	/**
	 * modulerStack用来维护本消息的后续处理模块，以便控制器进行分发。
	 *  
	 */
	private Stack modulerStack = new Stack();
	
	public AbstractMessage(){
		nextMessage = null;
	}
	
	public int getPriority(){
		return priority;
	}
	
	public void setPriority(int priority){
		this.priority = priority;
	}
	

	


	
	public boolean hasNextModule(){
		return modulerStack.size()>0;
	}

	public Object getAttachment() {
		return attachment;
	}

	public void setAttachment(Object obj) {
		attachment = obj;
	}
	
	public MessageType getMessageType()
	{
		return msgType;
	}
	
	public IMessage getNextMessage()
	{
		return nextMessage;
	}

	public String getUpRawString() {
		return this.upRawString;
	}
	
	public String getDownRawString(){
		return this.downRawString;
	}

	public Object getAttribute(Object key) {
		return hmAttributes.get(key);
	}

	public void setAttribute(Object key, Object value) {
		if(hmAttributes.containsKey(key)){
			hmAttributes.remove(key);
		}
		hmAttributes.put(key,value);
	}
	
	public void removeAttribute(Object key){
		hmAttributes.remove(key);
	}
	
	public void copyAttributes(AbstractMessage src){
		Iterator it = src.hmAttributes.keySet().iterator();
		Object key;
		while(it.hasNext()){
			key = it.next();
			setAttribute(key,src.getAttribute(key));
		}
	}
/*	
	public ByteBuffer getRawReply(){
		return HexDump.toByteBuffer(upRawString);
	}
*/
	public int getRtuaIn() {
		return 0;
	}

	public int getRtuaOut() {
		return 0;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public boolean isMultiReply() {
		return multiReply;
	}

	public void setMultiReply(boolean multiReply) {
		this.multiReply = multiReply;
	}
}
