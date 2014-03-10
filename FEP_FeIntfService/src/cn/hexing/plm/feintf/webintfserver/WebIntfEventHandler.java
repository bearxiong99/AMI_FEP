package cn.hexing.plm.feintf.webintfserver;

import org.apache.log4j.Logger;

import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.events.BasicEventHook;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.gate.MessageGate;
import cn.hexing.fk.message.zj.MessageZj;
import cn.hexing.fk.model.Master2FeRequest;
import cn.hexing.fk.sockserver.event.ReceiveMessageEvent;
import cn.hexing.fk.sockserver.event.SendMessageEvent;
import cn.hexing.plm.updatertu.UpdateRtuModule;

public class WebIntfEventHandler extends BasicEventHook {
	private static final Logger log = Logger.getLogger(WebIntfEventHandler.class);

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
					log.debug("WebIntf down heart-beat message :"+msg.getRawPacketString());
				e.getClient().send(MessageGate.createHReply());
				return;
			}
			if( log.isDebugEnabled() )
				log.debug("WebIntf down message :"+msg.getRawPacketString());
			//receive Master-station commands, send it to all FE.
			if( gatemsg.getHead().getCommand() == MessageGate.MASTER_FE_CMD ){
				Object request = gatemsg.getDataObject();
				if( null == request ){
					log.warn("receive master-station command,but not found object. packet="+gatemsg.getRawPacketString());
					return;
				}
				if( request instanceof Master2FeRequest ){
					dispatchWebRequest( (Master2FeRequest)request, gatemsg, e);
				}
				return;
			}
			else if( gatemsg.getHead().getCommand() == MessageGate.CMD_WRAP){
				//update packet from RTU to main-station.
				IMessage imsg = gatemsg.getInnerMessage();
				if( null != imsg ){
					if( imsg instanceof MessageZj ){
						MessageZj zjmsg = (MessageZj)imsg;
						dispatchZjUpdateMessage(zjmsg,e);
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
	}
	
	private void dispatchWebRequest(Master2FeRequest req,MessageGate msg, ReceiveMessageEvent e){
		if( !UpdateRtuModule.getInstance().forwardMaster2Fe(msg, req) ){
			log.error("Cann't send message to FE: "+ msg.getRawPacketString());
		}
	}
	
	private void dispatchZjUpdateMessage(MessageZj zjmsg,ReceiveMessageEvent e){
		UpdateRtuModule.getInstance().forwardZjUpdate2Fe(zjmsg);
	}
}
