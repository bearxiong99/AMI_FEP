package cn.hexing.fas.protocol.gw.parse;

import cn.hexing.exception.MessageDecodeException;
import cn.hexing.exception.MessageEncodeException;

/**
 * 十六进制与十六进制字符串转换格式
 *
 */
public class ParserHEX {	
	/**
	 * HEX->HEX
	 * @param data(按字节倒置的HEX) 	例如：1234
	 * @param len(字符数)				例如：4
	 * @return String(HEX)			例如：3412
	 */
	public static String parseValue(String data,int len){
		String rt="";
		try{
			rt=DataSwitch.ReverseStringByByte(data.substring(0,len));			
		}catch(Exception e){
			throw new MessageDecodeException(e);
		}
		return rt;
	}
	
	/**
	 * HEX->HEX
	 * @param data(HEX) 				例如：1234
	 * @param len(返回十六进制字符长度)		例如：4
	 * @return String(按字节倒置的HEX)		例如：3412
	 */
	public static String constructor(String data,int len){
		String rt="";
		try{			
	        rt=DataSwitch.StrStuff("0",len,data,"left");//不足长度补0
	        rt=DataSwitch.ReverseStringByByte(rt);//按字节倒置
		}catch(Exception e){
			throw new MessageEncodeException(e);
		}
		return rt;
	}
}
