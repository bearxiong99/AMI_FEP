package cn.hexing.fk.model;

public class RtuSetValue {
	/** ����ID */
    private long cmdId;
    /** ������� */
    private int cldh;
    /** ������״̬SJX1��ZT1��SJX2��ZT2������ZTΪ01ʧ�ܣ�Ĭ�ϣ���00�ɹ� */
    private String sjxzt;
    
	public long getCmdId() {
		return cmdId;
	}
	public void setCmdId(long cmdId) {
		this.cmdId = cmdId;
	}
	public int getCldh() {
		return cldh;
	}
	public void setCldh(int cldh) {
		this.cldh = cldh;
	}
	public String getSjxzt() {
		return sjxzt;
	}
	public void setSjxzt(String sjxzt) {
		this.sjxzt = sjxzt;
	}
	
    
	
}
