package cn.hexing.fas.protocol.zj.parse;

import org.apache.log4j.Logger;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fk.utils.StringUtil;

/**
 * 短信中心号码
 *
 */
public class Parser15 {
	
	private static final Logger log = Logger.getLogger(Parser15.class);

	
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
				rt=ParseTool.toPhoneCode(data,loc,len,0xAA);
			}
		}catch(Exception e){
			log.error(StringUtil.getExceptionDetailInfo(e));
		}
		return rt;
	}
	
	/**
	 * 组帧
	 * @param frame 存放数据的帧
	 * @param value 数据值
	 * @param loc   存放开始位置
	 * @param len   组帧长度
	 * @param fraction 数据中包含的小数位数
	 * @return
	 */
	public static int constructor(byte[] frame,String value,int loc,int len,int fraction){
		try{
			//check
			for(int i=0;i<value.length();i++){
				char c=value.charAt(i);										
				if(c>='0' && c<='9'){
					continue;
				}				
				throw new MessageEncodeException("错误的 短信中心号码 组帧参数:"+value);
			}
			ParseTool.StringToBcds(frame,loc,value);
			int flen=((value.length()>>>1)+(value.length() & 0x01));
			for(int i=loc+flen;i<loc+len;i++){
				frame[i]=(byte)0xAA;
			}			
		}catch(Exception e){
			throw new MessageEncodeException("错误的 短信中心号码 组帧参数:"+value);
		}
		return len;
	}
}
