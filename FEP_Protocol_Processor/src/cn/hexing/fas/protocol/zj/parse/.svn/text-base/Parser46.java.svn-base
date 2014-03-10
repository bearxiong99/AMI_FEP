package cn.hexing.fas.protocol.zj.parse;

import org.apache.log4j.Logger;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fk.utils.StringUtil;

/**
 * BCD码通讯地址解析和组帧
 *
 */
public class Parser46 {
	
	private static final Logger log = Logger.getLogger(Parser46.class);

	
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
			//ok=ParseTool.isValidBCD(data,loc,len);
			if(ok){
				rt=ParseTool.BytesToHexC(data,loc,len,(byte)0xAA);	//规约中0xAA为非法BCD占位符
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
				if(c>='a' && c<='f'){
					continue;
				}
				if(c>='A' && c<='F'){
					continue;
				}
				if(c>='0' && c<='9'){
					continue;
				}
				throw new MessageEncodeException("错误的 BCD 组帧参数:"+value);
			}
			ParseTool.HexsToBytesAA(frame,loc,value,len,(byte)0xAA);
		}catch(Exception e){
			throw new MessageEncodeException("错误的 BCD 组帧参数:"+value);
		}
		
		return len;
	}
}
