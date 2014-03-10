package cn.hexing.fas.protocol.gw.parse;

public class DataTimeTag {
	/** 数据时间 */
    private String dataTime;
    /** 数据间隔 */
    private int dataDensity;
    /** 数据点数 */
    private int dataCount;
	public String getDataTime() {
		return dataTime;
	}
	public void setDataTime(String dataTime) {
		this.dataTime = dataTime;
	}
	public int getDataDensity() {
		return dataDensity;
	}
	public void setDataDensity(int dataDensity) {
		this.dataDensity = dataDensity;
	}
	public int getDataCount() {
		return dataCount;
	}
	public void setDataCount(int dataCount) {
		this.dataCount = dataCount;
	}
    
}
