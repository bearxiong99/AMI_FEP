package cn.hexing.fas.protocol.zj.parse;

import java.text.NumberFormat;

import org.apache.log4j.Logger;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fk.utils.StringUtil;

/**
 * 无功补偿装置的配置:NNSSPP.PP1…… SSPP.PP16
 *
 */
public class Parser54 {
	
	private static final Logger log = Logger.getLogger(Parser54.class);

	
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
				sb.append(ParseTool.ByteToHex(data[loc+48]));
				sb.append(",");
				for (int i=15;i>=0;i--){
					sb.append(ParseTool.ByteToHex(data[loc+i*3+2]));
					sb.append(",");
					sb.append(String.valueOf((double)ParseTool.nBcdToDecimal(data,loc+i*3,2)/ParseTool.fraction[2]));
					sb.append(",");
				}				
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
	 * @param value 数据值(前后不能有多余空字符:NN,SS1,PP.PP1……SS16,PP.PP16)
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
				if(c=='.'){
					continue;
				}
				if(c>='0' && c<='9'){
					continue;
				}				
				throw new MessageEncodeException("错误的 NN,SS1,PP.PP1……SS16,PP.PP16 组帧参数:"+value);
			}
			String[] para=value.split(",");			
			frame[loc+48]=ParseTool.StringToBcd(para[0]);		//NN
			for (int i=0;i<16;i++){
				double xx=nf.parse(para[32-i*2]).doubleValue()*ParseTool.fraction[2];			
				ParseTool.IntToBcd(frame,(int)Math.round(xx),loc+i*3,2);	
				frame[loc+i*3+2]=ParseTool.IntToBcd(Integer.parseInt(para[32-i*2-1]));			
			}			
		}catch(Exception e){
			throw new MessageEncodeException("错误的 NN,SS1,PP.PP1……SS16,PP.PP16 组帧参数:"+value);
		}
		
		return len;
	}
}
