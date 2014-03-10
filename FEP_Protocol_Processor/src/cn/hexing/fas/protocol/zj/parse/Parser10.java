package cn.hexing.fas.protocol.zj.parse;

import org.apache.log4j.Logger;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fk.utils.StringUtil;

/**
 * BCD码表示的时间和数值 YYMMDDHHmm 0XXX
 *
 */
public class Parser10 {
	
	private static final Logger log = Logger.getLogger(Parser10.class);

	
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
				sb.append(ParseTool.ByteToHex(data[loc+6])); 	//YY
				sb.append("-");
				sb.append(ParseTool.ByteToHex(data[loc+5]));	//MM
				sb.append("-");
				sb.append(ParseTool.ByteToHex(data[loc+4]));	//DD
				sb.append(" ");
				sb.append(ParseTool.ByteToHex(data[loc+3]));	//HH
				sb.append(":");
				sb.append(ParseTool.ByteToHex(data[loc+2]));	//mm
				String format = "yyyy-MM-dd HH:mm";
				sb.append(":00");
				String value=ZjDateAssistant.upDateTimeProcess(sb.toString(), format);
				sb = new StringBuffer();
				sb.append(value);
				sb.append(",");
				sb.append(String.valueOf(ParseTool.nBcdToDecimal(data,loc,2)));	//xx
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
	 * @param value 数据值(前后不能有多余空字符 YYYY-MM-DD HH:MM,xxx)
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
				if(c==':'){
					continue;
				}
				if(c=='-'){
					continue;
				}
				if(c=='.'){
					continue;
				}
				if(c==' '){
					continue;
				}
				if(c>='0' && c<='9'){
					continue;
				}				
				throw new MessageEncodeException("错误的 YYMMDDHHmm 0XXX 组帧参数:"+value);
			}
			//组帧暂时不对处理，查看了一下目前用到的，没有组帧用到此格式
			String[] para=value.split(",");			
			int xx=Integer.parseInt(para[1]);
			String[] dpara=para[0].split(" ");
			String[] date=dpara[0].split("-");
			String[] time=dpara[1].split(":");
			
			ParseTool.IntToBcd(frame,xx,loc,2);
			frame[loc+6]=ParseTool.StringToBcd(date[0]);
			frame[loc+5]=ParseTool.StringToBcd(date[1]);
			frame[loc+4]=ParseTool.StringToBcd(date[2]);
			frame[loc+3]=ParseTool.StringToBcd(time[0]);
			frame[loc+2]=ParseTool.StringToBcd(time[1]);
		}catch(Exception e){
			throw new MessageEncodeException("错误的 YYMMDDHHmm 0XXX 组帧参数:"+value);
		}
		
		return len;
	}
}
