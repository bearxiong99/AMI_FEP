package cn.hexing.fas.protocol.zj.parse;

import org.apache.log4j.Logger;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fk.utils.StringUtil;

/**
 * ʱ���������֡ YYMMDD,WW
 *
 */
public class Parser08 {
	
	private static final Logger log = Logger.getLogger(Parser08.class);

	
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
				sb.append("20");
				sb.append(ParseTool.ByteToHex(data[loc+3]));
				sb.append("-");
				sb.append(ParseTool.ByteToHex(data[loc+2]));
				sb.append("-");
				sb.append(ParseTool.ByteToHex(data[loc+1]));
				String format = "yyyy-MM-dd";
				sb.append(" 00:00:00");
				String value=ZjDateAssistant.upDateTimeProcess(sb.toString(), format);
				sb = new StringBuffer();
				sb.append(value);
				sb.append(",");
				sb.append(ParseTool.ByteToHex(data[loc]));

				rt=sb.toString();
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
				if(c==','){
					continue;
				}
				if(c=='-'){
					continue;
				}				
				if(c>='0' && c<='9'){
					continue;
				}				
				throw new MessageEncodeException("����� YYYY-MM-DD ��֡����:"+value);
			}
			value=ZjDateAssistant.downDateTimeProcess(value+" 00:00:00", "yyyy-MM-dd hh:mm:ss");
			String[] para=value.split(",");
			String[] date=para[0].split("-");			
			frame[loc]=ParseTool.StringToBcd(para[1]);
			frame[loc+1]=ParseTool.StringToBcd(date[2]);
			frame[loc+2]=ParseTool.StringToBcd(date[1]);
			frame[loc+3]=ParseTool.StringToBcd(date[0].substring(date[0].length()-2,date[0].length()));
		}catch(Exception e){
			throw new MessageEncodeException("����� YYYY-MM-DD,WW ��֡����:"+value);
		}
		return len;
	}
	public static void main(String[] args) {
		System.setProperty("bp.isIranTime", "true");
		constructor(null, "2012-05-18", 0,0,0);
	}
}
