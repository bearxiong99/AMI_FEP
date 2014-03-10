package cn.hexing.dlms.cipher;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.bouncycastle.crypto.InvalidCipherTextException;

import cn.hexing.fk.utils.HexDump;

import com.hx.dlms.ASN1OctetString;
import com.hx.dlms.DecodeStream;
import com.hx.dlms.TagAdjunct;
import com.hx.dlms.aa.AarqApdu.CipherMechanism;
import com.hx.dlms.aa.DlmsContext;
import com.hx.dlms.cipher.AESGcm128;
import com.hx.dlms.cipher.CipherUtil;
import com.hx.dlms.cipher.SHA256;

public class Gcm128SoftCipher{
	
	public static final Gcm128SoftCipher getInstance(){ return instance; }
	
	private static final Gcm128SoftCipher instance = new Gcm128SoftCipher();
	private Gcm128SoftCipher(){}
	
	private static final byte[] makeAssociationData(byte[] authKey, byte sc){
		if( null == authKey || authKey.length == 0 )
			return null;
		ByteBuffer buf = ByteBuffer.allocate(authKey.length+1);
		buf.put(sc).put(authKey);
		buf.flip();
		return buf.array();
	}
	
	public byte[] auth(DlmsContext context, byte[] plain, byte[] initVector ) throws IOException {
		if( context.aaMechanism == CipherMechanism.BENGAL ){
			return SHA256.digest(plain);
		}
		try {
			byte[] associatedText = makeAssociationData(context.authKey,(byte)0x10);
			associatedText = CipherUtil.cat(associatedText,plain);
			return AESGcm128.encrypt(context.encryptKey, initVector, new byte[0], associatedText);
		} catch (InvalidCipherTextException exp) {
			throw new IOException(exp);
		}
	}
	
	public byte[] encrypt(DlmsContext context, byte[] plain, byte[] initVector ) throws IOException {
		try {
			byte[] at = makeAssociationData(context.authKey,(byte)0x30 );
			
			return AESGcm128.encrypt(context.encryptKey, initVector, plain, at);
		} catch (InvalidCipherTextException exp) {
			throw new IOException(exp);
		}
	}
	
	public byte[] encrypt(byte[] plain,byte[] initVector) throws IOException{
		byte[] at = makeAssociationData(HexDump.toArray("00000000000000000000000000000000"), (byte)0x30);
	
		try {
			return AESGcm128.encrypt(HexDump.toArray("00000000000000000000000000000000"), initVector, plain, at);
		} catch (InvalidCipherTextException e) {
			throw new IOException(e);
		}
	}

	public byte[] decrypt(DlmsContext context, byte[] ciphered,
			byte[] initVector ) throws IOException {
		try {
			byte[] ad = makeAssociationData(context.authKey,(byte)0x30);
			return AESGcm128.decrypt(context.encryptKey, initVector, ciphered, ad );
		} catch (InvalidCipherTextException exp) {
			throw new IOException(exp);
		}
	}
	
	private static void testEncrypt() throws InvalidCipherTextException{
		byte[] ek = HexDump.toArray("00000000000000000000000000000000");
		byte[] ak = ek;
		String sysTitle = "48584503356C6884";
		String fc = "00000006";
		byte[] iv = HexDump.cat(HexDump.toArray(sysTitle), HexDump.toArray(fc));
		byte[] plain = HexDump.toArray("010E00070003180300FF0201010204020412000809060000010000FF0F02120000090C07DC081D030C2D0000800000090C07DC081D030C2D00008000000100");
		byte[] ad = makeAssociationData(ak,(byte)0x30);
		byte[] p=AESGcm128.encrypt(ek, iv, plain, ad);
		System.out.println("cipher:"+HexDump.toHex(p));
	}
	
	private static void test() throws InvalidCipherTextException{
		byte[] ek = HexDump.toArray("00000000000000000000000000000000");
		byte[] ak = ek;
		String sysTitle = "4858451100000000";
//		String sysTitle = "48584503356C6884";
		String fc = "00000007";
		byte[] iv = HexDump.cat(HexDump.toArray(sysTitle), HexDump.toArray(fc));
		byte[] c = HexDump.toArray("52A108D20B6F1C35A27371B5B527B2760289E9FFE6A742AA7C");
		byte[] ad = makeAssociationData(ak,(byte)0x30);

		byte[] p = AESGcm128.decrypt(ek,iv,c,ad);
		System.out.println("plain ="+HexDump.toHex(p));
	}
	
	private static void testAuth() throws IOException{
		DlmsContext cxt = new DlmsContext();
		cxt.encryptKey = HexDump.toByteBuffer("000102030405060708090A0B0C0D0E0F").array();
		cxt.authKey =    HexDump.toByteBuffer("D0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF").array();
		byte[] iv =      HexDump.toByteBuffer("4D4D4D0000BC614E01234567").array();
		byte[] ctos =    HexDump.toByteBuffer("4B35366956616759").array();
		byte[] stoc =    HexDump.toByteBuffer("503677524A323146").array();

		
		byte[] a2 = getInstance().auth(cxt, stoc, iv);
		System.out.println("StoC plain : "+ new String(stoc));  //P6wRJ21F
		System.out.println("TStoC auth2: "+ HexDump.hexDumpCompact(a2, 0,a2.length));

		byte[] a1 = getInstance().auth(cxt, ctos, iv);
		System.out.println("CtoS plain : "+ new String(ctos));  //K56iVagY
		System.out.println("TCtoS auth1: "+ HexDump.hexDumpCompact(a1, 0,a1.length));
		System.out.println();
	}
	
	public byte[] cipher(String frame,String sysTitle,String seq) throws InvalidCipherTextException{
		byte[] ek = HexDump.toArray("00000000000000000000000000000000");
		byte[] ak = ek;
		byte[] iv = HexDump.cat(HexDump.toArray(sysTitle), HexDump.toArray(seq));
		byte[] plain = HexDump.toArray(frame);
		byte[] ad = makeAssociationData(ak,(byte)0x30);
		byte[] p=AESGcm128.encrypt(ek, iv, plain, ad);
		return p;
	}
	
	public byte[] decipher(String frame,String sysTitle) throws IOException{
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
			
			return array;
		}
		return null;
	}
	public static void main(String[] args) {
		try {
			testEncrypt();
			test();
			
			testAuth();
		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}

}
