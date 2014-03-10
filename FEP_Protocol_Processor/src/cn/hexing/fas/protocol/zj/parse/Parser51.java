package cn.hexing.fas.protocol.zj.parse;

import java.text.NumberFormat;

import org.apache.log4j.Logger;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fk.utils.StringUtil;

/**
 * BCD码表示的时间和数值 YYMMDDHHmm 格式1
 *
 */
public class Parser51 {
	
	private static final Logger log = Logger.getLogger(Parser51.class);

	
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
			ok=ParseTool.isHaveValidBCD(data,loc,len);
			if(ok){
				StringBuffer sb=new StringBuffer();
				sb.append("20");
				sb.append(ParseTool.ByteToHex(data[loc+len-1])); 	//YY
				sb.append("-");
				sb.append(ParseTool.ByteToHex(data[loc+len-2]));	//MM
				sb.append("-");
				sb.append(ParseTool.ByteToHex(data[loc+len-3]));	//DD
				sb.append(" ");
				sb.append(ParseTool.ByteToHex(data[loc+len-4]));	//HH
				sb.append(":");
				sb.append(ParseTool.ByteToHex(data[loc+len-5]));	//mm
				sb.append(",");
				sb.append(String.valueOf((double)ParseTool.nBcdToDecimal(data,loc,len-5)/ParseTool.fraction[fraction]));	//xx
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
	 * @param value 数据值(前后不能有多余空字符 YYYY-MM-DD HH:MM,格式1)
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
				if(c==':'){
					continue;
				}
				if(c=='-'){
					continue;
				}
				if(c==' '){
					continue;
				}
				if(c=='.'){
					continue;
				}
				if(c>='0' && c<='9'){
					continue;
				}
				throw new MessageEncodeException("错误的 YYYY-MM-DD HH:mm 格式1 组帧参数:"+value);
			}
			
			String[] para=value.split(",");
			String[] dpara=para[0].split(" ");
			String[] date=dpara[0].split("-");
			String[] time=dpara[1].split(":");
			
			double xx=nf.parse(para[1]).doubleValue()*ParseTool.fraction[fraction];			
			ParseTool.IntToBcd(frame,(int)Math.round(xx),loc,len-5);
			frame[loc+len-1]=ParseTool.StringToBcd(date[0]);
			frame[loc+len-2]=ParseTool.StringToBcd(date[1]);
			frame[loc+len-3]=ParseTool.StringToBcd(date[2]);
			frame[loc+len-4]=ParseTool.StringToBcd(time[0]);
			frame[loc+len-5]=ParseTool.StringToBcd(time[1]);
		}catch(Exception e){
			throw new MessageEncodeException("错误的 YYYY-MM-DD HH:mm 格式1 组帧参数:"+value);
		}
		
		return len;
	}
}
