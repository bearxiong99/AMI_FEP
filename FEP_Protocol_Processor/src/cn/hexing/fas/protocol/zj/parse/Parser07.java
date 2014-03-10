package cn.hexing.fas.protocol.zj.parse;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fk.utils.StringUtil;

/**
 * ʱ���������֡ MM-DD hh:mm
 *
 */
public class Parser07 {
	
	private static final Logger log = Logger.getLogger(Parser07.class);

	
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
			ok=ParseTool.isValidBCD(data,loc,len);
			if(ok){
				StringBuffer sb=new StringBuffer();
				sb.append(ParseTool.ByteToHex(data[loc+3]));
				sb.append("-");
				sb.append(ParseTool.ByteToHex(data[loc+2]));
				sb.append(" ");
				sb.append(ParseTool.ByteToHex(data[loc+1]));
				sb.append(":");
				sb.append(ParseTool.ByteToHex(data[loc]));
				//��֤ʱ��Ϸ���
				SimpleDateFormat df = new SimpleDateFormat("MM-dd HH:mm");
				Date dt=df.parse(sb.toString());
				df = new SimpleDateFormat("MM-dd HH:mm");
				rt=df.format(dt);	
				if (!sb.toString().equals(rt))//����ʱ��Ƿ�������Ϊ��
					rt="";;
			}
		}catch(Exception e){
			log.error(StringUtil.getExceptionDetailInfo(e));
		}
		return rt;
	}
	
	/**
	 * ��֡
	 * @param frame ������ݵ�֡
	 * @param value ����ֵ(ǰ�����ж�����ַ�)
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
				if(c==' '){
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
				throw new MessageEncodeException("����� MM-DD HH:mm ��֡����:"+value);
			}
			String[] para=value.split(" ");
			String[] date=para[0].split("-");
			String[] time=para[1].split(":");
			frame[loc]=ParseTool.StringToBcd(time[1]);
			frame[loc+1]=ParseTool.StringToBcd(time[0]);
			frame[loc+2]=ParseTool.StringToBcd(date[1]);
			frame[loc+3]=ParseTool.StringToBcd(date[0]);
		}catch(Exception e){
			throw new MessageEncodeException("����� MM-DD hh:mm ��֡����:"+value);
		}
		return len;
	}
}
