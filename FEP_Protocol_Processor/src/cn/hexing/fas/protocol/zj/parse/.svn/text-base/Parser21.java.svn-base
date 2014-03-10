package cn.hexing.fas.protocol.zj.parse;

import org.apache.log4j.Logger;

import cn.hexing.exception.MessageEncodeException;

/**
 * 功率控制用累加标志NNTN1TN2……TN8
 *
 */
public class Parser21 {
	
	private static final Logger log = Logger.getLogger(Parser21.class);

	
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
				sb.append(ParseTool.BytesBitC(data,loc+8,1));
				sb.append(",");
				sb.append(ParseTool.ByteToHex(data[loc+7]));
				sb.append(",");
				sb.append(ParseTool.ByteToHex(data[loc+6]));
				sb.append(",");
				sb.append(ParseTool.ByteToHex(data[loc+5]));
				sb.append(",");
				sb.append(ParseTool.ByteToHex(data[loc+4]));
				sb.append(",");
				sb.append(ParseTool.ByteToHex(data[loc+3]));
				sb.append(",");
				sb.append(ParseTool.ByteToHex(data[loc+2]));
				sb.append(",");
				sb.append(ParseTool.ByteToHex(data[loc+1]));
				sb.append(",");
				sb.append(ParseTool.ByteToHex(data[loc]));			
				rt=sb.toString();
			}
		}catch(Exception e){
			log.error(e.getMessage());
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
				if(c>='0' && c<='9'){
					continue;
				}	
				if(c>='A' && c<='F'){
					continue;
				}
				if(c>='a' && c<='f'){
					continue;
				}
				throw new MessageEncodeException("错误的 NN TN1TN2……TN8 组帧参数:"+value);
			}
			String[] para=value.split(",");
						
			frame[loc+8]=ParseTool.bitToByteC(para[0]);		//NN
			frame[loc]=ParseTool.StringToBcd(para[8]);		//TN8
			frame[loc+1]=ParseTool.StringToBcd(para[7]);
			frame[loc+2]=ParseTool.StringToBcd(para[6]);
			frame[loc+3]=ParseTool.StringToBcd(para[5]);
			frame[loc+4]=ParseTool.StringToBcd(para[4]);
			frame[loc+5]=ParseTool.StringToBcd(para[3]);
			frame[loc+6]=ParseTool.StringToBcd(para[2]);
			frame[loc+7]=ParseTool.StringToBcd(para[1]);
		}catch(Exception e){
			throw new MessageEncodeException("错误的 NN TN1TN2……TN8 组帧参数:"+value);
		}
		
		return len;
	}
}
