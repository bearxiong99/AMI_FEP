package cn.hexing.fk.gate.event;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.events.BasicEventHook;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.exception.MessageParseException;
import cn.hexing.fk.gate.CSDMessageQueue;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.MessageType;
import cn.hexing.fk.message.gate.MessageGate;
import cn.hexing.fk.message.gw.MessageGw;
import cn.hexing.fk.message.msgbytes.MessageBytes;
import cn.hexing.fk.sockclient.csd.CSDSyncTcpSocketClient;
import cn.hexing.fk.sockclient.csd.CSDSyncTcpSocketClient.CSD_CLIENT_STATUS;
import cn.hexing.fk.sockserver.event.AcceptEvent;
import cn.hexing.fk.sockserver.event.ClientCloseEvent;
import cn.hexing.fk.sockserver.event.ReceiveMessageEvent;
import cn.hexing.fk.sockserver.event.SendMessageEvent;
import cn.hexing.fk.utils.HexDump;

import com.hx.dlms.ASN1BitString;
import com.hx.dlms.ASN1Type;
import com.hx.dlms.DecodeStream;
import com.hx.dlms.DlmsAssistant;
import com.hx.dlms.DlmsAssistant.OP_TYPE;
import com.hx.dlms.aa.AareApdu;
import com.hx.dlms.aa.AarqApdu.CipherMechanism;
import com.hx.dlms.applayer.get.GetRequest;
import com.hx.dlms.applayer.get.GetRequestNext;
import com.hx.dlms.applayer.get.GetResponse;
import com.hx.dlms.applayer.get.GetResponseNormal;
import com.hx.dlms.applayer.get.GetResponseWithBlock;
import com.hx.dlms.cipher.Gcm128SoftCipher;
import com.hx.dlms.cipher.IDlmsCipher;
import com.hx.dlms.message.DlmsHDLCMessage;
import com.hx.dlms.message.DlmsMessage;

/**
 * 
 * @author gaoll
 *
 * @time 2013-3-21 下午06:43:20
 *
 * @info CSD Server Event Handler.
 * include handle get,set,action etc operation.
 */
public class CSDServerEventHandler extends BasicEventHook{
	private final byte[] msSysTitle = new byte[]{ 0x48, 0x58, 0x45, 0x11, 0, 0, 0, 0 };
	
	private static final Logger log = Logger.getLogger(CSDServerEventHandler.class);	

	private IDlmsCipher cipher = Gcm128SoftCipher.getInstance();
	
	@Override
	public void handleEvent(IEvent event) {
		
		if(event.getType()==EventType.ACCEPTCLIENT){
			AcceptEvent acceptEvent = (AcceptEvent) event;
			CSDMessageQueue.getInstance().onCSDClientConnected((CSDSyncTcpSocketClient) acceptEvent.getClient());
		}else if(event.getType()==EventType.CLIENTCLOSE){
			ClientCloseEvent closeEvent = (ClientCloseEvent)event;
			CSDMessageQueue.getInstance().onCSDClientClose((CSDSyncTcpSocketClient) closeEvent.getClient());
		}else if(event.getType()==EventType.MSG_RECV){
			ReceiveMessageEvent recvEvent = (ReceiveMessageEvent)event;
			CSDSyncTcpSocketClient csdClient=(CSDSyncTcpSocketClient) recvEvent.getClient();
			csdClient.lastSendTime = System.currentTimeMillis();
			try {
				onReceive(event);
			} catch (Exception e) {
				csdClient.channgeCommandMode();
				log.error("phoneNum:"+csdClient.phoneNum+" catch exception,hang up.",e);
			}
		}else if(event.getType()==EventType.MSG_SENT){
			SendMessageEvent sendEvent = (SendMessageEvent)event;
			((CSDSyncTcpSocketClient)sendEvent.getClient()).lastSendTime=System.currentTimeMillis();
		}
		super.handleEvent(event);
		
	}

	private void onReceive(IEvent event) throws Exception {
		ReceiveMessageEvent recvEvent = (ReceiveMessageEvent) event;
		if(!(recvEvent.getMessage() instanceof MessageBytes)){
			throw new RuntimeException("Not MessageBytes");
		}
		MessageBytes recvMsg=(MessageBytes) recvEvent.getMessage();
		CSDSyncTcpSocketClient csdClient=(CSDSyncTcpSocketClient) recvEvent.getClient();
		IMessage realMsg = csdClient.getRealMessage();
		
		if(null==realMsg) return;
		
		MessageGate gateMsg = (MessageGate) realMsg;
		MessageType type = gateMsg.getInnerMessage().getMessageType();
		
		String logPrefix="phoneNum "+csdClient.phoneNum;
		CSD_CLIENT_STATUS status = csdClient.getStatus();
		MessageBytes nextMsg = new MessageBytes();
		String strRecvMsg = HexDump.toHex(recvMsg.data);
		boolean recvFinish=true;
		switch(status){
		case DIAL:{
			log.debug(logPrefix+" dial return,msg:"+strRecvMsg); 
			if(csdClient.curMsg==null){
				csdClient.curMsg = recvMsg.data;
			}else{
				csdClient.curMsg=HexDump.cat(csdClient.curMsg, recvMsg.data);
			}
			String allMsg = HexDump.toHex(csdClient.curMsg);
			if(allMsg.contains(HexDump.toHex("CONNECT 9600".getBytes()))){
				if(type==MessageType.MSG_DLMS){
					nextMsg.setData(HexDump.toArray("7EA0230002FEFF0393E4B0818014050207D0060207D00704000000010804000000013AF27E"));
					csdClient.setStatus(CSD_CLIENT_STATUS.HDLC_LINK);
					log.debug(logPrefix+" hdlc link send:"+nextMsg);					
				}else if(type==MessageType.MSG_GW_10){
					MessageGw gw = (MessageGw) gateMsg.getInnerMessage();
					nextMsg.setData(gw.getRawPacket());
					csdClient.setStatus(CSD_CLIENT_STATUS.BUSY);
				}
				csdClient.curMsg=null;
			}else if(allMsg.contains(HexDump.toHex("+CREG: ".getBytes()))){
				csdClient.dial(csdClient.phoneNum);
				log.debug(logPrefix+" call ready ,dial again.");
				csdClient.curMsg=null;
			}else if(allMsg.contains(HexDump.toHex("NO CARRIER".getBytes()))){
				csdClient.onHangUp();
				CSDMessageQueue.getInstance().onCSDClientIdle(csdClient);
			}
			break;
		}
		case HDLC_LINK:{
			//回来的第一帧肯定是7EA0LL的格式
			log.debug(logPrefix+" hdlc link reutrn "+recvMsg);
			recvFinish=checkRecvFinished(recvMsg, csdClient);
			if(!recvFinish) return;
			byte[] allFrame = csdClient.curMsg;
			csdClient.curMsg=null;
			csdClient.curFrameLength = 0;
			log.debug(logPrefix+" hdlc link reutrn finish"+HexDump.toHex(allFrame));
			byte[] apdu = DlmsAssistant.getInstance().createAarqApdu(csdClient.aaMechanism,csdClient.nextFrameCounter(),csdClient.context,msSysTitle);
			apdu=wrapHDLCFrame(apdu, csdClient);
			nextMsg.setData(apdu);
			log.debug(logPrefix+" send app link "+nextMsg);
			csdClient.setStatus(CSD_CLIENT_STATUS.APP_LINK);
			break;
		}
		case APP_LINK:{
			log.debug(logPrefix+" app link recvMsg:"+recvMsg);
			recvFinish=checkRecvFinished(recvMsg, csdClient);
			if(!recvFinish) return;
			byte[] allFrame = csdClient.curMsg;
			csdClient.curMsg=null;
			csdClient.curFrameLength = 0;
			log.debug(logPrefix+" app link recv finish:"+HexDump.toHex(allFrame));
			byte[] unWrapHdlcData=unWrapHdlcFrame(allFrame,csdClient);
			byte[] apdu=handleAARE(unWrapHdlcData,csdClient);
			apdu=wrapHDLCFrame(apdu,csdClient);
			nextMsg.setData(apdu);
			break;
		}
		case SEND_STOC:{
			recvFinish=checkRecvFinished(recvMsg, csdClient);
			log.debug(logPrefix+" StoC recvice:"+recvMsg);
			if(!recvFinish) return;
			byte[] allFrame = csdClient.curMsg;
			csdClient.curMsg=null;
			csdClient.curFrameLength = 0;
			byte[] unWrapHdlcData=unWrapHdlcFrame(allFrame,csdClient);
			log.debug(logPrefix+" StoC recv finish:"+HexDump.toHex(allFrame));
			byte[] decipherApdu=getDecipherApdu(unWrapHdlcData,csdClient);
			log.debug(logPrefix+" After decipher:"+HexDump.toHex(decipherApdu));
			DlmsMessage dlmsMsg=(DlmsMessage) gateMsg.getInnerMessage();
			byte[] warpData = wrapHDLCFrame(dlmsMsg.getApdu().array(), csdClient);
			nextMsg.setData(warpData);
			csdClient.setStatus(CSD_CLIENT_STATUS.BUSY);
			log.debug(logPrefix+" real msg send:"+nextMsg);
			break;
		}
		case BUSY:{
			recvFinish=checkRecvFinished(recvMsg, csdClient);
			log.debug(logPrefix+" recv msg "+recvMsg);
			if(!recvFinish) return;
			byte[] allFrame = csdClient.curMsg;
			csdClient.curMsg=null;
			csdClient.curFrameLength = 0;
			log.debug(logPrefix+" recv msg finish "+HexDump.toHex(allFrame));
			if(type==MessageType.MSG_DLMS){
				byte[] unWrapHdlcData=unWrapHdlcFrame(allFrame,csdClient);
				byte[] apdu=handleUpMessage(unWrapHdlcData,csdClient);
				if(apdu==null) return ;
				apdu=wrapHDLCFrame(apdu, csdClient);
				nextMsg.setData(apdu);				
			}else if(type==MessageType.MSG_GW_10){
				communicateFinish(allFrame, csdClient);
			}
			break;
		}
		case DISC_HDLC_LINK:
			recvFinish=checkRecvFinished(recvMsg, csdClient);
			log.debug(logPrefix+" disc hdlc msg "+recvMsg);
			if(!recvFinish) return;
			csdClient.curMsg = null;
			csdClient.curFrameLength = 0;
			log.debug(logPrefix+" send channgeMode");
			csdClient.channgeCommandMode();
			break;
		case CHANNGE_COMMAND:
			log.debug(logPrefix+" recv channge command :"+recvMsg);
			csdClient.hangUp();
			break;
		case HANG_UP:{
			//到达这里说明返回了挂断命令请求，要判断是否挂断成功
			log.debug(logPrefix+" recv hang up :"+recvMsg);
			csdClient.onHangUp();
			CSDMessageQueue.getInstance().onCSDClientIdle(csdClient);
			break;
		}
		}
		if(nextMsg.data.length!=0)
			csdClient.send(nextMsg);
	}

	private boolean checkRecvFinished(MessageBytes recvMsg,
			CSDSyncTcpSocketClient csdClient) throws MessageParseException {
		boolean recvFinish = false;
		MessageGate gateMessage=(MessageGate) csdClient.getRealMessage();
		MessageType type = gateMessage.getInnerMessage().getMessageType();
		if(csdClient.curMsg==null){
			if(type==MessageType.MSG_DLMS){
				if(recvMsg.data.length>3){
					csdClient.curFrameLength=recvMsg.data[2]&0xFF;
					//判断是否接收完毕
					if(recvMsg.data.length==csdClient.curFrameLength+2){
						//接收完毕
						recvFinish = true;
					}else{
						recvFinish = false;
					}
				}				
			}else if(type==MessageType.MSG_GW_10){
				MessageGw gwMsg = new MessageGw();
				recvFinish=gwMsg.read(ByteBuffer.wrap(recvMsg.data));
			}
			csdClient.curMsg = recvMsg.data;
		}else{
			csdClient.curMsg=HexDump.cat(csdClient.curMsg, recvMsg.data);
			if(csdClient.curFrameLength==0){
				if(csdClient.curMsg.length>3){
					csdClient.curFrameLength = csdClient.curMsg[2]&0xFF;
				}
			}
			if(type==MessageType.MSG_DLMS){
				if(csdClient.curMsg.length==csdClient.curFrameLength+2){
					recvFinish = true;
				}else{
					recvFinish = false;
				}				
			}else if(type==MessageType.MSG_GW_10){
				MessageGw gwMsg = new MessageGw();
				recvFinish=gwMsg.read(ByteBuffer.wrap(csdClient.curMsg));
			}
		}
		return recvFinish;
	}
	
	private byte[] unWrapHdlcFrame(byte[] rawPacket,CSDSyncTcpSocketClient csdClient) {
		//将HDLC帧剥离
		ByteBuffer apdu = ByteBuffer.wrap(rawPacket);
		int tag = apdu.get();
		int frameType = apdu.get();
		int headLength = 0;
		if((tag!=(byte)0x7E)&& ((frameType!=(byte)0xA0)||(frameType!=(byte)0xA8))){
			throw new RuntimeException("not hdlc frame");
		}
		int len = apdu.get()&0XFF;
		if((len+2)!=rawPacket.length){
			throw new RuntimeException("hdlc frame length is uncorrect");
		}
		headLength = 2;
		
		int destAddr=apdu.get();
		int destAddrCount=1;
		while((destAddr&0x01)==0){
			destAddr = apdu.get();
			destAddrCount++;
		}
		headLength+=destAddrCount;
		int srcAddr = apdu.get();
		int srcAddrCount=1;
		while((srcAddr&0x01)==0){
			srcAddr = apdu.get();
			srcAddrCount++;
		}
		headLength+=srcAddrCount;
		csdClient.controlWord=apdu.get();
		headLength+=1;
		//FCS校验
		headLength+=2;
		headLength+=3;//E6 E7 00
		byte[] result = new byte[len-headLength-2];
		System.arraycopy(rawPacket, 1+headLength, result, 0, result.length);
		return result;
	}

	private byte[] wrapHDLCFrame(byte[] apdu,CSDSyncTcpSocketClient csdClient) throws Exception {
		int controlWord = 0xFE;
		if(csdClient.controlWord==0){
			controlWord = 0x10;
		}else{
			int rrr=(csdClient.controlWord>>5)&0x0F;
			int sss=((csdClient.controlWord&0x0F)>>1)&0x0F;
			if(++sss>7){
				sss=0;
			}
			int needResp = 1;
			controlWord = (0xFE & (rrr<<5)|(needResp<<4))|(sss<<1);
		}
		//7E A0|A8 LL ADDR CC HCS_H HCS_L E6 E6 00 APDU 7E
	
		DlmsHDLCMessage hdlc = new DlmsHDLCMessage();
		hdlc.setControlField((byte)controlWord);
		int type = apdu[0];
		OP_TYPE opType = OP_TYPE.OP_NA;
		switch(type){
		case (byte)0xC0:
			opType = OP_TYPE.OP_GET;
			break;
		case (byte)0xC1:
			opType = OP_TYPE.OP_SET;
			break;
		case (byte)0xC3:
			opType = OP_TYPE.OP_ACTION;
			break;
		}
		
		apdu = DlmsAssistant.getInstance().cipher(opType, apdu, csdClient.context);
		hdlc.setApdu(apdu);
		//7E A0 LL destAddr srcAddr c HCS E6 E6 00 APDU FCS 7E
		//1+1+1+hdlc.getServerAddr().length+hdlc.getClientAddr().length+1+2+3+apdu.length+2+1
		ByteBuffer buffer = ByteBuffer.allocate(12+hdlc.getServerAddr().length+hdlc.getClientAddr().length+apdu.length);

		hdlc.write(buffer);
		buffer.flip();
		return buffer.array();
	}

	private byte[] handleUpMessage(byte[] rawPacket,
			CSDSyncTcpSocketClient csdClient) throws Exception {
		rawPacket=getDecipherApdu(rawPacket, csdClient);
		System.out.println(HexDump.toHex(rawPacket));
		ByteBuffer apdu = ByteBuffer.wrap(rawPacket);
		int applicationTag = apdu.get(0) & 0xFF ;
		switch(applicationTag){
		case 0xC4:
			return handleGetResponse(apdu,csdClient);
		case 0xC5:
			return handleSetResponse(apdu,csdClient);
		case 0xC7:
			return handleActionResponse(apdu,csdClient);
		default:
			csdClient.channgeCommandMode();
			break;
		}
		
		return null;
	}

	private byte[] handleSetResponse(ByteBuffer apdu,
			CSDSyncTcpSocketClient csdClient) throws MessageParseException {
		communicateFinish(apdu.array(),csdClient);
		return null;
	}

	private byte[] handleActionResponse(ByteBuffer apdu,
			CSDSyncTcpSocketClient csdClient) throws MessageParseException {
		communicateFinish(apdu.array(),csdClient);
		return null;
	}

	private byte[] handleGetResponse(ByteBuffer apdu,
			CSDSyncTcpSocketClient csdClient) throws Exception {
		GetResponse resp = new GetResponse();
		resp.decode(DecodeStream.wrap(apdu));
		ASN1Type selObj = resp.getDecodedObject();
		if(selObj instanceof GetResponseNormal){
			communicateFinish(apdu.array(),csdClient);
		}else if(selObj instanceof GetResponseWithBlock){
			//如果是块数据,要再去读
			GetResponseWithBlock repBlock = (GetResponseWithBlock)selObj;
			boolean lastBlock = repBlock.isLastBlock();
			int blockNum = repBlock.getBlockNumber();

			boolean duplicated = false;
			byte[] temp = null;
			int len = 0;
			for(int i=0; i<csdClient.blockReplys.size(); i++){
				GetResponseWithBlock b = (GetResponseWithBlock)csdClient.blockReplys.get(i);
				temp = b.getRawData();
				len += null==temp ? 0 : temp.length ;
				if( b.getBlockNumber() == blockNum ){
					duplicated = true;
					break;
				}
			}
			if(duplicated){
				csdClient.channgeCommandMode();
				return null;
			}
			temp = repBlock.getRawData();
			if( null == temp ){
				throw new RuntimeException("get response failue");
			}else{
				len += temp.length ;
			}
			csdClient.blockReplys.add(repBlock);
			if( !lastBlock ){
				//sendNextBlock
				log.debug("phoneNum:"+csdClient.phoneNum+" read next block,block num is "+blockNum+" duplicated:"+duplicated);
				GetRequestNext next = new GetRequestNext();
				next.setInvokeId(repBlock.getInvokeId());
				next.setPriorityHigh(repBlock.isPriorityHigh());
				next.setBlockNumber(blockNum);
				GetRequest reqGet = new GetRequest(next);
				byte[] nextApdu = reqGet.encode();
				if(csdClient.context.aaMechanism==CipherMechanism.HLS_GMAC){
					nextApdu=DlmsAssistant.getInstance().cipher(OP_TYPE.OP_GET, nextApdu, csdClient.context);
				}
				return nextApdu;
			}
			else{
				//Last block, same as getResponseNormal invoked.
				ByteBuffer buf = ByteBuffer.allocate(len);
				for(int i=0; i<csdClient.blockReplys.size(); i++){
					GetResponseWithBlock b = (GetResponseWithBlock)csdClient.blockReplys.get(i);
					temp = b.getRawData();
					if( null != temp )
						buf.put(temp);
				}
				buf.flip();
				byte[] head=new byte[]{(byte) 0xC4,0x01,(byte) 0x81,0x00};
				byte[] frame = HexDump.cat(head, buf.array());
				communicateFinish(frame,csdClient);
				csdClient.blockReplys.clear();
			}
		}
		return null;
	}

	private void communicateFinish(byte[] apdu,CSDSyncTcpSocketClient csdClient) throws MessageParseException {
		MessageGate gateMsg = (MessageGate) csdClient.getRealMessage();
		MessageType type = gateMsg.getInnerMessage().getMessageType();
		if(type==MessageType.MSG_DLMS){
			DlmsMessage dm = new DlmsMessage();
			dm.setApdu(apdu);
			dm.setLogicalAddress(csdClient.getLocalAddr());
			dm.setPeerAddr("");
			dm.setTxfs("02");
			CSDMessageQueue.getInstance().feQueue.offerUpMessageInQueue(dm);
//			MessageBytes mb = new MessageBytes();
//			mb.setData(HexDump.toArray("7EA00A0002FEFF035352547E"));
//			log.debug("phoneNum:"+csdClient.phoneNum+" DISC HDLC LINK");
//			csdClient.setStatus(CSD_CLIENT_STATUS.DISC_HDLC_LINK);
//			csdClient.send(mb);			
			//将消息上送给FE,并且将当前的client状态设置为PENDING,如果此时再有消息进入,可以直接进行通讯，不再进行拨号
			csdClient.setStatus(CSD_CLIENT_STATUS.PENDING);
		}else if(type==MessageType.MSG_GW_10){
			MessageGw gw = new MessageGw();
			gw.read(ByteBuffer.wrap(apdu));
			gw.setLogicalAddress(csdClient.getLocalAddr());
			gw.setPeerAddr("");
			gw.setTxfs("02");
			CSDMessageQueue.getInstance().feQueue.offerUpMessageInQueue(gw);
			csdClient.channgeCommandMode();

		}
	}

	private byte[] getDecipherApdu(byte[] rawPacket,
			CSDSyncTcpSocketClient csdClient) throws IOException {
		ByteBuffer apdu = ByteBuffer.wrap(rawPacket);
		int applicationTag = apdu.get(0) & 0xFF;
		switch(applicationTag){
		case 0xCF:
		case 0xCC:
		case 0xCD:
			return DlmsAssistant.getInstance().decipher(apdu, csdClient.context,csdClient.meterSysTitle);
		}
		return rawPacket;
	}
	
	private byte[] handleAARE(byte[] apdu,CSDSyncTcpSocketClient csdClient) throws IOException{
		AareApdu aare = new AareApdu();
		aare.decode(DecodeStream.wrap(apdu));
		if( aare.getResultValue() == 0 ){
			if( csdClient.aaMechanism == CipherMechanism.HLS_GMAC ){
				csdClient.meterSysTitle = aare.getRespApTitle();
				if( null == csdClient.meterSysTitle ){
					return null;
				}
				
				byte[] cipheredUserInfo = aare.getUserInformation();
				byte[] cInitResp = new byte[cipheredUserInfo.length-7];  //Ciphered Initiate response
				for(int i=0; i<cInitResp.length; i++)
					cInitResp[i] = cipheredUserInfo[i+7];
				byte[] pInitResp = cipher.decrypt(csdClient.context, cInitResp, csdClient.makeInitVector(cipheredUserInfo, 3) );
				aare.setDecryptedUserInfo(pInitResp);
				csdClient.updateAare(aare);
				csdClient.setStatus(CSD_CLIENT_STATUS.SEND_STOC);
				return DlmsAssistant.getInstance().createStoC(csdClient.aaMechanism,csdClient.authenticationValue,csdClient.nextFrameCounter(),csdClient.context);
			}
			else if( csdClient.aaMechanism == CipherMechanism.HLS_2 ){
				csdClient.updateAare(aare);
				ASN1BitString conformance = new ASN1BitString();
				conformance.setInitValue(new byte[]{ (byte)0x00,(byte)0x50,(byte)0x1F});
				csdClient.setStatus(CSD_CLIENT_STATUS.SEND_STOC);
				return DlmsAssistant.getInstance().createStoC(csdClient.aaMechanism,csdClient.authenticationValue,csdClient.nextFrameCounter(),csdClient.context);
			}
		}
		
		if( csdClient.aaMechanism == CipherMechanism.HLS_2 ){
			csdClient.aaMechanism = CipherMechanism.HLS_GMAC;
		}else{
			throw new RuntimeException("HLS_2 And HLS_GMAC Can't Applink");
		}
		csdClient.setStatus(CSD_CLIENT_STATUS.APP_LINK);
		return DlmsAssistant.getInstance().createAarqApdu(csdClient.aaMechanism,csdClient.nextFrameCounter(),csdClient.context,msSysTitle);
	}
	
}
