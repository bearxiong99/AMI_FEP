package cn.hexing.fas.protocol.data;

import java.util.Hashtable;

public class DataItem {
	private Hashtable property;
	
	public DataItem(){
		property=new Hashtable();
	}
	
	public Hashtable getPropertys(){
		return property;
	}
	
	/**
	 * 取数据项属性
	 * @param key 常见的有：value（值） point（测量点） des（数据描述） datakey（数据标识）
	 * @return
	 */
	public Object getProperty(String key){
		if(property!=null){
			if(property.containsKey(key)){
				return property.get(key);
			}
		}
		return null;
	}
	
	public void addProperty(String key,Object value){
		try{
			if(property!=null){
				if(property.containsKey(key)){
					property.remove(key);					
				}
				property.put(key,value);
			}
		}catch(Exception e){
			//
		}
	}
	
	public String toString(){
		return (String)property.get("des");
	}
}
