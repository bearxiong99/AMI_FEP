package cn.hexing.fas.protocol.zj.parse;

import java.text.NumberFormat;

import cn.hexing.fk.utils.HexDump;

/**
 * 
 * @author gaoll
 *
 * @time 2012-11-27 下午1:47:48
 *
 * @info 温度高低阀值、油压高低阀值
 */
public class Parser61 {
	
	/**
	 * @param data 数据帧
	 * @param loc  解析开始位置
	 * @param len  解析字节长度
	 * @param fraction 解析后数据包含的小数位数
	 * @return 数据内容
	 */
	public static Object parsevalue(byte[] data,int loc,int len,int fraction){
		Object rt = null;
		
		//八个字节校准参数
		loc+=8;
		//四个字节阀值 (前两个字节 低阀值，后两个字节为 高阀值)
		int i_high=Integer.parseInt(HexDump.toHex(data, loc, 2),16);
		loc+=2;
		int i_low=Integer.parseInt(HexDump.toHex(data, loc, 2),16);
		String s_high="";
		String s_low="";
		if(fraction>0){
			NumberFormat snf=NumberFormat.getInstance();
			snf.setMinimumFractionDigits(fraction);
			snf.setGroupingUsed(false);
			s_high=snf.format((double)i_high/ParseTool.fraction[fraction]);
			s_low = snf.format((double)i_low/ParseTool.fraction[fraction]);
		}
		rt = s_high+":"+s_low;
  		return rt;
	}
	
	public static void main(String[] args) {
		Parser61.parsevalue(HexDump.toArray("051105220533054405EB05EE"), 0, 2, 2);
		
		
		
	}
	
	
}
