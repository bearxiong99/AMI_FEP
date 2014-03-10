package cn.hexing.fk.bp.dlms.persisit;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;

import cn.hexing.db.batch.AsyncService;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fk.bp.dlms.util.DlmsConstant;
import cn.hexing.fk.bp.model.AlarmData;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.model.DlmsMeterRtu;
import cn.hexing.fk.model.MeasuredPoint;
import cn.hexing.fk.model.RtuManage;
import cn.hexing.fk.utils.DateConvert;

import com.hx.dlms.applayer.eventnotification.DlmsAlarmItem;

/**
 * Dlms告警信息处理 
 */
public class DlmsAlarmMessageService {
	
	private static final Logger log = Logger.getLogger(DlmsAlarmMessageService.class);
	
	private static final DlmsAlarmMessageService instance = new DlmsAlarmMessageService();
	private AsyncService service;
	private DlmsAlarmMessageService(){}
	
	public void init(){};
	
	
	public static DlmsAlarmMessageService getInstance(){return instance;};
	
	
	public void operationDatabase(DlmsRequest request,DlmsAlarmItem[] alarms){
		String logicAddress = request.getMeterId();
		MeasuredPoint mp=null;
		if(request.isRelay()){
			logicAddress = request.getRelayParam().getDcLogicalAddress();
			BizRtu bizRtu = RtuManage.getInstance().getBizRtuInCache((int)Long.parseLong(logicAddress,16));
			mp=bizRtu.getMeasuredPoint(""+request.getRelayParam().getMeasurePoint());
		}else{
			DlmsMeterRtu meterRtu = RtuManage.getInstance().getDlmsMeterRtu(logicAddress);
			if(meterRtu==null){
				throw new RuntimeException("Can't find this meter in dataBase,meterId:"+logicAddress);
			}
			mp=meterRtu.getMeasuredPoint("0");
		}
		if(mp ==null){
			throw new RuntimeException("Can't find this meter's sjid. MeterId:"+logicAddress);
		}
		String dataSaveId = mp.getDataSaveID();
		if(alarms!=null){
			StringBuffer alarmSb = new StringBuffer();
			for(DlmsAlarmItem alarm : alarms){
				
				if(alarm == null) continue;


				String occurTime = alarm.getTime();
				//iran time to gregorian???
				occurTime = DlmsConstant.getInstance().isIranTime?DateConvert.iranToGregorian(occurTime):occurTime;
				
				
				String alarmCode = alarm.getAlarmCode();//告警编码
				StringBuilder sb = new StringBuilder();
				if(alarm.getRelatedData() !=null){
					
					HashMap<String, String> relatedData=alarm.getRelatedData();
					
					for(String str:relatedData.keySet()){
						sb.append(str+"="+relatedData.get(str)+";");
					}
					sb.deleteCharAt(sb.length()-1);
				}
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date date = null;
				try {
					date = sdf.parse(occurTime);
				} catch (ParseException e) {}
				AlarmData alarmData=new AlarmData();	
				alarmData.setSbcs(sb.toString());
				alarmData.setDataSaveID(new Long(dataSaveId));
				alarmData.setAlertTime(date);
				if("Event_Read".equals(request.getOperator())){//主站召测
					alarmData.setGjly("2");
				}
				alarmData.setAlertCodeHex(alarmCode);
				alarmData.setReceiveTime(new Date());
				service.addToDao(alarmData, 4000);
				if(log.isDebugEnabled())
					alarmSb.append(alarm).append(",");
			}
			if(log.isDebugEnabled() && alarmSb.length()>0){
				alarmSb=alarmSb.deleteCharAt(alarmSb.length()-1);
				log.debug("meterId:"+request.getMeterId()+" alarmIds:"+alarmSb);
			}
			
		}
		
	}


	public final void setService(AsyncService service) {
		this.service = service;
	}
	

	
	
}
