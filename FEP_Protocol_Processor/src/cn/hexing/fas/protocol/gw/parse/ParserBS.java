package cn.hexing.fas.protocol.gw.parse;

import cn.hexing.exception.MessageDecodeException;
import cn.hexing.exception.MessageEncodeException;

/**
 * ����λ�����ʮ�������ַ���ת����ʽ
 *
 */
public class ParserBS {	
	/**
	 * HEX->Bit
	 * @param data(���ֽڵ��õ�HEX) 	���磺1234
	 * @param len(�ַ���)				���磺4
	 * @return String(HEX)			���磺0011010000010010
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
	 * @param data(HEX) 				���磺0011010000010010
	 * @param len(����ʮ�������ַ�����)		���磺4
	 * @return String(���ֽڵ��õ�HEX)		���磺1234
	 */
	public static String constructor(String data,int len){
		String rt="";
		try{			
			data=DataSwitch.StrStuff("0",len*4,data,"left");//���㳤�Ȳ�0
	        for(int i=0;i<data.length()/8;i++)
	        	rt=rt+DataSwitch.Fun8BinTo2Hex(data.substring(i*8,i*8+8));
	        rt=DataSwitch.ReverseStringByByte(rt);//���ֽڵ���
		}catch(Exception e){
			throw new MessageEncodeException(e);
		}
		return rt;
	}
	/**
	 * ����BS��ʽֵ��1�ĸ���
	 * @param data(HEX) 				���磺0011010000010010
	 * @return int(1�ĸ���)				���磺5
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
