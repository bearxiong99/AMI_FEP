package cn.hexing.dp.dao;

import java.util.List;

import cn.hexing.dp.model.RtuTask;
import cn.hexing.dp.model.TaskCode;
import cn.hexing.dp.model.TaskTemplate;
import cn.hexing.fk.model.DlmsItemRelated;
import cn.hexing.fk.model.DlmsMeterRtu;

public interface LoadDatasDao {
	/**�������ģ��*/
	public List<TaskTemplate> getTaskTemplate(String protocol);
	/**���������Ϣ*/
	public List<RtuTask> getRtuTask(String protocol);
	/**���Dlms������������Ķ�Ӧ��ϵ*/
	public List<DlmsItemRelated> loadDlmsItemRelated();
	/**�����վ����*/
	public List<RtuTask> getMasterTask(String protocol);
	/**����rtuId�����վ����*/
	public List<RtuTask> getMasterTaskById(String protocol,String rtuId);
	/**�����վ����ģ��*/
	public List<TaskTemplate> getMasterTaskTemplate(String protocol);
	/**����ģ��id�����վ����*/
	public List<TaskTemplate> getMasterTaskTemplateById(String protocol,String mbid);
	/**���ģ���Ӧ��������*/
	public List<TaskCode> getTaskCodes();
	/**����id�������ģ��*/
	public List<TaskTemplate> getTaskTemplateById(String protocol,String mbid);
	/**����rtuID�������*/
	public List<RtuTask> getRtuTask(String protocol,String rtuId);
	/**���24Сʱ���ߵ�DLMS��*/
	public List<DlmsMeterRtu> get24HourOnlineMeter();
}
