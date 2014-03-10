package cn.hexing.fk.bp.ansi.masterDbService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.Flat3Map;
import org.apache.log4j.Logger;

import cn.hexing.db.batch.AsyncService;
import cn.hexing.db.bizprocess.MasterDbService;
import cn.hexing.db.managertu.ManageRtu;
import cn.hexing.fas.model.AnsiRequest;
import cn.hexing.fas.model.RtuData;
import cn.hexing.fas.model.RtuDataItem;
import cn.hexing.fk.bp.processor.TaskMessageHandler;
import cn.hexing.fk.model.MeasuredPoint;
import cn.hexing.fk.model.RtuManage;
import cn.hexing.fk.model.RtuTask;
import cn.hexing.fk.model.TaskTemplate;
import cn.hexing.fk.utils.Counter;
import cn.hexing.fk.utils.StringUtil;

import com.hx.ansi.ansiElements.AnsiContext;
import com.hx.ansi.ansiElements.AnsiDataItem;
import com.hx.ansi.ansiElements.ansiElements.Table;
import com.hx.ansi.ansiElements.ansiElements.MTtable.Table2050;
import com.hx.ansi.ansiElements.ansiElements.loadTable.Table64;
import com.hx.ansi.ansiElements.ansiElements.registerTable.Table26;
import com.hx.ansi.model.AnsiMeterRtu;
import com.hx.ansi.parse.AnsiDataSwitch;
//import com.sun.xml.internal.bind.v2.model.core.Ref;

/** 
 * @Description  xxxxx
 * @author  Rolinbor
 * @Copyright 2013 hexing Inc. All rights reserved
 * @time：2013-6-5 下午04:44:43
 * @version 1.0 
 */

public class AnsiTaskMessage {
	
	private static final Logger log = Logger.getLogger(AnsiTaskMessage.class);

	
	private static final AnsiTaskMessage instance=new AnsiTaskMessage();
	public static final  AnsiTaskMessage getInstance(){return instance;}
	
	private AnsiTaskMessage(){}
	private Counter taskCounter=new Counter(5000,"AnsiTaskMessage");
	private AsyncService service;
	private MasterDbService masterDbService;
	
	
	/**
	 *	处理负荷任务数据
	 * @param request
	 * @param table
	 */
	public void saveLoadTask(AnsiContext context,AnsiRequest request,String userData,Table table){
			AnsiDataItem[] dataItem=request.getDataItem();
//			Date date=null;
//			//modify by fangjianming on 2013-10-10,dataitem[0] get time of latest block
//			//String sdate=dataItem[1].resultData.substring(0, 10);
//			String sdate=dataItem[0].resultData.substring(0, 10);
//			sdate=AnsiDataSwitch.hexToString(sdate);
//			SimpleDateFormat sdf=new SimpleDateFormat("yyMMddHHmm");
//			try {
//				date=sdf.parse(sdate);
//			} catch (ParseException e) {
//				log.error(StringUtil.getExceptionDetailInfo(e));
//			}
			Table64 table64 = (Table64) table;    
			String logicAddress = request.getMeterId();
			String taskNo = (String) request.getAppendParam("taskNo");
			
			//added by fangjianming on 2013-10-11,to calculate the offset
			Date startTime=request.getDataItem()[request.loadTime+1].startTime;
			Date endTime=request.getDataItem()[request.loadTime+1].endTime;
			int sampleInterval=(Integer)request.getAppendParam("sampleInterval");
			String sampleIntervalUnit=(String)request.getAppendParam("sampleIntervalUnit");
			
			AnsiMeterRtu bizRtu=RtuManage.getInstance().getAnsiMeterRtu(logicAddress);
			if(bizRtu==null){
				bizRtu=ManageRtu.getInstance().refreshAnsiMeterRtu(logicAddress);
			}
			TaskMessageHandler taskHandler = new TaskMessageHandler();
			TaskTemplate taskTemp = bizRtu.getTaskTemplate(taskNo);
			List<String> datacodes=taskTemp.getDataCodes();
			int  count =getCount(context);//每一个数据项数据的长度
			int pointLen=4*context.table61.NBR_CHNS_SETx*2;
			List <RtuData> tasks=new ArrayList<RtuData>();
			//modify by fangjianming on 2013-10-10,dataitem[1] get the load profile data
			//String	data=dataItem[0].resultData;
			String	data=dataItem[request.loadTime].resultData;
			
			//modified by fangjianming on 2013-10-10,change the start time
			//Date taskDate=date;
			Date taskDate=startTime;
			Date taskDateClone=(Date)taskDate.clone();
			Date compareDate=(Date)taskDate.clone();
			Calendar calendar=Calendar.getInstance();
			Calendar calendar2=Calendar.getInstance();
			calendar.setTime(taskDateClone);
			calendar2.setTime(compareDate);
			
			
			int index=0;
			int flag=0;
			RtuTask rt = bizRtu.getRtuTask(taskNo);
			MeasuredPoint mp = bizRtu.getMeasuredPoint("0");
			if(request.loadTime==2)
			{
				String asdasds=taskDate.toString();
				asdasds="";
			}
			while(index<data.length()){
				String sdata="";
				double lg=0;
				
				if(flag>0){
					AddDateInteval(calendar,sampleInterval,sampleIntervalUnit);
				}
				Date date2=calendar.getTime();
				log.info(date2.toString()+" "+calendar.get(Calendar.DATE));
				if( calendar2.get(Calendar.DATE)==calendar.get(Calendar.DATE)){
					sdata=data.substring(index, pointLen+index);
					index=index+pointLen;
				}
				else {
					sdata=data.substring(index+10, pointLen+index+10);
					index=index+pointLen+10;
				}
				if(flag>0){
					AddDateInteval(calendar2,sampleInterval,sampleIntervalUnit);
				}
				
				RtuData task=new RtuData();
				for(int j=0;j<sdata.length()/8;j++){
					String ssdata="";
					String code="";
					String value="";
					RtuDataItem item=new RtuDataItem();
					code=context.table12.dataItemMap.get(context.table62.LP_SEL_SETx.get(j).index);
					lg=masterDbService.getANSIDimension(code);
					lg=Math.pow(10, lg);
					ssdata=sdata.substring(8*j, 8*j+8);//取出数据
					if(AnsiDataSwitch.isAllFF(ssdata, ssdata.length())){ssdata=ssdata.replace("F", "0");}
//					double dresult=Long.parseLong(ssdata, 16)*(context.table12.paramMap.get(context.table62.LP_SEL_SETx.get(j).index).multiplier)*lg;
					double dresult=Long.parseLong(ssdata, 16)*lg;
					value=String.valueOf(dresult);
					value=AnsiDataSwitch.getDouble(value, 2);
					if(datacodes.contains(code)){
//						item.setCode("00"+code);
						item.setCode(code);
						item.setValue(value);
						task.addDataList(item);
					}
				}
				task.setTaskNum(taskNo);
				task.setTime(calendar.getTime());
				taskHandler.dataSave(service, null, task, mp, null, rt, 1, bizRtu.getDeptCode());
				//tasks.add(task);
				flag++;
			}
	}
	
	public void AddDateInteval(Calendar calendar,int sampleInterval,String sampleIntervalUnit) {
//		switch (Integer.parseInt(sampleIntervalUnit)) {
//		case 2://minute
			calendar.add(Calendar.MINUTE, 15);
//			break;
//		case 3://hour
//			calendar.add(Calendar.HOUR, sampleInterval);
//			break;
//		case 4://day
//			calendar.add(Calendar.DATE, sampleInterval);
//			break;
//		case 5://year
//			calendar.add(Calendar.MONTH, sampleInterval);
//			break;
//		default:
//			break;
//		}
	}
	
	public Date SetTaskDataByIndex(Date startDate,int index) {
		
		Calendar calendar=Calendar.getInstance();
		calendar.setTime(startDate);
		calendar.add(Calendar.MINUTE, index*15);
		Date timeDate=calendar.getTime();
		return calendar.getTime();
	}
	
	/**
	 * 处理电量任务数据
	 * @param context
	 * @param request
	 * @param table
	 */
	public void saveEnergy(AnsiContext context,AnsiRequest request,Table table){
		if(table instanceof Table26){
			Table26 table26=(Table26)table;
			String logicAddress = request.getMeterId();
			String taskNo = (String) request.getAppendParam("taskNo");
			Date startTime=(Date)request.getAppendParam("startTime");
			Date endTime=(Date)request.getAppendParam("endTime");
			AnsiMeterRtu bizRtu=RtuManage.getInstance().getAnsiMeterRtu(logicAddress);
			if(bizRtu==null){
				bizRtu=ManageRtu.getInstance().refreshAnsiMeterRtu(logicAddress);
			}
			TaskMessageHandler taskHandler = new TaskMessageHandler();
			TaskTemplate taskTemp = bizRtu.getTaskTemplate(taskNo);
			List<String>  dataCodes=taskTemp.getDataCodes();
			RtuData taskData =new RtuData();
			for(int i=0;i<dataCodes.size();i++){
				if(table26.taskDataMap.containsKey(dataCodes.get(i))){
					RtuDataItem item=new RtuDataItem();
					item.setValue(table26.taskDataMap.get(dataCodes.get(i)));
//					item.setCode("01"+dataCodes.get(i));
					item.setCode(dataCodes.get(i));
					taskData.addDataList(item);
				}
				if(table26.taskDataMap.containsKey(dataCodes.get(i)+"00")){
					RtuDataItem item1=new RtuDataItem();
					item1.setValue(table26.taskDataMap.get(dataCodes.get(i)+"00"));
//					item1.setCode("01"+dataCodes.get(i)+"00");
					item1.setCode(dataCodes.get(i)+"00");
					taskData.addDataList(item1);
				}
				if(table26.taskDataMap.containsKey(dataCodes.get(i)+"01")){
					RtuDataItem item2=new RtuDataItem();
					item2.setValue(table26.taskDataMap.get(dataCodes.get(i)+"01"));
//					item2.setCode("01"+dataCodes.get(i)+"01");
					item2.setCode(dataCodes.get(i)+"01");
					taskData.addDataList(item2);
				}
			}
			taskData.setTaskNum(taskNo);
			taskData.setTime(table26.date);
			taskData.setTn("0");
			MeasuredPoint mp = bizRtu.getMeasuredPoint(taskData.getTn());
			RtuTask rt = bizRtu.getRtuTask(taskNo);
			taskHandler.dataSave(service, null, taskData, mp, null, rt, 1, bizRtu.getDeptCode());
		}
		else if(table instanceof Table2050){
			Table2050 table2050=(Table2050)table;
			String logicAddress = request.getMeterId();
			String taskNo = (String) request.getAppendParam("taskNo");
			Date startTime=(Date)request.getAppendParam("startTime");
			Date endTime=(Date)request.getAppendParam("endTime");
			AnsiMeterRtu bizRtu=RtuManage.getInstance().getAnsiMeterRtu(logicAddress);
			if(bizRtu==null){
				bizRtu=ManageRtu.getInstance().refreshAnsiMeterRtu(logicAddress);
			}
			TaskMessageHandler taskHandler = new TaskMessageHandler();
			TaskTemplate taskTemp = bizRtu.getTaskTemplate(taskNo);
			List<String>  dataCodes=taskTemp.getDataCodes();
			RtuData taskData =new RtuData();
			for(int i=0;i<dataCodes.size();i++){
				if(table2050.taskDataMap.containsKey(dataCodes.get(i))){
					RtuDataItem item=new RtuDataItem();
					item.setValue(table2050.taskDataMap.get(dataCodes.get(i)));
//					item.setCode("02"+dataCodes.get(i));
					item.setCode(dataCodes.get(i));
					taskData.addDataList(item);
				}
				if(table2050.taskDataMap.containsKey(dataCodes.get(i)+"00")){
					RtuDataItem item1=new RtuDataItem();
					item1.setValue(table2050.taskDataMap.get(dataCodes.get(i)+"00"));
//					item1.setCode("02"+dataCodes.get(i)+"00");
					item1.setCode(dataCodes.get(i)+"00");
					taskData.addDataList(item1);
				}
				if(table2050.taskDataMap.containsKey(dataCodes.get(i)+"01")){
					RtuDataItem item2=new RtuDataItem();
					item2.setValue(table2050.taskDataMap.get(dataCodes.get(i)+"01"));
					item2.setCode("02"+dataCodes.get(i)+"01");
					item2.setCode(dataCodes.get(i)+"01");
					taskData.addDataList(item2);
				}
			}
			taskData.setTaskNum(taskNo);
			taskData.setTime(table2050.date);
			taskData.setTn("0");
			MeasuredPoint mp = bizRtu.getMeasuredPoint(taskData.getTn());
			RtuTask rt = bizRtu.getRtuTask(taskNo);
			taskHandler.dataSave(service, null, taskData, mp, null, rt, 1, bizRtu.getDeptCode());
		}


	}
	
	
	
	/**
	 * 获取任务点
	 * @param taskTemp
	 * @param startTime
	 * @return
	 */
	private int getTaskPoint(TaskTemplate taskTemp,Date startTime){
		//计算任务点
		Map<Date,Integer> taskPointMap=new HashMap<Date,Integer>();
		Calendar calendar=Calendar.getInstance();
		SimpleDateFormat sdf=new SimpleDateFormat("yy-MM-dd");
		Date date = null;
		try {
			date=sdf.parse(sdf.format(startTime));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		calendar.setTime(date);
		int sampleInterval=taskTemp.getSampleInterval();
		String sampleIntervalUnit=taskTemp.getSampleIntervalUnit();
		if(sampleIntervalUnit.equals("02")){
			taskPointMap.clear();
			int point=24*60/sampleInterval;
			for(int i=0;i<point;i++){
				calendar.add(Calendar.MINUTE, +sampleInterval);
				taskPointMap.put(calendar.getTime(), i);
			}
		}
		else if(sampleIntervalUnit.equals("03")){
			taskPointMap.clear();
			int point =24/sampleInterval;
			for(int i=0;i<point;i++){
				calendar.add(Calendar.HOUR, +sampleInterval);
				taskPointMap.put(calendar.getTime(), i);
			}
		}
		//日和月暂时没用到
		else if(sampleIntervalUnit.equals("04")){
			calendar.add(Calendar.DATE, +sampleInterval);
		}
		else if(sampleIntervalUnit.equals("05")){
			calendar.add(Calendar.MONTH, +sampleInterval);
		}
		//请求传过来的时间是主站任务读取时间 ，不是终端冻结时间，所以这里需要处理一下。
//		Date taskDate=null;
//		switch(Integer.parseInt(taskTemp.getUploadIntervalUnit())){
//		case 2:
//			SimpleDateFormat sd=new SimpleDateFormat("yy-MM-dd HH");
//			try {
//				taskDate=sd.parse(sd.format(startTime));
//			} catch (ParseException e) {
//				log.error("Date format error"+e);
//			}
//			break;
//		case 3:
//			
//			
//			
//			break;
//		case 4:
//			break;
//		case 5:
//			break;
//		default:
//				log.error("error,can't find UploadIntervalUnit.");
//		
//		
//		}
		return taskPointMap.get(startTime);
	}
	
	/**
	 * 获取任务时间
	 * @param date
	 * @param startTime
	 * @return
	 */
	private Date getTaskDate(Date date,Date startTime){
		SimpleDateFormat sdf= new SimpleDateFormat("yyMMdd");
		boolean dateMatch=sdf.format(date).equals(sdf.format(startTime));
		if(!dateMatch){
			log.info("TaskDate is not match RequestDate,can't get right data.");
		}
		SimpleDateFormat df=new SimpleDateFormat("HHmmss");
		SimpleDateFormat sd=new SimpleDateFormat("yyMMddHHmmss");
		try {
			date=sd.parse(sdf.format(date)+df.format(startTime));
			
		} catch (ParseException e) {
			log.error(StringUtil.getExceptionDetailInfo(e));
		}
		return date;
	}
	/**
	 * 获取没一个数据项字节数
	 * @param context
	 * @return
	 */
	public int getCount(AnsiContext context){
		int offSet=0;
		//table0中有一些数据类型还不明确
		switch(context.table0.formatControl_3_NI_FMAT2){
		case 0:
			offSet=8;//FLOAT64
		case 1:
			offSet=4;//FLOAT32
		case 2:
			offSet=4;//FLOAT—CHAR12
		case 3:
			offSet=4;//FLOAT-CHAR6
		case 4:
			offSet=4;//INT32 /10000
		case 5:
			offSet=6;//BCD6
		case 6:
			offSet=4;//BCD4
		case 7:
			offSet=3;//INT24
		case 8:
			offSet=4;//INT32
		case 9:
			offSet=5;//INT40
		case 10:
			offSet=6;//INT48
		case 11:
			offSet=8;//BCD8
		case 12:
			offSet=4;//FLOAT-CHAR21
		default :
			offSet=4;//默认偏移4个字节
		}
		return offSet;
	}
	
	public AsyncService getService() {
		return service;
	}
	public void setService(AsyncService service) {
		this.service = service;
	}

	public MasterDbService getMasterDbService() {
		return masterDbService;
	}

	public void setMasterDbService(MasterDbService masterDbService) {
		this.masterDbService = masterDbService;
	}
	
}
