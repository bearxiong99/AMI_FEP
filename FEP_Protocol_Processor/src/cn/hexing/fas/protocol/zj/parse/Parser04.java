package cn.hexing.fas.protocol.zj.parse;

import java.text.NumberFormat;

import org.apache.log4j.Logger;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fk.utils.StringUtil;

/**
 * 带符号位的HEX码数值解析（最高位是符合位）
 *
 */
public class Parser04 {
	private static final Logger log = Logger.getLogger(Parser04.class);

	
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
			ok=ParseTool.isAllFF(data,loc,len);
			if(!ok){
				boolean bn=((data[loc+len-1] & 0x80)>0);
				int val=ParseTool.nByteToIntS(data,loc,len);
				if(bn){
					val=-val;
				}
				if(fraction>0){
					NumberFormat snf=NumberFormat.getInstance();
					snf.setMinimumFractionDigits(fraction);
					snf.setMinimumIntegerDigits(1);
					snf.setGroupingUsed(false);
					rt=snf.format((double)val/ParseTool.fraction[fraction]);
				}else{
					rt=new Integer(val);
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
	 * @param value 数据值
	 * @param loc   存放开始位置
	 * @param len   组帧长度
	 * @param fraction 数据中包含的小数位数
	 */
	public static int constructor(byte[] frame,String value,int loc,int len,int fraction){
		try{
			NumberFormat nf=NumberFormat.getInstance();
			nf.setMaximumFractionDigits(4);			
			String sn="";
			if(value.substring(0,1).equals("+")){
				sn=value.substring(1,value.length());
			}else{
				sn=value;
			}
			double val=nf.parse(sn).doubleValue();
			if(fraction>0){
				val*=ParseTool.fraction[fraction];
			}
			boolean bn=(val<0);
			if(bn){
				val=-val;
			}
			ParseTool.DecimalToBytes(frame,(int)Math.round(val),loc,len);
			if(bn){
				frame[loc+len-1]=(byte)(frame[loc+len-1]  | 0x80);
			}
		}catch(Exception e){
			throw new MessageEncodeException("错误的HEX码组帧参数:"+value);
		}
		return len;
	}
}
