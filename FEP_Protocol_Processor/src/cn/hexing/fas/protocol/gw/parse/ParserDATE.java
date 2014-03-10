package cn.hexing.fas.protocol.gw.parse;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.hexing.exception.MessageDecodeException;
import cn.hexing.exception.MessageEncodeException;

/**
 * 日期型与十进制字符串转换格式，针对国网格式：A15~A21
 *
 */
public class ParserDATE {	
	/**
	 * BCD->日期型
	 * @param data(按字节倒置的BCD) 		例如：
	 * @param inputFormat(输入日期型格式) 	例如：yyMMdd
	 * @param outputFormat(输入日期型格式) 例如：yyyy-MM-dd
	 * @param len(字符数)					例如：
	 * @return String(日期型)				例如：
	 */
	public static String parseValue(String data,String outputFormat,String inputFormat,int len){
		String rt="";
		try{			
			data=DataSwitch.ReverseStringByByte(data.substring(0,len));	
			if (data.indexOf("EE")>=0||data.indexOf("FF")>=0)
				return rt;
			//如果时间全是零，直接返回
			if(data.length()!=4 && 0==(new BigInteger(data)).intValue())
				return rt;
			SimpleDateFormat df = new SimpleDateFormat(inputFormat);
			Date dt=df.parse(data);
			df = new SimpleDateFormat(outputFormat);
			rt=df.format(dt);
		}catch(Exception e){
			throw new MessageDecodeException(e);
		}
		return rt;
	}
	
	/**
	 * 日期型->BCD
	 * @param data(日期型) 					例如：
	 * @param inputFormat(输入日期型格式) 		例如：yyyy-MM-dd
	 * @param outputFormat(输入日期型格式) 	例如：yyMMdd
	 * @param len(返回十进制字符长度)			例如：
	 * @return String(按字节倒置的BCD)			例如：
	 */
	public static String constructor(String data,String inputFormat,String outputFormat,int len){
		String rt="";
		try{	
			SimpleDateFormat df = new SimpleDateFormat(inputFormat);
			Date dt=df.parse(data);
			df = new SimpleDateFormat(outputFormat);
			rt=df.format(dt);			
	        rt=DataSwitch.ReverseStringByByte(rt);//按字节倒置
		}catch(Exception e){
			throw new MessageEncodeException(e);
		}
		return rt;
	}
}
