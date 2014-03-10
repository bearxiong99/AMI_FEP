package cn.hexing.dp.queue;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import cn.hexing.dp.bpserver.dlms.DlmsProcessor.TaskType;
import cn.hexing.dp.dao.LoadDatasDao;
import cn.hexing.dp.model.RtuTask;
import cn.hexing.dp.model.TaskTemplate;

public class GdgyRequestQueue {
	private static final Logger log = Logger.getLogger(GdgyRequestQueue.class);
	/** 任务模板列表<任务模板id,任务模板对应终端任务信息> */
    private Map<String,TaskTemplate> tasksMap=new HashMap<String,TaskTemplate> ();
    private static GdgyRequestQueue instance;
    private Object lock=new Object();
	private LoadDatasDao loadGgDatasDao;
	private	static String PROTOCOL = "04";
    public static GdgyRequestQueue getInstance(){
    	if (instance==null){
    		instance=new GdgyRequestQueue();
    	}
    	return instance;
    }
    public void initTaskData(){
		try{
			if(loadGgDatasDao!=null){
				tasksMap.clear();
				log.info("initGgTaskData...");
				List<TaskTemplate> taskTemplateList = loadGgDatasDao.getMasterTaskTemplate(PROTOCOL);
				List<RtuTask> rtuTaskList=loadGgDatasDao.getMasterTask(PROTOCOL);
				if (taskTemplateList!=null&&taskTemplateList.size()>0&&rtuTaskList!=null&&rtuTaskList.size()>0){
					for(TaskTemplate taskTemplate:taskTemplateList){
						taskTemplate.getRtuTaskList().clear();
						for(RtuTask rtuTask:rtuTaskList){
							if (taskTemplate.getTaskTemplateID().equals(rtuTask.getTaskTemplateID())){
								rtuTask.setTaskType(TaskType.Master);
								taskTemplate.getRtuTaskList().add(rtuTask);
							}
						}
						taskTemplate.createTaskDate();
						tasksMap.put(taskTemplate.getTaskTemplateID(), taskTemplate);
					}					
				}
			}
		}catch(Exception ex){
			
		}
	}
    public ArrayList<RtuTask> getRtuTaskRequestList(long time){
    	ArrayList<RtuTask> list=new ArrayList<RtuTask>();
    	ArrayList<Long> sendTimeList=new ArrayList<Long>();
    	synchronized (lock) {
    		for(TaskTemplate task:tasksMap.values()){
    			for(Long tst:task.getTaskSendTimeList()){
    				if (time>tst){
    					ArrayList<RtuTask> listTemp=task.getRtuTaskList();
    					for(RtuTask rtr:listTemp){
    						RtuTask rtuTask=new RtuTask();
    						rtuTask.setRtuId(rtr.getRtuId());
    						rtuTask.setTaskNo(rtr.getTaskNo());
    						rtuTask.setTaskTemplateID(rtr.getTaskTemplateID());
    						rtuTask.setTaskDate(task.getTaskDateMap().get(tst));
    						rtuTask.setTaskType(rtr.getTaskType());
    						rtuTask.setTaskProperty(rtr.getTaskProperty());
    						list.add(rtuTask);
    					}
    					sendTimeList.add(tst);   					
    				}
    			}
    			for(Long sendTime:sendTimeList){
    				task.getTaskSendTimeList().remove(sendTime);
					task.getTaskDateMap().remove(sendTime);
    			}
    		}    		
		}
    	return list;
    }

    public long getTimeValue(String dt) {
    	Date time=null;
		try{
			if (dt.trim().length()==16){
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				time = df.parse(dt);
			}
			else if (dt.trim().length()==13){
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH");
				time = df.parse(dt);
			}
			else if (dt.trim().length()==10){
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
				time = df.parse(dt);
			}
			else if (dt.trim().length()==7){
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM");
				time = df.parse(dt);
			}
		}
		catch(Exception ex){		
			log.error("getTimeValue error,dt="+dt+" error:"+ex.getLocalizedMessage());
		}
		if (time==null)
			return 0;
		else
			return time.getTime();
	}
	public final void setLoadGgDatasDao(LoadDatasDao loadGgDatasDao) {
		this.loadGgDatasDao = loadGgDatasDao;
	}
	public Map<String, TaskTemplate> getTasksMap() {
		return tasksMap;
	}
	public void setTasksMap(Map<String, TaskTemplate> tasksMap) {
		this.tasksMap = tasksMap;
	}


}
