package cn.hexing.fas.protocol.zj.parse;

import java.text.NumberFormat;

import org.apache.log4j.Logger;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fk.utils.StringUtil;

/**
 * HEX码数值解析
 *
 */
public class Parser03 {
	
	private static final Logger log = Logger.getLogger(Parser03.class);

	
	/**
	 * Hex to decimal
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
			/*if((data[loc] & 0xff)==0xff){
				ok=ParseTool.isValid(data,loc,len);
			}*/
			ok=ParseTool.isAllFF(data,loc,len);
			if(!ok){
				int val=ParseTool.nByteToInt(data,loc,len);
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
	 * decimal to Hex
	 * @param frame 字节存放数组
	 * @param value 数据内容
	 * @param loc   存放开始位置
	 * @param len   数据项长度
	 * @param fraction 数据包含小数位数
	 * @return
	 */
	public static int constructor(byte[] frame,String value,int loc,int len,int fraction){
		try{
			NumberFormat nf=NumberFormat.getInstance();
			nf.setMaximumFractionDigits(4);			
			
			double val=nf.parse(value).doubleValue();
			if(fraction>0){
				val*=ParseTool.fraction[fraction];
			}
			
			ParseTool.DecimalToBytes(frame,(int)Math.round(val),loc,len);
		}catch(Exception e){
			throw new MessageEncodeException("错误的HEX码组帧参数:"+value);
		}
		
		return len;
	}
}
