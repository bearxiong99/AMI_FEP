package cn.hexing.fas.protocol.meter;

import cn.hexing.fas.protocol.data.DataItem;
import cn.hexing.fk.model.BizRtu;

public interface IMeterParser {
	/**
	 * 转换数据标识
	 * @param datakey  浙江规约测量点数据标识
	 * @return
	 */
	public String[] convertDataKey(String[] datakey);
	
	/**
	 * 组表规约数据召测帧
	 * @param datakey  表规约数据标识
	 * @param para	   其他参数	
	 * @return
	 */
	public byte[] constructor(String[] datakey,DataItem para);
	
	/**
	 * 解析表帧
	 * @param data
	 * @return
	 */
	public Object[] parser(byte[] data,int loc,int len);
	public Object[] parser(byte[] data,int loc,int len,BizRtu rtu);
	/**
	 * 解析表帧
	 * @param data
	 * @return
	 */
	public Object[] parser(String key,String data,String meteraddr);
	public String[] getMeter1Code(String[] codes);
	public String[] getMeter2Code(String[] codes);
}
