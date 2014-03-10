package test;

import cn.hexing.fk.utils.DateConvert;

public class TestDateConvert {
	public static void main(String[] args) {
		System.out.println(	DateConvert.gregorianToIran("2012-12-27 15:36:00"));
		
		System.out.println(DateConvert.iranToGregorian("1391-09-30 00:00:00"));
	}
}
