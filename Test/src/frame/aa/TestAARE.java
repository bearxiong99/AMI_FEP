package frame.aa;

import java.io.IOException;

import cn.hexing.util.HexDump;

import com.hx.dlms.DecodeStream;
import com.hx.dlms.aa.AareApdu;

public class TestAARE {
	public static void main(String[] args) throws IOException {
		
		AareApdu aare = new AareApdu();
		aare.decode(new DecodeStream(HexDump.toArray("6169A109060760857405080103A203020100A305A10302010EA40A040848584501353EC60988020780890760857405080205AA12801044373643423333313732393343413931BE230421281F300000174BF456A0744EE1A6A451CB7E807A50FC94D9B04D93BBB1D388528C")));
		System.out.println(aare);
	
	}
}
