package cn.hexing.fas.protocol.zj.parse;

import org.apache.log4j.Logger;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fk.utils.StringUtil;

/**
 * BCD码的时间YYMMDDHHmmss,时间包含内容由长度决定  (区别于parser19 顺序写时间和解时间)
 *
 */
public class Parser59 {
	
	private static final Logger log = Logger.getLogger(Parser59.class);

	
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
			ok=ParseTool.isValidBCD(data,loc,len);
			if(ok){
				StringBuffer sb=new StringBuffer();
				//len =5 len=6
				sb.append("20");
				sb.append(ParseTool.ByteToHex(data[loc+0])); 	//YY
				sb.append("-");
				sb.append(ParseTool.ByteToHex(data[loc+1]));	//MM
				if (len>=3){
					sb.append("-");
					sb.append(ParseTool.ByteToHex(data[loc+2]));	//DD
					if (len>=4){
						sb.append(" ");
						sb.append(ParseTool.ByteToHex(data[loc+3]));	//HH
						if (len>=5){
							sb.append(":");
							sb.append(ParseTool.ByteToHex(data[loc+4]));	//mm
							if (len>=6){
								sb.append(":");
								sb.append(ParseTool.ByteToHex(data[loc+5]));	//ss
							}
						}
					}
				}															
				rt=sb.toString();
			}
		}catch(Exception e){
			log.error(StringUtil.getExceptionDetailInfo(e));
		}
		return rt;
	}
	
	/**
	 * 组帧()
	 * @param frame 存放数据的帧
	 * @param value 数据值(前后不能有多余空字符 YYYY-MM-DD HH:mm:ss)
	 * @param loc   存放开始位置
	 * @param len   组帧长度
	 * @param fraction 数据中包含的小数位数
	 * @return
	 */
	public static int constructor(byte[] frame,String value,int loc,int len,int fraction){//没做修改 用到的时候再做修改 
		try{
			//check
			for(int i=0;i<value.length();i++){
				char c=value.charAt(i);
				if(c=='-'){
					continue;
				}
				if(c==':'){
					continue;
				}
				if(c==' '){
					continue;
				}
				if(c>='0' && c<='9'){
					continue;
				}				
				throw new MessageEncodeException("错误的 YYYY-MM-DD HH:mm:ss 组帧参数:"+value);
			}
			String[] dpara=value.split(" ");
			String[] date=dpara[0].split("-");
						
			frame[loc]=ParseTool.StringToBcd(date[0]);
			frame[loc+1]=ParseTool.StringToBcd(date[1]);
			if (len>=3){
				frame[loc+2]=ParseTool.StringToBcd(date[2]);
				if (len>=4){
					String[] time=dpara[1].split(":");
					frame[loc+3]=ParseTool.StringToBcd(time[0]);
					if (len>=5){
						frame[loc+4]=ParseTool.StringToBcd(time[1]);
						if (len>=6){
							frame[loc+5]=ParseTool.StringToBcd(time[2]);
						}
					}
				}
			}										
		}catch(Exception e){
			throw new MessageEncodeException("错误的 YYYY-MM-DD HH:mm:ss 组帧参数:"+value);
		}
		
		return len;
	}
}
