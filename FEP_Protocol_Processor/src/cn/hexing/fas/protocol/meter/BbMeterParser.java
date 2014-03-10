package cn.hexing.fas.protocol.meter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.hexing.fas.protocol.Protocol;
import cn.hexing.fas.protocol.data.DataItem;
import cn.hexing.fas.protocol.gw.parse.DataSwitch;
import cn.hexing.fas.protocol.meter.conf.MeterProtocolDataItem;
import cn.hexing.fas.protocol.meter.conf.MeterProtocolDataSet;
import cn.hexing.fas.protocol.zj.parse.DataItemParser;
import cn.hexing.fas.protocol.zj.parse.ParseTool;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.utils.HexDump;

/**
 * @filename	BbMeterParser.java
 * TODO			������Լ
 * 				0x68---------------------֡ͷ��ʶ1
 * 				A0-----------------------��ַ����λ��
 * 				A1-----------------------��ַ
 * 				A2-----------------------��ַ
 * 				A3-----------------------��ַ
 * 				A4-----------------------��ַ
 * 				A5-----------------------��ַ����λ��
 * 				0x68---------------------֡ͷ��ʶ2
 * 				c------------------------������ bit7�����䷽��0-���� 1-Ӧ�� bit6���쳣��ʶ 0-���� 1-�쳣 
 * 											   bit5������֡��ʶ 0-��֡ 1-�к���֡
 *                                             bit0-bit4 ������
 *                                             00000 ��  ����
 *				                               00001 ��  ������
 *				                               00010 ��  ����������
 *				                               00011 ��  �ض�����
 *				                               00100 ��  д����
 *				                               01000 ��  �㲥Уʱ
 *				                               01010 ��  д�豸��ַ
 *				                               01100 ��  ����ͨѶ����
 *				                               01111 :   �޸�����
 *				                               10000 :   �����������
 *				L-------------------------�����򳤶�
 *				DATA----------------------������ �����ݲ�����200��д���ݲ�����50
 *				CS------------------------У�� ��֡ͷ��ʶ1��CSǰ�����ֽڵĺ�ģ256
 *				0x16----------------------֡β
 */
public class BbMeterParser implements IMeterParser{
	private final Log log=LogFactory.getLog(BbMeterParser.class);
	private MeterProtocolDataSet dataset;
	
	public BbMeterParser(){
		try{
			dataset=MeterProtocolFactory.createMeterProtocolDataSet("BBMeter");
		}catch(Exception e){
			log.error("������Լ��ʼ��ʧ��");
		}
	}
	
	public static void main(String[] args) {
		BbMeterParser bb = new BbMeterParser();
		DataItem item = new DataItem();
		item.addProperty("point", "1111");
		item.addProperty("params", "12");
		bb.constructor("D307", item, true);
		MeterProtocolDataItem s = bb.dataset.getDataItem("D307");
		String s1 = bb.dataset.getConvertCode("D307");
		System.out.println(s);
		String str = "6830000313200068E00235338016";
		bb.parser(HexDump.toArray(str), 0, str.length()/2,null);
	}
	
	/**
	 * ˵����������Լ�����ݱ�ʶ���㽭���Լ�����ݱ�ʶһ�£��㽭���Լ�е����ݼ���
	 *      ������Լ���ݼ���һ���Ӽ�
	 *      ���Ա�������ת�����ܼ�������Ҫ�Ǽ������ݱ�ʶ�ظ�
	 */
	public String[] convertDataKey(String[] datakey) {
		String[] rt=null;
		try{
			if(datakey!=null && datakey.length>0){//�����ݱ�ʶҪת��
				rt=new String[datakey.length];
				for(int i=0;i<datakey.length;i++){
					if((datakey[i]!=null) && datakey[i].equalsIgnoreCase("8902")){
						addDataKey(rt,"C034");
					}else{					
						addDataKey(rt,datakey[i]);
					}
				}
			}
		}catch(Exception e){
			log.error("��������ݱ�ʶת��",e);
		}
		return rt;
	}
	
	/**
	 * ����δ�������ݵ�key������
	 * @param datakeys
	 * @param dkey
	 * ˵�������ݱ�ʶΪXXXX�����֧���ٲ�XXFF���ݿ飬��֧��FFFF��XFFF����Ϊ����̫��
	 */
	private void addDataKey(String[] datakeys,String dkey){
		for(int i=0;i<datakeys.length;i++){
			if(datakeys[i]==null || datakeys[i].equals("")){//������δ����
				if(dkey.substring(0,1).equalsIgnoreCase("F") || dkey.substring(1,2).equalsIgnoreCase("F")){
					//���������ٲ�FFFF��XFFF
					break;
				}
				datakeys[i]=dkey;
				break;
			}
			String char1=datakeys[i].substring(0,1);
			String char2=datakeys[i].substring(1,2);
			String char3=datakeys[i].substring(2,3);
			if(char1.equalsIgnoreCase(dkey.substring(0,1))
				&& char2.equalsIgnoreCase(dkey.substring(1,2))
				&& char3.equalsIgnoreCase(dkey.substring(2,3))){//��һ�����ݿ�������
				
				StringBuffer sb=new StringBuffer();
				sb.append(char1);
				sb.append(char2);
				//С���ݿ�
				sb.append(char3);
				sb.append("F");
				
				datakeys[i]=sb.toString();
				sb=null;
				break;
			}
		}
	}
	
	//use for dlms realy only 97 protocol,create by gaoll at 2013��5��14��11:17:55
	public byte[] constructor(String dataKey,DataItem params,boolean isSet){
		byte[] frame = null;
		String meterAddr = (String)params.getProperty("point");
		String value = (String)params.getProperty("params");
		if(isSet){
			
			MeterProtocolDataItem meterDataItem = this.dataset.getDataItem(dataKey);
			byte[] dataArea = new byte[meterDataItem.getLength()];
			DataItemCoder.coder(dataArea, 0, value,meterDataItem.getType(), meterDataItem.getLength(),meterDataItem.getFraction());
			frame = new byte[14+dataArea.length+4]; //4���ֽ�����
			frame[0]=0x68;
			ParseTool.HexsToBytesAA(frame,1,meterAddr,6,(byte)0xAA);				
			frame[7]=0x68;
			frame[8]=0x04;
			frame[9]=(byte) (2+4+dataArea.length);
			ParseTool.HexsToBytes(frame,10,dataKey);
			frame[10]=(byte)(frame[10]+0x33);
			frame[11]=(byte)(frame[11]+0x33);

			ParseTool.HexsToBytes(frame,12,"33333333");

			for(int i=0;i<dataArea.length;i++){ //Ҫ��Ҫ��ת�ֽ�?
				frame[16+i]=(byte)(dataArea[i]+0x33);
			}

			frame[frame.length-2]=ParseTool.calculateCS(frame,0,frame.length-2);	//cs
			frame[frame.length-1]=0x16;	//cs
		}else{
			frame=new byte[14];
			frame[0]=0x68;
			ParseTool.HexsToBytesAA(frame,1,meterAddr,6,(byte)0xAA);				
			frame[7]=0x68;
			frame[8]=0x01;
			frame[9]=0x02;
			ParseTool.HexsToBytes(frame,10,dataKey);
			frame[10]=(byte)(frame[10]+0x33);
			frame[11]=(byte)(frame[11]+0x33);
			frame[12]=ParseTool.calculateCS(frame,0,12);	//cs
			frame[13]=0x16;	//cs
		}
		return frame;
	}
	
	/**
	 * datakeyֻ���ܵ�һ�����ݱ�ʶ����֧��������֡
	 */
	public byte[] constructor(String[] datakey, DataItem para) {
		byte[] frame=null;
		try{
			if((datakey!=null)&&(datakey.length>0)
					&&(para!=null)&&(para.getProperty("point")!=null)){	//check para
				if (datakey[0].length()==4){//97��ȫ������Լ
					String dkey=datakey[0];
					String value=(String)para.getProperty("write");//����
					String maddr=(String)para.getProperty("point");//���ַ
					if(null==value){//������������Ϊ�Ƕ���������
						frame=new byte[14];
						frame[0]=0x68;
						ParseTool.HexsToBytesAA(frame,1,maddr,6,(byte)0xAA);				
						frame[7]=0x68;
						frame[8]=0x01;
						frame[9]=0x02;
						ParseTool.HexsToBytes(frame,10,dkey);
						frame[10]=(byte)(frame[10]+0x33);
						frame[11]=(byte)(frame[11]+0x33);
						frame[12]=ParseTool.calculateCS(frame,0,12);	//cs
						frame[13]=0x16;	//cs
					}else{
						if(dkey.equals("EE01")||dkey.equals("EE02")||dkey.equals("EE04")){
							//�̵�����������բ ���� �˳�����
							frame=new byte[18];
							frame[0]=0x68;
							ParseTool.HexsToBytesAA(frame,1,maddr,6,(byte)0xAA);				
							frame[7]=0x68;
							frame[8]=0x04;
							frame[9]=0x06;
							ParseTool.HexsToBytes(frame,10,dkey);
							frame[10]=(byte)(frame[10]+0x33);
							frame[11]=(byte)(frame[11]+0x33);
							ParseTool.HexsToBytes(frame,12,"33333333");
							frame[16]=ParseTool.calculateCS(frame,0,16);	//cs
							frame[17]=0x16;	//cs	
						}
						else if(dkey.equals("EE09")){//��բ
							frame=new byte[20];
							frame[0]=0x68;
							ParseTool.HexsToBytesAA(frame,1,maddr,6,(byte)0xAA);				
							frame[7]=0x68;
							frame[8]=0x04;
							frame[9]=0x08;
							ParseTool.HexsToBytes(frame,10,dkey);
							frame[10]=(byte)(frame[10]+0x33);
							frame[11]=(byte)(frame[11]+0x33);
							ParseTool.HexsToBytes(frame,12,"33333333");
							frame[16]=0x09;
							frame[17]=0x09;
							frame[16]=(byte)(frame[16]+0x33);
							frame[17]=(byte)(frame[17]+0x33);
							frame[18]=ParseTool.calculateCS(frame,0,18);	//cs
							frame[19]=0x16;	//cs	
						}
						else if(dkey.equals("EA20")){//��ʱ
							frame=new byte[25];
							frame[0]=0x68;
							ParseTool.HexsToBytesAA(frame,1,maddr,6,(byte)0xAA);				
							frame[7]=0x68;
							frame[8]=0x04;
							frame[9]=0x0D;
							ParseTool.HexsToBytes(frame,10,dkey);
							frame[10]=(byte)(frame[10]+0x33);
							frame[11]=(byte)(frame[11]+0x33);
							ParseTool.HexsToBytes(frame,12,"33333333");
							SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							Date date=null;
							date=sdf.parse(value);
							SimpleDateFormat df = new SimpleDateFormat("yyMMddHHmmss");
							value=df.format(date);
							ParseTool.HexsToBytes(frame,16,"06"+value);
							for(int i=16;i<23;i++){//Ϊ���ݼ���33
								frame[i]=(byte)(frame[i]+0x33);
							}
							frame[23]=ParseTool.calculateCS(frame,0,23);	//cs
							frame[24]=0x16;	//cs
						}
						else if(dkey.equals("EE20")){//��ֵtoken�·�
							//68 76 55 74 20 41 01 68 04 10 53 21 33 33 33 33 B7 A8 CC 83 34 33 33 33 33 33 A6 16
							frame=new byte[28];
							frame[0]=0x68;
							ParseTool.HexsToBytesAA(frame,1,maddr,6,(byte)0xAA);				
							frame[7]=0x68;
							frame[8]=0x04;
							frame[9]=0x10;
							ParseTool.HexsToBytes(frame,10,dkey);
							frame[10]=(byte)(frame[10]+0x33);
							frame[11]=(byte)(frame[11]+0x33);
							ParseTool.HexsToBytes(frame,12,"33333333");
							ParseTool.HexsToBytes(frame,16,value);
							for(int i=16;i<26;i++){//Ϊ���ݼ���33
								frame[i]=(byte)(frame[i]+0x33);
							}
							frame[26]=ParseTool.calculateCS(frame,0,26);	//cs
							frame[27]=0x16;	//cs
						}
					}

				}else{//07��ȫ������Լ
					String dkey=datakey[0];
					String maddr=(String)para.getProperty("point");//���ַ	
					String value=(String)para.getProperty("write");
					if(dkey.equals("070000FF")){//�����֤
						frame=new byte[44];
						frame[0]=0x68;
						ParseTool.HexsToBytesAA(frame,1,maddr,6,(byte)0xAA);				
						frame[7]=0x68;	
						String identify=(String)para.getProperty("identify");
						frame[8]=0x03;
						frame[9]=0x20;						
						ParseTool.HexsToBytes(frame,10,dkey);//��ʶ
						ParseTool.HexsToBytes(frame,14,"00000000");//�����ߴ���
						ParseTool.HexsToBytes(frame,18,identify.substring(0,16));//����
						ParseTool.HexsToBytes(frame,26,identify.substring(16,32));//�����
						ParseTool.HexsToBytes(frame,34,identify.substring(32,48));//����
						for (int i=10;i<42;i++)
							frame[i]=(byte)(frame[i]+0x33);
						frame[42]=ParseTool.calculateCS(frame,0,42);	//cs
						frame[43]=0x16;	//cs
					}else if(dkey.equals("0800000100")){ // дTOKEN�·�
						frame = new byte[40];
						frame[0] = 0x68;
						ParseTool.HexsToBytesAA(frame,1,maddr,6,(byte)0xAA);				
						frame[7]=0x68;	
						frame[8]=0x20; //��ʶдTOKEN
						frame[9]=0x1C;
						ParseTool.HexsToBytes(frame, 10, "02");//����Ȩ��
						ParseTool.HexsToBytes(frame, 11, "000000");//3���ֽ�����
						ParseTool.HexsToBytes(frame, 14, "00000000");//4���ֽڲ����ߴ���
						ParseTool.HexsToBytes(frame, 18, DataSwitch.ReverseStringByByte(HexDump.toHex(value.getBytes())));//20���ֽ�TOKEN��
						for( int i=10;i<38;i++){
							frame[i]=(byte)(frame[i]+0x33);
						}
						frame[38] = ParseTool.calculateCS(frame, 0, 38);
						frame[39]=0x16;
					}else if(dkey.equals("0700000100")){ //����������,����Ҳ��һ��0700000100��Ŀǰ���滻��
						frame = new byte[28];
						frame[0] = 0x68;
						ParseTool.HexsToBytesAA(frame,1,maddr,6,(byte)0xAA);				
						frame[7]=0x68;	
						frame[8]=0x1C;
						frame[9]=0x10;
						ParseTool.HexsToBytes(frame, 10, "02");//����Ȩ��
						ParseTool.HexsToBytes(frame, 11, "000000");//3���ֽ�����
						ParseTool.HexsToBytes(frame, 14, "00000000");//4���ֽڲ����ߴ���
						String[] values = value.split("#"); //
						ParseTool.HexsToBytes(frame, 18, values[0]);
						ParseTool.HexsToBytes(frame, 19, "00");
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						Date date = sdf.parse(values[1]);
						sdf = new SimpleDateFormat("yyMMddHHmmss");
						ParseTool.HexsToBytes(frame,20,sdf.format(date));
						for(int i=10;i<26;i++){
							frame[i]=(byte)(frame[i]+0x33);
						}
						frame[26] = ParseTool.calculateCS(frame, 0, 26);
						frame[27] = 0x16;
						
					}else if(dkey.equals("070204FF")||dkey.equals("070201FF")||dkey.equals("070202FF")||dkey.equals("070203FF")){//��Կ����
						frame=new byte[60];
						frame[0]=0x68;
						ParseTool.HexsToBytesAA(frame,1,maddr,6,(byte)0xAA);				
						frame[7]=0x68;	
						String keyUpdate=(String)para.getProperty("keyUpdate");
						frame[8]=0x03;
						frame[9]=0x30;						
						ParseTool.HexsToBytes(frame,10,dkey);//��ʶ
						ParseTool.HexsToBytes(frame,14,"00000000");//�����ߴ���
						ParseTool.HexsToBytes(frame,18,keyUpdate.substring(0,16));//��Կ��Ϣ+MAC
						ParseTool.HexsToBytes(frame,26,keyUpdate.substring(16,80));//���������ļ���·������Կ
						for (int i=10;i<58;i++)
							frame[i]=(byte)(frame[i]+0x33);
						frame[58]=ParseTool.calculateCS(frame,0,58);	//cs
						frame[59]=0x16;	//cs
					}
					else if (dkey.equals("0700000100")){//����������
						frame=new byte[40];
						frame[0]=0x68;
						ParseTool.HexsToBytesAA(frame,1,maddr,6,(byte)0xAA);				
						frame[7]=0x68;	
						String userControl=(String)para.getProperty("userControl");
						frame[8]=0x1C;
						frame[9]=0x1C;
						ParseTool.HexsToBytes(frame,10,"02");//Ȩ��
						ParseTool.HexsToBytes(frame,11,"000000");//����
						ParseTool.HexsToBytes(frame,14,"00000000");//�����ߴ���
						ParseTool.HexsToBytes(frame,18,userControl);//����
						for (int i=10;i<38;i++)
							frame[i]=(byte)(frame[i]+0x33);
						frame[38]=ParseTool.calculateCS(frame,0,38);	//cs
						frame[39]=0x16;	//cs
					}else if(dkey.equals("04000103")){//�㲥��ʱ
						frame=new byte[18];
						frame[0]=0x68;
						ParseTool.HexsToBytesAA(frame,1,maddr,6,(byte)0xAA);
						frame[7]=0x68;
						String time =(String) para.getProperty("time");
						frame[8]=0x08;
						frame[9]=0x06;
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						Date date = sdf.parse(time);
						sdf = new SimpleDateFormat("yyMMddHHmmss");
						time=sdf.format(date);
						ParseTool.HexsToBytesAA(frame,10,time,6,(byte)0xAA);	
						frame[16]=ParseTool.calculateCS(frame, 0, 16);
						frame[17]=0x16;
					}else if(value!=null ){
						if(dkey.equals("04000101")){
							frame=new byte[28];
							frame[0]=0x68;
							ParseTool.HexsToBytesAA(frame,1,maddr,4,(byte)0xAA);
							frame[7]=0x68;
							frame[8]=0x14;
							frame[9]=16;
							ParseTool.HexsToBytes(frame, 10, dkey); //id
							ParseTool.HexsToBytes(frame, 14, "02");
							ParseTool.HexsToBytes(frame, 15, "000000");//pas
							ParseTool.HexsToBytes(frame, 18, "00000000");//pas
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							Date date = sdf.parse(value);
							sdf = new SimpleDateFormat("yyMMdd");
							Calendar c = Calendar.getInstance();
							c.setTime(date);
							value=sdf.format(date)+"0"+(c.get(Calendar.DAY_OF_WEEK)-1);
							ParseTool.HexsToBytes(frame, 22, value);//pas
							for(int i=10;i<10+16;i++){
								frame[i] = (byte) (frame[i]+0x33);
							}
							frame[26]=ParseTool.calculateCS(frame, 0, 26);
							frame[27]=0x16;
						}else if (dkey.equals("04000102")){
							frame=new byte[27];
							frame[0]=0x68;
							ParseTool.HexsToBytesAA(frame,1,maddr,4,(byte)0xAA);
							frame[7]=0x68;
							frame[8]=0x14;
							frame[9]=15;
							ParseTool.HexsToBytes(frame, 10, dkey); //id
							ParseTool.HexsToBytes(frame, 14, "02");
							ParseTool.HexsToBytes(frame, 15, "000000");//pas
							ParseTool.HexsToBytes(frame, 18, "00000000");//pas
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							Date date = sdf.parse(value);
							sdf = new SimpleDateFormat("HHmmss");
							Calendar c = Calendar.getInstance();
							c.setTime(date);
							value=sdf.format(c.getTime());
							ParseTool.HexsToBytes(frame, 22, value);//pas
							for(int i=10;i<10+15;i++){
								frame[i] = (byte) (frame[i]+0x33);
							}
							frame[25]=ParseTool.calculateCS(frame, 0, 25);
							frame[26]=0x16;
						}
					}
					else{
						frame=new byte[16];
						frame[0]=0x68;
						ParseTool.HexsToBytesAA(frame,1,maddr,6,(byte)0xAA);				
						frame[7]=0x68;	
						frame[8]=0x11;
						frame[9]=0x04;
						ParseTool.HexsToBytes(frame,10,dkey);
						frame[10]=(byte)(frame[10]+0x33);
						frame[11]=(byte)(frame[11]+0x33);
						frame[12]=(byte)(frame[12]+0x33);
						frame[13]=(byte)(frame[13]+0x33);
						frame[14]=ParseTool.calculateCS(frame,0,14);	//cs
						frame[15]=0x16;	//cs
					}
				}
				
			}
		}catch(Exception e){
			
		}
		return frame;
	}

	public Object[] parser(byte[] data, int loc, int len,BizRtu rtu) {
		List result=null;
		try{
			BbMeterFrame frame=new BbMeterFrame();
			frame.parse(data,loc,len);
			if(frame.getDatalen()>=0){	//�����а�����֡��֡�а�������
				result=new ArrayList();
				//��ȡ��֡����
				int datalen=frame.getDatalen();
				String meteraddr=frame.getMeteraddr();	//���ַ
				DataItem ma=new DataItem();
				ma.addProperty("value",meteraddr);
				ma.addProperty("datakey","8902");
				result.add(ma);
				
				int ctrl=frame.getCtrl();	/*������*/
				if((ctrl&0x10)==0x10||(ctrl&0x83)==0x83){//07��ȫ������Լ��ȡ���ؿ�����
					byte[] framedata=frame.getData();					
					int pos=frame.getPos();
					switch(ctrl & 0x0F){
						case 1:	//�����ݷ���
						case 3:	//�����֤����
							if ((ctrl&0xC3)==0xC3){//�����֤ʧ��
								ma=new DataItem();
								ma.addProperty("value",ParseTool.BytesToHexC(framedata,pos,2));
								ma.addProperty("datakey","07000000FF");
								result.add(ma);
							}
							else{
								String datakey=ParseTool.BytesToHexC(framedata,pos,4);
								datakey=dataset.getConvertCode(datakey);//ͨ����Լ���������ڲ�����
								MeterProtocolDataItem item=dataset.getDataItem(datakey);
								pos+=4;
								if(item!=null){//֧�ֵ����ݱ�ʶ
									parseValues(framedata,pos,item,result,Protocol.BBMeter07);
								}
							}						
							break;

						case 12:	//���������
							if ((ctrl&0xDC)==0xDC){//���������쳣����
								ma=new DataItem();
								ma.addProperty("value",ParseTool.BytesToHexC(framedata,pos,1));
								ma.addProperty("datakey","0700000100");
								result.add(ma);
							}
							else{//��������ɹ�����							
								ma=new DataItem();
								ma.addProperty("value","00");
								ma.addProperty("datakey","0700000100");
								result.add(ma);
							}
							
							break;
						case 4: //���÷���
							String res ="";
							if(frame.getDatalen()==0){
								//�ɹ�
								res = "00";
							}else{
								res = ParseTool.BytesToHex(framedata, pos, 1);
							}
							//���û�ò���datakey
							ma=new DataItem();
							ma.addProperty("value", res);
							ma.addProperty("datakey", "0500000701");
							result.add(ma);
							break;
						default:
							break;
					}
					
				}else if((ctrl&0x20)==0x20){//TOKENд����
					byte[] framedata = frame.getData();
					int pos = frame.getPos();
					ma = new DataItem();
					ma.addProperty("datakey", "0800000100");
					String res = "";
					if(ctrl==(byte)0xE0){//TOKENд�����쳣
						res = ""+Integer.parseInt(ParseTool.BytesToHexC(framedata, pos, 2),16);
					}else{
						if(frame.getLen()==12){ //������ֵ����
							res=""+ParseTool.BytesToHexC(framedata, pos-1, 1);
						}else{//ע�����ش���TOKEN��
							byte[] token = new byte[20];
							System.arraycopy(framedata, pos,token, 0,20);
							res="0:"+new String(token);
						}
					}
					ma.addProperty("value", res);
					result.add(ma);
					
				}else if((ctrl & 0x40)<=0){//����Ӧ��
					byte[] framedata=frame.getData();
					int pos=frame.getPos();
					String datakey=ParseTool.BytesToHexC(framedata,pos,2);
					MeterProtocolDataItem item=null;
					switch(ctrl & 0x1F){
						case 1:	//������
							datakey=ParseTool.BytesToHexC(framedata,pos,2);
							datakey=dataset.getConvertCode(datakey);//ͨ����Լ���������ڲ�����
							item=dataset.getDataItem(datakey);
							pos+=2;
							if(item!=null){//֧�ֵ����ݱ�ʶ
								parseValues(framedata,pos,item,result,Protocol.BBMeter97);
							}
							break;
						case 4://97��Լд���ݷ���
							datakey=dataset.getConvertCode(datakey);//ͨ����Լ���������ڲ�����
							item=dataset.getDataItem(datakey);
							DataItem ma2=new DataItem();
							if(datakey.equals("0700001600")){
								ma2.addProperty("value",framedata[12]);
							}else{
								ma2.addProperty("value","00");
							}
							ma2.addProperty("datakey",datakey);
							result.add(ma2);
							break;
						default:
							break;
					}
				}
				else{//�쳣Ӧ��
					byte[] framedata=frame.getData();
					int pos=frame.getPos();
					String datakey=ParseTool.BytesToHexC(framedata,pos,2);
					MeterProtocolDataItem item=null;
					switch(ctrl & 0x1F){
						case 1:	//������
							datakey=ParseTool.BytesToHexC(framedata,pos,2);
							datakey=dataset.getConvertCode(datakey);//ͨ����Լ���������ڲ�����
							item=dataset.getDataItem(datakey);
							DataItem ma1=new DataItem();
							ma1.addProperty("value","");
							ma1.addProperty("datakey",datakey);
							result.add(ma1);
							break;
						case 4://97��Լд���ݷ���
							datakey=(String) rtu.getParamFromMap(9999);//ͨ����Լ���������ڲ�����
							datakey=dataset.getConvertCode(datakey);//ͨ����Լ���������ڲ�����
							item=dataset.getDataItem(datakey);
							DataItem ma2=new DataItem();
							if(datakey.equals("0700001600")){
								ma2.addProperty("value","FF");//ʧ��FF
							}else{
								ma2.addProperty("value","01");
							}
							ma2.addProperty("datakey",datakey);
							result.add(ma2);
							break;
						default:
							break;
					}
				
				}
			}
		}catch(Exception e){
			log.error("������Լ",e);
		}
		if(result!=null){
			return result.toArray();
		}
		return null;
	}
	public Object[] parser(String key ,String data,String meteraddr) {
		List result=null;
		try{			
			result=new ArrayList();
			DataItem ma=new DataItem();
			ma.addProperty("value",meteraddr);
			ma.addProperty("datakey","8902");
			result.add(ma);
						
			key=dataset.getConvertCode(key);//ͨ����Լ���������ڲ�����
			MeterProtocolDataItem item=dataset.getDataItem(key);
			if(item!=null){//֧�ֵ����ݱ�ʶ
				parseValues(HexDump.toByteBuffer(data).array(),0,item,result,"");
			}
										
		}catch(Exception e){
			log.error("������Լ",e);
		}
		if(result!=null){
			return result.toArray();
		}
		return null;
	}
	
	
	/**
	 * ͨ���ڲ��������ʶ��ȡ07��ȫ����Լ���������ʶ
	 * @param codes		�ڲ��������ʶ 		��0100100000
	 * @param results	��Ӧ���Լ���������ʶ	��00010000
	 */
	public String[] getMeter2Code(String[] codes){
		String[] rtCodes=null;
		if(codes!=null&&codes.length>0){
			rtCodes=new String[codes.length];
			for(int i=0;i<codes.length;i++){
				MeterProtocolDataItem item=dataset.getDataItem(codes[i]);
				rtCodes[i]=item.getParentCode2();
			}
		}
		
		return rtCodes;
	}
	/**
	 * ͨ���ڲ��������ʶ��ȡ97��ȫ����Լ���������ʶ
	 * @param codes		�ڲ��������ʶ 		��0100100000
	 * @param results	��Ӧ���Լ���������ʶ	��9010
	 */
	public String[] getMeter1Code(String[] codes){
		String[] rtCodes=null;
		if(codes!=null&&codes.length>0){
			rtCodes=new String[codes.length];
			for(int i=0;i<codes.length;i++){
				MeterProtocolDataItem item=dataset.getDataItem(codes[i]);
				rtCodes[i]=item.getParentCode1();
			}
		}
		
		return rtCodes;
	}
	/**
	 * ����������
	 * @param data		��֡
	 * @param pos		��ǰ������ʼλ��
	 * @param item		��ǰ������������
	 * @param results	�����
	 */
	private int parseValues(byte[] data,int pos,MeterProtocolDataItem item,List results,String protocol){
		int rt=0;
		try{
			int loc=pos;
			if(item.getChildarray()!=null && item.getChildarray().size()>0){
				List children=item.getChildarray();
				for(int i=0;i<children.size();i++){
					if((data[loc] & 0xFF)==BbMeterFrame.FLAG_BLOCK_DATA){//���ݿ����
						rt+=1;
						break;
					}
					if(loc>=data.length){//�����ݿ��Խ�����Ҳû�������������������Ӧ���Ǵ��������Ⱥ���
						break;
					}
					int vlen=parseValues(data,loc,(MeterProtocolDataItem)children.get(i),results,protocol);
					if(vlen<=0){
						rt=0;
						break;
					}
					loc+=vlen;
					rt+=vlen;
				}
			}else{
				DataItem di=new DataItem();
				di.addProperty("datakey",item.getCode());
				Object val=parseItem(data,pos,item,protocol);
				di.addProperty("value",val);
				results.add(di);
				if (protocol.equals(Protocol.BBMeter07)){//07�����Լ�������ʽ��97�治һ�µ�ʱ��ȡ����	
					if (item.getLength2()>=0&&item.getLength2()!=item.getLength())
						rt=item.getLength2();	
					else
						rt=item.getLength();
				}
				else
					rt=item.getLength();
			}
		}catch(Exception e){
			rt=0;
			log.error("�������������",e);
		}
		return rt;
	}
	
	/**
	 * ������������
	 * @param frame
	 * @param loc
	 * @param mpd
	 * @return
	 */
	private Object parseItem(byte[] frame,int loc,MeterProtocolDataItem mpd,String protocol){
		Object val=null;
		if (protocol.equals(Protocol.BBMeter07)){//07�����Լ�������ʽ��97�治һ�µ�ʱ��ȡ����
			int len=mpd.getLength(),fraction=mpd.getFraction();
			if (mpd.getFraction2()>=0&&mpd.getFraction2()!=mpd.getFraction())
				fraction=mpd.getFraction2();
			if (mpd.getLength2()>=0&&mpd.getLength2()!=mpd.getLength())
				len=mpd.getLength2();
			val=DataItemParser.parsevalue(frame,loc,len,fraction,mpd.getType());
		}else
			val=DataItemParser.parsevalue(frame,loc,mpd.getLength(),mpd.getFraction(),mpd.getType());
		return val;
	}

	@Override
	public Object[] parser(byte[] data, int loc, int len) {
		// TODO Auto-generated method stub
		return null;
	}
}
