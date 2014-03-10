package cn.hexing.fas.model;

import java.util.Date;
/**
 * 广规控制码C=33H 请求命令 ：预付费信息类
 * @author Administrator
 *
 */
public class FaalGGKZM33Request extends FaalRequest {
	private static final long serialVersionUID = 4740138509881432066L;
	/** 电表地址*/
    private String meterAddr;
    /** 数据时间 */
    private Date dataTime;
    /**数据项编码*/
    private String code;
    /**数据项内容*/
    private String token;
    
    public FaalGGKZM33Request() {
        super();
        type = FaalRequest.TYPE_pay_token;
    }

	public String getMeterAddr() {
		return meterAddr;
	}

	public void setMeterAddr(String meterAddr) {
		this.meterAddr = meterAddr;
	}
	public Date getDataTime() {
		return dataTime;
	}

	public void setDataTime(Date dataTime) {
		this.dataTime = dataTime;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	
    
   
}
