package cn.hexing.fas.protocol.zj.parse;

import java.text.NumberFormat;

import org.apache.log4j.Logger;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fk.utils.StringUtil;

/**
 * 本次购电量SNNNNNNNXX
 *
 */
public class Parser25 {
	
	private static final Logger log = Logger.getLogger(Parser25.class);

	
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
				StringBuffer sb=new StringBuffer();
				boolean bn=((data[loc+len-1] & 0x10)>0);
				int val=ParseTool.nBcdToDecimalS(data,loc+1,4);
				if(bn){
					val=-val;
				}
				sb.append(String.valueOf(val));
				sb.append(",");
				sb.append(ParseTool.ByteToHex(data[loc]));
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
	 * @param value 数据值
	 * @param loc   存放开始位置
	 * @param len   组帧长度
	 * @param fraction 数据中包含的小数位数
	 * @return
	 */
	public static int constructor(byte[] frame,String value,int loc,int len,int fraction){
		try{
			NumberFormat nf=NumberFormat.getInstance();
			nf.setMaximumFractionDigits(4);			
			//check
			for(int i=0;i<value.length();i++){
				char c=value.charAt(i);
				if(c==','){
					continue;
				}
				if(c=='-'){
					continue;
				}
				if(c=='.'){
					continue;
				}
				if(c>='0' && c<='9'){
					continue;
				}				
				throw new MessageEncodeException("错误的 SNNNNNNN XX 组帧参数:"+value);
			}
			String[] para=value.split(",");
			
			int val=nf.parse(para[0]).intValue();			
			boolean bn=(val<0);
			if(bn){
				val=-val;
			}
			ParseTool.IntToBcd(frame,val,loc+1,4);
			if(bn){
				frame[loc+4]=(byte)((frame[loc+4] & 0xf) | 0x10);
			}
			
			frame[loc]=ParseTool.StringToBcd(para[1]);
		}catch(Exception e){
			throw new MessageEncodeException("错误的 SNNNNNNN XX 组帧参数:"+value);
		}
		
		return len;
	}
}
