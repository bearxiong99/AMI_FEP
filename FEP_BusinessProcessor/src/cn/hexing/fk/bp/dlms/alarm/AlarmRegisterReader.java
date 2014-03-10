package cn.hexing.fk.bp.dlms.alarm;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fas.model.dlms.DlmsRequest.DLMS_OP_TYPE;
import cn.hexing.fk.bp.dlms.DlmsEventProcessor;
import cn.hexing.fk.bp.dlms.util.DlmsConstant;
import cn.hexing.fk.bp.model.DlmsAlarmStatus;
import cn.hexing.fk.utils.DateConvert;

import com.hx.dlms.aa.DlmsContext;
import com.hx.dlms.applayer.SelectiveAccessDescriptor;

public class AlarmRegisterReader {
	
	private String [] oldProtocol= null;
	
	private static AlarmRegisterReader instance = new AlarmRegisterReader();
	
	private AlarmRegisterReader(){
		String strOldProtocol  = System.getProperty("bp.protocol.old");
		if(strOldProtocol!=null){
			oldProtocol = strOldProtocol.split(",");
		}
	}
	
	public static AlarmRegisterReader getInstance(){
		if(instance == null){
			instance = new AlarmRegisterReader();
		}
		return instance;
	}
	
	public void readAlarm(DlmsEventProcessor processor, DlmsObisItem[] params,
			DlmsRequest req,DlmsContext context,String dateTime) {
		
		boolean isIranTime = DlmsConstant.getInstance().isIranTime;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if(isIranTime&& !isOldProtocol(context.subProtocol)){//伊朗老表上报时间是公历,这里就不要将伊朗历转为公历了。
			dateTime = DateConvert.iranToGregorian(dateTime);
		}
		Date occurTime = null;
		try {
			occurTime= sdf.parse(dateTime);
			req.addAppendParam("EventOccurTime", occurTime);
			req.addAppendParam("OBIS", params[0].obisString);
		} catch (ParseException e) {
		}
		req.setOperator("EVENT_READING_AUTO");
		req.setOpType(DLMS_OP_TYPE.OP_GET);
		params[0].accessSelector = 1;
		params[0].classId = 7;
		params[0].attributeId = 2;
		req.setMeterId( context.meterId );
		SelectiveAccessDescriptor sad = new SelectiveAccessDescriptor();
		String fromTime = sdf.format(occurTime.getTime()-1*1000*60);
		String now = sdf.format(occurTime.getTime()+1*1000*60);
		DlmsAlarmStatus alarmStatus=DlmsAlarmManager.getInstance().getAlarmStatus(context.meterId, params[0].obisString);
		if(alarmStatus!=null){
//-------------------包含最大读事件间隔--------------------------------------------------
//			Calendar rc = Calendar.getInstance();
//			rc.setTime(alarmStatus.getLastReportTime());
//			Calendar fc = Calendar.getInstance();
//			fc.setTime(occurTime);
//			rc.add(Calendar.HOUR, DlmsConstant.getInstance().alarmTimeReadInterval);
//			if(!rc.before(fc)){//读事件的间隔不能太长。
//				fromTime=sdf.format(alarmStatus.getLastReportTime());				
//			}
//-------------------不包含最大读事件间隔------------------------------------------------			
			Calendar fc = Calendar.getInstance();
			fc.setTime(occurTime);
			fromTime=sdf.format(alarmStatus.getLastReportTime());				
		}
		//事件上报的帧里，含有时间，可以利用这个时间进行抄读
		fromTime=isIranTime?DateConvert.gregorianToIran(fromTime):fromTime;
		now=isIranTime?DateConvert.gregorianToIran(now):now;
		sad.selectByPeriodOfTime(fromTime, now,isOldProtocol(context.subProtocol));
		params[0].data.assignValue(sad.getParameter());
		req.setSubprotocol(context.subProtocol);
		req.setParams(params);
		processor.postWebRequest(req, null);
	}
	
	private boolean isOldProtocol(String subProtocol){
		if(subProtocol!=null && oldProtocol!=null){
			for(String sub : oldProtocol){
				if(sub.equals(subProtocol)){
					return true;
				}
			}
		}
		return false;
	}
	
}
