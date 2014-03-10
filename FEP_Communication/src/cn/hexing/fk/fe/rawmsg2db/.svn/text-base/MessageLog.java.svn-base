package cn.hexing.fk.fe.rawmsg2db;

import java.io.Serializable;
import java.util.Date;

/**
 * 终端上行/下行消息表映射类
 */
public class MessageLog implements Serializable{
	private static final long serialVersionUID = 879756271368369058L;
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
    /** 处理结果：0成功；1失败 */
    private String result;
    
    private String fwqm;		//服务器名

    
	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		//限制报文入库长度，避免保存出错，限制长度与数据库表字段长度一致
		if (body.length()>=4000)
			body=body.substring(0,3999);
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


	public String getResult() {
		return result;
	}


	public void setResult(String result) {
		this.result = result;
	}

	public String getFwqm() {
		return fwqm;
	}

	public void setFwqm(String fwqm) {
		this.fwqm = fwqm;
	}
}
