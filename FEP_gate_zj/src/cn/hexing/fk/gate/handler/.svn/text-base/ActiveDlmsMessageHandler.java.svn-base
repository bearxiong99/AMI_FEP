package cn.hexing.fk.gate.handler;

import java.io.IOException;

import org.apache.log4j.Logger;

import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.gate.RtuServerChannelMannager;
import cn.hexing.fk.gate.client.DlmsTerminalClient;
import cn.hexing.fk.gate.client.DlmsTerminalClient.STATUS;
import cn.hexing.fk.sockclient.JSocket;
import cn.hexing.fk.sockserver.event.ReceiveMessageEvent;
import cn.hexing.fk.utils.StringUtil;

import com.hx.dlms.ASN1BitString;
import com.hx.dlms.DecodeStream;
import com.hx.dlms.DlmsAssistant;
import com.hx.dlms.aa.AareApdu;
import com.hx.dlms.aa.AarqApdu.CipherMechanism;
import com.hx.dlms.cipher.Gcm128SoftCipher;
import com.hx.dlms.cipher.IDlmsCipher;
import com.hx.dlms.message.DlmsHDLCMessage;
import com.hx.dlms.message.DlmsMessage;

/**
 * 
 * @author gaoll
 *
 * @time 2013-3-15 下午12:31:22
 *
 * @info 主站作为客户端，终端作为服务端DLMS消息处理
 */
public class ActiveDlmsMessageHandler extends ActiveMessageHandler{
	
	private static final Logger log = Logger.getLogger(ActiveDlmsMessageHandler.class);

	
	private final byte[] msSysTitle = new byte[]{ 0x48, 0x58, 0x45, 0x11, 0, 0, 0, 0 };

	private IDlmsCipher cipher = Gcm128SoftCipher.getInstance();
	
	@Override
	public void handleEvent(IEvent event) {
		super.handleEvent(event);
		
		if(event.getType()==EventType.MSG_RECV){
			try {
				onReceive(event);
			} catch (IOException e) {
				log.error(StringUtil.getExceptionDetailInfo(e));
			}
		}
		
	}

	private void onReceive(IEvent event) throws IOException {
		ReceiveMessageEvent recvEvent= (ReceiveMessageEvent) event;
		JSocket jclient=(JSocket) recvEvent.getClient();
		DlmsHDLCMessage hdlcMessage = (DlmsHDLCMessage) recvEvent.getMessage();
		DlmsTerminalClient client=(DlmsTerminalClient) RtuServerChannelMannager.getClient(jclient.getPeerAddr());
		DlmsHDLCMessage nextMsg = new DlmsHDLCMessage();
		if(STATUS.HDLC_LINK==client.status && hdlcMessage.getControlField()==(byte)0x73){
			client.status = STATUS.APP_LINK;
			byte[] apdu = DlmsAssistant.getInstance().createAarqApdu(client.aaMechanism, client.nextFrameCounter(), client.context, msSysTitle);
			nextMsg.setApdu(apdu);
			nextMsg.setControlField((byte) 0x10);
			client.sendMessage(nextMsg);
		}else if(STATUS.APP_LINK==client.status){
			byte[] apdu = handleAARE(hdlcMessage.getApdu().array(), client);
			nextMsg.setApdu(apdu);
			nextMsg.setControlField((byte)0x23);
			client.sendMessage(nextMsg);
		}else if(STATUS.SEND_STOC==client.status){
			client.status = STATUS.APP_OK;
			nextMsg.setApdu(client.realMessage.getApdu());
			nextMsg.setControlField((byte)0x34);
			client.sendMessage(nextMsg);
		}else if(STATUS.APP_OK==client.status){
			DlmsMessage dm  =new DlmsMessage();
			dm.setApdu(hdlcMessage.getApdu());
			dm.setLogicalAddress(RtuServerChannelMannager.getMeterId(jclient.getPeerAddr()));
			dm.setPeerAddr(jclient.getPeerAddr());
			dm.setTxfs("02");
			queue.offerUpMessageInQueue(dm);
			
		}
	}
	
	private byte[] handleAARE(byte[] apdu,DlmsTerminalClient client) throws IOException{
		AareApdu aare = new AareApdu();
		aare.decode(DecodeStream.wrap(apdu));
		if( aare.getResultValue() == 0 ){
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
				client.status=STATUS.SEND_STOC;
				return DlmsAssistant.getInstance().createStoC(client.aaMechanism,client.authenticationValue,client.nextFrameCounter(),client.context);
			}
			else if( client.aaMechanism == CipherMechanism.HLS_2 ){
				client.updateAare(aare);
				ASN1BitString conformance = new ASN1BitString();
				conformance.setInitValue(new byte[]{ (byte)0x00,(byte)0x50,(byte)0x1F});
				client.status=STATUS.SEND_STOC;
				return DlmsAssistant.getInstance().createStoC(client.aaMechanism,client.authenticationValue,client.nextFrameCounter(),client.context);
			}
		}
		
		if( client.aaMechanism == CipherMechanism.HLS_2 ){
			client.aaMechanism = CipherMechanism.HLS_GMAC;
		}else{
			throw new RuntimeException("HLS_2 And HLS_GMAC Can't Applink");
		}
		client.status=STATUS.APP_LINK;
		return DlmsAssistant.getInstance().createAarqApdu(client.aaMechanism,client.nextFrameCounter(),client.context,msSysTitle);
	}
}
