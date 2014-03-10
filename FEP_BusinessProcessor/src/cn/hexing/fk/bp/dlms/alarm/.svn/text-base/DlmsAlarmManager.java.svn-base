/**
 * 
 */
package cn.hexing.fk.bp.dlms.alarm;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import cn.hexing.fk.bp.dlms.DlmsEventProcessor;
import cn.hexing.fk.bp.dlms.events.DlmsEvent;
import cn.hexing.fk.bp.dlms.persisit.JdbcDlmsDao;
import cn.hexing.fk.bp.model.DlmsAlarmStatus;

import com.hx.dlms.DecodeStream;
import com.hx.dlms.aa.DlmsContext;
import com.hx.dlms.aa.AarqApdu.CipherMechanism;
import com.hx.dlms.applayer.eventnotification.DlmsAlarmItem;
import com.hx.dlms.applayer.eventnotification.DlmsEventAlarmResolver;
import com.hx.dlms.applayer.eventnotification.EventNotificationRequest;

/**
 * @author Bao Hongwei
 *
 */
public class DlmsAlarmManager {
	public static final Logger log = Logger.getLogger(DlmsAlarmManager.class);
	private static DlmsAlarmManager instance = null;
	private AlarmRegisterNotification alarmNotify;
	
	private AlarmRegisterNotification oldAlarmNotify;
	
	private Map<String,List<DlmsAlarmStatus>> mapAlarmStatus;
	
	String[] oldProtocols = null;
	
	private JdbcDlmsDao alarmDao;
	
	public static final DlmsAlarmManager getInstance(){
		if( null == instance )
			instance = new DlmsAlarmManager();
		return instance;
	}
	
	public void init(){
		mapAlarmStatus = new HashMap<String,List<DlmsAlarmStatus>>();
		List<DlmsAlarmStatus> allAlarmStatus=alarmDao.loadDlmsAlarmStatus();
		for(DlmsAlarmStatus status:allAlarmStatus){
			List<DlmsAlarmStatus> alarmStatus = mapAlarmStatus.get(status.getLogicAddress());
			if(alarmStatus==null) alarmStatus = new ArrayList<DlmsAlarmStatus>();
			alarmStatus.add(status);
			mapAlarmStatus.put(status.getLogicAddress(), alarmStatus);
		}
	}
	
	private DlmsAlarmManager(){
		String strwithOutReadProtocols=System.getProperty("bp.protocol.old");
		if(strwithOutReadProtocols!=null && !"".equals(strwithOutReadProtocols)){
			oldProtocols = strwithOutReadProtocols.split(",");
		}

	}

	public DlmsAlarmStatus getAlarmStatus(String logicAddr,String obis){
		List<DlmsAlarmStatus> listMap = mapAlarmStatus.get(logicAddr);
		if(listMap==null) return null;
		for(DlmsAlarmStatus status:listMap){
			if(status.getObis().equals(obis))
				return status;
		}
		return null;
	}
	
	public void onEventNotificationRequest( ByteBuffer notifyReqApdu, DlmsEvent event, DlmsContext context){
		
		if(context==null){
			log.error("context is null. process alarm error.");
			return ;
		}
		
		DlmsEventProcessor processor = DlmsEventProcessor.getInstance();
		EventNotificationRequest er = new EventNotificationRequest();
		try {
			er.decode(DecodeStream.wrap(notifyReqApdu));
			int classid = er.getAttributeDescriptor().getClassId();
			int attrid = er.getAttributeDescriptor().getAttributeId();
			String obis = er.getAttributeDescriptor().getInstanceIdAsString();
			StringBuilder sb = new StringBuilder(64);
			sb.append(classid).append(".").append(obis).append(".").append(attrid).append("|");
			if( er.getAttributeDescriptor().getClassId() == 7 ){
				//Event notify, not register status word
				DlmsAlarmItem[] alarms = DlmsEventAlarmResolver.getInstance().resolveRunningEventArray(sb.toString(), obis, er.getAttributeValue());
				for( DlmsAlarmItem a : alarms ){
					if(log.isDebugEnabled())
						log.debug("DlmsAlarmItem:"+a);					
				}
				//Save the events into DB
			}else if( obis.equals("0.0.97.98.0.255") || 
					 obis.equals("0.0.97.129.0.255")){ //Alarm register notification
				if(isOldProtocol(context)){
					oldAlarmNotify.read(context, processor, er);
				}else if(context.getCipherMechanism()==CipherMechanism.BENGAL){
					alarmNotify.save(context,processor,er);
				}else{
					alarmNotify.read(context, processor, er);					
				}
			}else if(obis.equals("0.0.97.138.0.255")){//Collector Alarm
				alarmNotify.save(context,processor,er);
			}else{
				log.warn("classId-obis-attrId:"+classid+"-"+obis+"-"+attrid+",this event unsupport!");
			}
		} catch (IOException e) {
			log.error("Decode event-notification exception,meter="+context.meterId, e);
			return;
		}
	}

	/**
	 * 是否是伊朗老规约
	 * @param context
	 * @return
	 */
	public boolean isOldProtocol(DlmsContext context){
		
		if(context==null){
			log.error("no context for this alarm");
			return false;
		} 
		
		if(context.subProtocol!=null && oldProtocols!=null){
			for(String sub : oldProtocols){
				if(sub.equals(context.subProtocol)){
					return true;
				}
			}
		}
		return false;
	}
	
	public final AlarmRegisterNotification getAlarmNotify() {
		return alarmNotify;
	}

	public final void setAlarmNotify(AlarmRegisterNotification alarmNotify) {
		this.alarmNotify = alarmNotify;
	}

	public final AlarmRegisterNotification getOldAlarmNotify() {
		return oldAlarmNotify;
	}

	public final void setOldAlarmNotify(AlarmRegisterNotification oldAlarmNotify) {
		this.oldAlarmNotify = oldAlarmNotify;
	}

	public final JdbcDlmsDao getAlarmDao() {
		return alarmDao;
	}

	public final void setAlarmDao(JdbcDlmsDao alarmDao) {
		this.alarmDao = alarmDao;
	}
	
	public void updateDlmsAlarmStatus(DlmsAlarmStatus alarmStatus) {
		List<DlmsAlarmStatus> tempAlarmStatuss = mapAlarmStatus.get(alarmStatus.getLogicAddress());
		if(tempAlarmStatuss == null) tempAlarmStatuss = new ArrayList<DlmsAlarmStatus>();
		DlmsAlarmStatus tempStatus = null;
		for(DlmsAlarmStatus status:tempAlarmStatuss){
			if(status.getObis().equals(alarmStatus.getObis())){
				tempStatus = status;
				break;
			}
		}
		if(tempStatus==null){
			tempStatus = alarmStatus;
			tempAlarmStatuss.add(tempStatus);
		}else{
			tempStatus.setLastReportTime(alarmStatus.getLastReportTime());
		}
		mapAlarmStatus.put(alarmStatus.getLogicAddress(), tempAlarmStatuss);
	}

}
