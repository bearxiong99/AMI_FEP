package com.hx.ansi.ansiElements.ansiElements.registerTable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import cn.hexing.fk.utils.HexDump;
import cn.hexing.fk.utils.StringUtil;

import com.hx.ansi.ansiElements.AnsiContext;
import com.hx.ansi.ansiElements.AnsiDataItem;
import com.hx.ansi.ansiElements.ansiElements.AnsiCommandResult;
import com.hx.ansi.ansiElements.ansiElements.Table;
import com.hx.ansi.parse.AnsiDataSwitch;

/** 
 * @Description  Table23--��ǰ������������
 * @author  Rolinbor
 * @Copyright 2013 hexing Inc. All rights reserved
 * @time��2013-3-25 ����04:11:17
 * @version 1.0 
 */

public class Table23 extends Table{
	
	private static final Logger log = Logger.getLogger(Table23.class);

	
	public int resetTime;
	public String TOT_DATA_BLOCK;
	public String TIER_DATA_BLOCK;
	public int PackageDL;//�������ĳ���
	public int PackageXL;//�������ĳ���
//    /** �����б� */
//    private List<RtuDataItem> dataList=new ArrayList<RtuDataItem>();
    public Map<String,String> summationsMap=new HashMap<String,String>();
//    public Map<String,String> demandsMap=new HashMap<String,String>();
	
	@Override
	public void decode() {
		
	}
	public void decode(AnsiContext context,String data){
		//28B7000069660000C2230100BF5000005A200000020A0000A61F000000000000020A0000B4000000000000000000000000000000000000000000000000000000000000000000000000000000
		int  count =getCount(context);//ÿһ�����������ݵĳ���
		int  countDL=count;//�������ݲ���ʱ�䣬�������ݳ���
		int  countXL=count+5+4;//�������ݴ���5���ֽ�ʱ��  
		int PackageDL=(context.table21.NBR_SUMMATIONS)*countDL;//�������ĳ���
		int PackageXL=(context.table21.NBR_DEMANDS)*countXL;//�������ĳ���
		String summationsData=data.substring(0, PackageDL*2);//��������
		String demandsData=data.substring(PackageDL*2);
		for(int i=0;i<summationsData.length()/8;i++){
			String sdata="";
			String value="";
			sdata=summationsData.substring(8*i, 8*i+8);//ȡ������
			double dresult=Long.parseLong(sdata, 16)/1.0;
			value=String.valueOf(dresult);
			value=AnsiDataSwitch.getDouble(value, 2);
			String code=context.table12.dataItemMap.get(context.table22.selectMap.get(i));
			this.summationsMap.put(code, value);
		}
		for(int i=0;i<demandsData.length()/26;i++){
			String sdata="";
			String value="";
			String value1="";
			String value2="";
			String sdate="";
			sdata=demandsData.substring(26*i, 26*i+26);//ȡ������,������5���ֽ�ʱ��+����
			SimpleDateFormat df = new SimpleDateFormat("yyMMddHHmm");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date=new Date();
			try{
				date=df.parse(AnsiDataSwitch.hexToString(sdata.substring(0,10)));
				sdate=sdf.format(date);
			}catch(ParseException e){
				log.error(StringUtil.getExceptionDetailInfo(e));
			}
			double dresult=Long.parseLong(sdata.substring(10,18), 16)/1.0;
			double accumulative=Long.parseLong(sdata.substring(18,26), 16)/1.0;
			value1=String.valueOf(dresult);
			value2=String.valueOf(accumulative);
			value1=AnsiDataSwitch.getDouble(value1, 2);
			value2=AnsiDataSwitch.getDouble(value2, 2);
			String code=context.table12.dataItemMap.get(context.table22.selectMap.get(context.table21.NBR_SUMMATIONS+i));
			if(sdate.equals("1999-11-30 00:00:00")){
				sdate="00-00-00 00:00:00";
			}
			value=sdate+"#"+value2+"#"+value1;
			this.summationsMap.put(code, value);
		}
		
	}
	
//	public void decode(AnsiContext context,String data,AnsiRequest request){
//		AnsiDataItem[] datas=request.getDataItem();
//		if(1==datas[0].dataType){//��������
//			String sdata="";
//			String value="";
//			for(int i=0;i<data.length()/8;i++){
//				//sdata="714D0000";
//				HostCommandResult hr=new HostCommandResult();
//				sdata=data.substring(8*i, 8*i+8);//ȡ������
//				double dresult=Integer.parseInt(sdata, 16)/1.0;
//				value=String.valueOf(dresult);
//				String code=context.table12.dataItemMap.get(context.table22.selectMap.get(i));
//				hr.setCode(code);
//				hr.setValue(value);
//				hr.setCommandId(request.getCommId());
//				this.value.add(hr);
//			}
//		}else{//��������
//			String sdata="";
//			String value="";
//			for(int i=0;i<data.length()/18;i++){
//				//sdata="714D0000";
//				HostCommandResult hr=new HostCommandResult();
//				sdata=data.substring(18*i, 18*i+18);//ȡ������
//				double dresult=Integer.parseInt(sdata, 16)/1.0;
//				value=String.valueOf(dresult);
//				String code=context.table12.dataItemMap.get(context.table22.selectMap.get(i));
//				hr.setCode(code);
//				hr.setValue(value);
//				hr.setCommandId(request.getCommId());
//				this.value.add(hr);
//			}
//		}
//
//		
//		
//	}
	@Override
	public void encode() {
		// TODO Auto-generated method stub
		
	}
//	@Override
//	public AnsiDataItem getResult(AnsiDataItem ansiDataItem,Table table) {
//		if(table instanceof Table23){
//			Table23 table23 = (Table23) table;
//			String []codes=ansiDataItem.dataCode.split("#");
//			String value="";
//			for(String code :codes){
//				if(table23.summationsMap.containsKey(code)){
//					ansiDataItem.resultData=table23.summationsMap.get(code);
//					value+=ansiDataItem.resultData+"#";
//				}else if(table23.demandsMap.containsKey(code)){
//					ansiDataItem.resultData=table23.demandsMap.get(code);
//					value+=ansiDataItem.resultData+"#";
//				}
//				else{
//					value+=" "+"#";
//					System.out.println("��Ʋ�֧�ֵĲ���");
//				}
//			}
//			ansiDataItem.resultData=value;
//		}else{
//			System.out.println("�����table����������23");
//		}
//		return ansiDataItem;
//	}
	@Override
	public AnsiDataItem getResult(AnsiDataItem ansiDataItem,Table table) {
		if(table instanceof Table23){
		Table23 table23 = (Table23) table;
		List<AnsiCommandResult> rt=new ArrayList<AnsiCommandResult>();
		Iterator itor=table23.summationsMap.entrySet().iterator();
			while(itor.hasNext()){
				Map.Entry<String,String> entry=(Map.Entry<String,String>)itor.next();
				AnsiCommandResult acr=new AnsiCommandResult();
				acr.setCode(entry.getKey().substring(0, 7)+ansiDataItem.tiers);
				acr.setValue(entry.getValue());
				rt.add(acr);
		}
		ansiDataItem.commandResult=rt;	
	}else{
		System.out.println("�����table����������23");
	}
	return ansiDataItem;
	}
	
	@Override
	public AnsiDataItem getIndex(AnsiDataItem ansiDataItem, Table table, AnsiContext context) {
		//��վ�·������������Ƕ�һ�������Ϊ��֪��֧�ֵķ���������һ�����ݰ�Ҳ�Ƿ���code�����Ƕ������
		//i=0��ʾ��ȡ�����ܣ���������
//		int i=Integer.parseInt(ansiDataItem.dataCode.substring(6, 8), 16);//��ȡҪ��ȡ���ǵڼ����ʵ�����
//		if(i>context.table21.NBR_TIERS){
//			System.out.println("��Ʋ�֧�ֵķ���");
//			//������� ��֧��
//		}
//		ansiDataItem.dataCode=ansiDataItem.dataCode.substring(0,6)+"00";
//		int icode=Integer.parseInt(ansiDataItem.dataCode, 16);
//		int key12=context.table12.dataItemMap.get(icode);//��table 12�л�ȡҪ��ȡ�����������
//		int key23=context.table22.selectMap.get(key12);//��ȡ������23���е�����,����������������23��֧�֣�����key23ӦΪnull
		int resetTimes=0;//������λ�������ֽ���
		if(context.table21.resetTimes){//�����table23���Ƿ����һ���ֽڵ�������λ����
			resetTimes=1;
		}
		String offSet="";
		String scount="";
		int  count =getCount(context);//ÿһ�����������ݵĳ���
		int  countDL=count;//�������ݲ���ʱ�䣬�������ݳ���
		int  countXL=count+5+4;//�������ݴ���5���ֽ�ʱ��
		int PackageDL=(context.table21.NBR_SUMMATIONS)*countDL;//�������ĳ���
		int PackageXL=(context.table21.NBR_DEMANDS)*countXL;//�������ĳ���
		int i=ansiDataItem.tiers;
		if(i>context.table21.NBR_TIERS){
			System.out.println("��Ʋ�֧�ֵķ���");
			//������� ��֧��
		}
		offSet=HexDump.toHex(resetTimes+i*PackageDL+i*PackageXL).substring(2);
		scount=HexDump.toHex(PackageDL+PackageXL).substring(4);
		ansiDataItem.length=PackageDL+PackageXL;
//		switch(ansiDataItem.dataType){//ѡ����������,��Ʋ�֧��
//		case 1 :
//			offSet=HexDump.toHex(resetTimes+i*PackageDL+i*PackageXL).substring(2);
//			scount=HexDump.toHex(PackageDL).substring(4);
//			ansiDataItem.length=PackageDL;
//			break;
//		case 2:
//			offSet=HexDump.toHex(resetTimes+(i+1)*PackageDL+i*PackageXL).substring(2);
//			scount=HexDump.toHex(PackageXL).substring(4);
//			ansiDataItem.length=PackageXL;
//			break;
//		}
		/*	��Ϊ��Ʋ�֧��С���ȡ���ݣ�������ֻ�ܽ����еĵ������ݶ�������
		 	if(key23<context.table21.NBR_SUMMATIONS){//��������
			//i*(countDL*context.table21.NBR_SUMMATIONS+countXL*context.table21.NBR_DEMANDS)Ϊ����ƫ��
			offSet=HexDump.toHex(resetTimes+i*(countDL*context.table21.NBR_SUMMATIONS+countXL*context.table21.NBR_DEMANDS)+key23*countDL).substring(2);//��ȡƫ����
			scount=HexDump.toHex(countDL).substring(4);
		}else {//��������
			//(context.table21.NBR_SUMMATIONS)*countDLΪ���������ֽ���
			offSet=HexDump.toHex(resetTimes+i*(countDL*context.table21.NBR_SUMMATIONS+countXL*context.table21.NBR_DEMANDS)+(context.table21.NBR_SUMMATIONS)*countDL+(key23-context.table21.NBR_SUMMATIONS)*countXL).substring(2);//��ȡƫ����
			scount=HexDump.toHex(countXL).substring(4);
		}
		 */
		ansiDataItem.offset=offSet;
		ansiDataItem.count=scount;
		return ansiDataItem;
	}
	/**
	 * ��ȡûһ���������ֽ���
	 * @param context
	 * @return
	 */
	public int getCount(AnsiContext context){
		int offSet=0;
		//table0����һЩ�������ͻ�����ȷ
		switch(context.table0.formatControl_3_NI_FMAT2){
		case 0:
			offSet=8;//FLOAT64
		case 1:
			offSet=4;//FLOAT32
		case 2:
			offSet=4;//FLOAT��CHAR12
		case 3:
			offSet=4;//FLOAT-CHAR6
		case 4:
			offSet=4;//INT32 /10000
		case 5:
			offSet=6;//BCD6
		case 6:
			offSet=4;//BCD4
		case 7:
			offSet=3;//INT24
		case 8:
			offSet=4;//INT32
		case 9:
			offSet=5;//INT40
		case 10:
			offSet=6;//INT48
		case 11:
			offSet=8;//BCD8
		case 12:
			offSet=4;//FLOAT-CHAR21
		default :
			offSet=4;//Ĭ��ƫ��4���ֽ�
		}
		return offSet;
	}
	
	
	public static void main(String[] args) { 
//		String s="71 4D 00 00 33 00 00 00 A4 4D 00 00 3E 4D 00 00 E1 09 00 00 14 00 00 00 71 06 00 00 14 00 00 00 00 00 00 00 70 03 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 2A 21 00 00 00 00 00 00 2A 21 00 00 2A 21 00 00 D4 03 00 00 00 00 00 00 DC 00 00 00 00 00 00 00 00 00 00 00 F8 02 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 12 02 00 00 14 00 00 00 26 02 00 00 FE 01 00 00 0A 00 00 00 00 00 00 00 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 75 12 00 00 0B 00 00 00 80 12 00 00 6A 12 00 00 3F 01 00 00 00 00 00 00 3F 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ";
//		s=s.replace(" ", "");
////		System.out.println(s.length()/10);
//		String ss="";
//		String sdl=s.substring(0, 80);//����4�ֽ�*10��������
//		String sxl=s.substring(80, 260);//������5�ֽ�ʱ��+4�ֽ����ݣ�*10
//		String sdl1=s.substring(260, 260+80);//����4�ֽ�*10��������
//		String sxl1=s.substring(260+80, 260+260);//������5�ֽ�ʱ��+4�ֽ����ݣ�*10
//		String sdl2=s.substring(520, 600);//����4�ֽ�*10��������
//		String sxl2=s.substring(600, 780);//������5�ֽ�ʱ��+4�ֽ����ݣ�*10
//		String sdl3=s.substring(780, 860);//����4�ֽ�*10��������
//		String sxl3=s.substring(860, 1040);//������5�ֽ�ʱ��+4�ֽ����ݣ�*10
//		String sdl4=s.substring(1040, 1120);//����4�ֽ�*10��������
//		String sxl4=s.substring(1120, 1300);//������5�ֽ�ʱ��+4�ֽ����ݣ�*10
//		for(int i=0;i<sdl1.length()/8;i++){
//			ss=sdl1.substring(8*i, 8*i+8);
//			System.out.println(ss);
//		}
//		System.out.println("--------------");
//		for(int i=0;i<sxl1.length()/18;i++){
//			ss=sxl1.substring(18*i, 18*i+18);
//			System.out.println(ss);
//		}
		
		String d="49590000";
		int i=Integer.parseInt(d, 16);
		System.out.println(i);
	}
}
