package cn.hexing.fk.bp.processor.gg;

import java.util.LinkedList;

import org.apache.log4j.Logger;

import cn.hexing.fk.common.events.BasicEventHook;
import cn.hexing.fk.common.simpletimer.ITimerFunctor;
import cn.hexing.fk.common.simpletimer.TimerData;
import cn.hexing.fk.common.simpletimer.TimerScheduler;
import cn.hexing.fk.common.spi.IMessageQueue;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.MessageConst;
import cn.hexing.fk.message.zj.MessageZj;



/**
 * 
 * @author gaoll
 *
 * @time 2013-8-15 上午09:36:09
 *
 * @info 广规G3消息处理器，由于上行消息没有帧序号，所以处理要将上下行对应起来
 */
public class GgMessageProcessor extends BasicEventHook{
	private static final Logger log = Logger.getLogger(GgMessageProcessor.class);
	
    public static  boolean isG3MeterBox=false;
	
	private GgMessageProcessor(){};
	
	private static GgMessageProcessor instance = new GgMessageProcessor();
	
	public static GgMessageProcessor getInstance(){return instance;};
	

	private LinkedList<GgContext> sendingList = new LinkedList<GgContext>();
	
	private IGgContextManager contextManager = LocalGgContextManager.getInstance();
	
	//send msg
	private IMessageQueue messageQueue = null;

	private int resendInterval = 35000;	//re-send interval, milliseconds
	
	private int maxResend = 0;

	public void init(){
		
		if(isG3MeterBox){
		
			if( resendInterval > 500 ){
				ITimerFunctor secondTimer = new ITimerFunctor(){
					@Override
					public void onTimer(int id) {
						GgContext cxt;
						synchronized( sendingList ){
							while( sendingList.size()>0 ){
								cxt = sendingList.getFirst();
								if( cxt.curDownMsg == null ){
									sendingList.removeFirst();
									continue;
								}
								if( System.currentTimeMillis()- cxt.lastSendTime < resendInterval )
									break;
								cxt = sendingList.removeFirst();
								if( ++ cxt.resendCount >= maxResend ){
									try {
										onRequestFailed(cxt);
									} catch (Exception e) {
										log.warn(e.getLocalizedMessage(),e);
									}
								}
								else{
									cxt.waitReply.set(false);
									sendNextMessage(cxt.logicAddress);
								}
							}
						}
					}
				};
				TimerScheduler.getScheduler().addTimer(new TimerData(secondTimer,1,1));
			}
			super.init();
			
		}
		
		
	}
	
	
	
	public void onRequestFailed(GgContext context){
		
		IMessage msg = null;
		if(context.webReqList.size()>=0)
			msg = context.webReqList.remove(0);
		
		log.warn("request fail,down msg is:"+msg);
		
		context.onRequestFinished();
		
		if(((MessageZj) msg).head.c_func == MessageConst.GG_UPGRADE){
			GgUpgradeProcessor.getInstance().onUpgradeUnReturn(msg);
		}
		
		sendNextMessage(context.logicAddress);
	}
	
	
	
	public IMessageQueue getMessageQueue() {
		return messageQueue;
	}


	public void setMessageQueue(IMessageQueue messageQueue) {
		this.messageQueue = messageQueue;
	}


	/**
	 * 发送消息
	 * @param logicAddress	要发送的对象
	 * @param iMessage 要发送的内容
	 */
	public void sendNextMessage(String logicAddress) {
		GgContext context=contextManager.getContext(logicAddress);
		
		if(context.webReqList.size()<=0) return;
		
		if(context.waitReply.compareAndSet(false, true)){
			
			IMessage msg=context.webReqList.get(0);
			context.curDownMsg = msg;
			context.lastSendTime = System.currentTimeMillis();
			
			synchronized(sendingList){
				sendingList.addLast(context);
			}
			
			messageQueue.sendMessage(msg);
			log.debug("send msg to fe :"+msg);
		}
	}

	
	public void addMessage(String logicAddress,IMessage msg){
		GgContext context=contextManager.getContext(logicAddress);
		context.webReqList.add(msg);
	}
	
	public IMessage onRequestComplete(String logicAddress){
		
		GgContext context=contextManager.getContext(logicAddress);
		
		synchronized (sendingList) {
			if(sendingList.size()>0 && context == sendingList.get(0)){
				sendingList.removeFirst();
			}			
		}
		
		context.onRequestFinished();
		
		if( context.webReqList.size()>0 ){
			IMessage msg = context.webReqList.remove(0);
			try {
				sendNextMessage(logicAddress);
			} catch (Exception e) {
				log.error("GgMessageProcessor onRequestComplete Fail", e);
			}
			return msg;
		}
		
		return null;
	}


	public void setG3MeterBox(boolean isG3MeterBox) {
		GgMessageProcessor.isG3MeterBox = isG3MeterBox;
	}



	public int getResendInterval() {
		return resendInterval;
	}



	public void setResendInterval(int resendInterval) {
		if( resendInterval < 500 && resendInterval > 0 )
			resendInterval *= 1000;
		this.resendInterval = resendInterval;
	}



	public int getMaxResend() {
		return maxResend;
	}



	public void setMaxResend(int maxResend) {
		this.maxResend = maxResend;
	}
	
	
}
