package cn.hexing.fas.protocol.conf;


/**
 *@filename	IDataSets.java
 *TODO
 */
public interface IDataSets {
	/**
	 * 取解析工具
	 * @param key	解析工具key	 
	 * @return
	 */
	public abstract IItemParser getParser(String key);
	
	/**
	 * 取本地数据标识
	 * @param key	标准数据标识
	 * @param para	参数	DataItem类
	 * @return
	 */
	public abstract String getLocal(String key,Object para);	
}
