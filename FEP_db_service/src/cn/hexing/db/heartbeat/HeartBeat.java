package cn.hexing.db.heartbeat;

public class HeartBeat {
	private final static long FIRST_BEAT_DAY = Long.MAX_VALUE + 1;
	private String rtua;
	private String deptCode;
	private int columnIndex;
	private String valueOrigin;
	private long value;
	private int weekTag;
	private int weekOfYear;
	private String valueTime;
	
	public String getValueTime() {
		return valueTime;
	}

	public void setValueTime(String valueTime) {
		this.valueTime = valueTime;
	}

	public String getKey(){
		return rtua+weekTag;
	}
	
	public boolean isFirstOfDay(){
		return value == FIRST_BEAT_DAY;
	}
	public int getColumnIndex() {
		return columnIndex;
	}
	public void setColumnIndex(int columnIndex) {
		this.columnIndex = columnIndex;
	}
	public String getRtua() {
		return rtua;
	}
	public void setRtua(String rtua) {
		this.rtua = rtua;
	}
	public long getValue() {
		return value;
	}
	public void setValue(long value) {
		this.value = value;
	}
	public int getWeekTag() {
		return weekTag;
	}
	public void setWeekTag(int weekFlag) {
		this.weekTag = weekFlag;
	}
	public int getWeekOfYear() {
		return weekOfYear;
	}
	public void setWeekOfYear(int weekOfYear) {
		this.weekOfYear = weekOfYear;
	}
	public String getValueOrigin() {
		return valueOrigin;
	}
	public void setValueOrigin(String valueOrigin) {
		this.valueOrigin = valueOrigin;
	}

	public String getDeptCode() {
		return deptCode;
	}

	public void setDeptCode(String deptCode) {
		this.deptCode = deptCode;
	}
	
}
