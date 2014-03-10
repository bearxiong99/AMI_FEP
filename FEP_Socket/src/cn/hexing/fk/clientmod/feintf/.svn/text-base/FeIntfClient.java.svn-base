package cn.hexing.fk.clientmod.feintf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import cn.hexing.fk.clientmod.ClientModule;
import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.common.spi.IEventHandler;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.gate.MessageGate;
import cn.hexing.fk.message.gate.MessageGateCreator;
import cn.hexing.fk.model.Master2FeRequest;
import cn.hexing.fk.sockserver.event.ReceiveMessageEvent;
import cn.hexing.fk.sockserver.event.SendMessageEvent;

public class FeIntfClient {
	private static final Logger log = Logger.getLogger(FeIntfClient.class);
	
	//class local attributes
	private ClientModule client = null;
	private static IEventHandler eventHandler = new DefaultHandler();
	private static int seq = 1;
	private static final Object seqLock = new Object();
	private static int waitSeconds = 10;
	private static final Map<Integer,Master2FeRequest> waitMap = Collections.synchronizedMap(new HashMap<Integer,Master2FeRequest>());
	
	public FeIntfClient(){
		client = new ClientModule();
		client.setBufLength(1024*8);
		client.setEventHandler(eventHandler);
		client.setHeartInterval(20);
		client.setMessageCreator(new MessageGateCreator());
		client.setName("feIntf.client");
	}
	
	public List<String> getProfile(){
		Master2FeRequest req = new Master2FeRequest();
		req.setCmdSeq(nextCommandSeq());
		req.gatherFeProfile();
		List<String> list = doSendRequest(req);
		if( null == list )
			list = new ArrayList<String>();
		return list;
	}
	
	
	public boolean updateSIM(List<Integer>rtus ,List<String> sims){
		Master2FeRequest req = new Master2FeRequest();
		req.setCmdSeq(nextCommandSeq());
		req.updateSims(rtus, sims);
		return null != doSendRequest(req);
	}
	
	public boolean saveHeart2Db(boolean isSave){
		Master2FeRequest req = new Master2FeRequest();
		req.setCmdSeq(nextCommandSeq());
		req.saveHeart2Db(isSave);
		return null != doSendRequest(req);
	}
	
	public boolean saveHeart2Db(List<String>rtus){
		Master2FeRequest req = new Master2FeRequest();
		req.setCmdSeq(nextCommandSeq());
		req.saveHeart2Db(rtus);
		return null != doSendRequest(req);
	}
	
	/**
	 * 只有所有的FE，都不保存心跳，才返回true
	 * @return
	 */
	public boolean isAllFeSaveHeart(){
		Master2FeRequest req = new Master2FeRequest();
		req.setCmdSeq(nextCommandSeq());
		req.isAllFeSaveHeart();
		List<String> list = doSendRequest(req);
		for(String str: list){
			if("false".equals(str))
				return false;
		}
		return true;
	}
	
	public List<String> getSaveHeart2DbList(){
		Master2FeRequest req = new Master2FeRequest();
		req.setCmdSeq(nextCommandSeq());
		req.getSaveHeart2DbList();
		List<String> list = doSendRequest(req);
		if(list ==null)
			list = new ArrayList<String>();
		return list;
	}
	
	public boolean unsaveHeart2Db(List<String>rtus){
		Master2FeRequest req = new Master2FeRequest();
		req.setCmdSeq(nextCommandSeq());
		req.unsaveHeart2Db(rtus);
		return null != doSendRequest(req);
	}

	/**
	 * enable ZJ RTU do update.
	 * @param list
	 * @return
	 */
	public boolean enableUpdate(List<Integer> list){
		Master2FeRequest req = new Master2FeRequest();
		req.setCmdSeq(nextCommandSeq());
		req.enableUpdate(list);
		return null != doSendRequest(req);
	}
	public boolean disableUpdate(List<Integer> list){
		Master2FeRequest req = new Master2FeRequest();
		req.setCmdSeq(nextCommandSeq());
		req.disableUpdate(list);
		return null != doSendRequest(req);
	}
	public boolean clearUpdate(){
		Master2FeRequest req = new Master2FeRequest();
		req.setCmdSeq(nextCommandSeq());
		req.clearUpdate();
		return null != doSendRequest(req);
	}
	
	public String queryHeartBeatInfo(int rtua){
		Master2FeRequest req = new Master2FeRequest();
		req.setCmdSeq(nextCommandSeq());
		req.queryHeartBeatInfo(rtua);
		List<String> list = doSendRequest(req);
		
		if( null == list || list.size() == 0 )
			return "";
		else{
			for(String str : list){
				if( null == str || str.trim().length() == 0 )
					continue;
				return str;
			}
			return "";
		}
	}
	
	private int nextCommandSeq(){
		int result = 0;
		synchronized(seqLock){
			result = seq++;			
		}
		return result;
	}
	private List<String> doSendRequest(Master2FeRequest req){
		MessageGate msg = new MessageGate();
		msg.setDataObject(req);
		waitMap.put(req.getCmdSeq(), req);
		long time1 = System.currentTimeMillis();
		boolean success = client.sendMessage(msg);
		if( success && req.isNeedReply() ){
			synchronized(req){
				try{
					req.wait(waitSeconds*1000);
				}catch(Exception exp){}
			}
		}
		waitMap.remove(req.getCmdSeq());
		long timeSpan = System.currentTimeMillis() - time1;
		if( log.isInfoEnabled() )
			log.info("request execute time="+timeSpan+" milliseconds. req="+req.desc());
		if( req.isNeedReply() ){
			if( success )
				return req.getStrList();
			else
				return new ArrayList<String>();
		}
		else{
			if( success )
				return new ArrayList<String>();
			else
				return null;
		}
	}
	
	public void setIp(String ip){
		client.setHostIp(ip);
	}
	
	public void setPort(int p){
		client.setHostPort(p);
	}
	
	public void setEventHandler(IEventHandler handler){
		eventHandler = handler;
		client.setEventHandler(eventHandler);
	}
	
	public void init(){
		client.init();
		client.start();
	}
	
	private static class DefaultHandler implements IEventHandler {
		public void handleEvent(IEvent event) {
			if( event.getType() == EventType.MSG_RECV ){
				//当收到FE接口服务上行报文
				onRecvMessage( (ReceiveMessageEvent)event);
			}
			else if( event.getType() == EventType.MSG_SENT ){
				//当成功把报文发送给FE接口服务器
				onSendMessage( (SendMessageEvent)event );
			}
			else{
				log.info("unhandled event:"+event);
			}
		}

		private void onRecvMessage(ReceiveMessageEvent e){
			IMessage msg = e.getMessage();
			if( msg instanceof MessageGate ){
				MessageGate gatemsg = (MessageGate)msg;
				//heart-beat' reply
				if( gatemsg.isHeartbeat() ){
					if( log.isDebugEnabled() )
						log.debug("WebIntf heart-beat reply message :"+msg.getRawPacketString());
					return;
				}
				if( log.isInfoEnabled() )
					log.info("WebIntf up message :"+msg.getRawPacketString());
				//receive FE reply.
				if( gatemsg.getHead().getCommand() == MessageGate.FE_MASTER_REP ){
					Object reply = gatemsg.getDataObject();
					if( null == reply ){
						log.warn("receive master-station command,but not found object. packet="+gatemsg.getRawPacketString());
						return;
					}
					if( reply instanceof Master2FeRequest ){
						handleFeReply( (Master2FeRequest)reply, gatemsg, e);
					}
					return;
				}
				//not applicable.
				log.warn("Should not go here. Something wrong.gate.CMD=" + gatemsg.getHead().getCommand()+ ",message="+msg.getRawPacketString());
				return;
			}
			log.error("N/A");
		}
		
		private void onSendMessage(SendMessageEvent e){
		}
		
		private void handleFeReply(Master2FeRequest reply,MessageGate msg, ReceiveMessageEvent e){
			Master2FeRequest req = waitMap.remove(reply.getCmdSeq());
			if( null != req ){
				req.setStrList(reply.getStrList());
				req.setResult(reply.getResult());
				synchronized(req){
					req.notify();
				}
				if( log.isInfoEnabled() ){
					String info = "receive reply: "+ reply.desc();
					if( info.length()> 400 )
						info = info.substring(0, 400);
					log.info(info);
				}
			}
			else
				log.info(reply.desc());
		}
	}
}
