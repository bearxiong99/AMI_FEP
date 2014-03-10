package cn.hexing.reread.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import cn.hexing.fk.model.DlmsItemRelated;
import cn.hexing.reread.DlmsRereadApp;
import cn.hexing.reread.dao.LoadDatasDao;
import cn.hexing.reread.model.MasterReread;
import cn.hexing.reread.model.RereadPoint;
import cn.hexing.reread.model.RereadStrategy;
/**
 * 	数据库访问Service层
 * @author Administrator
 *
 */
public abstract class LoadDatasServiceParent implements LoadDatasService{
	protected static final Logger log = Logger.getLogger(LoadDatasServiceParent.class);
	
	protected LoadDatasDao loadDatasDao;

	protected abstract String getProtocol();
	
	public void setLoadDatasDao(LoadDatasDao loadDatasDao) {
		this.loadDatasDao = loadDatasDao;
	}	

	public List<RereadStrategy> getRereadStrategy() {
		try {
			if (loadDatasDao != null) {
				log.info("initRereadStrategy...");
				return loadDatasDao.getRereadStrategy(getProtocol());
			}
			return null;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public List<RereadPoint> getRereadPointByTime(String templateId,
			Object[] timePoints,String rwlx, int jgsj) {
		return this.getRereadPoint(templateId, timePoints, "", getProtocol(),
				24f/24,rwlx , jgsj);
	}

	public List<RereadPoint> getRereadPointByTimeDwdm(String templateId,
			Object[] timePoints, String dwdm,String rwlx ,int jgsj) {
		return this.getRereadPoint(templateId, timePoints, dwdm, getProtocol(),
				24f/24, rwlx ,jgsj);
	}

	/**
	 * 获取漏点
	 * 
	 * @param args
	 *            {模板id-String，时间点数组-Array，单位代码-String（不需要时传空字符串），规约类型-String，
	 *            通讯间隔时间-float（默认为2/24)}
	 * @return
	 */
	public List<RereadPoint> getRereadPoint(Object... args) {
		try {
			if (loadDatasDao != null) {
				List<RereadPoint> list = null;
				if(DlmsRereadApp.dataBaseName_mysql.equals(DlmsRereadApp.dataBaseName)){
					Object[] timeStrs = (Object[])args[1];
					//模板id,时间点开始时间，时间点截止时间，任务间隔时间，单位代码，规约类型，判断是否在线的时间阀值，任务类型
					list = loadDatasDao.getRereadPoint_mysql(args[0], timeStrs[0], timeStrs[timeStrs.length-1], args[6], args[2], args[3], args[4], args[5]);
				}else{
					//模板id,时间点数组，单位代码，规约类型，判断是否在线的时间阀值，任务类型
					list = loadDatasDao.getRereadPoint(args[0], args[1], args[2], args[3], args[4], args[5]);
				}
				return list;
			}
			return null;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public List<MasterReread> getMasterReread() {
		try {
			if (loadDatasDao != null) {
				log.info("initMasterReread...");
				return loadDatasDao.getMasterReread(getProtocol());
			}
			return null;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public List<DlmsItemRelated> loadDlmsItemRelated() {
	    	try{
				if(loadDatasDao!=null){
					log.info("loadDlmsItemRelated...");
					return loadDatasDao.loadDlmsItemRelated();
				}
				return null;
			}catch(Exception ex){
				throw new RuntimeException(ex);
			}
		}
	 

	public int setMasterRereadSuccess(String templateId, Date createTime, String rwlx) {
		return this.setMasterRereadSuccess(templateId, createTime, "1", rwlx);
	}

	public int setMasterRereadSuccess(String templateId, Date createTime,
			String state, String rwlx) {
		try {
			if (loadDatasDao != null) {
				log.info("setMasterRereadSuccess...");
				return loadDatasDao.setMasterRereadSuccess(templateId,
						createTime, state, rwlx);
			}
			return 0;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public int setRereadStrategyXgbj(String xgbj , String templateId, String rwlx) {
		try {
			if (loadDatasDao != null) {
				log.info("setRereadStrategyXgbj...");
				return loadDatasDao.setRereadStrategyXgbj(xgbj, templateId, rwlx);
			}
			return 0;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public int deleteRereadStrategy(String templateId, String rwlx) {
		try {
			if (loadDatasDao != null) {
				log.info("deleteRereadStrategy...");
				return loadDatasDao.deleteRereadStrategy(templateId, rwlx);
			}
			return 0;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	public long getTimeValue(String dt) {
		Date time = null;
		try {
			if (dt.trim().length() == 16) {
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				time = df.parse(dt);
			} else if (dt.trim().length() == 13) {
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH");
				time = df.parse(dt);
			} else if (dt.trim().length() == 10) {
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
				time = df.parse(dt);
			} else if (dt.trim().length() == 7) {
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM");
				time = df.parse(dt);
			}
		} catch (Exception ex) {
			log.error("getTimeValue error,dt=" + dt + " error:"
					+ ex.getLocalizedMessage());
		}
		if (time == null)
			return 0;
		else
			return time.getTime();
	}
}
