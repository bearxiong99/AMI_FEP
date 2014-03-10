/**
 * FE cluster-clients connect to this FE interface server, which supports Master-Station or manufacture's
 * third-part RTU updating-ware send request to clustered FEs.
 */
package cn.hexing.plm.feintf.feserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.events.BasicEventHook;
import cn.hexing.fk.common.simpletimer.ITimerFunctor;
import cn.hexing.fk.common.simpletimer.TimerData;
import cn.hexing.fk.common.simpletimer.TimerScheduler;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.common.spi.socket.IChannel;
import cn.hexing.fk.common.spi.socket.IServerSideChannel;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.gate.MessageGate;
import cn.hexing.fk.message.gw.MessageGw;
import cn.hexing.fk.message.zj.MessageZj;
import cn.hexing.fk.model.Master2FeRequest;
import cn.hexing.fk.sockserver.event.AcceptEvent;
import cn.hexing.fk.sockserver.event.ClientCloseEvent;
import cn.hexing.fk.sockserver.event.ReceiveMessageEvent;
import cn.hexing.fk.sockserver.event.SendMessageEvent;
import cn.hexing.plm.updatertu.UpdateRtuModule;

/**
 *
 */
public class FeEventHandler extends BasicEventHook {
	//class static properties
	private static final Logger log = Logger.getLogger(FeEventHandler.class);
	//configurable attributes
	private int minWaitTime = 60;		//Master-station request minimum wait time (seconds).
	private int timerPeriod = 1;		//timer period (seconds)
	
	//class local properties.
	private final List<IServerSideChannel> clients = Collections.synchronizedList(new ArrayList<IServerSideChannel>());
	private final HashMap<Integer,FeRequestItem> feRequests = new HashMap<Integer,FeRequestItem>();
	private ITimerFunctor timer = null;
	private int feCmdSeq = 10000;
	
	public int getActiveFECount(){
		return clients.size();
	}
	
	public boolean isFeChannelsIdle(){
		boolean busy = false;
		for(int i=0; i<clients.size(); i++){
			busy = busy || clients.get(i).sendQueueSize()>2 ;
		}
		return !busy;
	}
	
	private class FeRequestItem{
		public IChannel channel;
		public Master2FeRequest request;
		public int seq;
		public long reqtime ;
		public List<Master2FeRequest> replys = new ArrayList<Master2FeRequest>();
	}
	
	public boolean sendMessage(IMessage msg,boolean sendAllTag){
		if( getActiveFECount()==0 )
			return false;
		if( msg instanceof MessageGate ){
			MessageGate gatemsg = (MessageGate)msg;
			if( gatemsg.getHead().getCommand() == MessageGate.MASTER_FE_CMD ){
				FeRequestItem item = new FeRequestItem();
				item.request = (Master2FeRequest)gatemsg.getDataObject();
				if( item.request.getCmdSeq() == 0 )
					item.request.setCmdSeq(feCmdSeq++);
				item.seq = item.request.getCmdSeq();
				item.channel = gatemsg.getSource();
				item.reqtime = System.currentTimeMillis();
				synchronized(feRequests){
					feRequests.put(item.seq, item);
				}
				if( log.isDebugEnabled() )
					log.debug("request="+item.request.desc());
				int cmd = gatemsg.getHead().getCommand();
				gatemsg.setDataObject(item.request);
				gatemsg.getHead().setCommand(cmd);
				msg = gatemsg;
			}
		}
		boolean result = false;
		for(int i=0; i<clients.size(); i++ )
		{
			if( clients.get(i).send(msg) ){
				result = true;
				log.debug("send command to FE,peerAddr="+clients.get(i).getPeerAddr()+",clients.size="+clients.size());
				if (!sendAllTag)
					break;
				else{
					if( msg instanceof MessageGate ){
						MessageGate gatemsg = (MessageGate)msg;
						//gatemsg.getHead().setCommand(MessageGate.MASTER_FE_CMD);
						//gatemsg.getData().rewind();
						msg = gatemsg;
					}
				}
			}
		}
		
		return result;
	}
	
	@Override
	public void handleEvent(IEvent event) {
		if( event.getType() == EventType.MSG_RECV ){
			//当收到业务处理器下行报文
			onRecvMessage( (ReceiveMessageEvent)event);
		}
		else if( event.getType() == EventType.MSG_SENT ){
			//当成功把报文发送给业务处理器
			onSendMessage( (SendMessageEvent)event );
		}
		else if( event.getType() == EventType.ACCEPTCLIENT ){
			IServerSideChannel client = ((AcceptEvent)event).getClient();
			if( !clients.contains(client) )
				clients.add(client);
		}
		else if( event.getType() == EventType.CLIENTCLOSE ){
			IServerSideChannel client = ((ClientCloseEvent)event).getClient();
			clients.remove(client);
		}
		else
			super.handleEvent(event);
	}
	
	private void onRecvMessage(ReceiveMessageEvent e){
		IMessage msg = e.getMessage();
		if( msg instanceof MessageGate ){
			MessageGate gatemsg = (MessageGate)msg;
			//reply heart-beat
			if( gatemsg.isHeartbeat() ){
				if( log.isDebugEnabled() )
					log.debug("UP Message << "+msg.getRawPacketString());
				e.getClient().send(MessageGate.createHReply());
				return;
			}
			if( log.isDebugEnabled() )
				log.debug("UP Message << "+msg.getRawPacketString());
			//receive reply from FE for Master-station commands
			if( gatemsg.getHead().getCommand() == MessageGate.FE_MASTER_REP || gatemsg.getHead().getCommand() == MessageGate.MASTER_FE_CMD ){
				Object reply = gatemsg.getDataObject();
				if( null == reply ){
					log.warn("receive FE to master-station reply,but not found object. packet="+gatemsg.getRawPacketString());
					return;
				}
				if( reply instanceof Master2FeRequest ){
					_handleFeReply( (Master2FeRequest)reply,e);
				}
			}
			else if( gatemsg.getHead().getCommand() == MessageGate.CMD_GATE_REPLY || gatemsg.getHead().getCommand() == MessageGate.CMD_WRAP){
				//update packet from RTU to main-station.
				IMessage imsg = gatemsg.getInnerMessage();
				if( null != imsg ){
					if( imsg instanceof MessageZj ){
						MessageZj zjmsg = (MessageZj)imsg;
						_handleZjMessage(zjmsg,e);
					}
					else if( imsg instanceof MessageGw ){
						MessageGw gwmsg = (MessageGw)imsg;
						_handleGwMessage(gwmsg,e);
					}
				}
			}
		}
		else if( msg instanceof MessageZj){
			if( log.isInfoEnabled() )
				log.info("UP Message << "+msg.getRawPacketString());
			MessageZj zjmsg = (MessageZj)msg;
			_handleZjMessage(zjmsg,e);
		}
		else if( msg instanceof MessageGw ){
			if( log.isInfoEnabled() )
				log.info("UP Message << "+msg.getRawPacketString());
			MessageGw gwmsg = (MessageGw)msg;
			_handleGwMessage(gwmsg,e);
		}
	}
	
	private void _handleZjMessage(MessageZj zjmsg, ReceiveMessageEvent e){
		UpdateRtuModule.getInstance().precessReplyMessage(zjmsg);
	}
	private void _handleGwMessage(MessageGw gwmsg, ReceiveMessageEvent e){
		UpdateRtuModule.getInstance().precessReplyMessage(gwmsg);
	}
	
	private void _handleFeReply(Master2FeRequest reply, ReceiveMessageEvent e){
		FeRequestItem reqItem = this.feRequests.get(reply.getCmdSeq());
		if( null == reqItem ){
			log.warn("FE reply: cmdSeq="+reply.getCmdSeq()+", is not found in req map.");
			return;
		}
		log.info("reply = "+ reply.desc());
		reqItem.replys.add(reply);
		if( reqItem.replys.size() >= clients.size() ){
			forwardReply2MasterStation(reqItem);
			feRequests.remove(reqItem.seq);
			long timeSpend = System.currentTimeMillis() - reqItem.reqtime;
			log.info("Time takes=" + timeSpend );
		}
	}
	
	private void forwardReply2MasterStation(FeRequestItem reqItem){
		IChannel srcChannel = reqItem.channel;
		MessageGate replyMsg = new MessageGate();
		Master2FeRequest reqObj = (Master2FeRequest)reqItem.request;
		reqObj.setResult(0);
		if( null == reqObj.getStrList() )
			reqObj.setStrList(new ArrayList<String>());
		for(int i=0; i<reqItem.replys.size(); i++ ){
			reqObj.getStrList().addAll(reqItem.replys.get(i).getStrList());
			reqObj.setResult(reqItem.replys.get(i).getResult());
		}
		if( log.isInfoEnabled() ){
			for(String line: reqObj.getStrList() ){
				log.info("------"+line);
			}
		}
		replyMsg.setDataObject(reqObj);
		replyMsg.getHead().setCommand(MessageGate.FE_MASTER_REP);
		srcChannel.send( replyMsg );
	}
	
	private void onSendMessage(SendMessageEvent e){
		//send message to FEs.
		log.debug("Send to FE("+e.getClient().getPeerAddr()+") Success: "+e.getMessage().getRawPacketString());
		if( isFeChannelsIdle() ){
			UpdateRtuModule.getInstance().trySendNextPacket();
		}
	}

	@Override
	public boolean start() {
		if( null == timer ){
			timer = new ITimerFunctor(){
				public void onTimer(int id) {
					if( feRequests.size()>0 ){
						synchronized(feRequests){
							List<FeRequestItem> reqs = new ArrayList<FeRequestItem>();
							for( FeRequestItem reqItem: feRequests.values() ){
								long dif = System.currentTimeMillis() - reqItem.reqtime;
								if( dif > minWaitTime*1000 ){
									log.warn("timeout, span="+dif);
									reqs.add(reqItem);
								}
							}
							for(FeRequestItem item: reqs){
								feRequests.remove(item.seq);
								forwardReply2MasterStation(item);
							}
							if( reqs.size()>0 )
								log.warn("Master-Station request timeout, forced replys="+reqs.size());
							reqs = null;
						}
					}
					
				}};
			TimerScheduler.getScheduler().addTimer(new TimerData(timer,0,timerPeriod));
		}
		return super.start();
	}

	@Override
	public void stop() {
		if( null != timer ){
			TimerScheduler.getScheduler().removeTimer(timer, 0);
			timer = null;
		}
		super.stop();
	}

	public void setMinWaitTime(int minWaitTime) {
		this.minWaitTime = minWaitTime;
	}

	public void setTimerPeriod(int timerPeriod) {
		this.timerPeriod = timerPeriod>0 ? timerPeriod : 1;
	}
	
}
