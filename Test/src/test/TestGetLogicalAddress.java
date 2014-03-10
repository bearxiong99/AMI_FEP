package test;

import cn.hexing.util.HexDump;

public class TestGetLogicalAddress {

	public static void main(String[] args) {
		String frame = "DD1000000000303B30333930383530303138";

		byte[] buf = 		HexDump.toByteBuffer(frame).array();
		int begin = 2, end = buf.length-1;
		while(buf[begin] == 0 )
			begin++;
		while(buf[end] == 0)
			end--;
		String smi = HexDump.toHex(buf, begin, end-begin+1);
		System.out.println(smi);
		String meterId = new String(buf,begin,end-begin+1);
		System.out.println(meterId);
	}
}
