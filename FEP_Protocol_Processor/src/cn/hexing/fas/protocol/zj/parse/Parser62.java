package cn.hexing.fas.protocol.zj.parse;

import java.text.NumberFormat;

import cn.hexing.fk.utils.HexDump;

/**
 * 
 * @author gaoll
 *
 * @time 2012-11-27 下午4:35:48
 *
 * @info 前两个字节第一路模拟量，后两个字节是第二路模拟量
 */
public class Parser62 {
	
	
	public static Object parsevalue(byte[] data,int loc){
		
		Object rt = null;
		int first = ParseTool.nBcdToDecimal(data, loc, 2);
		loc+=2;
		int second = ParseTool.nBcdToDecimal(data, loc, 2);
		int fraction = 2;
		NumberFormat snf=NumberFormat.getInstance();
		String s_first="";
		String s_second="";
		snf.setMinimumFractionDigits(fraction);
		snf.setGroupingUsed(false);
		s_first=snf.format((double)first/ParseTool.fraction[fraction]);
		fraction = 1;
		snf.setMinimumFractionDigits(fraction);
		snf.setGroupingUsed(false);
		s_second = snf.format((double)second/ParseTool.fraction[fraction]);
		rt =s_second;
		return rt;
	}
	
	public static void main(String[] args) {
		parsevalue(HexDump.toArray("12131415"), 0);
	}
	
}
