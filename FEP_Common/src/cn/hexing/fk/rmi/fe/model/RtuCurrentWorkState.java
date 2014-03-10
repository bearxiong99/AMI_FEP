package cn.hexing.fk.rmi.fe.model;

import java.io.Serializable;
import java.util.Date;

public class RtuCurrentWorkState implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2335152714088819302L;
	//���һ������ʱ��
	private Date lastHeartBeatTime;
	//���һ��Gprsʱ��
	private Date lastGprsRecvTime;
	
	private String logicAddress;
	
	public Date getLastHeartBeatTime() {
		return lastHeartBeatTime;
	}
	public void setLastHeartBeatTime(Date lastHeartBeatTime) {
		this.lastHeartBeatTime = lastHeartBeatTime;
	}
	public Date getLastGprsRecvTime() {
		return lastGprsRecvTime;
	}
	public void setLastGprsRecvTime(Date lastGprsRecvTime) {
		this.lastGprsRecvTime = lastGprsRecvTime;
	}
	public String getLogicAddress() {
		return logicAddress;
	}
	public void setLogicAddress(String logicAddress) {
		this.logicAddress = logicAddress;
	}
	@Override
	public String toString() {
		return "lastHeartBeatTime:"+lastHeartBeatTime+",lastGprsRecvTime:"+lastGprsRecvTime;
	}
}
