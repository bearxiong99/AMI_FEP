package cn.hexing.fas.protocol.gw.codec;

import cn.hexing.exception.MessageDecodeException;
import cn.hexing.fas.model.HostCommand;
import cn.hexing.fas.model.HostCommandResult;
import cn.hexing.fas.protocol.gw.parse.DataItemParser;
import cn.hexing.fas.protocol.gw.parse.DataSwitch;
import cn.hexing.fas.protocol.gw.parse.DataTimeTag;
import cn.hexing.fas.protocol.gw.parse.ParseTool;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.gw.MessageGw;

/**
 * (功能码：01H,02H,09H,0AH,0BH,0CH,0EH,10H)用于下行报文帧解析
 * 
 */
public class C02MessageDecoder extends AbstractMessageDecoder {	
	
    public Object decode(IMessage message) {    
		HostCommand hc=new HostCommand();
		try{			
			String data=ParseTool.getMsgData(message);	
			MessageGw msg = (MessageGw) message;
			if (msg.head.seq_tpv==1)//TpV位置1表示附加域有时间标签
				data=data.substring(0,data.length()-12);//消去6个字节的时间标签
			if (msg.head.c_acd==1)//ACD位置1表示附加域有事件计数器
				data=data.substring(0,data.length()-4);//消去2个字节的事件计数器
			String sAFN=Integer.toString(msg.getAFN()& 0xff,16).toUpperCase();
			sAFN=DataSwitch.StrStuff("0", 2, sAFN, "left");				
			while(data.length()>=8){
				int[] tn=DataItemParser.measuredPointParser(data.substring(0,4));
				String[] codes= DataItemParser.dataCodeParser(data.substring(4,8), sAFN);
				data=data.substring(8);//消去数据单元标识
				
				if (codes.length>0&&codes[0].equals("0AF010")){
					if (data.length()>=8){
						int count=Integer.parseInt(DataItemParser.parseValue(data.substring(0,4),"HTB2").getValue());
						data=data.substring(4);
						String value="";
						for(int i=0;i<count;i++){
							value=value+DataItemParser.parseValue(data.substring(0,4),"HTB2").getValue()+",";
							data=data.substring(4);
						}
						if (!value.equals(""))
							value=value.substring(0,value.length()-1);
						for (int i=0;i<tn.length;i++){
							for (int j=0;j<codes.length;j++){
								HostCommandResult hcr=new HostCommandResult();
								hcr.setCode(codes[j]);
								hcr.setTn(""+tn[i]);
								hcr.setValue(value);														
								hc.addResult(hcr);								
							}
						}
					}
				}
				else if (codes.length>0&&(codes[0].equals("0AF011")||
									    	codes[0].equals("0AF013")||
									    	codes[0].equals("0AF014")||
									    	codes[0].equals("0AF015")||
									    	codes[0].equals("0AF033")||
									    	codes[0].equals("0AF034"))){
					if (data.length()>=4){
						int count=Integer.parseInt(DataItemParser.parseValue(data.substring(0,2),"HTB1").getValue());
						data=data.substring(2);
						String value="";
						for(int i=0;i<count;i++){
							value=value+DataItemParser.parseValue(data.substring(0,2),"HTB1").getValue()+",";
							data=data.substring(2);
						}
						if (!value.equals(""))
							value=value.substring(0,value.length()-1);
						for (int i=0;i<tn.length;i++){
							for (int j=0;j<codes.length;j++){
								HostCommandResult hcr=new HostCommandResult();
								hcr.setCode(codes[j]);
								hcr.setTn(""+tn[i]);
								hcr.setValue(value);														
								hc.addResult(hcr);								
							}
						}
					}
				}
				else if (codes.length>0&&(codes[0].equals("0AF038")||
										 	codes[0].equals("0AF039"))){
					if (data.length()>=6){
						String value=data.substring(0,2);
						data=data.substring(2);
						int count=Integer.parseInt(DataItemParser.parseValue(data.substring(0,2),"HTB1").getValue());
						data=data.substring(2);						
						for(int i=0;i<count;i++){
							value=value+DataItemParser.parseValue(data.substring(0,2),"HTB1").getValue()+",";
							data=data.substring(2);
						}
						if (!value.equals(""))
							value=value.substring(0,value.length()-1);
						for (int i=0;i<tn.length;i++){
							for (int j=0;j<codes.length;j++){
								HostCommandResult hcr=new HostCommandResult();
								hcr.setCode(codes[j]);
								hcr.setTn(""+tn[i]);
								hcr.setValue(value);														
								hc.addResult(hcr);								
							}
						}
					}
				}
				else if(sAFN.equals("0D")){
					DataTimeTag dataTimeTag=new DataTimeTag() ;	
					int dataType=0;	
					String date="";
					for (int i=0;i<tn.length;i++){					
						for (int j=0;j<codes.length;j++){
							dataType=getGw0DDataType(Integer.parseInt(codes[j].substring(3,6)));
							if (dataType==0){		
								//小时冻结时标解析得到：开始时间(YYYYMMDDHHNN)+时间间隔+数据点数
								dataTimeTag=DataItemParser.getTaskDateTimeInfo(data.substring(0,14), 2,"");
								date=DataSwitch.ReverseStringByByte(data.substring(0,14));
								data=data.substring(14);	
							}
							else{//冻结数据					
								if (dataType==1){
									//日冻结时标3个字YYYYMMDD
									dataTimeTag=DataItemParser.getTaskDateTimeInfo(data.substring(0,6), 3,"");
									date=DataSwitch.ReverseStringByByte(data.substring(0,6));
									data=data.substring(6);	
								}
								else{	
									//月冻结时标2个字YYYYMM
									dataTimeTag=DataItemParser.getTaskDateTimeInfo(data.substring(0,4), 4,"");
									date=DataSwitch.ReverseStringByByte(data.substring(0,4));
									data=data.substring(4);	
								}	
							}
							HostCommandResult hcr=new HostCommandResult();
							hcr.setCode(codes[j]);
							hcr.setTn(""+tn[i]);
							hcr.setValue(date);																
							hc.addResult(hcr);		
						}
					}
				}
				else if(sAFN.equals("0E")||sAFN.equals("10")||sAFN.equals("0F")){
					for (int i=0;i<tn.length;i++){
						for (int j=0;j<codes.length;j++){
							HostCommandResult hcr=new HostCommandResult();
							hcr.setCode(codes[j]);
							hcr.setTn(""+tn[i]);
							hcr.setValue(data);																
							hc.addResult(hcr);								
						}
					}
					data="";
				}							
				else{
					for (int i=0;i<tn.length;i++){
						for (int j=0;j<codes.length;j++){
							HostCommandResult hcr=new HostCommandResult();
							hcr.setCode(codes[j]);
							hcr.setTn(""+tn[i]);
							hcr.setValue("");																
							hc.addResult(hcr);								
						}
					}
				}
			}
			hc.setStatus(HostCommand.STATUS_SUCCESS);																			
		}catch(Exception e){
			throw new MessageDecodeException(e);
		}
		return hc;	
    }//判断二类数据类型:0曲线数据；1日冻结；2月冻结
	public int getGw0DDataType(int fn){
		//默认月冻结
		int type=2;
		//日冻结
		if ((fn>=1&&fn<=12)||(fn>=25&&fn<=32)||(fn>=41&&fn<=43)||(fn==45)||
			(fn>=49&&fn<=50)||(fn==53)||(fn>=57&&fn<=59)||(fn>=113&&fn<=129)||
			(fn>=153&&fn<=156)||(fn>=161&&fn<=176)||(fn>=185&&fn<=192)||(fn==209)){
			type=1;
		}
		//曲线
		else if((fn>=73&&fn<=110)||(fn>=138&&fn<=148)||(fn>=217&&fn<=218)){
			type=0;
		}
		return type;
	}

}
    
    