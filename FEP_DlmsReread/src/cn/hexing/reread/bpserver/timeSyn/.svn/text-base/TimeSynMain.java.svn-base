package cn.hexing.reread.bpserver.timeSyn;

import java.text.ParseException;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.quartz.CronExpression;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleTrigger;
import org.quartz.impl.StdSchedulerFactory;

import cn.hexing.fk.common.spi.IModule;
import cn.hexing.fk.common.spi.abstra.BaseModule;
import cn.hexing.fk.utils.CalendarUtil;
import cn.hexing.fk.utils.State;
import cn.hexing.reread.model.TimeSynStrategy;
import cn.hexing.reread.model.TimeSynTask;
import cn.hexing.reread.service.TimeSynService;
import cn.hexing.reread.utils.SystemType;

/**
 * @ClassName:DlmsRereadMain
 * @Description: 补召程序主程序，由Spring负责启动
 * 作用：
 * 	1、为每个任务模板启动一个定时补召任务
 * 	2、持续扫描是否有待完成的主站补召任务
 * @author kexl
 * @date 2012-9-24 上午10:29:35
 *
 */
public class TimeSynMain extends BaseModule{
	private static final Logger log = Logger.getLogger(TimeSynMain.class);
	
	private static Scheduler scheduler = getScheduler();
	//对象内部状态
	private volatile State state = new State();	
	private WorkThread work=null;
	//可配置属性
	private String name;	
	//数据库服务
	private TimeSynService service;
	//通讯服务
	private TimeSynReader reader;
	public static SystemType systemType = SystemType.NORMAL;//0-正常模式，1-表箱模式
	public String getName() {
		return name;
	}
	
	public boolean isActive() {
		return state.isActive();
	}
	
	private static Scheduler getScheduler() {
		SchedulerFactory sf = new StdSchedulerFactory();
		Scheduler tempscheduler = null;
		try {
			tempscheduler = sf.getScheduler();
		} catch (SchedulerException e) {
			log.error("Get scheduler error!" , e);
		}
		return tempscheduler;
	}
	public static String getJobKey(String dwdm , String rwlx){
		return getJobKey(dwdm, rwlx, "");
	}
	public static String getJobKey(String dwdm , String rwlx , String cron){
		return "job_"+rwlx+"_"+dwdm+"_" + cron;
	}
	public static String getJobGroupKey(String dwdm , String rwlx){
		return getJobGroupKey(dwdm, rwlx, "");
	}
	public static String getJobGroupKey(String dwdm , String rwlx, String cron){
		return "jGroup_"+rwlx+"_"+dwdm+"_" + cron;
	}
	public static String getTriggerKey(String dwdm , String rwlx){
		return getTriggerKey(dwdm, rwlx, "");
	}
	public static String getTriggerKey(String dwdm , String rwlx, String cron){
		return "trigger_"+rwlx+"_"+dwdm+"_" + cron;
	}
	public static String getTriggerGroupKey(String dwdm , String rwlx){
		return getTriggerGroupKey(dwdm, rwlx, "");
	}
	public static String getTriggerGroupKey(String dwdm , String rwlx, String cron){
		return "tGroup_"+rwlx+"_"+dwdm+"_" + cron;
	}
	
	public boolean start() {
		if("true".equals(System.getProperty("isMeterBox"))){
			TimeSynMain.systemType = SystemType.METERBOX;
		}
		if( !state.isStopped() )
			return false;
		state = State.STARTING;
		try {
			//1、获取Quartz调度程序实例
			if(scheduler == null || scheduler.isShutdown()){
				SchedulerFactory sf = new StdSchedulerFactory();        
				scheduler = sf.getScheduler();
			}
			/*** 自动对时策略加载 **/
			List<TimeSynStrategy> strategys = service.getTimeSynStrategy();
			for(TimeSynStrategy strategy : strategys){
				//启动时，对所有状态(0-初始状态，1-新增、修改，2-删除)都进行处理
				regTimeSynStrategy(strategy, service, reader ,scheduler );
			}
			//5、启动调度器
			scheduler.start();
			/***********定时轮询主站设置的对时任务**************/
			work=new WorkThread();
			work.start();
			state = State.RUNNING;
			log.info("thread("+name+") start...");
			return true;
		} catch (SchedulerException e) {
			log.error(e);
			return false;
		}
	}
	private static boolean regTimeSynStrategy(TimeSynStrategy strategy , TimeSynService service , TimeSynReader reader , Scheduler scheduler){
		String dwdm = strategy.getDwdm();
		String rwlx = strategy.getRwlx();
		String cron = strategy.getCron();
		//状态：0-初始状态，1-新增、修改，2-删除
		String triggerKey = getTriggerKey(dwdm, rwlx, "1".equals(rwlx)?cron:"");
		String triggerGroupKey = getTriggerGroupKey(dwdm, rwlx, "1".equals(rwlx)?cron:"");
		String jobKey = getJobKey(dwdm, rwlx, "1".equals(rwlx)?cron:"");
		String jobGroupKey = getJobGroupKey(dwdm, rwlx, "1".equals(rwlx)?cron:"");
		Class<?> processor = "1".equals(rwlx)?TimeSynRereadProcessor.class:TimeSynReadProcessor.class;
		if("0".equals(strategy.getXgbj()) || "1".equals(strategy.getXgbj())){ //重新注册补召策略
			try {
				CronTrigger trigger = (CronTrigger)getScheduler().getTrigger(triggerKey, triggerGroupKey); 
				if(trigger!=null){ //已存在，修改触发时间
					getScheduler().deleteJob(jobKey, jobGroupKey);
				}else{
					trigger = new CronTrigger(triggerKey, triggerGroupKey);
				}
				CronExpression cexp;
				try {
					cexp = new CronExpression(cron);//①-2：定义Cron表达式
					trigger.setCronExpression(cexp);//①-3：设置Cron表达式
					
					JobDetail jobDetail;
					jobDetail = new JobDetail(jobKey, jobGroupKey, processor);
					jobDetail.getJobDataMap().put(TimeSynReadProcessor.PARAMNAME_DWDM, dwdm);  
					jobDetail.getJobDataMap().put(TimeSynReadProcessor.PARAMNAME_SERVICE, service);  
					jobDetail.getJobDataMap().put(TimeSynReadProcessor.PARAMNAME_READER, reader); 
					
					scheduler.scheduleJob(jobDetail, trigger);
					log.info("Refresh TimeSynstrategy Success! strategy = " + strategy);
					if("1".equals(strategy.getXgbj())){
						//将状态修改为0
						service.setTimeSynStrategyState(dwdm, rwlx, cron, "0");
					}
					return true;
				} catch (ParseException e) {
					log.error(e);
				}
			} catch (SchedulerException e) {
				log.error(e);
			}
			log.error("Refresh TimeSynstrategy Failed! strategy = " + strategy);
		}else if("2".equals(strategy.getXgbj())){ //删除补召策略
			try{
				scheduler.pauseTrigger(triggerKey,triggerGroupKey);//停止触发器  
				scheduler.unscheduleJob(triggerKey,triggerGroupKey);//移除触发器  
				scheduler.deleteJob(jobKey, jobGroupKey);//删除任务
				service.deleteTimeSynstrategy(dwdm, rwlx, cron);
				log.info("Delete TimeSynStrategy Success! strategy = " + strategy);
				return true;
			}catch (Exception e) {
				log.error(e);
			}
			log.error("Delete TimeSynStrategy Failed! strategy = " + strategy );
		}else{
			log.error("The TimeSyn Strategy's XGBJ  is not supported! strategy = " + strategy);
		}
		return false;
	}
	private class WorkThread extends Thread{
		public WorkThread(){
		}
		public void run() {
			log.info("work running:"+this.getName());
			while( !TimeSynMain.this.state.isStopping() && !TimeSynMain.this.state.isStopped() ){
				try {
					Thread.sleep(5*60*1000);//5分钟执行一次查询
				} catch (InterruptedException e) {
					log.warn(e);
				}
				try{
					/*** 自动对时策略同步 **/
					List<TimeSynStrategy> strategys = service.getTimeSynStrategy();
					log.info("Get " + strategys.size() + " records of TimeSyn Strategy。");
					for(TimeSynStrategy strategy : strategys){
						//更新时，对状态1-新增、修改，2-删除进行处理
						if("1".equals(strategy.getXgbj()) || "2".equals(strategy.getXgbj())){
							regTimeSynStrategy(strategy, service, reader, scheduler);
						}
					}
				}catch(Exception exp){
					log.error("Refresh TimeSynStrategy  process excute error!", exp);
				}
				try{
					/*** 主站临时对时任务加载 **/
					List<TimeSynTask> tasks = service.getTimeSynTasks();
					log.info("Get " + tasks.size() + " records of Time Syn Task。");
					for(TimeSynTask task : tasks){
						try{
							if(regTimeSynTask(task))
								service.setTimeSynTaskSuccess(task.getDwdm(), task.getRwlx(), task.getZxsj());
						}catch (Exception e) {
							log.error("Master reread job excute failed!" ,e);
							continue;
						}
					}
				}catch(Exception exp){
					log.error("Refresh TimeSynTask  process excute error!", exp);
				}
			}
		}
		
		private boolean regTimeSynTask(TimeSynTask task){
			String taskType = task.getRwlx();
			if("0".equals(taskType)){  //召测任务
				String preJobKey = "timeSyn_ReadJob";
				String preJobGroupKey = "timeSyn_ReadJobGroup";
				String preTriggerKey = "timeSyn_ReadTrigger";
				String preTriggerGroup = "timeSyn_ReadTriggerGroup";
				
				String dwdm = task.getDwdm();
				String jobKey = preJobKey + dwdm;
				String jobGroupKey = preJobGroupKey + dwdm;
				String triggerKey = preTriggerKey + dwdm;
				String triggerGroupKey = preTriggerGroup + dwdm;
				String time = task.getZxsj();
				Calendar startTime = CalendarUtil.parse(time);
				try {
					SimpleTrigger trigger = (SimpleTrigger)scheduler.getTrigger(triggerKey,triggerGroupKey); 
					if(trigger!=null){ //已存在，修改触发时间
						scheduler.deleteJob(jobKey, jobGroupKey);
						trigger.setStartTime(startTime.getTime());
					}else{
						trigger = new SimpleTrigger(triggerKey, triggerGroupKey, startTime.getTime());
					}
					JobDetail job = new JobDetail(jobKey, jobGroupKey, TimeSynReadProcessor.class);
					job.getJobDataMap().put(TimeSynReadProcessor.PARAMNAME_DWDM, task.getDwdm());  
					job.getJobDataMap().put(TimeSynReadProcessor.PARAMNAME_SERVICE, service);  
					job.getJobDataMap().put(TimeSynReadProcessor.PARAMNAME_READER, reader); 
					scheduler.scheduleJob(job, trigger);
					log.info("scheduler add time syn read job,task=" + task);
				} catch (SchedulerException e) {
					log.error("Delete original task[dwdm=" +dwdm+ "] error!" , e);
					return false;
				}
				
			}else if("1".equals(taskType)){ 
				String preJobKey = "timeSyn_RereadJob";
				String preJobGroupKey = "timeSyn_RereadJobGroup";
				String preTriggerKey = "timeSyn_RereadTrigger";
				String preTriggerGroup = "timeSyn_RereadTriggerGroup";
				
				String dwdm = task.getDwdm();
				String time = task.getZxsj();
				String jobKey = preJobKey + dwdm + time;
				String jobGroupKey = preJobGroupKey + dwdm + time;
				String triggerKey = preTriggerKey + dwdm + time;
				String triggerGroupKey = preTriggerGroup + dwdm + time;
				Calendar startTime = CalendarUtil.parse(time);
				try {
					SimpleTrigger trigger = new SimpleTrigger(triggerKey, triggerGroupKey, startTime.getTime());
					JobDetail job = new JobDetail(jobKey, jobGroupKey, TimeSynRereadProcessor.class);
					job.getJobDataMap().put(TimeSynRereadProcessor.PARAMNAME_DWDM, task.getDwdm());  
					job.getJobDataMap().put(TimeSynRereadProcessor.PARAMNAME_SERVICE, service);  
					job.getJobDataMap().put(TimeSynRereadProcessor.PARAMNAME_READER, reader); 
					scheduler.scheduleJob(job, trigger);
					log.info("scheduler add time syn reread job,task=" + task);
				} catch (SchedulerException e) {
					log.error("Delete original task[dwdm=" +dwdm+ "] error!" , e);
					return false;
				}
			}else{
				log.error("The task[" + task + "] type is error!" );
				return false;
			}
			return true;
		}
	}
	
	public void stop() {
		state = State.STOPPING;
		state = State.STOPPED;
	}
	
	public String getModuleType() {
		return IModule.MODULE_TYPE_BP;
	}


	public TimeSynReader getReader() {
		return reader;
	}

	public void setReader(TimeSynReader reader) {
		this.reader = reader;
	}

	public void setName(String name) {
		this.name = name;
	}

	public TimeSynService getService() {
		return service;
	}

	public void setService(TimeSynService service) {
		this.service = service;
	}

}
