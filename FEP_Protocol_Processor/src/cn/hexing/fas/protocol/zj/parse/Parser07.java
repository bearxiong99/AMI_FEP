package cn.hexing.fas.protocol.zj.parse;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fk.utils.StringUtil;

/**
 * 时间解析及组帧 MM-DD hh:mm
 *
 */
public class Parser07 {
	
	private static final Logger log = Logger.getLogger(Parser07.class);

	
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
				sb.append(ParseTool.ByteToHex(data[loc+3]));
				sb.append("-");
				sb.append(ParseTool.ByteToHex(data[loc+2]));
				sb.append(" ");
				sb.append(ParseTool.ByteToHex(data[loc+1]));
				sb.append(":");
				sb.append(ParseTool.ByteToHex(data[loc]));
				//验证时间合法性
				SimpleDateFormat df = new SimpleDateFormat("MM-dd HH:mm");
				Date dt=df.parse(sb.toString());
				df = new SimpleDateFormat("MM-dd HH:mm");
				rt=df.format(dt);	
				if (!sb.toString().equals(rt))//传入时间非法，返回为空
					rt="";;
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
				if(c==' '){
					continue;
				}
				if(c==':'){
					continue;
				}
				if(c=='-'){
					continue;
				}
				if(c>='0' && c<='9'){
					continue;
				}				
				throw new MessageEncodeException("错误的 MM-DD HH:mm 组帧参数:"+value);
			}
			String[] para=value.split(" ");
			String[] date=para[0].split("-");
			String[] time=para[1].split(":");
			frame[loc]=ParseTool.StringToBcd(time[1]);
			frame[loc+1]=ParseTool.StringToBcd(time[0]);
			frame[loc+2]=ParseTool.StringToBcd(date[1]);
			frame[loc+3]=ParseTool.StringToBcd(date[0]);
		}catch(Exception e){
			throw new MessageEncodeException("错误的 MM-DD hh:mm 组帧参数:"+value);
		}
		return len;
	}
}
