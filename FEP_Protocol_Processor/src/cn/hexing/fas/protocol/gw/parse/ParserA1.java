package cn.hexing.fas.protocol.gw.parse;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import cn.hexing.exception.MessageDecodeException;
import cn.hexing.exception.MessageEncodeException;

/**
 * ��������ʮ�������ַ���ת����ʽ
 *
 */
public class ParserA1 {	
	/**
	 * HEX->������(yyyy-MM-dd HH:nn:ss)
	 * @param data(���ֽڵ��õ�HEX) 	���磺
	 * @param len(�ַ���)				���磺
	 * @return String(������)			���磺
	 */
	public static String parseValue(String data,int len){
		String rt="";
		try{
			data=DataSwitch.ReverseStringByByte(data.substring(0,len));	
			if (data.indexOf("EE")>=0||data.indexOf("FF")>=0)
				return rt;	
			SimpleDateFormat df = new SimpleDateFormat("yyMMddHHmmss");
			int month=Integer.parseInt(data.substring(2,3),16)&1;//��ȥ���ڵõ��·ݵ�ʮλ��
			//��ȥ���ڵõ������������͸�ʽ
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
	 * ������(yyyy-MM-dd HH:nn:ss)->HEX
	 * @param data(������) 				���磺
	 * @param len(����ʮ�������ַ�����)		���磺
	 * @return String(���ֽڵ��õ�HEX)		���磺
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
			if(week==1)//������
				week=7;
			else
				week=week-1;
			int month=Integer.parseInt(rt.substring(2,3));//�·ݵ�ʮλ��
			month=week*2+month;//�ϳ����ں��·ݵ�ʮλ��
			rt=rt.substring(0,2)+Integer.toString(month, 16)+rt.substring(3);
	        rt=DataSwitch.ReverseStringByByte(rt);//���ֽڵ���
		}catch(Exception e){
			throw new MessageEncodeException(e);
		}
		return rt;
	}
}
