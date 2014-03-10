package cn.hexing.fk.bp.dlms.alarm;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import cn.hexing.db.batch.AsyncService;
import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fk.bp.dlms.DlmsEventProcessor;
import cn.hexing.fk.bp.dlms.persisit.DlmsAlarmMessageService;
import cn.hexing.util.HexDump;

import com.hx.dlms.aa.AarqApdu.CipherMechanism;
import com.hx.dlms.aa.DlmsContext;
import com.hx.dlms.applayer.eventnotification.DlmsAlarmEventCode;
import com.hx.dlms.applayer.eventnotification.DlmsAlarmItem;
import com.hx.dlms.applayer.eventnotification.EventNotificationRequest;
/**
 * 按照旧文档进行读取
 * @author Administrator
 *
 */
public class AlarmRegisterNotificationOldImpl implements AlarmRegisterNotification{
	public static final Logger log = Logger.getLogger(AlarmRegisterNotificationOldImpl.class);

	private AsyncService service ;
	
	@Override
	public void read(DlmsContext context, DlmsEventProcessor processor,
			EventNotificationRequest er) {
		int alarmCode = er.getAttributeValue().getDoubleLong();
		String dateTime = er.getDateTime();
		log.warn("Event Status Word:"+(HexDump.toHex(alarmCode)+",meterId:"+context.meterId));
		//Byte0
		byte b = (byte)((alarmCode>>24) & 0xFF );

		if( (b & 0x08) != 0 ){ //Disconnector erro
			log.warn("Disconnector erro,meterId:"+context.meterId);
			DlmsObisItem[] params = new DlmsObisItem[]{ new DlmsObisItem() };
			DlmsRequest req = new DlmsRequest(); //0-0:99.98.1.255
			params[0].obisString = "0.0.99.98.2.255"; //继电器控制事件记录
			AlarmRegisterReader.getInstance().readAlarm(processor, params, req,context,dateTime);
		}
		else if( (b & 0x02) != 0 ){
			log.warn("Replace battery,meterId:"+context.meterId);
			DlmsObisItem[] params = new DlmsObisItem[]{ new DlmsObisItem() };
			DlmsRequest req = new DlmsRequest(); //0-0:99.98.1.255
			params[0].obisString = "0.0.99.98.0.255"; //标准事件记录
			AlarmRegisterReader.getInstance().readAlarm(processor, params, req,context,dateTime);
		}
		else if( (b & 0x01) != 0 ){
			log.warn("Clock invalid");
			DlmsObisItem[] params = new DlmsObisItem[]{ new DlmsObisItem() };
			DlmsRequest req = new DlmsRequest(); //0-0:99.98.1.255
			params[0].obisString = "0.0.99.98.0.255"; //标准事件记录
			AlarmRegisterReader.getInstance().readAlarm(processor, params, req,context,dateTime);
		}
		//Byte1
		b = (byte)((alarmCode>>16) & 0xFF );
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if( (b & 0x20 ) != 0 ){
			log.warn("fraud attempt,meterId:"+context.meterId);
			DlmsRequest req = new DlmsRequest(); //0-0:99.98.1.255
			if( context.aaMechanism == CipherMechanism.BENGAL ){
				String now = sdf.format(new Date());
				
				DlmsAlarmItem[] alarms = new DlmsAlarmItem[] { new DlmsAlarmItem(DlmsAlarmEventCode.FRAUD_OPEN_METER, now ) };
				DlmsAlarmMessageService.getInstance().operationDatabase(req, alarms);
				return;
			}
			DlmsObisItem[] params = new DlmsObisItem[]{ new DlmsObisItem() };
			params[0].obisString = "0.0.99.98.1.255";
//					SelectiveAccessDescriptor sad = new SelectiveAccessDescriptor();
			//Read events from specified event-code
//					params[0].accessSelector = 2;
//					sad.selectByIndex(DlmsAlarmEventCode.FRAUD_OPEN_METER, DlmsAlarmEventCode.FRAUD_CLOSE_METER);
			//Read events from 1 minute early till now
			AlarmRegisterReader.getInstance().readAlarm(processor, params, req,context,dateTime);
		}
		if( (b & 0x10) != 0 ){
			log.warn("watchdog error,meterId:"+context.meterId);
		}
		if( (b & 0x08 ) != 0 ){
			log.warn("Measurement system error,meterId:"+context.meterId);
		}
		if( (b & 0x04) != 0 ){
			log.warn("NV memory error,meterId:"+context.meterId);
		}
		//Byte2
		b = (byte)((alarmCode>>8) & 0xFF );
		if( (b & 0x02 ) != 0 ){
			log.warn("Open Meter,meterId:"+context.meterId);
		}
		else if( (b & 0x01) != 0 ){
			log.warn("Open terminal,meterId:"+context.meterId);
		}
		//byte3 ignored
	}

	@Override
	public void save(DlmsContext context, DlmsEventProcessor processor,
			EventNotificationRequest er) {
	}

	public final AsyncService getService() {
		return service;
	}

	public final void setService(AsyncService service) {
		this.service = service;
	}
}
