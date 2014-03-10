package cn.hexing.reread.dao;

import java.util.Date;
import java.util.List;

import cn.hexing.fk.model.DlmsItemRelated;
import cn.hexing.reread.model.MasterReread;
import cn.hexing.reread.model.RereadPoint;
import cn.hexing.reread.model.RereadStrategy;

/**
 * 数据库访问Dao层
 * @ClassName:LoadDatasDao
 * @Description:TODO
 * @author kexl
 * @date 2012-9-24 上午10:59:52
 *
 */
public interface LoadDatasDao {
	public List<RereadStrategy> getRereadStrategy(String protocol);
	public List<RereadPoint> getRereadPoint(Object... args);
	public List<RereadPoint> getRereadPoint_mysql(Object... args);
	
	public List<MasterReread> getMasterReread(String protocol);
	
	public int setMasterRereadSuccess(String templateId , Date createTime, String state,String rwlx);
	
	public List<DlmsItemRelated> loadDlmsItemRelated();
	
	public int setRereadStrategyXgbj(String xgbj , String templateId ,String rwlx);
	
	public int deleteRereadStrategy(String templateId ,String rwlx);
}
