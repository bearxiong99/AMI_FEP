package com.hx.ansi.ansiElements.ansiElements.dataTable;

import com.hx.ansi.ansiElements.ansiElements.Table;

/**
 * Table 13 �������Ʊ�
 * ������ز��������ã��ɶ�д��
 */
public class Table13 extends Table{
	//���ں��˱�ơ�ǰ�˸��ֽ�Ϊ0�����������ֽڶ������£�
	private byte  slipCycle;//����ʽ��������  1�ֽ�
	private byte  silpNumber;//������ 1�ֽ�
	private byte  intervalCycle;//����ʽ�������� 2���ֽ�
	
	
	/**
	 * ��ȡ����ʽ��������������
	 * @return
	 */
	public byte getSlipCycle() {
		return slipCycle;
	}
	/**
	 * ���û���ʽ��������������
	 * @param slipCycle
	 */
	public void setSlipCycle(byte slipCycle) {
		this.slipCycle = slipCycle;
	}
	/**
	 * ��ȡ����ʽ���������Ļ�����
	 * @return
	 */
	public byte getSilpNumber() {
		return silpNumber;
	}
	/**
	 * ���û���ʽ���������Ļ�����
	 * @param silpNumber
	 */
	public void setSilpNumber(byte silpNumber) {
		this.silpNumber = silpNumber;
	}
	/**
	 * ��ȡ����ʽ������������������
	 * @return
	 */
	public byte getIntervalCycle() {
		return intervalCycle;
	}
	/**
	 * ��������ʽ������������������
	 * @param intervalCycle
	 */
	public void setIntervalCycle(byte intervalCycle) {
		this.intervalCycle = intervalCycle;
	}
	@Override
	public void decode() {
		// TODO Auto-generated method stub
		
	}
	public void decode(String data){
		
		
		
	}
	@Override
	public void encode() {
		// TODO Auto-generated method stub
		
	}
	
}
