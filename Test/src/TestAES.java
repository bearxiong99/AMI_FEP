import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.engines.RFC3394WrapEngine;
import org.bouncycastle.crypto.params.KeyParameter;

import cn.hexing.fk.utils.HexDump;

import com.hx.dlms.cipher.AESGcm128;


public class TestAES {
	public static void main(String[] args) throws InvalidCipherTextException, InterruptedException {
		
		String masterKey = "00000000000000000000000000000000";
		
		String iv = "000000000000000000000000";
		String plain = "00000000000000000000000000000000";
		byte[] enc=AESGcm128.encrypt(HexDump.toArray(masterKey), HexDump.toArray(iv), HexDump.toArray(plain), new byte[0]);
		System.out.println(HexDump.toHex(enc));
		RFC3394WrapEngine rfc = new RFC3394WrapEngine(new AESEngine());
		rfc.init(true, new KeyParameter(HexDump.toArray(masterKey)));
		System.out.println(HexDump.toHex(rfc.wrap(enc, 0, 16)));
	}
}
