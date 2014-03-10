package cn.hexing.fas.protocol.zj.parse;


import org.apache.log4j.Logger;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fk.utils.StringUtil;

/**
 * @filename	Parser44.java
 * TODO			bit位码
 */
public class Parser44 {
	
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
			rt=ParseTool.BytesBit(data,loc,len);
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
			int vlen=value.length();
			for(int i=0;i<vlen;i++){
				if((value.substring(i,i+1).equals("0")) || (value.substring(i,i+1).equals("1"))){
					//
				}else{
					throw new MessageEncodeException("错误的 bit位码 组帧参数:"+value);
				}
			}
			if((vlen & 0x7)==0){//是8的整数倍数
				int blen=0;
				int iloc=loc+len-1;
				while(blen<vlen){
					frame[iloc]=ParseTool.bitToByte(value.substring(blen,blen+8));
					blen+=8;
					iloc--;
				}
				slen=len;
			}
		}catch(Exception e){
			throw new MessageEncodeException("错误的 bit位码 组帧参数:"+value);
		}
		return slen;
	}
}
