package cn.hexing.fas.protocol.zj.parse;

import org.apache.log4j.Logger;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fk.utils.StringUtil;

/**
 * @filename	Parser38.java
 * TODO			ascii码字符
 */
public class Parser38 {
	
	private static final Logger log = Logger.getLogger(Parser38.class);

	
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
			rt=Parser43.parsevalue(data,loc,len,fraction);
		}catch(Exception e){
			log.error(StringUtil.getExceptionDetailInfo(e));
		}
		return rt;
	}
	
	/**
	 * 组帧----按规约默认，长度不够时高位填充0x00
	 * @param frame 存放数据的帧
	 * @param value 数据值
	 * @param loc   存放开始位置
	 * @param len   组帧长度
	 * @param fraction 数据中包含的小数位数
	 * @return
	 */
	public static int constructor(byte[] frame,String value,int loc,int len,int fraction){
		int slen=-1;
		try{
			slen=Parser43.constructor(frame,value,loc,len,fraction);
		}catch(Exception e){
			throw new MessageEncodeException("错误的 ascii码字符 组帧参数:"+value);
		}
		return slen;
	}
}
