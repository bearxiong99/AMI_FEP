package test;

import java.math.BigDecimal;

import cn.hexing.fk.utils.HexDump;

public class TestDouble {
	public static void main(String[] args) {
		System.out.println(HexDump.toHex(1));
		double d= 5944313*0.000001;
		System.out.println(d);
		double   f   =   0.2230;  
		BigDecimal   b   =   new   BigDecimal(f); 
		double   f1   =   b.setScale(4,   BigDecimal.ROUND_HALF_UP).doubleValue();  
		System.out.println(f1);
	}
}
