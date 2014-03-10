package cn.hexing.fas.protocol.zj.parse;

import org.apache.log4j.Logger;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fk.utils.StringUtil;

/**
 * @filename	Parser38.java
 * TODO			ascii���ַ�
 */
public class Parser38 {
	
	private static final Logger log = Logger.getLogger(Parser38.class);

	
	/**
	 * ����
	 * @param data ����֡
	 * @param loc  ������ʼλ��
	 * @param len  ��������
	 * @param fraction ������С��λ��
	 * @return ����ֵ
	 */
	public static Object parsevalue(byte[] data,int loc,int len,int fraction){
		Object rt=null;
		try{
			rt=Parser43.parsevalue(data,loc,len,fraction);
		}catch(Exception e){
			log.error(StringUtil.getExceptionDetailInfo(e));
		}
		return rt;
	}
	
	/**
	 * ��֡----����ԼĬ�ϣ����Ȳ���ʱ��λ���0x00
	 * @param frame ������ݵ�֡
	 * @param value ����ֵ
	 * @param loc   ��ſ�ʼλ��
	 * @param len   ��֡����
	 * @param fraction �����а�����С��λ��
	 * @return
	 */
	public static int constructor(byte[] frame,String value,int loc,int len,int fraction){
		int slen=-1;
		try{
			slen=Parser43.constructor(frame,value,loc,len,fraction);
		}catch(Exception e){
			throw new MessageEncodeException("����� ascii���ַ� ��֡����:"+value);
		}
		return slen;
	}
}
