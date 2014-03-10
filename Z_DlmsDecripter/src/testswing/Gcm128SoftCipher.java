package testswing;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.bouncycastle.crypto.InvalidCipherTextException;

import com.hx.dlms.cipher.AESGcm128;

public class Gcm128SoftCipher {

	private static Gcm128SoftCipher instance = new Gcm128SoftCipher();
	
	private Gcm128SoftCipher(){}
	
	public static Gcm128SoftCipher getInstance(){
		return instance;
	}

	public byte[] decrypt(byte[] encryptKey,byte[] authKey,byte[] ciphered,byte[] initVector) throws IOException {
		try {
			byte[] ad = makeAssociationData(authKey,(byte)0x30);
			return AESGcm128.decrypt(encryptKey, initVector, ciphered, ad );
		} catch (InvalidCipherTextException exp) {
			throw new IOException(exp);
		}
	}
	
//	public  String decipher(String frame,String sysTitle) throws IOException{
//		ByteBuffer apdu = HexDump.toByteBuffer(frame);
//		ASN1OctetString octs = new ASN1OctetString();
//		TagAdjunct myAdjunct = TagAdjunct.contextSpecificImplicit(0xFF & apdu.get(0));
//		octs.forceEncodeTag(true);
//		myAdjunct.axdrCodec(true);
//		octs.setTagAdjunct(myAdjunct);
//		octs.decode(DecodeStream.wrap(apdu));
//		DlmsContext cxt = new DlmsContext();
//		cxt.meterSysTitle = HexDump.toArray(sysTitle);
//		byte[] val = octs.getValue();	// SH + C + T:  means security ctr + FC + cipher text + auth tag
//		if( val[0] == 0x30 ){
//			byte[] iv = cxt.makeInitVector(val, 1);
//			byte[] cipherText = new byte[val.length-5];
//			for(int i=0; i<cipherText.length; i++ )
//				cipherText[i] = val[i+5];
//			byte[] plainApdu = this.decrypt(cxt, cipherText, iv );
//			ByteBuffer b=ByteBuffer.wrap(plainApdu);
//			byte[] array = b.array();
//			
//			return HexDump.toHex(array);
//		}
//		return null;
//	}
	
	private static final byte[] makeAssociationData(byte[] authKey, byte sc){
		if( null == authKey || authKey.length == 0 )
			return null;
		ByteBuffer buf = ByteBuffer.allocate(authKey.length+1);
		buf.put(sc).put(authKey);
		buf.flip();
		return buf.array();
	}
	
	
}
