package cn.hexing.fas.protocol.zj.parse;

import cn.hexing.fk.utils.HexDump;

/**
 * 
 * @author gaoll
 *
 * @time 2012-11-27 ÏÂÎç7:18:33
 *
 * @info ÏÄÁîÊ±¶Î  YYMMDDHH - YYMMDDHH
 */
public class Parser64 {
	
	public static Object parsevalue(byte[] data,int loc){
		Object rt = null;
		String value="";
		for(int i = 0 ; i <4;i++){
			value+=ParseTool.getTime(data,loc,4);
			loc+=4;
			value+="-";
			value+=ParseTool.getTime(data,loc,4);
			loc+=4;
			value+=":";
		}
		rt=value.substring(0, value.length()-1);
		return rt;
	}
	
	public static int constructor(byte[] data,String value,int loc,int len){
		value=value.replace("-", "");	
		value=value.replace(" ", "");
		String[] periods=value.split(";");
		for(int i=0;i<periods.length;i++){
			String[] period=periods[i].split(",");
			for(int j=0;j<period.length;j++){
				byte[] b_data=HexDump.toArray(period[j]);
				data[loc+8*i+4*j+0]=b_data[0];
				data[loc+8*i+4*j+1]=b_data[1];
				data[loc+8*i+4*j+2]=b_data[2];
				data[loc+8*i+4*j+3]=b_data[3];
			}
		}
		return len;
	}
	
	
	
	public static void main(String[] args) {
		parsevalue(HexDump.toArray("0000000010000000200000003000000040000000500000006000000070000000"), 0);
	
		byte[] frame = new byte[40];
		constructor(frame, "12-11-12 10,12-11-12 10;12-11-12 10,12-11-12 10;12-11-12 10,12-11-12 10;12-11-12 10,12-11-12 10;", 0, 40);
		
		String value="12-11-12 10#12-11-12 10";
		String[] params = value.split("#");
		String[] s = params[0].split("-");
		String[] c = s[2].split(" ");
		System.out.println(c);
		System.out.println("sdds");
		byte[] b = HexDump.toArray(value);
		for(int i = 0;i<b.length;i++){
			System.out.println(b[i]);
		}
	}
	
	
	
}
