package test;

import java.util.Locale;

import cn.hexing.fk.utils.HexDump;

public class Test111 {
	public static void main(String[] args) {
		byte [] bs = new byte[]{(byte) 0xFF,(byte) 0xFE};
		                     
		byte b = (byte) 255;
		System.out.println(b);
		System.out.println(HexDump.toHex(bs));
		HexDump.toArray("123212");
		
	}
}
