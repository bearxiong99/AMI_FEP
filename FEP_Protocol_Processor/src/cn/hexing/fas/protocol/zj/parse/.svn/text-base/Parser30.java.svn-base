package cn.hexing.fas.protocol.zj.parse;

import org.apache.log4j.Logger;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fk.utils.StringUtil;

/**
 * 测量点设置MMYYPPPPPP
 *
 */
public class Parser30 {
	
	private static final Logger log = Logger.getLogger(Parser30.class);

	
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
				sb.append(ParseTool.ByteToHex(data[loc+4]));	//MM
				sb.append(",");
				sb.append(ParseTool.ByteToHex(data[loc+3]));	//YY
				sb.append(",");
				sb.append(String.valueOf(ParseTool.nBcdToDecimal(data,loc,3)));
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
			//check
			for(int i=0;i<value.length();i++){
				char c=value.charAt(i);
				if(c==','){
					continue;
				}				
				if(c>='0' && c<='9'){
					continue;
				}
				if(c>='A' && c<='F'){
					continue;
				}
				if(c>='a' && c<='f'){
					continue;
				}
				throw new MessageEncodeException("错误的 MM YY PPPPPP 组帧参数:"+value);
			}
			String[] para=value.split(",");
			ParseTool.IntToBcd(frame,Integer.parseInt(para[2]),loc,3);
			frame[loc+3]=ParseTool.IntToBcd(Integer.parseInt(para[1]));	//YY
			frame[loc+4]=ParseTool.IntToBcd(Integer.parseInt(para[0]));	//MM
		}catch(Exception e){
			throw new MessageEncodeException("错误的 MM YY PPPPPP 组帧参数:"+value);
		}
		
		return len;
	}
}
