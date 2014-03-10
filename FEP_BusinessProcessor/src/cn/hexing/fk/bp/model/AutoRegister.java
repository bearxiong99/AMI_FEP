package cn.hexing.fk.bp.model;

import java.io.Serializable;
import java.util.Date;

/**
 * 集中器自动注册
 * @author Administrator
 *
 */
public class AutoRegister implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2424517805121047344L;

	/**终端逻辑地址*/
	private String logicAddress;
	
	private String measurePoint;
	
	/**表地址*/
	private String meterAddress;
	
	private Date registerTime;
	/**接入状态,默认为0,0表示未接入，1表示接入*/
	private int status = 0;
	/**参数值*/
	private String value;

	public final String getLogicAddress() {
		return logicAddress;
	}

	public final void setLogicAddress(String logicAddress) {
		this.logicAddress = logicAddress;
	}

	public final String getMeasurePoint() {
		return measurePoint;
	}

	public final void setMeasurePoint(String measurePoint) {
		this.measurePoint = measurePoint;
	}

	public final Date getRegisterTime() {
		return registerTime;
	}

	public final void setRegisterTime(Date registerTime) {
		this.registerTime = registerTime;
	}

	public final int getStatus() {
		return status;
	}

	public final void setStatus(int status) {
		this.status = status;
	}

	public final String getValue() {
		return value;
	}

	public final void setValue(String value) {
		this.value = value;
	}

	public final String getMeterAddress() {
		return meterAddress;
	}

	public final void setMeterAddress(String meterAddress) {
		this.meterAddress = meterAddress;
	}
	
}
