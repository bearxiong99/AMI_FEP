package cn.hexing.fas.model;

/**
 * �����޲����������࣬��ӦAFN=01��04��05��0C
 */
public class FaalGWNoParamRequest extends FaalRequest {

    private static final long serialVersionUID = -162982183058685616L; 
    /** ���з���ʱ�� */
    private String tpSendTime;
    /** ���з��ʹ��䳬ʱʱ�� */
    private int tpTimeout;
    public FaalGWNoParamRequest() {
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
    
}