package cn.hexing.fas.protocol.zj.parse;

import org.apache.log4j.Logger;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fk.utils.StringUtil;

/**
 * BCD码表示的时段起始时间和两个一个字节的格式1:hhmmNNMM
 *
 */
public class Parser53 {
	
	private static final Logger log = Logger.getLogger(Parser53.class);

	
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
				sb.append(ParseTool.ByteToHex(data[loc+3]));	//hh
				sb.append(":");
				sb.append(ParseTool.ByteToHex(data[loc+2]));	//mm
				sb.append(",");
				sb.append(ParseTool.ByteToHex(data[loc+1]));	//NN	
				sb.append(",");
				sb.append(ParseTool.ByteToHex(data[loc]));	//MM		
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
	 * @param value 数据值(前后不能有多余空字符 HH:MM,NN,MM)
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
				if(c>='0' && c<='9'){
					continue;
				}				
				throw new MessageEncodeException("错误的 HH:mm NN MM组帧参数:"+value);
			}
			String[] para=value.split(",");
			String[] time=para[0].split(":");
			
			frame[loc]=ParseTool.StringToBcd(para[2]);//MM
			frame[loc+1]=ParseTool.StringToBcd(para[1]);//NN
			frame[loc+3]=ParseTool.StringToBcd(time[0]);//hh
			frame[loc+2]=ParseTool.StringToBcd(time[1]);//mm
		}catch(Exception e){
			throw new MessageEncodeException("错误的 hh:mm NN MM组帧参数:"+value);
		}
		
		return len;
	}
}
