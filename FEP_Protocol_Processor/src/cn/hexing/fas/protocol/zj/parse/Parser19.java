package cn.hexing.fas.protocol.zj.parse;

import org.apache.log4j.Logger;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fk.utils.StringUtil;

/**
 * BCD���ʱ��YYMMDDHHmmss,ʱ����������ɳ��Ⱦ���
 *
 */
public class Parser19 {
	
	private static final Logger log = Logger.getLogger(Parser19.class);

	
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
			ok=ParseTool.isValidBCD(data,loc,len);
			if(ok){
				String format = "yyyy-MM";
				StringBuffer sb=new StringBuffer();
				sb.append("20");
				int offset=0;
				sb.append(ParseTool.ByteToHex(data[loc+len-1-offset])); 	//YY
				sb.append("-");
				sb.append(ParseTool.ByteToHex(data[loc+len-2-offset]));	//MM

				if (len>=3){
					sb.append("-");
					sb.append(ParseTool.ByteToHex(data[loc+len-3-offset]));	//DD
					format="yyyy-MM-dd";
					if(len==7){
						offset=1;
					}
					if (len>=4){
						sb.append(" ");
						sb.append(ParseTool.ByteToHex(data[loc+len-4-offset]));	//HH
						format="yyyy-MM-dd HH";
						if (len>=5){
							sb.append(":");
							sb.append(ParseTool.ByteToHex(data[loc+len-5-offset]));	//mm
							format="yyyy-MM-dd HH:mm";
							if (len>=6){
								sb.append(":");
								sb.append(ParseTool.ByteToHex(data[loc+len-6-offset]));	//ss
								format="yyyy-MM-dd HH:mm:ss";
							}
						}
					}
				}
				String tempalte="2013-05-01 00:00:00";
				sb.append(tempalte.substring(sb.length()));
				rt=ZjDateAssistant.upDateTimeProcess(sb.toString(), format);
			}
		}catch(Exception e){
			log.error(StringUtil.getExceptionDetailInfo(e));
		}
		return rt;
	}
	
	/**
	 * ��֡
	 * @param frame ������ݵ�֡
	 * @param value ����ֵ(ǰ�����ж�����ַ� YYYY-MM-DD HH:mm:ss)
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
				if(c=='-'){
					continue;
				}
				if(c==':'){
					continue;
				}
				if(c==' '){
					continue;
				}
				if(c>='0' && c<='9'){
					continue;
				}				
				throw new MessageEncodeException("����� YYYY-MM-DD HH:mm:ss ��֡����:"+value);
			}
			value=ZjDateAssistant.downDateTimeProcess(value, "yyyy-MM-dd HH:mm:ss");
			String[] dpara=value.split(" ");
			String[] date=dpara[0].split("-");
			int offset= 0;
			frame[loc+len-(1+offset)]=ParseTool.StringToBcd(date[0]);
			frame[loc+len-(2+offset)]=ParseTool.StringToBcd(date[1]);
			if (len>=3){
				frame[loc+len-3]=ParseTool.StringToBcd(date[2]);
				if (len>=4){
					String[] time=dpara[1].split(":");
					if(len==7){
						frame[loc+len-4]=1;
						offset=1;
					}
					frame[loc+len-(4+offset)]=ParseTool.StringToBcd(time[0]);
					if (len>=5){
						frame[loc+len-(5+offset)]=ParseTool.StringToBcd(time[1]);
						if (len>=6){
							frame[loc+len-(6+offset)]=ParseTool.StringToBcd(time[2]);
						}
					}
				}
			}										
		}catch(Exception e){
			throw new MessageEncodeException("����� YYYY-MM-DD HH:mm:ss ��֡����:"+value);
		}
		return len;
	}
}
