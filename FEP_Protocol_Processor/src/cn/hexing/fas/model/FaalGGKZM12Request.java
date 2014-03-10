package cn.hexing.fas.model;

import java.util.Date;


/**
 * ��������=12��������������������ն�������
 */
public class FaalGGKZM12Request extends FaalRequest {
	private static final long serialVersionUID = 4740138509881420068L;

	/**��ʼ���˳���*/
	private int startMeterNo;
	/**���������*/
	private int num;
    /** ������ʼʱ�� */
    private Date startTime;
    /** ���ݽ���ʱ�� */
    private Date endTime;
    /**�����*/
    private String taskNo;
    
    public FaalGGKZM12Request() {
        super();
        type = FaalRequest.TYPE_READ_HISTORY_DATA12;
    }

	
    public int getStartMeterNo(){
    	return startMeterNo;
    }

    public void setStartMeterNo(int startMeterNo){
    	this.startMeterNo=startMeterNo;
    }
    public int getNum(){
    	return num;
    }

    public void setNum(int num){
    	this.num=num;
    }
	public Date getSrartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}


	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}


	public String getTaskNo() {
		return taskNo;
	}


	public void setTaskNo(String taskNo) {
		this.taskNo = taskNo;
	}
 
}
