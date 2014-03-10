package cn.hexing.fas.protocol.zj.parse;

import cn.hexing.fk.utils.HexDump;

/**
 * 
 * @author gaoll
 *
 * @time 2012-11-27 下午8:14:13
 *
 * @info 时段表解析  HHmmNN
 */
public class Parser66 {


	public static Object parsevalue(byte[] data,int loc){
		
		Object rt = null;
		String value="";
		for(int i = 0 ;i<8;i++){
//			String no=ParseTool.ByteToHex(data[loc]);
//			String period=ParseTool.getTime2Byte(data, loc+1);
//			loc+=3;
//			value += period+":"+no+";";
			value+=(String) Parser12.parsevalue(data, loc, 3, 0)+";";
		}
		rt = value.substring(0, value.length()-1);
		return rt;
	}
	
	public static int constructor(byte[] data,String value,int loc,int len,int fraction){
		String[] periods=value.split("#");
		
		for(int i = 0 ;i<periods.length;i++){
			Parser12.constructor(data, periods[3], loc+i*3, 3, fraction);
		}
		return len;
	}
	public static void main(String[] args) {
		parsevalue(HexDump.toArray("011122023344035566047788051122063344075566087788"), 0);
	
		byte[] data = new byte[24];
		constructor(data , "11:12,1#11:12,1#11:12,1#11:12,1#11:12,1#11:12,1#11:12,1#11:12,1", 0, 24,0);
	}
	
}
