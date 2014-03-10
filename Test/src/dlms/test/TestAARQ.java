package dlms.test;

import java.io.IOException;

import cn.hexing.fk.utils.HexDump;

import com.hx.dlms.DecodeStream;
import com.hx.dlms.aa.AareApdu;
import com.hx.dlms.aa.AarqApdu;

public class TestAARQ {
	public static void main(String[] args) throws IOException {
		AarqApdu aarq = new AarqApdu();
		byte[] aarq_bytes = HexDump.toArray("601DA109060760857405080101BE10040E01000000065F1F0400007E1F04B0");
		aarq.decode(DecodeStream.wrap(aarq_bytes));
		System.out.println(aarq);
		
		
		AareApdu aare = new AareApdu();
		aare.decode(DecodeStream.wrap("6142A109060760857405080101A203020100A305A10302010E88020780890760857405080202AA0A80083343353230433137BE10040E0800065F1F040000181F01000007"));
		System.out.println(HexDump.toHex(aare.getRespAuthenticationValue().getAuthValue()));
		
		System.out.println(aare);
	}
}
