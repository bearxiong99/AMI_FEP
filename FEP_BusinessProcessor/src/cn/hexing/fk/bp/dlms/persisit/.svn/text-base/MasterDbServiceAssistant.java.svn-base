package cn.hexing.fk.bp.dlms.persisit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import cn.hexing.db.batch.AsyncService;
import cn.hexing.db.bizprocess.MasterDbService;
import cn.hexing.db.managertu.ManageRtu;
import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.fas.model.dlms.DlmsRelayParam;
import cn.hexing.fas.model.dlms.DlmsRelayParam.RELAY_OPERATION;
import cn.hexing.fas.model.dlms.DlmsRelayParam.RELAY_PROTOCOL;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fas.model.dlms.DlmsRequest.DLMS_OP_TYPE;
import cn.hexing.fas.model.dlms.RelayParam;
import cn.hexing.fas.protocol.data.DataItem;
import cn.hexing.fas.protocol.meter.BbMeterParser;
import cn.hexing.fas.protocol.meter.ModbusParser;
import cn.hexing.fk.bp.dlms.DlmsEventProcessor;
import cn.hexing.fk.bp.dlms.alarm.DlmsAlarmManager;
import cn.hexing.fk.bp.dlms.protocol.DlmsScaleManager;
import cn.hexing.fk.bp.dlms.protocol.IDlmsScaleConvert;
import cn.hexing.fk.bp.dlms.time.SaveAutoTimeResult;
import cn.hexing.fk.bp.model.DlmsAlarmStatus;
import cn.hexing.fk.bp.model.HostCommandDb;
import cn.hexing.fk.bp.model.HostCommandItemDb;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.model.DlmsMeterRtu;
import cn.hexing.fk.model.MeasuredPoint;
import cn.hexing.fk.model.RtuManage;
import cn.hexing.fk.utils.HexDump;

import com.hx.dlms.ASN1Type;
import com.hx.dlms.DlmsData;
import com.hx.dlms.aa.DlmsContext;
import com.hx.dlms.applayer.eventnotification.DlmsAlarmItem;
import com.hx.dlms.applayer.eventnotification.DlmsEventAlarmResolver;
/**
 * 对数据库进行操作
 * @author Administrator
 *
 */
public class MasterDbServiceAssistant 
{
	private static final MasterDbServiceAssistant instance = new MasterDbServiceAssistant();
	MasterDbService masterDbService;
	private static final Logger log = Logger.getLogger(MasterDbServiceAssistant.class);

	private JdbcDlmsDao jdbcDao;
	public final static MasterDbServiceAssistant getInstance(){return instance;};
	
	private AsyncService service;
	
	//当主站读事件之后，由于是使用批处理，会延迟存储数据，导致，界面无法查到数据。这里设置一个延迟时间，当抄到数据之后，延迟几秒通知主站。
	private int noticeMSDelayTimeAfterReadEvent;
	
	private MasterDbServiceAssistant(){};
	
	/**
	 * 用于插入召测数据
	 * @param params
	 * @param req
	 * @param param 
	 * @return
	 */
	private int insertCommandCallResult(DlmsRequest req, DlmsObisItem param)
	{
		// 1.保存到cz_zcjg表中
		HostCommandItemDb commandItemDb = new HostCommandItemDb();
		commandItemDb.setCommandId(req.getCommId());
		commandItemDb.setTime(new Date());
		if(param.resultData !=null){
			commandItemDb.setValue(param.resultData.getStringValue());
		}else{
			commandItemDb.setValue("null:"+param.resultCode);
		}
		if(req.isRelay()){
			commandItemDb.setTn(""+req.getRelayParam().getMeasurePoint());
		}else{
			commandItemDb.setTn("0");
		}
		commandItemDb.setCode(param.sjx);
		commandItemDb.setLogicAddress(getRtuId(req));
		return masterDbService.insertCommandCallResult(commandItemDb);
	}
	/**
	 *  获得终端局号\表计局号
	 * @param req
	 * @param commandItemDb
	 */
	public String getRtuId(DlmsRequest req) {
		String meterId = "";
		if(req.isRelay()){
			String dcLogicAddr=req.getRelayParam().getDcLogicalAddress();
			BizRtu bizRtu=RtuManage.getInstance().getBizRtuInCache((int)Long.parseLong(dcLogicAddr,16));
			meterId=bizRtu.getRtuId();
		}else{
			DlmsMeterRtu meterRtu = RtuManage.getInstance().getDlmsMeterRtu(req.getMeterId());
			if(meterRtu==null){
				meterRtu=ManageRtu.getInstance().refreshDlmsMeterRtu(req.getMeterId());
				if(meterRtu==null){
					throw new RuntimeException("Can't Find MeterRtu,LogicAddress is "+req.getMeterId());
				}
			}
			meterId = meterRtu.getMeterId();
		}
		return meterId;
	}
	
	private int insertCommandSetResult(DlmsRequest req,String value){
		HostCommandItemDb commandItemDb = new HostCommandItemDb();
		commandItemDb.setCommandId(req.getCommId());
		commandItemDb.setTime(new Date());
		if(req.isRelay()){
			commandItemDb.setTn(""+req.getRelayParam().getMeasurePoint());
		}else{
			commandItemDb.setTn("0");
		}
		commandItemDb.setValue(value);
		commandItemDb.setLogicAddress(getRtuId(req));
		//对时间 进行的操作，要更新自动对时成功的状态
		if(req.getOperator()!=null && (req.getOperator().equals("Time") || req.getOperator().equals("TimeSyn"))&&(value.equals("8#0.0.1.0.0.255#2:0"))){
			HostCommandItemDb commandItemDbTime = new HostCommandItemDb();
			commandItemDbTime.setStatus(2);
			commandItemDbTime.setLogicAddress(getRtuId(req));
			if(req.isRelay()){
				commandItemDbTime.setTn(""+req.getRelayParam().getMeasurePoint());
			}else{
				commandItemDbTime.setTn("0");
			}
			masterDbService.updateAutoTimeResult(commandItemDbTime);
		}
		
		if(commandItemDb.getCommandId()==-1) return 0;
		
		return masterDbService.insertCommandSetResult(commandItemDb);
	}
	/**
	 * 修改数据库中的任务状态
	 * @param zt  0 正在执行,1：成功 2：失败 3：部分成功
	 * @param req
	 */
	public void updateTaskStatus(String zt, String commandId) 
	{
		masterDbService.updateTaskStatus(zt, commandId);
	}
	
	
	public boolean operationRequest(DlmsContext context,DlmsRequest req) throws IOException{
		if(req.getDlmsRelayParams()!=null){
			return operationDlmsRelay(context,req);
		}
		DlmsObisItem[] params = req.getParams();
		String subProtocol=req.getSubprotocol();
		for( DlmsObisItem param : params){
			IDlmsScaleConvert cvt = DlmsScaleManager.getInstance().getConvert(subProtocol, param.classId, param.obisString, param.attributeId);
			if( null != cvt && param.resultData!=null )
				param.resultData = cvt.upLinkConvert( req,param.resultData,param );
		}
		if(req.getOperator() ==null){
			MasterDbServiceAssistant.getInstance().operationDatabase(req);
			return false;
		}else if(req.getOperator().equals("DlmsPolling") || req.getOperator().equals("MasterTask")){
			TaskMessageService.getInstance().operationDataBase(context,req);
			return false;
		}else if(req.getOperator().equals("Time") || req.getOperator().equals("TimeSyn")){
			MasterDbServiceAssistant.getInstance().operationDatabase(req);  //先保存召测结果,然后对自动对时的结果进行处理
			return false;	
		}else if(req.getOperator().equals("Tariff")){
			MasterDbServiceAssistant.getInstance().operationDatabase(req);  //先保存召测结果,然后对自动对时的结果进行处理
			return false;
		}else if(req.getOperator().equals("ChannelReadSave")){//主站读通道需要保存
			MasterDbServiceAssistant.getInstance().operationDatabase(req);  //先保存召测结果
			if(!req.isRelay()) //中继不保存
				TaskMessageService.getInstance().operationDataBase(context,req); //再保存召测数据
			return false;
		}else if(req.getOperator().equals("TaskSet")){//任务设置
			MasterDbServiceAssistant.getInstance().operationDatabase(req);
			//更新通道任务
			MasterDbServiceAssistant.getInstance().updateChannelTask(req);
			return false;
		}else if( req.getOperator().equals("EVENT_READING_AUTO") || req.getOperator().equals("Event_Read") ){
			Object aObj=req.getAppendParam("EventOccurTime");
			if(aObj!=null){
				Date occurTime = (Date) aObj;
				DlmsAlarmStatus alarmStatus= new DlmsAlarmStatus();
				alarmStatus.setObis((String)req.getAppendParam("OBIS"));
				alarmStatus.setLogicAddress(req.getMeterId());
				alarmStatus.setLastReportTime(occurTime);
				DlmsAlarmManager.getInstance().updateDlmsAlarmStatus(alarmStatus);
				service.addToDao(alarmStatus, 4003);
			}
			
			//DLMS events reading automatically after BP handling ALARM-register notification.
			for(int i=0;i<params.length;i++){
				StringBuilder parentCode = new StringBuilder();
				parentCode.append(params[i].classId).append(".");
				parentCode.append(params[i].obisString).append(".");
				parentCode.append(params[i].attributeId).append("|");
				DlmsAlarmItem[] alarms = DlmsEventAlarmResolver.getInstance().resolveRunningEventArray(parentCode.toString(), params[i].obisString, params[i].resultData);
				
				DlmsAlarmMessageService.getInstance().operationDatabase(req, alarms);
			}
			if(req.getOperator().equals("Event_Read")){
				
				try {Thread.sleep(noticeMSDelayTimeAfterReadEvent);} catch (InterruptedException e) {}
				
				HostCommandDb commandDb=new HostCommandDb();
		        commandDb.setId(req.getCommId());
		        commandDb.setMessageCount(1);
		        commandDb.setStatus("1");
		        commandDb.setErrcode("1");
		        masterDbService.procUpdateCommandStatus(commandDb);
			}
			
			return false;
		}
		return true;
	}
	
	/**
	 * 对Dlms中继转发处理(中继命令为97DTL645的帧)
	 * @param context
	 * @param req
	 * @return true|false
	 */
	private boolean operationDlmsRelay(DlmsContext context, DlmsRequest req) {
		String operator = req.getOperator();
		DlmsRelayParam[] params = req.getDlmsRelayParams();
		for(DlmsRelayParam param:params){
			if(param.getRelayProtocol() == RELAY_PROTOCOL.METER_97){
				dtl645FrameProcess(param);
			}else if(param.getRelayProtocol()==RELAY_PROTOCOL.MODBUS){
				modbusFrameProcess(param);
			}
		}
		
		if("AUTO_READ_RELAY_EVENT".equals(operator)){
			branchEventProcess(context, req, params);
		}else if("AUTO_READ_BRANCH_STATUS".equals(operator)){
			branchStatusProcess(req, params);
		}else if("AUTO_READ_MODBUS_STATUS".equals(operator)){
			modbusStatusProcess(req,params);
		}else {
			//是主站召测或者设置
			RELAY_OPERATION operation = params[0].getOperation();
				switch(operation){
				case OP_GET:
					//保存数据到cz_zcjg里
					for(int i=0;i<params.length;i++){
						insertCallResultValue(req, params[i]);						
					}
					break;
				case OP_SET:
					//2.保存到cz_szjg表中
					StringBuilder sb = new StringBuilder();
					for(int i=0;i<params.length;i++){
						sb.append(params[i].getStartPos()+":"+params[i].getResultValue());
						if(i!=params.length-1){
							sb.append(",");
						}
					}
					insertCommandSetResult(req, sb.toString());
					break;
				default:
					break;
				}
			
			procUpdateCommandStatus(req.getCommId(),"1");

		}
		
		
		return false;
	}

	private void insertCallResultValue(DlmsRequest req,DlmsRelayParam param) {
		HostCommandItemDb commandItemDb = new HostCommandItemDb();
		commandItemDb.setCommandId(req.getCommId());
		commandItemDb.setTime(new Date());
		commandItemDb.setTn("0");
		commandItemDb.setValue(param.getResultValue());
		commandItemDb.setCode(param.getStartPos());
		commandItemDb.setLogicAddress(getRtuId(req));
		masterDbService.insertCommandCallResult(commandItemDb);
	}

	/**
	 * modbus分补共补信息
	 * @param req
	 * @param params
	 */
	private void modbusStatusProcess(DlmsRequest req, DlmsRelayParam[] params) {
	
		List<DlmsAlarmItem> items = new ArrayList<DlmsAlarmItem>();
		for(DlmsRelayParam param : params){
			
			DlmsAlarmItem item = new DlmsAlarmItem();
			item.setAlarmCode("0327"+param.getStartPos());
			item.setRelatedData(item.getAlarmCode(), param.getResultValue());
			items.add(item);
		}
		DlmsAlarmItem[] itemArray = new DlmsAlarmItem[items.size()];
		System.arraycopy(items.toArray(), 0, itemArray, 0, items.size());
		         
		DlmsAlarmMessageService.getInstance().operationDatabase(req, itemArray);
	}

	/**
	 * 支路检测仪状态处理
	 * @param req
	 * @param params
	 */
	private void branchStatusProcess(DlmsRequest req, DlmsRelayParam[] params) {
		DlmsAlarmItem item = new DlmsAlarmItem();
		item.setAlarmCode("032534");
		//支路状态
		for(DlmsRelayParam param:params){
			String resultValue = param.getResultValue();
			if(null!=resultValue && !"".equals(resultValue)){
				String[] values = resultValue.split(":");
				String key=values[0];
				String value = values[1];
				value=value.substring(0, 12);  //12路状态
				StringBuilder destValue = new StringBuilder();
				for(int i=0;i<value.length();i++){
					destValue.append(value.substring(i,i+1)).append(",");
				}
				if(destValue.length()>0)
					destValue.deleteCharAt(destValue.length()-1);
				item.setRelatedData(key, destValue.toString());
			}
		}
		DlmsAlarmMessageService.getInstance().operationDatabase(req, new DlmsAlarmItem[]{item});
	}

	
	/**
	 * 读到支路监测仪事件处理
	 * @param context
	 * @param req
	 * @param params
	 */
	
	private void branchEventProcess(DlmsContext context, DlmsRequest req,
			DlmsRelayParam[] params) {
		//值是8个字节的数据
		for(DlmsRelayParam param:params){
			String resultValue = param.getResultValue();
			String[] values = resultValue.split(":");
//				String key = values[0];
			String value = values[1];
			
			List<DlmsAlarmItem> items = new ArrayList<DlmsAlarmItem>();
			boolean branchChannges=false;
			String brachString=value.substring(8*6,8*8-4);
			
			if(!"000000000000".equals(brachString)){
				branchChannges = true;
			}else{
				branchChannges = true;
				log.warn("MeterId:"+context.meterId+" Branch Not Channge.");
			}
			
			
			String otherAlarmStr = value.substring(0,8*6);
			
			for(int i=0;i<otherAlarmStr.length()/8-1;i++){
				String _byte = otherAlarmStr.substring(i*8, i*8+8);
				for(int j=0;j<8;j++){
					if("1".equals(_byte.charAt(j))){
						DlmsAlarmItem item = new DlmsAlarmItem();
						item.setAlarmCode("0325"+HexDump.toHex((byte)(i*8+j)));
						items.add(item);
					}
				}
			}
			
			DlmsAlarmItem[] alarms = new DlmsAlarmItem[items.size()];
			int i=0;
			for(DlmsAlarmItem item:items){
				alarms[i++] =item;
			}
			DlmsAlarmMessageService.getInstance().operationDatabase(req, alarms);
			
			{
				log.warn("Get D300 Status.And Clear Device Status.");
				//将事件状态字全部置0
				DlmsRequest dr = new DlmsRequest();
				DlmsRelayParam[] drp = new DlmsRelayParam[1];
				drp[0] = new DlmsRelayParam();
				drp[0].setDeviceId("000000000000");
				drp[0].setItemId("D300");
				drp[0].setParams("0000000000000000000000000000000000000000000000000000000000000000");
				drp[0].setOperation(RELAY_OPERATION.OP_SET);
				dr.setMeterId(context.meterId);
				dr.setOpType(DLMS_OP_TYPE.OP_HEXINGEXPAND);
				dr.setDlmsRelayParams(drp);
				DlmsEventProcessor.getInstance().postWebRequest(dr, null);
			}
			
			if(branchChannges){
				//如果支路有变化，将支路状态读取出来
				log.warn("Branch Channges.Read Branch Status.DlmsRelay Id is D30A");
				DlmsRequest dr = new DlmsRequest();
				dr.setOperator("AUTO_READ_BRANCH_STATUS");
				DlmsRelayParam[] drp = new DlmsRelayParam[1];
				drp[0] = new DlmsRelayParam();
				drp[0].setDeviceId("000000000000");
				drp[0].setItemId("D30A");
				drp[0].setOperation(RELAY_OPERATION.OP_GET);
				dr.setMeterId(context.meterId);
				dr.setOpType(DLMS_OP_TYPE.OP_HEXINGEXPAND);
				dr.setDlmsRelayParams(drp);
				DlmsEventProcessor.getInstance().postWebRequest(dr, null);
			}
		}
		
		//发送清除告警状态字命令
	}

	/**
	 *	modbus 帧处理 
	 * @param param
	 */
	private void modbusFrameProcess(DlmsRelayParam param) {
		String modbusFrame = param.getUpRelayMessage();
		
		ModbusParser parser = new ModbusParser();
		
		String val=parser.parse(HexDump.toArray(modbusFrame), param.getStartPos());
		param.setResultValue(val);
		
	}

	/**
	 * dlt645帧处理
	 * @param param
	 */
	private void dtl645FrameProcess(DlmsRelayParam param) {
		String dtl645Frame=param.getUpRelayMessage();
		BbMeterParser parser = new BbMeterParser();
		byte[] byteFrame = HexDump.toArray(dtl645Frame);
		Object[] results = parser.parser(byteFrame, 0, byteFrame.length,new BizRtu());
		StringBuffer resultValue =new StringBuffer();
		if(results!=null && results.length>0){
			for(Object result : results){
				if(result instanceof DataItem){
					String key=(String) ((DataItem) result).getProperty("datakey");
					if("8902".equals(key)){ //表号
						continue;
					}else{
						resultValue.append(key+":"+((DataItem) result).getProperty("value")).append("#");
					}
				}
			}
			if(resultValue.length()>0){
				resultValue.deleteCharAt(resultValue.length()-1);
			}
		}
		param.setResultValue(resultValue.toString());
	}

	public void operationDatabase( DlmsRequest req) throws IOException
	{
		if(req.getOpType() == DLMS_OP_TYPE.OP_GET){
			operationGetResponse(req);
		}else if(req.getOpType() == DLMS_OP_TYPE.OP_SET){
			operationSetResponse(req);
		}else if(req.getOpType() == DLMS_OP_TYPE.OP_ACTION){
			operationActionResponse(req);
		}
	}
	/**对召测的返回进行处理*/
	private void operationGetResponse(DlmsRequest req) {
		DlmsObisItem[] params = req.getParams();
		//自动对时请求 需要先处理 并入库到TJ_ZDDSSJ
		if (req.getOperator() != null && req.getOperator().equals("TimeSyn")) {
			for (DlmsObisItem param : params) {
				SaveAutoTimeResult.getInstance().saveAutoTimeResult(req, param,masterDbService);
				return;
			}
		}
		for(DlmsObisItem param : params){
			if(param.sjx == null || "".equals(param.sjx)){
				//在数据库中查找数据项
				param.sjx=masterDbService.getRelatedCode(param.obisString, param.classId, param.attributeId);
			}
			insertCommandCallResult(req, param);
		}
		
		procUpdateCommandStatus(req.getCommId(),"1");
	}
	
	/**对设置的返回进行处理
	 * @param resp 
	 * @throws IOException */
	private void operationSetResponse(DlmsRequest req) throws IOException {
		
		boolean isNotNormal = false;
		for(int i=0;i<req.getParams().length;i++){
			if(req.getParams()[i].resultData !=null){
				isNotNormal = true;
				break;
			}
		}
		
		if(!isNotNormal){
			handleNormalSetResponse(req);
		}else{		//对于充值token返回，这里进行处理
			handleSetTokenResponse(req);
		}
	}
	
	public void updateNewEncKeyCommand(DlmsRequest req,String newEncKey,String resultCode){
		if(resultCode.equals("0")){ //如果没有成功，不更改密钥
			jdbcDao.updateEncriptKey(req.getMeterId(), newEncKey);
		}
		String value = req.getParams()[0].sjx+":"+resultCode;
		insertCommandSetResult(req, value);
		
		updateCommandStatus("1", ""+req.getCommId(), req.getMeterId());
		
		updateTaskStatus("1", ""+req.getCommId());
		
		
	}
	
	/**
	 * 处理设置token的返回
	 * @param req
	 * @param setResponseData
	 */
	private void handleSetTokenResponse(DlmsRequest req) {

		DlmsObisItem[] params = req.getParams();
		String logicAddress = getRtuId(req);
		for(DlmsObisItem param:params){
			StringBuilder sb = new StringBuilder();
			if(param.sjx == null || "".equals(param.sjx)){
				//在数据库中查找数据项
				param.sjx=masterDbService.getRelatedCode(param.obisString, param.classId, param.attributeId);
			}
			//对设置token返回进行处理
			ASN1Type[] members = param.resultData.getStructure().getMembers();
			DlmsData member = (DlmsData) members[0];
			sb.append(param.sjx+":"+member.getStringValue());
			DlmsData token  =(DlmsData) members[members.length-1]; //返回的结果中，最后一个是TOKEN
			String strToke = token.getStringValue();
			if(!strToke.equals("FFFFFFFFFFFFFFFFFFFF"))
				sb.append(":").append(strToke);
			insertCommandSetTokenResult(req,logicAddress,param, sb.toString());
			if(param.cmdId!=-1){
				updateCommandStatus("1",""+param.cmdId,logicAddress);
			}else if(req.getCommId()!=-1){
				updateCommandStatus("1", ""+req.getCommId(),logicAddress);
			}
		}

		//如果req.commId!=-1 修改任务状态
		if(req.getCommId()==-1){
			updateTaskStatus("1", ""+params[params.length-1].cmdId);
		}else{
			updateTaskStatus("1",""+ req.getCommId());
		}

	}


	private int insertCommandSetTokenResult(DlmsRequest req,String logicAddress,DlmsObisItem param, String value) {
		HostCommandItemDb commandItemDb = new HostCommandItemDb();
		commandItemDb.setCommandId(param.cmdId);
		commandItemDb.setTime(new Date());
		if(req.isRelay()){
			RelayParam relayParam = req.getRelayParam();
			commandItemDb.setTn(""+relayParam.getMeasurePoint());
			BizRtu bizRtu=RtuManage.getInstance().getBizRtuInCache((int)Long.parseLong(logicAddress,16));
			MeasuredPoint mp = bizRtu.getMeasuredPoint(""+relayParam.getMeasurePoint());
			logicAddress=mp.getTnAddr();
			
		}else{
			commandItemDb.setTn("0");
			DlmsMeterRtu meterRtu = RtuManage.getInstance().getDlmsMeterRtu(logicAddress);
			if(meterRtu==null){
				meterRtu=ManageRtu.getInstance().refreshDlmsMeterRtu(logicAddress);
			}
			logicAddress=meterRtu.getLogicAddress();
		}

		commandItemDb.setValue(value);
		commandItemDb.setLogicAddress(logicAddress);
		return masterDbService.insertCommandSetResult(commandItemDb);
	}



	private void handleNormalSetResponse(DlmsRequest req) {
		StringBuilder sb = new StringBuilder();
		DlmsObisItem[] params = req.getParams();
		for(DlmsObisItem param:params){
			if(param.sjx == null || "".equals(param.sjx)){
				//在数据库中查找数据项
				param.sjx=masterDbService.getRelatedCode(param.obisString, param.classId, param.attributeId);
			}
				sb.append(param.sjx+":"+param.resultCode).append(",");
		}

		sb.deleteCharAt(sb.length()-1);
		//保存设置结果
		insertCommandSetResult(req, sb.toString());
		
		if(req.getCommId()!=-1){
			procUpdateCommandStatus(req.getCommId(),"1");
		}else{
			//修改命令状态
			for(DlmsObisItem param:params){
				if(param.cmdId!=-1){
					procUpdateCommandStatus(Long.parseLong(""+param.cmdId),"1");
				}
			}
		}
	
	}
	
	/**对执行的返回进行处理*/
	private void operationActionResponse(DlmsRequest req) {
		
		DlmsObisItem[] params = req.getParams();
		StringBuilder sb = new StringBuilder();
		for(DlmsObisItem param : params){
			if(param.sjx == null || "".equals(param.sjx)){
				//在数据库中查找数据项
				param.sjx=masterDbService.getRelatedCode(param.obisString, param.classId, param.attributeId);
			}
			sb.append(param.sjx+":"+param.resultCode).append(",");
		}
		sb.deleteCharAt(sb.length()-1);
		
		//update result
		insertCommandSetResult(req, sb.toString());
		//update command
		procUpdateCommandStatus(req.getCommId(),"1");
	}
	
	private void procUpdateCommandStatus(long comdId,String status){
		if(comdId == -1) return;//命令id为-1，表示不需要更改命令
		HostCommandDb commandDb=new HostCommandDb();
        commandDb.setId(comdId);
        commandDb.setMessageCount(1);
        commandDb.setStatus(status);
        commandDb.setErrcode(status);
        masterDbService.procUpdateCommandStatus(commandDb);
	}
	
	
	
	/**
	 * 更新命令状态
	 * @param zt 0:正在执行   1：成功     2：失败      3：部分成功
	 * @param req
	 */
	public void updateCommandStatus(String zt,String commandID,String zdjh)
	{
		masterDbService.updateCommandStatus(zt,zdjh,commandID);
	}
	
	public MasterDbService getMasterDbService() {
		return masterDbService;
	}

	public void setMasterDbService(MasterDbService masterDbService) {
		this.masterDbService = masterDbService;
	}

	public final JdbcDlmsDao getJdbcDao() {
		return jdbcDao;
	}

	public final void setJdbcDao(JdbcDlmsDao jdbcDao) {
		this.jdbcDao = jdbcDao;
	}

	/**esam key version change*/
	public void updateEsamKeyVersion(DlmsRequest req,DlmsContext context) {
		int resultCode=req.getParams()[0].resultCode;
		if(resultCode==0){
			//change key version in db
			if(++context.keyVersion>255){
				context.keyVersion = 1;
			}
			String logicAddr = "";
//			if(req.isRelay()){
//				
//				RelayParam relayParam = req.getRelayParam();
//				String dcLogicAddr=relayParam.getDcLogicalAddress();
//				BizRtu br=RtuManage.getInstance().getBizRtuInCache(Integer.parseInt(dcLogicAddr,16));
//				MeasuredPoint mp=br.getMeasuredPoint(relayParam.getMeasurePoint()+"");
//				logicAddr = mp.getTnAddr();
//				
//			}else{
				logicAddr = context.meterId;
//			}
			jdbcDao.updateEsamKeyVersion(logicAddr,context.keyVersion);
		}
		//如果是读keyersion不需要保存命令
		if("ReadKeyVersion".equals(req.getOperator())) return;
		
		//insert set result
		String value = req.getParams()[0].sjx+":"+resultCode;
		insertCommandSetResult(req, value);
		
		//change command status
		updateCommandStatus("1", ""+req.getCommId(), req.getMeterId());
		
		//change task status
		updateTaskStatus("1", ""+req.getCommId());

	}
	/**
	 * 更新通道任务
	 * @param req
	 */
	public void updateChannelTask(DlmsRequest req) {
		
		//下发任务的时候，下发了两个，一个是任务数据项，一个是任务周期，只有两个全部成功，才更新任务是否有效。
		
		DlmsObisItem[] params = req.getParams();
		Map<String,Integer> results = new HashMap<String, Integer>();
		for(DlmsObisItem param:params){
			String channelNum="";
			//根据OBIS获得通道号，这里由于split(".")没有结果，所以将.替代为#
			String obis = param.obisString.replace('.', '#');
			channelNum=obis.split("#")[1];
			Integer successCounter=results.get(channelNum);
			if(successCounter==null){
				successCounter=0;
			}
			if(param.resultCode==0)  successCounter++;
			
			results.put(channelNum, successCounter);
		}
		DlmsMeterRtu meterRtu = RtuManage.getInstance().getDlmsMeterRtu(req.getMeterId());
		for(Iterator<String> it = results.keySet().iterator();it.hasNext();){
			String channelNum=it.next();
			int count=results.get(channelNum);
			if(count==2){//只对于任务数据项和任务周期都成功的，才更新
				jdbcDao.updateChannelSet(meterRtu.getMeterId(), channelNum);
			}
		}
	}

	public final AsyncService getService() {
		return service;
	}

	public final void setService(AsyncService service) {
		this.service = service;
	}

	public int getNoticeMSDelayTimeAfterReadEvent() {
		return noticeMSDelayTimeAfterReadEvent;
	}

	public void setNoticeMSDelayTimeAfterReadEvent(
			int noticeMSDelayTimeAfterReadEvent) {
		this.noticeMSDelayTimeAfterReadEvent = noticeMSDelayTimeAfterReadEvent * 1000;
	}


	

}
