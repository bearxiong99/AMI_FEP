package com.hx.ansi.ansiElements.ansiElements.dataTable;

import cn.hexing.fk.utils.HexDump;

import com.hx.ansi.ansiElements.ansiElements.Table;
import com.hx.ansi.parse.AnsiDataSwitch;

/** 
 * @Description  	Table 11 ����Դ���Ʊ�
 * @author  Rolinbor
 * @Copyright 2013 hexing Inc. All rights reserved
 * @time��2013-3-19 ����03:28:02
 * @version 1.0 
 */
/**
 * Table 11 ����Դ���Ʊ�
 * ����Դ���Ʊ��ṩ������ص����û���������Ϣ��ֻ����
 */
public class Table11 extends Table{
	
	public int SOURCE_FLAGS;
	public boolean isCTPT=false;//�Ƿ���CT��PT
	public boolean isThermal=false;//�Ƿ�֧��thermalʽ��������
	public boolean isSlip=false;//�Ƿ�֧�ֻ���ʽ��������
	public boolean isInterval=false;//�Ƿ�֧������ʽ��������
	public boolean isReset=false;//������λ������ָ������λ��һ��ʱ���ڲ������ٴθ�λ��
								//0��	��Ʋ��߱���������
								//1��	��ƾ߱���������
	public boolean isPower_down=false;//�����������ʱ��ĳЩ�����������������һ��ʱ�䲻������������������ļ��㡣
									//0-��Ʋ��߱���������
									//1-��ƾ߱���������
	public int NBR_UOM_ENTRIES;//Table12 UOM_ENTRY_TBL�ڶ���Ԫ�صĸ���
	public int NBR_DEMAND_CTRL_ENTRIES=1;//Table13 DEMAND_CONTROL_TBL�ڶ���Ԫ�صĸ��������˱�Ϊ1��
	public int DATA_CTRL_LENGTH;//Table14 DATA_CONTROL_TBL��ÿ��Ԫ�ر����ֽڳ���
	public int NBR_CTRL_CTRL_ENTRIES;//Table14 DATA_CONTROL_TBL��Ԫ�صĸ���
	public int NBR_CONSTANTS_ENTRIES;//Table15 CONSTANTS_TBL��Ԫ�صĸ���
	public int CONSTANTS_SELECTOR=2;//����ѡ�������˴�ѡ��ELECTRIC_CONSTANTS_RCD=2��
	public int NBR_SOURCES;//Table16 SOURCES_TBL��Ԫ�صĸ���
	
	@Override
	public void decode() {
		isCTPT=((SOURCE_FLAGS&32)>>>5)==0?false:true;
		isThermal=((SOURCE_FLAGS&16)>>>4)==0?false:true;
		isSlip=((SOURCE_FLAGS&8)>>>3)==0?false:true;
		isInterval=((SOURCE_FLAGS&4)>>>2)==0?false:true;
		isReset=((SOURCE_FLAGS&2)>>>1)==0?false:true;
		isPower_down=(SOURCE_FLAGS&1)==0?false:true;
	}
	public void decode(String data){
		byte []b=new byte[1024];
		b=HexDump.toArray(data);
		SOURCE_FLAGS=AnsiDataSwitch.parseBytetoInt(b[0]);
		NBR_UOM_ENTRIES=AnsiDataSwitch.parseBytetoInt(b[1]);
		DATA_CTRL_LENGTH=AnsiDataSwitch.parseBytetoInt(b[3]);
		NBR_CTRL_CTRL_ENTRIES=AnsiDataSwitch.parseBytetoInt(b[4]);
		NBR_CONSTANTS_ENTRIES=AnsiDataSwitch.parseBytetoInt(b[5]);
		NBR_SOURCES=AnsiDataSwitch.parseBytetoInt(b[7]);
		decode();
	}
	
	
	@Override
	public void encode() {
		// TODO Auto-generated method stub
		
	}
	public static void main(String[] args) {
		Table11 t=new Table11();
		t.decode("2A2A0A000001022A");
	}
}
