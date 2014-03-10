package cn.hexing.fas.protocol.zj.parse;

import java.util.Arrays;

import org.apache.log4j.Logger;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fk.utils.StringUtil;

/**
 * 功控执行时间设置MSDSMEDETIN3N2N1N0
 *
 */
public class Parser24 {
	
	private static final Logger log = Logger.getLogger(Parser24.class);

	
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
			ok=ParseTool.isValidBCD(data,loc+4,len-4);
			if(ok){
				StringBuffer sb=new StringBuffer();
				sb.append(ParseTool.ByteToHex(data[loc+8]));	//MS
				sb.append("-");
				sb.append(ParseTool.ByteToHex(data[loc+7]));	//DS
				sb.append(",");
				sb.append(ParseTool.ByteToHex(data[loc+6]));	//ME
				sb.append("-");
				sb.append(ParseTool.ByteToHex(data[loc+5]));	//DE
				sb.append(",");
				int ti=data[loc+4] & 0xff;
				sb.append(ParseTool.ByteToHex(data[loc+4]));	//TI
				sb.append(",");
				switch(ti){
					case 0x04:	//日
						sb.append(ParseTool.ByteToHex(data[loc]));	//N0
						break;
					case 0x05:	//月
//						for(int i=0;i<4;i++){
//							int flag=ParseTool.ByteToFlag(data[loc+i]);
//							if(flag>0){
//								sb.append(String.valueOf((i<<3)+flag));	//N0
//								break;
//							}
//						}
						sb.append(ParseTool.BytesBitC(data,loc,4));
						break;
					case 0x06:	//周
						sb.append(ParseTool.ByteBitC(data[loc]));	//N0 bit0是周日
						break;
					default:
						sb.append("0");
						break;
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
				throw new MessageEncodeException("错误的 MS-DS ME-DE TI N3N2N1N0 组帧参数:"+value);
			}
			
			String[] para=value.split(",");
			String[] sdate=para[0].split("-");
			String[] edate=para[1].split("-");
			
			frame[loc+8]=ParseTool.StringToBcd(sdate[0]);	//MS
			frame[loc+7]=ParseTool.StringToBcd(sdate[1]);
			frame[loc+6]=ParseTool.StringToBcd(edate[0]);	//ME
			frame[loc+5]=ParseTool.StringToBcd(edate[1]);
			
			int ti=Integer.parseInt(para[2]);
			frame[loc+4]=(byte)(ti % 10);	//未检查合法性
			Arrays.fill(frame,loc,loc+3,(byte)0x0);
			switch(ti){
			case 0x04:	//日
				frame[loc]=ParseTool.IntToBcd(Integer.parseInt(para[3]));
				break;
			case 0x05:	//月
				ParseTool.bitToBytesC(frame,para[3],loc);
				break;
			case 0x06:	//周
				ParseTool.bitToBytesC(frame,para[3],loc);	//以后要用可读信息代替之，如周一 周五等
				break;
			default:
				break;
		}			
		}catch(Exception e){
			throw new MessageEncodeException("错误的 MS-DS ME-DE TI N3N2N1N0 组帧参数:"+value);
		}
		
		return len;
	}
}
