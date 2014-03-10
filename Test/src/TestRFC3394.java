import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.engines.RFC3394WrapEngine;
import org.bouncycastle.crypto.params.KeyParameter;

import cn.hexing.fk.utils.HexDump;



public class TestRFC3394 {
	public static void main(String[] args) {
		RFC3394WrapEngine rfc = new RFC3394WrapEngine(new AESEngine());
		rfc.init(true, new KeyParameter(HexDump.toArray("00000000000000000000000000000000")));
		System.out.println(HexDump.toHex(rfc.wrap(HexDump.toArray("11111111111111111111111111111111"), 0, 16)));
	}
}
