package send;

import java.io.IOException;
import java.nio.ByteBuffer;

import send.CSDSyncTcpSocketClient.CSD_CLIENT_STATUS;
import cn.hexing.fk.bp.dlms.protocol.DlmsProtocolEncoder;
import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.events.BasicEventHook;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.message.msgbytes.MessageBytes;
import cn.hexing.fk.sockserver.event.AcceptEvent;
import cn.hexing.fk.sockserver.event.ClientCloseEvent;
import cn.hexing.fk.sockserver.event.ReceiveMessageEvent;
import cn.hexing.fk.utils.FCS;
import cn.hexing.fk.utils.HexDump;

import com.hx.dlms.ASN1BitString;
import com.hx.dlms.ASN1OctetString;
import com.hx.dlms.ASN1Type;
import com.hx.dlms.DecodeStream;
import com.hx.dlms.DlmsData;
import com.hx.dlms.TagAdjunct;
import com.hx.dlms.aa.AareApdu;
import com.hx.dlms.aa.AarqApdu;
import com.hx.dlms.aa.AarqApdu.CipherMechanism;
import com.hx.dlms.aa.DlmsContext;
import com.hx.dlms.applayer.action.ActionRequest;
import com.hx.dlms.applayer.action.ActionRequestNormal;
import com.hx.dlms.applayer.get.GetRequest;
import com.hx.dlms.applayer.get.GetRequestNext;
import com.hx.dlms.applayer.get.GetResponse;
import com.hx.dlms.applayer.get.GetResponseNormal;
import com.hx.dlms.applayer.get.GetResponseWithBlock;
import com.hx.dlms.cipher.AESECB128;
import com.hx.dlms.cipher.Gcm128SoftCipher;
import com.hx.dlms.cipher.IDlmsCipher;
import com.hx.dlms.message.DlmsMessage;

/**
 * 
 * @author gaoll
 *
 * @time 2013-3-21 下午06:43:20
 *
 * @info CSD Server Event Handler.
 */
public class CSDServerEventHandler extends BasicEventHook{
	private final byte[] msSysTitle = new byte[]{ 0x48, 0x58, 0x45, 0x11, 0, 0, 0, 0 };
	
	public static enum OP_TYPE { OP_NA, OP_GET, OP_SET, OP_ACTION, OP_EVENT_NOTIFY, OP_CHANGE_KEY,OP_UPGRADE};
	
	
	private IDlmsCipher cipher = Gcm128SoftCipher.getInstance();
	
	@Override
	public void handleEvent(IEvent event) {
		
		if(event.getType()==EventType.ACCEPTCLIENT){
			AcceptEvent acceptEvent = (AcceptEvent) event;
			CSDMessageQueue.getInstance().onCSDClientIdle((CSDSyncTcpSocketClient) acceptEvent.getClient());
		}else if(event.getType()==EventType.CLIENTCLOSE){
			ClientCloseEvent closeEvent = (ClientCloseEvent)event;
			CSDMessageQueue.getInstance().onCSDClientClose((CSDSyncTcpSocketClient) closeEvent.getClient());
		}else if(event.getType()==EventType.MSG_RECV){
			try {
				onReceive(event);
			} catch (Exception e) {
				ReceiveMessageEvent recvEvent = (ReceiveMessageEvent)event;
				CSDSyncTcpSocketClient csdClient=(CSDSyncTcpSocketClient) recvEvent.getClient();
				csdClient.hangUp();
				System.out.println("catch exception,hang up.");
				e.printStackTrace();
			}
		}
		
	}

	private void onReceive(IEvent event) throws Exception {
		ReceiveMessageEvent recvEvent = (ReceiveMessageEvent) event;
		if(!(recvEvent.getMessage() instanceof MessageBytes)){
			throw new RuntimeException("Not MessageBytes");
		}
		MessageBytes recvMsg=(MessageBytes) recvEvent.getMessage();
		CSDSyncTcpSocketClient csdClient=(CSDSyncTcpSocketClient) recvEvent.getClient();
		CSD_CLIENT_STATUS status = csdClient.getStatus();
		MessageBytes nextMsg = new MessageBytes();
		switch(status){
		case DIAL:{
			//暂时不处理拨号不成功的。
			//如果拨号成功了。发送链路层链接
			System.out.println("dial return");
			nextMsg.setData(HexDump.toArray("7EA020030393FEC9818014050207D0060207D00704000000010804000000013AF27E"));
			csdClient.setStatus(CSD_CLIENT_STATUS.HDLC_LINK);
			System.out.println("hdlc link send");
			break;
		}
		case HDLC_LINK:{
			System.out.println("hdlc link return");
			//链路层链接成功
			//7EA020030373F02E818014050200C8060200C80704000000010804000000010B877E
			//进行第一次APP_LINK
			byte[] apdu = createAarqApdu(csdClient);
			apdu=wrapHDLCFrame(apdu, csdClient);
			nextMsg.setData(apdu);
			csdClient.setStatus(CSD_CLIENT_STATUS.APP_LINK);
			break;
		}
		case APP_LINK:{
			byte[] unWrapHdlcData=unWrapHdlcFrame(recvMsg.getRawPacket(),csdClient);
			System.out.println("app link return");
			byte[] apdu=handleAARE(unWrapHdlcData,csdClient);
			apdu=wrapHDLCFrame(apdu,csdClient);
			nextMsg.setData(apdu);
			break;
		}
		case SEND_STOC:{
			byte[] unWrapHdlcData=unWrapHdlcFrame(recvMsg.getRawPacket(),csdClient);
			//发送真实msg
			System.out.println("StoC return");
			byte[] decipherApdu=getDecipherApdu(unWrapHdlcData,csdClient);
			System.out.println(HexDump.toHex(decipherApdu));
			DlmsMessage realMsg = (DlmsMessage) csdClient.getRealMessage();
			byte[] warpData = wrapHDLCFrame(realMsg.getApdu().array(), csdClient);
			nextMsg.setData(warpData);
			csdClient.setStatus(CSD_CLIENT_STATUS.BUSY);
			System.out.println("real msg send");
			break;
		}
		case BUSY:{
			byte[] unWrapHdlcData=unWrapHdlcFrame(recvMsg.getRawPacket(),csdClient);
			System.out.println(HexDump.toHex(unWrapHdlcData));
			byte[] apdu=handleUpMessage(unWrapHdlcData,csdClient);
			if(apdu==null) return ;
			apdu=wrapHDLCFrame(apdu, csdClient);
			nextMsg.setData(apdu);
			break;
		}
		case HANG_UP:{
			//到达这里说明返回了挂断命令请求，要判断是否挂断成功
			csdClient.onHangUp();
			break;
		}
		}
		csdClient.send(nextMsg);
	}
	
	private byte[] unWrapHdlcFrame(byte[] rawPacket,CSDSyncTcpSocketClient csdClient) {
		//将HDLC帧剥离
		ByteBuffer apdu = ByteBuffer.wrap(rawPacket);
		int tag = apdu.get();
		int frameType = apdu.get();
		int headLength = 0;
		if((tag!=0x7E)&& ((frameType!=0xA0)||(frameType!=0xA8))){
			throw new RuntimeException("not hdlc frame");
		}
		int len = apdu.get();
		if((len+2)!=rawPacket.length){
			throw new RuntimeException("hdlc frame length is uncorrect");
		}
		headLength = 2;
		
		int destAddr=apdu.get();
		System.out.println(destAddr+"-"+(destAddr&0x01));
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

	private byte[] wrapHDLCFrame(byte[] apdu,CSDSyncTcpSocketClient csdClient) {
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
		String strApdu = HexDump.toHex(apdu);
		StringBuilder sb = new StringBuilder();
		sb.append("A0")
		  .append("00")
		  .append("0303")
		  .append(HexDump.toHex((byte)controlWord))
		  .append("CS")
		  .append("CS")
		  .append("E6E600")
		  .append(strApdu)
		  .append("CS")
		  .append("CS");
		sb.replace(2, 4, HexDump.toHex((byte)(sb.length()/2)));
		int headLen = 10;
		String hcs = FCS.fcs(sb.substring(0, headLen));
		sb.replace(headLen, headLen+2, hcs.substring(2, 4));
		sb.replace(headLen+2, headLen+4, hcs.substring(0, 2));
		String fcs = FCS.fcs(sb.substring(0,sb.length()-4));
		sb.replace(sb.length()-4, sb.length()-2, fcs.substring(2, 4));
		sb.replace(sb.length()-2, sb.length(), fcs.substring(0, 2));
		sb.insert(0, "7E");
		sb.append("7E");
		return HexDump.toArray(sb.toString());
	}

	private byte[] handleUpMessage(byte[] rawPacket,
			CSDSyncTcpSocketClient csdClient) throws Exception {
		rawPacket=getDecipherApdu(rawPacket, csdClient);
		ByteBuffer apdu = ByteBuffer.wrap(rawPacket);
		int applicationTag = apdu.get(0) & 0xFF ;
		switch(applicationTag){
		case 0xC4:
			return handleGetResponse(apdu,csdClient);
		case 0xC5:
			return handleSetResponse(apdu,csdClient);
		case 0xC7:
			return handleActionResponse(apdu,csdClient);
		}
		
		return null;
	}

	private byte[] handleSetResponse(ByteBuffer apdu,
			CSDSyncTcpSocketClient csdClient) {
		return null;
	}

	private byte[] handleActionResponse(ByteBuffer apdu,
			CSDSyncTcpSocketClient csdClient) {
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
			System.out.println("duplicated:"+duplicated);
			temp = repBlock.getRawData();
			if( null == temp ){
				throw new RuntimeException("get response failue");
			}else{
				len += temp.length ;
			}
			csdClient.blockReplys.add(repBlock);
			if( !lastBlock ){
				//sendNextBlock
				GetRequestNext next = new GetRequestNext();
				next.setInvokeId(repBlock.getInvokeId());
				next.setPriorityHigh(repBlock.isPriorityHigh());
				next.setBlockNumber(blockNum);
				GetRequest reqGet = new GetRequest(next);
				byte[] nextApdu = reqGet.encode();
				if(csdClient.context.aaMechanism==CipherMechanism.HLS_GMAC){
					nextApdu=cipher(OP_TYPE.OP_GET, nextApdu, csdClient.context);
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
			}
		}
		
		return null;
	}

	private void communicateFinish(byte[] apdu,CSDSyncTcpSocketClient csdClient) {
		DlmsMessage dm = new DlmsMessage();
		dm.setApdu(apdu);
		//这里将数据上送给FE
		csdClient.hangUp();
	}

	private byte[] getDecipherApdu(byte[] rawPacket,
			CSDSyncTcpSocketClient csdClient) throws IOException {
		ByteBuffer apdu = ByteBuffer.wrap(rawPacket);
		int applicationTag = apdu.get(0) & 0xFF;
		switch(applicationTag){
		case 0xCF:
		case 0xCC:
		case 0xCD:
			return decipher(apdu, csdClient);
		}
		return rawPacket;
	}
	
	private byte[] decipher(ByteBuffer apdu,CSDSyncTcpSocketClient csdClient) throws IOException{
		ASN1OctetString octs = new ASN1OctetString();
		TagAdjunct myAdjunct = TagAdjunct.contextSpecificImplicit(0xFF & apdu.get(0));
		octs.forceEncodeTag(true);
		myAdjunct.axdrCodec(true);
		octs.setTagAdjunct(myAdjunct);
		octs.decode(DecodeStream.wrap(apdu));
		byte[] val = octs.getValue();	// SH + C + T:  means security ctr + FC + cipher text + auth tag
		if( val[0] == 0x30 ){
			byte[] iv = csdClient.makeInitVector(val, 1);
			byte[] cipherText = new byte[val.length-5];
			for(int i=0; i<cipherText.length; i++ )
				cipherText[i] = val[i+5];
			return cipher.decrypt(csdClient.context, cipherText, iv );
		}
		return null;
	
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
				return createStoC(csdClient);
			}
			else if( csdClient.aaMechanism == CipherMechanism.HLS_2 ){
				csdClient.updateAare(aare);
				ASN1BitString conformance = new ASN1BitString();
				conformance.setInitValue(new byte[]{ (byte)0x00,(byte)0x50,(byte)0x1F});
				return createStoC(csdClient);
			}
		}
		
		if( csdClient.aaMechanism == CipherMechanism.HLS_2 ){
			csdClient.aaMechanism = CipherMechanism.HLS_GMAC;
		}else{
			throw new RuntimeException("HLS_2 And HLS_GMAC Can't Applink");
		}
		return createAarqApdu(csdClient);
	}
	
	private byte[] createStoC(CSDSyncTcpSocketClient csdClient) throws IOException{
		//Action.request操作0-0:40.0.0.255的 method( 1 )，将服务端发送的随机数加密计算后的结果发送给服务端
		ActionRequest request = new ActionRequest();
		String obisString = "0.0.40.0.0.255";
		DlmsData param = new DlmsData();
		byte[] apdu = null;
		if( csdClient.aaMechanism == CipherMechanism.HLS_2 ){
			//HLS使用AES-ECB对16字节随机数进行加密
			byte[] ctosParam = AESECB128.encrypt(csdClient.authenticationValue);
			param.setOctetString(ctosParam);
			ActionRequestNormal reqNormal = new ActionRequestNormal(1,15,DlmsProtocolEncoder.convertOBIS(obisString),1,param);
			request.choose(reqNormal);
			apdu = request.encode();
		}
		else if( csdClient.aaMechanism == CipherMechanism.HLS_GMAC ){
			ByteBuffer iv = ByteBuffer.allocate(12);
			iv.put(msSysTitle);
			int _frameCount = csdClient.nextFrameCounter();
			iv.putInt( _frameCount );
			iv.flip();
			
			byte[] authTag = cipher.auth(csdClient.context, csdClient.authenticationValue, iv.array() );
			ByteBuffer fstoc = ByteBuffer.allocate(authTag.length+5);
			//Security head + frameCounter + authenticationTag
			fstoc.put((byte)0x10).putInt(_frameCount).put(authTag);
			fstoc.flip();
			param.setOctetString(fstoc.array());
			
			ActionRequestNormal reqNormal = new ActionRequestNormal(1,15,DlmsProtocolEncoder.convertOBIS(obisString),1,param);
			request.choose(reqNormal);
			byte[] plainText = request.encode();
			byte[] enc = cipher.encrypt(csdClient.context, plainText, iv.array() );
			ByteBuffer actStoC = ByteBuffer.allocate(enc.length+5);
			actStoC.put((byte)0x30).putInt(_frameCount).put(enc);
			
			//203:	global ciphering, glo-action-request
			ASN1OctetString cipheredStoC = new ASN1OctetString();
			TagAdjunct myAdjunct = TagAdjunct.contextSpecificImplicit(203);
			myAdjunct.axdrCodec(true);
			cipheredStoC.forceEncodeTag(true);
			cipheredStoC.setTagAdjunct(myAdjunct);
			cipheredStoC.setValue(actStoC.array());
			apdu = cipheredStoC.encode();
		}
		else{
			throw new RuntimeException("CtoS, not supported mechanism:"+csdClient.aaMechanism);
		}
		csdClient.setStatus(CSD_CLIENT_STATUS.SEND_STOC);
		System.out.println("StoC send");
		return apdu;
	
	}

	private byte[] cipher(OP_TYPE opType,byte[] plainApdu, DlmsContext context) throws Exception{

		int cipherTag = 0;
		switch( opType ){
		case OP_ACTION:
			cipherTag = 203;   //with global ciphering, glo-action-request
			break;
		case OP_GET:
			cipherTag = 200;   //with global ciphering, glo-get-request
			break;
		case OP_SET:
			cipherTag = 201;   //with global ciphering, glo-set-request
			break;
		case OP_EVENT_NOTIFY:
			cipherTag = 202;   //with global ciphering, glo-event-notification-request
			break;
		default:
			break;
		}
		
		ASN1OctetString octs = new ASN1OctetString();
		TagAdjunct myAdjunct = TagAdjunct.contextSpecificImplicit(cipherTag);
		octs.forceEncodeTag(true);
		myAdjunct.axdrCodec(true);
		octs.setTagAdjunct(myAdjunct);
		
		ByteBuffer iv = ByteBuffer.allocate(12);
		iv.put(this.msSysTitle);
		int _frameCount = context.nextFrameCounter();
		iv.putInt( _frameCount );
		iv.flip();
		byte[] enc = cipher.encrypt(context, plainApdu, iv.array() );
		ByteBuffer encWithSH = ByteBuffer.allocate(enc.length+5);
		encWithSH.put((byte)0x30).putInt(_frameCount).put(enc);
		
		octs.setValue(encWithSH.array());
		
		return octs.encode();
	}
	
	
	private byte[] createAarqApdu(CSDSyncTcpSocketClient csdClient)
			throws IOException {
		AarqApdu aarq = AarqApdu.create(csdClient.aaMechanism);
		switch( csdClient.aaMechanism ){
		case HLS_2:
			break;
		case HLS_GMAC:{
			//set SysTitle.
			aarq.setCallingApTitle(msSysTitle);
			ByteBuffer iv = ByteBuffer.allocate(12);
			iv.put(msSysTitle);
			int _frameCount = csdClient.nextFrameCounter();
			iv.putInt( _frameCount );
			iv.flip();
			byte[] initRequest = aarq.getInitiateRequest();
			byte[] cipherInitRequest = cipher.encrypt(csdClient.context, initRequest, iv.array() );
			ByteBuffer cipherContext = ByteBuffer.allocate(cipherInitRequest.length+5);
			//Security head + frameCounter + cipherText + authenticationTag
			cipherContext.put((byte)0x30).putInt(_frameCount).put(cipherInitRequest);
			cipherContext.flip();
			
			//glo-initiateRequest [33] IMPLICIT OCTET STRING,
			ASN1OctetString cipheredRequest = new ASN1OctetString();
			cipheredRequest.setBerCodec();
			TagAdjunct myAdjunct = TagAdjunct.contextSpecificImplicit(33);
			myAdjunct.axdrCodec(true);
			cipheredRequest.setTagAdjunct(myAdjunct);
			cipheredRequest.setValue(cipherContext.array());
			aarq.setInitiateRequest(cipheredRequest.encode());
		}
			break;
		case HLS_MD5:
			break;
		case HLS_SHA1:
			break;
		case LLS:
			break;
		case NO_SECURITY:
			break;
		default:
			break;
		}
		csdClient.setStatus(CSD_CLIENT_STATUS.APP_LINK);
		System.out.println("app link send:"+csdClient.aaMechanism);
		return aarq.encode();
	}
}
