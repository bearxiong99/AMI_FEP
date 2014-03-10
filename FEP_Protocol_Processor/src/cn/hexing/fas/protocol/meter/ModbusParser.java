package cn.hexing.fas.protocol.meter;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.hexing.fas.protocol.gw.parse.DataSwitch;
import cn.hexing.fas.protocol.gw.parse.DataValue;
import cn.hexing.fas.protocol.gw.parse.ParserHTB;
import cn.hexing.fas.protocol.meter.conf.MeterProtocolDataItem;
import cn.hexing.fas.protocol.meter.conf.MeterProtocolDataSet;
import cn.hexing.fas.protocol.zj.parse.ParseTool;
import cn.hexing.fk.utils.HexDump;
import cn.hexing.fk.utils.ModbusCrc16;

public class ModbusParser {
	
	private final Log log=LogFactory.getLog(ModbusParser.class);
	private MeterProtocolDataSet dataset;
	
	public ModbusParser(){
		try{
			dataset=MeterProtocolFactory.createMeterProtocolDataSet("Modbus");
		}catch(Exception e){
			log.error("Modbus Protocol init fail.");
		}
	}
	
	public static void main(String[] args) {
		String strFrame = "03030200E60000";
		
		ModbusParser mp=new  ModbusParser();
		mp.parse(HexDump.toArray(strFrame), "2B");
		
		System.out.println(HexDump.toHex(mp.construct("2B", "0.23", 0x10, 0, 1)));
	}
	
	public String parse(byte[] frame,String startPos){
		
		MeterProtocolDataItem item = getItemByStartPos(startPos);
		if(item == null)
			throw new RuntimeException("item is null. startPos = "+startPos);
		
		int operation = frame[1];
		int valLen = frame[2] & 0xFF;
		StringBuilder result = new StringBuilder();
		if(operation == 0x03){//读返回
			String[] formats = item.getFormat().split("#");
			int valSize=valLen/item.getLength();
			int loc =3;
			for(int i=0;i<valSize;i++){
				byte[] area = new byte[item.getLength()];
				//loc , length 
				System.arraycopy(frame, loc, area, 0, item.getLength());
				int tempLoc = 0;
				StringBuilder sbResult = new StringBuilder();
				for(int j=0;j<formats.length;j++){
					String format = formats[j];
					DataValue dataValue = parser(format, area, tempLoc, item.getFraction());
					tempLoc+=dataValue.getLen();
					sbResult.append(dataValue.getValue()).append(";");
				}
				
				if(sbResult.length()>0){
					sbResult.deleteCharAt(sbResult.length()-1);
				}
				result.append(sbResult).append("#");
				loc +=item.getLength();
			}
			if(result.length()>0){
				result.deleteCharAt(result.length()-1);
			}
			
		}else if(operation == 0x10){
			//设置返回
			//返回0、或1
			byte[] start = new byte[2];
			System.arraycopy(frame, 3, start, 0, 2);
			String oldPos="0000".substring(startPos.length())+startPos;
			if(oldPos.equals(HexDump.toHex(start))){
				result.append("0");
			}else{
				result.append("1");
			}

		}
		
		return result.toString();
	}
	
	/**
	 * 组Modbus的帧
	 * @param startPos  --用来寻找item
	 * @param dataValue	--参数
	 * @param operation	--操作，标识读写操作
	 * @param offset	--偏移量,如果读第5个数据，偏移量就是4
	 * @param requestNum--读数据的个数，读第5个之后的几个数据，用来计算数据长度
	 * @return
	 */
	public byte[] construct(String startPos,String dataValue,int operation,int offset,int requestNum){
		
		MeterProtocolDataItem item = getItemByStartPos(startPos);
		if(item == null)
			throw new RuntimeException("item is null. startPos = "+startPos);
		byte[] frame =null;

		int realStartPos = Integer.parseInt(startPos, 16)+offset*item.getLength();
		startPos = HexDump.toHex(realStartPos).substring(4, 8);
		int dataLen = requestNum*item.getLength();
		if(operation == 0x03){//读多个寄存器
			frame = new byte[7];
			frame[0]=0x03;frame[1]=0x03;
			byte[] start = HexDump.toArray("0000".substring(startPos.length())+startPos);
			System.arraycopy(start,0, frame, 2,2);
			frame[4]=(byte) dataLen;
			byte [] calcCsArea = new byte[5]; 
			System.arraycopy(frame, 0, calcCsArea, 0, calcCsArea.length);
			String strCs=ModbusCrc16.calc(calcCsArea);
			strCs=DataSwitch.ReverseStringByByte(strCs);
			ParseTool.HexsToBytes(frame, frame.length-2, strCs);
		}else if(operation == 0x10){ //写多个寄存器
			frame = new byte[7+dataLen];
			frame[0]=0x03; frame[1]=0x10;
			byte[] start = HexDump.toArray("0000".substring(startPos.length())+startPos);
			System.arraycopy(start,0, frame, 2,2);
			frame[4]=(byte) dataLen;
			//数据区
			String[] dataValues = dataValue.split("#");
//			if(dataValues.length !=offset)
//				throw new RuntimeException("Paramater is wrong. Check it");
			int loc = 5;
			for(int i=0;i<dataValues.length;i++){
				//1.将数据组成数据区
				String[] formats=item.getFormat().split("#");
				String[] values=dataValues[i].split(";");
				for(int j=0;j<formats.length;j++){
					String val = values[j];
					String format = formats[j];
					int fraction = item.getFraction();
					loc = loc+constructor(frame,loc,val,format,fraction);
				}
			}
			
			byte [] calCsArea = new byte[5+dataLen]; 
			System.arraycopy(frame, 0, calCsArea, 0, calCsArea.length);
			String strCs=ModbusCrc16.calc(calCsArea);
			strCs=DataSwitch.ReverseStringByByte(strCs);
			ParseTool.HexsToBytes(frame, frame.length-2, strCs);
		}

		
		return frame;
	}
	
	/**
	 * 根据起始地址获得item
	 * @param startPos
	 * @return
	 */
	public MeterProtocolDataItem getItemByStartPos(String startPos){

		List<?> list = dataset.getDataarray();
		int pos = Integer.parseInt(startPos, 16);
		//获得startPos
		for(Object o : list){
			if(o instanceof MeterProtocolDataItem){
				MeterProtocolDataItem item = (MeterProtocolDataItem) o;
				int itemStartPos = Integer.parseInt(item.getStartPos(), 16);
				int itemEndPos = Integer.parseInt(item.getEndPos(), 16);
				if(itemStartPos<=pos && itemEndPos>pos)
					return item;
			}
		}		
		return null;
	}
	
	private DataValue parser(String format,byte[] dataValue,int loc,int fraction){

		int len = 0;
		String output =null;
		DataValue value = new DataValue();
		
		if(format.startsWith("HTB")){
			len = Integer.parseInt(format.substring(3));
			byte[] data = new byte[len];
			System.arraycopy(dataValue, loc, data, 0, len);
			String strValue = ParserHTB.parseValue(DataSwitch.ReverseStringByByte(HexDump.toHex(data)), len*2);
			int val = Integer.parseInt(strValue);
			if(fraction>0){
				NumberFormat snf=NumberFormat.getInstance();
				snf.setMinimumFractionDigits(fraction);
				snf.setMinimumIntegerDigits(1);
				snf.setGroupingUsed(false);
				output = ""+snf.format((double)val/ParseTool.fraction[fraction]);
			}else{
				output = ""+val;
			}
		}
		value.setLen(len);
		value.setValue(output);
		return value;
	}
	
	
	private int constructor(byte[] frame,int loc,String value,String format,int fraction){
		
		int len = 0;
		if(format.startsWith("HTB")){
			len=Integer.parseInt(format.substring(3));
			constructHTB(frame,loc,len,value,fraction);
		}
		return len;
	}

	
	private void constructHTB(byte[] frame,int loc,int len,String value,int fraction){
		NumberFormat nf=NumberFormat.getInstance();
		nf.setMaximumFractionDigits(4);			
		
		double val;
		try {
			val = nf.parse(value).doubleValue();
			if(fraction>0){
				val*=ParseTool.fraction[fraction];
			}
			int intValue = (int) Math.round(val);
			for(int i = 0 ; i < len ; i++){
				frame[loc+(len-i-1)] = (byte) (intValue&0xFF);
				intValue = intValue >>> 8 ;
			}
			
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	
	
	
}
