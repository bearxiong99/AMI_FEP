package cn.hexing.fas.protocol.zj.parse;

import org.apache.log4j.Logger;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fk.utils.StringUtil;

/**
 * @filename	Parser37.java
 * TODO			主站通讯地址
 */
public class Parser37 {
	
	private static final Logger log = Logger.getLogger(Parser37.class);

	
	/**
	 * 解析
	 * @param data 数据帧
	 * @param loc  解析开始位置
	 * @param len  解析长度
	 * @param fraction 类型8010的MM
	 * @return 数据值
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
	 * 组帧
	 * @param frame 存放数据的帧
	 * @param value 数据值(前后不能有多余空字符 ip或号码)
	 * @param loc   存放开始位置
	 * @param len   组帧长度
	 * @param fraction 类型8010的MM
	 * @return
	 */
	public static int constructor(byte[] frame,String value,int loc,int len,int fraction){
		try{
			
			int type=fraction;			
			switch(type){
				case DataItemParser.COMM_TYPE_SMS:
					//号码
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
					//号码
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
					//号码
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
			throw new MessageEncodeException("错误的 主站通讯地址 组帧参数:"+value);
		}
		
		return len;
	}
}
