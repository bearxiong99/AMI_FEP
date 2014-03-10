package com.hx.ansi.ansiElements.ansiElements.basicTable;

import com.hx.ansi.ansiElements.AnsiDataItem;
import com.hx.ansi.ansiElements.ansiElements.Table;


/** 
 * @Description  Table 3 ״̬��
 * @author  Rolinbor
 * @Copyright 2013 hexing Inc. All rights reserved
 * @time��2013-3-19 ����02:29:15
 * @version 1.0 
 */

public class Table3 extends Table{

	public int ED_MODE_BFLD ;
	public boolean isFactory=false;//�Ƿ����ģʽ
	public boolean isStore=false;//�Ƿ�ִ�
	public boolean isTest=false;//����Ƿ��ڲ���ģʽ
	public boolean isWork=false;//����Ƿ��ڼ���ģʽ
	public int ED_STD_STATUS1;
	public boolean isReverse=false;//�Ƿ��⵽��ת
	public boolean isSteal=false;	//�Ƿ����Ե�״̬
	public boolean isPower_down=false;//�Ƿ��⵽����	
	public boolean isOverload=false;	//�Ƿ��⵽������
	public boolean isLowThreshold=false;	//�Ƿ��⵽ĳ��������ĳ���趨��ֵ
	public boolean isBattery_voltageLow=false;//�Ƿ��⵽���Ƿѹ	
	public boolean isMeasureError=false;	//�Ƿ��м�������
	public boolean isTimeError=false;//�Ƿ���ʱ�ӳ���
	public boolean isMemoryError=false;//�Ƿ��з���ʧ�Դ洢����
	public boolean isROMError=false;//ROM�Ƿ����
	public boolean isRAMError=false;//RAM�Ƿ����
	public boolean isSelfTestError=false;//�Լ����
	public boolean isConfigError=false;//���ô���
	public boolean isProgramError=false;//�Ƿ񱻱�̹�	0������̹���1��δ����̹������״̬
	public int ED_STD_STATUS2;//����
	public int ED_MFG_STATUS;//
	public String relayStatus="00";
	
	@Override
	public void decode() {
		isFactory=((ED_MODE_BFLD&8)>>>3)==0?false:true;
		isStore=((ED_MODE_BFLD&4)>>>2)==0?false:true;
		isTest=((ED_MODE_BFLD&2)>>>1)==0?false:true;
		isWork=(ED_MODE_BFLD&1)==0?false:true;
		isReverse=((ED_STD_STATUS1&8192)>>>13)==0?false:true;
		isSteal=((ED_STD_STATUS1&4096)>>>12)==0?false:true;
		isPower_down=((ED_STD_STATUS1&2048)>>>11)==0?false:true;
		isOverload=((ED_STD_STATUS1&1024)>>>10)==0?false:true;
		isLowThreshold=((ED_STD_STATUS1&512)>>>9)==0?false:true;
		isBattery_voltageLow=((ED_STD_STATUS1&256)>>>8)==0?false:true;
		isMeasureError=((ED_STD_STATUS1&128)>>>7)==0?false:true;
		isTimeError=((ED_STD_STATUS1&64)>>>6)==0?false:true;
		isMemoryError=((ED_STD_STATUS1&32)>>>5)==0?false:true;
		isROMError=((ED_STD_STATUS1&16)>>>4)==0?false:true;
		isRAMError=((ED_STD_STATUS1&8)>>>3)==0?false:true;
		isSelfTestError=((ED_STD_STATUS1&4)>>>2)==0?false:true;
		isConfigError=((ED_STD_STATUS1&2)>>>1)==0?false:true;
		isProgramError=(ED_STD_STATUS1&1)==0?false:true;
		relayStatus=(ED_STD_STATUS2&32)>>>5==1?"00":"01";//true duankai
		
	}
	@Override
	public void encode() {
		// TODO Auto-generated method stub
		
	}
	public void decode(String data){
		ED_MODE_BFLD=Integer.parseInt(data.substring(0, 2), 16);
		ED_STD_STATUS1=Integer.parseInt(data.substring(2, 6), 16);
		ED_STD_STATUS2=Integer.parseInt(data.substring(8, 10), 16);
		decode();
	}
	@Override
	public AnsiDataItem getResult(AnsiDataItem ansiDataItem,Table table) {
		if(table instanceof Table3){
			Table3 table3 = (Table3) table;
			int icode=Integer.parseInt(ansiDataItem.dataCode);
			switch(icode){
			case 20319://code:00010001
				ansiDataItem.resultData=table3.relayStatus;
				break;			
			}
		}else{
			System.out.println("�����table����");
		}
		return ansiDataItem;
	}
	public static void main(String[] args) {
		Table3 t=new Table3();
		t.decode("0100000020");
	}
	
	
}
