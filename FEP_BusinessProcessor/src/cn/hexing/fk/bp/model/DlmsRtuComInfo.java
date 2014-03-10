package cn.hexing.fk.bp.model;

import cn.hexing.fk.message.IMessage;

public class DlmsRtuComInfo {
	private int rtua;
	//�ն��߼���ַ
	private String logicAddress;
	//�ն˵�ǰ����Bp��ַ
	private String bpAddr;
	//�ն˿��б�� 0:����;1:��æ
	private int freeTag=0;	
	//���SysTitle
	private String meterSysTitle ;
	//��������
	private String meterRandom;
	//��֤ʱ��
	private long authTime=System.currentTimeMillis();
	//�������ֱ��ģ�ÿ���ն˶��ǹ̶��̶�
	private IMessage shakeHandMsg;
	//���ֱ��
	private boolean shakeHandTag=false;
	//��֤���
	private boolean authTag=false;
	//��֤ʧ�ܺ����·����������̴���
	private int shakeHandCount=0;
	
	public String getLogicAddress() {
		return logicAddress;
	}
	public void setLogicAddress(String logicAddress) {
		this.logicAddress = logicAddress;
	}
	public String getBpAddr() {
		return bpAddr;
	}
	public void setBpAddr(String bpAddr) {
		this.bpAddr = bpAddr;
	}
	public int getFreeTag() {
		return freeTag;
	}
	public void setFreeTag(int freeTag) {
		this.freeTag = freeTag;
	}
	public int getRtua() {
		return rtua;
	}
	public void setRtua(int rtua) {
		this.rtua = rtua;
	}
	public String getMeterSysTitle() {
		return meterSysTitle;
	}
	public void setMeterSysTitle(String meterSysTitle) {
		this.meterSysTitle = meterSysTitle;
	}
	public String getMeterRandom() {
		return meterRandom;
	}
	public void setMeterRandom(String meterRandom) {
		this.meterRandom = meterRandom;
	}
	public long getAuthTime() {
		return authTime;
	}
	public void setAuthTime(long authTime) {
		this.authTime = authTime;
	}
	public IMessage getShakeHandMsg() {
		return shakeHandMsg;
	}
	public void setShakeHandMsg(IMessage shakeHandMsg) {
		this.shakeHandMsg = shakeHandMsg;
	}
	public boolean isAuthTag() {
		return authTag;
	}
	public void setAuthTag(boolean authTag) {
		this.authTag = authTag;
	}
	public boolean isShakeHandTag() {
		return shakeHandTag;
	}
	public void setShakeHandTag(boolean shakeHandTag) {
		this.shakeHandTag = shakeHandTag;
	}
	public int getShakeHandCount() {
		return shakeHandCount;
	}
	public void setShakeHandCount(int shakeHandCount) {
		this.shakeHandCount = shakeHandCount;
	}
	public void shakeHandCountAdd(){
		this.shakeHandCount++;
	}

	
}
