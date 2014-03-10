package cn.hexing.fas.protocol.zj.parse;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fas.model.HostCommandResult;
import cn.hexing.fas.protocol.gw.parse.DataSwitch;
import cn.hexing.util.HexDump;

public class Parser60 {	
	
	private static final Logger log = Logger.getLogger(Parser60.class);

	
	/**
	 * 	功能：将预付费信息包解析成数据
	 * @param data 数据帧
	 * @return 解析完数据内容
	 */
	public static Object parsevalue(byte[] data){
		String	sdata=HexDump.toHex(data);
		List<HostCommandResult> value=new ArrayList<HostCommandResult>();
		//获取电表编号
		String meterNo= DataSwitch.ReverseStringByByte(sdata.substring(0, 12));
		sdata=sdata.substring(12);
		//取 数据项编码 code
		String code=DataSwitch.ReverseStringByByte(sdata.substring(0,4));
		sdata=sdata.substring(4);	
		HostCommandResult hcr = new HostCommandResult();
		String total=sdata.substring(0, 6)+"."+sdata.substring(6, 8);
		String tariff1=sdata.substring(8, 14)+"."+sdata.substring(14, 16);
		String tariff2=sdata.substring(16, 22)+"."+sdata.substring(22, 24);
		String tariff3=sdata.substring(24, 30)+"."+sdata.substring(30, 32);
		String tariff4=sdata.substring(32, 38)+"."+sdata.substring(38, 40);
		sdata=sdata.substring(40);
		String powerBalance =sdata.substring(0, 6)+"."+sdata.substring(6, 8);	//电表余额
		String balanceStatus=sdata.substring(8, 10);//余额状态
		String relayStatus=sdata.substring(10, 12);//继电器状态
		String relayMode=sdata.substring(12, 14);//继电器模式
		String actionReason=sdata.substring(14, 16);//继电器操作原因
		String eventStatus=sdata.substring(16, 24);//当前电表事件状态
		String svalue=total+"#"+tariff1+"#"+tariff2+"#"+tariff3+"#"+tariff4+"#"+powerBalance+"#"+balanceStatus+"#"+relayStatus+"#"+relayMode+"#"+actionReason+"#"+eventStatus;
		hcr.setValue(svalue);
		hcr.setCode(code);
		hcr.setMeterAddr(meterNo);
		value.add(hcr);
		return value;
	}
	
	/**
	 * decimal to bcd
	 * @param frame 字节存放数组
	 * @param value 数据内容
	 * @param loc   存放开始位置
	 * @param len   数据项长度
	 * @param fraction 数据包含小数位数
	 * @return 实际编码长度
	 */
	public static int constructor(byte[] frame,String value,int loc,int len,int fraction){
		try{
			NumberFormat nf=NumberFormat.getInstance();
			nf.setMaximumFractionDigits(4);			
			
			double val=nf.parse(value).doubleValue();
			if(fraction>0){
				val*=ParseTool.fraction[fraction];
			}
			
			ParseTool.IntToBcd(frame,(int)Math.round(val),loc,len);
		}catch(Exception e){
			//log.error(e.getMessage());
			throw new MessageEncodeException("bab BCD string:"+value);
		}
		return len;
	}
}
