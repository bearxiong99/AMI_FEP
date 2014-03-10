package cn.hexing.fas.protocol.gw.codec;


import java.util.ArrayList;
import java.util.List;

import cn.hexing.exception.MessageDecodeException;
import cn.hexing.fas.model.HostCommand;
import cn.hexing.fas.model.HostCommandResult;
import cn.hexing.fas.model.RtuData;
import cn.hexing.fas.model.RtuDataItem;
import cn.hexing.fas.protocol.conf.ProtocolDataItemConfig;
import cn.hexing.fas.protocol.gw.parse.DataItemParser;
import cn.hexing.fas.protocol.gw.parse.DataSwitch;
import cn.hexing.fas.protocol.gw.parse.DataTimeTag;
import cn.hexing.fas.protocol.gw.parse.DataValue;
import cn.hexing.fas.protocol.gw.parse.ParseTool;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.gw.MessageGw;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.model.RtuManage;
import cn.hexing.fk.utils.HexDump;

/**
 * 一类数据(功能码：0CH)响应消息解码器
 * 
 */
public class C0CMessageDecoder  extends AbstractMessageDecoder{

	public Object decode(IMessage message) {
		List<RtuData> datas = new ArrayList<RtuData>();
		HostCommand hc=new HostCommand();
		List<String> codesTemp=new ArrayList<String>();
		String rtuAddr=HexDump.toHex(message.getRtua());				
		String sTaskNum=null;				
		try{
			String sTaskDateTime="";					
			String data=ParseTool.getMsgData(message);	
			MessageGw msg = (MessageGw) message;
			if (msg.head.seq_tpv==1)//TpV位置1表示附加域有时间标签
				data=data.substring(0,data.length()-12);//消去6个字节的时间标签
			if (msg.head.c_acd==1)//ACD位置1表示附加域有事件计数器
				data=data.substring(0,data.length()-4);//消去2个字节的事件计数器
			DataValue dataValue=new DataValue();				
			while(data.length()>=8){//数据区至少要包含一个数据单元标识，暂时不考虑上行携带附加域的情况
				int[] tn=DataItemParser.measuredPointParser(data.substring(0,4));
				String[] codes= DataItemParser.dataCodeParser(data.substring(4,8), "0C");	
				data=data.substring(8);				
				for (int i=0;i<tn.length;i++){					
					for (int j=0;j<codes.length;j++){
						ProtocolDataItemConfig pdc=(ProtocolDataItemConfig)super.dataConfig.getDataItemConfig(codes[j]);							
						List<ProtocolDataItemConfig> childItems=pdc.getChildItems();						
						//小时冻结数据有2个字节冻结时标
						if (Integer.parseInt(codes[j].substring(3,6))>=81&&
							Integer.parseInt(codes[j].substring(3,6))<=121){	
							codesTemp.add(childItems.get(0).getCode());
							//小时冻结时标解析得到：开始时间(YYYYMMDDHHNN)+时间间隔+数据点数
							DataTimeTag dataTimeTag=DataItemParser.getTaskDateTimeInfo(data.substring(0,4), 1,rtuAddr);
							data=data.substring(4);						
							for(int k=0;k<dataTimeTag.getDataCount();k++){						
								if (childItems.size()>0){//任务返回才匹配任务号						
									dataValue=DataItemParser.parseValue(data.substring(0,childItems.get(0).getLength()*2), childItems.get(0).getFormat());
									data=data.substring(childItems.get(0).getLength()*2);														
									sTaskDateTime=DataSwitch.IncreaseDateTime(dataTimeTag.getDataTime(),dataTimeTag.getDataDensity() * k, 2);
									if (message.isTask()){//任务返回才匹配任务号																				
										RtuData rd=new RtuData();
										rd.setLogicAddress(rtuAddr);
										rd.setTn(""+tn[i]);										
										rd.setTime(sTaskDateTime);
										RtuDataItem rdItem=new RtuDataItem();
										rdItem.setCode(childItems.get(0).getCode());
										rdItem.setValue(dataValue.getValue());	
										rd.addDataList(rdItem);																																										
										datas.add(rd);										
									}
									else{
										HostCommandResult hcr=new HostCommandResult();
										hcr.setCode(childItems.get(0).getCode());
										hcr.setTn(""+tn[i]);
										hcr.setValue(sTaskDateTime+"#"+dataValue.getValue());
										hc.addResult(hcr);
										hc.setStatus(HostCommand.STATUS_SUCCESS);
									}																
								}																				
							}							
						}	
						else{
							if(childItems == null){
								if(pdc.getLength()==0)//变长数据项
									dataValue=DataItemParser.parser(data, pdc.getFormat(),false);
								else
									dataValue=DataItemParser.parseValue(data.substring(0,pdc.getLength()*2),pdc.getFormat());
								data=data.substring(dataValue.getLen());	
								HostCommandResult hcr=new HostCommandResult();
								hcr.setCode(pdc.getCode());
								hcr.setTn(""+tn[i]);
								hcr.setValue(dataValue.getValue());
								hc.addResult(hcr);	
							}else{
								for (int k=0;k<childItems.size();k++){
									ProtocolDataItemConfig pc=(ProtocolDataItemConfig)childItems.get(k);						
									//特殊变量N:后续数据项为逐项扩展N个数据项
									if (pc.getCode().equals("0000000000")||pc.getCode().equals("0000000001")||pc.getCode().equals("0000000002")||pc.getCode().equals("0000000003")){
										String tag=pc.getCode().substring(8,10);//后续数据项起始值
										DataValue nValue=new DataValue();
										nValue=DataItemParser.parseValue(data.substring(0,pc.getLength()*2),pc.getFormat());
										if (pc.getCode().equals("0000000000")){//此类变量用于费率,所以数值需要+1
											nValue.setValue(""+(Integer.parseInt(nValue.getValue())+1));
										}
										else if (pc.getCode().equals("0000000002")){//此类变量用于谐波N次(2~19),所以数值需要-1
											nValue.setValue(""+(Integer.parseInt(nValue.getValue())-1));
										}
										data=data.substring(pc.getLength()*2);	
										String nTag=pc.getCode();
										for(int m=k+1;m<childItems.size();m++){
											pc=(ProtocolDataItemConfig)childItems.get(m);
											if (nTag.equals("0000000003")){//此类变量用于前3个谐波电压含有率1~19次，后3个电流2~19次
												if(m>=4)//后续数据项起始值
													tag="02";
												else
													tag="01";
												if(m==4)
													nValue.setValue(""+(Integer.parseInt(nValue.getValue())-1));
											}
											for(int n=0;n<Integer.parseInt(nValue.getValue());n++){
												dataValue=DataItemParser.parseValue(data.substring(0,pc.getLength()*2),pc.getFormat());
												data=data.substring(pc.getLength()*2);
												HostCommandResult hcr=new HostCommandResult();
												hcr.setCode(DataSwitch.StrStuff("0", 10,""+(Long.parseLong(pc.getCode().substring(0,8)+tag)+n),"left"));
												hcr.setTn(""+tn[i]);
												hcr.setValue(dataValue.getValue());
												hc.addResult(hcr);		
											}
										}
										break;
									}
									//特殊变量M:后续数据项整体为一个单元扩展N个单元
									else if(pc.getCode().equals("X000000000")){
										DataValue nValue=new DataValue();
										nValue=DataItemParser.parseValue(data.substring(0,pc.getLength()*2),pc.getFormat());
										data=data.substring(pc.getLength()*2);	
										for(int n=0;n<Integer.parseInt(nValue.getValue())+1;n++){
											for(int m=k+1;m<childItems.size();m++){
												pc=(ProtocolDataItemConfig)childItems.get(m);
												dataValue=DataItemParser.parseValue(data.substring(0,pc.getLength()*2),pc.getFormat());
												data=data.substring(pc.getLength()*2);
												HostCommandResult hcr=new HostCommandResult();
												hcr.setCode(DataSwitch.StrStuff("0", 10,""+(Long.parseLong(pc.getCode().substring(0,8)+"00")+n),"left"));
												hcr.setTn(""+tn[i]);
												hcr.setValue(dataValue.getValue());
												hc.addResult(hcr);		
											}																			
										}																	
										break;
									}
									//凡是数据子项为Y开头，则后续数据项整体为一个单元扩展N个单元
									else if(pc.getCode().indexOf("Y")>0){
										DataValue nValue=new DataValue();
										nValue=DataItemParser.parseValue(data.substring(0,pc.getLength()*2),pc.getFormat());
										data=data.substring(pc.getLength()*2);	
										for(int n=0;n<Integer.parseInt(nValue.getValue());n++){
											for(int m=k+1;m<childItems.size();m++){
												pc=(ProtocolDataItemConfig)childItems.get(m);
												dataValue=DataItemParser.parseValue(data.substring(0,pc.getLength()*2),pc.getFormat());
												data=data.substring(pc.getLength()*2);
												HostCommandResult hcr=new HostCommandResult();
												hcr.setCode(pc.getCode().substring(0,8)+n+pc.getCode().substring(9,10));
												//hcr.setCode(DataSwitch.StrStuff("0", 10,""+(Long.parseLong(pc.getCode())+n*10),"left"));
												hcr.setTn(""+tn[i]);
												hcr.setValue(dataValue.getValue());
												hc.addResult(hcr);		
											}																			
										}																	
										break;
									}
									else{
										if(pc.getLength()==0)//变长数据项
											dataValue=DataItemParser.parser(data, pdc.getFormat(),false);
										else
											dataValue=DataItemParser.parseValue(data.substring(0,pc.getLength()*2),pc.getFormat());
										data=data.substring(dataValue.getLen());	
										HostCommandResult hcr=new HostCommandResult();
										hcr.setCode(pc.getCode());
										hcr.setTn(""+tn[i]);
										hcr.setValue(dataValue.getValue());
										hc.addResult(hcr);		
									}								
								}
							}
							hc.setStatus(HostCommand.STATUS_SUCCESS);
						}
					}						
				}
			}		
		}catch(Exception e){
			throw new MessageDecodeException(e);
		}
		if (message.isTask()){	
			if (codesTemp.size()>0){//有解析完成的数据
				BizRtu rtu=RtuManage.getInstance().getBizRtuInCache(((MessageGw)message).head.rtua);
				sTaskNum=rtu.getTaskNum(codesTemp,rtu.getRtuType());	
				codesTemp.clear();
				if (sTaskNum==null){
					String error="终端："+rtuAddr+"任务报文无法找到匹配的任务模版,"+message.getRawPacketString();	            						
					throw new MessageDecodeException(error);
				}
				else{
					for(RtuData rd:datas)
						rd.setTaskNum(sTaskNum);
				}
			}		
			return datas;
		}
		else
			return hc;
	}		
}
