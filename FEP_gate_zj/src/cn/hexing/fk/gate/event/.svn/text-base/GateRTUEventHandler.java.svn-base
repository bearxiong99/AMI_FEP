/**
 * 功能概述：
 * 		侦听收到终端上行报文、下行报文发送成功事件。
 *    终端上行报文进入MessageQueue队列，以便发送给前置机；
 *    终端下行报文成功事件，简单打印日志。无进一步处理需求。
 * 技术实现：
 * SimpleEventHandler派生类。
 * override handleEvent方法，针对ReceiveMessageEvent和SendMessageEvent特别处理。
 * 注意事项：在spring配置文件中，source对象必须是网关终端接口的SocketServer对象。
 */
package cn.hexing.fk.gate.event;

import org.apache.log4j.Logger;

import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.events.BasicEventHook;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.gate.MessageQueue;
import cn.hexing.fk.gate.PrefixRtuManage;
import cn.hexing.fk.gate.RTUChannelManager;
import cn.hexing.fk.gate.event.autoreply.AutoReply;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.MessageType;
import cn.hexing.fk.message.gate.GateHead;
import cn.hexing.fk.message.gate.MessageGate;
import cn.hexing.fk.message.gw.MessageGw;
import cn.hexing.fk.message.zj.MessageZj;
import cn.hexing.fk.sockserver.event.ClientCloseEvent;
import cn.hexing.fk.sockserver.event.ReceiveMessageEvent;
import cn.hexing.fk.utils.HexDump;

import com.hx.ansi.message.AnsiMessage;
import com.hx.ansi.message.AnsiMessageElement;
import com.hx.dlms.message.DlmsMessage;

/**
 */
public class GateRTUEventHandler extends BasicEventHook {
	private static final Logger log = Logger.getLogger(GateRTUEventHandler.class);
	private int seq = 0;
	private MessageQueue queue;

	public void setQueue(MessageQueue queue) {
		this.queue = queue;
	}

	/**
	 * 重载该方法。
	 */
	public void handleEvent(IEvent e) {
		/** 网关终端服务收到报文，必须尽快发送给前置机。由于异步原因，不能直接调用
		 *  网关前置机服务Accept的client发送。需要放到前置机上行队列。
		 *  	1）当前置机连接到网关，通知上行队列发送上行报文；
		 *  	2）当前置机对应client成功发送上行报文，通知上行队列继续发送；
		 */
		if( e.getType() == EventType.MSG_RECV ){
			//浙江规约报文入队列
			long n1 = System.currentTimeMillis();
			ReceiveMessageEvent evt = (ReceiveMessageEvent)e;
			IMessage msg = e.getMessage();
			if( msg.getMessageType() == MessageType.MSG_ZJ ){
				MessageZj zjmsg = (MessageZj)msg;
				RTUChannelManager.setClientChannel(zjmsg.getLogicalAddress(), evt.getClient());
			}
			else if( msg.getMessageType() == MessageType.MSG_GW_10 ){
				MessageGw gwmsg = (MessageGw)msg;
				RTUChannelManager.setClientChannel(gwmsg.getLogicalAddress(), evt.getClient());
			}
			else if ( msg.getMessageType() == MessageType.MSG_DLMS ){
				DlmsMessage dmsg = (DlmsMessage)msg;
				if( msg.isHeartbeat() ){
					byte[] buf = dmsg.getApdu().array();
					int begin = 2, end = buf[1];
					while(buf[begin] == 0  )
						begin++;
					while(buf[end] == 0)
						end--;
					for(int i = begin ; i < end;i++){
						if(buf[i]<48 || buf[i]>57){
							buf[i] = 48;
						}
					}
					String meterId = new String(buf,begin,end-begin+2);
					dmsg.setLogicalAddress(meterId);
					RTUChannelManager.setClientChannel(dmsg.getLogicalAddress(), evt.getClient());
					RTUChannelManager.setDlmsPeerAddr(dmsg.getPeerAddr(), meterId);
				}
				String meterId = RTUChannelManager.getMeterId(dmsg.getPeerAddr());
				dmsg.setLogicalAddress(meterId);
			}
			else if( msg.getMessageType() == MessageType.MSG_ANSI ){
				AnsiMessage  ansiMessage=(AnsiMessage)msg;
				if(ansiMessage.isHeartbeat()){
					String data=HexDump.hexDumpCompact(ansiMessage.getApdu());
					AnsiMessageElement ame=new AnsiMessageElement();
					ame.decodeMessage(data);
					ansiMessage.setLogicalAddress(ame.getMeterAddr());
					RTUChannelManager.setClientChannel(ansiMessage.getLogicalAddress(), evt.getClient());
					RTUChannelManager.setDlmsPeerAddr(ansiMessage.getPeerAddr(), ame.getMeterAddr());
//					String s="";
//					if(ansiMessage.isLogon()){
//						s="600ABE08280681048002FF02";
//					}else{
//						s="600ABE08280681048002FE02";
//					}
				}
				String meterId = RTUChannelManager.getMeterId(ansiMessage.getPeerAddr());
				ansiMessage.setLogicalAddress(meterId);
			}
			else{
				RTUChannelManager.setClientChannel(msg.getLogicalAddress(), evt.getClient());
				RTUChannelManager.setDlmsPeerAddr(msg.getPeerAddr(), msg.getLogicalAddress());
			}
			long n2 = System.currentTimeMillis();
			if( n2-n1> 15 )
				log.warn("RTUChannelManager.addClient处理>N毫秒,time="+(n2-n1));
			//测试报文不上送
			boolean isTest = false;
			if( msg.getMessageType() == MessageType.MSG_GW_10 ){
				MessageGw gwmsg = (MessageGw)msg;
				if( (0xFF & gwmsg.afn()) == 0xEE ){
					isTest = true;
				}
			}
			if( ! isTest ){
//				if ( msg.getMessageType() == MessageType.MSG_DLMS ){
//					queue.offerUpMessageInQueue(msg);
//				}
				queue.offerUpMessageInQueue(msg);
			}
			long n3 = System.currentTimeMillis();
			if( n3-n2> 80 )
				log.warn("offer.UpMessageInQueue处理>N毫秒,time="+(n3-n2));
			
			//测试：生成自动应答，原消息返回。
			IMessage rep = AutoReply.reply(e.getMessage());
			long n4 = System.currentTimeMillis();
			if( n4-n3>15 )
				log.warn("AutoReply.reply处理>N毫秒, time="+(n4-n3));
			if( null != rep ){
				try{
					if( rep.getMessageType() == MessageType.MSG_ZJ ){
						//本地配置文件的高科终端下行需要增加前导字符
						MessageZj zjmsg = (MessageZj)rep;
						zjmsg.setPrefix(PrefixRtuManage.getInstance().getRtuPrefix(zjmsg.head.rtua));
					}
					else if( rep.getMessageType() == MessageType.MSG_DLMS ){
						rep.setPeerAddr(e.getMessage().getPeerAddr());
						rep.setLogicalAddress(e.getMessage().getLogicalAddress());
					}
					else if( rep.getMessageType() == MessageType.MSG_ANSI ){
						rep.setPeerAddr(e.getMessage().getPeerAddr());
						rep.setLogicalAddress(e.getMessage().getLogicalAddress());
					}
					queue.sendDownMessage(rep);
				}catch(Exception exp){
					log.warn(exp.getLocalizedMessage(),exp);
				}
				//queue.sendDownMessage(rep);
				seq++;
				if( log.isDebugEnabled())
					log.debug("send msg="+seq+" msg="+rep);
			}
			long n5 = System.currentTimeMillis();
			if( n5-n3> 80 )
				log.warn("queue.sendDownMessage处理>N毫秒，＝"+(n5-n3));
		}
		else if( e.getType() == EventType.CLIENTCLOSE ){
			ClientCloseEvent cce = (ClientCloseEvent)e;
			String logicalAddr = RTUChannelManager.removeClient(cce.getClient());
			if( null != logicalAddr ){
				MessageGate gate = new MessageGate();
				gate.getHead().setCommand(MessageGate.CMD_RTU_CLOSE);
				gate.getHead().setAttribute(GateHead.ATT_LOGICALADDR, logicalAddr);
				gate.getHead().setAttribute(GateHead.ATT_SRCADDR, cce.getClient().getPeerAddr());
				queue.offerUpMessageInQueue(gate);
				if(log.isInfoEnabled())
					log.info("Client close:"+logicalAddr);
			}
		}
		super.handleEvent(e);
	}

}
