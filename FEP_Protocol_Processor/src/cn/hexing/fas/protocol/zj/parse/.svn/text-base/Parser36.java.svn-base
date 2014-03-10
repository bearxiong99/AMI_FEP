package cn.hexing.fas.protocol.zj.parse;

import org.apache.log4j.Logger;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fk.utils.StringUtil;

/**
 * @filename	Parser36.java
 * TODO			A1A2B2B1
 */
public class Parser36 {
	
	private static final Logger log = Logger.getLogger(Parser36.class);

	
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
			if(len!=4){
				ok=false;
			}
			if(ok){
				StringBuffer sb=new StringBuffer();
				sb.append(ParseTool.BytesToHexL(data,loc,2));
				sb.append(ParseTool.BytesToHexL(data,loc+2,2));				
				rt=sb.toString();				
			}
		}catch(Exception e){
			log.error(StringUtil.getExceptionDetailInfo(e));
		}
		return rt;
	}
	
	/**
	 * 组帧
	 * @param frame 存放数据的帧
	 * @param value 数据值	A1A2B2B1
	 * @param loc   存放开始位置
	 * @param len   组帧长度
	 * @param fraction 数据中包含的小数位数
	 * @return
	 */
	public static int constructor(byte[] frame,String value,int loc,int len,int fraction){
		try{
			ParseTool.HexsToBytesCB(frame,loc,value.substring(0,4));
			ParseTool.HexsToBytesCB(frame,loc+2,value.substring(4,8));
		}catch(Exception e){
			throw new MessageEncodeException("错误的 A1A2B2B1 组帧参数:"+value);
		}
		return len;
	}
}
