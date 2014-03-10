package cn.hexing.db.initrtu.dao;

import java.util.List;

import cn.hexing.fk.model.DlmsMeterRtu;
import cn.hexing.fk.model.MeasuredPoint;
import cn.hexing.fk.model.RtuTask;

public interface DlmsMeterRtuDao {
	/**���Dlms����*/
	public List<DlmsMeterRtu> loadDlmsMeterRtu();
	
	/**���ز�����*/
	public List<MeasuredPoint> loadMeasuredPoints();
	
	/**�����ն�����*/
	public List<RtuTask> loadRtuTasks();
	
	/**������վ����*/
	public List<RtuTask> loadMasterTasks();
}
