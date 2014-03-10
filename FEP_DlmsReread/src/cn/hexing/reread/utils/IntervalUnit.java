package cn.hexing.reread.utils;
/**
 * 采样间隔时间单位
 * @ClassName:IntervalUnit
 * @Description:TODO
 * @author kexl
 * @date 2012-9-24 上午10:24:02
 *
 */
public enum IntervalUnit {
	SECOND("01"), 
	MUNITE("02"),
	HOUR("03"),
	DAY("04"),
	MONTH("05");
	
	private String value;
	private IntervalUnit(String value){
		this.value = value;
	}
	public String value(){
		return this.value;
	}
	public static IntervalUnit getByValue(String value){
		for(IntervalUnit unit:IntervalUnit.values()){
			if(unit.value().equals(value)){
				return unit;
			}
		}
		return null;
	}
}
