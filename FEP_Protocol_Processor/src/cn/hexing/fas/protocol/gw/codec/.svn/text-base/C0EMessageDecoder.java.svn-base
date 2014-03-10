package cn.hexing.fas.protocol.gw.codec;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.hexing.exception.MessageDecodeException;
import cn.hexing.fas.model.RtuAlert;
import cn.hexing.fas.protocol.gw.parse.DataItemParser;
import cn.hexing.fas.protocol.gw.parse.DataSwitch;
import cn.hexing.fas.protocol.gw.parse.DataValue;
import cn.hexing.fas.protocol.gw.parse.ParseTool;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.MessageConst;
import cn.hexing.fk.message.gw.MessageGw;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.model.RtuManage;

/**
 * 三类数据(功能码：0EH)响应消息编码器
 * 
 */
public class C0EMessageDecoder extends AbstractMessageDecoder{

	public Object decode(IMessage message) {
		List<RtuAlert> rt=new ArrayList<RtuAlert>();
		try{			
			String data=ParseTool.getMsgData(message);
			MessageGw msg = (MessageGw) message;
			boolean isReporting = true; //是否主动上报
			if (msg.head.seq_tpv==1)//TpV位置1表示附加域有时间标签
				data=data.substring(0,data.length()-12);//消去6个字节的时间标签
			if (msg.head.c_acd==1)//ACD位置1表示附加域有事件计数器
				data=data.substring(0,data.length()-4);//消去2个字节的事件计数器
			if(msg.head.c_prm==MessageConst.DIR_DOWN) 
				isReporting =false;
			String[] codes= DataItemParser.dataCodeParser(data.substring(4,8), "0E");
			data=data.substring(8);//消去数据单元标识
			if (codes.length>0){				
				if (codes[0].equals("0EF001")||codes[0].equals("0EF002")){
					data=data.substring(4);//消去事件计数器
					int pm=Integer.parseInt(data.substring(0,2),16);//起始指针
					int pn=Integer.parseInt(data.substring(2,4),16);//结束指针
					int count=0;//事件记录数
					if(pm<pn)
						count=pn-pm;
					else if(pm>pn)
						count=256+pn-pm;
					data=data.substring(4);
					int rtua=((MessageGw)message).head.rtua;
					BizRtu rtu=RtuManage.getInstance().getBizRtuInCache(rtua);
					for(int i=0;i<count;i++){
						int alertCode=Integer.parseInt(data.substring(0,2),16);
						int len=Integer.parseInt(data.substring(2,4),16);
						data=data.substring(4);
						List<RtuAlert> raList=alertParse(data.substring(0,len*2),alertCode);
						data=data.substring(len*2);
						if (rtu!=null){
							for (RtuAlert ra:raList){
								ra.setRtuId(rtu.getRtuId());
								ra.setCorpNo(rtu.getDeptCode());                                        
		                        if (rtu.getMeasuredPoint(ra.getTn())!=null){
		                        	ra.setDataSaveID(rtu.getMeasuredPoint(ra.getTn()).getDataSaveID());
		                            ra.setCustomerNo(rtu.getMeasuredPoint(ra.getTn()).getCustomerNo());                                       
		                            ra.setStationNo(rtu.getMeasuredPoint(ra.getTn()).getCustomerNo());
		                        }
		                        ra.setGjly(isReporting?"1":"2"); //告警来源，主动上报这是为1，主站召测为2
		                        ra.setCodeItem(codes[0]);
		                        ra.setReceiveTime(new Date(((MessageGw) message).getIoTime()));
		                        rt.add(ra);
							}							
						}
						else{
							for (RtuAlert ra:raList){
		                        ra.setGjly(isReporting?"1":"2"); //告警来源，主动上报这是为1，主站召测为2
								rt.add(ra);
							}
						}
					}					
				}														
			}
		}catch(Exception e){
			throw new MessageDecodeException(e);
		}
		return rt;	
	}
	private List<RtuAlert> alertParse(String data,int alertCode){
		List<RtuAlert> raList=new ArrayList<RtuAlert>();		
		String alertCodeHex="";
		String alertTime=data.substring(0,10);
		data=data.substring(10);
		int tn=0;
		switch (alertCode){
			case 1:{//ERC1：数据初始化和版本变更记录
				String tag=data.substring(0,2);
				DataValue param1=DataItemParser.parseValue(data.substring(2,10),"ASC4");
				DataValue param2=DataItemParser.parseValue(data.substring(10,18),"ASC4");				
				if ((Integer.parseInt(tag)&1)==1){
					RtuAlert ra=new RtuAlert();
					ra.setSbcs(alertCodeHex+"01="+param1.getValue()+"#"+alertCodeHex+"02="+param2.getValue());
					alertCodeHex="C011";//数据初始化记录
					ra.setAlertCode(Integer.parseInt(alertCodeHex,16));	
					raList.add(ra);
				}
				if((Integer.parseInt(tag)&2)==2){
					RtuAlert ra=new RtuAlert();
					ra.setSbcs(alertCodeHex+"01="+param1.getValue()+"#"+alertCodeHex+"02="+param2.getValue());
					alertCodeHex="C012";//版本变更记录
					ra.setAlertCode(Integer.parseInt(alertCodeHex,16));
					raList.add(ra);
				}
				break;
			}	
			case 2:{//ERC2：参数丢失
				String tag=data.substring(0,2);
				if ((Integer.parseInt(tag)&1)==1){
					RtuAlert ra=new RtuAlert();
					alertCodeHex="C021";//终端参数丢失记录
					ra.setAlertCode(Integer.parseInt(alertCodeHex,16));		
					raList.add(ra);
				}
				if((Integer.parseInt(tag)&2)==2){
					RtuAlert ra=new RtuAlert();
					alertCodeHex="C022";//测量点参数丢失记录
					ra.setAlertCode(Integer.parseInt(alertCodeHex,16));
					raList.add(ra);
				}
				break;
			}
			case 3:{//ERC3：参数变更
				RtuAlert ra=new RtuAlert();
				alertCodeHex="C030";
				ra.setAlertCode(Integer.parseInt(alertCodeHex,16));
				String value=alertCodeHex+"01="+data.substring(0,2)+",";
				data=data.substring(2);
				String str="";
				for (int i=0;i<data.length()/8;i++){
					value=value+"C030"+DataSwitch.IntToHex(""+i+2, 2)+"="+data.substring(i*8,i*8+8)+",";
					String code=data.substring(i*8,i*8+8);
					//int[] tns=DataItemParser.measuredPointParser(code.substring(0,4));
					String[] codes= DataItemParser.dataCodeParser(code.substring(4,8), "04");
					for(int j=0;j<codes.length;j++){
						if (codes[j].equals("04F150")){	//F10参数设置事件需要把变更测量点列表提交给自动装接接口							
							str=codes[j];												
							break;
						}
					}
				}
				if (str.length()>0){
					ra.setAlertInfo(str);
				}				
				value=value.substring(0,value.length()-1);//消去最后一个,
				ra.setSbcs(value);
				raList.add(ra);
				break;
			}
			case 4:{//ERC4：状态量变位记录
				RtuAlert ra=new RtuAlert();
				alertCodeHex="C040";
				ra.setAlertCode(Integer.parseInt(alertCodeHex,16));
				String value="C04001="+DataItemParser.parseValue(data.substring(0,2),"BS1").getValue()+",";
				value=value+"C04002="+DataItemParser.parseValue(data.substring(2,4),"BS1").getValue();
				ra.setSbcs(value);
				raList.add(ra);
				break;
			}
			case 5:{//ERC5：遥控跳闸记录
				RtuAlert ra=new RtuAlert();
				alertCodeHex="C050";
				ra.setAlertCode(Integer.parseInt(alertCodeHex,16));
				String value="C05001="+DataItemParser.parseValue(data.substring(0,2),"BS1").getValue()+",";
				value=value+"C05002="+DataItemParser.parseValue(data.substring(2,6),"A2").getValue()+",";
				value=value+"C05002="+DataItemParser.parseValue(data.substring(6,10),"A2").getValue();
				ra.setSbcs(value);
				raList.add(ra);
				break;
			}
			case 6:{//ERC6：功控跳闸记录
				RtuAlert ra=new RtuAlert();
				alertCodeHex="C060";
				ra.setAlertCode(Integer.parseInt(alertCodeHex,16));
				tn=Integer.parseInt(DataItemParser.parseValue(data.substring(0,2),"HTB1").getValue());
				data=data.substring(2);
				String value="C06001="+DataItemParser.parseValue(data.substring(0,2),"BS1").getValue()+",";
				value=value+"C06002="+DataItemParser.parseValue(data.substring(2,4),"BS1").getValue()+",";
				value=value+"C06003="+DataItemParser.parseValue(data.substring(4,8),"A2").getValue()+",";
				value=value+"C06004="+DataItemParser.parseValue(data.substring(8,12),"A2").getValue()+",";
				value=value+"C06005="+DataItemParser.parseValue(data.substring(12,16),"A2").getValue();
				ra.setSbcs(value);
				raList.add(ra);
				break;
			}
			case 7:{//ERC7：电控跳闸记录
				tn=Integer.parseInt(DataItemParser.parseValue(data.substring(0,2),"HTB1").getValue());
				data=data.substring(2);
				String value="C07001="+DataItemParser.parseValue(data.substring(0,2),"BS1").getValue()+",";
				data=data.substring(2);
				int tag=Integer.parseInt(data.substring(0,2),16);
				data=data.substring(2);		
				value=value+"C07002="+DataItemParser.parseValue(data.substring(0,8),"A3").getValue()+",";
				value=value+"C07003="+DataItemParser.parseValue(data.substring(8,16),"A3").getValue();
				if((tag&1)==1){
					RtuAlert ra=new RtuAlert();
					alertCodeHex="C071";//月电控
					ra.setAlertCode(Integer.parseInt(alertCodeHex,16));							
					ra.setSbcs(value);
					raList.add(ra);
				}
				if ((tag&2)==2){
					RtuAlert ra=new RtuAlert();
					alertCodeHex="C072";//购电控
					ra.setAlertCode(Integer.parseInt(alertCodeHex,16));					
					ra.setSbcs(value);
					raList.add(ra);
				}
				
				break;
			}
			case 8:{//ERC8：电能表参数变更
				tn=Integer.parseInt(DataItemParser.parseValue(data.substring(0,4),"HTB2").getValue());
				data=data.substring(4);				
				if((Integer.parseInt(data.substring(0,2),16)&1)==1){
					RtuAlert ra=new RtuAlert();
					alertCodeHex="C081";//电能表费率时段变化
					ra.setAlertCode(Integer.parseInt(alertCodeHex,16));
					raList.add(ra);
				}
				if((Integer.parseInt(data.substring(0,2),16)&2)==2){
					RtuAlert ra=new RtuAlert();
					alertCodeHex="C082";//电能表编程时间更改
					ra.setAlertCode(Integer.parseInt(alertCodeHex,16));	
				}
				if((Integer.parseInt(data.substring(0,2),16)&4)==4){
					RtuAlert ra=new RtuAlert();
					alertCodeHex="C083";//电能表抄表日更改
					ra.setAlertCode(Integer.parseInt(alertCodeHex,16));	
					raList.add(ra);
				}
				if((Integer.parseInt(data.substring(0,2),16)&8)==8){
					RtuAlert ra=new RtuAlert();
					alertCodeHex="C084";//电能表编程时间更改
					ra.setAlertCode(Integer.parseInt(alertCodeHex,16));	
					raList.add(ra);
				}
				if((Integer.parseInt(data.substring(0,2),16)&16)==16){
					RtuAlert ra=new RtuAlert();
					alertCodeHex="C085";//电能表的互感器倍率更改
					ra.setAlertCode(Integer.parseInt(alertCodeHex,16));	
					raList.add(ra);
				}
				if((Integer.parseInt(data.substring(0,2),16)&32)==32){
					RtuAlert ra=new RtuAlert();
					alertCodeHex="C086";//电能表的互感器倍率更改
					ra.setAlertCode(Integer.parseInt(alertCodeHex,16));	
					raList.add(ra);
				}
				if((Integer.parseInt(data.substring(0,2),16)&64)==64){
					RtuAlert ra=new RtuAlert();
					alertCodeHex="C087";//表地址更改
					ra.setAlertCode(Integer.parseInt(alertCodeHex,16));	
					raList.add(ra);
				}
				data=data.substring(2);				
				break;
			}
			case 9:	//ERC9：电流回路异常
			case 10://ERC10：电压回路异常
			{
				RtuAlert ra=new RtuAlert();
				tn=Integer.parseInt(DataItemParser.parseValue(data.substring(0,4),"HTB2").getValue());
				data=data.substring(4);	
				if ((tn&32768)==32768)
					alertCodeHex="C"+DataSwitch.StrStuff("0", 2, ""+alertCode, "left")+"0";//发生
				else
					alertCodeHex="C"+DataSwitch.StrStuff("0", 2, ""+alertCode, "left")+"1";//恢复
				ra.setAlertCode(Integer.parseInt(alertCodeHex,16));	
				tn=tn&32767;				
				String value=alertCodeHex+"00="+DataItemParser.parseValue(data.substring(0,2),"BS1").getValue()+",";	
				data=data.substring(2);	
				value=value+alertCodeHex+"01="+DataItemParser.parseValue(data.substring(0,4),"A7").getValue()+",";
				value=value+alertCodeHex+"02="+DataItemParser.parseValue(data.substring(4,8),"A7").getValue()+",";
				value=value+alertCodeHex+"03="+DataItemParser.parseValue(data.substring(8,12),"A7").getValue()+",";
				value=value+alertCodeHex+"04="+DataItemParser.parseValue(data.substring(12,18),"A25").getValue()+",";
				value=value+alertCodeHex+"05="+DataItemParser.parseValue(data.substring(18,24),"A25").getValue()+",";
				value=value+alertCodeHex+"06="+DataItemParser.parseValue(data.substring(24,30),"A25").getValue()+",";
				value=value+alertCodeHex+"07="+DataItemParser.parseValue(data.substring(30,40),"A14").getValue();
				data=data.substring(40);
				ra.setSbcs(value);
				raList.add(ra);
				break;
			}
			case 11:{//ERC11：相序异常	
				RtuAlert ra=new RtuAlert();
				tn=Integer.parseInt(DataItemParser.parseValue(data.substring(0,4),"HTB2").getValue());
				data=data.substring(4);	
				if ((tn&32768)==32768)
					alertCodeHex="C110";//发生
				else
					alertCodeHex="C111";//恢复
				ra.setAlertCode(Integer.parseInt(alertCodeHex,16));	
				tn=tn&32767;				
				String value=alertCodeHex+"00="+DataItemParser.parseValue(data.substring(0,4),"A5").getValue()+",";	
				value=value+alertCodeHex+"01="+DataItemParser.parseValue(data.substring(4,8),"A5").getValue()+",";
				value=value+alertCodeHex+"02="+DataItemParser.parseValue(data.substring(8,12),"A5").getValue()+",";
				value=value+alertCodeHex+"03="+DataItemParser.parseValue(data.substring(12,16),"A5").getValue()+",";
				value=value+alertCodeHex+"04="+DataItemParser.parseValue(data.substring(16,20),"A5").getValue()+",";
				value=value+alertCodeHex+"05="+DataItemParser.parseValue(data.substring(20,24),"A5").getValue()+",";
				value=value+alertCodeHex+"06="+DataItemParser.parseValue(data.substring(24,34),"A14").getValue();
				data=data.substring(34);
				ra.setSbcs(value);
				raList.add(ra);
				break;
			}
			case 12:{//ERC12：电能表时间超差	
				RtuAlert ra=new RtuAlert();
				tn=Integer.parseInt(DataItemParser.parseValue(data.substring(0,4),"HTB2").getValue());
				data=data.substring(4);	
				if ((tn&32768)==32768)
					alertCodeHex="C120";//发生
				else
					alertCodeHex="C121";//恢复
				ra.setAlertCode(Integer.parseInt(alertCodeHex,16));	
				tn=tn&32767;
				raList.add(ra);
				break;
			}
			case 13:{//ERC13：电表故障信息			
				tn=Integer.parseInt(DataItemParser.parseValue(data.substring(0,4),"HTB2").getValue());
				data=data.substring(4);	
				if ((tn&32768)==32768){
					if((Integer.parseInt(data.substring(0,2),16)&1)==1){
						RtuAlert ra=new RtuAlert();
						alertCodeHex="C131";//电能表编程次数或最大需量清零次数变化发生
						ra.setAlertCode(Integer.parseInt(alertCodeHex,16));	
						raList.add(ra);
					}
					if((Integer.parseInt(data.substring(0,2),16)&2)==2){
						RtuAlert ra=new RtuAlert();
						alertCodeHex="C132";//电能表断相次数变化发生
						ra.setAlertCode(Integer.parseInt(alertCodeHex,16));	
						raList.add(ra);
					}
					if((Integer.parseInt(data.substring(0,2),16)&4)==4){
						RtuAlert ra=new RtuAlert();
						alertCodeHex="C133";//电能表失压次数变化发生
						ra.setAlertCode(Integer.parseInt(alertCodeHex,16));	
						raList.add(ra);
					}
					if((Integer.parseInt(data.substring(0,2),16)&8)==8){
						RtuAlert ra=new RtuAlert();
						alertCodeHex="C134";//电能表停电次数变化发生
						ra.setAlertCode(Integer.parseInt(alertCodeHex,16));	
						raList.add(ra);
					}
					if((Integer.parseInt(data.substring(0,2),16)&16)==16){
						RtuAlert ra=new RtuAlert();
						alertCodeHex="C135";//电能表电池欠压发生
						ra.setAlertCode(Integer.parseInt(alertCodeHex,16));	
						raList.add(ra);
					}
				}
				else{
					if((Integer.parseInt(data.substring(0,2),16)&1)==1){
						RtuAlert ra=new RtuAlert();
						alertCodeHex="C136";//电能表编程次数或最大需量清零次数变化恢复
						ra.setAlertCode(Integer.parseInt(alertCodeHex,16));	
						raList.add(ra);
					}
					if((Integer.parseInt(data.substring(0,2),16)&2)==2){
						RtuAlert ra=new RtuAlert();
						alertCodeHex="C137";//电能表断相次数变化恢复
						ra.setAlertCode(Integer.parseInt(alertCodeHex,16));	
						raList.add(ra);
					}
					if((Integer.parseInt(data.substring(0,2),16)&4)==4){
						RtuAlert ra=new RtuAlert();
						alertCodeHex="C138";//电能表失压次数变化恢复
						ra.setAlertCode(Integer.parseInt(alertCodeHex,16));	
						raList.add(ra);
					}
					if((Integer.parseInt(data.substring(0,2),16)&8)==8){
						RtuAlert ra=new RtuAlert();
						alertCodeHex="C139";//电能表停电次数变化恢复
						ra.setAlertCode(Integer.parseInt(alertCodeHex,16));	
						raList.add(ra);
					}
					if((Integer.parseInt(data.substring(0,2),16)&16)==16){
						RtuAlert ra=new RtuAlert();
						alertCodeHex="C13A";//电能表电池欠压恢复
						ra.setAlertCode(Integer.parseInt(alertCodeHex,16));	
						raList.add(ra);
					}
				}
				tn=tn&32767;								
				break;
			}
			case 14:{//ERC14：终端停/上电记录
				RtuAlert ra=new RtuAlert();
				String alertTime1=data.substring(0,10);//上电时间
				//停电时间>上电时间
				if (Long.parseLong(DataSwitch.ReverseStringByByte(alertTime))>
					Long.parseLong(DataSwitch.ReverseStringByByte(alertTime1))){
					alertCodeHex="C141";//停电
				}
				else{
					alertCodeHex="C142";//上电
					alertTime=alertTime1;
				}
				ra.setAlertCode(Integer.parseInt(alertCodeHex,16));
				raList.add(ra);
				break;
			}
			case 15:{//ERC15：谐波越限告警	
				RtuAlert ra=new RtuAlert();
				tn=Integer.parseInt(DataItemParser.parseValue(data.substring(0,4),"HTB2").getValue());
				data=data.substring(4);	
				if ((tn&32768)==32768)
					alertCodeHex="C150";//发生
				else
					alertCodeHex="C151";//恢复
				ra.setAlertCode(Integer.parseInt(alertCodeHex,16));	
				tn=tn&32767;				
				String value=alertCodeHex+"00="+DataItemParser.parseValue(data.substring(0,2),"BS1").getValue()+",";
				String format="";
				if((Integer.parseInt(data.substring(0,2),16)&128)==128)
					format="A6";
				else 
					format="A5";
				value=value+alertCodeHex+"01="+DataItemParser.parseValue(data.substring(2,8),"BS3").getValue()+",";
				data.substring(8);
				for(int i=0;i<19;i++){
					value=value+alertCodeHex+DataSwitch.StrStuff("0", 2, ""+(i+2), "left")+"="+DataItemParser.parseValue(data.substring(i*4,i*4+4),format).getValue()+",";
				}				
				data=data.substring(19*4);
				if(value.endsWith(","))//消去最后一个","
					value=value.substring(0,value.length()-1);
				ra.setSbcs(value);
				raList.add(ra);
				break;
			}
			case 16:{//ERC16：直流模拟量越限记录	
				RtuAlert ra=new RtuAlert();
				tn=Integer.parseInt(DataItemParser.parseValue(data.substring(0,2),"HTB1").getValue());
				data=data.substring(2);	
				if ((tn&128)==128)
					alertCodeHex="C160";//发生
				else
					alertCodeHex="C161";//恢复
				ra.setAlertCode(Integer.parseInt(alertCodeHex,16));	
				tn=tn&127;				
				String value=alertCodeHex+"00="+DataItemParser.parseValue(data.substring(0,2),"BS1").getValue()+",";				
				value=value+alertCodeHex+"01="+DataItemParser.parseValue(data.substring(2,6),"A2").getValue();								
				data=data.substring(6);
				ra.setSbcs(value);
				raList.add(ra);
				break;
			}
			case 17:{//ERC17：电压/电流不平衡度越限记录
				tn=Integer.parseInt(DataItemParser.parseValue(data.substring(0,4),"HTB2").getValue());
				data=data.substring(4);	
				int tag=Integer.parseInt(data.substring(0,2),16);
				data=data.substring(2);	
				DataValue param1=DataItemParser.parseValue(data.substring(0,4),"A5");	
				DataValue param2=DataItemParser.parseValue(data.substring(4,8),"A5");	
				DataValue param3=DataItemParser.parseValue(data.substring(8,12),"A7");	
				DataValue param4=DataItemParser.parseValue(data.substring(12,16),"A7");	
				DataValue param5=DataItemParser.parseValue(data.substring(16,20),"A7");
				DataValue param6=DataItemParser.parseValue(data.substring(20,26),"A25");	
				DataValue param7=DataItemParser.parseValue(data.substring(26,32),"A25");	
				DataValue param8=DataItemParser.parseValue(data.substring(32,38),"A25");	
				data=data.substring(38);
				if ((tn&32768)==32768){
					if((tag&1)==1){
						RtuAlert ra=new RtuAlert();
						alertCodeHex="C171";//电压不平衡度越限发生
						ra.setAlertCode(Integer.parseInt(alertCodeHex,16));	
						ra.setSbcs(alertCodeHex+"01="+param1.getValue()+","+alertCodeHex+"02="+param2.getValue()+","
									+alertCodeHex+"03="+param3.getValue()+","+alertCodeHex+"04="+param4.getValue()+","
									+alertCodeHex+"05="+param5.getValue()+alertCodeHex+"06="+param6.getValue()+","
									+alertCodeHex+"07="+param7.getValue()+alertCodeHex+"08="+param8.getValue());
						raList.add(ra);
					}
					if((tag&2)==2){
						RtuAlert ra=new RtuAlert();
						alertCodeHex="C172";//电流不平衡度越限发生
						ra.setAlertCode(Integer.parseInt(alertCodeHex,16));	
						ra.setSbcs(alertCodeHex+"01="+param1.getValue()+","+alertCodeHex+"02="+param2.getValue()+","
								+alertCodeHex+"03="+param3.getValue()+","+alertCodeHex+"04="+param4.getValue()+","
								+alertCodeHex+"05="+param5.getValue()+alertCodeHex+"06="+param6.getValue()+","
								+alertCodeHex+"07="+param7.getValue()+alertCodeHex+"08="+param8.getValue());
						raList.add(ra);
					}
				}
				else{
					if((tag&1)==1){
						RtuAlert ra=new RtuAlert();
						alertCodeHex="C173";//电压不平衡度越限恢复
						ra.setAlertCode(Integer.parseInt(alertCodeHex,16));	
						ra.setSbcs(alertCodeHex+"01="+param1.getValue()+","+alertCodeHex+"02="+param2.getValue()+","
								+alertCodeHex+"03="+param3.getValue()+","+alertCodeHex+"04="+param4.getValue()+","
								+alertCodeHex+"05="+param5.getValue()+alertCodeHex+"06="+param6.getValue()+","
								+alertCodeHex+"07="+param7.getValue()+alertCodeHex+"08="+param8.getValue());
						raList.add(ra);
					}
					if((tag&2)==2){
						RtuAlert ra=new RtuAlert();
						alertCodeHex="C174";//电流不平衡度越限恢复
						ra.setAlertCode(Integer.parseInt(alertCodeHex,16));	
						ra.setSbcs(alertCodeHex+"01="+param1.getValue()+","+alertCodeHex+"02="+param2.getValue()+","
								+alertCodeHex+"03="+param3.getValue()+","+alertCodeHex+"04="+param4.getValue()+","
								+alertCodeHex+"05="+param5.getValue()+alertCodeHex+"06="+param6.getValue()+","
								+alertCodeHex+"07="+param7.getValue()+alertCodeHex+"08="+param8.getValue());
						raList.add(ra);
					}
				}
				tn=tn&32767;								
				break;
			}
			case 18:{//ERC18：电容器投切自锁记录
				tn=Integer.parseInt(DataItemParser.parseValue(data.substring(0,4),"HTB2").getValue());
				data=data.substring(4);	
				int tag=Integer.parseInt(data.substring(0,2),16);
				data=data.substring(2);	
				DataValue param1=DataItemParser.parseValue(data.substring(0,4),"BS2");		
				DataValue param2=DataItemParser.parseValue(data.substring(4,8),"A5");	
				DataValue param3=DataItemParser.parseValue(data.substring(8,12),"A23");	
				DataValue param4=DataItemParser.parseValue(data.substring(12,16),"A7");	
				data=data.substring(16);
				if ((tn&32768)==32768){
					if((tag&1)==1){
						RtuAlert ra=new RtuAlert();
						alertCodeHex="C181";//电容器过压发生
						ra.setAlertCode(Integer.parseInt(alertCodeHex,16));	
						ra.setSbcs(alertCodeHex+"01="+param1.getValue()+","+alertCodeHex+"02="+param2.getValue()+","
								+alertCodeHex+"03="+param3.getValue()+","+alertCodeHex+"04="+param4.getValue());
						raList.add(ra);
					}
					if((tag&2)==2){
						RtuAlert ra=new RtuAlert();
						alertCodeHex="C182";//电容器装置故障发生
						ra.setAlertCode(Integer.parseInt(alertCodeHex,16));	
						ra.setSbcs(alertCodeHex+"01="+param1.getValue()+","+alertCodeHex+"02="+param2.getValue()+","
								+alertCodeHex+"03="+param3.getValue()+","+alertCodeHex+"04="+param4.getValue());
						raList.add(ra);
					}
					if((tag&4)==4){
						RtuAlert ra=new RtuAlert();
						alertCodeHex="C183";//电容器执行回路故障发生
						ra.setAlertCode(Integer.parseInt(alertCodeHex,16));	
						ra.setSbcs(alertCodeHex+"01="+param1.getValue()+","+alertCodeHex+"02="+param2.getValue()+","
								+alertCodeHex+"03="+param3.getValue()+","+alertCodeHex+"04="+param4.getValue());
						raList.add(ra);
					}
				}
				else{
					if((tag&1)==1){
						RtuAlert ra=new RtuAlert();
						alertCodeHex="C184";//电容器过压恢复
						ra.setAlertCode(Integer.parseInt(alertCodeHex,16));	
						ra.setSbcs(alertCodeHex+"01="+param1.getValue()+","+alertCodeHex+"02="+param2.getValue()+","
								+alertCodeHex+"03="+param3.getValue()+","+alertCodeHex+"04="+param4.getValue());
						raList.add(ra);
					}
					if((tag&2)==2){
						RtuAlert ra=new RtuAlert();
						alertCodeHex="C185";//电容器装置故障恢复
						ra.setAlertCode(Integer.parseInt(alertCodeHex,16));	
						ra.setSbcs(alertCodeHex+"01="+param1.getValue()+","+alertCodeHex+"02="+param2.getValue()+","
								+alertCodeHex+"03="+param3.getValue()+","+alertCodeHex+"04="+param4.getValue());
						raList.add(ra);
					}
					if((tag&4)==4){
						RtuAlert ra=new RtuAlert();
						alertCodeHex="C186";//电容器执行回路故障恢复
						ra.setAlertCode(Integer.parseInt(alertCodeHex,16));	
						ra.setSbcs(alertCodeHex+"01="+param1.getValue()+","+alertCodeHex+"02="+param2.getValue()+","
								+alertCodeHex+"03="+param3.getValue()+","+alertCodeHex+"04="+param4.getValue());
						raList.add(ra);
					}
				}
				tn=tn&32767;								
				break;
			}
			case 19:{//ERC19：购电参数设置记录
				RtuAlert ra=new RtuAlert();
				alertCodeHex="C190";
				ra.setAlertCode(Integer.parseInt(alertCodeHex,16));
				tn=Integer.parseInt(DataItemParser.parseValue(data.substring(0,2),"HTB1").getValue());
				data=data.substring(2);
				String value="C19001="+DataItemParser.parseValue(data.substring(0,8),"HTB4").getValue()+",";
				value=value+"C19002="+DataItemParser.parseValue(data.substring(8,10),"HTB1").getValue()+",";
				value=value+"C19003="+DataItemParser.parseValue(data.substring(10,18),"A3").getValue()+",";
				value=value+"C19004="+DataItemParser.parseValue(data.substring(18,26),"A3").getValue()+",";
				value=value+"C19005="+DataItemParser.parseValue(data.substring(26,34),"A3").getValue()+",";
				value=value+"C19006="+DataItemParser.parseValue(data.substring(34,42),"A3").getValue()+",";
				value=value+"C19007="+DataItemParser.parseValue(data.substring(42,50),"A3").getValue();
				data=data.substring(50);
				ra.setSbcs(value);
				raList.add(ra);
				break;
			}
			case 20:{//ERC20：消息认证错误记录
				RtuAlert ra=new RtuAlert();
				alertCodeHex="C200";	
				ra.setAlertCode(Integer.parseInt(alertCodeHex,16));	
				String value="C20001="+DataItemParser.parseValue(data.substring(0,32),"HEX16").getValue()+",";				
				value=value+"C20002="+DataItemParser.parseValue(data.substring(32,34),"HEX1").getValue();
				data=data.substring(34);
				ra.setSbcs(value);
				raList.add(ra);
				break;
			}
			case 21:{//ERC21：终端故障记录
				RtuAlert ra=new RtuAlert();
				int tag=Integer.parseInt(data.substring(0,2),16);
				alertCodeHex="C21"+tag;
				ra.setAlertCode(Integer.parseInt(alertCodeHex,16));
				raList.add(ra);
				break;
			}
			case 22:{//ERC22：有功总电能差动越限事件记录
				RtuAlert ra=new RtuAlert();
				tn=Integer.parseInt(DataItemParser.parseValue(data.substring(0,2),"HTB1").getValue());
				data=data.substring(2);		
				if((tn&128)==128){//有功总电能差动越限事件发生
					alertCodeHex="C220";
					ra.setAlertCode(Integer.parseInt(alertCodeHex,16));								
				}
				else{			//有功总电能差动越限事件恢复
					alertCodeHex="C221";
					ra.setAlertCode(Integer.parseInt(alertCodeHex,16));				
				}
				String value=alertCodeHex+"01="+DataItemParser.parseValue(data.substring(0,8),"A3").getValue()+",";
				value=value+alertCodeHex+"02="+DataItemParser.parseValue(data.substring(8,16),"A3").getValue()+",";
				value=value+alertCodeHex+"03="+DataItemParser.parseValue(data.substring(16,18),"HTB1").getValue()+",";
				value=value+alertCodeHex+"04="+DataItemParser.parseValue(data.substring(18,26),"A3").getValue()+",";
				value=value+alertCodeHex+"05="+DataItemParser.parseValue(data.substring(26,28),"HTB1").getValue()+",";
				int num1=Integer.parseInt(data.substring(26,28),16);
				data=data.substring(28);		
				for (int i=0;i<num1;i++){
					value=value+alertCodeHex+DataSwitch.StrStuff("0", 2, ""+(i+6), "left")+"="+DataItemParser.parseValue(data.substring(i*10,i*10+10),"A14").getValue()+",";
				}
				data=data.substring(10*num1);
				
				value=value+alertCodeHex+DataSwitch.StrStuff("0", 2, ""+(num1+6), "left")+"="+DataItemParser.parseValue(data.substring(0,2),"HTB1").getValue()+",";
				int num2=Integer.parseInt(data.substring(0,2),16);
				data=data.substring(2);		
				for (int i=0;i<num2;i++){
					value=value+alertCodeHex+DataSwitch.StrStuff("0", 2, ""+(i+num1+7), "left")+"="+DataItemParser.parseValue(data.substring(i*10,i*10+10),"A14").getValue()+",";
				}	
				data=data.substring(10*num2);
				value=value.substring(0,value.length()-1);//消去最后一个,
				ra.setSbcs(value);
				tn=tn&127;
				raList.add(ra);
				break;
			}
			case 23:{//ERC23：电控告警事件记录
				RtuAlert ra=new RtuAlert();
				tn=Integer.parseInt(DataItemParser.parseValue(data.substring(0,2),"HTB1").getValue())&63;
				data=data.substring(2);	
				String tag=data.substring(3,4);
				if((Integer.parseInt(tag)&1)==1){//月电控
					alertCodeHex="C231";
					ra.setAlertCode(Integer.parseInt(alertCodeHex,16));								
					
				}
				else{//购电控
					alertCodeHex="C232";
					ra.setAlertCode(Integer.parseInt(alertCodeHex,16));		
				}
				String value=alertCodeHex+"01="+DataItemParser.parseValue(data.substring(0,2),"BS1").getValue()+",";
				value=value+alertCodeHex+"02="+DataItemParser.parseValue(data.substring(2,4),"BS1").getValue()+",";
				value=value+alertCodeHex+"02="+DataItemParser.parseValue(data.substring(4,12),"A3").getValue()+",";
				value=value+alertCodeHex+"02="+DataItemParser.parseValue(data.substring(12,20),"A3").getValue();
				data=data.substring(20);
				ra.setSbcs(value);
				raList.add(ra);
				break;
			}
			case 24:{//ERC24：电压越限记录
				tn=Integer.parseInt(DataItemParser.parseValue(data.substring(0,4),"HTB2").getValue());
				data=data.substring(4);	
				int tag=Integer.parseInt(data.substring(0,2),16);
				data=data.substring(2);	
				DataValue param1=DataItemParser.parseValue(data.substring(0,4),"A7");	
				DataValue param2=DataItemParser.parseValue(data.substring(4,8),"A7");	
				DataValue param3=DataItemParser.parseValue(data.substring(8,12),"A7");					
				data=data.substring(12);
				if ((tn&32768)==32768){//告警发生
					if((tag&1)==1){//A相
						RtuAlert ra=new RtuAlert();
						if((tag&64)==64){
							alertCodeHex="C241";//A相电压越上上限发生						
						}
						else{
							alertCodeHex="C242";//A相电压越下下限发生							
						}
						ra.setAlertCode(Integer.parseInt(alertCodeHex,16));	
						ra.setSbcs(alertCodeHex+"01="+param1.getValue()+","+alertCodeHex+"02="+param2.getValue()+","
								+alertCodeHex+"03="+param3.getValue());
						raList.add(ra);
					}
					if((tag&2)==2){//B相
						RtuAlert ra=new RtuAlert();
						if((tag&64)==64){
							alertCodeHex="C243";//B相电压越上上限发生						
						}
						else{
							alertCodeHex="C244";//B相电压越下下限发生							
						}
						ra.setAlertCode(Integer.parseInt(alertCodeHex,16));	
						ra.setSbcs(alertCodeHex+"01="+param1.getValue()+","+alertCodeHex+"02="+param2.getValue()+","
								+alertCodeHex+"03="+param3.getValue());
						raList.add(ra);
					}
					if((tag&4)==4){//C相
						RtuAlert ra=new RtuAlert();
						if((tag&64)==64){
							alertCodeHex="C245";//C相电压越上上限发生						
						}
						else{
							alertCodeHex="C246";//C相电压越下下限发生							
						}
						ra.setAlertCode(Integer.parseInt(alertCodeHex,16));	
						ra.setSbcs(alertCodeHex+"01="+param1.getValue()+","+alertCodeHex+"02="+param2.getValue()+","
								+alertCodeHex+"03="+param3.getValue());
						raList.add(ra);
					}
				}
				else{//恢复
					if((tag&1)==1){//A相
						RtuAlert ra=new RtuAlert();
						if((tag&64)==64){
							alertCodeHex="C247";//A相电压越上上限恢复						
						}
						else{
							alertCodeHex="C248";//A相电压越下下限恢复							
						}
						ra.setAlertCode(Integer.parseInt(alertCodeHex,16));	
						ra.setSbcs(alertCodeHex+"01="+param1.getValue()+","+alertCodeHex+"02="+param2.getValue()+","
								+alertCodeHex+"03="+param3.getValue());
						raList.add(ra);
					}
					if((tag&2)==2){//B相
						RtuAlert ra=new RtuAlert();
						if((tag&64)==64){
							alertCodeHex="C249";//B相电压越上上限恢复						
						}
						else{
							alertCodeHex="C24A";//B相电压越下下限恢复							
						}
						ra.setAlertCode(Integer.parseInt(alertCodeHex,16));	
						ra.setSbcs(alertCodeHex+"01="+param1.getValue()+","+alertCodeHex+"02="+param2.getValue()+","
								+alertCodeHex+"03="+param3.getValue());
						raList.add(ra);
					}
					if((tag&4)==4){//C相
						RtuAlert ra=new RtuAlert();
						if((tag&64)==64){
							alertCodeHex="C24B";//C相电压越上上限恢复						
						}
						else{
							alertCodeHex="C24C";//C相电压越下下限恢复							
						}
						ra.setAlertCode(Integer.parseInt(alertCodeHex,16));	
						ra.setSbcs(alertCodeHex+"01="+param1.getValue()+","+alertCodeHex+"02="+param2.getValue()+","
								+alertCodeHex+"03="+param3.getValue());
						raList.add(ra);
					}
				}
				tn=tn&32767;								
				break;
			}
			case 25:{//ERC25：电流越限记录
				tn=Integer.parseInt(DataItemParser.parseValue(data.substring(0,4),"HTB2").getValue());
				data=data.substring(4);	
				int tag=Integer.parseInt(data.substring(0,2),16);
				data=data.substring(2);	
				DataValue param1=DataItemParser.parseValue(data.substring(0,6),"A25");	
				DataValue param2=DataItemParser.parseValue(data.substring(6,12),"A25");	
				DataValue param3=DataItemParser.parseValue(data.substring(12,18),"A25");					
				data=data.substring(18);
				if ((tn&32768)==32768){//告警发生
					if((tag&1)==1){//A相
						RtuAlert ra=new RtuAlert();
						if((tag&64)==64){
							alertCodeHex="C251";//A相电流越上上限发生						
						}
						else{
							alertCodeHex="C252";//A相电流越上限发生							
						}
						ra.setAlertCode(Integer.parseInt(alertCodeHex,16));	
						ra.setSbcs(alertCodeHex+"01="+param1.getValue()+","+alertCodeHex+"02="+param2.getValue()+","
								+alertCodeHex+"03="+param3.getValue());
						raList.add(ra);
					}
					if((tag&2)==2){//B相
						RtuAlert ra=new RtuAlert();
						if((tag&64)==64){
							alertCodeHex="C253";//B相电流越上上限发生						
						}
						else{
							alertCodeHex="C254";//B相电流越上限发生							
						}
						ra.setAlertCode(Integer.parseInt(alertCodeHex,16));	
						ra.setSbcs(alertCodeHex+"01="+param1.getValue()+","+alertCodeHex+"02="+param2.getValue()+","
								+alertCodeHex+"03="+param3.getValue());
						raList.add(ra);
					}
					if((tag&4)==4){//C相
						RtuAlert ra=new RtuAlert();
						if((tag&64)==64){
							alertCodeHex="C255";//C相电流越上上限发生						
						}
						else{
							alertCodeHex="C256";//C相电流越上限发生							
						}
						ra.setAlertCode(Integer.parseInt(alertCodeHex,16));	
						ra.setSbcs(alertCodeHex+"01="+param1.getValue()+","+alertCodeHex+"02="+param2.getValue()+","
								+alertCodeHex+"03="+param3.getValue());
						raList.add(ra);
					}
				}
				else{//恢复
					if((tag&1)==1){//A相
						RtuAlert ra=new RtuAlert();
						if((tag&64)==64){
							alertCodeHex="C257";//A相电流越上上限恢复						
						}
						else{
							alertCodeHex="C258";//A相电流越上限恢复							
						}
						ra.setAlertCode(Integer.parseInt(alertCodeHex,16));	
						ra.setSbcs(alertCodeHex+"01="+param1.getValue()+","+alertCodeHex+"02="+param2.getValue()+","
								+alertCodeHex+"03="+param3.getValue());
						raList.add(ra);
					}
					if((tag&2)==2){//B相
						RtuAlert ra=new RtuAlert();
						if((tag&64)==64){
							alertCodeHex="C259";//B相电流越上上限恢复						
						}
						else{
							alertCodeHex="C25A";//B相电流越上限恢复							
						}
						ra.setAlertCode(Integer.parseInt(alertCodeHex,16));	
						ra.setSbcs(alertCodeHex+"01="+param1.getValue()+","+alertCodeHex+"02="+param2.getValue()+","
								+alertCodeHex+"03="+param3.getValue());
						raList.add(ra);
					}
					if((tag&4)==4){//C相
						RtuAlert ra=new RtuAlert();
						if((tag&64)==64){
							alertCodeHex="C25B";//C相电流越上上限恢复						
						}
						else{
							alertCodeHex="C25C";//C相电流越上限恢复							
						}
						ra.setAlertCode(Integer.parseInt(alertCodeHex,16));	
						ra.setSbcs(alertCodeHex+"01="+param1.getValue()+","+alertCodeHex+"02="+param2.getValue()+","
								+alertCodeHex+"03="+param3.getValue());
						raList.add(ra);
					}
				}
				tn=tn&32767;								
				break;
			}
			case 26:{//ERC26：视在功率越限记录
				tn=Integer.parseInt(DataItemParser.parseValue(data.substring(0,4),"HTB2").getValue());
				data=data.substring(4);	
				int tag=Integer.parseInt(data.substring(0,2),16);
				data=data.substring(2);	
				DataValue param1=DataItemParser.parseValue(data.substring(0,6),"A23");	
				DataValue param2=DataItemParser.parseValue(data.substring(6,12),"A23");						
				data=data.substring(12);
				if ((tn&32768)==32768){//告警发生
					RtuAlert ra=new RtuAlert();
					if((tag&64)==64){
						alertCodeHex="C261";//视在功率越上上限发生						
					}
					else{
						alertCodeHex="C262";//视在功率越上限发生							
					}
					ra.setAlertCode(Integer.parseInt(alertCodeHex,16));	
					ra.setSbcs(alertCodeHex+"01="+param1.getValue()+","+alertCodeHex+"02="+param2.getValue());
					raList.add(ra);				
				}
				else{//恢复
					RtuAlert ra=new RtuAlert();
					if((tag&64)==64){
						alertCodeHex="C263";//视在功率越上上限恢复			
					}
					else{
						alertCodeHex="C264";//视在功率越上限恢复								
					}
					ra.setAlertCode(Integer.parseInt(alertCodeHex,16));	
					ra.setSbcs(alertCodeHex+"01="+param1.getValue()+","+alertCodeHex+"02="+param2.getValue());
					raList.add(ra);		
				}
				tn=tn&32767;								
				break;
			}
			case 27:{//ERC27：电能表示度下降记录
				tn=Integer.parseInt(DataItemParser.parseValue(data.substring(0,4),"HTB2").getValue());
				data=data.substring(4);		
				DataValue param1=DataItemParser.parseValue(data.substring(0,10),"A14");	
				DataValue param2=DataItemParser.parseValue(data.substring(10,20),"A14");						
				data=data.substring(20);
				RtuAlert ra=new RtuAlert();				
				if ((tn&32768)==32768){//告警发生						
					alertCodeHex="C271";//电能表示度下降发生										
				}
				else{//恢复			
					alertCodeHex="C272";//电能表示度下降恢复						
				}
				ra.setAlertCode(Integer.parseInt(alertCodeHex,16));	
				ra.setSbcs(alertCodeHex+"01="+param1.getValue()+","+alertCodeHex+"02="+param2.getValue());
				raList.add(ra);	
				tn=tn&32767;								
				break;
			}
			case 28:{//ERC28：电能量超差记录
				tn=Integer.parseInt(DataItemParser.parseValue(data.substring(0,4),"HTB2").getValue());
				data=data.substring(4);		
				DataValue param1=DataItemParser.parseValue(data.substring(0,10),"A14");	
				DataValue param2=DataItemParser.parseValue(data.substring(10,20),"A14");	
				DataValue param3=DataItemParser.parseValue(data.substring(20,22),"A22");						
				data=data.substring(22);
				RtuAlert ra=new RtuAlert();	
				if ((tn&32768)==32768){//告警发生									
					alertCodeHex="C281";//电能量超差发生															
				}
				else{//恢复				
					alertCodeHex="C282";//电能量超差恢复					
				}
				ra.setAlertCode(Integer.parseInt(alertCodeHex,16));		
				ra.setSbcs(alertCodeHex+"01="+param1.getValue()+","+alertCodeHex+"02="+param2.getValue()+","
						+alertCodeHex+"03="+param3.getValue());
				raList.add(ra);	
				tn=tn&32767;								
				break;
			}
			case 29:{//ERC29：电能表飞走记录
				tn=Integer.parseInt(DataItemParser.parseValue(data.substring(0,4),"HTB2").getValue());
				data=data.substring(4);		
				DataValue param1=DataItemParser.parseValue(data.substring(0,10),"A14");	
				DataValue param2=DataItemParser.parseValue(data.substring(10,20),"A14");	
				DataValue param3=DataItemParser.parseValue(data.substring(20,22),"A22");						
				data=data.substring(22);
				RtuAlert ra=new RtuAlert();			
				if ((tn&32768)==32768){//告警发生							
					alertCodeHex="C291";//电能表飞走发生														
				}
				else{//恢复			
					alertCodeHex="C292";//电能表飞走恢复					
				}
				ra.setAlertCode(Integer.parseInt(alertCodeHex,16));		
				ra.setSbcs(alertCodeHex+"01="+param1.getValue()+","+alertCodeHex+"02="+param2.getValue()+","
						+alertCodeHex+"03="+param3.getValue());
				raList.add(ra);		
				tn=tn&32767;								
				break;
			}
			case 30:{//ERC30：电能表停走记录
				tn=Integer.parseInt(DataItemParser.parseValue(data.substring(0,4),"HTB2").getValue());
				data=data.substring(4);		
				DataValue param1=DataItemParser.parseValue(data.substring(0,10),"A14");	
				DataValue param2=DataItemParser.parseValue(data.substring(10,12),"HTB1");					
				data=data.substring(12);
				RtuAlert ra=new RtuAlert();					
				if ((tn&32768)==32768){//告警发生					
					alertCodeHex="C301";//电能表停走发生														
				}
				else{//恢复			
					alertCodeHex="C302";//电能表停走恢复					
				}
				ra.setAlertCode(Integer.parseInt(alertCodeHex,16));	
				ra.setSbcs(alertCodeHex+"01="+param1.getValue()+","+alertCodeHex+"02="+param2.getValue());
				raList.add(ra);		
				tn=tn&32767;								
				break;
			}
			case 31:{//ERC31：终端485抄表失败记录
				RtuAlert ra=new RtuAlert();
				String tag=data.substring(3,4);
				tn=Integer.parseInt(data.substring(2,3)+data.substring(0,2),16);
				tn=tn&32767;	
				data=data.substring(4);
				DataValue param1=DataItemParser.parseValue(data.substring(0,10),"A15");	
				DataValue param2=DataItemParser.parseValue(data.substring(10,20),"A14");
				DataValue param3=DataItemParser.parseValue(data.substring(20,28),"A11");					
				if((Integer.parseInt(tag)&8)==8){//告警发生
					alertCodeHex="C310";	//终端485抄表失败发生						
				}
				else{//告警恢复
					alertCodeHex="C311";	//终端485抄表失败恢复			
				}
				ra.setAlertCode(Integer.parseInt(alertCodeHex,16));
				ra.setSbcs(alertCodeHex+"01="+param1.getValue()+","+alertCodeHex+"02="+param2.getValue()+","
						+alertCodeHex+"03="+param3.getValue());
				raList.add(ra);
				break;
			}
			case 32:{//ERC32：终端与主站通信流量超门限事件记录
				RtuAlert ra=new RtuAlert();
				alertCodeHex="C320";
				ra.setAlertCode(Integer.parseInt(alertCodeHex,16));		
				String value="C32001="+DataItemParser.parseValue(data.substring(0,8),"HTB4").getValue()+",";
				value=value+"C32002="+DataItemParser.parseValue(data.substring(8,16),"HTB4").getValue();				
				ra.setSbcs(value);
				raList.add(ra);
				break;
			}
			case 33:{//ERC33：电能表运行状态字变位事件记录
				RtuAlert ra=new RtuAlert();
				alertCodeHex="C330";
				ra.setAlertCode(Integer.parseInt(alertCodeHex,16));	
				tn=Integer.parseInt(DataItemParser.parseValue(data.substring(0,4),"HTB2").getValue());
				data=data.substring(4);	
				String value="";
				for (int i=0;i<14;i++){
					value=value+alertCodeHex+DataSwitch.StrStuff("0", 2, ""+i, "left")+"="+DataItemParser.parseValue(data.substring(i*4,i*4+4),"BS2").getValue()+",";
				}	
				value=value.substring(0,value.length()-1);//消去最后一个,
				data=data.substring(56);
				ra.setSbcs(value);
				raList.add(ra);
				break;
			}
			case 34:{//ERC34：CT异常事件记录
				tn=Integer.parseInt(DataItemParser.parseValue(data.substring(0,4),"HTB2").getValue());
				data=data.substring(4);		
				DataValue param1=DataItemParser.parseValue(data.substring(0,2),"BS1");						
				data=data.substring(12);
				RtuAlert ra=new RtuAlert();					
				if ((tn&32768)==32768){//告警发生					
					alertCodeHex="C341";//CT异常事件发生														
				}
				else{//恢复			
					alertCodeHex="C342";//CT异常事件恢复					
				}
				ra.setAlertCode(Integer.parseInt(alertCodeHex,16));	
				ra.setSbcs(alertCodeHex+"01="+param1.getValue());
				raList.add(ra);		
				tn=tn&32767;								
				break;
			}
			case 35:{//ERC35：发现未知电表事件记录
				RtuAlert ra=new RtuAlert();
				alertCodeHex="C350";
				ra.setAlertCode(Integer.parseInt(alertCodeHex,16));	
				tn=Integer.parseInt(DataItemParser.parseValue(data.substring(0,2),"HTB1").getValue());
				data=data.substring(2);	
				String value="";
				int num=Integer.parseInt(DataItemParser.parseValue(data.substring(0,2),"HTB1").getValue());
				for (int i=0;i<num;i++){
					value=value+alertCodeHex+DataSwitch.StrStuff("0", 2, ""+i*3, "left")+"="+DataItemParser.parseValue(data.substring(0,12),"HEX6").getValue()+",";
					value=value+alertCodeHex+DataSwitch.StrStuff("0", 2, ""+i*3+1, "left")+"="+DataItemParser.parseValue(data.substring(12,14),"BS1").getValue()+",";
					value=value+alertCodeHex+DataSwitch.StrStuff("0", 2, ""+i*3+2, "left")+"="+DataItemParser.parseValue(data.substring(14,16),"BS1").getValue()+",";
					data=data.substring(16);
				}	
				value=value.substring(0,value.length()-1);//消去最后一个,
				ra.setSbcs(value);
				raList.add(ra);
				break;
			}
			case 46:
				alertCodeHex="C460";
			case 39:{//集中器自动发现注册
				if("".equals(alertCodeHex))
					alertCodeHex="C390";
				String format = "HTB2#HTB2#BS1#HTB1#HEX6#HEX6#BS1#BS1#HEX6#BS1";
				DataValue dataValue=DataItemParser.parser(data, format, false);
				String value =dataValue.getValue();
				RtuAlert ra=new RtuAlert();
				if(data.length()==(2+2+1+1+6+6+1+1+6+1+6)*2){
					//兼容数据区带时间的处理
					String timeValue = data.substring(data.length()-6*2,data.length());
					DataValue time = DataItemParser.parser(timeValue, "A1", false);
					ra.setAlertFullTime(time.getValue());
				}
				ra.setSbcs(value);
				ra.setAlertCode(Integer.parseInt(alertCodeHex,16));
				raList.add(ra);
				break;
			}
			case 40:{//标准事件,测量点+CODE+时间
				String format="HTB2#HEX1#A15";
				DataValue dataValue = DataItemParser.parser(data, format, false);
				RtuAlert ra = new RtuAlert();
				String value = dataValue.getValue();
				String[] params=value.split("#");
				tn = Integer.parseInt(params[0]);
				ra.setAlertCode(Integer.parseInt("0301"+params[1],16));
				if (data.length() == (2 + 1 + 5) * 2) {//兼容两种程序，一种时间不带秒，一种时间带秒
					ra.setAlertTime(params[2]);
				} else {
					ra.setAlertFullTime(params[2]+":"+(data.substring(data.length() - 2,data.length())));
				}
				raList.add(ra);
				break;
			}
			case 41:{//电表窃电事件,测量点+CODE+时间
				String format="HTB2#HEX1#A15";
				DataValue dataValue = DataItemParser.parser(data, format, false);
				RtuAlert ra = new RtuAlert();
				String value = dataValue.getValue();
				String[] params=value.split("#");
				tn = Integer.parseInt(params[0]);
				ra.setAlertCode(Integer.parseInt("0302"+params[1],16));
				if (data.length() == (2 + 1 + 5) * 2) {//兼容两种程序，一种时间不带秒，一种时间带秒
					ra.setAlertTime(params[2]);
				} else {
					ra.setAlertFullTime(params[2]+":"+(data.substring(data.length() - 2,data.length())));
				}
				raList.add(ra);
				break;
			}
			case 42:{//继电器事件,测量点+CODE+时间
				String format="HTB2#HEX1#A15";
				DataValue dataValue = DataItemParser.parser(data, format, false);
				RtuAlert ra = new RtuAlert();
				String value = dataValue.getValue();
				String[] params=value.split("#");
				tn = Integer.parseInt(params[0]);
				ra.setAlertCode(Integer.parseInt("0303"+params[1],16));
				if (data.length() == (2 + 1 + 5) * 2) {//兼容两种程序，一种时间不带秒，一种时间带秒
					ra.setAlertTime(params[2]);
				} else {
					ra.setAlertFullTime(params[2]+":"+(data.substring(data.length() - 2,data.length())));
				}
				raList.add(ra);
				break;
			}
			case 43:{//电网事件,测量点+CODE+时间
				String format="HTB2#HEX1#A15";
				DataValue dataValue = DataItemParser.parser(data, format, false);
				RtuAlert ra = new RtuAlert();
				String value = dataValue.getValue();
				String[] params=value.split("#");
				tn = Integer.parseInt(params[0]);
				ra.setAlertCode(Integer.parseInt("0304"+params[1],16));
				if (data.length() == (2 + 1 + 5) * 2) {//兼容两种程序，一种时间不带秒，一种时间带秒
					ra.setAlertTime(params[2]);
				} else {
					ra.setAlertFullTime(params[2]+":"+(data.substring(data.length() - 2,data.length())));
				}
				raList.add(ra);
				break;
			}
			case 44:{//固件升级结果
				alertCodeHex="C440";
				String format="HTB2#HTB1#HTB1#ASC32";//2字节测量点+1字节事件属性（1：表计程序，2：表计设置参数，3：集中器程序）+1字节标志（1：OK，0：fail）+32字节版本信息
				DataValue dataValue = DataItemParser.parser(data, format, false);
				RtuAlert ra = new RtuAlert();
				String value = dataValue.getValue();
				ra.setSbcs(value);
				tn  = Integer.parseInt(value.split("#")[0]);
				ra.setAlertCode(Integer.parseInt(alertCodeHex,16));
				raList.add(ra);
				break;
			}
			case 45:{//表箱或采集器事件
				alertCodeHex="C450";
				String sec = null;
				if(data.length() != (2+2+5+2)*2){
					//兼容两种程序，一种时间不带秒，一种时间带秒
					String A8 = data.substring(data.length()-4, data.length());
					String prefix = data.substring(0,data.length()-6);
					sec= data.substring(data.length()-6, data.length()-4);
					data = prefix+A8;
				}
				String format = "HTB2#RVR2#A15#A8"; //PN#CODE#TIME#VALUE
				DataValue dataValue = DataItemParser.parser(data, format, false);
				RtuAlert ra = new RtuAlert();
				String value = dataValue.getValue();
				tn = Integer.parseInt(value.split("#")[0]);
				if(tn==0){//如果参数里上来的测量点为0,将其置为1
					value="1"+value.substring(value.indexOf("#"));
				}
				ra.setInnerCode(value.split("#")[1]);//用于主站召测,获得事件ID
				if(sec!=null){
					int s1=value.indexOf("#",0)+1;
					s1=value.indexOf("#",s1)+1;
					int s2=value.indexOf('#',s1);
					String prefix = value.substring(0,s1);
					String time  = value.substring(s1, s2);
					String last = value.substring(s2);
					time+=":"+sec;
					value = prefix+time+last;
					ra.setAlertFullTime(time);
				}else{
					ra.setAlertTime(value.split("#")[2]);
				}
				ra.setSbcs(value);
				ra.setAlertCode(Integer.parseInt(alertCodeHex,16));
				raList.add(ra);
				break;
			}	
			case 60:{//ERC60：通信测试请求事件
				RtuAlert ra=new RtuAlert();
				alertCodeHex="C600";
				ra.setAlertCode(Integer.parseInt(alertCodeHex,16));		
				String value="C60001="+DataItemParser.parseValue(data.substring(0,8),"BS4").getValue();								
				ra.setSbcs(value);	
				raList.add(ra);
				break;
			}
		}
		for (RtuAlert rtuAlert:raList){
			if(tn==0) tn=1;
			rtuAlert.setTn(""+tn);
			if(rtuAlert.getAlertTime()==null){
				rtuAlert.setAlertTime(DataItemParser.parseValue(alertTime,"A15").getValue());				
			}
		}		
		return raList;
	}
}
