package cn.hexing.fas.protocol.zj.parse;


import org.apache.log4j.Logger;

import cn.hexing.exception.MessageDecodeException;
import cn.hexing.exception.MessageEncodeException;

public class Parser48 {
	
	private static final Logger log = Logger.getLogger(Parser48.class);

	
	/**
	 * N个3个字节的01类型
	 * @param data 数据帧
	 * @param loc  解析开始位置
	 * @param len  解析字节长度
	 * @param fraction 解析后数据包含的小数位数
	 * @return 数据内容
	 */
	public static Object parsevalue(byte[] data,int loc,int len,int fraction){
		Object rt=null;
		try{
			boolean ok=true;
			int pos=loc;
			if(ok){
				StringBuffer sb=new StringBuffer();
				for(int i=0;i<len/3;i++){
					Object v=Parser01.parsevalue(data,pos,2,fraction);
					if(v!=null){
						sb.append(v.toString());
					}else{
						sb.append("null");
					}
					sb.append(",");
					pos+=2;
				}
				rt=sb.toString().substring(0,sb.length()-1);
			}
		}catch(Exception e){
			throw new MessageDecodeException(e);
		}
		return rt;
	}
	
	/**
	 * 
	 * @param frame 字节存放数组
	 * @param value 数据内容
	 * @param loc   存放开始位置
	 * @param len   数据项长度
	 * @param fraction 数据包含小数位数
	 * @return 实际编码长度
	 */
	public static int constructor(byte[] frame,String value,int loc,int len,int fraction){
		try{	
			String[] vs=value.split(",");			
			int pos=loc;
			for(int i=0;i<len/3;i++){								
				Parser01.constructor(frame,vs[i],pos,2,fraction);
				pos+=2;
			}			
		}catch(Exception e){
			//log.error(e.getMessage());
			throw new MessageEncodeException("invalid string:"+value);
		}
		return len;
	}
}
