package cn.hexing.fas.protocol.zj.parse;

import cn.hexing.fk.utils.HexDump;



/**
 * 
 * @author gaoll
 *
 * @time 2012-11-27 下午7:47:53
 *
 * @info 5个夏令时时段冬令时起始时分结束时分,夏令时起始时分结束时分   HHmm-HHmm
 */
public class Parser65 {
	
	public static Object parsevalue(byte[] data,int loc){
		Object rt = null;
		String value="";
		for(int i = 0 ; i<2;i++){
			String start=ParseTool.getTime2Byte(data, loc);
			String end=ParseTool.getTime2Byte(data, loc+2);
			loc+=4;
			value+=start+"-"+end+":";
		}
		rt = value.substring(0, value.length()-1);
		return rt;
	}
	
	public static int constructor(byte[] data,String value,int loc,int len){
		value=value.replace(":", "");
		String[] periods = value.split(";");
		for(int i=0;i<periods.length;i++){
			String[] period=periods[i].split("-");
			for(int j=0;j<period.length;j++){
				byte[] b_data=HexDump.toArray(period[j]);
				data[loc+4*i+2*j+0]=b_data[0];
				data[loc+4*i+2*j+1]=b_data[1];
			}
		}
		return len;
	}
	
	public static void main(String[] args) {
		byte[] data = new byte[8];
		
		constructor(data, "11:12-12:22;11:12-12:22", 0, 8);
		
	}
}
