package cn.hexing.fas.model.handheld;

/**
 * 
 * @author gaoll
 *
 * @time 2013-5-6 下午03:20:15
 *
 * @info 发送给掌机的请求。   根据这个请求，组帧。
 */
public class HandHeldRequest {

	
	private String meterId ;
	
	private byte cField=0x08;
	
	private byte ciField ; //0=ACK,1=NACK,2=BILLING,3=Instantaneous
	
	private byte packetNum=0x00;
	
	private String value = "";

	public String getMeterId() {
		return meterId;
	}

	public void setMeterId(String meterId) {
		this.meterId = meterId;
	}

	public byte getcField() {
		return cField;
	}

	public void setcField(byte cField) {
		this.cField = cField;
	}

	public byte getCiField() {
		return ciField;
	}

	public void setCiField(byte ciField) {
		this.ciField = ciField;
	}

	public byte getPacketNum() {
		return packetNum;
	}

	public void setPacketNum(byte packetNum) {
		this.packetNum = packetNum;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	
}
