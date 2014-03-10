package cn.hexing.fas.protocol.zj.parse;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.hexing.fk.utils.StringUtil;


/**
 * @filename	Parser39.java
 * TODO
 */
public class Parser39 {
	private static final Log log=LogFactory.getLog(Parser39.class);
	
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
				rt=sb.toString();
			}
		}catch(Exception e){
			log.error(StringUtil.getExceptionDetailInfo(e));
		}
		return rt;
	}
	
	/**
	 * 组帧----按规约默认
	 * @param frame 存放数据的帧
	 * @param value 数据值 （信道，通信地址，厂商编号，电压编码，户号，户名，表计局号，手机）
	 * @param loc   存放开始位置
	 * @param len   组帧长度
	 * @param fraction 数据中包含的小数位数
	 * @return
	 */
	public static int constructor(byte[] frame,String value,int loc,int len,int fraction){
		int slen=-1;
		try{
			String[] para=value.split(",");
			if((para!=null)&&(para.length>0)){
				int iloc=loc;				
				//Parser36.constructor(frame,para[0],iloc,4,0);
				ParseTool.RtuaToBytesC(frame,para[0],iloc,4);
				iloc+=4;
				
				if(!para[1].equals("null")){
					frame[iloc]=(byte)Integer.parseInt(para[1]);	//地址
				}else{
					frame[iloc]=(byte)0xff;
				}				
				iloc+=1;
				
				if(!para[2].equals("null")){
					if(fraction>0){//强制刷SIM卡号
						Parser37.constructor(frame,para[2],iloc,8,DataItemParser.COMM_TYPE_SMS);	//通信地址
					}else{
						Parser37.constructor(frame,para[2],iloc,8,(frame[iloc-1] & 0xff));	//通信地址
					}					
				}else{
					Arrays.fill(frame,iloc,iloc+8,(byte)0xff);
				}				
				iloc+=8;
				
				if(!para[3].equals("null")){
					frame[iloc]=(byte)Integer.parseInt(para[3]);	//厂商编号
				}else{
					frame[iloc]=(byte)0xff;
				}				
				iloc+=1;
				
				if(!para[4].equals("null")){
					frame[iloc]=para[4].getBytes()[0];	//电压编码
				}else{
					frame[iloc]=(byte)0xff;
				}				
				iloc+=1;
				
				if(!para[5].equals("null")){
					Parser43.constructor(frame,para[5],iloc,10,0);	//户号
				}else{
					Arrays.fill(frame,iloc,iloc+10,(byte)0xff);
				}				
				iloc+=10;
				
				if(!para[6].equals("null")){
					Parser43.constructor(frame,para[6],iloc,20,0);	//户名
				}else{
					Arrays.fill(frame,iloc,iloc+20,(byte)0xff);
				}				
				iloc+=20;
				
				if(!para[7].equals("null")){
					Parser43.constructor(frame,para[7],iloc,12,0);	//表计局号
				}else{
					Arrays.fill(frame,iloc,iloc+12,(byte)0xff);
				}
				iloc+=12;
				
				if(!para[8].equals("null")){
					Parser43.constructor(frame,para[8],iloc,14,0);	//手机
				}else{
					Arrays.fill(frame,iloc,iloc+14,(byte)0xff);
				}
				
				slen=71;
			}
		}catch(Exception e){
			//throw new MessageEncodeException("错误的 终端参数 组帧参数:"+value);
			log.warn("错误的 终端参数 组帧参数:"+value);
		}
		return slen;
	}
}
