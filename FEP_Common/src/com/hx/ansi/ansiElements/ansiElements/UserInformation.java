package com.hx.ansi.ansiElements.ansiElements;
/** 
 * @Description  �û���Ϣ��
 * @author  Rolinbor
 * @Copyright 2013 hexing Inc. All rights reserved
 * @time��2013-3-15 ����04:19:00
 * @version 1.0 
 */
/**
 * �û���Ϣ��
 */
public class UserInformation {
	/** �û�ID*/
	private String userID;
	/**�û���Ϣ */
	private String userName;
	/**���� */
	private String passWord;
	/**�ն˵�ַ��IP+port*/
	private String peerAddr;
	/** ���ط��ʵ�ַ*/
	private String localAddr;
	/** ��������*/
	private int securityMode;
	
	public String getLocalAddr() {
		return localAddr;
	}
	public void setLocalAddr(String localAddr) {
		this.localAddr = localAddr;
	}
	public String getPeerAddr() {
		return peerAddr;
	}
	public void setPeerAddr(String peerAddr) {
		this.peerAddr = peerAddr;
	}
	public String getUserID() {
		return userID;
	}
	public void setUserID(String userID) {
		this.userID = userID;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassWord() {
		return passWord;
	}
	public void setPassWord(String passWord) {
		this.passWord = passWord;
	}
	public int getSecurityMode() {
		return securityMode;
	}
	public void setSecurityMode(int securityMode) {
		this.securityMode = securityMode;
	}
	
	
	
}
