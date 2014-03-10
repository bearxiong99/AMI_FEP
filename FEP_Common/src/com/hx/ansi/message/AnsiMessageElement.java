package com.hx.ansi.message;

import com.hx.ansi.element.CalledAPInvocationIdElement;
import com.hx.ansi.element.CalledAPTitleElement;
import com.hx.ansi.element.CallingAPInvocationIdElement;
import com.hx.ansi.element.CallingAPTitleElement;
import com.hx.ansi.element.UserInformationElement;

/** 
 * @Description  ANSIMessage Element
 * @author  Rolinbor
 * @Copyright 2013 hexing Inc. All rights reserved
 * @time��2013-5-2 ����11:02:56
 * @version 1.0 
 */

public class AnsiMessageElement {
	
//	private static  final AnsiMessageElement instance=new AnsiMessageElement();
//	public static  final AnsiMessageElement getInstance (){ return instance;}
	private String serverTag;
	private String meterAddr;
	private String peerAddr;//A6 �������б��ģ�����A6�����ն˵ĵ�ַ
	private String hostAddr;//A2 �������б��ģ�����A2������վ�ĵ�ַ
	private int calledInvocId;//A4
	private int callingInvocId;//A8 ��֡�� A8 04 03 02 ZZS DQZ ��ZZS-��֡����DQZ����ǰ֡��
	private int  calledTotalMessage;	
	private String userData;//BE ������
	private String epsem_control;
	private int dataLength;
	private String result;
	private String BEuserData;//ȥ��BE������һЩ������Ϣ֮������ݣ��� resultData
	private int SEQ;
	private String A4;
	private String A8;
	
	public AnsiMessageElement(){}

	public  void decodeMessage(String data){
		
		/* ������Ϣ���ػ�ͨ�����ӵ�peerAddr����ȡ��Ϣ��context��context�������logicalAddress���˴��Ǹ���ͨ�ŵĹ����е�A6����ȡpeerAddr
		          ��������upMessage�������ش����������peerAddr������ֱ�Ӵ�message�����ȡpeerAddr��
		String logicalAddress = evt.getMessage().getLogicalAddress();
		String peerAddr=evt.getMessage().getPeerAddr();*/
//		String acse_pdu=data.substring(0, 4);
		String s=data.substring(4, 6);
		//605EA20806067F000001F126A403020102A6080606AC10FBEF1D94A80302010DBE3F283D813B8039000035000030020A884845434F0200191901000A0202030100EFBDF1DF031EFCF0C11FC203C807000000E0A0400002244000000AC0031630
		if(s.equalsIgnoreCase("A2")){
			//����A2
			CalledAPTitleElement calledAPTitle=new CalledAPTitleElement();
			int calledAPlen=Integer.parseInt(data.substring(6, 8), 16);
			calledAPTitle.setCalledAPTitleElement(data.substring(4, 8+calledAPlen*2));
			calledAPTitle.decode();//����A2
			this.hostAddr=calledAPTitle.getPeerAddr();
			data=data.substring(8+calledAPlen*2);//ȥ���Ѿ������Ĳ���
			//����A4
			CalledAPInvocationIdElement calledAPInvocationId=new CalledAPInvocationIdElement();
			calledAPInvocationId.setCalledAPInvocationIdElement(data.substring(8, 12));
			calledAPInvocationId.decode();//����A4 ���ڹ̶�invocationIdΪ1���ֽ�
			this.calledInvocId=calledAPInvocationId.getCalledAPInvocationId();
			this.calledTotalMessage=calledAPInvocationId.getcalledTotalMessage();
			data=data.substring(12);
			//����A6
			CallingAPTitleElement callingAPTitle=new CallingAPTitleElement();
			int callingAPlen=Integer.parseInt(data.substring(2, 4), 16);
			callingAPTitle.setCallingAPTitleElement(data.substring(0, 4+callingAPlen*2));
			callingAPTitle.decode();//����A6
			this.peerAddr=callingAPTitle.getPeerAddr();
//			//ͨ��������Ϣ�д��е�ip��port��ȡcontext
//			AnsiContext context=contextManager.getContextByAddr(callingAPTitle.getPeerAddr());
			data=data.substring(4+callingAPlen*2);
			//����A8
			CallingAPInvocationIdElement callingAPInvocationId=new CallingAPInvocationIdElement();
			callingAPInvocationId.setCallingAPInvocationIdElement(data.substring(8, 10));
			callingAPInvocationId.decode();//����A8 ���ڹ̶�invocationIdΪ1���ֽ�
			this.callingInvocId=callingAPInvocationId.getCallingAPInvocationId();
			data=data.substring(10);
			//�����û�����BE
			this.userData=data;
			UserInformationElement userInformation=new UserInformationElement();
			userInformation.decode(data);//����������
			this.epsem_control=userInformation.getEpsem_control();
			this.dataLength=userInformation.getDataLength();
			this.result=userInformation.getResult();
			this.BEuserData=userInformation.getUserData();
		}else if(s.equalsIgnoreCase("A4")){
			//����A4
			CalledAPInvocationIdElement calledAPInvocationId=new CalledAPInvocationIdElement();
			calledAPInvocationId.setCalledAPInvocationIdElement(data.substring(8, 20));
			this.A4=data.substring(8, 20);
			calledAPInvocationId.decode();//����A4 ���ڹ̶�invocationIdΪ1���ֽ�
			this.SEQ=calledAPInvocationId.getSeq();
			this.calledInvocId=calledAPInvocationId.getCalledAPInvocationId();
			this.calledTotalMessage=calledAPInvocationId.getcalledTotalMessage();
			data=data.substring(20);
			//����A8
			CallingAPInvocationIdElement callingAPInvocationId=new CallingAPInvocationIdElement();
			callingAPInvocationId.setCallingAPInvocationIdElement(data.substring(8, 10));
			this.A8=data.substring(0, 10);
			callingAPInvocationId.decode();//����A8 ���ڹ̶�invocationIdΪ1���ֽ�
			this.callingInvocId=callingAPInvocationId.getCallingAPInvocationId();
			data=data.substring(10);
			//�����û�����BE
			this.userData=data;
			UserInformationElement userInformation=new UserInformationElement();
			userInformation.decode(data);//����������
			this.epsem_control=userInformation.getEpsem_control();
			this.dataLength=userInformation.getDataLength();
			this.result=userInformation.getResult();
			this.BEuserData=userInformation.getUserData();
		}
		else if(s.equalsIgnoreCase("BE")){
			data=data.substring(4);
			//�����û�����BE
			this.userData=data;
			UserInformationElement userInformation=new UserInformationElement();
			userInformation.decode(data);//����������
			this.epsem_control=userInformation.getEpsem_control();
			this.dataLength=userInformation.getDataLength();
			this.serverTag=userInformation.getServerTag();
			this.meterAddr=userInformation.getMeterAddr();
		}
	}
	
	
	
	public String getServerTag() {
		return serverTag;
	}
	public void setServerTag(String serverTag) {
		this.serverTag = serverTag;
	}
	public String getMeterAddr() {
		return meterAddr;
	}
	public void setMeterAddr(String meterAddr) {
		this.meterAddr = meterAddr;
	}
	public String getPeerAddr() {
		return peerAddr;
	}
	public void setPeerAddr(String peerAddr) {
		this.peerAddr = peerAddr;
	}
	public String getHostAddr() {
		return hostAddr;
	}
	public void setHostAddr(String hostAddr) {
		this.hostAddr = hostAddr;
	}

	public int getCalledInvocId() {
		return calledInvocId;
	}

	public void setCalledInvocId(int calledInvocId) {
		this.calledInvocId = calledInvocId;
	}

	public int getCallingInvocId() {
		return callingInvocId;
	}

	public void setCallingInvocId(int callingInvocId) {
		this.callingInvocId = callingInvocId;
	}

	public String getUserData() {
		return userData;
	}

	public void setUserData(String userData) {
		this.userData = userData;
	}

	public String getEpsem_control() {
		return epsem_control;
	}

	public void setEpsem_control(String epsem_control) {
		this.epsem_control = epsem_control;
	}

	public int getDataLength() {
		return dataLength;
	}

	public void setDataLength(int dataLength) {
		this.dataLength = dataLength;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getBEuserData() {
		return BEuserData;
	}

	public void setBEuserData(String bEuserData) {
		BEuserData = bEuserData;
	}

	public int getcalledTotalMessage() {
		return calledTotalMessage;
	}

	public void setcalledTotalMessage(int calledTotalMessage) {
		this.calledTotalMessage = calledTotalMessage;
	}

	public int getSEQ() {
		return SEQ;
	}

	public void setSEQ(int sEQ) {
		SEQ = sEQ;
	}

	public String getA4() {
		return A4;
	}

	public void setA4(String a4) {
		A4 = a4;
	}

	public String getA8() {
		return A8;
	}

	public void setA8(String a8) {
		A8 = a8;
	}
	
}
