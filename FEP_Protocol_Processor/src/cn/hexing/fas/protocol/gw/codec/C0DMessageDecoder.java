package cn.hexing.fas.protocol.gw.codec;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cn.hexing.db.managertu.ManageRtu;
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
import cn.hexing.fk.model.RtuTask;
import cn.hexing.fk.utils.HexDump;

/**
 * 二类数据(功能码：0DH)响应消息解码器
 * 
 */
public class C0DMessageDecoder extends AbstractMessageDecoder{
	public Object decode(IMessage message) {
		List<RtuData> datas = new ArrayList<RtuData>();
		HostCommand hc=new HostCommand();
		int dataType=0;	
		String sTaskNum=null;	
		List<String> codesTemp=new ArrayList<String>();
		String rtuAddr=HexDump.toHex(message.getRtua());	
		try{
			String sTaskDateTime="";					
			String data=ParseTool.getMsgData(message);			
			MessageGw msg = (MessageGw) message;
			
			if (msg.head.seq_tpv==1)//TpV位置1表示附加域有时间标签
				data=data.substring(0,data.length()-12);//消去6个字节的时间标签
			if (msg.head.c_acd==1)//ACD位置1表示附加域有事件计数器
				data=data.substring(0,data.length()-4);//消去2个字节的事件计数器			
			DataValue dataValue=new DataValue();				
			while(data.length()>=12){//数据区至少要包含一个数据单元标识+最短时间标签，暂时不考虑上行携带附加域的情况
				int[] tn=DataItemParser.measuredPointParser(data.substring(0,4));
				String[] codes= DataItemParser.dataCodeParser(data.substring(4,8), "0D");	
				data=data.substring(8);
				DataTimeTag dataTimeTag=new DataTimeTag() ;				
				for (int i=0;i<tn.length;i++){					
					for (int j=0;j<codes.length;j++){
						ProtocolDataItemConfig pdc=(ProtocolDataItemConfig)super.dataConfig.getDataItemConfig(codes[j]);							
						List<ProtocolDataItemConfig> childItems=pdc.getChildItems();
						dataType=getGw0DDataType(Integer.parseInt(codes[j].substring(3,6)));
						HostCommand hcTemp=new HostCommand();
						//曲线数据有7个字节数据时标
						if (dataType==0){		
							//小时冻结时标解析得到：开始时间(YYYYMMDDHHNN)+时间间隔+数据点数
							dataTimeTag=DataItemParser.getTaskDateTimeInfo(data.substring(0,14), 2,rtuAddr);
							data=data.substring(14);						
							for(int k=0;k<dataTimeTag.getDataCount();k++){
								if (childItems.size()>0){//任务返回才匹配任务号	
									sTaskDateTime=DataSwitch.IncreaseDateTime(dataTimeTag.getDataTime(),dataTimeTag.getDataDensity() * k, 2);
									for (ProtocolDataItemConfig pc:childItems){
										dataValue=DataItemParser.parseValue(data.substring(0,pc.getLength()*2), pc.getFormat());
										data=data.substring(pc.getLength()*2);	
										HostCommandResult hcr=new HostCommandResult();
										hcr.setCode(pc.getCode());
										hcr.setTn(""+tn[i]);
										hcr.setValue(dataValue.getValue());
										hcTemp.addResult(hcr);
										if(k==dataTimeTag.getDataCount()-1)//曲线数据每个点数据项都一致，只需留最后一个
											codesTemp.add(hcr.getCode());
									}													
									if (message.isTask()){//任务数据							
										RtuData rd=new RtuData();
										rd.setLogicAddress(rtuAddr);
										rd.setTn(""+tn[i]);								
										rd.setTime(sTaskDateTime);
										for(HostCommandResult hcr:hcTemp.getResults()){
											RtuDataItem rdItem=new RtuDataItem();
											rdItem.setCode(hcr.getCode());
											rdItem.setValue(hcr.getValue());	
											rd.addDataList(rdItem);	
										}	
										datas.add(rd);									
									}
									else{//招测返回
										for(HostCommandResult hcr:hcTemp.getResults()){
											hcr.setValue(sTaskDateTime+"#"+hcr.getValue());
											hc.addResult(hcr);
										}										
									}
									hcTemp.getResults().clear();																							
								}																				
							}						
						}	
						else{//冻结数据					
							if (dataType==1){
								//日冻结时标3个字YYYYMMDD
								dataTimeTag=DataItemParser.getTaskDateTimeInfo(data.substring(0,6), 3,rtuAddr);
								data=data.substring(6);	
							}
							else{	
								//月冻结时标2个字YYYYMM
								dataTimeTag=DataItemParser.getTaskDateTimeInfo(data.substring(0,4), 4,rtuAddr);
								data=data.substring(4);	
							}						
							for (int k=0;k<childItems.size();k++){
								ProtocolDataItemConfig pc=(ProtocolDataItemConfig)childItems.get(k);						
								//特殊变量N:后续数据项为逐项扩展N个数据项
								if (pc.getCode().equals("0000000000")||pc.getCode().equals("0000000001")||pc.getCode().equals("0000000002")){
									String tag=pc.getCode().substring(8,10);//后续数据项起始值
									DataValue nValue=new DataValue();
									nValue=DataItemParser.parseValue(data.substring(0,pc.getLength()*2),pc.getFormat());
									data=data.substring(pc.getLength()*2);	
									if (pc.getCode().equals("0000000000")){//此类变量用于费率,所以数值需要+1
										nValue.setValue(""+(Integer.parseInt(nValue.getValue())+1));
									}
									else if (pc.getCode().equals("0000000002")){//此类变量用于谐波N次(2~19),所以数值需要-1
										nValue.setValue(""+(Integer.parseInt(nValue.getValue())-1));
									}
									for(int m=k+1;m<childItems.size();m++){
										pc=(ProtocolDataItemConfig)childItems.get(m);
										for(int n=0;n<Integer.parseInt(nValue.getValue());n++){
											dataValue=DataItemParser.parseValue(data.substring(0,pc.getLength()*2),pc.getFormat());
											data=data.substring(pc.getLength()*2);
											HostCommandResult hcr=new HostCommandResult();
											hcr.setCode(DataSwitch.StrStuff("0", 10,""+(Long.parseLong(pc.getCode().substring(0,8)+tag)+n),"left"));
											hcr.setTn(""+tn[i]);
											hcr.setValue(dataValue.getValue());
											hcTemp.addResult(hcr);	
											codesTemp.add(hcr.getCode());
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
											hcTemp.addResult(hcr);	
											codesTemp.add(hcr.getCode());
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
											hcTemp.addResult(hcr);	
											codesTemp.add(hcr.getCode());
										}																			
									}																	
									break;
								}
								else{
									dataValue=DataItemParser.parseValue(data.substring(0,pc.getLength()*2),pc.getFormat());
									data=data.substring(pc.getLength()*2);	
									HostCommandResult hcr=new HostCommandResult();
									hcr.setCode(pc.getCode());
									hcr.setTn(""+tn[i]);
									hcr.setValue(dataValue.getValue());
									hcTemp.addResult(hcr);		
									codesTemp.add(hcr.getCode());
								}								
							}
							if (codesTemp.size()>0){//有解析完成的数据
								if (message.isTask()){//匹配冻结数据任务							
									RtuData rd=new RtuData();
									rd.setLogicAddress(rtuAddr);
									rd.setTn(""+tn[i]);								
									rd.setTime(dataTimeTag.getDataTime());
									if(dataType==1){//日冻结数据将时标+1
										rd.setTime(rd.getNextday());
									}else{//月冻结数据时标将月+1
										rd.setTime(rd.getNextMonth());
									}
									for(HostCommandResult hcr:hcTemp.getResults()){
										RtuDataItem rdItem=new RtuDataItem();
										rdItem.setCode(hcr.getCode());
										rdItem.setValue(hcr.getValue());	
										rd.addDataList(rdItem);	
									}	
									datas.add(rd);
									
								}
								else{//招测返回
									RtuData rd = new RtuData();
									rd.setTime(dataTimeTag.getDataTime());
									String dataTime =dataTimeTag.getDataTime();
									//召测到的时间，日+1，月+1，倪周辉要求做更改
									SimpleDateFormat df = null ;
									if (dataTime.trim().length()==16){
										df= new SimpleDateFormat("yyyy-MM-dd HH:mm");
									}else if (dataTime.trim().length()==10){
										df = new SimpleDateFormat("yyyy-MM-dd");
									}else if (dataTime.trim().length()==7){
										df = new SimpleDateFormat("yyyy-MM");
									}else if(dataTime.trim().length()==19){
										df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
									}
									if(dataType==1){
										dataTimeTag.setDataTime(df.format(rd.getNextday()));
									}else{
										dataTimeTag.setDataTime(df.format(rd.getNextMonth()));
									}
									for(HostCommandResult hcr:hcTemp.getResults()){
										hcr.setValue(dataTimeTag.getDataTime()+"#"+hcr.getValue());
										hc.addResult(hcr);
									}										
								}
								hcTemp.getResults().clear();
							}
						}						
					}										
				}
			}	
			hc.setStatus(HostCommand.STATUS_SUCCESS);
		}catch(Exception e){
			throw new MessageDecodeException(e);
		}
		if (message.isTask()){	
			if (codesTemp.size()>0){//有解析完成的数据
				BizRtu rtu=RtuManage.getInstance().getBizRtuInCache(((MessageGw)message).head.rtua);
				try {
					sTaskNum = rtu.getTaskNum(codesTemp, rtu.getRtuType());
				} catch (Exception e) {
				}
				if (sTaskNum==null){
					//在这里重新刷新模板
					boolean canRefresh = rtu.isCanRefresh();
					if(canRefresh){
						//刷新终端任务
						ManageRtu.getInstance().refreshBizRtu(rtu.getLogicAddress());
						rtu=RtuManage.getInstance().getBizRtuInCache(rtu.getLogicAddress());
						//刷新模板
						Map<Integer, RtuTask> taskMap = rtu.getTasksMap();
						Iterator<Integer> it = taskMap.keySet().iterator();
						while(it.hasNext()){
							RtuTask rtuTask = taskMap.get(it.next());
							ManageRtu.getInstance().refreshTaskTemplate(rtuTask.getTaskTemplateID());
						}
					}
					sTaskNum=rtu.getTaskNum(codesTemp,rtu.getRtuType());
					if(sTaskNum==null){
						String error="Terminal "+rtuAddr+" Can't Find Task Template "+message.getRawPacketString();
						throw new MessageDecodeException(error);
					}else{
						codesTemp.clear();
						for(RtuData rd:datas)
							rd.setTaskNum(sTaskNum);
					}
				}
				else{
					codesTemp.clear();
					for(RtuData rd:datas)
						rd.setTaskNum(sTaskNum);
				}
			}		
			return datas;
		}
		else
			return hc;
	}
	
	//判断二类数据类型:0曲线数据；1日冻结；2月冻结
	public int getGw0DDataType(int fn){
		//默认月冻结
		int type=2;
		//日冻结
		if ((fn>=1&&fn<=12)||(fn>=25&&fn<=32)||(fn>=41&&fn<=43)||(fn==45)||
			(fn>=49&&fn<=50)||(fn==53)||(fn>=57&&fn<=59)||(fn>=113&&fn<=129)||
			(fn>=153&&fn<=156)||(fn>=161&&fn<=176)||(fn>=185&&fn<=192)||(fn==209) || (fn==254)){
			type=1;
		}
		//曲线
		else if((fn>=73&&fn<=110)||(fn>=138&&fn<=149)||(fn>=217&&fn<=218)){
			type=0;
		}
		return type;
	}

	
		
}
