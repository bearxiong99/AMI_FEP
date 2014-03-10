/**
 * 初始化加载RTU对象。
 * 通信前置机与业务处理器所需要加载的属性是不一样的。因此需要分别加载。
 */
package cn.hexing.db.managertu;

import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import cn.hexing.db.DbMonitor;
import cn.hexing.db.DbState;
import cn.hexing.db.initrtu.dao.AnsiMeterRtuDao;
import cn.hexing.db.initrtu.dao.BizRtuDao;
import cn.hexing.db.initrtu.dao.ComRtuDao;
import cn.hexing.db.initrtu.dao.DlmsMeterRtuDao;
import cn.hexing.db.rtu.RtuRefreshDao;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.model.ComRtu;
import cn.hexing.fk.model.DlmsMeterRtu;
import cn.hexing.fk.model.MeasuredPoint;
import cn.hexing.fk.model.RtuAlertCode;
import cn.hexing.fk.model.RtuManage;
import cn.hexing.fk.model.RtuTask;
import cn.hexing.fk.model.TaskDbConfig;
import cn.hexing.fk.model.TaskTemplate;
import cn.hexing.fk.model.TaskTemplateItem;
import cn.hexing.fk.tracelog.TraceLog;
import cn.hexing.fk.utils.HexDump;

import com.hx.ansi.model.AnsiMeterRtu;

/**
 *
 */
public class ManageRtu {
	private static final Logger log = Logger.getLogger(ManageRtu.class);
	private static final TraceLog tracer = TraceLog.getTracer(ManageRtu.class);
	private static ManageRtu instance = null;
	private DataSource dataSource;						//用于DbMonitor
	private List<ComRtuDao> comRtuDaoList;
	private List<BizRtuDao> bizRtuDaoList;
	private List<DlmsMeterRtuDao> dlmsMeterRtuDaoList;//dlms表计档案
	private List<AnsiMeterRtuDao> ansiMeterRtuDaoList;//ansi表计档案
	
	private RtuRefreshDao rtuRefreshDao; 
	public RtuManage rtuManage=RtuManage.getInstance();

	public ManageRtu(){ instance = this; }
	
	public static final ManageRtu getInstance(){
		return instance; 
	}
	
	public void loadComRtu(){
		log.info("start initializeComRtu");
    	//初始化通讯前置机终端基本属性
    	try{
    		for(ComRtuDao comRtuDao:comRtuDaoList){
    			List<ComRtu> comRtus=comRtuDao.loadComRtu();		
    			log.info("ComRtus size:"+comRtus.size());
    			for( ComRtu rtu: comRtus ){
    				rtuManage.putComRtuToCache(rtu);
    			}
    		} 		
    	}catch(Exception ex){
    		log.error("loadComRtus"+ex);
    	} 
		log.info("end initializeComRtu");		
	}
	
	
	public void loadBizRtu(){
		DbState ds = DbMonitor.getInstance().getMonitor(dataSource);
		if( null == ds || !ds.isAvailable() ){
			tracer.trace("Db not available. Can not load BizRtu!");
			return ;
		}
		
		int size=0;
		long endTime=0,timeConsume=0,speed=0;
		long startTime= System.currentTimeMillis();
		
		tracer.trace("start initializeDlmsMeterRtu");
		size=initializeDlmsMeterRtu();
		tracer.trace("end initializeDlmsMeterRtu");
		endTime = System.currentTimeMillis();
		timeConsume = endTime-startTime;
		if (timeConsume>0){
			speed = size*1000 / timeConsume ;
			tracer.trace(size+"个Dlms表计加载时间="+timeConsume+"ms;效率="+speed+"/s");
		}
		//加载ANSI表档案,暂时不加载
		startTime= System.currentTimeMillis();
		tracer.trace("start initializeAnsiMeterRtu");
		size=initializeAnsiMeterRtu();
		tracer.trace("end initializeAnsiMeterRtu");
		endTime = System.currentTimeMillis();
		timeConsume = endTime-startTime;
		if (timeConsume>0){
			speed = size*1000 / timeConsume ;
			tracer.trace(size+"个Ansi表计加载时间="+timeConsume+"ms;效率="+speed+"/s");
		}
		
		startTime= System.currentTimeMillis();
		//为北京融合度检测临时处理，初始化不加载终端相关档案
		tracer.trace("start initializeBizRtu");
		size=initializeBizRtu();
		tracer.trace("end initializeBizRtu");
		endTime = System.currentTimeMillis();
		timeConsume = endTime-startTime;
		if (timeConsume>0){
			speed = size*1000 / timeConsume ;
			tracer.trace(size+"个终端加载时间="+timeConsume+"ms;效率="+speed+"/s");
		}
		
		
		startTime= System.currentTimeMillis();	
		tracer.trace("start initializeTaskTemplate");
		size=initializeTaskTemplate();
		tracer.trace("end initializeTaskTemplate");
		endTime = System.currentTimeMillis();
		timeConsume = endTime-startTime;
		if (timeConsume>0){
			speed = size*1000 / timeConsume ;
			tracer.trace(size+"个任务模版加载时间="+timeConsume+"ms;效率="+speed+"/s");
		}
		
		startTime= System.currentTimeMillis();
		tracer.trace("start initializeTaskDbConfig");
		size=initializeTaskDbConfig();
		tracer.trace("end initializeTaskDbConfig");
		endTime = System.currentTimeMillis();
		timeConsume = endTime-startTime;
		if (timeConsume>0){
			speed = size*1000 / timeConsume ;
			tracer.trace(size+"个任务数据项数据库表映射关系加载时间="+timeConsume+"ms;效率="+speed+"/s");
		}
		
		startTime= System.currentTimeMillis();
		tracer.trace("start initializeAlertCode");
		size=initializeAlertCode();
		tracer.trace("end initializeAlertCode");
		endTime = System.currentTimeMillis();
		timeConsume = endTime-startTime;
		if (timeConsume>0){
			speed = size*1000 / timeConsume ;
			tracer.trace(size+"个异常数据项加载时间="+timeConsume+"ms;效率="+speed+"/s");
		}
	}
	
	/**
	 * 根据终端局号重新加载终端对象(包括终端资产,测量点信息及终端任务配置信息)。
	 * Use Case：主站主动通知前置机刷新终端信息。
	 * @param zdjh
	 * @return
	 */
	public  synchronized boolean refreshBizRtu(String zdjh){
		//数据库不可用，则直接退出
		DbState ds = DbMonitor.getInstance().getMonitor(dataSource);
		if( null == ds || !ds.isAvailable() ){
			log.error("Db not available. Can not refresh BizRtu:"+zdjh);
			return false;
		}
		try{
			log.debug("RefreshBizRtu. zdjh="+zdjh);
			BizRtu bizRtu=rtuRefreshDao.getRtu(zdjh);
			if (bizRtu!=null){
				System.out.println("size"+bizRtu.getMeasuredPoints().size()+"--"+bizRtu.getMeasuredPoints());
				bizRtu.setLastRefreshTime(new Date());
				BizRtu tmpRtu = rtuManage.getBizRtuInCache(zdjh);
				bizRtu.copy(tmpRtu);
				rtuManage.putBizRtuToCache(bizRtu);	
				return true;
			}
			else return false;
		}catch(Exception ex){
			log.error("find not rtuId:"+zdjh);
			return false;
		}
	}
	
	/**
	 * 根据终端RTUA重新加载终端对象(包括终端资产,测量点信息及终端任务配置信息)。
	 * Use Case: 任务解析失败，根据RTUA加载对象。
	 * @param rtua
	 * @return
	 */
	public boolean refreshBizRtu(int rtua){
		//数据库不可用，则直接退出
		DbState ds = DbMonitor.getInstance().getMonitor(dataSource);
		if( null == ds || !ds.isAvailable() ){
			log.error("Db not available. Can not refresh BizRtu:"+HexDump.toHex(rtua));
			return false;
		}
		try{
			BizRtu bizRtu=rtuRefreshDao.getRtu(rtua);
			if (bizRtu!=null){
				log.debug("RefreshBizRtu. zdjh="+HexDump.toHex(rtua));
				bizRtu.setLastRefreshTime(new Date());
				rtuManage.putBizRtuToCache(bizRtu);
				return true;
			}
			else
				return false;
		}catch(Exception ex){
			log.error("find not rtuAdd:"+HexDump.toHex(rtua));
			return false;
		}
	}
	/**
	 * 根据logicalAddress重新加载
	 * @param meterId
	 */
	public DlmsMeterRtu refreshDlmsMeterRtu(String logicalAddress){
		DbState ds = DbMonitor.getInstance().getMonitor(dataSource);
		if( null == ds || !ds.isAvailable() ){
			log.error("Db not available. Can not refresh DlmsMeterRtu:"+logicalAddress);
			return null;
		}
		try {
			DlmsMeterRtu dlmsRtu = rtuRefreshDao.getDlmsRtuByLogicAddr(logicalAddress);
			if(dlmsRtu!=null){
				dlmsRtu.setLastRefreshTime(new Date());
				rtuManage.putDlmsMeterRtuToCache(dlmsRtu);
				return dlmsRtu;
			}else{
				return null;
			}
		} catch (Exception e) {
			log.error("can't find dlmsMeterRtu:"+logicalAddress,e);
			return null;
		}
	}
	/**
	 * 根据logicalAddress重新加载
	 * @param meterId
	 */
	public AnsiMeterRtu refreshAnsiMeterRtu(String logicalAddress){
		DbState ds = DbMonitor.getInstance().getMonitor(dataSource);
		if( null == ds || !ds.isAvailable() ){
			log.error("Db not available. Can not refresh AnsiMeterRtu:"+logicalAddress);
			return null;
		}
		try {
			AnsiMeterRtu ansiRtu = rtuRefreshDao.getAnsiRtuByLogicAddr(logicalAddress);
			if(ansiRtu!=null){
				ansiRtu.setLastRefreshTime(new Date());
				rtuManage.putAnsiMeterRtuToCache(ansiRtu);
				return ansiRtu;
			}else{
				return null;
			}
		} catch (Exception e) {
			log.error("can't find ansiMeterRtu:"+logicalAddress,e);
			return null;
		}
	}
	/**
	 * 根据终端RTUA重新加载通讯前置终端对象。
	 * Use Case: 下行发送内存找不到指定终端，根据RTUA加载对象。
	 * @param rtua
	 * @return
	 */
	public ComRtu refreshComRtu(String logicalAddress){
		//数据库不可用，则直接退出批量保存。
		DbState ds = DbMonitor.getInstance().getMonitor(dataSource);
		if( null == ds || !ds.isAvailable() ){
			log.error("Db not available. Can not load Rtu:"+logicalAddress);
			return null;
		}
		
		try{
			ComRtu comRtu=rtuRefreshDao.getComRtu(logicalAddress);
			if (comRtu!=null){
				rtuManage.putComRtuToCache(comRtu);
				return comRtu;
			}
			else
				return null;
		}catch(Exception ex){
			log.error("find not comRtuAdd:"+logicalAddress);
			return null;
		}
	}
	
	public ComRtu reloadComRtu(String logicalAddress){
		//数据库不可用，则直接退出批量保存。
		DbState ds = DbMonitor.getInstance().getMonitor(dataSource);
		if( null == ds ||!ds.isAvailable() ){
			log.error("Db not available. Can not load Rtu:" + logicalAddress );
			return null;
		}
		
		try{
			return rtuRefreshDao.getComRtu(logicalAddress);
		}catch(Exception ex){
			log.error("find not comRtuAdd:"+ logicalAddress );
			return null;
		}
	}
	
	/**
	 * 刷新任务模板信息。
	 * Use Case：主站修改终端任务模板，在批量更新该模板的终端任务后，通知前置机刷新模板信息。
	 * @param templID
	 * @return
	 */
	public void refreshTaskTemplate(String templID){
		//数据库不可用，则直接退出批量保存。
		DbState ds = DbMonitor.getInstance().getMonitor(dataSource);
		if( null == ds ||!ds.isAvailable() ){
			log.error("Db not available. Can not refresh TaskTemplate:"+templID);
			return ;
		}
		try{
			TaskTemplate tt = rtuRefreshDao.getTaskTemplate(templID);
			rtuManage.putTaskTemplateToCache(tt);
			List<TaskTemplateItem> ttis = rtuRefreshDao.getTaskTemplateItems(templID);
			for( TaskTemplateItem tti: ttis ){
				rtuManage.putTaskTemplateItemToCache(tti);
			}
		}catch(Exception ex){
			log.error("find not templID:"+templID);
		}
	}
	/**
	 * 刷新主站任务模板信息
	 * @param taskNum
	 */
	public void refreshMasterTaskTemplate(String templID) {
		DbState ds = DbMonitor.getInstance().getMonitor(dataSource);
		if(null == ds || !ds.isAvailable()){
			log.error("Db not available. Can not refresh TaskTemplate:"+templID);
			return ;
		}
		try{
			TaskTemplate tt = rtuRefreshDao.getMasterTaskTemplate(templID);
			rtuManage.putTaskTemplateToCache(tt);
			List<TaskTemplateItem> ttis = rtuRefreshDao.getMasterTaskItems(templID);
			for(TaskTemplateItem tti:ttis){
				rtuManage.putTaskTemplateItemToCache(tti);
			}
		}catch (Exception e) {
			log.error("refresh MaterTaskTemplate can not find templID:"+templID);
		}
		
	}
	
		
	
	private int initializeDlmsMeterRtu(){
		int size = 0;
		for(DlmsMeterRtuDao dlmsMeterRtuDao:dlmsMeterRtuDaoList){
			List<DlmsMeterRtu> dlmsMeterRtus = dlmsMeterRtuDao.loadDlmsMeterRtu();
			size = size+dlmsMeterRtus.size();
			tracer.trace("DlmsMeterRtuList size:"+dlmsMeterRtus.size());
			for(DlmsMeterRtu rtu: dlmsMeterRtus){
				rtuManage.putDlmsMeterRtuToCache(rtu);
			}

			// 初始化测量点基本属性
			try {
				List<MeasuredPoint> mps =  dlmsMeterRtuDao.loadMeasuredPoints();
				if (mps == null) {
					continue;
				}
				tracer.trace("MeasuredPointList size:" + mps.size());
				for (MeasuredPoint mp : mps) {
					rtuManage.putMeasurePointToDlmsCache(mp);
				}
			} catch (Exception ex) {
				tracer.trace("loadMeasuredPoints" + ex);
			}
			
	     	//加载终端任务
        	try{   			
    			List<RtuTask> rts = dlmsMeterRtuDao.loadRtuTasks();
    			if(rts==null)
    				continue;
    			tracer.trace("RtuTaskList size:"+rts.size());
    			for( RtuTask rt: rts ){
    				rtuManage.putTaskToDlmsCache(rt);
    			}
        	}catch(Exception ex){
        		tracer.trace("loadRtuTasks"+ex);
        	} 
        	//加载主站任务
        	try{   			
    			List<RtuTask> rts = dlmsMeterRtuDao.loadMasterTasks();
    			tracer.trace("RtuTaskList size:"+rts.size());
    			for( RtuTask rt: rts ){
    				rtuManage.putTaskToDlmsCache(rt);
    			}
        	}catch(Exception ex){
        		tracer.trace("loadRtuTasks"+ex);
        	} 

		}
    	
		return size;
	}
	private int initializeAnsiMeterRtu(){

		int size = 0;
		if(ansiMeterRtuDaoList ==null) return 0;
		for(AnsiMeterRtuDao ansiMeterRtuDao:ansiMeterRtuDaoList){
			List<AnsiMeterRtu> ansiMeterRtus = ansiMeterRtuDao.loadAnsiMeterRtu();
			size = size+ansiMeterRtus.size();
			tracer.trace("AnsiMeterRtuList size:"+ansiMeterRtus.size());
			for(AnsiMeterRtu rtu: ansiMeterRtus){
				rtuManage.putAnsiMeterRtuToCache(rtu);
			}

			// 初始化测量点基本属性
			try {
				List<MeasuredPoint> mps =  ansiMeterRtuDao.loadMeasuredPoints();
				if (mps == null) {
					continue;
				}
				tracer.trace("MeasuredPointList size:" + mps.size());
				for (MeasuredPoint mp : mps) {
					rtuManage.putMeasurePointToAnsiCache(mp);
				}
			} catch (Exception ex) {
				tracer.trace("loadMeasuredPoints" + ex);
			}
			
	     	//加载终端任务
        	try{   			
    			List<RtuTask> rts = ansiMeterRtuDao.loadRtuTasks();
    			if(rts==null)
    				continue;
    			tracer.trace("RtuTaskList size:"+rts.size());
    			for( RtuTask rt: rts ){
    				rtuManage.putTaskToAnsiCache(rt);
    			}
        	}catch(Exception ex){
        		tracer.trace("loadRtuTasks"+ex);
        	} 
        	//加载主站任务
        	try{   			
    			List<RtuTask> rts = ansiMeterRtuDao.loadMasterTasks();
    			tracer.trace("RtuTaskList size:"+rts.size());
    			for( RtuTask rt: rts ){
    				rtuManage.putTaskToAnsiCache(rt);
    			}
        	}catch(Exception ex){
        		tracer.trace("loadRtuTasks"+ex);
        	} 

		}
    	
		return size;
	
	}
	/**
	 * 业务处理器终端档案(包括测量点和终端任务)初始化。
	 */
    private int initializeBizRtu(){ 
    	int size=0;
    	for(BizRtuDao bizRtuDao:bizRtuDaoList){
    		//初始化终端基本属性
        	try{       		
    			List<BizRtu> bizRtus = bizRtuDao.loadBizRtu();
    			size=size+bizRtus.size();
    			tracer.trace("BizRtuList size:"+bizRtus.size());
    			for( BizRtu rtu: bizRtus ){
    				if(rtu.getRtuProtocol()==null)//终端浙江规约
    					rtu.setRtuProtocol("01");
    				rtuManage.putBizRtuToCache(rtu);
    			}
        	}catch(Exception ex){
        		tracer.trace("loadBizRtus"+ex);
        		return size;
        	} 
        	//初始化终端测量点基本属性
        	try{			
    			List<MeasuredPoint> mps = bizRtuDao.loadMeasuredPoints();
    			tracer.trace("MeasuredPointList size:"+mps.size());
    			for( MeasuredPoint mp: mps ){
    				rtuManage.putMeasuredPointToCache(mp);
    			}
        	}catch(Exception ex){
        		tracer.trace("loadMeasuredPoints"+ex);
        	}       	
        	//初始化终端任务配置基本属性
        	try{   			
    			List<RtuTask> rts = bizRtuDao.loadRtuTasks();
    			tracer.trace("RtuTaskList size:"+rts.size());
    			for( RtuTask rt: rts ){
    				rtuManage.putRtuTaskToCache(rt);
    			}
        	}catch(Exception ex){
        		tracer.trace("loadRtuTasks"+ex);
        	} 
        	//初始化终端任务配置基本属性
        	try{   			
    			List<RtuTask> rts = bizRtuDao.loadMasterTasks();
    			tracer.trace("RtuTaskList size:"+rts.size());
    			for( RtuTask rt: rts ){
    				rtuManage.putRtuTaskToCache(rt);
    			}
        	}catch(Exception ex){
        		tracer.trace("loadRtuTasks"+ex);
        	} 
		}   	
    	return size;
    }
    /**
	 * 业务处理器任务模版初始化。
	 * @return
	 */
    private int initializeTaskTemplate(){
    	int size=0;
    	for(BizRtuDao bizRtuDao:bizRtuDaoList){   		
        	//初始化任务模版基本属性
        	try{
    			List<TaskTemplate> tts = bizRtuDao.loadTaskTemplate();
    			size=size+tts.size();
    			tracer.trace("TaskTemplateList size:"+tts.size());
    			for( TaskTemplate tt: tts ){
    				rtuManage.putTaskTemplateToCache(tt);
    			}
        	}catch(Exception ex){
        		tracer.trace("loadTaskTemplate"+ex);
        		return size;
        	} 
        	break;//任务模版是通用的，只要初始化一次
    	}   	
    	return size;
    }
  
    /**
	 * 业务处理器任务保存映射信息初始化。
	 * @return
	 */
    public int initializeTaskDbConfig(){
    	int size=0;
    	//数据库不可用，则直接退出批量保存。
		DbState ds = DbMonitor.getInstance().getMonitor(dataSource);
		if( null == ds ||!ds.isAvailable() ){
			log.error("Db not available. Can not initialize TaskDbConfig");
			return size;
		}
    	for(BizRtuDao bizRtuDao:bizRtuDaoList){   		
    		//初始化任务保存映射信息基本属性
        	try{
    			List<TaskDbConfig> rdcs = bizRtuDao.loadTaskDbConfig();
    			size=size+rdcs.size();
    			tracer.trace("TaskDbConfigList size:"+rdcs.size());
    			for( TaskDbConfig rdc: rdcs ){
    				rtuManage.putTaskDbConfigToCache(rdc);
    			}
        	}catch(Exception ex){
        		tracer.trace("loadTaskDbConfig"+ex);
        	} 
        	break;//任务保存映射信息基本属性是通用的，只要初始化一次
    	}    	
    	return size;
    }
    /**
	 * 业务处理器异常携带数据项初始化。
	 * @return
	 */
    private int initializeAlertCode(){
    	int size=0;
    	for(BizRtuDao bizRtuDao:bizRtuDaoList){   	
    		//初始化异常携带数据项基本属性
        	try{
    			List<RtuAlertCode> racs = bizRtuDao.loadRtuAlertCodes();
    			size=size+racs.size();
    			tracer.trace("RtuAlertCodeList size:"+racs.size());
    			for( RtuAlertCode rac: racs ){
    				rtuManage.putAlertCodeToCache(rac);
    			}
        	}catch(Exception ex){
        		tracer.trace("loadRtuAlertCodes"+ex);
        	} 
        	break;//异常携带数据项基本属性是通用的，只要初始化一次
    	}  	
    	return size;
    }
	public void setRtuRefreshDao(RtuRefreshDao rtuRefreshDao) {
		this.rtuRefreshDao = rtuRefreshDao;
	}

	public void setComRtuDaoList(List<ComRtuDao> comRtuDaoList) {
		this.comRtuDaoList = comRtuDaoList;
	}

	public void setBizRtuDaoList(List<BizRtuDao> bizRtuDaoList) {
		this.bizRtuDaoList = bizRtuDaoList;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public final void setDlmsMeterRtuDaoList(
			List<DlmsMeterRtuDao> dlmsMeterRtuDaoList) {
		this.dlmsMeterRtuDaoList = dlmsMeterRtuDaoList;
	}
	public final void setAnsiMeterRtuDaoList(
			List<AnsiMeterRtuDao> ansiMeterRtuDaoList) {
		this.ansiMeterRtuDaoList=ansiMeterRtuDaoList;
	}
}
