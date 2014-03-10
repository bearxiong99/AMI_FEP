package cn.hexing.fk.fe.cluster;

import java.io.Serializable;

import org.springframework.util.StringUtils;
/**
 * �ն�״̬��������ʵʱͬ���Ĳ�����ͨ�Ż���������
 * �������ݻ������ȣ�4+20+12+2+11 ~= 50
 * 150���նˣ������ڴ���Ҫ70M��JAVA�����ڴ����100M��
 *
 */
public class RtuState implements Serializable {
	private static final long serialVersionUID = -6780420689435993338L;
	private String rtua = null;
    /** ��ǰ��ЧGPRS���ص�ַ,��ʽ:IP:PORT */
	private String activeGprs = null;
    /** ��ǰ��Ч��������Ӧ�ú�,����955983015 */
    private String activeUms = null;
    private String activeSubAppId;		//�ն˶������з��ֵ�SubAppId����ʼ��ֵ�����ն˲�����
    /** �ն�SIM���� */
    private String simNum = null;
    private String dwdm = "";			//�ն˵�λ����
    private String usage = "";			//�ն���;
    private boolean heartBeatPersist = false;	//�Ƿ񱣴�����
    private String terminalType;
    private String communicationMode;
    
    protected RtuState(String logicalAddr ){
    	rtua = logicalAddr;
    }
    
	public String getRtua() {
		return rtua;
	}
	public void setRtua(String _rtua) {
		this.rtua = _rtua;
	}
	public String getActiveGprs() {
		return activeGprs;
	}
	public void setActiveGprs(String activeGprs) {
		this.activeGprs = activeGprs;
	}
	public String getActiveUms() {
		return activeUms;
	}
	public void setActiveUms(String activeUms) {
		this.activeUms = activeUms;
	}
	public String getSimNum() {
		return simNum;
	}
	public void setSimNum(String simNum) {
		this.simNum = simNum;
	}
	
	public String toString(){
		return "rtu state=[" + rtua + ",gprs=" + activeGprs + ",ums=" + activeUms + ",sim=" + simNum + "]";  
	}
	public String getActiveSubAppId() {
		return activeSubAppId;
	}
	public void setActiveSubAppId(String activeSubAppId) {
		this.activeSubAppId = activeSubAppId;
	}
	
	public String getUmsAddress(){
		StringBuilder sb = new StringBuilder();
		if( StringUtils.hasText(activeUms)){
			sb.append("95598").append(activeUms);
			if( StringUtils.hasText(activeSubAppId))
				sb.append(activeSubAppId);
		}
		return sb.toString();
	}
	
	public String simNumber(){
		return StringUtils.hasText(simNum) ? simNum : "";
	}

	public String getDwdm() {
		return dwdm;
	}

	public void setDwdm(String dwdm) {
		this.dwdm = dwdm;
	}

	public String getUsage() {
		return usage;
	}

	public void setUsage(String usage) {
		this.usage = usage;
	}

	public boolean isHeartBeatPersist() {
		return heartBeatPersist;
	}

	public void setHeartBeatPersist(boolean heartBeatPersist) {
		this.heartBeatPersist = heartBeatPersist;
	}

	public String getTerminalType() {
		return terminalType;
	}

	public void setTerminalType(String terminalType) {
		this.terminalType = terminalType;
	}

	public String getCommunicationMode() {
		return communicationMode;
	}

	public void setCommunicationMode(String communicationMode) {
		this.communicationMode = communicationMode;
	}
}
