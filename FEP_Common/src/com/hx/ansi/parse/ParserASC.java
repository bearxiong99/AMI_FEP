package com.hx.ansi.parse;



/**
 * �ַ�����ASCII��ʮ������ֵת��(���赹��)
 *
 */
public class ParserASC {	
	/**
	 * ASCII->String
	 * @param data(ASCII) 		���磺
	 * @param len(�ַ���)			���磺
	 * @return String(String)	���磺
	 */
	public static String parseValue(String data,int len){
		String rt="";
		try{
			data=data.substring(0,len);
			if((data.length()%2)==0){
	          int byteLen=data.length()/2;  //�ֽڳ���
	          char[] chrList=new char[byteLen];
	          for (int i=0;i<byteLen;i++){
	            chrList[i]=(char)(Integer.parseInt(ParserHTB.parseValue(data.substring(2*i,2*i+2),2)));
	          }
	          rt=(new String(chrList)).trim();
	        }
		}catch(Exception e){
			
		}
		return rt;
	}
	
	/**
	 * String->ASCII
	 * @param data(String) 			���磺
	 * @param len(�����ַ�����)		���磺
	 * @return String(ASCII)		���磺
	 */
	public static String constructor(String data,int len){
		String rt="";
		try{						
	        byte[] bt=data.getBytes();
	        for (int i=0;i<bt.length;i++){
	        	rt=rt+Integer.toHexString(bt[i]).toUpperCase();
	        }
	        rt=AnsiDataSwitch.StrStuff("0",len,rt,"right");//���㳤���Ҳ�0
		}catch(Exception e){
			
		}
		return rt;
	}
}
