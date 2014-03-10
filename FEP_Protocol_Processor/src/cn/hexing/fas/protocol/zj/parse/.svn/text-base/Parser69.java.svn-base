package cn.hexing.fas.protocol.zj.parse;

import org.apache.log4j.Logger;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fas.protocol.gw.parse.DataSwitch;
import cn.hexing.fk.utils.StringUtil;
import cn.hexing.util.HexDump;


/**
 * 按字节解析     
 * 低位在前，高位在后
 * 12 08 11 00 12 解析为18.0.17.8.18
 * @author Administrator
 *
 */
public class Parser69 {
	
	private static final Logger log = Logger.getLogger(Parser69.class);

	
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
		String sdata=DataSwitch.ReverseStringByByte(HexDump.toHex(data));
		String s="";
		try{
			for(int i=0;i<len-3;i++){
				String dataint=sdata.substring(2*i,2*i+2);
				int ii=Integer.parseInt(dataint,16);
				s=s+ii+".";
			}
		}catch(Exception e){
			log.error(StringUtil.getExceptionDetailInfo(e));
		}
		rt=s.substring(0,s.length()-1);
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
				if(c==':'){
					continue;
				}
				if(c=='-'){
					continue;
				}
				if(c>='0' && c<='9'){
					continue;
				}
				throw new MessageEncodeException("错误的 BCD 组帧参数:"+value);
			}
			ParseTool.StringToBcds(frame,loc,value,len,(byte)0xAA);
		}catch(Exception e){
			throw new MessageEncodeException("错误的 BCD 组帧参数:"+value);
		}
		
		return len;
	}
}
