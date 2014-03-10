package cipher;

import java.io.IOException;
import java.nio.ByteBuffer;

import cn.hexing.fk.bp.dlms.cipher.Gcm128SoftCipher;
import cn.hexing.fk.utils.HexDump;

import com.hx.dlms.ASN1OctetString;
import com.hx.dlms.DecodeStream;
import com.hx.dlms.TagAdjunct;
import com.hx.dlms.aa.DlmsContext;

//4858451100000000  下行解密默认
//485845033133048C

//4845430005000001 上位机下行
//4858450330B92EB8
//4858450330B853A4
//4858450330000030
// 48 58 45 01 30 03 12 57
//C0010900070001180300FF0201010204020412000809060000010000FF0F02120000090C07DD0411030A000000800000090C07DD0411030A0000008000000100
public class Decipher {
	public static void main(String[] args) throws IOException {
		
		String frame = "C923300000000479AA048EEDCCF55B9CFE1E2188869BB7591BAB9FA477B907E4F0CD75452A";
		String sysTitle="4845430005000001";
		System.out.println(decipher(frame, sysTitle));
		
		
		
	}
	
	public static String decipher(String frame,String sysTitle) throws IOException{
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
	
	
}
