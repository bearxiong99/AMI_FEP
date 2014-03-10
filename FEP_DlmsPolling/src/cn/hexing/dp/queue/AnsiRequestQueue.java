package cn.hexing.dp.queue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import cn.hexing.dp.bpserver.ansi.AnsiTaskProcessor.AnsiTaskType;
import cn.hexing.dp.dao.LoadDatasDao;
import cn.hexing.dp.model.RtuTask;
import cn.hexing.dp.model.TaskTemplate;

/** 
 * @Description  xxxxx
 * @author  Rolinbor
 * @Copyright 2013 hexing Inc. All rights reserved
 * @time：2013-6-8 上午10:21:58
 * @version 1.0 
 */

public class AnsiRequestQueue {
	private static final Logger log=Logger.getLogger(AnsiRequestQueue.class);
	/** 模板id和任务模板对应的任务信息*/
	private Map<String ,TaskTemplate> tasksMap=new HashMap<String ,TaskTemplate>();
	private static AnsiRequestQueue instance;
	public static AnsiRequestQueue getInstance(){
		if(instance==null){
			instance=new AnsiRequestQueue();
		}
		return instance;
	}
	
	private Object lock=new Object();
	private static String PROTOCOL="06";
	private LoadDatasDao loadAnsiDatasDao;
	
	/**
	 * 初始化任务数据
	 */
    public void initTaskData(){
		try{
			if(loadAnsiDatasDao!=null){
				log.info("initAnsiTaskData...");
				tasksMap.clear();
				List<TaskTemplate> taskTemplateList=loadAnsiDatasDao.getTaskTemplate(PROTOCOL);
				List<RtuTask> rtuTaskList=loadAnsiDatasDao.getRtuTask(PROTOCOL);
				if (taskTemplateList!=null&&taskTemplateList.size()>0&&rtuTaskList!=null&&rtuTaskList.size()>0){
					for(TaskTemplate taskTemplate:taskTemplateList){
						taskTemplate.getRtuTaskList().clear();
						for(RtuTask rtuTask:rtuTaskList){
							if (taskTemplate.getTaskTemplateID().equals(rtuTask.getTaskTemplateID())){
								if( rtuTask.getTaskNo().equals("123")){
									
									String string="";
									string="";
								}
								rtuTask.setAnsiTaskType(AnsiTaskType.Terminal);
								rtuTask.setSampleInterval(taskTemplate.getSampleInterval());
								rtuTask.setSampleIntervalUnit(taskTemplate.getSampleIntervalUnit());
								log.info(taskTemplate.getSampleInterval()+" "+taskTemplate.getSampleIntervalUnit());
								taskTemplate.getRtuTaskList().add(rtuTask);
							}
						}
						
						taskTemplate.createTaskDate();
						tasksMap.put(taskTemplate.getTaskTemplateID(), taskTemplate);
					}					
				}	
				log.info("initAnsiMasterTaskData...");
				taskTemplateList = loadAnsiDatasDao.getMasterTaskTemplate(PROTOCOL);
				rtuTaskList=loadAnsiDatasDao.getMasterTask(PROTOCOL);
				if (taskTemplateList!=null&&taskTemplateList.size()>0&&rtuTaskList!=null&&rtuTaskList.size()>0){
					for(TaskTemplate taskTemplate:taskTemplateList){
						taskTemplate.getRtuTaskList().clear();
						for(RtuTask rtuTask:rtuTaskList){
							if (taskTemplate.getTaskTemplateID().equals(rtuTask.getTaskTemplateID())){
								rtuTask.setAnsiTaskType(AnsiTaskType.Master);
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
	/**
	 * 获取当前任务
	 * @param time
	 * @return
	 */
    public ArrayList<RtuTask> getRtuTaskRequestList(long time){
    	ArrayList<RtuTask> list=new ArrayList<RtuTask>();
    	ArrayList<Long> sendTimeList=new ArrayList<Long>();
    	synchronized (lock) {
    		for(TaskTemplate task:tasksMap.values()){
    			for(Long tst:task.getTaskSendTimeList()){
    				
    				if (time>tst){
    					
    					ArrayList<RtuTask> listTemp=task.getRtuTaskList();
    					for(RtuTask rtr:listTemp){
    						if( rtr.getTaskNo().equals("123")){
    							
    							String string="";
    							string="";
    						}
    						RtuTask rtuTask=new RtuTask();
    						rtuTask.setRtuId(rtr.getRtuId());
    						rtuTask.setTaskNo(rtr.getTaskNo());
    						rtuTask.setSampleInterval(rtr.getSampleInterval());
    						rtuTask.setSampleIntervalUnit(rtr.getSampleIntervalUnit());
    						rtuTask.setTaskTemplateID(rtr.getTaskTemplateID());
    						rtuTask.setTaskDate(task.getTaskDateMap().get(tst));
    						rtuTask.setAnsiTaskType(rtr.getAnsiTaskType());
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
	public final void setLoadAnsiDatasDao(LoadDatasDao loadAnsiDatasDao) {
		this.loadAnsiDatasDao = loadAnsiDatasDao;
	}
	public Map<String, TaskTemplate> getTasksMap() {
		return tasksMap;
	}
	public void setTasksMap(Map<String, TaskTemplate> tasksMap) {
		this.tasksMap = tasksMap;
	}

	
	
}
