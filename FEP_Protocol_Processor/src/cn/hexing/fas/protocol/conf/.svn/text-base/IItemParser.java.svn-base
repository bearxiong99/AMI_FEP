package cn.hexing.fas.protocol.conf;

import java.util.List;


/**
 *@filename	IItemParser.java
 *TODO
 */
public interface IItemParser {
	/**
	 * 解析
	 * @param data		数据域
	 * @param pos		解析开始位置
	 * @param para		参数
	 * @param result	结果集合(返回参数)
	 * @param no       路数
	 * @return			解析长度
	 */
	public abstract int parse(byte[] data,int pos,Object para,Long cmdId,List result);
	
	/**
	 * 组帧
	 * @param frame		帧数据域
	 * @param pos		开始存放位置
	 * @param para      参数
	 * @return          组帧长度
	 */
	public abstract int construct(byte[] frame,int pos,Object para,int no);
}
