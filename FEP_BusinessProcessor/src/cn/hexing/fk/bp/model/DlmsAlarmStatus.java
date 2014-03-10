package cn.hexing.fk.bp.model;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @author gaoll
 *
 * @time 2013-2-2 ÏÂÎç12:41:59
 *
 * @info DLMS ¸æ¾¯×´Ì¬
 */
public class DlmsAlarmStatus implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -9172081650885528084L;

	private String logicAddress;
	
	private String obis;
	
	private Date lastReportTime;

	public final String getLogicAddress() {
		return logicAddress;
	}

	public final void setLogicAddress(String logicAddress) {
		this.logicAddress = logicAddress;
	}

	public final String getObis() {
		return obis;
	}

	public final void setObis(String obis) {
		this.obis = obis;
	}

	public final Date getLastReportTime() {
		return lastReportTime;
	}

	public final void setLastReportTime(Date lastReportTime) {
		this.lastReportTime = lastReportTime;
	}
	
}
