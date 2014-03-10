package cn.hexing.fas.protocol.pos.parse;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.hexing.fas.protocol.gw.parse.DataSwitch;
import cn.hexing.fas.protocol.gw.parse.ParserASC;
import cn.hexing.fas.protocol.gw.parse.ParserBS;
import cn.hexing.fas.protocol.gw.parse.ParserHTB;
import cn.hexing.fas.protocol.pos.PosItemConfig;

public class DataItemCoder {

	private static Log log=LogFactory.getLog(DataItemCoder.class);
	/**
	 * 单个测量点和单个命令组合成数据单元标识
	 * @param 测量点号	例mt=10
	 * @param 数据块标识	例code=04F001
	 * @return 数据单元标识	return 02020001
	 */
	public static String getCodeFrom1To1(int mt,String code){
	    String sDADT="",sDA="",sDT="";
		try{
			//计算信息点DA
			char[] chr1={'0','0','0','0','0','0','0','0'};
			if (mt==0)//测量点为0
				sDA="0000";
			else if (mt>0 && mt<=2040){
				if (mt % 8==0)
					chr1[0]='1';
				else
					chr1[8-mt % 8]='1';				
				sDA=DataSwitch.Fun8BinTo2Hex(new String(chr1).trim())+DataSwitch.IntToHex(""+((int)Math.floor((mt-1)/8)+1),2);				
			}
			//计算信息类DT
			char[] chr2={'0','0','0','0','0','0','0','0'};
			int fn=Integer.parseInt(code.substring(3,6));//命令：04F001，取001
			if (fn>0 && fn<=2040){
				if (fn % 8==0)
					chr2[0]='1';
				else
					chr2[8-fn % 8]='1';	
				//跟DA是有区别的:DA(1~255);DT(0~254)
				sDT=DataSwitch.Fun8BinTo2Hex(new String(chr2).trim())+DataSwitch.IntToHex(""+(int)Math.floor((fn-1)/8),2);		    	
			 }
			 sDADT=sDA+sDT;
		       
		}
		catch(Exception e){
			log.error("getCodeFrom1To1 error:"+e.toString());	        
		}
		return sDADT;
	}
	/**
	 * 多个测量点和多个命令组合成数据单元标识
	 * @param 测量点号	例mts=1,2
	 * @param 数据块标识	例codes=04F001,04F002
	 * @return 数据单元标识	return 01030003
	 */
	@SuppressWarnings("unchecked")
	public static String[] getCodeFromNToN(int[] mts,String[] codes){
	    String[] sDADTList=null,sDAList=null,sDTList=null;
		try{
			Map<Integer,char[]> chrMap=new HashMap<Integer,char[]>();			
			char[] chr0={'0','0','0','0','0','0','0','0'};
			char[] chrFF={'1','1','1','1','1','1','1','1'};
			char[] chr;
			//计算信息点DA队列
			for (int i=0;i<mts.length;i++){
				if (mts[i]==0){//测量点为0
					chr=chr0;
					chrMap.put(0, chr);
				}
				else if (mts[i]==9999){//特殊约定测量点号，表示所有测量点
					chr=chrFF;
					chrMap.put(0, chr);
				}
				else if (mts[i]>0 && mts[i]<=2040){
					int iDA2=(int)Math.floor((mts[i]-1)/8)+1;
					chr=chrMap.get(new Integer(iDA2));
					if(chr==null){
						chr=chr0.clone();
					}
					if (mts[i] % 8==0)
						chr[0]='1';
					else
						chr[8-mts[i] % 8]='1';	
					chrMap.put(new Integer(iDA2), chr);						
				}
			}
			sDAList=new String[chrMap.size()];
			Iterator it=chrMap.entrySet().iterator();
			int icount=0;
			while(it.hasNext()){
				Map.Entry<Integer,char[]> entry=(Map.Entry<Integer,char[]>)it.next();
				sDAList[icount]=DataSwitch.Fun8BinTo2Hex(new String((char[])entry.getValue()).trim())+DataSwitch.IntToHex(""+entry.getKey(),2);
				icount++;
			}
			
			chrMap.clear();		
			//计算信息类DT队列
			for (int i=0;i<codes.length;i++){
				int fn=Integer.parseInt(codes[i].substring(3,6));//命令：04F001，取001
				if (fn>0 && fn<=2040){
					int iDT2=(int)Math.floor((fn-1)/8);//跟DA是有区别的:DA(1~255);DT(0~254)
					chr=chrMap.get(new Integer(iDT2));
					if(chr==null){
						chr=chr0.clone();
					}
					if (fn % 8==0)
						chr[0]='1';
					else
						chr[8-fn % 8]='1';			
					chrMap.put(new Integer(iDT2), chr);			    	
				 }
			}
			sDTList=new String[chrMap.size()];
			it=chrMap.entrySet().iterator();
			icount=0;
			while(it.hasNext()){
				Map.Entry<Integer,char[]> entry=(Map.Entry<Integer,char[]>)it.next();
				sDTList[icount]=DataSwitch.Fun8BinTo2Hex(new String((char[])entry.getValue()).trim())+DataSwitch.IntToHex(""+entry.getKey(),2);
				icount++;
			}
			sDADTList=new String[sDAList.length*sDTList.length];
			icount=0;
			for(int i=0;i<sDAList.length;i++){
				for(int j=0;j<sDTList.length;j++){
					sDADTList[icount]=sDAList[i]+sDTList[j];
					icount++;
				}
			}
		    
		}
		catch(Exception e){
			log.error("getCodeFromNToN error:"+e.toString());	        
		}
		return sDADTList;
	}
	
	/**
	 * 复合型数据格式拆分编码方法
	 * @param 数据值	例input=
	 * @param 数据格式	例format=N1#HTB1#BS2#BS4#A19#HTB1#A18#N1#A19
	 * @return 报文数据	return 
	 */
	public static String coder(String input,String format){
		String[] loop = format.split("!");
		String output="";
		int loopNum = 1;
		if(loop.length > 1 ){
			loopNum = Integer.parseInt(input.substring(0,input.indexOf("!")));
			output=output+constructor(input.substring(0,input.indexOf("!")),loop[0]);
			format=format.substring(format.indexOf("!")+1);
			input=input.substring(input.indexOf("!")+1);
		}
		String[] inputs = input.split("&");
		String rawFormat = format;

		for(int y=0;y<loopNum;y++){
		try{
			input = inputs[y];
			String[] formatItems=rawFormat.split("#");
			String[] inputItems=input.split("#");
			if (formatItems.length>0){
				for (int i=0;i<formatItems.length;i++){
					if (formatItems[i].startsWith("N")||formatItems[i].startsWith("X")||formatItems[i].startsWith("M")){//数据格式存在变长格式N和M
						int	num=0;
						if (!input.equals("0")){//对F33时段数可以为0时特殊处理		
							if (formatItems[i].startsWith("N")||formatItems[i].startsWith("X"))
								num=Integer.parseInt(input.substring(0,input.indexOf("#")));//取变量N值
							else
								num=ParserBS.getBSCount(input.substring(0,input.indexOf("#")));
							output=output+constructor(input.substring(0,input.indexOf("#")),formatItems[i]);
							format=format.substring(format.indexOf("#")+1);
							input=input.substring(input.indexOf("#")+1);	
							if (num>1){//有多个时才递归调用						
								if (formatItems[i].startsWith("X")){//数据格式为并行变长,即两个变长格式没有从属关系
									inputItems=input.split(",");	
									for (int j=0;j<num;j++){
										output=output+coder(inputItems[j],format.substring(0,format.indexOf("#")));
										if (j==num-1)//最后一个需要特殊处理
											input=input.substring(input.indexOf("#")+1);
										else
											input=input.substring(input.indexOf(",")+1);
									}
									format=format.substring(format.indexOf("#")+1);									
									i++;
								}else{	
									inputItems=input.split(";");	
									if (num!=inputItems.length){//递归调用分隔符不是";"就是","
										inputItems=input.split(",");								
									}
									for (int j=0;j<inputItems.length;j++){
										output=output+coder(inputItems[j],format);
									}
									break;
								}																
							}
						}
						else {
							output=output+constructor(input,formatItems[i]);
							break;
						}									
					}else if(formatItems[i].startsWith("CB")){//条件区域块
						//CB_HTB1_4_1
						String[] blockInfo=formatItems[i].split("_");
						String strIfType=blockInfo[1];  //当前条件类型
						String subId=blockInfo[2];  
						String index=blockInfo[3];
						int ifType=Integer.parseInt(input.substring(0,input.indexOf("#")));//取变量N值
						output=output+constructor(input.substring(0,input.indexOf("#")),strIfType);
						format=format.substring(format.indexOf("#")+1);
						input=input.substring(input.indexOf("#")+1);	
						
//						//取得传过来的条件
						String blockId = subId+"_"+index+"_"+ifType;
						String blockFormat=PosItemConfig.itemBlockMap.get(blockId);
						if(blockFormat!=null){
							String[] b = blockFormat.split("#");
							String[] v = input.split("#");
							for(int k=0;k<b.length;k++){
								String f = b[k];
								String val = v[k];
								if(f.contains("N1,")){
									f = f.replace(",", "#");
									val = val.replace("*", "#");
								}
								output = output+coder(val,f);
								input=input.substring(input.indexOf("#")+1);
							}
						}
						
					}
					else{
						if (i==formatItems.length-1){//最后一个没有#
							if (input.endsWith(",")||input.endsWith(";")||input.endsWith("#"))
								input=input.substring(0,input.length()-1);
							output=output+constructor(input,formatItems[i]);
						}
						else
							output=output+constructor(input.substring(0,input.indexOf("#")),formatItems[i]);
						format=format.substring(format.indexOf("#")+1);
						input=input.substring(input.indexOf("#")+1);
					}
				}
			}
		}
		catch(Exception e){
			log.error("coder error:"+e.toString());	        
		}
		
		}
		return output;
	}
	/**
	 * 数据格式编码方法
	 * @param 数据值		例input=10
	 * @param 数据格式	例format=HTB1
	 * @return 报文数据	return =0A
	 */
	public static String constructor(String input,String format){
		String output="";
		try{
			int len=0;	
			if(format.startsWith("BCD")){
				len = Integer.parseInt(format.substring(3));
				output=DataSwitch.StrStuff("0",len*2,input,"left");
			}else if(format.startsWith("HTB")){
				len=Integer.parseInt(format.substring(3));
				output=ParserHTB.constructor(input, len*2);
				output=DataSwitch.ReverseStringByByte(output);
			}else if(format.startsWith("ASC")){
				len=Integer.parseInt(format.substring(3));
				output=ParserASC.constructor(input, len*2);
			}else if(format.startsWith("N")){
				len=Integer.parseInt(format.substring(1));
				output=ParserHTB.constructor(input, len*2);
				output=DataSwitch.ReverseStringByByte(output);
			}else if(format.startsWith("CB")){
				output=coder(input, format);
			}					
		}
		catch(Exception e){
			log.error("constructor error:"+e.toString());	        
		}
		return output;
	}

	public static void main(String[] args) {
		//String rt=coder("111.111.111.111#111.111.111.111#111.123.123.123#0#111.123.123.123:12344#1#1#C#1#C#12345","IP4#IP4#IP4#HTB1#IP6#HTB1#X1#ASC1#X1#ASC1#HTB2");
	
		String str = coder("abc#efg#asd#1#sd#sd#asd#sd#1#1#1#1#1#1#1#1#abc#as#sd#sd#as#1#2#1#1#sd;1#1#sd","ASC40#ASC40#ASC80#BCD7#ASC15#ASC13#ASC13#ASC20#ASC13#HTB1#HTB1#BCD3#HTB1#HTB1#N1#CB_HTB1_4_1");
		System.out.println(str);
	}
	

}
