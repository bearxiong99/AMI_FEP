package cn.hexing.fk.model;

import cn.hexing.fk.utils.CalendarUtil;



/**
 * ͨѶǰ�û��ն˵����ṹ
 */
public class ComRtu {     
    /** �ն˾ֺ�ID */
    private String rtuId;
    /** ��λ���� */
    private String deptCode = "";
    /** �ն�ͨѶ��Լ */
    private String rtuProtocol;
    /** �ն��߼���ַ */
    private int rtua;
    /** �ն��߼���ַ��HEX�� */
    private String logicAddress;
    /** �ն���;��01ר�䣬02���䣬03��ѹ */
    private String rtuType;
    /** �ն�SIM���� */
    private String simNum;
    /** ��ͨѶ�ŵ����� (8010)*/
    private String commType;
    /** ��ͨѶ�ŵ���ַ (8010)*/
    private String commAddress;
    /** ����ͨ������1(8011) */
    private String b1CommType;
    /** ����ͨ����ַ1(8011) */
    private String b1CommAddress;
    /** �ն�����*/
    private String terminalType;
    /** ͨѶ���� ����GPRS����̫��*/
    private String communicationMode;
    
    
	public String getTerminalType() {
		return terminalType;
	}
	public void setTerminalType(String terminalType) {
		this.terminalType = terminalType;
	}
	public String getB1CommAddress() {
		return b1CommAddress;
	}
	/**
	 * @param commAddress Ҫ���õ� b1CommAddress��
	 */
	public void setB1CommAddress(String commAddress) {
		b1CommAddress = commAddress;
	}
	/**
	 * @return ���� b1CommType��
	 */
	public String getB1CommType() {
		return b1CommType;
	}
	/**
	 * @param commType Ҫ���õ� b1CommType��
	 */
	public void setB1CommType(String commType) {
		b1CommType = commType;
	}
	
	public String getRtuType() {
		return rtuType;
	}
	public void setRtuType(String rtuType) {
		this.rtuType = rtuType;
	}
	/**
	 * @return ���� commAddress��
	 */
	public String getCommAddress() {
		return commAddress;
	}
	/**
	 * @param commAddress Ҫ���õ� commAddress��
	 */
	public void setCommAddress(String commAddress) {
		this.commAddress = commAddress;
	}
	/**
	 * @return ���� commType��
	 */
	public String getCommType() {
		return commType;
	}
	/**
	 * @param commType Ҫ���õ� commType��
	 */
	public void setCommType(String commType) {
		this.commType = commType;
	}

	/**
	 * @return ���� deptCode��
	 */
	public String getDeptCode() {
		return deptCode;
	}
	/**
	 * @param deptCode Ҫ���õ� deptCode��
	 */
	public void setDeptCode(String deptCode) {
		this.deptCode = deptCode;
	}

	/**
	 * @return ���� logicAddress��
	 */
	public String getLogicAddress() {
		return logicAddress;
	}
	/**
	 * @param logicAddress Ҫ���õ� logicAddress��
	 */
	public void setLogicAddress(String logicAddress) {
		this.logicAddress = logicAddress;
	}
	
	/**
	 * @return ���� rtua��
	 */
	public int getRtua() {
		return rtua;
	}
	/**
	 * @param rtua Ҫ���õ� rtua��
	 */
	public void setRtua(int rtua) {
		this.rtua = rtua;
	}
	/**
	 * @return ���� rtuId��
	 */
	public String getRtuId() {
		return rtuId;
	}
	/**
	 * @param rtuId Ҫ���õ� rtuId��
	 */
	public void setRtuId(String rtuId) {
		this.rtuId = rtuId;
	}
	/**
	 * @return ���� rtuProtocol��
	 */
	public String getRtuProtocol() {
		return rtuProtocol;
	}
	/**
	 * @param rtuProtocol Ҫ���õ� rtuProtocol��
	 */
	public void setRtuProtocol(String rtuProtocol) {
		this.rtuProtocol = rtuProtocol;
	}

	/**
	 * @param simNum Ҫ���õ� simNum��
	 */
	public void setSimNum(String simNum) {
		this.simNum = simNum;
	}
	
	public String getDateString(){
		return CalendarUtil.getDateString(System.currentTimeMillis());
	}
	public String getSimNum() {
		return simNum;
	}
	public String getCommunicationMode() {
		return communicationMode;
	}
	public void setCommunicationMode(String communicationMode) {
		this.communicationMode = communicationMode;
	}
}
