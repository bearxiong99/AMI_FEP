package cn.hexing.tool.decriptor;

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
	
	private static final byte[] makeAssociationData(byte[] authKey, byte sc){
		if( null == authKey || authKey.length == 0 )
			return null;
		ByteBuffer buf = ByteBuffer.allocate(authKey.length+1);
		buf.put(sc).put(authKey);
		buf.flip();
		return buf.array();
	}
	
	
}
