package cn.hexing.fas.protocol.zj.parse;

import org.apache.log4j.Logger;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fk.utils.StringUtil;

/**
 * 主站通讯地址
 *
 */
public class Parser14 {
	
	private static final Logger log = Logger.getLogger(Parser12.class);

	
	/**
	 * 解析
	 * @param data 数据帧
	 * @param loc  解析开始位置
	 * @param len  解析长度
	 * @param fraction 解析后小数位数
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
				int type=ParseTool.BCDToDecimal(data[loc+8]);
				String stype=ParseTool.ByteToHex(data[loc+8]);
				
				switch(type){
					case DataItemParser.COMM_TYPE_SMS:	//SMS					
						rt=stype+","+ParseTool.toPhoneCode(data,loc,8,0xAA);				
						break;
					case DataItemParser.COMM_TYPE_GPRS:					
						int port=ParseTool.nByteToInt(data,loc,2);	//net port					
						String ip=(data[loc+5] & 0xff)+"."+(data[loc+4] & 0xff)
							+"."+(data[loc+3] & 0xff)+"."+(data[loc+2] & 0xff);					
						rt=stype+","+ip+":"+port;
						break;
					case DataItemParser.COMM_TYPE_DTMF:					
						rt=stype+","+ParseTool.toPhoneCode(data,loc,8,0xAA);
						break;
					case DataItemParser.COMM_TYPE_ETHERNET:					
						port=ParseTool.nByteToInt(data,loc,2);	//net port					
						ip=(data[loc+5] & 0xff)+"."+(data[loc+4] & 0xff)
							+"."+(data[loc+3] & 0xff)+"."+(data[loc+2] & 0xff);
						rt=stype+","+ip+":"+port;
						break;
					case DataItemParser.COMM_TYPE_INFRA:					
						break;
					case DataItemParser.COMM_TYPE_RS232:					
						break;
					case DataItemParser.COMM_TYPE_CSD:					
						rt=stype+","+ParseTool.toPhoneCode(data,loc,8,0xAA);
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
	 * @param value 数据值(前后不能有多余空字符 nn,ip)
	 * @param loc   存放开始位置
	 * @param len   组帧长度
	 * @param fraction 数据中包含的小数位数
	 * @return
	 */
	public static int constructor(byte[] frame,String value,int loc,int len,int fraction){
		try{
			String[] para=value.split(",");
			int type=Integer.parseInt(para[0]);
			frame[loc+8]=(byte)type;
			switch(type){
				case DataItemParser.COMM_TYPE_SMS:
					//号码
					ParseTool.StringToBcds(frame,loc,para[1]);
					int flen=((para[1].length()>>>1)+(para[1].length() & 0x01));
					for(int i=loc+flen;i<loc+len-1;i++){
						frame[i]=(byte)0xAA;
					}
					break;
				case DataItemParser.COMM_TYPE_GPRS:
					//IP
					ParseTool.IPToBytes(frame,loc,para[1]);
					frame[loc+6]=(byte)0xAA;
					frame[loc+7]=(byte)0xAA;
					break;
				case DataItemParser.COMM_TYPE_DTMF:
					//号码
					ParseTool.StringToBcds(frame,loc,para[1]);
					flen=((para[1].length()>>>1)+(para[1].length() & 0x01));
					for(int i=loc+flen;i<loc+len-1;i++){
						frame[i]=(byte)0xAA;
					}
					break;
				case DataItemParser.COMM_TYPE_ETHERNET:
					//IP
					ParseTool.IPToBytes(frame,loc,para[1]);
					frame[loc+6]=(byte)0xAA;
					frame[loc+7]=(byte)0xAA;
					break;
				case DataItemParser.COMM_TYPE_INFRA:						
					break;
				case DataItemParser.COMM_TYPE_RS232:						
					break;
				case DataItemParser.COMM_TYPE_CSD:
					//号码
					ParseTool.StringToBcds(frame,loc,para[1]);
					flen=((para[1].length()>>>1)+(para[1].length() & 0x01));
					for(int i=loc+flen;i<loc+len-1;i++){
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
