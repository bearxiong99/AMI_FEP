package cn.hexing.dp.dao;

import java.util.List;

import cn.hexing.dp.model.RtuTask;
import cn.hexing.dp.model.TaskCode;
import cn.hexing.dp.model.TaskTemplate;
import cn.hexing.fk.model.DlmsItemRelated;
import cn.hexing.fk.model.DlmsMeterRtu;

public interface LoadDatasDao {
	/**获得任务模板*/
	public List<TaskTemplate> getTaskTemplate(String protocol);
	/**获得任务信息*/
	public List<RtuTask> getRtuTask(String protocol);
	/**获得Dlms数据项与内码的对应关系*/
	public List<DlmsItemRelated> loadDlmsItemRelated();
	/**获得主站任务*/
	public List<RtuTask> getMasterTask(String protocol);
	/**根据rtuId获得主站任务*/
	public List<RtuTask> getMasterTaskById(String protocol,String rtuId);
	/**获得主站任务模板*/
	public List<TaskTemplate> getMasterTaskTemplate(String protocol);
	/**根据模板id获得主站任务*/
	public List<TaskTemplate> getMasterTaskTemplateById(String protocol,String mbid);
	/**获得模板对应的数据项*/
	public List<TaskCode> getTaskCodes();
	/**根据id获得任务模板*/
	public List<TaskTemplate> getTaskTemplateById(String protocol,String mbid);
	/**根据rtuID获得任务*/
	public List<RtuTask> getRtuTask(String protocol,String rtuId);
	/**获得24小时在线的DLMS表*/
	public List<DlmsMeterRtu> get24HourOnlineMeter();
}
