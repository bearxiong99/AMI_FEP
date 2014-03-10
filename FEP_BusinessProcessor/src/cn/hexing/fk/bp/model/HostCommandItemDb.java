package cn.hexing.fk.bp.model;

import java.util.Date;

/**
 * ��վ������������
 */
public class HostCommandItemDb {
     /** �������ý�������óɹ� */
    public static final int STATUS_SUCCESS = 0;
    /** �������ý������ȷ�� */
    public static final int STATUS_AMBIGUOUS = 1;
    /** �������ý��������ʧ�� */
    public static final int STATUS_FAILED = 2;
    
    /** ����ID */
    private long commandId;
    /** ������� */
    private String tn;
    /** ��������� */
    private String code;
    /** ����ֵ */
    private String value;
    /** ����ʱ��/�澯ʱ�� */
    private Date time;
    /** �ն�ʱ��*/
    private Date terminalTime;
    /** ���ʱ�� */
    private Date programTime;
    /** ͨ�� */
    private String channel;
    /** ��λ���� */
    private String dwcode;
    /** �������ý�� */
    private int status;
    /**ʱ��ƫ���� */
    private long timedifference;
    /**�Ƿ����м�*/
    private int relay;
    
    private String logicAddress;
    
    /**
     * @return Returns the commandId.
     */
    public Long getCommandId() {
        return commandId;
    }
    /**
     * @param commandId The commandId to set.
     */
    public void setCommandId(Long commandId) {
        this.commandId = commandId;
    }
    
    public Long getTimedifference() {
        return timedifference;
    }
    /**
     * @param timedifference to set.
     */
    public void setTimedifference(Long timedifference) {
        this.timedifference = timedifference;
    }
    /**
     * @return Returns the tn.
     */
    public String getTn() {
        return tn;
    }
    /**
     * @param tn The tn to set.
     */
    public void setTn(String tn) {
        this.tn = tn;
    }
    /**
     * @return Returns the alertCode.
     */

    /**
     * @return Returns the code.
     */
    public String getCode() {
        return code;
    }
    /**
     * @param code The code to set.
     */
    public void setCode(String code) {
        this.code = code;
    }
    /**
     * @return Returns the value.
     */
    public String getValue() {
        return value;
    }
    /**
     * @param value The value to set.
     */
    public void setValue(String value) {
        this.value = value;
    }
    /**
     * @return Returns the time.
     */
    public Date getTime() {
        return time;
    }
    /**
     * @param time The time to set.
     */
    public void setTime(Date time) {
        this.time = time;
    }
    public Date getTerminalTime() {
        return terminalTime;
    }
    /**
     * @param time The time to set.
     */
    public void setTerminalTime(Date terminalTime) {
        this.terminalTime = terminalTime;
    }
    /**
     * @return Returns the programTime.
     */
    public Date getProgramTime() {
        return programTime;
    }
    /**
     * @param programTime The programTime to set.
     */
    public void setProgramTime(Date programTime) {
        this.programTime = programTime;
    }
    /**
     * @return Returns the channel.
     */
    public String getChannel() {
        return channel;
    }
    /**
     * @param channel The channel to set.
     */
    public void setChannel(String channel) {
        this.channel = channel;
    }
    //��ӵ�λ����
    public String getDwcode() {
        return dwcode;
    }
    /**
     * @param set dwdm.
     */
    public void setDwcode(String dwcode) {
        this.dwcode = dwcode;
    }
    /**
     * @return Returns the status.
     */
    public int getStatus() {
        return status;
    }
    /**
     * @param status The status to set.
     */
    public void setStatus(int status) {
        this.status = status;
    }
    public int getRelay() {
        return relay;
    }
    /**
     * @param relay to set 0:GPRS  1:�м�.
     */
    public void setRelay(int relay) {
        this.relay = relay;
    }
	public void setCommandId(long commandId) {
		this.commandId = commandId;
	}
	public String getLogicAddress() {
		return logicAddress;
	}
	public void setLogicAddress(String logicAddress) {
		this.logicAddress = logicAddress;
	}
}
