package cn.hexing.fas.protocol.gw.parse;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.hexing.fas.protocol.zj.parse.Parser19;
import cn.hexing.fk.utils.HexDump;


/**
 * ���������ݱ���Ϊ�ֽ�����
 *
 */
public class DataItemCoder {
	private static Log log=LogFactory.getLog(DataItemCoder.class);
	/**
	 * ����������͵���������ϳ����ݵ�Ԫ��ʶ
	 * @param �������	��mt=10
	 * @param ���ݿ��ʶ	��code=04F001
	 * @return ���ݵ�Ԫ��ʶ	return 02020001
	 */
	public static String getCodeFrom1To1(int mt,String code){
	    String sDADT="",sDA="",sDT="";
		try{
			//������Ϣ��DA
			char[] chr1={'0','0','0','0','0','0','0','0'};
			if (mt==0)//������Ϊ0
				sDA="0000";
			else if (mt>0 && mt<=2040){
				if (mt % 8==0)
					chr1[0]='1';
				else
					chr1[8-mt % 8]='1';				
				sDA=DataSwitch.Fun8BinTo2Hex(new String(chr1).trim())+DataSwitch.IntToHex(""+((int)Math.floor((mt-1)/8)+1),2);				
			}
			//������Ϣ��DT
			char[] chr2={'0','0','0','0','0','0','0','0'};
			int fn=Integer.parseInt(code.substring(3,6));//���04F001��ȡ001
			if (fn>0 && fn<=2040){
				if (fn % 8==0)
					chr2[0]='1';
				else
					chr2[8-fn % 8]='1';	
				//��DA���������:DA(1~255);DT(0~254)
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
	 * ���������Ͷ��������ϳ����ݵ�Ԫ��ʶ
	 * @param �������	��mts=1,2
	 * @param ���ݿ��ʶ	��codes=04F001,04F002
	 * @return ���ݵ�Ԫ��ʶ	return 01030003
	 */
	@SuppressWarnings("unchecked")
	public static String[] getCodeFromNToN(int[] mts,String[] codes){
	    String[] sDADTList=null,sDAList=null,sDTList=null;
		try{
			Map<Integer,char[]> chrMap=new HashMap<Integer,char[]>();			
			char[] chr0={'0','0','0','0','0','0','0','0'};
			char[] chrFF={'1','1','1','1','1','1','1','1'};
			char[] chr;
			//������Ϣ��DA����
			for (int i=0;i<mts.length;i++){
				if (mts[i]==0){//������Ϊ0
					chr=chr0;
					chrMap.put(0, chr);
				}
				else if (mts[i]==9999){//����Լ��������ţ���ʾ���в�����
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
			//������Ϣ��DT����
			for (int i=0;i<codes.length;i++){
				int fn=Integer.parseInt(codes[i].substring(3,6));//���04F001��ȡ001
				if (fn>0 && fn<=2040){
					int iDT2=(int)Math.floor((fn-1)/8);//��DA���������:DA(1~255);DT(0~254)
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
	 * ���������ݸ�ʽ��ֱ��뷽��
	 * @param ����ֵ	��input=
	 * @param ���ݸ�ʽ	��format=N1#HTB1#BS2#BS4#A19#HTB1#A18#N1#A19
	 * @return ��������	return 
	 */
	public static String coder(String input,String format){
		String output="";
		try{
			String[] formatItems=format.split("#");
			String[] inputItems=input.split("#");
			if (formatItems.length>0){
				for (int i=0;i<formatItems.length;i++){
					if (formatItems[i].startsWith("N")||formatItems[i].startsWith("X")||formatItems[i].startsWith("M")||formatItems[i].startsWith("L")){//���ݸ�ʽ���ڱ䳤��ʽN��M
						int	num=0;
						if (!input.equals("0")){//��F33ʱ��������Ϊ0ʱ���⴦��		
							if (formatItems[i].startsWith("N")||formatItems[i].startsWith("X")||formatItems[i].startsWith("L"))
								num=Integer.parseInt(input.substring(0,input.indexOf("#")));//ȡ����Nֵ
							else
								num=ParserBS.getBSCount(input.substring(0,input.indexOf("#")));
							output=output+constructor(input.substring(0,input.indexOf("#")),formatItems[i]);
							format=format.substring(format.indexOf("#")+1);
							input=input.substring(input.indexOf("#")+1);
							
							if(formatItems[i].startsWith("L")){
								//�䳤��ʽ
								if(i+1<formatItems.length){
									formatItems[i+1]=formatItems[i+1]+""+num;
								}else{
									//error
								}
								continue;
							}
							
							
							if (num>1){//�ж��ʱ�ŵݹ����						
								if (formatItems[i].startsWith("X")){//���ݸ�ʽΪ���б䳤,�������䳤��ʽû�д�����ϵ
									inputItems=input.split(",");	
									for (int j=0;j<num;j++){
										output=output+coder(inputItems[j],format.substring(0,format.indexOf("#")));
										if (j==num-1)//���һ����Ҫ���⴦��
											input=input.substring(input.indexOf("#")+1);
										else
											input=input.substring(input.indexOf(",")+1);
									}
									format=format.substring(format.indexOf("#")+1);									
									i++;
								}else{	
									inputItems=input.split(";");	
									if (num!=inputItems.length){//�ݹ���÷ָ�������";"����","
										inputItems=input.split(",");								
									}
									for (int j=0;j<inputItems.length;j++){
										output=output+coder(inputItems[j],format);
									}
									break;
								}																
							}else if( num==0 && formatItems[i].startsWith("X")){
								//����Ǳ䳤X��ʽ���������Ϊ0����ôֱ��������һ����ʽ
								i++;
							}
						}
						else {
							output=output+constructor(input,formatItems[i]);
							break;
						}									
					}
					else{
						if (i==formatItems.length-1){//���һ��û��#
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
		return output;
	}
	/**
	 * ���ݸ�ʽ���뷽��
	 * @param ����ֵ		��input=10
	 * @param ���ݸ�ʽ	��format=HTB1
	 * @return ��������	return =0A
	 */
	public static String constructor(String input,String format){
		String output="";
		try{
			int len=0;			
			if(format.startsWith("HTB")){
				len=Integer.parseInt(format.substring(3));
				output=ParserHTB.constructor(input, len*2);
			}else if(format.startsWith("HEX")){
				len=Integer.parseInt(format.substring(3));
				output=ParserHEX.constructor(input, len*2);
			}else if(format.startsWith("STS")){
				len=Integer.parseInt(format.substring(3));
				output=ParserString.constructor(input, len*2);
			}else if(format.startsWith("ASC")){
				len=Integer.parseInt(format.substring(3));
				output=ParserASC.constructor(input, len*2);
			}else if(format.startsWith("SIM")){
				len=Integer.parseInt(format.substring(3));
				output=ParserSIM.constructor(input, len*2);
			}else if(format.startsWith("BS")){
				len=Integer.parseInt(format.substring(2));
				output=ParserBS.constructor(input, len*2);
			}else if(format.startsWith("IP")){
				len=Integer.parseInt(format.substring(2));
				output=ParserIP.constructor(input, len*2);
			}else if(format.startsWith("N")||format.startsWith("X") || format.startsWith("L")){
				len=Integer.parseInt(format.substring(1));
				output=ParserHTB.constructor(input, len*2);
			}else if(format.startsWith("M")){
				len=Integer.parseInt(format.substring(1));
				output=ParserBS.constructor(input, len*2);
			}else if(format.equals("A1")){//����������ʱ����
				output=ParserA1.constructor(input, 6*2);
			}else if(format.equals("A2")){//���ݲ��������ŵ���ֵ��
				output=ParserA2.constructor(input, 2*2);
			}else if(format.equals("A3")){//����λ�������ŵ���ֵ��
				output=ParserA3.constructor(input, 4*2);
			}else if(format.equals("A4")){//CC�������ŵĸ�����
				output=ParserFTB.constructor(input,"CC",1*2);
			}else if(format.equals("A5")){//CCC.C�������ŵĸ�����
				output=ParserFTB.constructor(input,"CCC.C",2*2);
			}else if(format.equals("A6")){//CC.CC�������ŵĸ�����
				output=ParserFTB.constructor(input,"CC.CC",2*2);
			}else if(format.equals("A7")){//000.0������
				output=ParserFTB.constructor(input,"000.0",2*2);
			}else if(format.equals("A8")){//0000������
				output=ParserFTB.constructor(input,"0000",2*2);
			}else if(format.equals("A9")){//CC.CCCC�������ŵĸ�����
				output=ParserFTB.constructor(input,"CC.CCCC",3*2);
			}else if(format.equals("A10")){//000000������
				output=ParserFTB.constructor(input,"000000",3*2);
			}else if(format.equals("A11")){//000000.00������
				output=ParserFTB.constructor(input,"000000.00",4*2);
			}else if(format.equals("A12")){//000000000000������
				output=ParserFTB.constructor(input,"000000000000",6*2);
			}else if(format.equals("A13")){//0000.0000������
				output=ParserFTB.constructor(input,"0000.0000",4*2);
			}else if(format.equals("A14")){//000000.0000������
				output=ParserFTB.constructor(input,"000000.0000",5*2);
			}else if(format.equals("A15")){//yyMMddHHmmʱ����
				output=ParserDATE.constructor(input,"yyyy-MM-dd HH:mm","yyMMddHHmm",5*2);
			}else if(format.equals("A16")){//ddHHmmssʱ����
				output=ParserDATE.constructor(input,"dd HH:mm:ss","ddHHmmss",4*2);
			}else if(format.equals("A17")){//MMddHHmmʱ����
				output=ParserDATE.constructor(input,"MM-dd HH:mm","MMddHHmm",4*2);
			}else if(format.equals("A18")){//ddHHmmʱ����
				output=ParserDATE.constructor(input,"dd HH:mm","ddHHmm",3*2);
			}else if(format.equals("A19")){//HHmmʱ����
				output=ParserDATE.constructor(input,"HH:mm","HHmm",2*2);
			}else if(format.equals("A20")){//yyMMddʱ����
				output=ParserDATE.constructor(input,"yyyy-MM-dd","yyMMdd",3*2);
			}else if(format.equals("A21")){//yyMMʱ����
				output=ParserDATE.constructor(input,"yyyy-MM","yyMM",2*2);
			}else if(format.equals("A22")){//0.0������
				output=ParserFTB.constructor(input,"0.0",1*2);
			}else if(format.equals("A23")){//00.0000������
				output=ParserFTB.constructor(input,"00.0000",3*2);
			}else if(format.equals("A24")){//ddHHʱ����
				output=ParserDATE.constructor(input,"dd HH","ddHH",2*2);
			}else if(format.equals("A25")){//CCC.CCC�������ŵĸ�����
				output=ParserFTB.constructor(input,"CCC.CCC",3*2);
			}else if(format.equals("A26")){//0.000������
				output=ParserFTB.constructor(input,"0.000",2*2);
			}else if(format.equals("A27")){//00000000������
				output=ParserFTB.constructor(input,"00000000",4*2);
			}else if(format.equals("A30")){//000.00000������
				output=ParserFTB.constructor(input,"000.00000",4*2);
			}else if(format.startsWith("RAW")){
				len=Integer.parseInt(format.substring(3));
				output = input.substring(0, len*2);
			}else if(format.equals("FTM")){
				len=7;
				byte[] data = new byte[7];
				Parser19.constructor(data, input, 0, 7, 0);
				output=  HexDump.toHex(data);
			}
		}
		catch(Exception e){
			log.error("constructor error:"+e.toString());	        
		}
		return output;
	}

	public static void main(String[] args) {
		//String rt=coder("111.111.111.111#111.111.111.111#111.123.123.123#0#111.123.123.123:12344#1#1#C#1#C#12345","IP4#IP4#IP4#HTB1#IP6#HTB1#X1#ASC1#X1#ASC1#HTB2");
		String rt=coder("5612","HTB16");
		System.out.println(rt);
	}
	
}
