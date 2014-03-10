package cn.hexing.fas.protocol.conf;

import java.util.List;


/**
 *@filename	IDataItem.java
 *TODO
 */
public interface IDataItem {	
	/**
	 * 返回对应标准数据集的数据项集合
	 */
	public abstract List getStandardDatas();
	
	/**
	 * 返回转换为标准数据集的工具类key
	 */
	public abstract String getSdRobot();
	
	/**
	 * 标准数据项是否包含在此数据项中
	 * @param dataid	标准数据集中数据项id
	 */
	public abstract boolean isMe(String dataid);
}
