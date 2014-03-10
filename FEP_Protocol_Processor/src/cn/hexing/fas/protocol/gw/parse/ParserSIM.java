package cn.hexing.fas.protocol.gw.parse;

import cn.hexing.exception.MessageDecodeException;
import cn.hexing.exception.MessageEncodeException;

/**
 * 十六进制与十六进制字符串转换格式
 *
 */
public class ParserSIM {	
	/**
	 * HEX->SIM卡号
	 * @param data 					例如：
	 * @param len(字符数)				例如：
	 * @return String(HEX)			例如：
	 */
	public static String parseValue(String data,int len){
		String rt="";
		try{
			data=data.substring(0,len);
			for (int i=0;i<data.length();i++){
				if(data.substring(i,i+1).equals("A"))
					rt=rt+",";
				else if(data.substring(i,i+1).equals("B"))
					rt=rt+"#";
				else if(data.substring(i,i+1).equals("F"))//填充符
					rt=rt+"";
				else
					rt=rt+data.substring(i,i+1);									
			}				
		}catch(Exception e){
			throw new MessageDecodeException(e);
		}
		return rt;
	}
	
	/**
	 * SIM卡号->HEX
	 * @param data(SIM卡号) 		例如：
	 * @param len(返回字符长度)	例如：
	 * @return String(HEX)		例如：
	 */
	public static String constructor(String data,int len){
		String rt="";
		try{			
	        rt=DataSwitch.StrStuff("F",len,data,"right");//不足长度补0
		}catch(Exception e){
			throw new MessageEncodeException(e);
		}
		return rt;
	}
}
