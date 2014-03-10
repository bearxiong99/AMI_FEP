/**
 * 收到客户端报文时，需要确定是否自动应答。
 * 根据报文类型，生产自动应答报文。
 */
package cn.hexing.fk.gate.event.autoreply;

import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.MessageType;
import cn.hexing.fk.message.bengal.BengalMessage;
import cn.hexing.fk.message.gw.MessageGw;
import cn.hexing.fk.message.zj.MessageZj;
import cn.hexing.fk.utils.HexDump;

import com.hx.ansi.message.AnsiMessage;
import com.hx.dlms.message.DlmsMessage;

/**
 */
public class AutoReply {
	
	public static final IMessage reply(IMessage msg){
		if( msg instanceof MessageZj )
			return AutoReplyMessageZj.reply((MessageZj)msg);
		else if( msg instanceof MessageGw ){
			MessageGw gwmsg = (MessageGw)msg;
			MessageGw rep = null;
			if( (0xFF & gwmsg.afn()) == 0xEE ){
				rep = gwmsg;
			}
			else
				rep = gwmsg.createConfirm(); 
			return rep;
		}
		else if( msg.getMessageType() == MessageType.MSG_DLMS ){
			DlmsMessage dlmsgsg = (DlmsMessage)msg;
			if ( dlmsgsg.isHeartbeat() ){//心跳
				DlmsMessage rep =DlmsMessage.createHeartReply();
				rep.setDstAddr(dlmsgsg.getSrcAddr());
				return rep;
			}else if(dlmsgsg.isEventNeedReply()){//表箱事件需要回复
				DlmsMessage rep = DlmsMessage.createEventReply();
				rep.setDstAddr(dlmsgsg.getSrcAddr());
				return rep;
			}		
		}else if( msg.getMessageType() == MessageType.MSG_BENGAL){
			BengalMessage bmmsg =(BengalMessage) msg;
			BengalMessage confirm = bmmsg.createConfirm();
			return confirm;
		}else if( msg.getMessageType() == MessageType.MSG_ANSI){
			AnsiMessage  ansiMessage=(AnsiMessage)msg;
			if(ansiMessage.isHeartbeat()){
				String s="";
				AnsiMessage amsg=new AnsiMessage();
				if(ansiMessage.isLogon()){
					s="600ABE08280681048002FF02";
				}else{
					s="600ABE08280681048002FE02";
				}
				amsg.setApdu(HexDump.toArray(s));
				return amsg;
			}
		}
		return null;
	}
}
