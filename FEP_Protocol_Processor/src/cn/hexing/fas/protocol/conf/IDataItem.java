package cn.hexing.fas.protocol.conf;

import java.util.List;


/**
 *@filename	IDataItem.java
 *TODO
 */
public interface IDataItem {	
	/**
	 * ���ض�Ӧ��׼���ݼ����������
	 */
	public abstract List getStandardDatas();
	
	/**
	 * ����ת��Ϊ��׼���ݼ��Ĺ�����key
	 */
	public abstract String getSdRobot();
	
	/**
	 * ��׼�������Ƿ�����ڴ���������
	 * @param dataid	��׼���ݼ���������id
	 */
	public abstract boolean isMe(String dataid);
}
