package cn.hexing.fk.model;

public class RtuSynchronizeItem {
	/** �ն˾ֺ�ID */
    private String rtuId;
    /** ͬ�����ͣ�0 �ն˵���ͬ����1 ����ģ��ͬ�� */
    private int SycType;
    /** ͬ��ʱ�� */
    private String SycTime;
    
	public String getRtuId() {
		return rtuId;
	}
	public void setRtuId(String rtuId) {
		this.rtuId = rtuId;
	}
	public int getSycType() {
		return SycType;
	}
	public void setSycType(int sycType) {
		SycType = sycType;
	}
	public String getSycTime() {
		return SycTime;
	}
	public void setSycTime(String sycTime) {
		SycTime = sycTime;
	}
    
}
