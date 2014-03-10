package cn.hexing.fas.protocol.zj.parse;

import org.apache.log4j.Logger;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fk.utils.StringUtil;

/**
 * 异常任务
 *
 */
public class Parser28 {
	
	private static final Logger log = Logger.getLogger(Parser28.class);

	
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
				sb.append(ParseTool.BytesToHexC(data,loc+1,2)); //ALR
				sb.append(",");		
				sb.append(String.valueOf(data[loc+4] & 0xff)); //TI--NN
				sb.append(",");
				sb.append(ParseTool.ByteToHex(data[loc+3]));	//TI--UU
				sb.append(",");
				sb.append(String.valueOf(ParseTool.BCDToDecimal(data[loc+5]))); //TN
				sb.append(",");
				int din=ParseTool.BCDToDecimal(data[loc+6]);
				sb.append(String.valueOf(din)); //DIN		
				if(din<=32){	//DI 不超过32
					int iloc=loc+7;
					for(int i=0;i<din;i++){
						sb.append(",");
						sb.append(ParseTool.ByteToHex(data[iloc+2]));
						sb.append(" ");
						sb.append(ParseTool.BytesToHexC(data,iloc,2));				
						iloc+=3;
					}
					sb.append(",");
					sb.append(String.valueOf(ParseTool.BCDToDecimal(data[iloc])));
				}
				rt= sb.toString();
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
		int slen=-1;
		try{
			String[] para=value.split(",");
			frame[loc]=ParseTool.IntToBcd(Integer.parseInt(para[0]));	//TT
			
			ParseTool.HexsToBytes(frame,loc+1,para[1]);
			
			frame[loc+3]=ParseTool.StringToBcd(para[3]);	//TI--UU			
			frame[loc+4]=(byte)(Integer.parseInt(para[2]) & 0xff);	//NN
			
			frame[loc+5]=ParseTool.IntToBcd(Integer.parseInt(para[4]));	//TN
			
			int nums=Integer.parseInt(para[5]);
			frame[loc+6]=ParseTool.IntToBcd(nums);	//din
			
			if((2*nums)!=(para.length-7)){
				//wrong para
				System.out.println("task para is error");
			}
			int iloc=loc+7;
			int ipara=6;
			for(int i=0;i<nums;i++){	//DI				
				ParseTool.HexsToBytes(frame,iloc,para[ipara+1]);
				frame[iloc+2]=ParseTool.IntToBcd(Integer.parseInt(para[ipara]));	//TN
				ipara+=2;
				iloc+=3;
			}
			frame[iloc]=ParseTool.IntToBcd(Integer.parseInt(para[ipara]));
			slen=iloc-loc+1;
		}catch(Exception e){
			throw new MessageEncodeException("错误的 异常任务 组帧参数:"+value);
		}
		return slen;
	}
}
