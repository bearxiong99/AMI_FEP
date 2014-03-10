package cn.hexing.fas.protocol.zj.parse;

import java.nio.ByteBuffer;
import java.text.NumberFormat;

import org.apache.log4j.Logger;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fk.utils.StringUtil;
import cn.hexing.util.HexDump;

/**
 * BCD码表示的数值和时间：XXXXXX,XXXX.XX,XXXX.XX,XXXXXX,XXXXXX,XXX.X,MMDDHHmm,XXX.X,MMDDHHmm
 *
 */
public class Parser58 {
	
	private static final Logger log = Logger.getLogger(Parser58.class);

	
	/**
	 * 解析
	 * @param data 数据帧
	 * @param loc  解析开始位置
	 * @param len  解析长度
	 * @param fraction 解析后小数位数
	 * @return 数据值 HH:mm,格式1
	 */
	public static Object parsevalue(byte[] data,int loc,int len,int fraction){
		Object rt=null;
		try{			
			boolean ok=true;
			ok=ParseTool.isHaveValidBCD(data,loc,len);
			if(ok){
				StringBuffer sb=new StringBuffer();
				sb.append(ParseTool.ByteToHex(data[loc+3]));	//MM
				sb.append("-");
				sb.append(ParseTool.ByteToHex(data[loc+2]));	//DD
				sb.append(" ");
				sb.append(ParseTool.ByteToHex(data[loc+1]));	//HH
				sb.append(":");
				sb.append(ParseTool.ByteToHex(data[loc]));	//mm
				sb.append(",");
				sb.append(String.valueOf((double)ParseTool.nBcdToDecimal(data,loc+4,2)/ParseTool.fraction[1]));	//xxx.x
				sb.append(",");
				sb.append(ParseTool.ByteToHex(data[loc+9]));	//MM
				sb.append("-");
				sb.append(ParseTool.ByteToHex(data[loc+8]));	//DD
				sb.append(" ");
				sb.append(ParseTool.ByteToHex(data[loc+7]));	//HH
				sb.append(":");
				sb.append(ParseTool.ByteToHex(data[loc+6]));	//mm
				sb.append(",");
				sb.append(String.valueOf((double)ParseTool.nBcdToDecimal(data,loc+10,2)/ParseTool.fraction[1]));	//xxx.x
				sb.append(",");
				sb.append(String.valueOf((double)ParseTool.nBcdToDecimal(data,loc+12,3)/ParseTool.fraction[0]));//xxxxxx
				sb.append(",");
				sb.append(String.valueOf((double)ParseTool.nBcdToDecimal(data,loc+15,3)/ParseTool.fraction[0]));//xxxxxx
				sb.append(",");
				sb.append(String.valueOf((double)ParseTool.nBcdToDecimal(data,loc+18,3)/ParseTool.fraction[2]));//xxxx.xx
				sb.append(",");
				sb.append(String.valueOf((double)ParseTool.nBcdToDecimal(data,loc+21,3)/ParseTool.fraction[2]));//xxxx.xx
				sb.append(",");
				sb.append(String.valueOf((double)ParseTool.nBcdToDecimal(data,loc+24,3)/ParseTool.fraction[0]));//xxxxxx
				rt=sb.toString();
			}
		}catch(Exception e){
			log.error(StringUtil.getExceptionDetailInfo(e));
		}
		return rt;
	}
	
	/**
	 * 组帧(07版全国电表规约电影合格率统计数据专用，只有解析，没有组包)
	 * @param frame 存放数据的帧
	 * @param value 数据值(前后不能有多余空字符 XXXXXX,XXXX.XX,XXXX.XX,XXXXXX,XXXXXX,XXX.X,MMDDHHmm,XXX.X,MMDDHHmm)
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
				if(c==':'){
					continue;
				}
				if(c=='.'){
					continue;
				}
				if(c>='0' && c<='9'){
					continue;
				}
				throw new MessageEncodeException("错误的 XXXXXX,XXXX.XX,XXXX.XX,XXXXXX,XXXXXX,XXX.X,MMDDHHmm,XXX.X,MMDDHHmm 组帧参数:"+value);
			}
			
		}catch(Exception e){
			throw new MessageEncodeException("错误的 XXXXXX,XXXX.XX,XXXX.XX,XXXXXX,XXXXXX,XXX.X,MMDDHHmm,XXX.X,MMDDHHmm 组帧参数:"+value);
		}
		
		return len;
	}
	public static void main(String[] args) {
		byte[] rt=new byte[27];
		ByteBuffer data=HexDump.toByteBuffer("010203041234010203041234112233112233112233112233112233112233");
		data.get(rt);
		Object str=parsevalue(rt,0,27,0);
		rt=new byte[4];

	}
}
