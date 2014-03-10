package cn.hexing.fas.protocol.zj.parse;

import org.apache.log4j.Logger;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fk.utils.StringUtil;

/**
 * 时间解析及组帧 YYMMDD,WW
 *
 */
public class Parser08 {
	
	private static final Logger log = Logger.getLogger(Parser08.class);

	
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
				sb.append("20");
				sb.append(ParseTool.ByteToHex(data[loc+3]));
				sb.append("-");
				sb.append(ParseTool.ByteToHex(data[loc+2]));
				sb.append("-");
				sb.append(ParseTool.ByteToHex(data[loc+1]));
				String format = "yyyy-MM-dd";
				sb.append(" 00:00:00");
				String value=ZjDateAssistant.upDateTimeProcess(sb.toString(), format);
				sb = new StringBuffer();
				sb.append(value);
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
	 * @param value 数据值(前后不能有多余空字符)
	 * @param loc   存放开始位置
	 * @param len   组帧长度
	 * @param fraction 数据中包含的小数位数
	 * @return
	 */
	public static int constructor(byte[] frame,String value,int loc,int len,int fraction){
		try{
			//check
			for(int i=0;i<value.length();i++){
				char c=value.charAt(i);
				if(c==','){
					continue;
				}
				if(c=='-'){
					continue;
				}				
				if(c>='0' && c<='9'){
					continue;
				}				
				throw new MessageEncodeException("错误的 YYYY-MM-DD 组帧参数:"+value);
			}
			value=ZjDateAssistant.downDateTimeProcess(value+" 00:00:00", "yyyy-MM-dd hh:mm:ss");
			String[] para=value.split(",");
			String[] date=para[0].split("-");			
			frame[loc]=ParseTool.StringToBcd(para[1]);
			frame[loc+1]=ParseTool.StringToBcd(date[2]);
			frame[loc+2]=ParseTool.StringToBcd(date[1]);
			frame[loc+3]=ParseTool.StringToBcd(date[0].substring(date[0].length()-2,date[0].length()));
		}catch(Exception e){
			throw new MessageEncodeException("错误的 YYYY-MM-DD,WW 组帧参数:"+value);
		}
		return len;
	}
	public static void main(String[] args) {
		System.setProperty("bp.isIranTime", "true");
		constructor(null, "2012-05-18", 0,0,0);
	}
}
