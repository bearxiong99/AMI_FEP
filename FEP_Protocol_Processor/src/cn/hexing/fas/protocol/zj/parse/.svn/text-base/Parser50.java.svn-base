package cn.hexing.fas.protocol.zj.parse;


import java.text.NumberFormat;

import org.apache.log4j.Logger;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fk.utils.StringUtil;

public class Parser50 {
	
	private static final Logger log = Logger.getLogger(Parser50.class);

	
	/**
	 * BCD码表示的数值和时间 格式1 DDHHmm
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
			ok=ParseTool.isHaveValidBCD(data,loc,len);
			if(ok){
				StringBuffer sb=new StringBuffer();
				sb.append(ParseTool.ByteToHex(data[loc+2]));	//DD
				sb.append(" ");
				sb.append(ParseTool.ByteToHex(data[loc+1]));	//HH
				sb.append(":");
				sb.append(ParseTool.ByteToHex(data[loc]));	//mm
				sb.append(",");
				sb.append(String.valueOf((double)ParseTool.nBcdToDecimal(data,loc+3,len-3)/ParseTool.fraction[fraction]));	//xx
				rt=sb.toString();
			}
		}catch(Exception e){
			log.error(StringUtil.getExceptionDetailInfo(e));
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
			NumberFormat nf=NumberFormat.getInstance();
			nf.setMaximumFractionDigits(4);
			
			//check
			for(int i=0;i<value.length();i++){
				char c=value.charAt(i);
				if(c==' '){
					continue;
				}
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
				throw new MessageEncodeException("错误的 格式1,DD HH:mm  组帧参数:"+value);
			}
			
			String[] para=value.split(",");
			String[] date=para[1].split(" ");
			String[] time=para[1].split(":");
			
			double xx=nf.parse(para[0]).doubleValue()*ParseTool.fraction[fraction];			
			ParseTool.IntToBcd(frame,(int)Math.round(xx),loc+3,len-3);
			frame[loc]=ParseTool.StringToBcd(time[1]);
			frame[loc+1]=ParseTool.StringToBcd(time[0]);
			frame[loc+2]=ParseTool.StringToBcd(date[0]);
		}catch(Exception e){
			throw new MessageEncodeException("错误的 格式1,DD HH:mm 组帧参数:"+value);
		}
		
		return len;
	}
}
