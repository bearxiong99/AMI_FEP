package cn.hexing.fk.bp.processor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.hexing.db.bizprocess.MasterDbService;
import cn.hexing.fas.model.FaalRereadTaskResponse;
import cn.hexing.fk.bp.feclient.RereadTaskChannelManage;
import cn.hexing.fk.bp.filecache.RereadTaskCache;
import cn.hexing.fk.bp.model.AlarmData;
import cn.hexing.fk.bp.webapi.MessageWeb;
import cn.hexing.fk.bp.webapi.WebMessageEncoder;
import cn.hexing.fk.utils.Counter;

/**
 * 业务后处理器
 * 
 */
public class BPLatterProcessor {
	private static final Log log = LogFactory.getLog(BPLatterProcessor.class);
	/** Web 接口消息编码器 */
    private final WebMessageEncoder encoder = new WebMessageEncoder();
    /** 单例 */
    private static BPLatterProcessor instance;
    /** 工作线程 */
    private Worker worker;
    /** 存储过程服务模块 */
    private MasterDbService masterDbService;
    /** 队列最大阀值 */
    private static final int LIST_MAX_COUNT = 1000000;
    /** 单个更新最大时间 */
    private static final int UPDATE_MAX_TIME = 1000;
    /** 工作线程单步最大时间（ms） */
    private static final int WORKER_MAX_STEP_TIME = 5000;
    /** 漏点补招任务信息队列 */
    private static List<FaalRereadTaskResponse> rereadtaskInfoList=Collections.synchronizedList(new LinkedList<FaalRereadTaskResponse>());
	/** 异常数据队列 */
	private static List<AlarmData> alertDataList=Collections.synchronizedList(new LinkedList<AlarmData>());
	private static final Object listSync = new Object();
	
	private Counter AlertCounter=new Counter(5000,"AlarmLatterP");
	private Counter rereadtaskCounter=new Counter(20000,"RereadTaskLatterP");
	public BPLatterProcessor() {				   	
	}
	public boolean start() {
		if (masterDbService!=null){
			this.worker=new Worker();
			this.worker.start();
			if( log.isInfoEnabled())
				log.info("BPLatterProcessor start success");//log.info("业务处理器后处理线程启动成功");
			return true;
		}
		else{
			if( log.isInfoEnabled() )
				log.info("BPLatterProcessor start Failed:存储过程服务模块没有启动");//log.info("业务处理器后处理线程启动失败:存储过程服务模块没有启动");
			return false;
		}
	}
	/**
     * 取得终端缓存对象。若缓存尚未初始化，则在初始化后返回
     * @return 终端缓存对象
     */
    public static BPLatterProcessor getInstance() {
        if (instance == null) {
            synchronized (BPLatterProcessor.class) {
                if (instance == null) {
                    instance = new BPLatterProcessor();
                }
            }
        }       
        return instance;
    }
	
	public void rereadTaskAdd(FaalRereadTaskResponse data) {
		synchronized( listSync ){
			rereadtaskInfoList.add(data);
			listSync.notifyAll();
		}
	}
	public void alertDataAdd(AlarmData alarm) {
		synchronized( listSync ){
			alertDataList.add(alarm);
			listSync.notifyAll();
		}
	}
	/**
     * 监视工作线程。如果有工作线程等待时间超过阀值,则打印信息
     */
    public void monitorWorkerThreads() {
    	long now = System.currentTimeMillis();   	
	    if (now - worker.getLastSaveTime() >= WORKER_MAX_STEP_TIME) {	            
	        log.warn(
        		"DBBP Worker timeOut:"+
        		"rereadtaskInfoList.size="+rereadtaskInfoList.size()+
        		"alertDataList.size="+alertDataList.size()+
        		worker.toString());	        
	    } 	   
    }
    private class Worker extends Thread {
    	/** 线程终止标志 */
    	private boolean shouldTerminate=false;
    	private long saveTime=System.currentTimeMillis();
    	private long curTime=System.currentTimeMillis();
    	private long lastSaveTime=System.currentTimeMillis();
    	public Worker() {
        }  
    	public long getLastSaveTime() {
            return lastSaveTime;
        }
    	
    	/*
    	 * (non-Javadoc)
    	 * 
    	 * @see java.lang.Thread#run()
    	 */
    	public void run() {
    		int alertCount=0,taskCount=0,rereadtaskCount=0;  
    		boolean sendTag=false;
    		List<FaalRereadTaskResponse> rereadTasks = new ArrayList<FaalRereadTaskResponse>();
			while (true) {
				if (shouldTerminate)
					break;
				//等待数据
				synchronized( listSync ){
					try{
						if (rereadtaskInfoList.size()<=0&&alertDataList.size()<=0)
							listSync.wait();
					}catch(Exception ex){}
				}				
				if (rereadtaskInfoList.size()>0){
					FaalRereadTaskResponse data=(FaalRereadTaskResponse)rereadtaskInfoList.get(0);
					rereadtaskInfoList.remove(0);
					if (RereadTaskChannelManage.getInstance().getClientAlive()){						
						try{
							MessageWeb msgWeb=encoder.encode(data);
							sendTag=RereadTaskChannelManage.getInstance().sendMessage(msgWeb);
							if (!sendTag){//如果发送失败，放回临时队列
								rereadTasks.add(data);
								continue;
							}
							else
								rereadtaskCounter.add();
	    				}catch(Exception ex){
	                    	log.error("send rereadtaskInfo error:", ex);
	                    }	
	    				
    					//加载缓存文件并处理		
	    				Collection<FaalRereadTaskResponse> rereadTaskTemps=RereadTaskCache.getInstance().loadFromFile();
	    				if (rereadTaskTemps!=null&&rereadTaskTemps.size()>0){
	    					log.info("load rereadTaskCount from file is"+rereadTaskTemps.size());
	    					for(FaalRereadTaskResponse rereadTaskTemp:rereadTaskTemps)
	    						rereadTasks.add(rereadTaskTemp);
	    					rereadTaskTemps.clear();
    						while(rereadTasks.size()>0){
    							try{
									MessageWeb msgWeb=encoder.encode(rereadTasks.remove(0));
									sendTag=RereadTaskChannelManage.getInstance().sendMessage(msgWeb);
									if (!sendTag){//如果发送失败，放回临时队列
										rereadTasks.add(data);
										break;
									}
									else
										rereadtaskCounter.add();
			    				}catch(Exception ex){
			                    	log.error("send rereadtaskInfo error:", ex);
			                    }
    						}   													
	    				}    				    				    				
						rereadtaskCount=rereadtaskInfoList.size();	
						if (rereadtaskCount>=5000&&rereadtaskCount % 5000==0){
							log.warn("rereadtaskList size="+rereadtaskCount);
						}	
						if (rereadTasks.size()>0){
							RereadTaskCache.getInstance().save2File(rereadTasks);							
							log.info("save2File of RereadTaskCache count is "+rereadTasks.size());
							rereadTasks.clear();
						}
					}
					else{							
						rereadTasks.add(data);
						if (rereadTasks.size()>=10000){
							RereadTaskCache.getInstance().save2File(rereadTasks);
							log.info("save2File of RereadTaskCache count is "+rereadTasks.size());
							rereadTasks.clear();						
						}
					}
					//避免只有rereadtaskInfoList.size()>0时，cpu被此线程大量占用
					if (alertDataList.size()<=0&&rereadtaskInfoList.size()>0){
						try{
							Thread.sleep(5);
						}catch(Exception ex){}
					}				
				}	
				if (alertDataList.size()>0){//异常后处理
					AlarmData alert=(AlarmData)alertDataList.get(0);
					alertDataList.remove(0);
					if (alertDataList.size()<LIST_MAX_COUNT){//超过阀值则丢弃
						curTime= System.currentTimeMillis();
						if(alert.getTxfs()==null)//通讯方式未知
							alert.setTxfs("99");
						try{
							masterDbService.procPostCreateRtuAlert(alert);
							lastSaveTime=System.currentTimeMillis();
							saveTime=lastSaveTime-curTime;
							if (saveTime>UPDATE_MAX_TIME)
								log.info("postCreateRtuAlert="+saveTime);
						}catch(Exception ex){
	                    	log.error("postCreateRtuAlert error:", ex);
	                    }					
						alertCount=alertDataList.size();
						if (alertCount>=1000&&alertCount % 1000==0){
							log.warn("alertDataList size="+alertCount);
						}
						AlertCounter.add();							
					}					
				}
			}
    	}    			
	}    
    
	public void setMasterDbService(MasterDbService masterDbService) {
		this.masterDbService = masterDbService;
	}
	

}
