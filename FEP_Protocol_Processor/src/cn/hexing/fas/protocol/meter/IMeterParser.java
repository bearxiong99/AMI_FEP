package cn.hexing.fas.protocol.meter;

import cn.hexing.fas.protocol.data.DataItem;
import cn.hexing.fk.model.BizRtu;

public interface IMeterParser {
	/**
	 * ת�����ݱ�ʶ
	 * @param datakey  �㽭��Լ���������ݱ�ʶ
	 * @return
	 */
	public String[] convertDataKey(String[] datakey);
	
	/**
	 * ����Լ�����ٲ�֡
	 * @param datakey  ���Լ���ݱ�ʶ
	 * @param para	   ��������	
	 * @return
	 */
	public byte[] constructor(String[] datakey,DataItem para);
	
	/**
	 * ������֡
	 * @param data
	 * @return
	 */
	public Object[] parser(byte[] data,int loc,int len);
	public Object[] parser(byte[] data,int loc,int len,BizRtu rtu);
	/**
	 * ������֡
	 * @param data
	 * @return
	 */
	public Object[] parser(String key,String data,String meteraddr);
	public String[] getMeter1Code(String[] codes);
	public String[] getMeter2Code(String[] codes);
}
