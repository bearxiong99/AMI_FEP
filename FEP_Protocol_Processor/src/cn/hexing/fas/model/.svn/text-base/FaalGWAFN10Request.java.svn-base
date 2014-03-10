package cn.hexing.fas.model;

/**
 * 国网AFN=10命令请求类
 */
public class FaalGWAFN10Request extends FaalRequest {

    private static final long serialVersionUID = -462982183058685616L; 
    /** 下行发送时间 */
    private String tpSendTime;
    /** 下行发送传输超时时间 */
    private int tpTimeout;
    /** 终端通信端口号 */
    private int port=2;
    /** 透明转发通信控制字 */
    private String kzz="01101011";
    /** 透明转发接收等待报文超时时间 */
    private String msgTimeout="133";
    /** 透明转发接收等待字节超时时间 */
    private int byteTimeout=100;
    /** 指定表计规约 */
    private String fixProto;	
    /** 指定表计地址 */
    private String fixAddre;	
    /** 是否为广播命令 */
    private boolean broadcast = false;
    /** 表计广播地址 */
    private String broadcastAddress;
    /** 加密信息 */
    private String endata;
    /** 转发类型:F1透明转发;F9转发主站直接对电表的抄读数据命令的应答  F10转发主站直接对电表的遥控跳闸允许和闸命令*/
    private String transmitType;
    
    public FaalGWAFN10Request() {
    }
	public String getTpSendTime() {
		return tpSendTime;
	}
	public void setTpSendTime(String tpSendTime) {
		this.tpSendTime = tpSendTime;
	}
	public int getTpTimeout() {
		return tpTimeout;
	}
	public void setTpTimeout(int tpTimeout) {
		this.tpTimeout = tpTimeout;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getKzz() {
		return kzz;
	}
	public void setKzz(String kzz) {
		this.kzz = kzz;
	}
	public String getMsgTimeout() {
		return msgTimeout;
	}
	public void setMsgTimeout(String msgTimeout) {
		this.msgTimeout = msgTimeout;
	}
	public int getByteTimeout() {
		return byteTimeout;
	}
	public void setByteTimeout(int byteTimeout) {
		this.byteTimeout = byteTimeout;
	}
	public String getFixProto() {
		return fixProto;
	}
	public void setFixProto(String fixProto) {
		this.fixProto = fixProto;
	}
	public String getFixAddre() {
		return fixAddre;
	}
	public void setFixAddre(String fixAddre) {
		this.fixAddre = fixAddre;
	}
	public String getBroadcastAddress() {
		return broadcastAddress;
	}
	public void setBroadcastAddress(String broadcastAddress) {
		this.broadcastAddress = broadcastAddress;
	}
	public boolean getBroadcast() {
		return broadcast;
	}
	public void setBroadcast(boolean broadcast) {
		this.broadcast = broadcast;
	}
	public String getEndata() {
		return endata;
	}
	public void setEndata(String endata) {
		this.endata = endata;
	}
	public String getTransmitType() {
		return transmitType;
	}
	public void setTransmitType(String transmitType) {
		this.transmitType = transmitType;
	}

	
    
}