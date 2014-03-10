package cn.hexing.db.managertu;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import cn.hexing.db.bizprocess.MasterDbService;
import cn.hexing.fk.model.RtuSynchronizeItem;



public class RtuRefreshManage {
	private static final Logger log = Logger.getLogger(RtuRefreshManage.class);
	private MasterDbService masterDbService;  	//spring 配置实现。
	private ManageRtu manageRtu;				//spring 配置实现。
    /** 最近一次刷新档案时间 */
    private Calendar lastRefreshTime=Calendar.getInstance();
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
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
    				manageRtu.refreshBizRtu(rst.getRtuId());
    				manageRtu.refreshDlmsMeterRtu(rst.getRtuId());
    			}
    			else{
    				//刷新任务模板
    				manageRtu.refreshTaskTemplate(rst.getRtuId());
    				manageRtu.refreshMasterTaskTemplate(rst.getRtuId());
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


}
