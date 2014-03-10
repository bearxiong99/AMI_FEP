package cn.hexing.fas.protocol.gw.parse;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import cn.hexing.exception.MessageDecodeException;
import cn.hexing.exception.MessageEncodeException;

/**
 * 日期型与十六进制字符串转换格式
 *
 */
public class ParserA1 {	
	/**
	 * HEX->日期型(yyyy-MM-dd HH:nn:ss)
	 * @param data(按字节倒置的HEX) 	例如：
	 * @param len(字符数)				例如：
	 * @return String(日期型)			例如：
	 */
	public static String parseValue(String data,int len){
		String rt="";
		try{
			data=DataSwitch.ReverseStringByByte(data.substring(0,len));	
			if (data.indexOf("EE")>=0||data.indexOf("FF")>=0)
				return rt;	
			SimpleDateFormat df = new SimpleDateFormat("yyMMddHHmmss");
			int month=Integer.parseInt(data.substring(2,3),16)&1;//消去星期得到月份的十位数
			//消去星期得到完整的日期型格式
			data=data.substring(0,2)+month+data.substring(3);
			Date dt=df.parse(data);
			df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			rt=df.format(dt);
		}catch(Exception e){
			throw new MessageDecodeException(e);
		}
		return rt;
	}
	
	/**
	 * 日期型(yyyy-MM-dd HH:nn:ss)->HEX
	 * @param data(日期型) 				例如：
	 * @param len(返回十六进制字符长度)		例如：
	 * @return String(按字节倒置的HEX)		例如：
	 */
	public static String constructor(String data,int len){
		String rt="";
		try{	
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date dt=df.parse(data);
			df = new SimpleDateFormat("yyMMddHHmmss");
			rt=df.format(dt);
			Calendar date=Calendar.getInstance();
			date.setTime(dt);
			int week=date.get(Calendar.DAY_OF_WEEK);
			if(week==1)//星期天
				week=7;
			else
				week=week-1;
			int month=Integer.parseInt(rt.substring(2,3));//月份的十位数
			month=week*2+month;//合成星期和月份的十位数
			rt=rt.substring(0,2)+Integer.toString(month, 16)+rt.substring(3);
	        rt=DataSwitch.ReverseStringByByte(rt);//按字节倒置
		}catch(Exception e){
			throw new MessageEncodeException(e);
		}
		return rt;
	}
}
