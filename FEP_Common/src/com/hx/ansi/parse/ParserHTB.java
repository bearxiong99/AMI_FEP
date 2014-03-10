package com.hx.ansi.parse;


/**
 * ʮ��������ʮ����ת����ʽ
 *
 */
public class ParserHTB {	
	/**
	 * HEX->BCD
	 * @param data(���ֽڵ��õ�HEX) 	���磺0B0A
	 * @param len(�ַ���)				���磺4
	 * @return String(BCD)			���磺2571
	 */
	public static String parseValue(String data,int len){
		String rt="";
		try{
			data=AnsiDataSwitch.ReverseStringByByte(data.substring(0,len));
			rt=""+Long.parseLong(data,16);
		}catch(Exception e){
			
		}
		return rt;
	}
	
	/**
	 * BCD->HEX
	 * @param data(BCD) 				���磺2571
	 * @param len(����ʮ�������ַ�����)		���磺4
	 * @return String(���ֽڵ��õ�HEX)		���磺0B0A
	 */
	public static String constructor(String data,int len){
		String rt="";
		try{
			rt=(Long.toString(Long.parseLong(data),16)).toUpperCase();
	        rt=AnsiDataSwitch.StrStuff("0",len,rt,"left");//���㳤�Ȳ�0
	        rt=AnsiDataSwitch.ReverseStringByByte(rt);//���ֽڵ���
		}catch(Exception e){
			
		}
		return rt;
	}
}
