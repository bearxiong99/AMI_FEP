package cn.hexing.dlms.meter.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cn.hexing.dlms.meter.cipher.Gcm128SoftCipher;
import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.common.spi.IEventHandler;
import cn.hexing.fk.message.msgbytes.MessageBytes;
import cn.hexing.fk.sockserver.event.ReceiveMessageEvent;
import cn.hexing.fk.utils.DateConvert;
import cn.hexing.fk.utils.HexDump;

import com.hx.dlms.ASN1OctetString;
import com.hx.dlms.DecodeStream;
import com.hx.dlms.DlmsData;
import com.hx.dlms.TagAdjunct;
import com.hx.dlms.aa.AareApdu;
import com.hx.dlms.aa.AarqApdu;
import com.hx.dlms.aa.AuthenticationMechanismName;
import com.hx.dlms.aa.DlmsContext;
import com.hx.dlms.aa.InitiateResponse;
import com.hx.dlms.message.DlmsMessage;

/**
 * 
 * @author gaoll
 *
 * @time 2012-12-3 下午3:29:28
 *
 * @info 消息处理器
 */
public class MessageHandler implements IEventHandler{
	
	
	public static enum OP_TYPE { OP_NA, OP_GET, OP_SET, OP_ACTION, OP_EVENT_NOTIFY, OP_CHANGE_KEY };
	
	private List<Integer> channelValue = new ArrayList<Integer>();
	
	/**主站sysTitle*/
	private final byte[] msSysTitle = new byte[]{ 0x48, 0x58, 0x45, 0x11, 0, 0, 0, 0 };

	private final String mySysTitle = "485845033557301F";
	
	private boolean isIran = false;
	
	private boolean isEncript = false;
	
	private int index = 0;
	
	private short version = 0x0001;
	private short srcAddr = 0x0001;
	private short dstAddr = 0x0010;
	
	public MessageBytes createMessage(byte[] apdu) throws Exception{
		ByteBuffer buffer = ByteBuffer.allocate(apdu.length+6+2); 
		buffer.putShort(version);
		buffer.putShort(srcAddr);
		buffer.putShort(dstAddr);
		buffer.putShort((short) apdu.length);
		buffer.put(apdu);
		buffer.flip();
		byte[] decipherFrame = null;
		decipherFrame = buffer.array();
		MessageBytes message = new MessageBytes();
		message.setData(decipherFrame);
		return message;
	}

	public void setValue(String value){
		String[] values = value.split(";");
		for(int i=0;i<values.length;i++){
			channelValue.add(Integer.parseInt(values[i]));
		}
	}
	
	@Override
	public void handleEvent(IEvent event) {
		try {
			if( event.getType().equals(EventType.MSG_RECV) ){
				onReceive(event);
			}
		} catch (Exception e) {
			System.out.println("occur error:"+e);
		}
	}
	
	public void onReceive(IEvent event) throws Exception{
		
		MessageBytes message=(MessageBytes) event.getMessage();
		DlmsMessage dm = new DlmsMessage();
		dm.read(ByteBuffer.wrap(message.getRawPacket()));
		ByteBuffer apdu = dm.getApdu();
		byte tag=apdu.get(0);
		
		ReceiveMessageEvent recvEvent = (ReceiveMessageEvent) event;
		
		MessageBytes sendMessage = null;
		switch (tag) {
		case (byte) 0xDA:
			break;
		case 0x60:
			sendMessage=handleAARQ(dm.getApdu().array(),event);
			break;
		case (byte) 0xC0:
			sendMessage=handleGet(dm.getApdu().array(),event);
			break;
		case (byte) 0xC1:
			break;
		case (byte) 0xC3:
			break;
		case (byte) 0xC8:
			//解密
			byte[] decipherFrame=decipher(dm.getApdu(),HexDump.toHex(msSysTitle));
			sendMessage=handleGet(decipherFrame,event);
			break;
		case (byte) 0xC9:
			break;
		case (byte) 0xCB: //对于Action只支持 认证
			sendMessage=createMessage(HexDump.toArray("CF2A3000000002FF6E8C6A001FC05C1790EA0C7ED27871D3312ED5B55AF84BC7461463D972A662B83EEB8EA9"));
			break;
		default:
			break;
		}
		if(sendMessage!=null){
			recvEvent.getClient().send(sendMessage);
		}
	}
	
	public byte[] cipher(byte[] plainApdu) throws Exception{

		int cipherTag = 204;

		
		ASN1OctetString octs = new ASN1OctetString();
		TagAdjunct myAdjunct = TagAdjunct.contextSpecificImplicit(cipherTag);
		octs.forceEncodeTag(true);
		myAdjunct.axdrCodec(true);
		octs.setTagAdjunct(myAdjunct);
		
		ByteBuffer iv = ByteBuffer.allocate(12);
		iv.put(HexDump.toArray(mySysTitle));
		int _frameCount = 1;
		iv.putInt( _frameCount );
		iv.flip();
		byte[] enc = Gcm128SoftCipher.getInstance().encrypt(plainApdu, iv.array() );
		ByteBuffer encWithSH = ByteBuffer.allocate(enc.length+5);
		encWithSH.put((byte)0x30).putInt(_frameCount).put(enc);
		
		octs.setValue(encWithSH.array());
		
		return octs.encode();
	}
	public  byte[]  decipher(ByteBuffer apdu,String sysTitle) throws IOException{
		ASN1OctetString octs = new ASN1OctetString();
		TagAdjunct myAdjunct = TagAdjunct.contextSpecificImplicit(0xFF & apdu.get(0));
		octs.forceEncodeTag(true);
		myAdjunct.axdrCodec(true);
		Gcm128SoftCipher cipher = Gcm128SoftCipher.getInstance();
		octs.setTagAdjunct(myAdjunct);
		octs.decode(DecodeStream.wrap(apdu));
		DlmsContext cxt = new DlmsContext();
		cxt.meterSysTitle = HexDump.toArray(sysTitle);
		byte[] val = octs.getValue();	// SH + C + T:  means security ctr + FC + cipher text + auth tag
		if( val[0] == 0x30 ){
			byte[] iv = cxt.makeInitVector(val, 1);
			byte[] cipherText = new byte[val.length-5];
			for(int i=0; i<cipherText.length; i++ )
				cipherText[i] = val[i+5];
			byte[] plainApdu = cipher.decrypt(cxt, cipherText, iv );
			ByteBuffer b=ByteBuffer.wrap(plainApdu);
			byte[] array = b.array();
			return array;
		}
		return null;
	}
	
	
	public static void main(String[] args) throws IOException {
		ByteBuffer apdu = HexDump.toByteBuffer("22113344");
		System.out.println(HexDump.toHex(apdu.get()));
		System.out.println(HexDump.toHex(apdu.get()));
		System.out.println(HexDump.toHex(apdu.get()));
		System.out.println(HexDump.toHex(apdu.get()));
		System.out.println(HexDump.toHex((byte)1));
		DlmsData time = new DlmsData();
		time.setDlmsDateTime("1391-01-01 00:00:00");
		List<Integer> channelValue = new ArrayList<Integer>();
		channelValue.add(1122);
		channelValue.add(1133);
		channelValue.add(1144);
		channelValue.add(1155);
		channelValue.add(1166);
		StringBuffer sb = new StringBuffer();
		sb.append("0101").append("02").append(HexDump.toHex((byte)(channelValue.size()+1)));
		sb.append(HexDump.toHex(time.encode()));
		for(int i=0;i<channelValue.size();i++){
			DlmsData value = new DlmsData();
			value.setDoubleLong(channelValue.get(i));
			sb.append(HexDump.toHex(value.encode()));
		}
		System.out.println(sb);
		
	}
	private MessageBytes handleGet(byte[] apdu, IEvent event) throws Exception {
		String frame=HexDump.toHex(apdu);
		
		String seq = frame.substring(4, 6);//seq
		int  classId=Integer.parseInt(frame.substring(6, 10));
		String obis=frame.substring(10, 6*2+10);
		int attrId=Integer.parseInt(frame.substring(6*2+10, 6*2+10+2));
		StringBuffer sb=createApdu(frame,seq,classId,obis,attrId);
		byte[] returnApdu = null;
		if(isEncript){
			returnApdu = cipher(HexDump.toArray(sb.toString()));
		}else{
			returnApdu = HexDump.toArray(sb.toString());
		}
		return createMessage(returnApdu);
	}

	private StringBuffer createApdu( String frame, String seq,int classId, String obis,
			int attrId) throws IOException {
		StringBuffer sb = new StringBuffer();
		sb.append("C401").append(seq).append("00");
		if(classId==8 && "0000010000FF".equals(obis) && attrId==2){//时间
			DlmsData dd = new DlmsData();
			dd.setDlmsDateTime(new Date());
			sb.append(HexDump.toHex(dd.encode()));
		}else if(classId==7 && "0001180300FF".equals(obis) && attrId==2){//通道数据，只支持1号通道
			DlmsData time = new DlmsData();
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH");
			Calendar c = Calendar.getInstance();
			c.set(Calendar.HOUR, index++ %24);
			String dateTime=sdf.format(c.getTime())+":00:00";
			time.setDlmsDateTime(isIran?DateConvert.gregorianToIran(dateTime):dateTime);
			sb.append("0101").append("02").append(HexDump.toHex((byte)(channelValue.size()+1)));
			sb.append(HexDump.toHex(time.encode()));
			for(int i=0;i<channelValue.size();i++){
				DlmsData value = new DlmsData();
				value.setDoubleLongUnsigned(channelValue.get(i));
				sb.append(HexDump.toHex(value.encode()));
			}
		
		}
		return sb;
	}

	private MessageBytes handleAARQ(byte[] apdu, IEvent event) throws Exception {
		AarqApdu aa = new AarqApdu();
		aa.decode(DecodeStream.wrap(apdu));
		AareApdu aare = new AareApdu();
		if(isEncript ){
			String apduString = HexDump.toHex(apdu);
			if(apduString.contains("857405080103")){
				aare.decode(DecodeStream.wrap(HexDump.toArray("6162A109060760857405080103A203020100A305A103020100A40A0408485845033557301F88020780890760857405080205AA0A800815077D90FFABD949BE24042228203000000003E12476E9BB342470C72722775269E3E2454ADB8960DCE8000A798C")));
			}else{
				aare.decode(DecodeStream.wrap(HexDump.toArray("6141A109060760857405080101A203020101A305A10302010288020780890760857405080201AA0A80083030303030303030BE0F040D0800065F040000001001000007")));
			}
		}else{
			InitiateResponse resp = new InitiateResponse();
			resp.getMaxRecvPduSize().setValue(0x01f4);
			resp.getConformance().setInitValue(new byte[]{0,(byte)0x50,(byte)0x1F});
			aare.setInitResponse(resp);
			aare.getAaResult().setValue(0);
			aare.getDiagnostics()[0].setValue(0);
			aare.getDiagnostics()[0].setBerCodec();
			aare.getAaResultDiagnostic().choose(aare.getDiagnostics()[0]);
			aare.setMechanismName(AuthenticationMechanismName.LLS);
		}
	
		byte[] encodes = aare.encode();

		MessageBytes message=createMessage(encodes);
		return message;
	}

	public final void setEncript(boolean isEncript) {
		this.isEncript = isEncript;
	}

	public final boolean isIran() {
		return isIran;
	}

	public final void setIran(boolean isIran) {
		this.isIran = isIran;
	}
	
	
	
	
}
