package cn.hexing.fas.protocol.zj.parse;

import org.apache.log4j.Logger;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fk.utils.StringUtil;

/**
 * HEX�ַ�����������֡ ��λ��ǰ ��λ�ں�
 *
 */
public class Parser05 {
	
	private static final Logger log = Logger.getLogger(Parser05.class);

	
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
			boolean ok=true;
			/*if((data[loc] & 0xff)==0xff){
				ok=ParseTool.isValid(data,loc,len);
			}*/
			//ok=ParseTool.isAllFF(data,loc,len);
			if(ok){
				rt=ParseTool.BytesToHexC(data,loc,len);
			}
		}catch(Exception e){
			log.error(StringUtil.getExceptionDetailInfo(e));
		}
		return rt;
	}
	
	/**
	 * ��֡
	 * @param frame ������ݵ�֡
	 * @param value ����ֵ
	 * @param loc   ��ſ�ʼλ��
	 * @param len   ��֡����
	 * @param fraction �����а�����С��λ��
	 * @return
	 */
	public static int constructor(byte[] frame,String value,int loc,int len,int fraction){
		try{
			//check
			for(int i=0;i<value.length();i++){
				char c=value.charAt(i);
				if(c>='a' && c<='f'){
					continue;
				}
				if(c>='A' && c<='F'){
					continue;
				}
				if(c>='0' && c<='9'){
					continue;
				}				
				throw new MessageEncodeException("����� Hex ��֡����:"+value);
			}
			ParseTool.HexsToBytes(frame,loc,value);
		}catch(Exception e){
			throw new MessageEncodeException("�����HEX����֡����:"+value);
		}
		return len;
	}
}
