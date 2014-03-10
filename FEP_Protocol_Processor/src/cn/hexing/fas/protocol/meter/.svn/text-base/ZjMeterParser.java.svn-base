package cn.hexing.fas.protocol.meter;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.hexing.fas.protocol.data.DataItem;
import cn.hexing.fas.protocol.meter.conf.MeterProtocolDataItem;
import cn.hexing.fas.protocol.meter.conf.MeterProtocolDataSet;
import cn.hexing.fas.protocol.zj.parse.DataItemParser;
import cn.hexing.fas.protocol.zj.parse.ParseTool;
import cn.hexing.fk.model.BizRtu;


/**
 * 浙江表规约解析器
 * 			浙江表规约
 * 		0X68-------------帧头标识1
 * 		L   -------------数据长（从表地址开始到校验和之前的所有字节长度）
 * 		L   -------------数据长（从表地址开始到校验和之前的所有字节长度）
 * 		0X68-------------帧头标识2
 * 		addr-------------表地址
 * 		CMDL-------------命令字低字节
 * 		CMDH-------------命令字高字节
 * 		Data-------------数据区
 * 		CS  -------------校验和（数据长对应字节的和模256）
 * 		0X0D-------------帧尾标识
 */
public class ZjMeterParser implements IMeterParser{
	private final Log log=LogFactory.getLog(ZjMeterParser.class);
	private MeterProtocolDataSet dataset;
	
	public ZjMeterParser(){
		try{
			dataset=MeterProtocolFactory.createMeterProtocolDataSet("ZJMeter");
		}catch(Exception e){
			log.error("浙江表规约初始化失败");
		}
	}
	
	public String[] convertDataKey(String[] datakey) {
		String[] rt=null;
		try{
			
		}catch(Exception e){
			log.error("convert datakey",e);
		}		
		return fixCode(rt);
	}
	
	/**
	 * 修正数据编码，同一数据块内数据以数据块编码召测
	 * @param codes
	 * @return
	 */
	private String[] fixCode(String[] codes){
		String[] rt=null;
		try{			
			rt=new String[codes.length];
			rt[0]=codes[0];
			int j=1;
			for(int i=1;i<codes.length;i++){
				boolean fixed=false;
				for(int k=0;k<j;k++){
					if(rt[k].equalsIgnoreCase(codes[i])){	//重复的编码
						fixed=true;
						break;
					}else{
						if(rt[k].substring(0, 3).equalsIgnoreCase(codes[i].substring(0, 3))){
							fixed=true;
							rt[k]=rt[k].substring(0, 3)+"0";
						}
					}					
				}
				if(!fixed){	//未包含在前面的数据编码内
					rt[j]=codes[i];
					j++;
				}
			}
		}catch(Exception e){
			//
		}
		return rt;
	}
	
	private List getParents(MeterProtocolDataSet dataset){
		List rt=null;
		try{
			Hashtable dks=dataset.getDataset();
			Enumeration dkey=dks.elements();
			rt=new ArrayList();
			while(dkey.hasMoreElements()){
				MeterProtocolDataItem di=(MeterProtocolDataItem)dkey.nextElement();
				if(di.getChildarray()==null){
					continue;
				}
				if(di.getChildarray().size()<=0){
					continue;
				}
				rt.add(di);
			}
		}catch(Exception e){
			log.error("pretreatment protocol",e);
		}
		return rt;
	}
	
	/**
	 * 说明：浙江表规约比较特殊，不能召测多个数据标识，如果要召测多个数据项
	 * 		只能找出多个数据项的父标识（数据块标识）来召测
	 */
	public byte[] constructor(String[] datakey,DataItem para) {
		String dk="";
		byte[] frame=null;
		try{
			if((datakey!=null)&&(datakey.length>0)
				&&(para!=null)&&(para.getProperty("point")!=null)){	//check para				
				
				if(datakey.length==1){	//just one datakey
					dk=datakey[0];
				}else{
					dk=datakey[0].substring(0,3)+"0";
					for(int i=1;i<datakey.length;i++){
						if(dk.substring(0,2).equals(datakey[i].substring(0,2))){//0XXX中0X一定要相同
							if(!dk.substring(2,3).equals(datakey[i].substring(2,3))){
								if(!dk.substring(2,3).equals("0")){
									dk=dk.substring(0,2)+"00";
								}
							}
						}else{
							//error！不可能召测两大类(浙江表规数据标识为XXX，我把它扩充为0XXX)
							log.info("目前只支持召测同类数据，非同类数据请分别召测！");
							dk="";
							break;
						}
					}
				}
				if(dk.length()>0){
					frame=new byte[9];
					constructFrameCallData(frame,dk,ParseTool.HexToByte((String)para.getProperty("point")));
				}				
			}			
		}catch(Exception e){
			log.error("Construct ZJ meter frame ",e);
		}
		return frame;
	}
	
	private void constructFrameCallData(byte[] frame,String datakey,byte maddr){
		frame[0]=0x68;
		frame[1]=0x3;
		frame[2]=0x3;
		frame[3]=0x68;
		frame[4]=maddr;
		ParseTool.HexsToBytes(frame,5,datakey);
		frame[7]=ParseTool.calculateCS(frame,4,3);
		frame[8]=0x0D;
	}
	public Object[] parser(String key,String data,String meteraddr) {
		return null;
	}
	/**
	 * 解析表帧
	 */
	public Object[] parser(byte[] data,int loc,int len) {
		List result=null;
		try{
			ZjMeterFrame frame=new ZjMeterFrame();
			frame.parse(data,loc,len);
			if(frame.getDatalen()>0){	//数据中包含表帧
				result=new ArrayList();
				//抽取表帧数据
				int datalen=frame.getDatalen();
				if(datalen==1){	//上行空数据帧
					//不处理
				}else{
					byte[] framedata=frame.getData();
					String meteraddr=frame.getMeteraddr();	//表地址
					DataItem item=new DataItem();
					item.addProperty("value",meteraddr);
					item.addProperty("datakey","8902");
					result.add(item);
					if(datalen==2){	//上行应答帧
						int rtype=(framedata[frame.getPos()+1] & 0xff);
						if(rtype==ZjMeterFrame.FLAG_REPLY_ERROR){
							//表应答错误帧
						}
						if(rtype==ZjMeterFrame.FLAG_REPLY_OK){
							//表应答确认帧
						}						
					}else{	//数据帧
						int iloc=frame.getPos();
						iloc++;
						while(iloc<(framedata.length-2)){
							String datakey=ParseTool.BytesToHexC(framedata,iloc,2);	//数据标识
							MeterProtocolDataItem mpd=dataset.getDataItem(dataset.getConvertCode(datakey));
							iloc+=2;	//移至数据开始位置
							if(mpd!=null){
								List children=mpd.getChildarray();
								if((children!=null)&& (children.size()>0)){	//数据类
									for(int ic=0;ic<children.size();ic++){
										MeterProtocolDataItem cmpd=(MeterProtocolDataItem)children.get(ic);
										if((framedata[iloc] & 0xff)==ZjMeterFrame.FLAG_BLOCK_DATA){	//块数据结束标志											
											break;
										}
										if((framedata[iloc] & 0xff)==ZjMeterFrame.FLAG_NO_DATA){	//数据缺失标志
											iloc++;
											continue;
										}
										//解析数据项										
										Object val=parseItem(framedata,iloc,cmpd);
										toZjDataItem(val,cmpd,result);
										iloc+=cmpd.getLength();
									}
									if((framedata[iloc] & 0xff)==ZjMeterFrame.FLAG_BLOCK_DATA){	//块数据结束标志											
										iloc++;
									}
								}else{	//数据项
									if((framedata[iloc] & 0xff)==ZjMeterFrame.FLAG_NO_DATA){	//数据缺失标志
										iloc++;										
									}else{										
										Object val=parseItem(framedata,iloc,mpd);
										toZjDataItem(val,mpd,result);
										iloc+=mpd.getLength();
									}									
								}
							}else{
								//错误数据标识
								break;
							}
						}
					}
				}				
			}
		}catch(Exception e){
			log.error("解析浙江表规约",e);
		}
		if(result!=null){
			return result.toArray();
		}
		return null;
	}
	private void toZjDataItem(Object val,MeterProtocolDataItem mpd,List result){
		try{
			DataItem item=new DataItem();
			item.addProperty("value",val);
			item.addProperty("datakey",mpd.getCode());
			result.add(item);
		}catch(Exception e){
			log.error("convert to zj data",e);
		}
	}
	/**
	 * 解析表数据项
	 * @param frame
	 * @param loc
	 * @param mpd
	 * @return
	 */
	private Object parseItem(byte[] frame,int loc,MeterProtocolDataItem mpd){
		Object val=DataItemParser.parsevalue(frame,loc,mpd.getLength(),mpd.getFraction(),mpd.getType());
		return val;
	}
	
	/**
	 * 通过内部数据项标识获取对应表规约的数据项标识
	 * @param codes		内部数据项标识 		例0100100001
	 * @param results	对应表规约的数据项标识	例0111
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
	 * 通过内部数据项标识获取对应表规约的数据项标识
	 * @param codes		内部数据项标识 		例0100100001
	 * @param results	对应表规约的数据项标识	例0111
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

	@Override
	public Object[] parser(byte[] data, int loc, int len, BizRtu rtu) {
		// TODO Auto-generated method stub
		return null;
	}
}
