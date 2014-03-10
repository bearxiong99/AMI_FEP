package cn.hexing.fas.protocol.zj.parse;

import org.apache.log4j.Logger;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fk.utils.StringUtil;

/**
 * 普通任务设置
 *
 */
public class Parser26 {
	
	private static final Logger log = Logger.getLogger(Parser26.class);

	
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
				sb.append(ParseTool.ByteToHex(data[loc]));	//TT
				sb.append(",");
				sb.append(String.valueOf(data[loc+2] & 0xff)); //TS--NN
				sb.append(",");
				sb.append(ParseTool.ByteToHex(data[loc+1]));	//TS--UU
				sb.append(",");
				sb.append(String.valueOf(data[loc+4] & 0xff)); //TI--NN
				sb.append(",");
				sb.append(ParseTool.ByteToHex(data[loc+3]));	//TI--UU
				sb.append(",");
				sb.append(String.valueOf(data[loc+6] & 0xff)); //RS--NN
				sb.append(",");
				sb.append(ParseTool.ByteToHex(data[loc+5]));	//RS--UU
				sb.append(",");
				sb.append(String.valueOf(data[loc+8] & 0xff)); //RI--NN
				sb.append(",");
				sb.append(ParseTool.ByteToHex(data[loc+7]));	//RI--UU
				sb.append(",");
				sb.append(String.valueOf(ParseTool.BCDToDecimal(data[loc+9]))); //RDI
				sb.append(",");
				sb.append(String.valueOf(ParseTool.BCDToDecimal(data[loc+10]))); //TN
				sb.append(",");
				sb.append(String.valueOf(ParseTool.nBcdToDecimal(data,loc+11,2)));	//SP
				sb.append(",");
				sb.append(String.valueOf(ParseTool.nBcdToDecimal(data,loc+13,2)));	//RT
				sb.append(",");
				int din=ParseTool.BCDToDecimal(data[loc+15]);
				sb.append(String.valueOf(din)); //DIN		
				if(din<=32){	//DI 不超过32
					int iloc=loc+16;
					for(int i=0;i<din;i++){
						sb.append(",");
						sb.append(ParseTool.BytesToHexC(data,iloc,2));
						iloc+=2;
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
	 * 组帧
	 * @param frame 存放数据的帧
	 * @param value 数据值
	 * @param loc   存放开始位置
	 * @param len   组帧长度
	 * @param fraction 数据中包含的小数位数
	 * @return
	 */
	public static int constructor(byte[] frame,String value,int loc,int len,int fraction){
		int iloc=loc;
		int slen=-1;
		try{
			String[] para=value.split(",");
			frame[loc]=ParseTool.IntToBcd(Integer.parseInt(para[0]));	//TT
			
			frame[loc+1]=ParseTool.StringToBcd(para[2]);	//TS--UU			
			frame[loc+2]=(byte)(Integer.parseInt(para[1]) & 0xff);	//NN
			
			frame[loc+3]=ParseTool.StringToBcd(para[4]);	//TI--UU			
			frame[loc+4]=(byte)(Integer.parseInt(para[3]) & 0xff);	//NN
			
			frame[loc+5]=ParseTool.StringToBcd(para[6]);	//RS--UU
			frame[loc+6]=(byte)(Integer.parseInt(para[5]) & 0xff);	//NN
			
			frame[loc+7]=ParseTool.StringToBcd(para[8]);	//RI--UU
			frame[loc+8]=(byte)(Integer.parseInt(para[7]) & 0xff);	//NN
			
			frame[loc+9]=ParseTool.IntToBcd(Integer.parseInt(para[9]));	//RDI
			
			frame[loc+10]=ParseTool.IntToBcd(Integer.parseInt(para[10]));	//TN
			
			int nums=Integer.parseInt(para[11]);
			ParseTool.IntToBcd(frame,nums,loc+11,2);	//sp
			
			nums=Integer.parseInt(para[12]);
			ParseTool.IntToBcd(frame,nums,loc+13,2);	//rt
			
			nums=Integer.parseInt(para[13]);
			frame[loc+15]=ParseTool.IntToBcd(nums);	//din
			
			if(nums!=(para.length-14)){
				//wrong para
				System.out.println("task para is error");
			}
			iloc=loc+16;
			for(int i=14;i<para.length;i++){	//DI
				ParseTool.HexsToBytes(frame,iloc,para[i]);
				iloc+=2;
			}
			slen=iloc-loc;
		}catch(Exception e){
			throw new MessageEncodeException("错误的 普通任务设置 组帧参数:"+value);
		}
		
		return slen;
	}
}
