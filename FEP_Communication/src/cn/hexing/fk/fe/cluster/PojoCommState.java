/**
 * Communication state of RTU.
 * Used by DB service.
 */
package cn.hexing.fk.fe.cluster;

import java.io.Serializable;
import java.util.Date;

import cn.hexing.fk.utils.CalendarUtil;

/**
 *
 */
public class PojoCommState implements Serializable{
	
	private static final long serialVersionUID = 8128041375296010816L;
/*	private int rtua = 0;
    *//** 当前有效短信网关应用号,范例955983015 *//*
    private String activeUms = null;
    *//** 终端SIM卡号 *//*
    private String simNum = null;
    private long lastGprsTime = 0;		//最近gprs上行时间
    private long lastSmsTime = 0;		//最近SMS上行时间
    private long lastHeartbeat = 0;
    private int taskCount = 0;			//当天任务上行数量
    //工况信息
    private int upGprsFlowmeter = 0;	//上行gprs流量
    private int downGprsFlowmeter = 0;	//下行GPRS流量
    private int upSmsCount = 0;			//上行sms条数
    private int downSmsCount = 0;		//下行sms条数
    private int upGprsCount = 0;		//上行GPRS报文数量
    private int downGprsCount = 0;		//下行GPRS报文数量
*/  
	private RtuState rtuState = null;
	private WorkState workState = null;
	private String address = "n/a";

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getRtua() {
		return rtuState.getRtua();
	}
	
	public String getLogicalAddress(){
		return rtuState.getRtua();
	}

	public String getActiveUms() {
		return rtuState.getActiveUms();
	}

	public String getSimNum() {
		return rtuState.getSimNum();
	}
	
	public String getTerminalType(){
		return rtuState.getTerminalType();
	}
	

	public Date getLastGprsRecvTime() {
		long time = workState.getLastGprsTime();
		if( 0 != time )
			return new Date(time);
		return null;
	}

	public Date getLastSmsRecvTime() {
		long time = workState.getLastSmsTime();
		if( 0 != time )
			return new Date(time);
		return null;
	}

	public Date getLastHeartbeatTime() {
		long time = workState.getLastHeartbeat();
		if( 0 != time )
			return new Date(time);
		return null;
	}

	public int getTaskCount() {
		return workState.getTaskCount();
	}

	public int getLoginGprsFlowmeter(){
		return workState.getLoginGprsFlowmeter();
	}
	
	public int getHeartGprsFlowmeter(){
		return workState.getHeartGprsFlowmeter();
	}
	
	public int getUpGprsFlowmeter() {
		return workState.getUpGprsFlowmeter();
	}

	public int getDownGprsFlowmeter() {
		return workState.getDownGprsFlowmeter();
	}

	public int getUpSmsCount() {
		return workState.getUpSmsCount();
	}

	public int getDownSmsCount() {
		return workState.getDownSmsCount();
	}

	public int getUpGprsCount() {
		return workState.getUpGprsCount();
	}

	public int getDownGprsCount() {
		return workState.getDownGprsCount();
	}
	
	public String getHasTaskFlag(){
		return getTaskCount()>0 ? "1" : "0";
	}

	public String getUsage() {
		return this.rtuState.getUsage();
	}

	public String getDateString() {
		return CalendarUtil.getDateString(System.currentTimeMillis());
	}

	public void setRtuState(RtuState rtuState) {
		this.rtuState = rtuState;
	}

	public void setWorkState(WorkState workState) {
		this.workState = workState;
	}

}
