package cn.hexing.fas.protocol.zj.parse;

import org.apache.log4j.Logger;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fas.protocol.gw.parse.DataSwitch;
import cn.hexing.fk.utils.StringUtil;
import cn.hexing.util.HexDump;


/**
 * ���ֽڽ���     
 * ��λ��ǰ����λ�ں�
 * 12 08 11 00 12 ����Ϊ18.0.17.8.18
 * @author Administrator
 *
 */
public class Parser69 {
	
	private static final Logger log = Logger.getLogger(Parser69.class);

	
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
		String sdata=DataSwitch.ReverseStringByByte(HexDump.toHex(data));
		String s="";
		try{
			for(int i=0;i<len-3;i++){
				String dataint=sdata.substring(2*i,2*i+2);
				int ii=Integer.parseInt(dataint,16);
				s=s+ii+".";
			}
		}catch(Exception e){
			log.error(StringUtil.getExceptionDetailInfo(e));
		}
		rt=s.substring(0,s.length()-1);
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
				if(c==','){
					continue;
				}
				if(c==':'){
					continue;
				}
				if(c=='-'){
					continue;
				}
				if(c>='0' && c<='9'){
					continue;
				}
				throw new MessageEncodeException("����� BCD ��֡����:"+value);
			}
			ParseTool.StringToBcds(frame,loc,value,len,(byte)0xAA);
		}catch(Exception e){
			throw new MessageEncodeException("����� BCD ��֡����:"+value);
		}
		
		return len;
	}
}
