package gw.convert;

import cn.hexing.fas.protocol.gw.parse.DataItemCoder;
import cn.hexing.fk.utils.HexDump;

public class ValueConvert {
	public static void main(String[] args) {
		System.out.println("C:\\FILE\\d.txt".length());
		System.out.println(DataItemCoder.coder("C:\\FILE\\d.txt", "ASC32"));
		String s  = new String(HexDump.toArray("433A5C46494C455C642E747874"));
		System.out.println(s);
	}
}
