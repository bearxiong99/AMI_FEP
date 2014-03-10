package cn.hexing.fas.protocol.zj.parse;

import java.util.Arrays;

import org.apache.log4j.Logger;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fk.utils.StringUtil;


/**
 * ascii字符解析与编码---0x00是填充，可能填充在后或前（有些终端填充0xAA）
 *
 */
public class Parser43 {
	
	private static final Logger log = Logger.getLogger(Parser43.class);

	
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
			int begin=loc;
			for(int i=0;i<len;i++){	//remove prefix
				if(!(((data[loc+i] & 0xff)==0x00) || ((data[loc+i] & 0xff)>=0x80))){
					break;
				}
				begin++;
			}
			int rlen=0;
			for(int i=begin;i<loc+len;i++){	//remove suffix
				if(((data[i] & 0xff)==0x00) || ((data[i] & 0xff)>=0x80)){
					break;
				}
				rlen++;
			}
			if(rlen>0){
				byte[] apn=new byte[rlen];
				int iloc=begin+rlen-1;
				for(int i=0;i<rlen;i++){
					apn[i]=data[iloc];
					iloc--;
				}
				rt=new String(apn,"GBK");
			}
		}catch(Exception e){
			log.error(StringUtil.getExceptionDetailInfo(e));
		}
		return rt;
	}
	
	/**
	 * 组帧----按规约默认，长度不够时高位填充0x00
	 * @param frame 存放数据的帧
	 * @param value 数据值
	 * @param loc   存放开始位置
	 * @param len   组帧长度
	 * @param fraction 数据中包含的小数位数
	 * @return
	 */
	public static int constructor(byte[] frame,String value,int loc,int len,int fraction){
		int slen=-1;
		try{
			Arrays.fill(frame,loc,loc+len-1,(byte)0x0);
			byte[] str=value.getBytes();
			int rlen=str.length;
			if(rlen>len){//错误，数据过长
				rlen=len;
			}
			if(fraction==0){
				int src=str.length-1;
				int dest=loc;
				for(int i=0;i<rlen;i++){
					frame[dest]=str[src];
					src--;
					dest++;
				}	
			}else{//低位填0
				int src=0;
				int dest=loc+len-1;
				for(int i=0;i<rlen;i++){
					frame[dest]=str[src];
					src++;
					dest--;
				}	
			}
					
			slen=len;
		}catch(Exception e){
			throw new MessageEncodeException("错误的 ascii字符码 组帧参数:"+value);
		}
		return slen;
	}
}
