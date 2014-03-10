package cn.hexing.fas.protocol.zj.parse;

import java.nio.ByteBuffer;
import java.text.NumberFormat;

import org.apache.log4j.Logger;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fk.utils.StringUtil;
import cn.hexing.util.HexDump;

/**
 * BCD码表示的数值和时间 格式1 HHmm
 *
 */
public class Parser55 {
	
	private static final Logger log = Logger.getLogger(Parser55.class);

	
	/**
	 * 解析
	 * @param data 数据帧
	 * @param loc  解析开始位置
	 * @param len  解析长度
	 * @param fraction 解析后小数位数
	 * @return 数据值 HH:mm,格式1
	 */
	public static Object parsevalue(byte[] data,int loc,int len,int fraction){
		Object rt=null;
		try{			
			boolean ok=true;
			ok=ParseTool.isHaveValidBCD(data,loc,len);
			if(ok){
				StringBuffer sb=new StringBuffer();
				sb.append(ParseTool.ByteToHex(data[loc+1]));	//HH
				sb.append(":");
				sb.append(ParseTool.ByteToHex(data[loc]));	//mm
				sb.append(",");
				sb.append(String.valueOf((double)ParseTool.nBcdToDecimal(data,loc+2,len-2)/ParseTool.fraction[fraction]));	//xx
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
	 * @param value 数据值(前后不能有多余空字符 格式1,HH:MM)
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
				if(c=='.'){
					continue;
				}
				if(c>='0' && c<='9'){
					continue;
				}
				throw new MessageEncodeException("错误的 格式1,HH:mm  组帧参数:"+value);
			}
			
			String[] para=value.split(",");
			String[] time=para[1].split(":");
			
			double xx=nf.parse(para[0]).doubleValue()*ParseTool.fraction[fraction];			
			ParseTool.IntToBcd(frame,(int)Math.round(xx),loc+2,len-2);
			frame[loc]=ParseTool.StringToBcd(time[1]);
			frame[loc+1]=ParseTool.StringToBcd(time[0]);
		}catch(Exception e){
			throw new MessageEncodeException("错误的 格式1,HH:mm 组帧参数:"+value);
		}
		
		return len;
	}
	public static void main(String[] args) {
		byte[] rt=new byte[4];
		ByteBuffer data=HexDump.toByteBuffer("10111234");
		data.get(rt);
		Object str=parsevalue(rt,0,4,2);
		rt=new byte[4];
		str=constructor(rt,"34.12,11:10",0,4,2);
		str=parsevalue(rt,0,4,2);
	}
}
