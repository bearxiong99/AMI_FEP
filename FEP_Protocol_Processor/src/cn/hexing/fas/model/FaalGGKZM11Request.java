package cn.hexing.fas.model;

import java.util.Date;

/**
 * ��������=11��������������������ն�������
 */
public class FaalGGKZM11Request extends FaalRequest {
	private static final long serialVersionUID = 4740138509881420066L;
	/** ����ַ*/
    private String meterAddr;
    /** ��������� */
    private int meterNo;
    /** ����ʱ�� */
    private Date dataTime;
    
    private boolean isTask=false;
    
    public FaalGGKZM11Request() {
        super();
        this.setProtocol("04");
        type = FaalRequest.TYPE_READ_HISTORY_DATA11;
    }

	public String getMeterAddr() {
		return meterAddr;
	}

	public void setMeterAddr(String meterAddr) {
		this.meterAddr = meterAddr;
	}

	public int getMeterNo() {
		return meterNo;
	}

	public void setMeterNo(int meterNo) {
		this.meterNo = meterNo;
	}

	public Date getDataTime() {
		return dataTime;
	}

	public void setDataTime(Date dataTime) {
		this.dataTime = dataTime;
	}

	public boolean isTask() {
		return isTask;
	}

	public void setTask(boolean isTask) {
		this.isTask = isTask;
	}

	
    
   
}
