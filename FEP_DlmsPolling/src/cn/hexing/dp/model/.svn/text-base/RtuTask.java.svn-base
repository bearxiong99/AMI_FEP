package cn.hexing.dp.model;

import java.util.Date;

import cn.hexing.dp.bpserver.ansi.AnsiTaskProcessor.AnsiTaskType;
import cn.hexing.dp.bpserver.dlms.DlmsProcessor.TaskType;


public class RtuTask{
	/** 终端逻辑地址 */
    private String rtuId;
    /** 终端任务号 */
    private String taskNo;
    /** 任务模版ID */
    private String taskTemplateID;
    /** 任务时间 */
    private Date taskDate;
    /** 发送次数 */
    private int sendCount=0;
    /**任务属性  日冻结，月冻结*/
    private String taskProperty;
    /**任务类型 终端任务=0,主站任务=1,事件任务=2*/
    private TaskType taskType;
    private AnsiTaskType ansiTaskType;
    /**采样加个时间单位*/
    private String sampleIntervalUnit;
    /**采样间隔时间*/
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
