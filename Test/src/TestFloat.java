import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;

import cn.hexing.fk.utils.HexDump;

import com.hx.dlms.DlmsData;


public class TestFloat {
	public static void main(String[] args) throws IOException {
		DecimalFormat df = new DecimalFormat("#.000");
//		double d = 12345678.12345;
		System.out.println(Double.parseDouble("3333322233.81"));
		String dStr = df.format(Double.parseDouble("3333322233.81"));
		System.out.println(dStr);
		
		DlmsData dd = new DlmsData();
		dd.setFloat32((float) 1.0);
		System.out.println(HexDump.toHex(dd.encode()));
		
		
		ByteBuffer buf = ByteBuffer.allocate(4);
		buf.putFloat(8);
		System.out.println(HexDump.toHex(buf.array()));
	}
}
