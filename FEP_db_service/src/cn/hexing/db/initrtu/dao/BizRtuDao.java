/**
 * �ն�RTU�ĳ�ʼ�����ء��ն˵Ĳ����㡢�����������
 */
package cn.hexing.db.initrtu.dao;

import java.util.List;

import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.model.MeasuredPoint;
import cn.hexing.fk.model.RtuAlertCode;
import cn.hexing.fk.model.RtuTask;
import cn.hexing.fk.model.TaskDbConfig;
import cn.hexing.fk.model.TaskTemplate;

/**
 *
 */
public interface BizRtuDao {
	/**
	 * ����ҵ������ʹ�õ��ն��б���Щ�նˣ�ֻ���ػ������ԡ����ڼ��������ԣ�������أ�Ȼ����װ��
	 * @return
	 */
	List<BizRtu> loadBizRtu();
	
	/**
	 * ����ҵ������ʹ�õ��ն��б���Щ�նˣ�ֻ���ػ������ԡ����ڼ��������ԣ�������أ�Ȼ����װ��
	 * @return
	 */
	List<BizRtu> loadBizGwRtu();
	
	/**
	 * �����ն������б�
	 * Use Case: ��ʼ��Service����loadRtuTask(),����BizRtu�����tasklist Ϊnull��
	 * 			����list����ӵ�list�С�
	 * @return
	 */
	List<RtuTask> loadRtuTasks();
	
	/**
	 * ������վ�����б�.
	 */
	List<RtuTask> loadMasterTasks();
	
	
	/**
	 * �����ն˵Ĳ������б�
	 * @return
	 */
	List<MeasuredPoint> loadMeasuredPoints();

	
	/**
	 * ����ȫ���澯���붨��
	 * @return
	 */
	List<RtuAlertCode> loadRtuAlertCodes();
	
	/**
	 * �������񱣴�����ݿ��������Ϣ��
	 * @return
	 */
	List<TaskDbConfig> loadTaskDbConfig();
	
	/**
	 * ��ʼ������ģ��
	 * @return
	 */
	List<TaskTemplate> loadTaskTemplate();

}
