package convert;

import java.io.IOException;

import com.hx.dlms.DlmsData;

import cn.hexing.util.HexDump;

public class Test1 {
	public static void main(String[] args) throws IOException {
		
		
		DlmsData d = new DlmsData();
		d.setOctetString("333610".getBytes());
		System.out.println(HexDump.toHex(d.encode()));
		String classId="0A";
		String s = "0100010802FF";
		
		byte[] b=HexDump.toArray(s);
		for(int i = 0;i<b.length;i++){
			System.out.println(Integer.parseInt(HexDump.toHex(b[i]),16));
		}
		
		String attrId="2";
	}
}
