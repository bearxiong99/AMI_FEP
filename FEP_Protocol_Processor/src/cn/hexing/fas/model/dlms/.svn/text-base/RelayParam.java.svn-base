/**
 * 
 */
package cn.hexing.fas.model.dlms;

import java.io.Serializable;

/**
 * @author Bao Hongwei
 *
 */
public class RelayParam implements Serializable {
	private static final long serialVersionUID = 231920839031049626L;
	//Attributes definition for DC
	private String dcLogicalAddress = null; //Data concentrator logical address
    /** DC protocol type 集中器规约类型 */
    private String protocol = "gw";
    private String password = "00000000000000000000000000000000";
    
	//Attributes definition related to packet relay.
    /** meter measure point number */
    private int measurePoint = 0;
    /** 终端通信端口号 */
	private int port = 31;		//1 : 485;  31 for PLC. Used by Data concentrator
	/** Transparent forward control-word */
	private String forwardControlWord = "4B";
	/** 透明转发接收等待报文超时时间 */
	private String msgTimeout = "85";
	/** 透明转发接收等待字节超时时间 */
	private String byteTimeout = "64";
	
	public final String getDcLogicalAddress() {
		return dcLogicalAddress;
	}
	public final void setDcLogicalAddress(String dcLogicalAddress) {
		this.dcLogicalAddress = dcLogicalAddress;
	}
	public final String getProtocol() {
		return protocol;
	}
	public final void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	public final int getPort() {
		return port;
	}
	public final void setPort(int port) {
		this.port = port;
	}
	public final String getForwardControlWord() {
		return forwardControlWord;
	}
	public final void setForwardControlWord(String forwardControlWord) {
		this.forwardControlWord = forwardControlWord;
	}
	public final String getMsgTimeout() {
		return msgTimeout;
	}
	public final void setMsgTimeout(String msgTimeout) {
		this.msgTimeout = msgTimeout;
	}
	public final String getByteTimeout() {
		return byteTimeout;
	}
	public final void setByteTimeout(String byteTimeout) {
		this.byteTimeout = byteTimeout;
	}
	public final int getMeasurePoint() {
		return measurePoint;
	}
	public final void setMeasurePoint(int measurePoint) {
		this.measurePoint = measurePoint;
	}
	public final String getPassword() {
		return password;
	}
	public final void setPassword(String password) {
		this.password = password;
	}
}
