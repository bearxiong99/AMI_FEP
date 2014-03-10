package cn.hexing.dp.model;

import java.util.Date;

import cn.hexing.dp.bpserver.ansi.AnsiTaskProcessor.AnsiTaskType;
import cn.hexing.dp.bpserver.dlms.DlmsProcessor.TaskType;


public class RtuTask{
	/** �ն��߼���ַ */
    private String rtuId;
    /** �ն������ */
    private String taskNo;
    /** ����ģ��ID */
    private String taskTemplateID;
    /** ����ʱ�� */
    private Date taskDate;
    /** ���ʹ��� */
    private int sendCount=0;
    /**��������  �ն��ᣬ�¶���*/
    private String taskProperty;
    /**�������� �ն�����=0,��վ����=1,�¼�����=2*/
    private TaskType taskType;
    private AnsiTaskType ansiTaskType;
    /**�����Ӹ�ʱ�䵥λ*/
    private String sampleIntervalUnit;
    /**�������ʱ��*/
    private int sampleInterval;
    
	public String getRtuId() {
		return rtuId;
	}
	public void setRtuId(String rtuId) {
		this.rtuId = rtuId;
	}
	public String getTaskNo() {
		return taskNo;
	}
	public void setTaskNo(String taskNo) {
		this.taskNo = taskNo;
	}
	public String getTaskTemplateID() {
		return taskTemplateID;
	}
	public void setTaskTemplateID(String taskTemplateID) {
		this.taskTemplateID = taskTemplateID;
	}
	public Date getTaskDate() {
		return taskDate;
	}
	public void setTaskDate(Date taskDate) {
		this.taskDate = taskDate;
	}
	public int getSendCount() {
		return sendCount;
	}
	public void setSendCount(int sendCount) {
		this.sendCount = sendCount;
	}
	public final String getTaskProperty() {
		return taskProperty;
	}
	public final void setTaskProperty(String taskProperty) {
		this.taskProperty = taskProperty;
	}
	public final TaskType getTaskType() {
		return taskType;
	}
	public final void setTaskType(TaskType taskType) {
		this.taskType = taskType;
	}
	public final String getSampleIntervalUnit() {
		return sampleIntervalUnit;
	}
	public final void setSampleIntervalUnit(String sampleIntervalUnit) {
		this.sampleIntervalUnit = sampleIntervalUnit;
	}
	public final int getSampleInterval() {
		return sampleInterval;
	}
	public final void setSampleInterval(int sampleInterval) {
		this.sampleInterval = sampleInterval;
	}
	public AnsiTaskType getAnsiTaskType() {
		return ansiTaskType;
	}
	public void setAnsiTaskType(AnsiTaskType ansiTaskType) {
		this.ansiTaskType = ansiTaskType;
	}
	
}
