package cn.hexing.fas.model;

import java.io.Serializable;
import java.util.Date;

/**
 * FAAL ͨѶ��Ӧ
 */
public class FaalRereadTaskResponse implements Serializable{
	private static final long serialVersionUID = 2L;
	
	/** �ն��߼���ַ */
    private String logicAddress;
    /** ��λ���� */
    private String deptCode;
    /** ����� */
    private String taskNum;
    /** ����ģ��ID */
    private String taskTemplateID;    
	/** ����ʱ�� */
    private Date SJSJ;
    /** ���б�־��0�ɹ���1ʧ�� */
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
