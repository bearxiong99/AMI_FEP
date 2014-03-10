package cn.hexing.fas.protocol.zj.parse;

import cn.hexing.fk.utils.HexDump;

/**
 * 
 * @author gaoll
 *
 * @time 2012-11-27 下午8:32:01
 *
 * @info 特殊节假日日期及日时段表号
 */
public class Parser67 {
	
	public static Object parsevalue(byte[] data ,int loc){
		Object rt = null;
		
		String value="";
		for(int i = 0;i<8;i++){
			String no = ParseTool.ByteToHex(data[loc]);
			String period = ParseTool.getTime(data, loc, 3);
			value+=period+":"+no+";";
			loc+=4;
		}
		rt = value.substring(0, value.length()-1);
		return rt;
	}
	
	public static int constructor(byte[] data,String value,int loc,int len,int fraction){
		
		value=value.replace("-", "");
		value=value.replace(" ", "");
		String[] periods = value.split(";");
		for(int i=0;i<periods.length;i++){
			byte[] datas=HexDump.toArray(periods[i]);
			for(int j=0;j<datas.length;j++){
				data[loc+i*4+j] = datas[j];
			}
		}
		return len;
	}
	
	public static void main(String[] args) {
		parsevalue(HexDump.toArray("0000000010000000200000003000000040000000500000006000000070000000"), 0);
	
		byte[] data = new byte[32];
		constructor(data, "12-11-10 15;12-11-10 15;12-11-10 15;12-11-10 15;12-11-10 15;12-11-10 15;12-11-10 15;12-11-10 15", 0, 32, 0);
	}
	
	
	
}
