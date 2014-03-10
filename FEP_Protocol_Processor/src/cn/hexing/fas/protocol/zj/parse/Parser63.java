package cn.hexing.fas.protocol.zj.parse;

import java.text.NumberFormat;

import cn.hexing.fk.utils.HexDump;

/**
 * 
 * @author gaoll
 *
 * @time 2012-11-27 下午7:18:19
 *
 * @info 经度，温度， 海拔高度
 */
public class Parser63 {
	
	
	public static Object parsevalue(byte[] data, int loc){
		Object rt = null;

		int i_Longitude=ParseTool.nByteToInt(data, loc, 4);
		loc+=4;
		int i_Latitude=ParseTool.nByteToIntS(data, loc, 4);
		loc+=4;
		int i_Elevation=ParseTool.nByteToInt(data, loc, 4);
		int fraction = 6;
		NumberFormat snf=NumberFormat.getInstance();
		String s_Longitude="";
		String s_Latitude="";
		String s_Elevation="";
		snf.setMinimumFractionDigits(fraction);
		snf.setGroupingUsed(false);
		s_Longitude=snf.format((double)i_Longitude/ParseTool.fraction[fraction]);
		s_Latitude=snf.format((double)i_Latitude/ParseTool.fraction[fraction]);
		s_Elevation=snf.format((double)i_Elevation/ParseTool.fraction[fraction]);
		
		rt = s_Longitude+":"+s_Latitude+":"+s_Elevation;
		
		return rt;
		
	}
	
	public static void main(String[] args) {
		parsevalue(HexDump.toArray("120000011200000112000001"), 0);
	}

	
}
