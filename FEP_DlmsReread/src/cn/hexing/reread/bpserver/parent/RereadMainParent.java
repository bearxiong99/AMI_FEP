package cn.hexing.reread.bpserver.parent;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.quartz.CronExpression;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

import cn.hexing.fk.common.spi.IModule;
import cn.hexing.fk.common.spi.abstra.BaseModule;
import cn.hexing.fk.model.DlmsItemRelated;
import cn.hexing.fk.utils.State;
import cn.hexing.reread.model.RereadStrategy;
import cn.hexing.reread.service.LoadDatasService;

public abstract class RereadMainParent  extends BaseModule{
	public static String PARAMNAME_STRATEGY = "strategy";
	public static String PARAMNAME_SERVICE = "service";
	public static String PARAMNAME_READER = "reader";
	public static String PARAMNAME_DLMSITEMRELATED = "dlmsItemRelated";
	
	private static final Logger log = Logger.getLogger(RereadMainParent.class);
	//对象内部状态
	protected volatile State state = new State();	
	//可配置属性
	protected String name;	
	//数据库服务
	protected LoadDatasService service;
	//通讯服务
	protected Rereader reader;
	
	protected String masterRereadInterval;
	
	protected Scheduler scheduler = null;
	
	//数据项与obis对应关系表  [obis,code]
	private static Map<String,String> dlmsItemRelated = new HashMap<String, String>();
	
	private Map<String,String> getDlmsItemRelated() {
		if(dlmsItemRelated==null || dlmsItemRelated.size()==0){
			List<DlmsItemRelated> list = service.loadDlmsItemRelated();
			for(DlmsItemRelated item : list){
				dlmsItemRelated.put( item.getCode(),item.getAttribute());
			}
		}
		return dlmsItemRelated;
	}

	protected abstract Class<?> getProcessor();
	
	public static Map<String , List<String>> keySuffixMap = new HashMap<String , List<String>>();
	
	public static String getJobKey(String suffix , String keyPre){
		return "job_"+keyPre+"_"+suffix;
	}
	public static String getJobGroupKey(String suffix , String keyPre){
		return "jGroup_"+keyPre+"_"+suffix;
	}
	public static String getTriggerKey(String suffix , String keyPre){
		return "trigger_"+keyPre+"_"+suffix;
	}
	public static String getTriggerGroupKey(String suffix , String keyPre){
		return "tGroup_"+keyPre+"_"+suffix;
	}
	
	public boolean start() {
		if( !state.isStopped()) return false;
		state = State.STARTING;
		List<RereadStrategy> strategys = service.getRereadStrategy();
		try {
			//1、获取Quartz调度程序实例
			if(scheduler == null || scheduler.isShutdown()){
				SchedulerFactory sf = new StdSchedulerFactory();        
				scheduler = sf.getScheduler();
			}
			for(RereadStrategy strategy : strategys){
				String templateId = strategy.getTaskTemplateID();
				String rwlx = strategy.getRwlx();
				if("2".equals(strategy.getXgbj())){
					getService().deleteRereadStrategy(templateId, rwlx);
					log.info("Delete RereadStrategy success. " + strategy);
					continue; //已删除的不加载
				}
				//如果没有配置补召策略，则使用默认的补召策略
				if(strategy.getCron()==null || "".equals(strategy.getCron())){
					log.warn("任务模板["+templateId+"]没有配置对应的补召策略，不进行补召！");
					continue;
				}
				String[] corns = strategy.getCron().split(";");
				boolean flag = true;
				String keyPre = templateId + "_" + rwlx;
				keySuffixMap.put(keyPre, new ArrayList<String>());
				for(String corn : corns){
					if("".equals(corn)) continue;
					String suffix = UUID.randomUUID().toString();
					keySuffixMap.get(keyPre).add(suffix);
					//2、创建JobDetail实例
					JobDetail jobDetail = new JobDetail(getJobKey(suffix ,keyPre), getJobGroupKey(suffix ,keyPre), getProcessor());
					jobDetail.getJobDataMap().put(PARAMNAME_STRATEGY, strategy);  
					jobDetail.getJobDataMap().put(PARAMNAME_SERVICE, service);  
					jobDetail.getJobDataMap().put(PARAMNAME_READER, reader);  
					jobDetail.getJobDataMap().put(PARAMNAME_DLMSITEMRELATED, getDlmsItemRelated());
					//3、创建触发器
					CronTrigger cronTrigger = new CronTrigger(getTriggerKey(suffix ,keyPre), getTriggerGroupKey(suffix ,keyPre));//①-1：创建CronTrigger，指定组及名称
					CronExpression cexp;
					try {
						cexp = new CronExpression(corn);//①-2：定义Cron表达式
					} catch (ParseException e) {
						log.error(corn + " cron is error!",e);
						flag = false;
						break;
					}
					cronTrigger.setCronExpression(cexp);//①-3：设置Cron表达式
					//4、注册Job和Trigger
					scheduler.scheduleJob(jobDetail, cronTrigger);
				}
				if(flag){
					log.info("scheduler add job,"+strategy);
					//设置任务xgbj为0
					getService().setRereadStrategyXgbj("0", templateId, rwlx);
				}else{
					keySuffixMap.remove(keyPre);
					log.error(strategy + " is error!");
				}
			}
			//5、启动调度器
			scheduler.start();
		
			/***********定时轮询主站设置的补召任务**************/
			new WorkThread().start();
			
		} catch (SchedulerException e) {
			log.error(e);
			return false;
		}
		state = State.RUNNING;
		log.info("thread("+name+") start...");
		return true;
	}
	public void stop() {
		state = State.STOPPING;
		state = State.STOPPED;
	}

	private class WorkThread extends RereadWorkThreadParent{
		public WorkThread(){
		}

		@Override
		protected cn.hexing.fk.utils.State getMainClassState() {
			return RereadMainParent.this.state;
		}

		@Override
		protected LoadDatasService getService() {
			return RereadMainParent.this.service;
		}

		@Override
		protected Rereader getRereader() {
			return RereadMainParent.this.reader;
		}

		@Override
		protected Map<String, String> getDlmsItemRelated() {
			return RereadMainParent.this.getDlmsItemRelated();
		}

		@Override
		protected String getMasterRereadInterval() {
			return RereadMainParent.this.masterRereadInterval;
		}

		@Override
		protected Scheduler getScheduler() {
			return scheduler;
		}

		@Override
		protected Class<?> getProcessor() {
			return RereadMainParent.this.getProcessor();
		}
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isActive() {
		return state.isActive();
	}
	public String getModuleType() {
		return IModule.MODULE_TYPE_BP;
	}

	public LoadDatasService getService() {
		return service;
	}

	public void setService(LoadDatasService service) {
		this.service = service;
	}

	public Rereader getReader() {
		return reader;
	}

	public void setReader(Rereader reader) {
		this.reader = reader;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMasterRereadInterval() {
		return masterRereadInterval;
	}

	public void setMasterRereadInterval(String masterRereadInterval) {
		this.masterRereadInterval = masterRereadInterval;
	}

}
