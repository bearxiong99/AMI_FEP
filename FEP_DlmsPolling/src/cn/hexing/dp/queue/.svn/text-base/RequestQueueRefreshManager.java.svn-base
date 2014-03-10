package cn.hexing.dp.queue;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hx.ansi.model.AnsiMeterRtu;

import cn.hexing.db.bizprocess.MasterDbService;
import cn.hexing.db.managertu.ManageRtu;
import cn.hexing.dp.bpserver.dlms.DlmsProcessor.TaskType;
import cn.hexing.dp.dao.LoadDatasDao;
import cn.hexing.dp.model.RtuTask;
import cn.hexing.dp.model.TaskTemplate;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.model.DlmsMeterRtu;
import cn.hexing.fk.model.RtuManage;
import cn.hexing.fk.model.RtuSynchronizeItem;

public class RequestQueueRefreshManager {
	private static final Logger log = Logger.getLogger(RequestQueueRefreshManager.class);
	private MasterDbService masterDbService;  	//spring 配置实现。
	private ManageRtu manageRtu;				//spring 配置实现。
    /** 最近一次刷新档案时间 */
    private Calendar lastRefreshTime=Calendar.getInstance();
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private LoadDatasDao loadDatasDao;

    
    public void refreshRtuCache(){
    	try{
    		if (log.isInfoEnabled())
    			log.info("refresh rtu cache job excute");
    		List<RtuSynchronizeItem> rsts=masterDbService.getRtuSycItem(lastRefreshTime.getTime());
    		if (rsts!=null && rsts.size()>0){
    			log.info("refresh size="+rsts.size());
    		}
    		else {
    			log.info("no record need refresh");
    			return;
    		}
    		long time=lastRefreshTime.getTimeInMillis();
    		for (RtuSynchronizeItem rst:rsts){
    			if (rst.getSycType()==0){//刷新终端档案
    				boolean refreshSuccess=manageRtu.refreshBizRtu(rst.getRtuId());
    				DlmsMeterRtu meterRtu = manageRtu.refreshDlmsMeterRtu(rst.getRtuId());
    				AnsiMeterRtu ansiMeter=manageRtu.refreshAnsiMeterRtu(rst.getRtuId());
    				//当有终端需要刷新的时候，需要刷新终端任务
    				if(meterRtu!=null){
    					refreshdlmsTerminalTask(meterRtu);
    					refreshMasterTask(meterRtu.getMeterId(),"03");
    				}else if(ansiMeter!=null){
    					refreshansiTerminalTask(ansiMeter);
    					refreshMasterTask(ansiMeter.getMeterId(),"06");
    				}
    				else if(refreshSuccess){
    					BizRtu rtu = RtuManage.getInstance().getBizRtuInCache(rst.getRtuId());
    					refreshMasterTask(rtu.getRtuId(), rtu.getRtuProtocol());
    				}
    			}
    			else{
    				//刷新模板
    				if(refreshTaskTemplate(rst,"03")){}else 
    				if(refreshTaskTemplate(rst,"04")){}else
    				if(refreshTaskTemplate(rst,"06")){}
    				String mbid = rst.getRtuId();
    				manageRtu.refreshTaskTemplate(mbid);
            		manageRtu.refreshMasterTaskTemplate(mbid);
    				
    			}	
    			Date dt=null;
    			try{
    				dt=format.parse(rst.getSycTime());
    			}catch(Exception ex){
    				log.error("rtu syctime parse error:"+ex);
    				continue;
    			}
        		if (time<dt.getTime())
    				time=dt.getTime();   	
        		Thread.sleep(100);
    		}  		
    		lastRefreshTime.setTimeInMillis(time);
    	}catch(Exception e){
    		log.error("getRtuSycItem error:"+e);
    	}
    	
    }

    /**
     * 根据规约号，刷新任务模板
     * @param rst
     * @param protocol
     * @return
     */
	private boolean refreshTaskTemplate(RtuSynchronizeItem rst,String protocol){

    	String mbid = rst.getRtuId();
    	List<TaskTemplate> taskTemplates = loadDatasDao.getTaskTemplateById(protocol, mbid);
    	if(taskTemplates==null || taskTemplates.size()==0){
    		//不做处理
    	}else{
    		return refreshTemplate(mbid, taskTemplates);	
    	}
    	
    	taskTemplates=loadDatasDao.getMasterTaskTemplateById(protocol, mbid);
    	if(taskTemplates==null || taskTemplates.size()==0){
    		//不做处理
    	}else{
    		return refreshTemplate(mbid, taskTemplates);	
    	}
    	return false;
	
    }
    
	/**
	 * 根据模板ID刷新任务
	 * @param mbid
	 * @param taskTemplates
	 * @return
	 */
	private boolean refreshTemplate(String mbid, List<TaskTemplate> taskTemplates) {
		TaskTemplate taskTemplate = taskTemplates.get(0);
		taskTemplate.createTaskDate();
		Map<String, TaskTemplate> taskMap = DlmsReqestQueue.getInstance().getTasksMap();
		TaskTemplate temp = taskMap.get(mbid);
		if(temp!=null){
			taskTemplate.getRtuTaskList().addAll(temp.getRtuTaskList());    			
		}
		taskMap.put(mbid, taskTemplate);
		return true;
	}

	
	/**
	 * 刷新主站任务
	 * @param rtuId
	 * @param protocol
	 */
	private void refreshMasterTask(String rtuId,String protocol){
		List<RtuTask> tasks=loadDatasDao.getMasterTaskById(protocol, rtuId);
		for(RtuTask task:tasks){
			List<TaskTemplate> templates = loadDatasDao.getMasterTaskTemplateById(protocol, task.getTaskTemplateID());
			if(templates.size()!=1){
				log.error("find task,can't find taskTemplate. RtuId="+task.getRtuId()+",template="+task.getTaskTemplateID());
			}else{
				Map<String, TaskTemplate> taskMaps = null;
				if("03".equals(protocol)){
					taskMaps=DlmsReqestQueue.getInstance().getTasksMap();
				}else if("04".equals(protocol)){
					taskMaps=GdgyRequestQueue.getInstance().getTasksMap();
				}else if("06".equals(protocol)){
					taskMaps=AnsiRequestQueue.getInstance().getTasksMap();
				}
				if(taskMaps==null) return ;
				taskRefresh(rtuId, task, templates,taskMaps);
			}
		}
	}
	
    /**
     * 刷新任务
     * @param rtuId
     * @param task
     * @param templates
     * @param taskMap
     */
	private void taskRefresh(String rtuId,RtuTask task,List<TaskTemplate> templates,Map<String, TaskTemplate> taskMap){

		TaskTemplate template = templates.get(0);
		
		task.setTaskType(TaskType.Master);
		task.setSampleInterval(template.getSampleInterval());
		task.setSampleIntervalUnit(template.getSampleIntervalUnit());
		
		template.getRtuTaskList().add(task);
		TaskTemplate rawTemplate = taskMap.get(template.getTaskTemplateID());
		if(rawTemplate==null){
			template.createTaskDate();
			taskMap.put(template.getTaskTemplateID(), template);
		}else{
			int size = rawTemplate.getRtuTaskList().size();
			//有可能是新增任务,也有可能不是新增任务.如果不是新增任务，需要将列表中的任务删除,再添加
			int removeCount = 0;
			for(int i=0;i<size;i++){
				RtuTask tempTask = rawTemplate.getRtuTaskList().get(i-removeCount);
				if(tempTask.getRtuId().equals(rtuId)){
					rawTemplate.getRtuTaskList().remove(i-removeCount);
					removeCount++;
				}
			}
			rawTemplate.getRtuTaskList().add(task);
			taskMap.put(rawTemplate.getTaskTemplateID(), rawTemplate);
		}
	
	}
    
    
	private void taskRefresh(DlmsMeterRtu meterRtu, RtuTask task,
			List<TaskTemplate> templates) {
		TaskTemplate template = templates.get(0);
		
		task.setTaskType(TaskType.Terminal);
		task.setSampleInterval(template.getSampleInterval());
		task.setSampleIntervalUnit(template.getSampleIntervalUnit());
		
		template.getRtuTaskList().add(task);
		Map<String, TaskTemplate> taskMap = DlmsReqestQueue.getInstance().getTasksMap();
		TaskTemplate rawTemplate = taskMap.get(template.getTaskTemplateID());
		if(rawTemplate==null){
			template.createTaskDate();
			taskMap.put(template.getTaskTemplateID(), template);
		}else{
			int size = rawTemplate.getRtuTaskList().size();
			//有可能是新增任务,也有可能不是新增任务.如果不是新增任务，需要将列表中的任务删除,再添加
			int removeCount = 0;
			for(int i=0;i<size;i++){
				RtuTask tempTask = rawTemplate.getRtuTaskList().get(i-removeCount);
				if(tempTask.getRtuId().equals(meterRtu.getMeterId())){
					rawTemplate.getRtuTaskList().remove(i-removeCount);
					removeCount++;
				}
			}
			rawTemplate.getRtuTaskList().add(task);
			taskMap.put(rawTemplate.getTaskTemplateID(), rawTemplate);
		}
	}
	
	private void ansitaskRefresh(AnsiMeterRtu meterRtu, RtuTask task,
			List<TaskTemplate> templates) {
		TaskTemplate template = templates.get(0);
		
		task.setTaskType(TaskType.Terminal);
		task.setSampleInterval(template.getSampleInterval());
		task.setSampleIntervalUnit(template.getSampleIntervalUnit());
		
		template.getRtuTaskList().add(task);
		Map<String, TaskTemplate> taskMap = AnsiRequestQueue.getInstance().getTasksMap();
		TaskTemplate rawTemplate = taskMap.get(template.getTaskTemplateID());
		if(rawTemplate==null){
			template.createTaskDate();
			taskMap.put(template.getTaskTemplateID(), template);
		}else{
			int size = rawTemplate.getRtuTaskList().size();
			//有可能是新增任务,也有可能不是新增任务.如果不是新增任务，需要将列表中的任务删除,再添加
			int removeCount = 0;
			for(int i=0;i<size;i++){
				RtuTask tempTask = rawTemplate.getRtuTaskList().get(i-removeCount);
				if(tempTask.getRtuId().equals(meterRtu.getMeterId())){
					rawTemplate.getRtuTaskList().remove(i-removeCount);
					removeCount++;
				}
			}

			rawTemplate.getRtuTaskList().add(task);
			taskMap.put(rawTemplate.getTaskTemplateID(), rawTemplate);
		}
	}
	
	
	/**
	 * Dlms终端任务刷新
	 * @param meterRtu
	 */
	private void refreshdlmsTerminalTask(DlmsMeterRtu meterRtu) {
		//1.如果当前终端刷新的，需要取出当前终端的所有任务
		//2.从DlmsRequestQueue取出当前任务的taskTemplate,从taskTemplate移除当前终端的任务
		List<RtuTask> tasks = loadDatasDao.getRtuTask("03", meterRtu.getMeterId());
		for(RtuTask task:tasks){
			List<TaskTemplate> templates = loadDatasDao.getTaskTemplateById("03", task.getTaskTemplateID());
			if(templates.size()!=1){
				log.error("find task,can't find taskTemplate. RtuId="+task.getRtuId()+",template="+task.getTaskTemplateID());
			}else{
				taskRefresh(meterRtu, task, templates);
			}
		}
	}
    
	/**
	 * ANSI终端任务刷新
	 * @param meterRtu
	 */
	private void refreshansiTerminalTask(AnsiMeterRtu meterRtu) {
		//1.如果当前终端刷新的，需要取出当前终端的所有任务
		//2.从DlmsRequestQueue取出当前任务的taskTemplate,从taskTemplate移除当前终端的任务
		List<RtuTask> tasks = loadDatasDao.getRtuTask("06", meterRtu.getMeterId());
		for(RtuTask task:tasks){
			List<TaskTemplate> templates = loadDatasDao.getTaskTemplateById("06", task.getTaskTemplateID());
			if(templates.size()!=1){
				log.error("find task,can't find taskTemplate. RtuId="+task.getRtuId()+",template="+task.getTaskTemplateID());
			}else{
				ansitaskRefresh(meterRtu, task, templates);
			}
		}
	}
	
	public MasterDbService getMasterDbService() {
		return masterDbService;
	}
	public void setMasterDbService(MasterDbService masterDbService) {
		this.masterDbService = masterDbService;
	}
	public ManageRtu getManageRtu() {
		return manageRtu;
	}
	public void setManageRtu(ManageRtu manageRtu) {
		this.manageRtu = manageRtu;
	}

	public void setLoadDatasDao(LoadDatasDao loadDatasDao) {
		this.loadDatasDao = loadDatasDao;
	}
}
