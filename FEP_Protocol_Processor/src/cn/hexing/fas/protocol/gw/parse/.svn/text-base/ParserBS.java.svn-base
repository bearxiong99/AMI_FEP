package cn.hexing.fas.protocol.gw.parse;

import cn.hexing.exception.MessageDecodeException;
import cn.hexing.exception.MessageEncodeException;

/**
 * 独立位组合与十六进制字符串转换格式
 *
 */
public class ParserBS {	
	/**
	 * HEX->Bit
	 * @param data(按字节倒置的HEX) 	例如：1234
	 * @param len(字符数)				例如：4
	 * @return String(HEX)			例如：0011010000010010
	 */
	public static String parseValue(String data,int len){
		String rt="";
		try{
			data=DataSwitch.ReverseStringByByte(data.substring(0,len));
			for(int i=0;i<data.length()/2;i++)
				rt=rt+DataSwitch.Fun2HexTo8Bin(data.substring(i*2,i*2+2));
		}catch(Exception e){
			throw new MessageDecodeException(e);
		}
		return rt;
	}	
	/**
	 * Bit->HEX
	 * @param data(HEX) 				例如：0011010000010010
	 * @param len(返回十六进制字符长度)		例如：4
	 * @return String(按字节倒置的HEX)		例如：1234
	 */
	public static String constructor(String data,int len){
		String rt="";
		try{			
			data=DataSwitch.StrStuff("0",len*4,data,"left");//不足长度补0
	        for(int i=0;i<data.length()/8;i++)
	        	rt=rt+DataSwitch.Fun8BinTo2Hex(data.substring(i*8,i*8+8));
	        rt=DataSwitch.ReverseStringByByte(rt);//按字节倒置
		}catch(Exception e){
			throw new MessageEncodeException(e);
		}
		return rt;
	}
	/**
	 * 计算BS格式值中1的个数
	 * @param data(HEX) 				例如：0011010000010010
	 * @return int(1的个数)				例如：5
	 */
	public static int getBSCount(String data){
		int rt=0;
		try{			
	        for (int i=0;i<data.length();i++){
	        	if (data.substring(i,i+1).equals("1"))
	        		rt++;
	        }
		}catch(Exception e){
			throw new MessageEncodeException(e);
		}
		return rt;
	}
}
