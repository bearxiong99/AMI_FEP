package cn.hexing.fas.protocol.pos.parse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.hexing.fas.protocol.gw.parse.DataSwitch;
import cn.hexing.fas.protocol.gw.parse.DataValue;
import cn.hexing.fas.protocol.gw.parse.ParserBS;
import cn.hexing.fas.protocol.gw.parse.ParserHTB;
import cn.hexing.fas.protocol.pos.PosItemConfig;


public class DataItemParser {
	private static Log log=LogFactory.getLog(DataItemParser.class);
	public static DataValue parser(String input,String format,boolean recurrentTag){
		
		DataValue dataItem=new DataValue();
		try{
			String[] formatItems=format.split("#");
			String value="";
			int len=0;
			DataValue dataItemTemp=new DataValue();
			if (formatItems.length>0){
				for (int i=0;i<formatItems.length;i++){				
					if (formatItems[i].startsWith("N")||formatItems[i].startsWith("M")){//数据格式（存在递归调用变长）存在变长格式N
						DataValue nValue=new DataValue();
						nValue=parseValue(input,formatItems[i]);
						input=input.substring(nValue.getLen());						
						format=format.substring(format.indexOf("#")+1);	
						value=value+nValue.getValue()+"#";
						len=len+nValue.getLen();
						if (formatItems[i].startsWith("M")){//如果变量为按位表示的数量的格式，则取bit=1的个数
							nValue.setValue(""+ParserBS.getBSCount(nValue.getValue()));
						}
						for(int j=0;j<Integer.parseInt(nValue.getValue());j++){							
							if (format.indexOf("N")>=0||format.indexOf("M")>=0){//有递归格式,第一层用";"
								dataItemTemp=parser(input,format,true);
								input=input.substring(dataItemTemp.getLen());	
								value=value+dataItemTemp.getValue()+";";								
							}
							else{	
								dataItemTemp=parser(input,format,false);
								input=input.substring(dataItemTemp.getLen());	
								if (!recurrentTag)	//没递归用";"
									value=value+dataItemTemp.getValue()+";";
								else				//有递归格式,第二层用","
									value=value+dataItemTemp.getValue()+",";
							}
							len=len+dataItemTemp.getLen();
						}
						if (value.endsWith(",")||value.endsWith(";"))//消去最后一个间隔符
							value=value.substring(0,value.length()-1);
						break;
																
					}
					else if (formatItems[i].startsWith("X")){//数据格式为并行变长,即两个变长格式没有从属关系
						DataValue nValue=new DataValue();
						while(format.length()>0){
							if (format.indexOf("#")>0&&format.substring(0,format.indexOf("#")).startsWith("X")){
								nValue=parseValue(input,format.substring(0,format.indexOf("#")));
								format=format.substring(format.indexOf("#")+1);	
								input=input.substring(nValue.getLen());						
								value=value+nValue.getValue()+"#";
								len=len+nValue.getLen();						
								for(int j=0;j<Integer.parseInt(nValue.getValue());j++){
									dataItemTemp=parser(input,format.substring(0,format.indexOf("#")),false);
									input=input.substring(dataItemTemp.getLen());			
									value=value+dataItemTemp.getValue()+";";								
									len=len+dataItemTemp.getLen();
								}
								if (value.endsWith(";")){//消去最后一个间隔符,用#号替换
									value=value.substring(0,value.length()-1)+"#";
								}
								format=format.substring(format.indexOf("#")+1);	
							}	
							else{
								dataItemTemp=parseValue(input,format);
								input=input.substring(dataItemTemp.getLen());						
								format="";	
								value=value+dataItemTemp.getValue()+"#";
								len=len+dataItemTemp.getLen();
							}							
						}									
						break;
					}else if(formatItems[i].startsWith("CB")){//如果是以BLOCK开始的
						String block=formatItems[i];
						//这里需要知道当前是哪个命令的CB_HTB1_4_1  
						String[] splits=block.split("_");
						String strIfType=splits[1];//条件类型
						String subId = splits[2];//subClass
						String index=splits[3];//第几个条件选择块
						dataItemTemp = parseValue(input, strIfType);
						input = input.substring(dataItemTemp.getLen());
						value +=dataItemTemp.getValue()+"#";
						len +=dataItemTemp.getLen();
						//						String condition = values[values.length-1];
//						//cmd_index_condition
						format=PosItemConfig.itemBlockMap.get(subId+"_"+index+"_"+dataItemTemp.getValue());
						if(format!=null){
							dataItemTemp=parser(input, format,false);
							input=input.substring(dataItemTemp.getLen());
							value += dataItemTemp.getValue().replace("#", "@")+"#";
							len +=dataItemTemp.getLen();
						}
					}
					else{
						dataItemTemp=parseValue(input,formatItems[i]);
						input=input.substring(dataItemTemp.getLen());
						if(format!=null){
							format=format.substring(format.indexOf("#")+1);	
						}
						value=value+dataItemTemp.getValue()+"#";
						len=len+dataItemTemp.getLen();
					}
				}
				if (value.endsWith("#")||value.endsWith(",")||value.endsWith(";"))//消去最后一个间隔符
					value=value.substring(0,value.length()-1);
				dataItem.setValue(value);
				dataItem.setLen(len);
			}
		}
		catch(Exception e){
			   log.equals("parsevalue error:"+e.toString());
		}
		return dataItem;
		
	}

	private static DataValue parseValue(String input, String format) {
		
		DataValue dataValue=new DataValue();
		try{
			String output="";
			int len=0;			
			try{
				if(format.startsWith("HTB")){
					len=Integer.parseInt(format.substring(3));
					input=DataSwitch.ReverseStringByByte(input.substring(0,len*2));
					output=ParserHTB.parseValue(input, len*2);
				}else if(format.startsWith("BCD")){
					len=Integer.parseInt(format.substring(3));
					output = input.substring(0,len*2);
				}else if(format.startsWith("ASC")){
					len=Integer.parseInt(format.substring(3));
					String data=input.substring(0, len*2);
					if((data.length()%2)==0){
			          int byteLen=data.length()/2;  //字节长度
			          char[] chrList=new char[byteLen];
			          for (int i=0;i<byteLen;i++){
			            chrList[i]=(char)(Integer.parseInt(data.substring(2*i,2*i+2),16));
			          }
			          output=(new String(chrList)).trim();
			        }
				}else if(format.startsWith("N")){
					len=Integer.parseInt(format.substring(1));
					output=ParserHTB.parseValue(input, len*2);
				}
			}
			catch(Exception e){
				 log.equals("parsevalue error:"+e.toString());
			}
			dataValue.setValue(output);
			dataValue.setLen(len*2);
		}
		catch(Exception e){
		}
		return dataValue;
	}
	
	public static void main(String[] args) {
		DataValue s = parser("616263000000000000000000000000000000000000000000000000000000000000000000000000006566670000000000000000000000000000000000000000000000000000000000000000000000000061736400000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000173640000000000000000000000000073640000000000000000000000617364000000000000000000007364000000000000000000000000000000000000310000000000000000000000000101000001010101016162630000000000000000000000000000000000617300000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000007364000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000073640000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000617300000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000102000000010000000173640000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000010000000173640000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", 
				"ASC40#ASC40#ASC80#BCD7#ASC15#ASC13#ASC13#ASC20#ASC13#HTB1#HTB1#BCD3#HTB1#HTB1#N1#CB_HTB1_4_1", 
				false);
		System.out.println(s);
	}
}
