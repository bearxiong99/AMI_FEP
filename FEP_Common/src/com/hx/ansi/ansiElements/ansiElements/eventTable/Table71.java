package com.hx.ansi.ansiElements.ansiElements.eventTable;

import cn.hexing.fk.utils.HexDump;

import com.hx.ansi.ansiElements.ansiElements.Table;
import com.hx.ansi.parse.AnsiDataSwitch;

/** 
 * @Description  	Table 71 �¼�ʵ�����Ʊ�
 * @author  Rolinbor
 * @Copyright 2013 hexing Inc. All rights reserved
 * @version 1.0 
 */

public class Table71 extends Table {

	public int LOG_FLAGS_BFLD;
	public boolean eventInhibit;
	public boolean histInhibit;
	public boolean histSeqNum;
	public boolean histDatetime;
	public boolean eventNumber;
	public int NBR_STD_EVENTS;//�������table 72�б��֧�ֵı�׼�¼���EVENTS_SUPPORTED_TBL.STD_EVENTS_SUPPORTED�����ֽ���
	public int NBR_MFG_EVENTS;//�������table 72�б��֧�ֵĳ����Զ����¼���EVENTS_SUPPORTED_TBL.MFG_EVENTS_SUPPORTED�����ֽ���
	public int HIST_DATA_LENGTH;//History log���¼���¼���ݣ�HISTORY_LOG_DATA_TBL.HISTORY_ARGUMENT�����ֽ���
	public int EVENT_DATA_LENGTH;//Event log���¼���¼���ݣ�EVENT_LOG_DATA_TBL.EVENT_ARGUMENT�����ֽ���
	public int NBR_HISTORY_ENTRIES;//History log��¼�������������
	public int NBR_EVENT_ENTRIES ;//Event log��¼�������������
	public int EXT_LOG_FLAGS;
	public int NBR_PROGRAM_TABLES;
	
	
	@Override
	public void decode() {
		eventInhibit=((LOG_FLAGS_BFLD&16)>>4)==0?false:true;
		histInhibit=((LOG_FLAGS_BFLD&8)>>3)==0?false:true;
		histSeqNum=((LOG_FLAGS_BFLD&4)>>2)==0?false:true;
		histDatetime=((LOG_FLAGS_BFLD&2)>>1)==0?false:true;
		eventNumber=(LOG_FLAGS_BFLD&1)==0?false:true;
	}

	@Override
	public void encode() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void decode(String data) {
		//06 07 01 02 02 0A 00 0A 00
		//��׼������12���ֽڣ����Ǵӱ���������������9���ֽڣ�Ҳ���ǲ�����STD_VERSION_NO > 1����������3���ֽ�û�С�����table0���溣�˲��õ���STD_VERSION_NO=2
		//������������ΪӦ����12���ֽڣ������9���ֽ� ��ôҲҪ����9���ֽڴ���
		byte []b=new byte[1024];
		b=HexDump.toArray(data);
		LOG_FLAGS_BFLD=AnsiDataSwitch.parseBytetoInt(b[0]);
		NBR_STD_EVENTS=AnsiDataSwitch.parseBytetoInt(b[1]);
		NBR_MFG_EVENTS=AnsiDataSwitch.parseBytetoInt(b[2]);
		HIST_DATA_LENGTH=AnsiDataSwitch.parseBytetoInt(b[3]);
		EVENT_DATA_LENGTH=AnsiDataSwitch.parseBytetoInt(b[4]);
		NBR_HISTORY_ENTRIES=Integer.parseInt(data.substring(10, 14), 16);
		NBR_EVENT_ENTRIES=Integer.parseInt(data.substring(14, 18), 16);
//		EXT_LOG_FLAGS=AnsiDataSwitch.parseBytetoInt(b[9]);
//		NBR_PROGRAM_TABLES=Integer.parseInt(data.substring(20, 24), 16);
		decode();
	}
	public static void main(String[] args) {
		Table71 t71=new Table71();
		t71.decode("06070102020A000A00");
		
		
	}
}