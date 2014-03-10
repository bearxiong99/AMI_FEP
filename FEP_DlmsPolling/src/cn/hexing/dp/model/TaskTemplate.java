package cn.hexing.dp.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import cn.hexing.util.CalendarUtil;

public class TaskTemplate {
	private static final Logger log = Logger.getLogger(TaskTemplate.class);
	
	private static int BeforeTime = -60;
	
	static{
		String taskTime=System.getProperty("task.startTime");
		if(taskTime!=null && !"".equals(taskTime)){
			BeforeTime=-Integer.parseInt(taskTime);
		}
	}
	
	private String protocolType;
	
	 /** 任务模版ID */
    private String taskTemplateID;
    /** 采样开始基准时间  */
    private int sampleStartTime;
    /** 采样开始基准时间 单位 02：分，03：时，04：日，05：月*/
    private String sampleStartTimeUnit;
    /** 采样间隔时间 */
    private int sampleInterval;
    /** 采样间隔时间单位 单位02：分，03：时，04：日，05：月*/
    private String sampleIntervalUnit;
    /** 发送时间列表 */
    private ArrayList<Long> taskSendTimeList=new ArrayList<Long>();
    /** 任务时间列表 */
    private Map<Long,Date> taskDateMap=new HashMap<Long,Date>();
    ///** 终端列表<发送时间,终端任务信息> */
    //private Map<Long,ArrayList<RtuTaskRequest>> rtuTasksMap=new HashMap<Long,ArrayList<RtuTaskRequest>> ();
    private ArrayList<RtuTask> rtuTaskList=new ArrayList<RtuTask>();
    
	public String getTaskTemplateID() {
		return taskTemplateID;
	}
	public void setTaskTemplateID(String taskTemplateID) {
		this.taskTemplateID = taskTemplateID;
	}
	public int getSampleStartTime() {
		return sampleStartTime;
	}
	public void setSampleStartTime(int sampleStartTime) {
		this.sampleStartTime = sampleStartTime;
	}
	public String getSampleStartTimeUnit() {
		return sampleStartTimeUnit;
	}
	public void setSampleStartTimeUnit(String sampleStartTimeUnit) {
		this.sampleStartTimeUnit = sampleStartTimeUnit;
	}
	public int getSampleInterval() {
		return sampleInterval;
	}
	public void setSampleInterval(int sampleInterval) {
		this.sampleInterval = sampleInterval;
	}
	public String getSampleIntervalUnit() {
		return sampleIntervalUnit;
	}
	public void setSampleIntervalUnit(String sampleIntervalUnit) {
		this.sampleIntervalUnit = sampleIntervalUnit;
	}
	public ArrayList<Long> getTaskSendTimeList() {
		return taskSendTimeList;
	}
	public void setTaskSendTimeList(ArrayList<Long> taskSendTimeList) {
		this.taskSendTimeList = taskSendTimeList;
	}

	public ArrayList<RtuTask> getRtuTaskList() {
		return rtuTaskList;
	}
	public void setRtuTaskList(ArrayList<RtuTask> rtuTaskList) {
		this.rtuTaskList = rtuTaskList;
	}
	public void createTaskDate(){
		try{
			if (sampleIntervalUnit!=null){
				taskSendTimeList.clear();
				taskDateMap.clear();
					Calendar date=CalendarUtil.getBeginOfToday();  	
					Date taskDate=new Date();
					int n=0;
					if (sampleIntervalUnit.equals("02")){//采样间隔时间单位：分钟
						n=24*60/sampleInterval;
					}
					else if(sampleIntervalUnit.equals("03")){//采样间隔时间单位：时
						n=24/sampleInterval;
					}
					else if(sampleIntervalUnit.equals("04")){//采样间隔时间单位：日
						n=1;
					}
					else if(sampleIntervalUnit.equals("05")){//采样间隔时间单位：月
						n=1;
					}
					for(int i=0;i<n;i++){
						if(i>=1){
							switch (Integer.parseInt(sampleIntervalUnit)){
				            case 2: date.add(Calendar.MINUTE,sampleInterval);//累加分钟               
				              break;
				            case 3: date.add(Calendar.HOUR,sampleInterval);//累加小时 
				              break;
				            case 4: date.add(Calendar.DATE,sampleInterval);//累加日    
				              break;
				            case 5: date.add(Calendar.MONTH,sampleInterval);//累加月  
				              break;	
							}
						}	
						Calendar now = Calendar.getInstance();
						//为了避免每次开启的时候，把0点到当前的时间点再读一遍的情况出现，这里做一些处理.
						//当前时间减去15分钟，与任务时间比较，如果这个时间小于计算的时间，则不放在发送列表中.
						now.add(Calendar.MINUTE, BeforeTime);
						if(now.after(date)) continue;
						taskDate=date.getTime();
						switch (Integer.parseInt(sampleStartTimeUnit)){
				            case 2: date.add(Calendar.MINUTE,sampleStartTime);//累加分钟               
				              break;
				            case 3: date.add(Calendar.HOUR,sampleStartTime);//累加小时 
				              break;
				            case 4: date.add(Calendar.DATE,sampleStartTime);//累加日    
				              break;
				            case 5: date.add(Calendar.MONTH,sampleStartTime);//累加月  
				              break;
						} 
						
						taskSendTimeList.add(date.getTimeInMillis());
						taskDateMap.put(date.getTimeInMillis(), taskDate);
						// 将累加的，再减去
						switch (Integer.parseInt(sampleStartTimeUnit)){
			            case 2: date.add(Calendar.MINUTE,-sampleStartTime);//累加分钟               
			              break;
			            case 3: date.add(Calendar.HOUR,-sampleStartTime);//累加小时 
			              break;
			            case 4: date.add(Calendar.DATE,-sampleStartTime);//累加日    
			              break;
			            case 5: date.add(Calendar.MONTH,-sampleStartTime);//累加月  
			              break;
					} 
					}					
			}
		}catch(Exception ex){
			log.error("createTaskDate error:"+ex.getLocalizedMessage());
		}
	}
	public Map<Long, Date> getTaskDateMap() {
		return taskDateMap;
	}
	public void setTaskDateMap(Map<Long, Date> taskDateMap) {
		this.taskDateMap = taskDateMap;
	}
	public void setProtocolType(String protocolType) {
		this.protocolType = protocolType;
	}
	public String getProtocolType() {
		return protocolType;
	}
	
}
