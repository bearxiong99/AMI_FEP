package cn.hexing.reread.utils;

public enum SystemType {
	NORMAL("0"), 
	METERBOX("1");
	
	private String value;
	private SystemType(String value){
		this.value = value;
	}
	public String value(){
		return this.value;
	}
	public static SystemType getByValue(String value){
		for(SystemType unit:SystemType.values()){
			if(unit.value().equals(value)){
				return unit;
			}
		}
		return null;
	}
}
