package cn.hexing.fas.protocol.zj.parse;

import java.text.NumberFormat;

import org.apache.log4j.Logger;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fk.utils.StringUtil;

/**
 * BCD码表示 HHmm NN X.XXX(多个) 
 *
 */
public class Parser57 {
	
	private static final Logger log = Logger.getLogger(Parser57.class);

	
	/**
	 * 解析
	 * @param data 数据帧
	 * @param loc  解析开始位置
	 * @param len  解析长度
	 * @param fraction 解析后小数位数
	 * @return 数据值 HH:mm,NN,X.XXX(多个)
	 */
	public static Object parsevalue(byte[] data,int loc,int len,int fraction){
		Object rt=null;
		try{			
			boolean ok=true;
			ok=ParseTool.isHaveValidBCD(data,loc,len);
			if(ok){
				StringBuffer sb=new StringBuffer();
				sb.append(ParseTool.ByteToHex(data[loc+len-1]));	//HH
				sb.append(":");
				sb.append(ParseTool.ByteToHex(data[loc+len-2]));	//mm
				sb.append(",");
				sb.append(ParseTool.ByteToHex(data[loc+len-3]));	//NN
				sb.append(",");
				for (int i=(len-3)/2-1;i>=0;i--){
					sb.append(String.valueOf((double)ParseTool.nBcdToDecimal(data,loc+i*2,2)/ParseTool.fraction[fraction]));	//x.xxx
					if (i>0)//不是最后一个
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
	 * @param value 数据值(前后不能有多余空字符 HH:MM,NN,X.XXX(多个) )
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
				throw new MessageEncodeException("错误的 HH:MM,NN,X.XXX(多个)   组帧参数:"+value);
			}
			
			String[] para=value.split(",");
			String[] time=para[0].split(":");
			
			for(int i=para.length-1;i>=2;i--){
				double xx=nf.parse(para[i]).doubleValue()*ParseTool.fraction[fraction];			
				ParseTool.IntToBcd(frame,(int)Math.round(xx),loc+(para.length-1-i)*2,2);
			}			
			frame[loc+(para.length-2)*2]=ParseTool.StringToBcd(para[1]);
			frame[loc+(para.length-2)*2+1]=ParseTool.StringToBcd(time[1]);
			frame[loc+(para.length-2)*2+2]=ParseTool.StringToBcd(time[0]);
		}catch(Exception e){
			throw new MessageEncodeException("错误的 HH:MM,NN,X.XXX(多个)  组帧参数:"+value);
		}
		
		return len;
	}
	public static void main(String[] args) {
		byte[] rt=new byte[7];
		/*ByteBuffer data=HexDump.toByteBuffer("10111234");
		data.get(rt);
		Object str=parsevalue(rt,0,4,2);*/
		Object str=constructor(rt,"16:30,1,0.9,0.7",0,7,3);
		str=parsevalue(rt,0,7,3);
	}
}
