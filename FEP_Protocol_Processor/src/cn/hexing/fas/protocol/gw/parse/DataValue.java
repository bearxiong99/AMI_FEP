package cn.hexing.fas.protocol.gw.parse;

public class DataValue {
    /** 数据值 */
    private String value;
    /** 反馈的已处理数据长度:字符数而不是字节数 */
    private int len;
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public int getLen() {
		return len;
	}
	public void setLen(int len) {
		this.len = len;
	}
    
}
