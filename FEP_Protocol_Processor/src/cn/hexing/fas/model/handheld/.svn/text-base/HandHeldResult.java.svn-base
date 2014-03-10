package cn.hexing.fas.model.handheld;

/**
 * 
 * @author gaoll
 *
 * @time 2013-5-3 下午03:40:13
 *
 * @info 掌机上送数据到主站 ,解析结果类
 */
public class HandHeldResult {
	
	//0=ACK, 1=NACK ,2=Billing Data, 3=Instantaneous
	private byte ci_filed;
	
	private byte c_field;
	
	//Value = 0 for current, 1 for previous month, NACK Code 1-3
	private byte packetNum;
	
	private String value;
	
	private String meterId;

	public byte getCi_filed() {
		return ci_filed;
	}

	public void setCi_filed(byte ci_filed) {
		this.ci_filed = ci_filed;
	}

	public byte getC_field() {
		return c_field;
	}

	public void setC_field(byte c_field) {
		this.c_field = c_field;
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

	@Override
	public String toString() {
		return this.value;
	}

	public String getMeterId() {
		return meterId;
	}

	public void setMeterId(String meterId) {
		this.meterId = meterId;
	}
	
	
}
