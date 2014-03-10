package cn.hexing.dlms.cipher;

import java.io.IOException;
import java.nio.ByteBuffer;

import cn.hexing.fk.utils.HexDump;

import com.hx.dlms.ASN1OctetString;
import com.hx.dlms.DecodeStream;
import com.hx.dlms.TagAdjunct;
import com.hx.dlms.aa.DlmsContext;

public class DecipherAssistant {
	
	
	private static DecipherAssistant instance = new DecipherAssistant();
	
	public static DecipherAssistant getInstance(){return instance;}
	
	private DecipherAssistant(){}
	
	public  String decipher(String frame,String sysTitle) throws IOException{
		ByteBuffer apdu = HexDump.toByteBuffer(frame);
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
			
			return HexDump.toHex(array);
		}
		return null;
	}
	//4858451100000000  下行解密默认
	public static void main(String[] args) throws IOException {
		
		DecipherAssistant.getInstance().decipher("", "");
		
	}
		
}
