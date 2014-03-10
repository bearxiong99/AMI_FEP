package cn.hexing.fk.bp.model;

import cn.hexing.fk.message.IMessage;

public class DlmsRtuComInfo {
	private int rtua;
	//终端逻辑地址
	private String logicAddress;
	//终端当前所属Bp地址
	private String bpAddr;
	//终端空闲标记 0:空闲;1:繁忙
	private int freeTag=0;	
	//电表SysTitle
	private String meterSysTitle ;
	//电表随机数
	private String meterRandom;
	//认证时间
	private long authTime=System.currentTimeMillis();
	//下行握手报文，每个终端都是固定固定
	private IMessage shakeHandMsg;
	//握手标记
	private boolean shakeHandTag=false;
	//认证标记
	private boolean authTag=false;
	//认证失败后，重新发起握手流程次数
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
