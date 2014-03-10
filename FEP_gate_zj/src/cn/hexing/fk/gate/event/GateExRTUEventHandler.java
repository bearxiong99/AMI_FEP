package cn.hexing.fk.gate.event;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.events.BasicEventHook;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.gate.MessageQueue;
import cn.hexing.fk.gate.RTUExChannelManager;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.MessageType;
import cn.hexing.fk.message.msgbytes.MessageBytes;
import cn.hexing.fk.sockserver.AsyncExHdlcDlmsSocketClient;
import cn.hexing.fk.sockserver.AsyncExHdlcDlmsSocketClient.CLIENTSTATUS;
import cn.hexing.fk.sockserver.event.AcceptEvent;
import cn.hexing.fk.sockserver.event.ClientCloseEvent;
import cn.hexing.fk.sockserver.event.ReceiveMessageEvent;
import cn.hexing.fk.sockserver.event.SendMessageEvent;
import cn.hexing.fk.utils.HexDump;

import com.hx.dlms.ASN1BitString;
import com.hx.dlms.ASN1Type;
import com.hx.dlms.DecodeStream;
import com.hx.dlms.DlmsAssistant;
import com.hx.dlms.DlmsData;
import com.hx.dlms.aa.AareApdu;
import com.hx.dlms.aa.AarqApdu.CipherMechanism;
import com.hx.dlms.applayer.get.GetResponse;
import com.hx.dlms.applayer.get.GetResponseNormal;
import com.hx.dlms.cipher.Gcm128SoftCipher;
import com.hx.dlms.cipher.IDlmsCipher;
import com.hx.dlms.message.DlmsHDLCMessage;
import com.hx.dlms.message.DlmsMessage;

/***
 * 
 * @author gaoll
 *
 * @time 2013-3-13 下午7:40:54
 *
 * @info 外置模块的终端接入事件处理
 * 这种模式，其实就是通过485与主站通信，几种模式需要设置：
 * 1. don't need data link
 * 2. don't check frame num
 * 3. without even check
 */
public class GateExRTUEventHandler extends BasicEventHook {
	private static final Logger log = Logger.getLogger(GateExRTUEventHandler.class);
	private MessageQueue queue;
	private final byte[] msSysTitle = new byte[]{ 0x48, 0x58, 0x45, 0x11, 0, 0, 0, 0 };

	private IDlmsCipher cipher = Gcm128SoftCipher.getInstance();
	public void setQueue(MessageQueue queue) {
		this.queue = queue;
	}

	/**
	 * 重载该方法。
	 */
	public void handleEvent(IEvent e) {
		
		if(e.getType() == EventType.MSG_RECV){
			try {
				onReceive(e);
			} catch (IOException e1) {
				log.error(e1.getMessage());
			}
		}else if(e.getType() == EventType.ACCEPTCLIENT){
			AcceptEvent event = (AcceptEvent) e;
			AsyncExHdlcDlmsSocketClient client=(AsyncExHdlcDlmsSocketClient) event.getClient();
			if(!client.autoHeartBeat){ //如果模块带有心跳，那么 等待心跳上送，如果没有心跳,直接发送hdlc连接
				client.sendHdlcLink();				
			}
		}else if(e.getType() == EventType.MSG_SENT){
			SendMessageEvent event = (SendMessageEvent) e;
			AsyncExHdlcDlmsSocketClient client=(AsyncExHdlcDlmsSocketClient) event.getClient();
			if(client.status == CLIENTSTATUS.READY){
				client.status = CLIENTSTATUS.BUSY;
			}
		}else if(e.getType() == EventType.CLIENTCLOSE){
			ClientCloseEvent event = (ClientCloseEvent) e;
			AsyncExHdlcDlmsSocketClient client=(AsyncExHdlcDlmsSocketClient) event.getClient();
			client.removeTimeScheduler();
			RTUExChannelManager.removeClient(client);
		}
		super.handleEvent(e);
	}

	private void onReceive(IEvent e) throws IOException {
		ReceiveMessageEvent event = (ReceiveMessageEvent) e;
		AsyncExHdlcDlmsSocketClient client=(AsyncExHdlcDlmsSocketClient) event.getClient();
		client.lastCommunicateTime = System.currentTimeMillis();
		IMessage msg = e.getMessage();
		if(msg.getMessageType()==MessageType.MSG_DLMS_HDLC || msg.getMessageType()==MessageType.MSG_DLMS){
			
			if(msg.getMessageType()==MessageType.MSG_DLMS){
				DlmsMessage dlmsMsg = (DlmsMessage) msg;
				if(!dlmsMsg.isHeartbeat())
					throw new RuntimeException("this frame is not hdlc.");
				else{
					DlmsMessage dmsg = (DlmsMessage)msg;
					if( msg.isHeartbeat() ){
						byte[] buf = dmsg.getApdu().array();
						int begin = 2, end = buf.length-1;
						while(buf[begin] == 0  )
							begin++;
						while(buf[end] == 0)
							end--;
						for(int i = begin ; i < end;i++){
							if(buf[i]<48 || buf[i]>57){
								buf[i] = 48;
							}
						}
						String meterId = new String(buf,begin,end-begin+1);
						dmsg.setLogicalAddress(meterId);
						RTUExChannelManager.setClientChannel(dmsg.getLogicalAddress(), event.getClient());
						RTUExChannelManager.setDlmsPeerAddr(dmsg.getPeerAddr(), meterId);
					}
				}
			}else{
				DlmsHDLCMessage nextMsg = new DlmsHDLCMessage();
				DlmsHDLCMessage hdlcMessage = (DlmsHDLCMessage) msg;
				if(hdlcMessage.moduleHeartBeart){
					ByteBuffer apdu = hdlcMessage.getApdu();
					String strApdu=HexDump.toHex(apdu.array());
					strApdu=strApdu.replaceFirst("DD", "DA");
					strApdu = "7e0001001000010012"+strApdu;
					MessageBytes mb = new MessageBytes();
					mb.setData(HexDump.toArray(strApdu));
					client.send(mb);
					//只有第一次发送hdlc连接,其余的不发送hdlc连接
					if(client.status == CLIENTSTATUS.NULL)
						client.sendHdlcLink();
				}else{
					client.lastCommunicateTime = System.currentTimeMillis();
					if(client.status == CLIENTSTATUS.HDLC_LINK){
						//发送认证连接
						client.status = CLIENTSTATUS.APP_LINK;
						byte[] apdu = DlmsAssistant.getInstance().createAarqApdu(client.aaMechanism, client.nextFrameCounter(), client.context, msSysTitle);
						nextMsg.setApdu(apdu);
						nextMsg.setControlField((byte) 0x10);
						client.send(nextMsg);
					}else if(client.status == CLIENTSTATUS.APP_LINK){
						byte[] apdu = handleAARE(hdlcMessage.getApdu().array(), client);
						nextMsg.setApdu(apdu);
						nextMsg.setControlField((byte) 0x32);
						client.send(nextMsg);
					}else if(client.status == CLIENTSTATUS.SEND_STOC){
						client.readMeterNo();
					}else if(client.status == CLIENTSTATUS.READ_NUM){
						byte[] apdu = hdlcMessage.getApdu().array();
						if(apdu[0]==(byte)0xD8){
							client.sendHdlcLink();
							return;
						}
						//获得表号,组成心跳,发送给FE.设置状态为ready
						GetResponse resp = new GetResponse();
						resp.decode(DecodeStream.wrap(apdu));
						ASN1Type selObj = resp.getDecodedObject();
						if( selObj instanceof GetResponseNormal){
							ASN1Type result = ((GetResponseNormal) selObj).getResult().getDecodedObject();
							if(result instanceof DlmsData	){
								String meterId=((DlmsData) result).getVisiableString();
								meterId = "000000000000".substring(meterId.length())+meterId;
								DlmsMessage dm = new DlmsMessage();
								dm.setLogicalAddress(meterId);
								dm.setPeerAddr(client.getPeerAddr());
								dm.setTxfs("02");
								dm.setApdu(HexDump.cat(HexDump.toArray("DD1000000000"), meterId.getBytes()));
								client.status = CLIENTSTATUS.READY;
								client.logicAddress = meterId;
								RTUExChannelManager.setClientChannel(meterId, event.getClient());
								RTUExChannelManager.setDlmsPeerAddr(client.getPeerAddr(), meterId);
								queue.offerUpMessageInQueue(dm);
							}else{
								log.error("read meter fail");
							}
						}else{
							//返回数据不是正确的
							log.error("read meter fail");
						}
					}else if(client.status == CLIENTSTATUS.BUSY){
						client.status = CLIENTSTATUS.READY;
						DlmsMessage dm = new DlmsMessage();
						if(hdlcMessage.getApdu()==null && client.acceptAARE!=null){
							dm.setApdu(client.acceptAARE);
						}else if(hdlcMessage.getApdu()!=null){
							if(hdlcMessage.getApdu().array()[0]==(byte)0xD8){
								client.sendHdlcLink();
								return ;
							}
							dm.setApdu(hdlcMessage.getApdu());						
						}
						dm.setTxfs("02");
						dm.setPeerAddr(client.getPeerAddr());
						dm.setLogicalAddress(client.logicAddress);
						queue.offerUpMessageInQueue(dm);
					}
				}
				
				
			}
		}
	}
	private byte[] handleAARE(byte[] apdu,AsyncExHdlcDlmsSocketClient client) throws IOException{
		AareApdu aare = new AareApdu();
		aare.decode(DecodeStream.wrap(apdu));
		if( aare.getResultValue() == 0 ){
			client.acceptAARE = apdu;
			if( client.aaMechanism == CipherMechanism.HLS_GMAC ){
				client.meterSysTitle = aare.getRespApTitle();
				if( null == client.meterSysTitle ){
					return null;
				}
				
				byte[] cipheredUserInfo = aare.getUserInformation();
				byte[] cInitResp = new byte[cipheredUserInfo.length-7];  //Ciphered Initiate response
				for(int i=0; i<cInitResp.length; i++)
					cInitResp[i] = cipheredUserInfo[i+7];
				byte[] pInitResp = cipher.decrypt(client.context, cInitResp, DlmsAssistant.getInstance().makeInitVector(client.authenticationValue, 3,client.meterSysTitle) );
				aare.setDecryptedUserInfo(pInitResp);
				client.updateAare(aare);
				client.status=CLIENTSTATUS.SEND_STOC;
				return DlmsAssistant.getInstance().createStoC(client.aaMechanism,client.authenticationValue,client.nextFrameCounter(),client.context);
			}
			else if( client.aaMechanism == CipherMechanism.HLS_2 ){
				client.updateAare(aare);
				ASN1BitString conformance = new ASN1BitString();
				conformance.setInitValue(new byte[]{ (byte)0x00,(byte)0x50,(byte)0x1F});
				client.status=CLIENTSTATUS.SEND_STOC;
				return DlmsAssistant.getInstance().createStoC(client.aaMechanism,client.authenticationValue,client.nextFrameCounter(),client.context);
			}
		}
		
		if( client.aaMechanism == CipherMechanism.HLS_2 ){
			client.aaMechanism = CipherMechanism.HLS_GMAC;
		}else{
			throw new RuntimeException("HLS_2 And HLS_GMAC Can't Applink");
		}
		client.status=CLIENTSTATUS.APP_LINK;
		return DlmsAssistant.getInstance().createAarqApdu(client.aaMechanism,client.nextFrameCounter(),client.context,msSysTitle);
	}
}
