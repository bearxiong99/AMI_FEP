package cn.hexing.fk.fe.cluster;

import java.io.Serializable;

import org.springframework.util.StringUtils;
/**
 * 终端状态：即必须实时同步的参数（通信基本参数）
 * 对象数据基本长度：4+20+12+2+11 ~= 50
 * 150万终端，基本内存需要70M，JAVA对象内存估计100M。
 *
 */
public class RtuState implements Serializable {
	private static final long serialVersionUID = -6780420689435993338L;
	private String rtua = null;
    /** 当前有效GPRS网关地址,格式:IP:PORT */
	private String activeGprs = null;
    /** 当前有效短信网关应用号,范例955983015 */
    private String activeUms = null;
    private String activeSubAppId;		//终端短信上行发现的SubAppId。初始化值来自终端参数表。
    /** 终端SIM卡号 */
    private String simNum = null;
    private String dwdm = "";			//终端单位代码
    private String usage = "";			//终端用途
    private boolean heartBeatPersist = false;	//是否保存心跳
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
