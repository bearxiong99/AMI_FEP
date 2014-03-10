package cn.hexing.fas.protocol.zj.parse;


import java.text.NumberFormat;

import org.apache.log4j.Logger;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fk.utils.StringUtil;

public class Parser49 {
	
	private static final Logger log = Logger.getLogger(Parser49.class);

	
	/**
	 * BCD���ʾ����ֵ��ʱ�� ��ʽ1(���λ����λ) HHmm
	 * @param data ����֡
	 * @param loc  ������ʼλ��
	 * @param len  �����ֽڳ���
	 * @param fraction ���������ݰ�����С��λ��
	 * @return ��������
	 */
	public static Object parsevalue(byte[] data,int loc,int len,int fraction){
		Object rt=null;
		try{			
			boolean ok=true;
			ok=ParseTool.isHaveValidBCD(data,loc,len);
			if(ok){
				StringBuffer sb=new StringBuffer();
				sb.append(ParseTool.ByteToHex(data[loc+1]));	//HH
				sb.append(":");
				sb.append(ParseTool.ByteToHex(data[loc]));	//mm
				sb.append(",");
				boolean bn=((data[loc+len-1] & 0x10)>0);
				int val=ParseTool.nBcdToDecimalS(data,loc+2,len-2);
				if(bn){
					val=-val;
				}			
				sb.append(String.valueOf((double)val/ParseTool.fraction[fraction]));	//xx
				rt=sb.toString();
			}
		}catch(Exception e){
			log.error(StringUtil.getExceptionDetailInfo(e));
		}
		return rt;
	}
	
	/**
	 * 
	 * @param frame �ֽڴ������
	 * @param value ��������
	 * @param loc   ��ſ�ʼλ��
	 * @param len   �������
	 * @param fraction ���ݰ���С��λ��
	 * @return ʵ�ʱ��볤��
	 */
	public static int constructor(byte[] frame,String value,int loc,int len,int fraction){
		try{
			NumberFormat nf=NumberFormat.getInstance();
			nf.setMaximumFractionDigits(4);
			
			//check
			for(int i=0;i<value.length();i++){
				char c=value.charAt(i);
				if(c==','){
					continue;
				}
				if(c==':'){
					continue;
				}
				if(c=='-'){
					continue;
				}
				if(c=='.'){
					continue;
				}
				if(c>='0' && c<='9'){
					continue;
				}
				throw new MessageEncodeException("����� ��ʽ1(���λ����λ),HH:mm  ��֡����:"+value);
			}
			
			String[] para=value.split(",");
			String[] time=para[1].split(":");
			
			Parser02.constructor(frame,para[1],loc+2,len-2,fraction);
			frame[loc]=ParseTool.StringToBcd(time[1]);
			frame[loc+1]=ParseTool.StringToBcd(time[0]);
		}catch(Exception e){
			throw new MessageEncodeException("����� ��ʽ1(���λ����λ),HH:mm ��֡����:"+value);
		}
		
		return len;
	}
}
