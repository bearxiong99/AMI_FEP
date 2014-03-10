package cn.hexing.reread.dao;

import java.util.List;

import cn.hexing.reread.model.ReadTimeModel;
import cn.hexing.reread.model.TimeSynStrategy;
import cn.hexing.reread.model.TimeSynTask;

/**
 * 数据库访问Dao层
 * @ClassName:LoadDatasDao
 * @Description:TODO
 * @author kexl
 * @date 2012-9-24 上午10:59:52
 *
 */
public interface TimeSynDao {
	public List<TimeSynTask> getTimeSynTasks();
	
	public List<ReadTimeModel> getReadTimeModel(String dwdm); //读取需要召测时间的终端、表计
	
	public List<ReadTimeModel> getReadTimeModelForMeterBox(String dwdm); //读取需要召测时间的终端、表计
	
	public int initReadStatus(String dwdm);//初始化对应终端的召测状态为‘0’
	
	public int initReadStatusForMeterBox(String dwdm);//初始化对应终端的召测状态为‘0’
	
	public List<ReadTimeModel> getRereadTimeModel(String dwdm);//读取需要补召时间的终端、表计
	
	public int setTimeSynTaskSuccess(String dwdm , String rwlx , String zxsj); //更新对时任务状态为已读
	
	public long getMlId();//获取一个命令ID
	
	public long getTaskId();//获取一个命令ID
	
	public int insMl(long mlId, long taskId , String zdjh , int cldh);//新建一个ml
	
	public int insSzjg(long mlId, String zdjh , int cldh , String sjx);//新建一个ml
	
	public int insTask(long taskId , String czyId);//新建一个task
	
	public int addTimeSynLog(String rwdwdm,String rwlx,String rwzxsj, String zdljdz,String dwdm,
			int cldh,String fsqqsj,String iszj,String dsbz, int sjcfz);
	
	public int deleteReadStatus(String dwdm);//删除对应单位下的所有对时数据

	public List<TimeSynStrategy> getTimeSynStrategy();

	public int setTimeSynStrategyState(String dwdm, String rwlx, String cron, String xgbj);

	public int deleteTimeSynstrategy(String dwdm, String rwlx, String cron);

}
