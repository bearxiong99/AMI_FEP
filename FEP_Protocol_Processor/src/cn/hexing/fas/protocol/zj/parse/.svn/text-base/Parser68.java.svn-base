package cn.hexing.fas.protocol.zj.parse;

import cn.hexing.fk.utils.HexDump;

/**
 * 
 * @author gaoll
 *
 * @time 2012-11-27 下午8:50:21
 *
 * @info 对于一个数据项分为单个字节的数据解析
 */
public class Parser68 {
	
	
	public static Object parsevalue(byte[] data,int loc,int len){
		Object rt=null;
		String value="";
		for(int i = 0;i<len;i++){
			int count = ParseTool.BCDToDecimal(data[loc++]);
			value+=count+":";
		}
		rt =value.substring(0, value.length()-1);
		return rt;
	}
	public static int constructor(byte[] data,String value,int loc,int len,int fraction){
		String[] values = value.split(";");
		for(int i = 0 ;i<len;i++){
			Parser01.constructor(data, values[i], loc+i, 1, fraction);
		}
		return len;
	}
	
	public static void main(String[] args) {
		byte[] a = HexDump.toArray("010101010101010000000000000000000000000000");
		parsevalue(a, 0, 21);
		byte[] data = new byte[12];
		constructor(data , "1;2;3;4;5;6;7;8;9;10;11;12", 0, 12, 0);
	}
	
}
