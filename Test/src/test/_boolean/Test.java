package test._boolean;

import cn.hexing.util.HexDump;


public class Test {
	public static void main(String[] args) {
		
		byte[] bs = HexDump.toArray("0000 0000 0000 0000 0000 0000 0000 1000");
		
	
		System.out.println(Long.toBinaryString(	Long.parseLong("80400080",16)));
		//100110010101100111011010000
		//100000000100000000000000 1000 0000
	}
}
