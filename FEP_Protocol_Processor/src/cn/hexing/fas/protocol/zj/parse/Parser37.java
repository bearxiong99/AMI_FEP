package cn.hexing.fas.protocol.zj.parse;

import org.apache.log4j.Logger;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fk.utils.StringUtil;

/**
 * @filename	Parser37.java
 * TODO			��վͨѶ��ַ
 */
public class Parser37 {
	
	private static final Logger log = Logger.getLogger(Parser37.class);

	
	/**
	 * ����
	 * @param data ����֡
	 * @param loc  ������ʼλ��
	 * @param len  ��������
	 * @param fraction ����8010��MM
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
				int type=fraction;
				
				switch(type){
					case DataItemParser.COMM_TYPE_SMS:	//SMS					
						rt=ParseTool.toPhoneCode(data,loc,8,0xAA);				
						break;
					case DataItemParser.COMM_TYPE_GPRS:					
						int port=ParseTool.nByteToInt(data,loc,2);	//net port					
						String ip=(data[loc+5] & 0xff)+"."+(data[loc+4] & 0xff)
							+"."+(data[loc+3] & 0xff)+"."+(data[loc+2] & 0xff);					
						rt=ip+":"+port;
						break;
					case DataItemParser.COMM_TYPE_DTMF:					
						rt=ParseTool.toPhoneCode(data,loc,8,0xAA);
						break;
					case DataItemParser.COMM_TYPE_ETHERNET:					
						port=ParseTool.nByteToInt(data,loc,2);	//net port					
						ip=(data[loc+5] & 0xff)+"."+(data[loc+4] & 0xff)
							+"."+(data[loc+3] & 0xff)+"."+(data[loc+2] & 0xff);
						rt=ip+":"+port;
						break;
					case DataItemParser.COMM_TYPE_INFRA:					
						break;
					case DataItemParser.COMM_TYPE_RS232:					
						break;
					case DataItemParser.COMM_TYPE_CSD:					
						rt=ParseTool.toPhoneCode(data,loc,8,0xAA);
						break;
					case DataItemParser.COMM_TYPE_RADIO:					
						break;
					default:
						break;
				}	
			}
		}catch(Exception e){
			log.error(StringUtil.getExceptionDetailInfo(e));
		}
		return rt;
	}
	
	/**
	 * ��֡
	 * @param frame ������ݵ�֡
	 * @param value ����ֵ(ǰ�����ж�����ַ� ip�����)
	 * @param loc   ��ſ�ʼλ��
	 * @param len   ��֡����
	 * @param fraction ����8010��MM
	 * @return
	 */
	public static int constructor(byte[] frame,String value,int loc,int len,int fraction){
		try{
			
			int type=fraction;			
			switch(type){
				case DataItemParser.COMM_TYPE_SMS:
					//����
					ParseTool.StringToBcds(frame,loc,value);
					int flen=((value.length()>>>1)+(value.length() & 0x01));
					for(int i=loc+flen;i<loc+len;i++){
						frame[i]=(byte)0xAA;
					}
					break;
				case DataItemParser.COMM_TYPE_GPRS:
					//IP
					ParseTool.IPToBytes(frame,loc,value);
					frame[loc+6]=(byte)0xAA;
					frame[loc+7]=(byte)0xAA;
					break;
				case DataItemParser.COMM_TYPE_DTMF:
					//����
					ParseTool.StringToBcds(frame,loc,value);
					flen=((value.length()>>>1)+(value.length() & 0x01));
					for(int i=loc+flen;i<loc+len;i++){
						frame[i]=(byte)0xAA;
					}
					break;
				case DataItemParser.COMM_TYPE_ETHERNET:
					//IP
					ParseTool.IPToBytes(frame,loc,value);
					frame[loc+6]=(byte)0xAA;
					frame[loc+7]=(byte)0xAA;
					break;
				case DataItemParser.COMM_TYPE_INFRA:						
					break;
				case DataItemParser.COMM_TYPE_RS232:						
					break;
				case DataItemParser.COMM_TYPE_CSD:
					//����
					ParseTool.StringToBcds(frame,loc,value);
					flen=((value.length()>>>1)+(value.length() & 0x01));
					for(int i=loc+flen;i<loc+len;i++){
						frame[i]=(byte)0xAA;
					}
					break;
				case DataItemParser.COMM_TYPE_RADIO:						
					break;
				default:
					break;
			}
		}catch(Exception e){
			throw new MessageEncodeException("����� ��վͨѶ��ַ ��֡����:"+value);
		}
		
		return len;
	}
}
