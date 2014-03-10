
package cn.hexing.fas.model;

import java.util.Date;

/**
 * 广规控制码=14的请求命令：集中器控制操作类
 */
public class FaalGGKZM14Request extends FaalRequest {
	private static final long serialVersionUID = 4740138509881420066L;
	/** 电表地址*/
    private String meterAddr;
    /** 有效时间  默认为10分钟  单位：分钟*/
    private int EffectiveTime=10;
    /** 数据时间 */
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
