package send;

import java.io.IOException;

import cn.hexing.fk.utils.HexDump;

import com.hx.dlms.aa.AarqApdu;
import com.hx.dlms.aa.AarqApdu.CipherMechanism;

public class TestAARQ {
	public static void main(String[] args) throws IOException {
		AarqApdu aarq = AarqApdu.create(CipherMechanism.HLS_2);
		System.out.println(HexDump.toHex(aarq.encode()));
	}
}
