package cn.hexing.fk.bp.dlms.alarm;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import cn.hexing.db.batch.AsyncService;
import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.fas.model.dlms.DlmsRelayParam;
import cn.hexing.fas.model.dlms.DlmsRelayParam.RELAY_OPERATION;
import cn.hexing.fas.model.dlms.DlmsRelayParam.RELAY_PROTOCOL;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fas.model.dlms.DlmsRequest.DLMS_OP_TYPE;
import cn.hexing.fk.bp.dlms.DlmsEventProcessor;
import cn.hexing.fk.bp.dlms.util.DlmsConstant;
import cn.hexing.fk.bp.model.AlarmData;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.model.DlmsMeterRtu;
import cn.hexing.fk.model.MeasuredPoint;
import cn.hexing.fk.model.RtuManage;
import cn.hexing.fk.utils.DateConvert;
import cn.hexing.util.HexDump;

import com.hx.dlms.aa.AarqApdu.CipherMechanism;
import com.hx.dlms.aa.DlmsContext;
import com.hx.dlms.applayer.eventnotification.EventNotificationRequest;
/**
 * 按照新文档的定义进行读取
 * @author Administrator
 *
 */
public class AlarmRegisterNotificationImpl implements AlarmRegisterNotification{
	public static final Logger log = Logger.getLogger(AlarmRegisterNotificationImpl.class);

	private AsyncService service ;
	
	/**事件寄存器*/
	private static final String AlarmRegister = "0.0.97.98.0.255";
	
	/**采集器事件*/
	private static final String CollectorEvent="0.0.97.138.0.255";
	
	private static final String Prefix = "03";
	
	private static final String StandEventBit = "01";
	
	private static final String FraudAttemptBit="02";

	private static final String DisconnectEventBit="03";
	
	private static final String GridEventBit="04";
	
	private static final String CollectorEventBit="05";
	
	@Override
	public void read(DlmsContext context, DlmsEventProcessor processor,
			EventNotificationRequest er) {
		int alarmCode = er.getAttributeValue().getDoubleLong();
		String dateTime=er.getDateTime();
		log.warn("alarmCode:"+HexDump.toHex(alarmCode)+",meterId:"+context.meterId);			
		byte b = (byte)((alarmCode) & 0xFF ); //>>24
		try {
			//有些GPRS模块，如果读太块，会没有反应,这里休息3秒钟
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		byte[] bigEventReadBytes=new byte[4]; //标识每一类大事件是否被读了。用于下面有些小类事件读取
		if((b&0x01)!=0){
			readStandEvent(context, processor, dateTime, bigEventReadBytes);
		}
		if((b&0x02)!=0){
			readFraudEvent(context, processor, dateTime, bigEventReadBytes);
		}
		if((b&0x04)!=0){
			readDisconnectorEvent(context, processor, dateTime,bigEventReadBytes);
		}
		if((b&0x08)!=0){
			readGridEvent(context, processor, dateTime, bigEventReadBytes);
		}
		if((b&0x10)!=0){
			//长掉电
			DlmsObisItem[] params = new DlmsObisItem[]{ new DlmsObisItem() };
			DlmsRequest req = new DlmsRequest(); //0-0:99.98.1.255
			params[0].obisString = "1.0.99.97.0.255"; //电网事件
			AlarmRegisterReader.getInstance().readAlarm(processor, params, req,context,dateTime);
		}
		if((b&0x20)!=0){
			//MBUS事件记录
			log.warn("MBUS event,meterId:"+context.meterId);
			DlmsObisItem[] params = new DlmsObisItem[]{ new DlmsObisItem() };
			DlmsRequest req = new DlmsRequest(); //0-0:99.98.1.255
			params[0].obisString = "0.0.99.98.3.255"; //MBUS
			AlarmRegisterReader.getInstance().readAlarm(processor, params, req,context,dateTime);
		}
		if((b&0x40)!=0){
			//MBUS1事件记录
			log.warn("MBUS1 event,meterId:"+context.meterId);
			DlmsObisItem[] params = new DlmsObisItem[]{ new DlmsObisItem() };
			DlmsRequest req = new DlmsRequest();
			params[0].obisString = "0.1.24.5.0.255"; //MBUS1
			AlarmRegisterReader.getInstance().readAlarm(processor, params, req,context,dateTime);
		}
		if((b&0x80)!=0){
			//MBUS2事件记录
			log.warn("MBUS2 event,meterId:"+context.meterId);
			DlmsObisItem[] params = new DlmsObisItem[]{ new DlmsObisItem() };
			DlmsRequest req = new DlmsRequest(); //0-0:99.98.1.255
			params[0].obisString = "0.2.24.5.0.255"; //MBUS2
			AlarmRegisterReader.getInstance().readAlarm(processor, params, req,context,dateTime);
		}
		b = (byte)((alarmCode>>8) & 0xFF ); //>>16
		if((b&0x01)!=0){
			//MBUS3事件记录
			log.warn("MBUS3 event,meterId:"+context.meterId);
			DlmsObisItem[] params = new DlmsObisItem[]{ new DlmsObisItem() };
			DlmsRequest req = new DlmsRequest(); //0-0:99.98.1.255
			params[0].obisString = "0.3.24.5.0.255"; //MBUS3
			AlarmRegisterReader.getInstance().readAlarm(processor, params, req,context,dateTime);
		}
		if((b&0x02)!=0){
			//MBUS4事件记录
			log.warn("MBUS4 event,meterId:"+context.meterId);
			DlmsObisItem[] params = new DlmsObisItem[]{ new DlmsObisItem() };
			DlmsRequest req = new DlmsRequest(); //0-0:99.98.1.255
			params[0].obisString = "0.4.24.5.0.255"; //MBUS4
			AlarmRegisterReader.getInstance().readAlarm(processor, params, req,context,dateTime);
		}
		if((b&0x04)!=0){
			//强磁场事件记录
			log.warn("Strong magnetic field Event,meterId:"+context.meterId);
//			DlmsObisItem[] params = new DlmsObisItem[]{ new DlmsObisItem() };
////			DlmsRequest req = new DlmsRequest();
//			params[0].obisString = "0.5.24.5.0.255"; //强磁场事件
//			//AlarmRegisterReader.getInstance().readAlarm(processor, params, req,context,dateTime);
//			AlarmData alarm=createAlarmData(Prefix+FraudAttemptBit, "2A", dateTime, context);
//			service.addToDao(alarm, 4000);	
			readFraudEvent(context, processor, dateTime, bigEventReadBytes);
		}
		if((b&0x08)!=0){
			//开表盖事件记录
			log.warn("Open Meter Event,meterId:"+context.meterId);
//			DlmsObisItem[] params = new DlmsObisItem[]{ new DlmsObisItem() };
//			DlmsRequest req = new DlmsRequest();
//			params[0].obisString = "0.6.24.5.0.255"; //开表盖事件记录
			//AlarmRegisterReader.getInstance().readAlarm(processor, params, req,context,dateTime);
//			AlarmData alarm=createAlarmData(Prefix+FraudAttemptBit, "2C", dateTime, context);
//			service.addToDao(alarm, 4000);	
			readFraudEvent(context, processor, dateTime, bigEventReadBytes);
		}
		if((b&0x10)!=0){
			//开端钮盖事件记录
			log.warn("Open Terminal Cover Event,meterId:"+context.meterId);
//			DlmsObisItem[] params = new DlmsObisItem[]{ new DlmsObisItem() };
//			DlmsRequest req = new DlmsRequest();
//			params[0].obisString = "0.7.24.5.0.255"; //开端钮盖事件记录
			//AlarmRegisterReader.getInstance().readAlarm(processor, params, req,context,dateTime);
//			AlarmData alarm=createAlarmData(Prefix+FraudAttemptBit, "28", dateTime, context);
//			service.addToDao(alarm, 4000);
			readFraudEvent(context, processor, dateTime, bigEventReadBytes);
		}
		if((b&0x20)!=0){
			//电流反向事件记录
			log.warn("Reverse current event records,meterId:"+context.meterId);
			readGridEvent(context, processor, dateTime, bigEventReadBytes);
		}
		if((b&0x40)!=0){
			//重新编程事件记录
			log.warn("Reprogrammed Event,meterId:"+context.meterId);
//			DlmsObisItem[] params = new DlmsObisItem[]{ new DlmsObisItem() };
////			DlmsRequest req = new DlmsRequest();
//			params[0].obisString = "0.9.24.5.0.255"; //重新编程事件记录
			//AlarmRegisterReader.getInstance().readAlarm(processor, params, req,context,dateTime);
			readStandEvent(context, processor, dateTime, bigEventReadBytes);
		}
		if((b&0x80)!=0){
			//电网掉电事件记录
			log.warn("Power-Down Event,meterId:"+context.meterId);
//			DlmsObisItem[] params = new DlmsObisItem[]{ new DlmsObisItem() };
//			DlmsRequest req = new DlmsRequest();
//			params[0].obisString = "0.10.24.5.0.255"; //电网掉电记录
			//AlarmRegisterReader.getInstance().readAlarm(processor, params, req,context,dateTime);
//			AlarmData alarm = createAlarmData(Prefix+GridEventBit, "4B", dateTime, context);
//			service.addToDao(alarm, 4000);	
			readGridEvent(context, processor, dateTime, bigEventReadBytes);
		}
		
		b = (byte)((alarmCode>>16) & 0xFF ); //>>8
		if((b&0x01)!=0){
			System.out.println("A相失压");
			//A相失压
		}
		if((b&0x02)!=0){
			System.out.println("B相失压");
			//B相失压
		}
		if((b&0x04)!=0){
			System.out.println("C相失压");
			//C相失压
		}
		if((b&0x08)!=0){
			System.out.println("A相过压");
			//A相过压
		}
		if((b&0x10)!=0){
			System.out.println("B相过压");
			//B相过压
		}
		if((b&0x20)!=0){
			System.out.println("C相过压");
			//C相过压
		}
		if((b&0x40)!=0){
			System.out.println("过流");
			//过流
		}
		if((b&0x80)!=0){
			System.out.println("上下电");
			//上下电
		}
		b = (byte)((alarmCode>>24) & 0xFF ); //高字节
		if((b&0x01)!=0){//24bit
			AlarmData data = createAlarmData(Prefix+24, "00", dateTime, context);
			service.addToDao(data, 4000);
		}
		if((b&0x02)!=0){//25bit,支路检测仪有监测信息故障
			//走中继去读
			log.warn("Read MDT Status.DlmsRelay Id is D300,meterId:"+context.meterId);
			DlmsRequest dr = new DlmsRequest();
			dr.setOperator("AUTO_READ_RELAY_EVENT");
			DlmsRelayParam[] drp = new DlmsRelayParam[1];
			drp[0] = new DlmsRelayParam();
			drp[0].setDeviceId("000000000000");
			drp[0].setItemId("D300");
			drp[0].setOperation(RELAY_OPERATION.OP_GET);
			dr.setMeterId(context.meterId);
			dr.setOpType(DLMS_OP_TYPE.OP_HEXINGEXPAND);
			dr.setDlmsRelayParams(drp);
			processor.postWebRequest(dr, null);
		}
		
		if((b&0x04)!=0){
			AlarmData data = createAlarmData(Prefix+26, "00", dateTime, context);
			service.addToDao(data, 4000);
		} 
		
		if((b&0x8) !=0){ //27bit,Modbus
			log.warn("Read Modbus Status.,meterId:"+context.meterId);
			//走中继去读
			DlmsRequest dr = new DlmsRequest();
			dr.setOperator("AUTO_READ_MODBUS_STATUS");
			DlmsRelayParam[] drp = new DlmsRelayParam[3];
			for(int i=0;i<drp.length;i++){
				drp[i] = new DlmsRelayParam();
				drp[i].setOperation(RELAY_OPERATION.OP_GET);
				drp[i].setRelayProtocol(RELAY_PROTOCOL.MODBUS);
			}
			drp[0].setStartPos("29");//分补电容器数量、共补电容器数量
			drp[1].setStartPos("4B");drp[1].setRequestNum(32);//第X台智能电容器数据状态1
			drp[2].setStartPos("8B");drp[2].setRequestNum(32);//第X台智能电容器数据状态2
			
			dr.setMeterId(context.meterId);
			dr.setOpType(DLMS_OP_TYPE.OP_HEXINGEXPAND);
			dr.setDlmsRelayParams(drp);
			processor.postWebRequest(dr, null);
		}
		
		if((b&0x10)!=0){
			AlarmData data = createAlarmData(Prefix+28, "00", dateTime, context);
			service.addToDao(data, 4000);
		}
		if((b&0x20)!=0){
			AlarmData data = createAlarmData(Prefix+29, "00", dateTime, context);
			service.addToDao(data, 4000);
		}
		
	}

	private void readDisconnectorEvent(DlmsContext context,
			DlmsEventProcessor processor, String dateTime,
			byte[] bigEventReadBytes) {
		if(bigEventReadBytes[2]==0){
			log.warn("Read Disconnector Event,meterId:"+context.meterId);
			DlmsObisItem[] params = new DlmsObisItem[]{ new DlmsObisItem() };
			DlmsRequest req = new DlmsRequest(); //0-0:99.98.1.255
			params[0].obisString = "0.0.99.98.2.255"; //继电器
			AlarmRegisterReader.getInstance().readAlarm(processor, params, req,context,dateTime);
			bigEventReadBytes[2]=1;
		}else{
			log.warn("Readed Disconnector Event Before,meterId:"+context.meterId);
		}
	
	}

	private void readFraudEvent(DlmsContext context,
			DlmsEventProcessor processor, String dateTime,
			byte[] bigEventReadBytes) {
		if(	bigEventReadBytes[1]==0){
			log.warn("Read Fraud Attempt Event,meterId:"+context.meterId);
			DlmsObisItem[] params = new DlmsObisItem[]{ new DlmsObisItem() };
			DlmsRequest req = new DlmsRequest(); //0-0:99.98.1.255
			params[0].obisString = "0.0.99.98.1.255"; //欺诈事件记录
			AlarmRegisterReader.getInstance().readAlarm(processor, params, req,context,dateTime);
			bigEventReadBytes[1]=1;
		}else{
			log.warn("Readed Fraud Attempt Before.Do Nothing,meterId:"+context.meterId);
		}

	}

	private void readStandEvent(DlmsContext context,
			DlmsEventProcessor processor, String dateTime,
			byte[] bigEventReadBytes) {
		if(bigEventReadBytes[0]==0){
			DlmsObisItem[] params = new DlmsObisItem[]{ new DlmsObisItem() };
			DlmsRequest req = new DlmsRequest(); //0-0:99.98.1.255
			params[0].obisString = "0.0.99.98.0.255"; //标准事件记录
			AlarmRegisterReader.getInstance().readAlarm(processor, params, req,context,dateTime);
			bigEventReadBytes[0]=1;
			log.warn("stand event,meterId:"+context.meterId);
		}else{
			log.warn("Readed Stand Event Before.Do Nothing,meterId:"+context.meterId);
		}
	}

	private void readGridEvent(DlmsContext context,
			DlmsEventProcessor processor, String dateTime,
			byte[] bigEventReadBytes) {
		if(bigEventReadBytes[3]==0){
			DlmsObisItem[] params = new DlmsObisItem[]{ new DlmsObisItem() };
			DlmsRequest req = new DlmsRequest(); //0-0:99.98.1.255
			params[0].obisString = "0.0.99.98.4.255"; //电网事件
			AlarmRegisterReader.getInstance().readAlarm(processor, params, req,context,dateTime);
			bigEventReadBytes[3]=1;
			log.warn("Read Electrical Net Event,meterId:"+context.meterId);
		}else{
			log.warn("Readed Electrical Net Before.Do Nothing,meterId:"+context.meterId);
		}
	}

	@Override
	public void save(DlmsContext context, DlmsEventProcessor processor,
			EventNotificationRequest er) {
		List<AlarmData> alarms = null;
		String obis = er.getAttributeDescriptor().getInstanceIdAsString();
		if(context.getCipherMechanism()==CipherMechanism.BENGAL){
			alarms = bengalEventSave(context,er);
		}else{
			if(obis.equals(CollectorEvent)){  
				alarms = moduleEventSave(context, er);
			}else if(obis.equals(AlarmRegister)){
				alarms = alarmRegisterEventSave(context,er);
			}
		}
		for(AlarmData alarm : alarms){
			service.addToDao(alarm, 4000);	
		}
	}
	
	private List<AlarmData> bengalEventSave(DlmsContext context,
			EventNotificationRequest er) {
		int alarmCode = er.getAttributeValue().getDoubleLong();
		String dateTime = er.getDateTime();
		List<AlarmData> alarms = new ArrayList<AlarmData>();
		System.out.println(HexDump.toHex(alarmCode));
		//Byte1
		byte b = (byte)((alarmCode>>24) & 0xFF );
		if( (b & 0x01) != 0){
			log.warn("Clock Valid,meterId:"+context.meterId);
			alarms.add(createAlarmData(Prefix+StandEventBit, "06", dateTime, context));
		}
		if( (b & 0x02 ) != 0 ){
			log.warn("Replace Battary,meterId:"+context.meterId);
			alarms.add(createAlarmData(Prefix+StandEventBit, "07", dateTime, context));
		}
		
		if( (b & 0x08 )!= 0){
			log.warn("Disconnector error,meterId:"+context.meterId);
			alarms.add(createAlarmData(Prefix+DisconnectEventBit, "00", dateTime, context));
		}
		
		b=(byte)((alarmCode>>16) & 0xFF);
		
		if((b & 0x04) !=0){
			log.warn("Clear Warn Register,meterId:"+context.meterId);
			alarms.add(createAlarmData(Prefix+StandEventBit, "0B", dateTime, context));
		}
		if((b & 0x08) !=0){
			//03010D
			log.warn("Clear Data Register,meterId:"+context.meterId);
			alarms.add(createAlarmData(Prefix+StandEventBit, "0D", dateTime, context));
		}
		
		if((b & 0x20) !=0){
			//03010D
			log.warn("fraud attempt,meterId:"+context.meterId);
			alarms.add(createAlarmData(Prefix+FraudAttemptBit, "00", dateTime, context));
		}
		
//		if((b & 0x40) !=0){
//			//03010D
//			log.warn("data error");
//			alarms.add(createAlarmData(Prefix+StandEventBit, "0D", dateTime, context));
//		}
		
		return alarms;
	}

	private List<AlarmData> alarmRegisterEventSave(DlmsContext context,
			EventNotificationRequest er) {
		int alarmCode = er.getAttributeValue().getDoubleLong();
		String dateTime = er.getDateTime();
		//Byte0
		byte b = (byte)((alarmCode>>24) & 0xFF );
		List<AlarmData> alarms = new ArrayList<AlarmData>();
		
		if( (b & 0x08) != 0 ){ //Disconnector erro
			log.warn("Disconnector erro,meterId:"+context.meterId);
			alarms.add(createAlarmData(Prefix+DisconnectEventBit, "00", dateTime, context));
		}
		else if( (b & 0x02) != 0 ){
			log.warn("Replace battery,meterId:"+context.meterId);
			alarms.add(createAlarmData(Prefix+StandEventBit, "07", dateTime, context));
		}
		else if( (b & 0x01) != 0 ){
			log.warn("Clock invalid,meterId:"+context.meterId);
			alarms.add(createAlarmData(Prefix+StandEventBit, "06", dateTime, context));
		}
		//Byte1
		b = (byte)((alarmCode>>16) & 0xFF );
		if( (b & 0x20 ) != 0 ){
			log.warn("Fraud Attempt,meterId:"+context.meterId);
			alarms.add(createAlarmData(Prefix+FraudAttemptBit, "00", dateTime, context));
		}
		if( (b & 0x10) != 0 ){
			log.warn("watchdog error,meterId:"+context.meterId);
		}
		if( (b & 0x08 ) != 0 ){
			log.warn("Measurement system error,meterId:"+context.meterId);
			alarms.add(createAlarmData(Prefix+StandEventBit, "16", dateTime, context));
		}
		if( (b & 0x04) != 0 ){
			log.warn("NV memory error,meterId:"+context.meterId);
			alarms.add(createAlarmData(Prefix+StandEventBit, "14", dateTime, context));
		}
		//Byte2
		b = (byte)((alarmCode>>8) & 0xFF );
		if( (b & 0x02 ) != 0 ){
			log.warn("Open Meter,meterId:"+context.meterId);
			alarms.add(createAlarmData(Prefix+FraudAttemptBit, "44", dateTime, context));
		}
		else if( (b & 0x01) != 0 ){
			log.warn("Open terminal,meterId:"+context.meterId);
			alarms.add(createAlarmData(Prefix+FraudAttemptBit, "40", dateTime, context));
		}
		//byte3 ignored
		return alarms;
	}
	/**
	 * 模块的事件保存
	 * @param context
	 * @param er
	 * @return
	 */
	private List<AlarmData> moduleEventSave(DlmsContext context,
			EventNotificationRequest er) {
		int alarmCode = er.getAttributeValue().getDoubleLong();
		String dateTime=er.getDateTime();
		byte b = (byte)((alarmCode>>24) & 0xFF );
		List<AlarmData> alarms = new ArrayList<AlarmData>();
		if((b&0x01)!=0){ //bit0
			log.warn("power off 030501,meterId:"+context.meterId);
			alarms.add(createAlarmData(Prefix+CollectorEventBit, "01", dateTime, context));
		}
		if((b&0x02)!=0){//bit1
			log.warn("power on 030502,meterId:"+context.meterId);
			alarms.add(createAlarmData(Prefix+CollectorEventBit, "02", dateTime, context));
		}
		if((b&0x04)!=0){//bit2
			log.warn("disconnect error 030300,meterId:"+context.meterId);
			alarms.add(createAlarmData(Prefix+DisconnectEventBit, "00", dateTime, context));
		}
		if((b&0x08)!=0){//bit3
			log.warn("meterbox cover open 030231,meterId:"+context.meterId);
			alarms.add(createAlarmData(Prefix+FraudAttemptBit, "31", dateTime, context));
		}
		if((b&0x10)!=0){//bit4
			log.warn("meterbox cover close 030232,meterId:"+context.meterId);
			alarms.add(createAlarmData(Prefix+FraudAttemptBit, "32", dateTime, context));
		}
		if((b&0x40)!=0){ //bit6
			log.warn("disconnect success 030503,meterId:"+context.meterId);
			alarms.add(createAlarmData(Prefix+CollectorEventBit, "03", dateTime, context));
		}
		if(((b&0x80)!=0)){//bit7
			log.warn("disconnect fail 030504,meterId:"+context.meterId);
			alarms.add(createAlarmData(Prefix+CollectorEventBit, "04", dateTime, context));
		}
		b = (byte)((alarmCode>>16) & 0xFF );
		if((b&0x01)!=0){//bit8
			
		}
		if((b&0x02)!=0){//bit9
			
		}
		if((b&0x04)!=0){//bit10
			alarms.add(createAlarmData(Prefix+FraudAttemptBit, "38", dateTime, context));
		}
		if((b&0x08)!=0){//bit11
			alarms.add(createAlarmData(Prefix+FraudAttemptBit, "39", dateTime, context));
		}
		if((b&0x10)!=0){//bit12
			
		}
		if((b&0x20)!=0){//bit13
			
		}
		
		
		
		return alarms;
	}

	private AlarmData createAlarmData(String prefix,String bit,String occurTime,DlmsContext context){
		AlarmData alarm = new AlarmData();
		alarm.setAlertCodeHex(prefix+bit);
		occurTime = DlmsConstant.getInstance().isIranTime?DateConvert.iranToGregorian(occurTime):occurTime;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = null;
		try {
			date = sdf.parse(occurTime);
		} catch (ParseException e) {}
		alarm.setAlertTime(date);
		alarm.setReceiveTime(new Date());
		String logicAddress = context.meterId;
		MeasuredPoint mp=null;
		if(context.isRelay){
			logicAddress = context.dcLogicAddr;
			BizRtu bizRtu = RtuManage.getInstance().getBizRtuInCache((int)Long.parseLong(logicAddress,16));
			mp=bizRtu.getMeasuredPoint(""+context.measurePoint);
		}else{
			DlmsMeterRtu meterRtu = RtuManage.getInstance().getDlmsMeterRtu(logicAddress);
			mp=meterRtu.getMeasuredPoint("0");
		}
		if(mp==null){
			throw new RuntimeException("mp is null. logicAddress:"+logicAddress);
		}
		alarm.setDataSaveID(new Long(mp.getDataSaveID()));
		return alarm;
	}

	public final AsyncService getService() {
		return service;
	}

	public final void setService(AsyncService service) {
		this.service = service;
	}

}
