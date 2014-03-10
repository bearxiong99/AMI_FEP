/**
 * 终端工作状态（类似终端工况）
 */
package cn.hexing.fk.fe.cluster;

import java.io.Serializable;

/**
 * 用于批量非实时同步，保持前置机的终端工况基本同步.
 */
public class RtuWorkStateItem implements Serializable{
	private static final long serialVersionUID = 2647288886023712747L;
	public static final byte TXFS_SMS = 1;
	public static final byte TXFS_GPRS = 2;
	public static final byte TXFS_ETHERNET = 4;
	public static final byte TXFS_RADIO = 8;
	public static final byte FUNC_DOWN_REQ = 0;
	public static final byte FUNC_HEART = 1;		//心跳报文
	public static final byte FUNC_LOGIN = 2;		//登录
	public static final byte FUNC_GW_LOGIN=12; //国网登陆
	public static final byte FUNC_TASK = 3;			//任务报文
	public static final byte FUNC_ALARM = 4;		//告警
	public static final byte FUNC_REPLY = 5;		//终端应答报文
	public static final byte FUNC_GW_HEART = 6;		//心跳报文
//	public static final byte FUNC_GW_TASK = 7;		//任务报文
	public static final byte FUNC_GW_NEED_CFM = 8;	//国网终端，需要确认
	public static final byte FUNC_DOWN_CFM = 9;
	public static final byte FUNC_DLMS_HEART=10; //DLMS心跳报文
	public static final byte FUNC_DLMS_AA=11;//DLMS认证报文，当做登陆帧处理
	public static final byte FUNC_DLMS_NEED_CFM=13;//DLMS需要确认
	
	private String rtua = null;
	private byte txfs = TXFS_GPRS;
	private byte func = FUNC_HEART;
	private int len = 0;			//报文长度
	private long ioTime = 0;		//通信时间: (最近gprs上行时间,最近SMS上行时间)
	private String lastCommunicationIp;
	
	
	public boolean isUp() {
		return func != FUNC_DOWN_REQ && func != FUNC_DOWN_CFM ;
	}
	
	public boolean isDownReq(){
		return func == FUNC_DOWN_REQ;
	}
	
	public byte getTxfs() {
		return txfs;
	}
	
	public void setTxfs(byte txfs) {
		this.txfs = txfs;
	}
	
	public byte getFunc() {
		return func;
	}
	
	public void setFunc(byte func) {
		this.func = func;
	}
	
	public int getLen() {
		return len;
	}
	
	public void setLen(int len) {
		this.len = len;
	}
	
	public long getIoTime() {
		return ioTime;
	}
	
	public void setIoTime(long ioTime) {
		this.ioTime = ioTime;
	}

	public String getRtua() {
		return rtua;
	}

	public void setRtua(String logicalAddr) {
		this.rtua = logicalAddr;
	}

	public String getLastCommunicationIp() {
		return lastCommunicationIp;
	}

	public void setLastCommunicationIp(String lastCommunicationIp) {
		this.lastCommunicationIp = lastCommunicationIp;
	}

}
