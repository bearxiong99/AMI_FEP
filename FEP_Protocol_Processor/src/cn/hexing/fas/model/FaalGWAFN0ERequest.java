package cn.hexing.fas.model;

/**
 * ����AFN=0E����������
 */
public class FaalGWAFN0ERequest extends FaalRequest {

    private static final long serialVersionUID = -562982183058685616L; 
    /** ���з���ʱ�� */
    private String tpSendTime;
    /** ���з��ʹ��䳬ʱʱ�� */
    private int tpTimeout;
    /** �����¼���¼��ʼָ��pm */
    private int pm;
    /** �����¼���¼����ָ��pn */
    private int pn;
    public FaalGWAFN0ERequest() {
    }
	public String getTpSendTime() {
		return tpSendTime;
	}
	public void setTpSendTime(String tpSendTime) {
		this.tpSendTime = tpSendTime;
	}
	public int getTpTimeout() {
		return tpTimeout;
	}
	public void setTpTimeout(int tpTimeout) {
		this.tpTimeout = tpTimeout;
	}
	public int getPm() {
		return pm;
	}
	public void setPm(int pm) {
		this.pm = pm;
	}
	public int getPn() {
		return pn;
	}
	public void setPn(int pn) {
		this.pn = pn;
	}
    
}