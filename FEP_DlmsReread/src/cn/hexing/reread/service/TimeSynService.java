package cn.hexing.reread.service;

import java.util.List;

import org.apache.log4j.Logger;

import cn.hexing.reread.dao.TimeSynDao;
import cn.hexing.reread.model.ReadTimeModel;
import cn.hexing.reread.model.TimeSynStrategy;
import cn.hexing.reread.model.TimeSynTask;
import cn.hexing.reread.utils.SystemType;

public class TimeSynService {
	private static final Logger log = Logger.getLogger(TimeSynService.class);


	private static TimeSynService instance;

	private TimeSynDao timeSynDao;

	public static TimeSynService getInstance() {
		if (instance == null) {
			synchronized (TimeSynService.class) {
				if (instance == null) {
					instance = new TimeSynService();
				}
			}
		}
		return instance;

	}

	public void setLoadDatasDao(TimeSynDao timeSynDao) {
		this.timeSynDao = timeSynDao;
	}
	
	public List<TimeSynTask> getTimeSynTasks(){
		try {
			if (timeSynDao != null) {
				log.info("getTimeSynTasks...");
				return timeSynDao.getTimeSynTasks();
			}
			return null;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	public List<TimeSynStrategy> getTimeSynStrategy(){
		try {
			if (timeSynDao != null) {
				log.info("getTimeSynStrategy...");
				return timeSynDao.getTimeSynStrategy();
			}
			return null;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	public List<ReadTimeModel> getReadTimeModel(String dwdm ,SystemType systemType){
		//读取需要召测时间的终端、表计
		try {
			if (timeSynDao != null) {
				log.info("getReadTimeModel...");
				if(SystemType.METERBOX.equals(systemType)){
					return timeSynDao.getReadTimeModelForMeterBox(dwdm);
				}else{
					return timeSynDao.getReadTimeModel(dwdm);
				}
			}
			return null;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public List<ReadTimeModel> getRereadTimeModel(String dwdm){
		//读取需要补召时间的终端、表计
		try {
			if (timeSynDao != null) {
				log.info("getRereadTimeModel...");
				return timeSynDao.getRereadTimeModel(dwdm);
			}
			return null;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	/**
	 * 将对时任务状态修改为”1“-已读
	 * @param dwdm
	 * @param rwlx
	 * @param zxsj
	 * @param dqzt
	 * @return
	 */
	public int setTimeSynTaskSuccess(String dwdm , String rwlx , String zxsj){
		//更新对时任务状态为已读
		try {
			if (timeSynDao != null) {
				log.info("setTimeSynTaskSuccess...");
				return timeSynDao.setTimeSynTaskSuccess(dwdm ,rwlx ,zxsj);
			}
			return 0;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	/**
	 * 将对时策略状态修改为给定状态
	 * @param dwdm
	 * @param rwlx
	 * @param zxsj
	 * @return
	 */
	public int setTimeSynStrategyState(String dwdm , String rwlx , String cron , String xgbj){
		//更新对时任务状态为已读
		try {
			if (timeSynDao != null) {
				log.info("setTimeSynStrategyState...");
				return timeSynDao.setTimeSynStrategyState(dwdm ,rwlx ,cron , xgbj);
			}
			return 0;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	public int deleteTimeSynstrategy(String dwdm , String rwlx , String cron){
		//更新对时任务状态为已读
		try {
			if (timeSynDao != null) {
				log.info("deleteTimeSynstrategy...");
				return timeSynDao.deleteTimeSynstrategy(dwdm ,rwlx ,cron);
			}
			return 0;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	public int initReadStatus(String dwdm ,SystemType systemType){
		//更新对时任务状态为已读
		try {
			if (timeSynDao != null) {
				log.info("initReadStatus...");
				timeSynDao.deleteReadStatus(dwdm);
				if(SystemType.METERBOX.equals(systemType)){
					return timeSynDao.initReadStatusForMeterBox(dwdm);
				}else{
					return timeSynDao.initReadStatus(dwdm);
				}
			}
			return 0;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public long getMlId(){
		//获得命令ID
		try {
			if (timeSynDao != null) {
				return timeSynDao.getMlId();
			}
			return 0;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	public long getTaskId(){
		//获得任务ID
		try {
			if (timeSynDao != null) {
				return timeSynDao.getTaskId();
			}
			return 0;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	public int insTask(long taskId, String czyId){
		//获得任务ID
		try {
			if (timeSynDao != null) {
				return timeSynDao.insTask(taskId, czyId);
			}
			return 0;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	public int insMl(long mlId , long taskId, String zdjh , int cldh){
		//获得任务ID
		try {
			if (timeSynDao != null) {
				return timeSynDao.insMl(mlId, taskId, zdjh, cldh);
			}
			return 0;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public int insSzjg(long mlId , String zdjh , int cldh , String sjx){
		//获得任务ID
		try {
			if (timeSynDao != null) {
				return timeSynDao.insSzjg(mlId, zdjh, cldh, sjx);
			}
			return 0;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	/**
	 * 增加自动对时日志
	 */
	public int addTimeSynLog(String rwdwdm,String rwlx,String rwzxsj, String zdljdz,String dwdm,
			int cldh,String fsqqsj,String iszj,String dsbz, int sjcfz){
		//获得任务ID
		try {
			if (timeSynDao != null) {
				return timeSynDao.addTimeSynLog(rwdwdm, rwlx, rwzxsj, zdljdz, dwdm, cldh, fsqqsj, iszj, dsbz, sjcfz);
			}
			return 0;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public TimeSynDao getTimeSynDao() {
		return timeSynDao;
	}

	public void setTimeSynDao(TimeSynDao timeSynDao) {
		this.timeSynDao = timeSynDao;
	}
	
	
}
