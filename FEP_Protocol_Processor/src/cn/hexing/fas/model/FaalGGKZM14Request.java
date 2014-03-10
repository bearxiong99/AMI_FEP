
package cn.hexing.fas.model;

import java.util.Date;

/**
 * ��������=14������������������Ʋ�����
 */
public class FaalGGKZM14Request extends FaalRequest {
	private static final long serialVersionUID = 4740138509881420066L;
	/** ����ַ*/
    private String meterAddr;
    /** ��Чʱ��  Ĭ��Ϊ10����  ��λ������*/
    private int EffectiveTime=10;
    /** ����ʱ�� */
    private Date dataTime;
    
    public FaalGGKZM14Request() {
        super();
        this.setProtocol("04");
        type = FaalRequest.TYPE_Action;
    }

	public String getMeterAddr() {
		return meterAddr;
	}

	public void setMeterAddr(String meterAddr) {
		this.meterAddr = meterAddr;
	}

	public int getEffectiveTime() {
		return EffectiveTime;
	}

	public void setEffectiveTime(int EffectiveTime) {
		this.EffectiveTime = EffectiveTime;
	}

	public Date getDataTime() {
		return dataTime;
	}

	public void setDataTime(Date dataTime) {
		this.dataTime = dataTime;
	}

	
    
   
}
