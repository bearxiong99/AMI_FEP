package cn.hexing.em.client;

import java.util.Map;

/** 
 * @Description ���ܻ��ӿڷ��ؽ������
 * @author  jun
 * @Copyright 2012 hexing Inc. All rights reserved
 * @time��2012-9-9
 * @version AMI3.0 
 */
public class EmResult {
	// ���������־
	private boolean success;
	// ��������
	private Map<String,String> rtnMap;
	
	private int resultCode;
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public Map<String, String> getRtnMap() {
		return rtnMap;
	}
	public void setRtnMap(Map<String, String> rtnMap) {
		this.rtnMap = rtnMap;
	}
	public final int getResultCode() {
		return resultCode;
	}
	public final void setResultCode(int resultCode) {
		this.resultCode = resultCode;
	}
}
