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
 * ҵ�������
 * 
 */
public class BPLatterProcessor {
	private static final Log log = LogFactory.getLog(BPLatterProcessor.class);
	/** Web �ӿ���Ϣ������ */
    private final WebMessageEncoder encoder = new WebMessageEncoder();
    /** ���� */
    private static BPLatterProcessor instance;
    /** �����߳� */
    private Worker worker;
    /** �洢���̷���ģ�� */
    private MasterDbService masterDbService;
    /** �������ֵ */
    private static final int LIST_MAX_COUNT = 1000000;
    /** �����������ʱ�� */
    private static final int UPDATE_MAX_TIME = 1000;
    /** �����̵߳������ʱ�䣨ms�� */
    private static final int WORKER_MAX_STEP_TIME = 5000;
    /** ©�㲹��������Ϣ���� */
    private static List<FaalRereadTaskResponse> rereadtaskInfoList=Collections.synchronizedList(new LinkedList<FaalRereadTaskResponse>());
	/** �쳣���ݶ��� */
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
				log.info("BPLatterProcessor start success");//log.info("ҵ�����������߳������ɹ�");
			return true;
		}
		else{
			if( log.isInfoEnabled() )
				log.info("BPLatterProcessor start Failed:�洢���̷���ģ��û������");//log.info("ҵ�����������߳�����ʧ��:�洢���̷���ģ��û������");
			return false;
		}
	}
	/**
     * ȡ���ն˻��������������δ��ʼ�������ڳ�ʼ���󷵻�
     * @return �ն˻������
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
     * ���ӹ����̡߳�����й����̵߳ȴ�ʱ�䳬����ֵ,���ӡ��Ϣ
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
    	/** �߳���ֹ��־ */
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
				//�ȴ�����
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
							if (!sendTag){//�������ʧ�ܣ��Ż���ʱ����
								rereadTasks.add(data);
								continue;
							}
							else
								rereadtaskCounter.add();
	    				}catch(Exception ex){
	                    	log.error("send rereadtaskInfo error:", ex);
	                    }	
	    				
    					//���ػ����ļ�������		
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
									if (!sendTag){//�������ʧ�ܣ��Ż���ʱ����
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
					//����ֻ��rereadtaskInfoList.size()>0ʱ��cpu�����̴߳���ռ��
					if (alertDataList.size()<=0&&rereadtaskInfoList.size()>0){
						try{
							Thread.sleep(5);
						}catch(Exception ex){}
					}				
				}	
				if (alertDataList.size()>0){//�쳣����
					AlarmData alert=(AlarmData)alertDataList.get(0);
					alertDataList.remove(0);
					if (alertDataList.size()<LIST_MAX_COUNT){//������ֵ����
						curTime= System.currentTimeMillis();
						if(alert.getTxfs()==null)//ͨѶ��ʽδ֪
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
