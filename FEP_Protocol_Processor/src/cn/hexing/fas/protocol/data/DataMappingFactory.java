package cn.hexing.fas.protocol.data;

import java.util.Hashtable;

/**
 * 数据集合工厂---创建不同规约的数据集合
 * @version 
 */
public class DataMappingFactory {
	private static Hashtable datamappings;
	private static Object lock=new Object();
	
	/**
	 * 工厂方法
	 * @param key
	 * @return
	 */
	public static IMapping createDataMapping(String key){
		synchronized(lock){
			if(datamappings==null){
				datamappings=new Hashtable();
			}
			if(!datamappings.containsKey(key)){			
				//创建数据集合
				datamappings.put(key,createMapping(key));				
			}
			return (IMapping)datamappings.get(key);
		}
	}
	
	/**
	 * 新建数据集合
	 * @param key
	 * @return
	 */
	private static IMapping createMapping(String key){
		if(key.equals("ZJ")){
			return new DataMappingZJ();
		}
		return null;
	}
}
