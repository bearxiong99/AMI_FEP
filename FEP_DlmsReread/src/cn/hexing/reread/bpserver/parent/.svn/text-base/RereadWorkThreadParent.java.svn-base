package cn.hexing.reread.bpserver.parent;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.quartz.CronExpression;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;

import cn.hexing.fk.utils.CalendarUtil;
import cn.hexing.reread.model.MasterReread;
import cn.hexing.reread.model.RereadStrategy;
import cn.hexing.reread.service.LoadDatasService;
/**
 * 漏点补召主程序通用父类
 * @ClassName:RereadWorkThreadParent
 * @Description:TODO
 * @author kexl
 * @date 2012-12-11 上午10:10:07
 *
 */
public abstract class RereadWorkThreadParent  extends Thread{
	private static final Logger log = Logger.getLogger(RereadWorkThreadParent.class);
	
	public void run() {
		log.info("work running:"+this.getName());
		while( !getMainClassState().isStopping() && !getMainClassState().isStopped() ){
			doMasterRereadTask(); //注册主站补召任务
			refreshRereadStrategy();//补召策略同步
			
			int interval = 1;
			try{interval = Integer.parseInt(getMasterRereadInterval());}catch (Exception e) {log.error("system parameter[masterRereadInterval] parse error!" ,e); }
			try {
				Thread.sleep(interval*1000);
			} catch (InterruptedException e) {
				log.warn(e);
			}
		}
	}
	private void doMasterRereadTask(){
		try{
			List<MasterReread> masterRereads = getService().getMasterReread();
			log.info("Get " + masterRereads.size() + " records of master reread。");
			for(MasterReread masterReread : masterRereads){
				String templateId = masterReread.getTaskTemplateID();
				String time = masterReread.getZxsj();
				Calendar startTime = CalendarUtil.parse(time);
				Date createTime = masterReread.getCreateTime();
				String rwlx = masterReread.getRwlx();
				String jobKey = "masterReread_job" + "_"+rwlx+"_"+createTime+"_"+templateId;
				String jobGroupKey = "masterReread_jGroup" + "_"+rwlx+"_"+createTime+"_"+templateId;
				String triggerKey = "masterReread_trigger" + "_"+rwlx+"_"+createTime+"_"+templateId;
				String triggerGroupKey = "masterReread_tGroup" + "_"+rwlx+"_"+createTime+"_"+templateId;
				try {
					SimpleTrigger trigger = (SimpleTrigger)getScheduler().getTrigger(triggerKey,triggerGroupKey); 
					if(trigger!=null){ //已存在，修改触发时间
						getScheduler().deleteJob(jobKey, jobGroupKey);
						trigger.setStartTime(startTime.getTime());
					}else{
						trigger = new SimpleTrigger(triggerKey, triggerGroupKey, startTime.getTime());
					}
					JobDetail job = new JobDetail(jobKey, jobGroupKey, MasterRereadTaskProcessor.class);
					job.getJobDataMap().put(MasterRereadTaskProcessor.PARAMNAME_MASTERREREAD, masterReread);
					job.getJobDataMap().put(MasterRereadTaskProcessor.PARAMNAME_DLMSITEMRELATED, this.getDlmsItemRelated());  
					job.getJobDataMap().put(MasterRereadTaskProcessor.PARAMNAME_SERVICE, this.getService());  
					job.getJobDataMap().put(MasterRereadTaskProcessor.PARAMNAME_READER, this.getRereader()); 
					getScheduler().scheduleJob(job, trigger);
					log.info("scheduler add master reread task,task=" + masterReread);
					getService().setMasterRereadSuccess(templateId, createTime, rwlx);
				} catch (SchedulerException e) {
					log.error("Master reread job excute failed!" ,e);
					getService().setMasterRereadSuccess(templateId, createTime, "2" , rwlx);//state:1-成功， 2-失败

				}
			}
		}catch(Exception exp){
			log.error("Master reread process excute error!", exp);
		}
	}
	private void refreshRereadStrategy(){
		List<RereadStrategy>  strategys = this.getService().getRereadStrategy();
		for(RereadStrategy strategy:strategys){
			String templateId = strategy.getTaskTemplateID();
			String rwlx = strategy.getRwlx();
			String xgbj = strategy.getXgbj();
			if("1".equals(xgbj)){ //重新注册补召策略
				try {
					this.deleteJobFromScheduler(templateId, rwlx);
					String[] corns = strategy.getCron().split(";");
					boolean flag = true;
					String keyPre = templateId + "_" + rwlx;
					RereadMainParent.keySuffixMap.put(keyPre, new ArrayList<String>());
					for(String corn : corns){
						if("".equals(corn)) continue;
						String suffix = UUID.randomUUID().toString();
						RereadMainParent.keySuffixMap.get(keyPre).add(suffix);
						//2、创建JobDetail实例
						JobDetail jobDetail = new JobDetail(RereadMainParent.getJobKey(suffix ,keyPre), RereadMainParent.getJobGroupKey(suffix ,keyPre), getProcessor());
						jobDetail.getJobDataMap().put(RereadMainParent.PARAMNAME_STRATEGY, strategy);  
						jobDetail.getJobDataMap().put(RereadMainParent.PARAMNAME_SERVICE, getService());  
						jobDetail.getJobDataMap().put(RereadMainParent.PARAMNAME_READER, getRereader());  
						jobDetail.getJobDataMap().put(RereadMainParent.PARAMNAME_DLMSITEMRELATED, getDlmsItemRelated());
						//3、创建触发器
						CronTrigger cronTrigger = new CronTrigger(RereadMainParent.getTriggerKey(suffix ,keyPre), RereadMainParent.getTriggerGroupKey(suffix ,keyPre));//①-1：创建CronTrigger，指定组及名称
						CronExpression cexp;
						try {
							cexp = new CronExpression(corn);//①-2：定义Cron表达式
							cronTrigger.setCronExpression(cexp);//①-3：设置Cron表达式
							//4、注册Job和Trigger
							getScheduler().scheduleJob(jobDetail, cronTrigger);
						} catch (ParseException e) {
							log.error(corn + " cron is error!",e);
							flag = false;
							break;
						}
					}
					if(flag){
						log.info("Refresh RereadStrategy Success! strategy = " + strategy);
						getService().setRereadStrategyXgbj("0", templateId, rwlx);//设置任务xgbj为0
					}else{
						RereadMainParent.keySuffixMap.remove(keyPre);
						log.error("Refresh RereadStrategy Failed!" + strategy + " is error!");
					}
				} catch (Exception e) {
					log.error("Refresh RereadStrategy Failed! strategy = " + strategy , e);
				}
			}else if("2".equals(strategy.getXgbj())){ //删除补召策略
				try{
					this.deleteJobFromScheduler(templateId, rwlx);
					log.info("Delete RereadStrategy Success! strategy = " + strategy);
				}catch (Exception e) {
					log.error("Delete RereadStrategy Failed! strategy = " + strategy , e);
				}
			}else{
				
			}
		}
	}
	
	private void deleteJobFromScheduler(String templateId , String rwlx) throws SchedulerException{
		String keyPre = templateId + "_" + rwlx;
		List<String> suffixs = RereadMainParent.keySuffixMap.get(keyPre);
		if(suffixs!=null){
			for(String suffix : suffixs){
				getScheduler().pauseTrigger(RereadMainParent.getTriggerKey(suffix ,keyPre),RereadMainParent.getTriggerGroupKey(suffix ,keyPre));//停止触发器  
				getScheduler().unscheduleJob(RereadMainParent.getTriggerKey(suffix ,keyPre),RereadMainParent.getTriggerGroupKey(suffix ,keyPre));//移除触发器  
				getScheduler().deleteJob(RereadMainParent.getJobKey(suffix ,keyPre), RereadMainParent.getJobGroupKey(suffix ,keyPre));//删除任务
			}
		}
	}
	protected abstract cn.hexing.fk.utils.State getMainClassState();
	
	protected abstract LoadDatasService getService();
	
	protected abstract Rereader getRereader();
	
	protected abstract Scheduler getScheduler();
	
	protected abstract Class<?> getProcessor();
	/**
	 * DLMS终端、国网集中器需要覆盖该方法
	 * @return
	 */
	protected Map<String,String> getDlmsItemRelated(){ 
		return null;
	}
	
	protected abstract String getMasterRereadInterval();
}
