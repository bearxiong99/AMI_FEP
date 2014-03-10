package cn.hexing.fas.model;

import java.io.Serializable;
import java.util.Date;

/**
 * FAAL 通讯响应
 */
public class FaalRereadTaskResponse implements Serializable{
	private static final long serialVersionUID = 2L;
	
	/** 终端逻辑地址 */
    private String logicAddress;
    /** 单位代码 */
    private String deptCode;
    /** 任务号 */
    private String taskNum;
    /** 任务模版ID */
    private String taskTemplateID;    
	/** 数据时间 */
    private Date SJSJ;
    /** 补招标志：0成功；1失败 */
    private int rereadTag;
    
	public String getLogicAddress() {
		return logicAddress;
	}
	public void setLogicAddress(String logicAddress) {
		this.logicAddress = logicAddress;
	}
	public String getTaskNum() {
		return taskNum;
	}
	public void setTaskNum(String taskNum) {
		this.taskNum = taskNum;
	}
	public Date getSJSJ() {
		return SJSJ;
	}
	public void setSJSJ(Date sjsj) {
		SJSJ = sjsj;
	}
	public int getRereadTag() {
		return rereadTag;
	}
	public void setRereadTag(int rereadTag) {
		this.rereadTag = rereadTag;
	}
	public String getTaskTemplateID() {
		return taskTemplateID;
	}
	public void setTaskTemplateID(String taskTemplateID) {
		this.taskTemplateID = taskTemplateID;
	}
	public String getDeptCode() {
		return deptCode;
	}
	public void setDeptCode(String deptCode) {
		this.deptCode = deptCode;
	}
       
}
