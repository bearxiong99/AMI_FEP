package cn.hexing.fk.bp.model;

import java.util.Date;

/**
 * 终端上行/下行消息表映射类
 */
public class MessageLog {
	/** 终端逻辑地址 */
    private String logicAddress;
    /** 区域码(终端逻辑地址前两位) */
    private String qym;
    /** 控制码 */
    private String kzm;
    /** 源地址 */
    private String srcAddr;
    /** 目标地址 */
    private String destAddr;
    /** 通讯方式 */
    private String txfs;
    /** 通讯时间 */
    private Date time;   
    /** 报文大小 */
    private int size;
    /** 原始报文 */
    private String body;

    
	public String getBody() {
		return body;
	}


	public void setBody(String body) {
		this.body = body;
	}


	public String getDestAddr() {
		return destAddr;
	}


	public void setDestAddr(String destAddr) {
		this.destAddr = destAddr;
	}


	public String getKzm() {
		return kzm;
	}


	public void setKzm(String kzm) {
		this.kzm = kzm;
	}


	public String getLogicAddress() {
		return logicAddress;
	}


	public void setLogicAddress(String logicAddress) {
		this.logicAddress = logicAddress;
	}


	public String getQym() {
		return qym;
	}


	public void setQym(String qym) {
		this.qym = qym;
	}


	public int getSize() {
		return size;
	}


	public void setSize(int size) {
		this.size = size;
	}


	public String getSrcAddr() {
		return srcAddr;
	}


	public void setSrcAddr(String srcAddr) {
		this.srcAddr = srcAddr;
	}


	public Date getTime() {
		return time;
	}


	public void setTime(Date time) {
		this.time = time;
	}


	public String getTxfs() {
		return txfs;
	}


	public void setTxfs(String txfs) {
		this.txfs = txfs;
	}


	public String toString() {
		return "rtua="+logicAddress + ",message="+body;
	}
}
