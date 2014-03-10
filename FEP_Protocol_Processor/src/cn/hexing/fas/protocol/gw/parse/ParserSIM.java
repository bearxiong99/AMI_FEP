package cn.hexing.fas.protocol.gw.parse;

import cn.hexing.exception.MessageDecodeException;
import cn.hexing.exception.MessageEncodeException;

/**
 * ʮ��������ʮ�������ַ���ת����ʽ
 *
 */
public class ParserSIM {	
	/**
	 * HEX->SIM����
	 * @param data 					���磺
	 * @param len(�ַ���)				���磺
	 * @return String(HEX)			���磺
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
				else if(data.substring(i,i+1).equals("F"))//����
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
	 * SIM����->HEX
	 * @param data(SIM����) 		���磺
	 * @param len(�����ַ�����)	���磺
	 * @return String(HEX)		���磺
	 */
	public static String constructor(String data,int len){
		String rt="";
		try{			
	        rt=DataSwitch.StrStuff("F",len,data,"right");//���㳤�Ȳ�0
		}catch(Exception e){
			throw new MessageEncodeException(e);
		}
		return rt;
	}
}
