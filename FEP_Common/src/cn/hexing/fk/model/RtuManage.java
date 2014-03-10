package cn.hexing.fk.model;


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hx.ansi.model.AnsiMeterRtu;



/**
 * �ն˻���
 * 
 */
public class RtuManage {	
	private static final Log log = LogFactory.getLog(RtuManage.class);
	/** ���� */
	private static RtuManage instance;

	/** �ն˵�ȱʡ��Լ:01�㽭��Լ */
	private static String defaultRtuProtocol="01";
	/** ˲ʱ�й���ֵ��ƽ���й���˲ʱ�й���ֵ:��Ϊ�����ϱ����������������洫�����*/
	private static SysConfig sysConfig;
	
	/** �ն��߼���ַ��ͨѶǰ�û��ն˶��ձ�[rtua - ComRtu] */
	private static Map<Integer,ComRtu> comRtuMap=new HashMap<Integer,ComRtu>(102400);
	
	/** ͨ���ն�ID����ҵ�������ն��߼���ַ[rtuId -> rtua] */
	private static Map<String,Integer> bizRtuaIdMap=new HashMap<String,Integer>(102400);
	/** �ն��߼���ַ��ҵ�������ն˶��ձ�[rtua - BusRtu] */
	private static Map<Integer,BizRtu> bizRtuMap=new HashMap<Integer,BizRtu>(102400);
	/** ����ģ��ID������ģ����ձ�[taskPlateID - TaskPlate] */
	private static Map<Integer,TaskTemplate> taskPlateMap=new HashMap<Integer,TaskTemplate>();
	/** �澯������澯�������֮��Ķ��ձ�[code - RtuAlertCode] */
    private static Map<Integer,RtuAlertCode> alertCodeMap=new HashMap<Integer,RtuAlertCode>();
    /** ���񱣴����ݿ��Ӱ����Ϣ���ձ�[code - TaskSaveInfo] */
    private static Map<String,TaskDbConfig> taskDbConfigMap=new HashMap<String,TaskDbConfig>();
    /** �ն�Զ�������ǼǱ�[rtuaInt - String] */
    private static Map<Integer,Boolean> rtuRemoteUpdateMap=new HashMap<Integer,Boolean>();
    
	private static Map<String,DlmsMeterRtu> dlmsMeterRtuMap = new HashMap<String, DlmsMeterRtu>();
	private static Map<String,AnsiMeterRtu> ansiMeterRtuMap = new HashMap<String, AnsiMeterRtu>();
	/** meterId->logicAddress*/
	private static Map<String,String> dlmsMeterIdMap = new HashMap<String, String>(102400);
	/** meterId->logicAddress*/
	private static Map<String,String> ansiMeterIdMap = new HashMap<String, String>(102400);
	/**
	 * ����һ���ն˻���
	 */
	private RtuManage() {		
		// ���ػ���
		//init();				
	}
	
	/**
	 * ȡ���ն˻��������������δ��ʼ�������ڳ�ʼ���󷵻�
	 * 
	 * @return �ն˻������
	 */
	public static RtuManage getInstance() {
		if (instance == null) {
			synchronized (RtuManage.class) {
				if (instance == null) {
					instance = new RtuManage();
				}
			}
		}
		return instance;
	}
	
	/**
	 * ����ն˵�ͨѶ��Լ
	 * 
	 * @param rtu
	 *            �ն�
	 */
	private static void checkProtocol(BizRtu rtu) {
		if (rtu.getRtuProtocol() == null) {
			rtu.setRtuProtocol(defaultRtuProtocol);
		}
	}
	
	
	/**
	 * ֱ���ڻ����в���ͨѶ�ն˵����������Դ����ݿ��в���
	 * 
	 * @param rtua
	 *            �ն��߼���ַ
	 * @return ƥ����նˡ��������ڣ��򷵻� null
	 */
	public ComRtu getComRtuInCache(int rtua) {
		return (ComRtu) comRtuMap.get(new Integer(rtua));
	}
	
	public Collection<ComRtu> getAllComRtu(){
		return comRtuMap.values();
	}
	
	public Map<Integer,ComRtu> getComRtuMap(){
		return comRtuMap;
	}
	/**
	 * ֱ���ڻ����в���ҵ���ն˵����������Դ����ݿ��в���
	 * 
	 * @param rtua
	 *            �ն��߼���ַ
	 * @return ƥ����նˡ��������ڣ��򷵻� null
	 */
	public BizRtu getBizRtuInCache(int rtua) {
		return (BizRtu) bizRtuMap.get(new Integer(rtua));
	}
	/**
	 * ֱ���ڻ����в���dlms��Ƶ���
	 * @param logicAddress  �߼���ַ
	 * @return
	 */
	public DlmsMeterRtu getDlmsMeterRtu(String logicAddress)
	{
		return dlmsMeterRtuMap.get(logicAddress);
	}
	/**
	 * ͨ����ƾֺ��ҵ�Dlms��
	 */
	public DlmsMeterRtu getDlmsMeterRtuById(String rtuId){
		return dlmsMeterRtuMap.get(dlmsMeterIdMap.get(rtuId));
	}
	/**
	 * ֱ���ڻ����в���ansi��Ƶ���
	 * @param logicAddress  �߼���ַ
	 * @return
	 */
	public AnsiMeterRtu getAnsiMeterRtu(String logicAddress)
	{
		return ansiMeterRtuMap.get(logicAddress);
	}
	/**
	 * ͨ����ƾֺ��ҵ�Ansi��
	 */
	public AnsiMeterRtu getAnsiMeterRtuById(String rtuId){
		return ansiMeterRtuMap.get(ansiMeterIdMap.get(rtuId));
	}
	/**
	 * ֱ���ڻ����в���ҵ���ն˵����������Դ����ݿ��в���
	 * 
	 * @param rtuId
	 *            �ն˾ֺ�
	 * @return ƥ����նˡ��������ڣ��򷵻� null
	 */
	public BizRtu getBizRtuInCache(String rtuId) {
		return (BizRtu) bizRtuMap.get(bizRtuaIdMap.get(rtuId));
	}
	/**
	 * ���»����е�ҵ�������ն˵���
	 * 
	 * @param BizRtu
	 */
	public synchronized void putBizRtuToCache(BizRtu bizRtu) {
		try {				
			checkProtocol(bizRtu);
			bizRtu.setRtua((int)Long.parseLong(bizRtu.getLogicAddress(), 16));
			bizRtuaIdMap.put(bizRtu.getRtuId(), new Integer(bizRtu.getRtua()));		
			bizRtuMap.put(new Integer(bizRtu.getRtua()), bizRtu);		
		} catch (Exception ex) {
			log.debug("Error to put BizRtu: " + bizRtu.toString());
		}
	}
	
	public synchronized void putDlmsMeterRtuToCache(DlmsMeterRtu meterRtu){
		dlmsMeterIdMap.put(meterRtu.getMeterId(), meterRtu.getLogicAddress());
		dlmsMeterRtuMap.put(meterRtu.getLogicAddress(), meterRtu);
	}
	public synchronized void putAnsiMeterRtuToCache(AnsiMeterRtu meterRtu){
		ansiMeterIdMap.put(meterRtu.getMeterId(), meterRtu.getLogicAddress());
		ansiMeterRtuMap.put(meterRtu.getLogicAddress(), meterRtu);
	}
	/**
	 * ���»����е�ͨѶǰ�û��ն˵���
	 * 
	 * @param BizRtu
	 */
	public synchronized void putComRtuToCache(ComRtu comRtu) {
		try {				
			comRtu.setRtua((int)Long.parseLong(comRtu.getLogicAddress(), 16));				
			comRtuMap.put(new Integer(comRtu.getRtua()), comRtu);		
		} catch (Exception ex) {
			log.debug("Error to put ComRtu: " + comRtu.toString(), ex);
		}
	}
	/**
	 * ����Dlms������������ڻ����С�
	 * @param mp
	 */
	public synchronized void putMeasurePointToDlmsCache(MeasuredPoint mp){
		DlmsMeterRtu dlmsRtu = getDlmsMeterRtuById(mp.getRtuId());
		if(dlmsRtu ==null){
//			log.debug("Can't find dlmsRtu when loading MeasuredPoint: "
//					+ mp.toString());
			return;
		}
		dlmsRtu.addMeasuredPoint(mp);
		
	}
	/**
	 * ����ansi������������ڻ����С�
	 * @param mp
	 */
	public synchronized void putMeasurePointToAnsiCache(MeasuredPoint mp){
		AnsiMeterRtu ansiRtu = getAnsiMeterRtuById(mp.getRtuId());
		if(ansiRtu ==null){
//			log.debug("Can't find dlmsRtu when loading MeasuredPoint: "
//					+ mp.toString());
			return;
		}
		ansiRtu.addMeasuredPoint(mp);
		
	}
	/**
	 * ���»����е�ҵ�������ն˲����㵵��
	 * 
	 * @param MeasuredPoint
	 */
	public synchronized void putMeasuredPointToCache(MeasuredPoint mp) {
		try {				
			BizRtu bizRtu = getBizRtuInCache(mp.getRtuId());
			if (bizRtu == null) {
//				log.debug("Can't find busRtu when loading MeasuredPoint: "
//						+ mp.toString());
				return;
			}
			bizRtu.addMeasuredPoint(mp);	
		} catch (Exception ex) {
			log.debug("Error to put MeasuredPoint: " + mp.toString(), ex);
		}
	}
	/**
	 * ���ն��������վ����洢�ڻ�����
	 * @param rt
	 */
	public synchronized void putTaskToDlmsCache(RtuTask rt) {
		DlmsMeterRtu dlmsRtu = getDlmsMeterRtuById(rt.getRtuId());

		if (dlmsRtu == null) {
			return;
		}
		dlmsRtu.addRtuTask(rt);
	}
	/**
	 * ���ն��������վ����洢�ڻ�����ANSI
	 * @param rt
	 */
	public synchronized void putTaskToAnsiCache(RtuTask rt) {
		AnsiMeterRtu dlmsRtu = getAnsiMeterRtuById(rt.getRtuId());

		if (dlmsRtu == null) {
			return;
		}
		dlmsRtu.addRtuTask(rt);
	}
	/**
	 * ���»����е�ҵ�������ն˲����㵵��
	 * 
	 * @param RtuTask
	 */
	public synchronized void putRtuTaskToCache(RtuTask rt) {
		try {				
			BizRtu bizRtu = getBizRtuInCache(rt.getRtuId());
			if (bizRtu == null) {
				return;
			}
			bizRtu.addRtuTask(rt);	
		} catch (Exception ex) {
			log.debug("Error to put RtuTask: " + rt.toString(), ex);
		}
	}
	/**
	 * ֱ���ڻ����в�������ģ����Ϣ�������Դ����ݿ��в���
	 * 
	 * @param rtua
	 *            �ն��߼���ַ
	 * @return ƥ����նˡ��������ڣ��򷵻� null
	 */
	public TaskTemplate getTaskPlateInCache(String taskPlateID) {
		return (TaskTemplate) taskPlateMap.get(new Integer(taskPlateID));
	}
	/**
	 * ֱ���ڻ����в����쳣Я����������Ϣ�������Դ����ݿ��в���
	 * 
	 * @param rtua
	 *            �ն��߼���ַ
	 * @return ƥ����նˡ��������ڣ��򷵻� null
	 */
	public RtuAlertCode getRtuAlertCode(int code) {
		return (RtuAlertCode) alertCodeMap.get(new Integer(code));
	}
	/**
	 * ���»����е�ҵ����������ģ�浵��
	 * 
	 * @param TaskTemplate
	 */
	public synchronized void putTaskTemplateToCache(TaskTemplate tp) {
		try {				
			taskPlateMap.put(new Integer(tp.getTaskTemplateID()), tp);
		} catch (Exception ex) {
			log.debug("Error to put TaskPlate: " + tp.toString(), ex);
		}
	}
	/**
	 * ���»����е�ҵ����������ģ���������
	 * 
	 * @param TaskTemplateItem
	 */
	public synchronized void putTaskTemplateItemToCache(TaskTemplateItem tpi) {
		try {							
			TaskTemplate tp = (TaskTemplate)taskPlateMap.get(new Integer(tpi.getTaskTemplateID()));
			if (tp == null) {
				log.debug("Can't find TaskPlate when loading TaskPlateItem: "
								+ tpi.toString());
				return;
			}
			tp.addDataCode(tpi.getCode());
		} catch (Exception ex) {
			log.debug("Error to put TaskPlateItem: " + tpi.toString(), ex);
		}
	}	
	/**
	 * ֱ���ڻ����в������񱣴����ݿ��Ӱ����Ϣ�������Դ����ݿ��в���
	 * 
	 * @param rtua
	 *            �ն��߼���ַ
	 * @return ƥ����նˡ��������ڣ��򷵻� null
	 */
	public TaskDbConfig getTaskDbConfigInCache(String key) {
		return (TaskDbConfig) taskDbConfigMap.get(key);
	}
	/**
	 * ���»����е�ҵ���������񱣴�ӳ����Ϣ����
	 * 
	 * @param TaskDbConfig
	 */
	public synchronized void putTaskDbConfigToCache(TaskDbConfig tsi) {
		try {							
			tsi.setDbConfigStr(tsi.getDbConfigStr());
			taskDbConfigMap.put(tsi.getCode(), tsi);
		} catch (Exception ex) {
			log.debug("Error to put TaskDbConfig: " + tsi.toString(), ex);
		}
	}
	
	/**
	 * ���»����е�ҵ�������쳣Я���������
	 * 
	 * @param RtuAlertCode
	 */
	public synchronized void putAlertCodeToCache(RtuAlertCode rac) {
		try {							
			alertCodeMap.put(new Integer(Integer.parseInt(rac.getCode(), 16)), rac);
		} catch (Exception ex) {
			//log.debug("Error to put RtuAlertCode: " + rac.toString(), ex);
		}
	}
	/**
	 * �Ǽ�Զ�������ն��߼���ַ
	 * 
	 * @param rtua
	 */
	public synchronized void putRemoteUpateRtuaToCache(String rtua) {
		try {							
			rtuRemoteUpdateMap.put(new Integer((int)Long.parseLong(rtua, 16)), true);
		} catch (Exception ex) {
			log.debug("Error to put RemoteUpateRtua: " + rtua, ex);
		}
	}
	/**
	 * ��ȡ�ն˵�ַ�Ƿ�����������־
	 * 
	 * @param rtua
	 */
	public boolean getRemoteUpateRtuaTag(String rtua) {
		if (rtuRemoteUpdateMap.get(new Integer((int)Long.parseLong(rtua, 16)))!=null)
			return rtuRemoteUpdateMap.get(new Integer((int)Long.parseLong(rtua, 16)));
		else
			return false;
	}
	/**
	 * ��ȡ�ն˵�ַ�Ƿ�����������־
	 * 
	 * @param rtua
	 */
	public boolean getRemoteUpateRtuaTag(int rtua) {
		if (rtuRemoteUpdateMap.get(new Integer(rtua))!=null)
			return rtuRemoteUpdateMap.get(new Integer(rtua));
		else
			return false;
	}
	/**
	 * �Ǽ�Զ�������ն��߼���ַ
	 * 
	 * @param rtua
	 */
	public synchronized void clearRtuRemoteUpdateMap() {						
		rtuRemoteUpdateMap.clear();		
	}
	public SysConfig getSysConfig() {
		return sysConfig;
	}

	public void setSysConfig(SysConfig myConfig) {
		sysConfig = myConfig;
	}

}
