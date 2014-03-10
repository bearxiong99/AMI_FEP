package cn.hexing.fas.protocol.zj.parse;

import org.apache.log4j.Logger;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fk.utils.StringUtil;

/**
 * 任务解析入口
 *
 */
public class Parser34 {
	
	private static final Logger log = Logger.getLogger(Parser34.class);

	
	/**
	 * BCD to decimal
	 * @param data 数据帧
	 * @param loc  解析开始位置
	 * @param len  解析字节长度
	 * @param fraction 解析后数据包含的小数位数
	 * @return 数据内容
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
				int tasktype=ParseTool.BCDToDecimal(data[loc]);
				switch(tasktype){
					case DataItemParser.TASK_TYPE_NORMAL:
						rt=Parser26.parsevalue(data,loc,len,fraction);
						break;
					case DataItemParser.TASK_TYPE_RELAY:
						rt=Parser27.parsevalue(data,loc,len,fraction);
						break;
					case DataItemParser.TASK_TYPE_EXCEPTION:
						rt=Parser28.parsevalue(data,loc,len,fraction);
						break;
					default:
						break;
				}
			}
		}catch(Exception e){
			log.error(StringUtil.getExceptionDetailInfo(e));
		}
		return rt;
	}
	
	/**
	 * decimal to bcd
	 * @param frame 字节存放数组
	 * @param value 数据内容
	 * @param loc   存放开始位置
	 * @param len   数据项长度
	 * @param fraction 数据包含小数位数
	 * @return
	 */
	public static int constructor(byte[] frame,String value,int loc,int len,int fraction){
		int slen=0;
		try{
			int index=value.indexOf(",");
			if(index>0){
				int tasktype=Integer.parseInt(value.substring(0,index));
				switch(tasktype){
				case DataItemParser.TASK_TYPE_NORMAL:
					slen=Parser26.constructor(frame,value,loc,len,fraction);
					break;
				case DataItemParser.TASK_TYPE_RELAY:
					slen=Parser27.constructor(frame,value,loc,len,fraction);
					break;
				case DataItemParser.TASK_TYPE_EXCEPTION:
					slen=Parser28.constructor(frame,value,loc,len,fraction);
					break;
				default:
					break;
			}
			}
		}catch(Exception e){
			throw new MessageEncodeException("错误的 任务 组帧参数:"+value);
		}
		return slen;
	}
}
