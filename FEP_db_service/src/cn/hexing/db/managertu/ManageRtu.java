/**
 * ��ʼ������RTU����
 * ͨ��ǰ�û���ҵ����������Ҫ���ص������ǲ�һ���ġ������Ҫ�ֱ���ء�
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
	private DataSource dataSource;						//����DbMonitor
	private List<ComRtuDao> comRtuDaoList;
	private List<BizRtuDao> bizRtuDaoList;
	private List<DlmsMeterRtuDao> dlmsMeterRtuDaoList;//dlms��Ƶ���
	private List<AnsiMeterRtuDao> ansiMeterRtuDaoList;//ansi��Ƶ���
	
	private RtuRefreshDao rtuRefreshDao; 
	public RtuManage rtuManage=RtuManage.getInstance();

	public ManageRtu(){ instance = this; }
	
	public static final ManageRtu getInstance(){
		return instance; 
	}
	
	public void loadComRtu(){
		log.info("start initializeComRtu");
    	//��ʼ��ͨѶǰ�û��ն˻�������
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
			tracer.trace(size+"��Dlms��Ƽ���ʱ��="+timeConsume+"ms;Ч��="+speed+"/s");
		}
		//����ANSI����,��ʱ������
		startTime= System.currentTimeMillis();
		tracer.trace("start initializeAnsiMeterRtu");
		size=initializeAnsiMeterRtu();
		tracer.trace("end initializeAnsiMeterRtu");
		endTime = System.currentTimeMillis();
		timeConsume = endTime-startTime;
		if (timeConsume>0){
			speed = size*1000 / timeConsume ;
			tracer.trace(size+"��Ansi��Ƽ���ʱ��="+timeConsume+"ms;Ч��="+speed+"/s");
		}
		
		startTime= System.currentTimeMillis();
		//Ϊ�����ں϶ȼ����ʱ������ʼ���������ն���ص���
		tracer.trace("start initializeBizRtu");
		size=initializeBizRtu();
		tracer.trace("end initializeBizRtu");
		endTime = System.currentTimeMillis();
		timeConsume = endTime-startTime;
		if (timeConsume>0){
			speed = size*1000 / timeConsume ;
			tracer.trace(size+"���ն˼���ʱ��="+timeConsume+"ms;Ч��="+speed+"/s");
		}
		
		
		startTime= System.currentTimeMillis();	
		tracer.trace("start initializeTaskTemplate");
		size=initializeTaskTemplate();
		tracer.trace("end initializeTaskTemplate");
		endTime = System.currentTimeMillis();
		timeConsume = endTime-startTime;
		if (timeConsume>0){
			speed = size*1000 / timeConsume ;
			tracer.trace(size+"������ģ�����ʱ��="+timeConsume+"ms;Ч��="+speed+"/s");
		}
		
		startTime= System.currentTimeMillis();
		tracer.trace("start initializeTaskDbConfig");
		size=initializeTaskDbConfig();
		tracer.trace("end initializeTaskDbConfig");
		endTime = System.currentTimeMillis();
		timeConsume = endTime-startTime;
		if (timeConsume>0){
			speed = size*1000 / timeConsume ;
			tracer.trace(size+"���������������ݿ��ӳ���ϵ����ʱ��="+timeConsume+"ms;Ч��="+speed+"/s");
		}
		
		startTime= System.currentTimeMillis();
		tracer.trace("start initializeAlertCode");
		size=initializeAlertCode();
		tracer.trace("end initializeAlertCode");
		endTime = System.currentTimeMillis();
		timeConsume = endTime-startTime;
		if (timeConsume>0){
			speed = size*1000 / timeConsume ;
			tracer.trace(size+"���쳣���������ʱ��="+timeConsume+"ms;Ч��="+speed+"/s");
		}
	}
	
	/**
	 * �����ն˾ֺ����¼����ն˶���(�����ն��ʲ�,��������Ϣ���ն�����������Ϣ)��
	 * Use Case����վ����֪ͨǰ�û�ˢ���ն���Ϣ��
	 * @param zdjh
	 * @return
	 */
	public  synchronized boolean refreshBizRtu(String zdjh){
		//���ݿⲻ���ã���ֱ���˳�
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
	 * �����ն�RTUA���¼����ն˶���(�����ն��ʲ�,��������Ϣ���ն�����������Ϣ)��
	 * Use Case: �������ʧ�ܣ�����RTUA���ض���
	 * @param rtua
	 * @return
	 */
	public boolean refreshBizRtu(int rtua){
		//���ݿⲻ���ã���ֱ���˳�
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
	 * ����logicalAddress���¼���
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
	 * ����logicalAddress���¼���
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
	 * �����ն�RTUA���¼���ͨѶǰ���ն˶���
	 * Use Case: ���з����ڴ��Ҳ���ָ���նˣ�����RTUA���ض���
	 * @param rtua
	 * @return
	 */
	public ComRtu refreshComRtu(String logicalAddress){
		//���ݿⲻ���ã���ֱ���˳��������档
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
		//���ݿⲻ���ã���ֱ���˳��������档
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
	 * ˢ������ģ����Ϣ��
	 * Use Case����վ�޸��ն�����ģ�壬���������¸�ģ����ն������֪ͨǰ�û�ˢ��ģ����Ϣ��
	 * @param templID
	 * @return
	 */
	public void refreshTaskTemplate(String templID){
		//���ݿⲻ���ã���ֱ���˳��������档
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
	 * ˢ����վ����ģ����Ϣ
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

			// ��ʼ���������������
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
			
	     	//�����ն�����
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
        	//������վ����
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

			// ��ʼ���������������
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
			
	     	//�����ն�����
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
        	//������վ����
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
	 * ҵ�������ն˵���(������������ն�����)��ʼ����
	 */
    private int initializeBizRtu(){ 
    	int size=0;
    	for(BizRtuDao bizRtuDao:bizRtuDaoList){
    		//��ʼ���ն˻�������
        	try{       		
    			List<BizRtu> bizRtus = bizRtuDao.loadBizRtu();
    			size=size+bizRtus.size();
    			tracer.trace("BizRtuList size:"+bizRtus.size());
    			for( BizRtu rtu: bizRtus ){
    				if(rtu.getRtuProtocol()==null)//�ն��㽭��Լ
    					rtu.setRtuProtocol("01");
    				rtuManage.putBizRtuToCache(rtu);
    			}
        	}catch(Exception ex){
        		tracer.trace("loadBizRtus"+ex);
        		return size;
        	} 
        	//��ʼ���ն˲������������
        	try{			
    			List<MeasuredPoint> mps = bizRtuDao.loadMeasuredPoints();
    			tracer.trace("MeasuredPointList size:"+mps.size());
    			for( MeasuredPoint mp: mps ){
    				rtuManage.putMeasuredPointToCache(mp);
    			}
        	}catch(Exception ex){
        		tracer.trace("loadMeasuredPoints"+ex);
        	}       	
        	//��ʼ���ն��������û�������
        	try{   			
    			List<RtuTask> rts = bizRtuDao.loadRtuTasks();
    			tracer.trace("RtuTaskList size:"+rts.size());
    			for( RtuTask rt: rts ){
    				rtuManage.putRtuTaskToCache(rt);
    			}
        	}catch(Exception ex){
        		tracer.trace("loadRtuTasks"+ex);
        	} 
        	//��ʼ���ն��������û�������
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
	 * ҵ����������ģ���ʼ����
	 * @return
	 */
    private int initializeTaskTemplate(){
    	int size=0;
    	for(BizRtuDao bizRtuDao:bizRtuDaoList){   		
        	//��ʼ������ģ���������
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
        	break;//����ģ����ͨ�õģ�ֻҪ��ʼ��һ��
    	}   	
    	return size;
    }
  
    /**
	 * ҵ���������񱣴�ӳ����Ϣ��ʼ����
	 * @return
	 */
    public int initializeTaskDbConfig(){
    	int size=0;
    	//���ݿⲻ���ã���ֱ���˳��������档
		DbState ds = DbMonitor.getInstance().getMonitor(dataSource);
		if( null == ds ||!ds.isAvailable() ){
			log.error("Db not available. Can not initialize TaskDbConfig");
			return size;
		}
    	for(BizRtuDao bizRtuDao:bizRtuDaoList){   		
    		//��ʼ�����񱣴�ӳ����Ϣ��������
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
        	break;//���񱣴�ӳ����Ϣ����������ͨ�õģ�ֻҪ��ʼ��һ��
    	}    	
    	return size;
    }
    /**
	 * ҵ�������쳣Я���������ʼ����
	 * @return
	 */
    private int initializeAlertCode(){
    	int size=0;
    	for(BizRtuDao bizRtuDao:bizRtuDaoList){   	
    		//��ʼ���쳣Я���������������
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
        	break;//�쳣Я�����������������ͨ�õģ�ֻҪ��ʼ��һ��
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
