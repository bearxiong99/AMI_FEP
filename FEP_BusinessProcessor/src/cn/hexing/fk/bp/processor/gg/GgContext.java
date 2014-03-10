package cn.hexing.fk.bp.processor.gg;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.hexing.fk.message.IMessage;

/**
 * 
 * @author gaoll
 *
 * @time 2013-8-15 上午09:37:17
 *
 * @info 广规上下行Context
 */
public class GgContext {
	public List<IMessage> webReqList = Collections.synchronizedList(new LinkedList<IMessage>()); //GGMessage

	
	public AtomicBoolean waitReply = new AtomicBoolean(false);
	public IMessage curDownMsg = null;
	
	public long lastSendTime = 0;
	
	public int resendCount=0;
	
	public String logicAddress;
	
	public void onRequestFinished(){
		enableSend();
		
	}
	public void enableSend(){
		curDownMsg = null;
		resendCount=0;
		waitReply.set(false);
	}
	
	
}
