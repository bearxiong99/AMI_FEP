package cn.hexing.fas.protocol.meter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.hexing.fas.protocol.Protocol;
import cn.hexing.fas.protocol.data.DataItem;
import cn.hexing.fas.protocol.gw.parse.DataSwitch;
import cn.hexing.fas.protocol.meter.conf.MeterProtocolDataItem;
import cn.hexing.fas.protocol.meter.conf.MeterProtocolDataSet;
import cn.hexing.fas.protocol.zj.parse.DataItemParser;
import cn.hexing.fas.protocol.zj.parse.ParseTool;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.utils.HexDump;

/**
 * @filename	BbMeterParser.java
 * TODO			部颁表规约
 * 				0x68---------------------帧头标识1
 * 				A0-----------------------地址（低位）
 * 				A1-----------------------地址
 * 				A2-----------------------地址
 * 				A3-----------------------地址
 * 				A4-----------------------地址
 * 				A5-----------------------地址（高位）
 * 				0x68---------------------帧头标识2
 * 				c------------------------控制码 bit7：传输方向，0-命令 1-应答 bit6：异常标识 0-正常 1-异常 
 * 											   bit5：后续帧标识 0-单帧 1-有后续帧
 *                                             bit0-bit4 功能码
 *                                             00000 ：  保留
 *				                               00001 ：  读数据
 *				                               00010 ：  读后续数据
 *				                               00011 ：  重读数据
 *				                               00100 ：  写数据
 *				                               01000 ：  广播校时
 *				                               01010 ：  写设备地址
 *				                               01100 ：  更改通讯速率
 *				                               01111 :   修改密码
 *				                               10000 :   最大需量清零
 *				L-------------------------数据域长度
 *				DATA----------------------数据域 读数据不超过200，写数据不超过50
 *				CS------------------------校验 从帧头标识1到CS前所有字节的和模256
 *				0x16----------------------帧尾
 */
public class BbMeterParser implements IMeterParser{
	private final Log log=LogFactory.getLog(BbMeterParser.class);
	private MeterProtocolDataSet dataset;
	
	public BbMeterParser(){
		try{
			dataset=MeterProtocolFactory.createMeterProtocolDataSet("BBMeter");
		}catch(Exception e){
			log.error("部颁表规约初始化失败");
		}
	}
	
	public static void main(String[] args) {
		BbMeterParser bb = new BbMeterParser();
		DataItem item = new DataItem();
		item.addProperty("point", "1111");
		item.addProperty("params", "12");
		bb.constructor("D307", item, true);
		MeterProtocolDataItem s = bb.dataset.getDataItem("D307");
		String s1 = bb.dataset.getConvertCode("D307");
		System.out.println(s);
		String str = "6830000313200068E00235338016";
		bb.parser(HexDump.toArray(str), 0, str.length()/2,null);
	}
	
	/**
	 * 说明：部颁表规约中数据标识和浙江表规约中数据标识一致，浙江表规约中的数据集是
	 *      部颁表规约数据集的一个子集
	 *      所以本函数的转换功能减弱，主要是减少数据标识重复
	 */
	public String[] convertDataKey(String[] datakey) {
		String[] rt=null;
		try{
			if(datakey!=null && datakey.length>0){//有数据标识要转换
				rt=new String[datakey.length];
				for(int i=0;i<datakey.length;i++){
					if((datakey[i]!=null) && datakey[i].equalsIgnoreCase("8902")){
						addDataKey(rt,"C034");
					}else{					
						addDataKey(rt,datakey[i]);
					}
				}
			}
		}catch(Exception e){
			log.error("部颁表数据标识转换",e);
		}
		return rt;
	}
	
	/**
	 * 加入未包含数据点key到队列
	 * @param datakeys
	 * @param dkey
	 * 说明：数据标识为XXXX，最多支持召测XXFF数据块，不支持FFFF、XFFF，因为数据太多
	 */
	private void addDataKey(String[] datakeys,String dkey){
		for(int i=0;i<datakeys.length;i++){
			if(datakeys[i]==null || datakeys[i].equals("")){//队列中未包含
				if(dkey.substring(0,1).equalsIgnoreCase("F") || dkey.substring(1,2).equalsIgnoreCase("F")){
					//丢弃，不召测FFFF、XFFF
					break;
				}
				datakeys[i]=dkey;
				break;
			}
			String char1=datakeys[i].substring(0,1);
			String char2=datakeys[i].substring(1,2);
			String char3=datakeys[i].substring(2,3);
			if(char1.equalsIgnoreCase(dkey.substring(0,1))
				&& char2.equalsIgnoreCase(dkey.substring(1,2))
				&& char3.equalsIgnoreCase(dkey.substring(2,3))){//是一个数据块内数据
				
				StringBuffer sb=new StringBuffer();
				sb.append(char1);
				sb.append(char2);
				//小数据块
				sb.append(char3);
				sb.append("F");
				
				datakeys[i]=sb.toString();
				sb=null;
				break;
			}
		}
	}
	
	//use for dlms realy only 97 protocol,create by gaoll at 2013年5月14日11:17:55
	public byte[] constructor(String dataKey,DataItem params,boolean isSet){
		byte[] frame = null;
		String meterAddr = (String)params.getProperty("point");
		String value = (String)params.getProperty("params");
		if(isSet){
			
			MeterProtocolDataItem meterDataItem = this.dataset.getDataItem(dataKey);
			byte[] dataArea = new byte[meterDataItem.getLength()];
			DataItemCoder.coder(dataArea, 0, value,meterDataItem.getType(), meterDataItem.getLength(),meterDataItem.getFraction());
			frame = new byte[14+dataArea.length+4]; //4个字节密码
			frame[0]=0x68;
			ParseTool.HexsToBytesAA(frame,1,meterAddr,6,(byte)0xAA);				
			frame[7]=0x68;
			frame[8]=0x04;
			frame[9]=(byte) (2+4+dataArea.length);
			ParseTool.HexsToBytes(frame,10,dataKey);
			frame[10]=(byte)(frame[10]+0x33);
			frame[11]=(byte)(frame[11]+0x33);

			ParseTool.HexsToBytes(frame,12,"33333333");

			for(int i=0;i<dataArea.length;i++){ //要不要倒转字节?
				frame[16+i]=(byte)(dataArea[i]+0x33);
			}

			frame[frame.length-2]=ParseTool.calculateCS(frame,0,frame.length-2);	//cs
			frame[frame.length-1]=0x16;	//cs
		}else{
			frame=new byte[14];
			frame[0]=0x68;
			ParseTool.HexsToBytesAA(frame,1,meterAddr,6,(byte)0xAA);				
			frame[7]=0x68;
			frame[8]=0x01;
			frame[9]=0x02;
			ParseTool.HexsToBytes(frame,10,dataKey);
			frame[10]=(byte)(frame[10]+0x33);
			frame[11]=(byte)(frame[11]+0x33);
			frame[12]=ParseTool.calculateCS(frame,0,12);	//cs
			frame[13]=0x16;	//cs
		}
		return frame;
	}
	
	/**
	 * datakey只接受第一个数据标识，以支持外层组多帧
	 */
	public byte[] constructor(String[] datakey, DataItem para) {
		byte[] frame=null;
		try{
			if((datakey!=null)&&(datakey.length>0)
					&&(para!=null)&&(para.getProperty("point")!=null)){	//check para
				if (datakey[0].length()==4){//97版全国电表规约
					String dkey=datakey[0];
					String value=(String)para.getProperty("write");//参数
					String maddr=(String)para.getProperty("point");//表地址
					if(null==value){//不带参数就认为是读数据请求
						frame=new byte[14];
						frame[0]=0x68;
						ParseTool.HexsToBytesAA(frame,1,maddr,6,(byte)0xAA);				
						frame[7]=0x68;
						frame[8]=0x01;
						frame[9]=0x02;
						ParseTool.HexsToBytes(frame,10,dkey);
						frame[10]=(byte)(frame[10]+0x33);
						frame[11]=(byte)(frame[11]+0x33);
						frame[12]=ParseTool.calculateCS(frame,0,12);	//cs
						frame[13]=0x16;	//cs
					}else{
						if(dkey.equals("EE01")||dkey.equals("EE02")||dkey.equals("EE04")){
							//继电器操作，合闸 保电 退出保电
							frame=new byte[18];
							frame[0]=0x68;
							ParseTool.HexsToBytesAA(frame,1,maddr,6,(byte)0xAA);				
							frame[7]=0x68;
							frame[8]=0x04;
							frame[9]=0x06;
							ParseTool.HexsToBytes(frame,10,dkey);
							frame[10]=(byte)(frame[10]+0x33);
							frame[11]=(byte)(frame[11]+0x33);
							ParseTool.HexsToBytes(frame,12,"33333333");
							frame[16]=ParseTool.calculateCS(frame,0,16);	//cs
							frame[17]=0x16;	//cs	
						}
						else if(dkey.equals("EE09")){//拉闸
							frame=new byte[20];
							frame[0]=0x68;
							ParseTool.HexsToBytesAA(frame,1,maddr,6,(byte)0xAA);				
							frame[7]=0x68;
							frame[8]=0x04;
							frame[9]=0x08;
							ParseTool.HexsToBytes(frame,10,dkey);
							frame[10]=(byte)(frame[10]+0x33);
							frame[11]=(byte)(frame[11]+0x33);
							ParseTool.HexsToBytes(frame,12,"33333333");
							frame[16]=0x09;
							frame[17]=0x09;
							frame[16]=(byte)(frame[16]+0x33);
							frame[17]=(byte)(frame[17]+0x33);
							frame[18]=ParseTool.calculateCS(frame,0,18);	//cs
							frame[19]=0x16;	//cs	
						}
						else if(dkey.equals("EA20")){//对时
							frame=new byte[25];
							frame[0]=0x68;
							ParseTool.HexsToBytesAA(frame,1,maddr,6,(byte)0xAA);				
							frame[7]=0x68;
							frame[8]=0x04;
							frame[9]=0x0D;
							ParseTool.HexsToBytes(frame,10,dkey);
							frame[10]=(byte)(frame[10]+0x33);
							frame[11]=(byte)(frame[11]+0x33);
							ParseTool.HexsToBytes(frame,12,"33333333");
							SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							Date date=null;
							date=sdf.parse(value);
							SimpleDateFormat df = new SimpleDateFormat("yyMMddHHmmss");
							value=df.format(date);
							ParseTool.HexsToBytes(frame,16,"06"+value);
							for(int i=16;i<23;i++){//为数据加上33
								frame[i]=(byte)(frame[i]+0x33);
							}
							frame[23]=ParseTool.calculateCS(frame,0,23);	//cs
							frame[24]=0x16;	//cs
						}
						else if(dkey.equals("EE20")){//充值token下发
							//68 76 55 74 20 41 01 68 04 10 53 21 33 33 33 33 B7 A8 CC 83 34 33 33 33 33 33 A6 16
							frame=new byte[28];
							frame[0]=0x68;
							ParseTool.HexsToBytesAA(frame,1,maddr,6,(byte)0xAA);				
							frame[7]=0x68;
							frame[8]=0x04;
							frame[9]=0x10;
							ParseTool.HexsToBytes(frame,10,dkey);
							frame[10]=(byte)(frame[10]+0x33);
							frame[11]=(byte)(frame[11]+0x33);
							ParseTool.HexsToBytes(frame,12,"33333333");
							ParseTool.HexsToBytes(frame,16,value);
							for(int i=16;i<26;i++){//为数据加上33
								frame[i]=(byte)(frame[i]+0x33);
							}
							frame[26]=ParseTool.calculateCS(frame,0,26);	//cs
							frame[27]=0x16;	//cs
						}
					}

				}else{//07版全国电表规约
					String dkey=datakey[0];
					String maddr=(String)para.getProperty("point");//表地址	
					String value=(String)para.getProperty("write");
					if(dkey.equals("070000FF")){//身份认证
						frame=new byte[44];
						frame[0]=0x68;
						ParseTool.HexsToBytesAA(frame,1,maddr,6,(byte)0xAA);				
						frame[7]=0x68;	
						String identify=(String)para.getProperty("identify");
						frame[8]=0x03;
						frame[9]=0x20;						
						ParseTool.HexsToBytes(frame,10,dkey);//标识
						ParseTool.HexsToBytes(frame,14,"00000000");//操作者代码
						ParseTool.HexsToBytes(frame,18,identify.substring(0,16));//密文
						ParseTool.HexsToBytes(frame,26,identify.substring(16,32));//随机数
						ParseTool.HexsToBytes(frame,34,identify.substring(32,48));//密文
						for (int i=10;i<42;i++)
							frame[i]=(byte)(frame[i]+0x33);
						frame[42]=ParseTool.calculateCS(frame,0,42);	//cs
						frame[43]=0x16;	//cs
					}else if(dkey.equals("0800000100")){ // 写TOKEN下发
						frame = new byte[40];
						frame[0] = 0x68;
						ParseTool.HexsToBytesAA(frame,1,maddr,6,(byte)0xAA);				
						frame[7]=0x68;	
						frame[8]=0x20; //标识写TOKEN
						frame[9]=0x1C;
						ParseTool.HexsToBytes(frame, 10, "02");//密码权限
						ParseTool.HexsToBytes(frame, 11, "000000");//3个字节密码
						ParseTool.HexsToBytes(frame, 14, "00000000");//4个字节操作者代码
						ParseTool.HexsToBytes(frame, 18, DataSwitch.ReverseStringByByte(HexDump.toHex(value.getBytes())));//20个字节TOKEN码
						for( int i=10;i<38;i++){
							frame[i]=(byte)(frame[i]+0x33);
						}
						frame[38] = ParseTool.calculateCS(frame, 0, 38);
						frame[39]=0x16;
					}else if(dkey.equals("0700000100")){ //电表控制命令,下面也有一个0700000100，目前先替换掉
						frame = new byte[28];
						frame[0] = 0x68;
						ParseTool.HexsToBytesAA(frame,1,maddr,6,(byte)0xAA);				
						frame[7]=0x68;	
						frame[8]=0x1C;
						frame[9]=0x10;
						ParseTool.HexsToBytes(frame, 10, "02");//密码权限
						ParseTool.HexsToBytes(frame, 11, "000000");//3个字节密码
						ParseTool.HexsToBytes(frame, 14, "00000000");//4个字节操作者代码
						String[] values = value.split("#"); //
						ParseTool.HexsToBytes(frame, 18, values[0]);
						ParseTool.HexsToBytes(frame, 19, "00");
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						Date date = sdf.parse(values[1]);
						sdf = new SimpleDateFormat("yyMMddHHmmss");
						ParseTool.HexsToBytes(frame,20,sdf.format(date));
						for(int i=10;i<26;i++){
							frame[i]=(byte)(frame[i]+0x33);
						}
						frame[26] = ParseTool.calculateCS(frame, 0, 26);
						frame[27] = 0x16;
						
					}else if(dkey.equals("070204FF")||dkey.equals("070201FF")||dkey.equals("070202FF")||dkey.equals("070203FF")){//密钥更新
						frame=new byte[60];
						frame[0]=0x68;
						ParseTool.HexsToBytesAA(frame,1,maddr,6,(byte)0xAA);				
						frame[7]=0x68;	
						String keyUpdate=(String)para.getProperty("keyUpdate");
						frame[8]=0x03;
						frame[9]=0x30;						
						ParseTool.HexsToBytes(frame,10,dkey);//标识
						ParseTool.HexsToBytes(frame,14,"00000000");//操作者代码
						ParseTool.HexsToBytes(frame,18,keyUpdate.substring(0,16));//密钥信息+MAC
						ParseTool.HexsToBytes(frame,26,keyUpdate.substring(16,80));//参数更新文件线路保护密钥
						for (int i=10;i<58;i++)
							frame[i]=(byte)(frame[i]+0x33);
						frame[58]=ParseTool.calculateCS(frame,0,58);	//cs
						frame[59]=0x16;	//cs
					}
					else if (dkey.equals("0700000100")){//电表控制命令
						frame=new byte[40];
						frame[0]=0x68;
						ParseTool.HexsToBytesAA(frame,1,maddr,6,(byte)0xAA);				
						frame[7]=0x68;	
						String userControl=(String)para.getProperty("userControl");
						frame[8]=0x1C;
						frame[9]=0x1C;
						ParseTool.HexsToBytes(frame,10,"02");//权限
						ParseTool.HexsToBytes(frame,11,"000000");//密码
						ParseTool.HexsToBytes(frame,14,"00000000");//操作者代码
						ParseTool.HexsToBytes(frame,18,userControl);//密文
						for (int i=10;i<38;i++)
							frame[i]=(byte)(frame[i]+0x33);
						frame[38]=ParseTool.calculateCS(frame,0,38);	//cs
						frame[39]=0x16;	//cs
					}else if(dkey.equals("04000103")){//广播对时
						frame=new byte[18];
						frame[0]=0x68;
						ParseTool.HexsToBytesAA(frame,1,maddr,6,(byte)0xAA);
						frame[7]=0x68;
						String time =(String) para.getProperty("time");
						frame[8]=0x08;
						frame[9]=0x06;
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						Date date = sdf.parse(time);
						sdf = new SimpleDateFormat("yyMMddHHmmss");
						time=sdf.format(date);
						ParseTool.HexsToBytesAA(frame,10,time,6,(byte)0xAA);	
						frame[16]=ParseTool.calculateCS(frame, 0, 16);
						frame[17]=0x16;
					}else if(value!=null ){
						if(dkey.equals("04000101")){
							frame=new byte[28];
							frame[0]=0x68;
							ParseTool.HexsToBytesAA(frame,1,maddr,4,(byte)0xAA);
							frame[7]=0x68;
							frame[8]=0x14;
							frame[9]=16;
							ParseTool.HexsToBytes(frame, 10, dkey); //id
							ParseTool.HexsToBytes(frame, 14, "02");
							ParseTool.HexsToBytes(frame, 15, "000000");//pas
							ParseTool.HexsToBytes(frame, 18, "00000000");//pas
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							Date date = sdf.parse(value);
							sdf = new SimpleDateFormat("yyMMdd");
							Calendar c = Calendar.getInstance();
							c.setTime(date);
							value=sdf.format(date)+"0"+(c.get(Calendar.DAY_OF_WEEK)-1);
							ParseTool.HexsToBytes(frame, 22, value);//pas
							for(int i=10;i<10+16;i++){
								frame[i] = (byte) (frame[i]+0x33);
							}
							frame[26]=ParseTool.calculateCS(frame, 0, 26);
							frame[27]=0x16;
						}else if (dkey.equals("04000102")){
							frame=new byte[27];
							frame[0]=0x68;
							ParseTool.HexsToBytesAA(frame,1,maddr,4,(byte)0xAA);
							frame[7]=0x68;
							frame[8]=0x14;
							frame[9]=15;
							ParseTool.HexsToBytes(frame, 10, dkey); //id
							ParseTool.HexsToBytes(frame, 14, "02");
							ParseTool.HexsToBytes(frame, 15, "000000");//pas
							ParseTool.HexsToBytes(frame, 18, "00000000");//pas
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							Date date = sdf.parse(value);
							sdf = new SimpleDateFormat("HHmmss");
							Calendar c = Calendar.getInstance();
							c.setTime(date);
							value=sdf.format(c.getTime());
							ParseTool.HexsToBytes(frame, 22, value);//pas
							for(int i=10;i<10+15;i++){
								frame[i] = (byte) (frame[i]+0x33);
							}
							frame[25]=ParseTool.calculateCS(frame, 0, 25);
							frame[26]=0x16;
						}
					}
					else{
						frame=new byte[16];
						frame[0]=0x68;
						ParseTool.HexsToBytesAA(frame,1,maddr,6,(byte)0xAA);				
						frame[7]=0x68;	
						frame[8]=0x11;
						frame[9]=0x04;
						ParseTool.HexsToBytes(frame,10,dkey);
						frame[10]=(byte)(frame[10]+0x33);
						frame[11]=(byte)(frame[11]+0x33);
						frame[12]=(byte)(frame[12]+0x33);
						frame[13]=(byte)(frame[13]+0x33);
						frame[14]=ParseTool.calculateCS(frame,0,14);	//cs
						frame[15]=0x16;	//cs
					}
				}
				
			}
		}catch(Exception e){
			
		}
		return frame;
	}

	public Object[] parser(byte[] data, int loc, int len,BizRtu rtu) {
		List result=null;
		try{
			BbMeterFrame frame=new BbMeterFrame();
			frame.parse(data,loc,len);
			if(frame.getDatalen()>=0){	//数据中包含表帧且帧中包含数据
				result=new ArrayList();
				//抽取表帧数据
				int datalen=frame.getDatalen();
				String meteraddr=frame.getMeteraddr();	//表地址
				DataItem ma=new DataItem();
				ma.addProperty("value",meteraddr);
				ma.addProperty("datakey","8902");
				result.add(ma);
				
				int ctrl=frame.getCtrl();	/*控制码*/
				if((ctrl&0x10)==0x10||(ctrl&0x83)==0x83){//07版全国电表规约读取返回控制码
					byte[] framedata=frame.getData();					
					int pos=frame.getPos();
					switch(ctrl & 0x0F){
						case 1:	//读数据返回
						case 3:	//身份认证返回
							if ((ctrl&0xC3)==0xC3){//身份认证失败
								ma=new DataItem();
								ma.addProperty("value",ParseTool.BytesToHexC(framedata,pos,2));
								ma.addProperty("datakey","07000000FF");
								result.add(ma);
							}
							else{
								String datakey=ParseTool.BytesToHexC(framedata,pos,4);
								datakey=dataset.getConvertCode(datakey);//通过规约数据项找内部编码
								MeterProtocolDataItem item=dataset.getDataItem(datakey);
								pos+=4;
								if(item!=null){//支持的数据标识
									parseValues(framedata,pos,item,result,Protocol.BBMeter07);
								}
							}						
							break;

						case 12:	//控制命令返回
							if ((ctrl&0xDC)==0xDC){//控制命令异常返回
								ma=new DataItem();
								ma.addProperty("value",ParseTool.BytesToHexC(framedata,pos,1));
								ma.addProperty("datakey","0700000100");
								result.add(ma);
							}
							else{//控制命令成功返回							
								ma=new DataItem();
								ma.addProperty("value","00");
								ma.addProperty("datakey","0700000100");
								result.add(ma);
							}
							
							break;
						case 4: //设置返回
							String res ="";
							if(frame.getDatalen()==0){
								//成功
								res = "00";
							}else{
								res = ParseTool.BytesToHex(framedata, pos, 1);
							}
							//设置获得不了datakey
							ma=new DataItem();
							ma.addProperty("value", res);
							ma.addProperty("datakey", "0500000701");
							result.add(ma);
							break;
						default:
							break;
					}
					
				}else if((ctrl&0x20)==0x20){//TOKEN写返回
					byte[] framedata = frame.getData();
					int pos = frame.getPos();
					ma = new DataItem();
					ma.addProperty("datakey", "0800000100");
					String res = "";
					if(ctrl==(byte)0xE0){//TOKEN写返回异常
						res = ""+Integer.parseInt(ParseTool.BytesToHexC(framedata, pos, 2),16);
					}else{
						if(frame.getLen()==12){ //正常充值返回
							res=""+ParseTool.BytesToHexC(framedata, pos-1, 1);
						}else{//注销返回带有TOKEN码
							byte[] token = new byte[20];
							System.arraycopy(framedata, pos,token, 0,20);
							res="0:"+new String(token);
						}
					}
					ma.addProperty("value", res);
					result.add(ma);
					
				}else if((ctrl & 0x40)<=0){//正常应答
					byte[] framedata=frame.getData();
					int pos=frame.getPos();
					String datakey=ParseTool.BytesToHexC(framedata,pos,2);
					MeterProtocolDataItem item=null;
					switch(ctrl & 0x1F){
						case 1:	//读数据
							datakey=ParseTool.BytesToHexC(framedata,pos,2);
							datakey=dataset.getConvertCode(datakey);//通过规约数据项找内部编码
							item=dataset.getDataItem(datakey);
							pos+=2;
							if(item!=null){//支持的数据标识
								parseValues(framedata,pos,item,result,Protocol.BBMeter97);
							}
							break;
						case 4://97规约写数据返回
							datakey=dataset.getConvertCode(datakey);//通过规约数据项找内部编码
							item=dataset.getDataItem(datakey);
							DataItem ma2=new DataItem();
							if(datakey.equals("0700001600")){
								ma2.addProperty("value",framedata[12]);
							}else{
								ma2.addProperty("value","00");
							}
							ma2.addProperty("datakey",datakey);
							result.add(ma2);
							break;
						default:
							break;
					}
				}
				else{//异常应答
					byte[] framedata=frame.getData();
					int pos=frame.getPos();
					String datakey=ParseTool.BytesToHexC(framedata,pos,2);
					MeterProtocolDataItem item=null;
					switch(ctrl & 0x1F){
						case 1:	//读数据
							datakey=ParseTool.BytesToHexC(framedata,pos,2);
							datakey=dataset.getConvertCode(datakey);//通过规约数据项找内部编码
							item=dataset.getDataItem(datakey);
							DataItem ma1=new DataItem();
							ma1.addProperty("value","");
							ma1.addProperty("datakey",datakey);
							result.add(ma1);
							break;
						case 4://97规约写数据返回
							datakey=(String) rtu.getParamFromMap(9999);//通过规约数据项找内部编码
							datakey=dataset.getConvertCode(datakey);//通过规约数据项找内部编码
							item=dataset.getDataItem(datakey);
							DataItem ma2=new DataItem();
							if(datakey.equals("0700001600")){
								ma2.addProperty("value","FF");//失败FF
							}else{
								ma2.addProperty("value","01");
							}
							ma2.addProperty("datakey",datakey);
							result.add(ma2);
							break;
						default:
							break;
					}
				
				}
			}
		}catch(Exception e){
			log.error("部颁表规约",e);
		}
		if(result!=null){
			return result.toArray();
		}
		return null;
	}
	public Object[] parser(String key ,String data,String meteraddr) {
		List result=null;
		try{			
			result=new ArrayList();
			DataItem ma=new DataItem();
			ma.addProperty("value",meteraddr);
			ma.addProperty("datakey","8902");
			result.add(ma);
						
			key=dataset.getConvertCode(key);//通过规约数据项找内部编码
			MeterProtocolDataItem item=dataset.getDataItem(key);
			if(item!=null){//支持的数据标识
				parseValues(HexDump.toByteBuffer(data).array(),0,item,result,"");
			}
										
		}catch(Exception e){
			log.error("部颁表规约",e);
		}
		if(result!=null){
			return result.toArray();
		}
		return null;
	}
	
	
	/**
	 * 通过内部数据项标识获取07版全国规约的数据项标识
	 * @param codes		内部数据项标识 		例0100100000
	 * @param results	对应表规约的数据项标识	例00010000
	 */
	public String[] getMeter2Code(String[] codes){
		String[] rtCodes=null;
		if(codes!=null&&codes.length>0){
			rtCodes=new String[codes.length];
			for(int i=0;i<codes.length;i++){
				MeterProtocolDataItem item=dataset.getDataItem(codes[i]);
				rtCodes[i]=item.getParentCode2();
			}
		}
		
		return rtCodes;
	}
	/**
	 * 通过内部数据项标识获取97版全国规约的数据项标识
	 * @param codes		内部数据项标识 		例0100100000
	 * @param results	对应表规约的数据项标识	例9010
	 */
	public String[] getMeter1Code(String[] codes){
		String[] rtCodes=null;
		if(codes!=null&&codes.length>0){
			rtCodes=new String[codes.length];
			for(int i=0;i<codes.length;i++){
				MeterProtocolDataItem item=dataset.getDataItem(codes[i]);
				rtCodes[i]=item.getParentCode1();
			}
		}
		
		return rtCodes;
	}
	/**
	 * 解析表数据
	 * @param data		表帧
	 * @param pos		当前解析开始位置
	 * @param item		当前待解析数据项
	 * @param results	结果集
	 */
	private int parseValues(byte[] data,int pos,MeterProtocolDataItem item,List results,String protocol){
		int rt=0;
		try{
			int loc=pos;
			if(item.getChildarray()!=null && item.getChildarray().size()>0){
				List children=item.getChildarray();
				for(int i=0;i<children.size();i++){
					if((data[loc] & 0xFF)==BbMeterFrame.FLAG_BLOCK_DATA){//数据块结束
						rt+=1;
						break;
					}
					if(loc>=data.length){//无数据可以解析，也没遇到块结束符，理论上应该是错误发生，先忽略
						break;
					}
					int vlen=parseValues(data,loc,(MeterProtocolDataItem)children.get(i),results,protocol);
					if(vlen<=0){
						rt=0;
						break;
					}
					loc+=vlen;
					rt+=vlen;
				}
			}else{
				DataItem di=new DataItem();
				di.addProperty("datakey",item.getCode());
				Object val=parseItem(data,pos,item,protocol);
				di.addProperty("value",val);
				results.add(di);
				if (protocol.equals(Protocol.BBMeter07)){//07版电表规约数据项格式与97版不一致的时候取备用	
					if (item.getLength2()>=0&&item.getLength2()!=item.getLength())
						rt=item.getLength2();	
					else
						rt=item.getLength();
				}
				else
					rt=item.getLength();
			}
		}catch(Exception e){
			rt=0;
			log.error("解析部颁表数据",e);
		}
		return rt;
	}
	
	/**
	 * 解析表数据项
	 * @param frame
	 * @param loc
	 * @param mpd
	 * @return
	 */
	private Object parseItem(byte[] frame,int loc,MeterProtocolDataItem mpd,String protocol){
		Object val=null;
		if (protocol.equals(Protocol.BBMeter07)){//07版电表规约数据项格式与97版不一致的时候取备用
			int len=mpd.getLength(),fraction=mpd.getFraction();
			if (mpd.getFraction2()>=0&&mpd.getFraction2()!=mpd.getFraction())
				fraction=mpd.getFraction2();
			if (mpd.getLength2()>=0&&mpd.getLength2()!=mpd.getLength())
				len=mpd.getLength2();
			val=DataItemParser.parsevalue(frame,loc,len,fraction,mpd.getType());
		}else
			val=DataItemParser.parsevalue(frame,loc,mpd.getLength(),mpd.getFraction(),mpd.getType());
		return val;
	}

	@Override
	public Object[] parser(byte[] data, int loc, int len) {
		// TODO Auto-generated method stub
		return null;
	}
}
