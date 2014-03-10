package cn.hexing.fas.protocol.gw.parse;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.hexing.fas.protocol.zj.parse.Parser01;
import cn.hexing.fas.protocol.zj.parse.Parser19;
import cn.hexing.fk.tracelog.TraceLog;
import cn.hexing.fk.utils.HexDump;



/**
 * 字节数据解析为可读数据项数据
 *
 */
public class DataItemParser {
	private static Log log=LogFactory.getLog(DataItemCoder.class);
	private static final TraceLog trace = TraceLog.getTracer(DataItemParser.class);
	
	public static void main(String[] args) {
		if (isValidTime("2010-06-32","yyyy-MM-dd")){			
			System.out.println("time is valid");
		}
		else{
			System.out.println("time is unvalid");
		}
	}
	/**
	 * 数据格式解码方法
	 * @param 报文数据项内容	例input=16
	 * @param 数据项格式		例format=HTB1
	 * @return 解析数据值		return =0A
	 */
	public static DataValue parseValue(String input,String format){
		DataValue dataValue=new DataValue();
		try{
			String output="";
			int len=0;			
			try{				
				if(format.startsWith("HTB")){
					len=Integer.parseInt(format.substring(3));
					output=ParserHTB.parseValue(input, len*2);
				}else if(format.startsWith("HEX")){
					len=Integer.parseInt(format.substring(3));
					output=ParserHEX.parseValue(input, len*2);
				}else if(format.startsWith("STS")){
					len=Integer.parseInt(format.substring(3));
					output=ParserString.parseValue(input, len*2);
				}else if(format.startsWith("ASC")){
					len=Integer.parseInt(format.substring(3));
					output=ParserASC.parseValue(input, len*2);
				}else if(format.startsWith("SIM")){
					len=Integer.parseInt(format.substring(3));
					output=ParserSIM.parseValue(input, len*2);
				}else if(format.startsWith("BS")){
					len=Integer.parseInt(format.substring(2));
					output=ParserBS.parseValue(input, len*2);
				}else if(format.startsWith("IP")){
					len=Integer.parseInt(format.substring(2));
					output=ParserIP.parseValue(input, len*2);
				}else if(format.startsWith("N")||format.startsWith("X") || format.startsWith("L")){
					len=Integer.parseInt(format.substring(1));
					output=ParserHTB.parseValue(input, len*2);
				}else if(format.startsWith("M")){
					len=Integer.parseInt(format.substring(1));
					output=ParserBS.parseValue(input, len*2);
				}else if(format.equals("A1")){//完整的日期时间型
					len=6;
					output=ParserA1.parseValue(input, len*2);
				}else if(format.equals("A2")){//带幂部和正负号的数值型
					len=2;
					output=ParserA2.parseValue(input, len*2);
				}else if(format.equals("A3")){//带单位和正负号的数值型
					len=4;
					output=ParserA3.parseValue(input, len*2);
				}else if(format.equals("A4")){//CC带正负号的浮点数
					len=1;
					output=ParserFTB.parseValue(input,"CC",len*2);
				}else if(format.equals("A5")){//CCC.C带正负号的浮点数
					len=2;
					output=ParserFTB.parseValue(input,"CCC.C",len*2);
				}else if(format.equals("A6")){//CC.CC带正负号的浮点数
					len=2;
					output=ParserFTB.parseValue(input,"CC.CC",len*2);
				}else if(format.equals("A7")){//000.0浮点数
					len=2;
					output=ParserFTB.parseValue(input,"000.0",len*2);
				}else if(format.equals("A8")){//0000浮点数
					len=2;
					output=ParserFTB.parseValue(input,"0000",len*2);
				}else if(format.equals("A9")){//CC.CCCC带正负号的浮点数
					len=3;
					output=ParserFTB.parseValue(input,"CC.CCCC",len*2);
				}else if(format.equals("A10")){//000000浮点数
					len=3;
					output=ParserFTB.parseValue(input,"000000",len*2);
				}else if(format.equals("A11")){//000000.00浮点数
					len=4;
					output=ParserFTB.parseValue(input,"000000.00",len*2);
				}else if(format.equals("A12")){//000000000000浮点数
					len=6;
					output=ParserFTB.parseValue(input,"000000000000",len*2);
				}else if(format.equals("A13")){//0000.0000浮点数
					len=4;
					output=ParserFTB.parseValue(input,"0000.0000",len*2);
				}else if(format.equals("A14")){//000000.0000浮点数
					len=5;
					output=ParserFTB.parseValue(input,"000000.0000",len*2);
				}else if(format.equals("A15")){//yyMMddHHmm时间型
					len=5;
					output=ParserDATE.parseValue(input,"yyyy-MM-dd HH:mm","yyMMddHHmm",len*2);
				}else if(format.equals("A16")){//ddHHmmss时间型
					len=4;
					output=ParserDATE.parseValue(input,"dd HH:mm:ss","ddHHmmss",len*2);
				}else if(format.equals("A17")){//MMddHHmm时间型
					len=4;
					output=ParserDATE.parseValue(input,"MM-dd HH:mm","MMddHHmm",len*2);
				}else if(format.equals("A18")){//ddHHmm时间型
					len=3;
					output=ParserDATE.parseValue(input,"dd HH:mm","ddHHmm",len*2);
				}else if(format.equals("A19")){//HHmm时间型
					len=2;
					output=ParserDATE.parseValue(input,"HH:mm","HHmm",len*2);
				}else if(format.equals("A20")){//yyMMdd时间型
					len=3;
					output=ParserDATE.parseValue(input,"yyyy-MM-dd","yyMMdd",len*2);
					//output=dateStrCheck(output,DataSwitch.ReverseStringByByte(input.substring(0,len*2)),"yyyy-MM-dd");
				}else if(format.equals("A21")){//yyMM时间型
					len=2;
					output=ParserDATE.parseValue(input,"yyyy-MM","yyMM",len*2);
				}else if(format.equals("A22")){//0.0浮点数
					len=1;
					output=ParserFTB.parseValue(input,"0.0",len*2);
				}else if(format.equals("A23")){//00.0000浮点数
					len=3;
					output=ParserFTB.parseValue(input,"00.0000",len*2);
				}else if(format.equals("A24")){//ddHH时间型
					len=2;
					output=ParserDATE.parseValue(input,"dd HH","ddHH",len*2);
				}else if(format.equals("A25")){//CCC.CCC带正负号的浮点数
					len=3;
					output=ParserFTB.parseValue(input,"CCC.CCC",len*2);
				}else if(format.equals("A26")){//0.000浮点数
					len=2;
					output=ParserFTB.parseValue(input,"0.000",len*2);
				}else if(format.equals("A27")){//00000000浮点数
					len=4;
					output=ParserFTB.parseValue(input,"00000000",len*2);
				}else if(format.equals("A28")){//CCCCCC.CC带正负号的浮点数
					len=4;
					output=ParserFTB.parseValue(input, "CCCCCC.CC", len*2);
				}else if(format.equals("A30")){//000.00000浮点数
					len=4;
					output=ParserFTB.parseValue(input,"000.00000",len*2);
				}else if(format.equals("Z00")){ //HX645 BCD码  000000.00
					len=4;
					output= ""+Parser01.parsevalue(HexDump.toArray(input), 0, 4, 2);
				}else if(format.startsWith("RVR")){//use for HX645 token ,reverse token
					len=Integer.parseInt(format.substring(3));
					output=DataSwitch.ReverseStringByByte(input.substring(0, len*2));
				}else if(format.startsWith("BCD")){//use for hx645
					len=Integer.parseInt(format.substring(3));
					output=""+Parser01.parsevalue(HexDump.toArray(input), 0, len, 0);
				}else if(format.startsWith("RAW")){
					len=Integer.parseInt(format.substring(3));
					output=input.substring(0, len*2);
				}else if(format.startsWith("FTM")){//full time  年月日周时分秒
					len=7;
					output=(String) Parser19.parsevalue(HexDump.toArray(input), 0, len, 0);
				}
			}
			catch(Exception e){
				log.error("constructor error:"+e.toString());	        
			}
			dataValue.setValue(output);
			dataValue.setLen(len*2);
		}
		catch(Exception e){
		    log.equals("parsevalue error:"+e.toString());	        
		}
		return dataValue;
	}
	public static String dateStrCheck(String dateStr,String inputStr,String dateFormat){
		String rt=dateStr;
		if (dateStr.length()==dateFormat.length()&&dateFormat.equals("yyyy-MM-dd")){
			inputStr="20"+inputStr.substring(0,2)+"-"+inputStr.substring(2,4)+"-"+inputStr.substring(4,6);
			if (!dateStr.equals(inputStr)){
				if( trace.isEnabled() )
					trace.trace("date parse error,inputStr="+inputStr+",dateStr="+dateStr+",dateFormat="+dateFormat);
				dateStr=inputStr;			
			}
		}
		return rt;
	}
	
	//解析信息点得到测量点列表
	public static int[] measuredPointParser(String sDA) { 
        int iCount = 0;
        int[] measuredListTemp = new int[8];
        try {	                
            String sDA1 = sDA.substring(0, 2); //信息点元
            String sDA2 = sDA.substring(2, 4); //信息点组
            if (sDA.equals("0000")) { //测量点号为0
                iCount = 1;
                measuredListTemp[0] = 0;
            } else {
                char[] cDA1;
                int iDA2 = Integer.parseInt(sDA2,16);
                cDA1 = (DataSwitch.Fun2HexTo8Bin(sDA1)).toCharArray();
                for (int i = 7; i >=0; i--) {
                    if (cDA1[i] == '1') {
                    	measuredListTemp[iCount] = (iDA2 - 1) * 8 + 8 -
                                i; //信息点序号
                        iCount = iCount + 1;
                    }
                }
            }
            
        } catch (Exception e) {
        	log.error("MeasuredPointParser error:"+e.toString());	        
        }
        int[] measuredList=new int[iCount];           
        for (int i = 0; i < iCount; i++) {
        	measuredList[i] = measuredListTemp[i];
        }
        return measuredList;     
    }
	//解析信息类得到数据项标识列表
	public static String[] dataCodeParser(String sDT,String sAFN) { 
        int iCount = 0;
        int[] codeListTemp = new int[8];
        try {	                
            String sDT1 = sDT.substring(0, 2); //信息类元
            String sDT2 = sDT.substring(2, 4); //信息类组
            char[] cDT1;
            int iDT2 = Integer.parseInt(sDT2,16);
            cDT1 = (DataSwitch.Fun2HexTo8Bin(sDT1)).toCharArray();
            for (int i = 7; i >=0; i--) {
                if (cDT1[i] == '1') {
                	codeListTemp[iCount] = iDT2 * 8 + 8 -
                            i; //信息点序号
                    iCount = iCount + 1;
                }
            }
            
            
        } catch (Exception e) {
        	log.error("dataCodeParser error:"+e.toString());	        
        }
        String[] codeList=new String[iCount];           
        for (int i = 0; i < iCount; i++) {
        	codeList[i] = sAFN+"F"+DataSwitch.StrStuff("0",3,""+codeListTemp[i],"left");
        }
        return codeList;     
    }
	//返回值：开始时间(YYYYMMDDHHNNSS)+时间间隔+数据点数
	public static DataTimeTag getTaskDateTimeInfo(String sDateTimeLabel, int DateType,String rtuAddr) { 
		DataTimeTag dataTimeTag=new DataTimeTag();
        try {
            String sDateTime = "", sNowDateTime = "";
            int iDataDensity = 0,iDataCount=0; //时间间隔、数据点数
            if (DateType == 1) { //小时冻结数据时标
               	//小时              
                sDateTime = ""+(Integer.parseInt(sDateTimeLabel.substring(0, 1)) & 3)+sDateTimeLabel.substring(1, 2);
            	//数据密度
                iDataDensity = Integer.parseInt(sDateTimeLabel.substring(2,4),16);     
                //获取当前时间
                Calendar cLogTime = Calendar.getInstance();
                SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
                sNowDateTime = formatter.format(cLogTime.getTime());
                
                //判断上送数据小时大于当前小时则为上一天数据
                if (Integer.parseInt(sDateTime)>Integer.parseInt(sNowDateTime.substring(8, 10))) {
                    sNowDateTime = DataSwitch.IncreaseDateTime(sNowDateTime,-1, 4);
                    sNowDateTime=sNowDateTime.substring(0,4)+sNowDateTime.substring(5,7)+sNowDateTime.substring(8,10)+sNowDateTime.substring(11,13)+sNowDateTime.substring(14,16);
                }
            } else if (DateType == 2) { //曲线数据时标
            	//年月日时分
                sDateTime = "20" +DataSwitch.ReverseStringByByte(sDateTimeLabel.substring(0, 10)); 
                //数据密度
                iDataDensity = Integer.parseInt(sDateTimeLabel.substring(10,12), 16); 
                //数据点数
                iDataCount = Integer.parseInt(sDateTimeLabel.substring(12, 14), 16); 
            } else if (DateType == 3) { //日数据冻结时标
            	//年月日
                sDateTime = parseValue(sDateTimeLabel.substring(0, 6),"A20").getValue(); 
               /* if (!isValidTime(sDateTime,"yyyy-MM-dd")){//如果日冻结时标非法则打印日志
                	trace.trace("rtuAddr="+rtuAddr+";sDateTimeLabel="+sDateTimeLabel+";sDateTime="+sDateTime);
                }*/
            } else if (DateType == 4) { //月数据冻结时标
            	//年月
                sDateTime = parseValue(sDateTimeLabel.substring(0, 4),"A21").getValue();  
            }
            switch (iDataDensity) {//数据密度转换成数据间隔和数据点数
	            case 1: {
	            	iDataDensity = 15;
	                if (DateType == 1) {
	                	//小时冻结默认密度为15分钟，则冻结时间为15，30，45，0	               
	                	sDateTime = sNowDateTime.substring(0, 8) + sDateTime +"15"; 
	                	iDataCount = 4;
	                }
	                break;
	            }
	            case 2: {
	            	iDataDensity =30;
	                if (DateType == 1) {
	                	//小时冻结密度为30分钟，则冻结时间为30，0
	                	sDateTime = sNowDateTime.substring(0, 8) + sDateTime +"30"; 
	                	iDataCount = 2;
	                }
	                break;
	            }
	            case 3: {
	            	iDataDensity = 60;
	                if (DateType == 1) {
	                	sDateTime = sNowDateTime.substring(0, 8) + sDateTime +"00";
	                	iDataCount = 1;
	                }
	                break;
	            }
	            case 254: {
	            	iDataDensity = 5;
	                if (DateType == 1) {
	                	sDateTime = sNowDateTime.substring(0, 8) + sDateTime +"05";
	                	iDataCount = 12;
	                }
	                break;
	            }
	            case 255: {
	            	iDataDensity = 1;
	                if (DateType == 1) {
	                	sDateTime = sNowDateTime.substring(0, 8) + sDateTime +"01";
	                	iDataCount = 60;
	                }
	                break;
	            }
	            default: { //日月冻结数据
	            	iDataDensity =0;
	                iDataCount = 1;
	            }
            }
            dataTimeTag.setDataTime(sDateTime);
            dataTimeTag.setDataDensity(iDataDensity);
            dataTimeTag.setDataCount(iDataCount);
        } catch (Exception e) {
        	log.error("getTaskDateTimeInfo error："+e.toString());
        }
        return dataTimeTag;

    }
	/**
	 * 复合型数据格式拆分解析方法
	 * @param 数据值	例input=
	 * @param 数据格式	例format=N1#HTB1#BS2#BS4#A19#HTB1#A18#N1#A19
	 * @param 递归标志 默认false，进入递归置true
	 * @return 报文数据	return 第一层变长用";"间隔符，第二层用","
	 */
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
					}else if(formatItems[i].startsWith("L")){
						//变长数据
						DataValue nValue=new DataValue();
						nValue=parseValue(input,formatItems[i]);
						input=input.substring(nValue.getLen());						
						format=format.substring(format.indexOf("#")+1);	
						if((i+1)<formatItems.length){
							formatItems[i+1]=formatItems[i+1]+nValue.getValue();
						}
						value+=nValue.getValue()+"#";
						len+=nValue.getLen();
					}else{
						dataItemTemp=parseValue(input,formatItems[i]);
						input=input.substring(dataItemTemp.getLen());						
						format=format.substring(format.indexOf("#")+1);	
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
			log.error("coder error:"+e.toString());	        
		}
		return dataItem;
	}
	public static boolean isValidTime(String input,String inputFormat){
		boolean rt=false;
		if (input.length()==inputFormat.length()){
			try{
				SimpleDateFormat df = new SimpleDateFormat(inputFormat);	
				Date dt=df.parse(input);
				Calendar ct=Calendar.getInstance();
				ct.setTime(dt);
				if (ct.getTimeInMillis()>System.currentTimeMillis())
					return rt=false;
				else{
					df = new SimpleDateFormat("yyyy-MM-dd");
					String output=df.format(dt);
					if (input.equals(output))
						rt=true;
				}			
			}catch(Exception e){			
			}
		}
		return rt;
	}
	
}
