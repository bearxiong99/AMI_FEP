package cn.hexing.fk.fe.feintf;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import cn.hexing.fk.FasSystem;
import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.common.spi.IEventHandler;
import cn.hexing.fk.common.spi.socket.IChannel;
import cn.hexing.fk.fe.ChannelManage;
import cn.hexing.fk.fe.cluster.RealtimeSynchronizer;
import cn.hexing.fk.fe.cluster.RtuState;
import cn.hexing.fk.fe.filecache.HeartbeatPersist;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.gate.MessageGate;
import cn.hexing.fk.model.Master2FeRequest;
import cn.hexing.fk.sockserver.event.ReceiveMessageEvent;
import cn.hexing.fk.sockserver.event.SendMessageEvent;
import cn.hexing.fk.utils.HexDump;
import cn.hexing.fk.utils.StringUtil;
import cn.hexing.ws.logic.WsGlobalMap;

public class IntfClientEventHandler implements IEventHandler {
	private static final Logger log = Logger.getLogger(IntfClientEventHandler.class);

	private String name ="FE"; 
	
	public void handleEvent(IEvent event) {
		try{
			if( event.getType() == EventType.MSG_RECV ){
				//当收到FE接口服务下行报文
				onRecvMessage( (ReceiveMessageEvent)event);
			}
			else if( event.getType() == EventType.MSG_SENT ){
				//当成功把报文发送给FE接口服务器
				onSendMessage( (SendMessageEvent)event );
			}
			else{
				log.info("unhandled event:"+event);
			}
		}catch(Exception exp){
			log.error(exp.getLocalizedMessage(),exp);
		}
	}

	private void onRecvMessage(ReceiveMessageEvent e){
		IMessage msg = e.getMessage();
		if( msg instanceof MessageGate ){
			MessageGate gatemsg = (MessageGate)msg;
			//heart-beat' reply
			if( gatemsg.isHeartbeat() ){
				if( log.isDebugEnabled() )
					log.debug("FeIntf heart-beat reply message :"+msg.getRawPacketString());
				return;
			}
			if( log.isInfoEnabled() )
				log.info("FeIntf down message :"+msg.getRawPacketString());
			//receive Master-station commands, send it to all FE.
			if( gatemsg.getHead().getCommand() == MessageGate.MASTER_FE_CMD ){
				Object request = gatemsg.getDataObject();
				if( null == request ){
					log.warn("receive master-station command,but not found object. packet="+gatemsg.getRawPacketString());
					return;
				}
				if( request instanceof Master2FeRequest ){
					handleWebRequest( (Master2FeRequest)request, gatemsg, e);
				}
				return;
			}
			else if( gatemsg.getHead().getCommand() == MessageGate.CMD_WRAP){
				//update packet from main-station to RTU.
				IMessage msgUpdate = gatemsg.getInnerMessage();
				if( null != msgUpdate ){
					if( log.isInfoEnabled() )
						log.info("Forward update-message to RTU:" + msgUpdate);
					IChannel channel = ChannelManage.getInstance().getGPRSChannel(msgUpdate.getLogicalAddress());
					if( null == channel ){
						log.warn("Update Message Cann't send to RTU for no GPRS channel,RTUA="+HexDump.toHex(msgUpdate.getRtua()));
					}
					else{
						channel.send(msgUpdate);
					}
					return;
				}
			}
			//not applicable.
			log.warn("Should not go here. Something wrong.gate.CMD=" + gatemsg.getHead().getCommand()+ ",message="+msg.getRawPacketString());
			return;
		}
		log.error("N/A");
	}
	
	private void onSendMessage(SendMessageEvent e){
		IMessage msg = e.getMessage();
		if( log.isDebugEnabled() )
			log.debug("send to feIntf:"+msg.toString());
	}
	
	private void handleWebRequest(Master2FeRequest req,MessageGate msg, ReceiveMessageEvent e){
		switch( req.getCommand() ){
		case Master2FeRequest.CMD_FE_PROFILE:
			{
				String profile = FasSystem.getFasSystem().getProfile();
				req.setResult(0);
				List<String> result = new ArrayList<String>();
				int rootIndex = profile.indexOf("<root>")+"<root>".length();
				String head = profile.substring(0, rootIndex);
				if(!StringUtil.isEmptyString(name)){
					head+="\r\n    "+"<name>"+name+"</name>";
				}
				String tail = profile.substring(rootIndex);
				profile = head+tail;

				result.add(profile);
				req.setStrList(result);
				MessageGate reply = new MessageGate();
				reply.setDataObject(req);
				reply.getHead().setCommand(MessageGate.FE_MASTER_REP);
				e.getClient().send(reply);
			}
			break;
		case Master2FeRequest.CMD_GET_SAVEHEART_LIST:{
				List<String> rtus = WsGlobalMap.getInstance().getRtusHeartSave2Db();
				req.setResult(0);
				req.setStrList(rtus);
				MessageGate reply = new MessageGate();
				reply.setDataObject(req);
				reply.getHead().setCommand(MessageGate.FE_MASTER_REP);
				e.getClient().send(reply);
			}
			break;
		case Master2FeRequest.CMD_UPDATE_SIM:
			for( int i=0; i<req.getRtuaList().size(); i++ ){
				int rtua = req.getRtuaList().get(i);
				String assets = req.getStrList().get(i).trim();
				String simNum = "", zdyt="", dwdm="";
				String[] strstr = assets.split(",");
				simNum = strstr[0].trim();
				if( strstr.length>=1 )
					zdyt = strstr[1].trim();
				if( strstr.length>=2 ){
					dwdm = strstr[2].trim();
					if( dwdm.length()>=5 )
						dwdm = dwdm.substring(3,5);
					if( dwdm.length()>2 )
						dwdm = dwdm.substring(0, 2);
				}
				RtuState stateOld = RealtimeSynchronizer.getInstance().getRtuState(HexDump.toHex(rtua));
//				RtuState stateNew = RealtimeSynchronizer.getInstance().loadFromDb(rtua);
				if( null != stateOld ){
					stateOld.setSimNum(simNum);
					if( zdyt.length()>0 )
						stateOld.setUsage(zdyt);
//					else if( StringUtils.hasText(stateNew.getUsage()) )
//						stateOld.setUsage(stateNew.getUsage());
					if( dwdm.length()>0 )
						stateOld.setDwdm(dwdm);
//					else if( StringUtils.hasText(stateNew.getDwdm()))
//						stateOld.setDwdm(stateNew.getDwdm());
					RealtimeSynchronizer.getInstance().setRtuState(stateOld);
				}
				else{
					RtuState stateNew = RealtimeSynchronizer.getInstance().getRtuStateCreate(HexDump.toHex(rtua));
					stateNew.setSimNum(simNum);
					if( zdyt.length()>0 )
						stateNew.setUsage(zdyt);
					if( dwdm.length()>0 )
						stateNew.setDwdm(dwdm);
					RealtimeSynchronizer.getInstance().setRtuState(stateNew);
				}
				if( log.isInfoEnabled() )
					log.info("simNumber updated. RTUA="+HexDump.toHex(rtua)+",sim="+simNum+",dwdm="+dwdm+",zdyt="+zdyt);
			}
			break;
		case Master2FeRequest.CMD_GET_IS_ALL_SAVE_HEART:{
				boolean isSave=WsGlobalMap.getInstance().isSaveHeartBeat();
				req.setResult(0);
				List<String> strList=new ArrayList<String>();
				strList.add(Boolean.toString(isSave));
				req.setStrList(strList);
				MessageGate reply = new MessageGate();
				reply.setDataObject(req);
				reply.getHead().setCommand(MessageGate.FE_MASTER_REP);
				e.getClient().send(reply);
			}
			break;
		case Master2FeRequest.CMD_HEART_SWITCH_LOG:
			WsGlobalMap.getInstance().setSaveHeartBeat(req.isSaveHeartBeart());
			break;
		case Master2FeRequest.CMD_ENABLE_HEART_LOG:
			for( int i=0; i<req.getLogicAddrList().size(); i++ ){
				String rtua = req.getLogicAddrList().get(i);
				WsGlobalMap.getInstance().setRtuHeartbeatSaveFlag(rtua, true);
			}
			break;
		case Master2FeRequest.CMD_DISABLE_HEART_LOG:
			for( int i=0; i<req.getLogicAddrList().size(); i++ ){
				String rtua = req.getLogicAddrList().get(i);
				WsGlobalMap.getInstance().setRtuHeartbeatSaveFlag(rtua, false);
			}
			break;
		case Master2FeRequest.CMD_QUERY_RTU_HEARTBEAT:
			if( req.getRtuaList().size()>0 ){
				int rtua = req.getRtuaList().get(0);
				String heartBeatInfo = HeartbeatPersist.getInstance().queryHeartbeatInfo(rtua);
				req.setResult(0);
				List<String> result = new ArrayList<String>();
				result.add(heartBeatInfo);
				req.setStrList(result);
				MessageGate reply = new MessageGate();
				reply.setDataObject(req);
				reply.getHead().setCommand(MessageGate.FE_MASTER_REP);
				e.getClient().send(reply);
			}
			break;
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
