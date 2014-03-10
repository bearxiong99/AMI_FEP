package cn.hexing.fas.protocol.gw.parse;

import cn.hexing.exception.MessageDecodeException;
import cn.hexing.exception.MessageEncodeException;

/**
 * 带单位和正负号的十进制与十六进制转换格式
 *
 */
public class ParserA3 {	
	/**
	 * HEX->BCD
	 * @param data(按字节倒置的HEX) 		例如：
	 * @param len(字符数)					例如：
	 * @return String(带单位和正负号BCD)	例如：
	 */
	public static String parseValue(String data,int len){
		String rt="";
		try{			
			data=DataSwitch.ReverseStringByByte(data.substring(0,len));	
			if (data.indexOf("EE")>=0||data.indexOf("FF")>=0)
				return rt;
			String tag=data.substring(0,1);           //单位、符号高半字节
	        int unit=(Integer.parseInt(tag,16)&4)>>2; //第三位为单位标志
	        if ((Integer.parseInt(tag,16)&1)==1)      //最低位等于1为负数
	        	tag="-";
	        else
	        	tag="+"; 
	        rt=(""+unit)+tag+data.substring(1,8);//例如:0+1234567
		}catch(Exception e){
			throw new MessageDecodeException(e);
		}
		return rt;
	}
	
	/**
	 * BCD->HEX
	 * @param data(带单位和正负号BCD) 		例如：
	 * @param len(返回十六进制字符长度)		例如：
	 * @return String(按字节倒置的HEX)		例如：
	 */
	public static String constructor(String data,int len){
		String rt="";
		try{
			int unit=Integer.parseInt(data.substring(0,1))<<2;//单位
			if (data.substring(1,2).equals("-"))//负号+1，正号+0
				unit=unit+1;	
			data=data.substring(2);
	        rt=unit+DataSwitch.StrStuff("0",len-1,data,"left");//不足长度补0
	        rt=DataSwitch.ReverseStringByByte(rt);//按字节倒置
		}catch(Exception e){
			throw new MessageEncodeException(e);
		}
		return rt;
	}
}
