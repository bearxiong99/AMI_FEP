package com.hx.ansi.ansiElements;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.hx.ansi.ansiElements.ansiElements.AnsiCommandResult;

/** 
 * @Description  xxxxx
 * @author  Rolinbor
 * @Copyright 2013 hexing Inc. All rights reserved
 * @time��2013-4-19 ����02:12:07
 * @version 1.0 
 */

public class AnsiDataItem implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1123654L;

	public String dataCode;//������
	public int dataType;//��������    0 -��������  
						//	    1-��������   ��������  
 						//		2-ʵʱ��
	public int tiers;//�� -0   ����1-1 ��������
	public long cmdId = -1L; //��վ���д�����id���������ݽ������������id���
	public String data;//���д�����
	public TierSwitch[] TierSwitches=null;
	public NonRecurrDate[] NonRecurrDates=null;
	public RecurrDate[]RecurrDates=null;
	public DailySchedule[]DailySchedules=null;
	public String resultData;//�����ն˷�������
	public String offset;//ƫ�ƶ�ȡ��ʱ���ƫ����
	public String count;//ƫ���ֽ���
	public Date date=null;
	public int index;//�ڶ�Ӧ���е�λ��
	public int length; //���ݳ���
	public List<AnsiCommandResult> commandResult;
	public AnsiTaskInf taskInf;
	
	//for load profile
	public Date startTime=null;
	public Date endTime=null;
	public boolean readLatestDate=false;
	public boolean readData=false;
}
