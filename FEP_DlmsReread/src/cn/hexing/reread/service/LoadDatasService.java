package cn.hexing.reread.service;

import java.util.Date;
import java.util.List;

import cn.hexing.fk.model.DlmsItemRelated;
import cn.hexing.reread.model.MasterReread;
import cn.hexing.reread.model.RereadPoint;
import cn.hexing.reread.model.RereadStrategy;
/**
 * 漏点补召通用数据库访问接口
 * @ClassName:LoadDataService
 * @Description:TODO
 * @author kexl
 * @date 2012-12-11 上午10:09:31
 *
 */
public interface LoadDatasService {
	public List<RereadStrategy> getRereadStrategy();
	
	public List<RereadPoint> getRereadPointByTime(String templateId,
			Object[] timePoints,String rwlx ,int jgsj);
	
	public List<RereadPoint> getRereadPointByTimeDwdm(String templateId,
			Object[] timePoints, String dwdm,String rwlx , int jgsj);
	
	/**
	 * 获取漏点
	 * 
	 * @param args
	 *            {模板id-String，时间点数组-Array，单位代码-String（不需要时传空字符串），规约类型-String，
	 *            通讯间隔时间-float（默认为2/24)}
	 * @return
	 */
	public List<RereadPoint> getRereadPoint(Object... args) ;
	
	public List<MasterReread> getMasterReread();
	
	public List<DlmsItemRelated> loadDlmsItemRelated() ;
	
	public int setMasterRereadSuccess(String templateId, Date createTime, String rwlx) ;
	
	public int setMasterRereadSuccess(String templateId, Date createTime,String state, String rwlx);
	
	public int setRereadStrategyXgbj(String xgbj , String templateId ,String rwlx);
	
	public int deleteRereadStrategy(String templateId ,String rwlx);
}
