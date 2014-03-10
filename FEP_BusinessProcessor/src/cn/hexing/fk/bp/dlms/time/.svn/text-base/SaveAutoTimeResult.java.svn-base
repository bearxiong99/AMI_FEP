package cn.hexing.fk.bp.dlms.time;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import cn.hexing.db.bizprocess.MasterDbService;
import cn.hexing.fas.model.HostCommandResult;
import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fk.bp.dlms.DlmsEventProcessor;
import cn.hexing.fk.bp.dlms.persisit.MasterDbServiceAssistant;
import cn.hexing.fk.bp.dlms.util.DlmsConstant;
import cn.hexing.fk.bp.model.HostCommandItemDb;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.model.MeasuredPoint;
import cn.hexing.fk.model.RtuManage;
import cn.hexing.fk.utils.DateConvert;
import cn.hexing.fk.utils.StringUtil;

public class SaveAutoTimeResult {
	private static final SaveAutoTimeResult instance = new SaveAutoTimeResult();
	protected static final Logger log = Logger.getLogger(SaveAutoTimeResult.class);

	public final static SaveAutoTimeResult getInstance(){return instance;};
	
	private SaveAutoTimeResult(){};
	
	// 保存DLMS 对时结果
	public int  saveAutoTimeResult(DlmsRequest req , DlmsObisItem param,MasterDbService masterDbService) {
		// save result to tj_zddssj
		HostCommandItemDb commandItemDb = new HostCommandItemDb();
		commandItemDb.setTime(new Date());
		if(param.resultData !=null){
			String time=param.resultData.getStringValue();
			// iran? convert(date):date
			time =  DlmsConstant.getInstance().isIranTime?time=DateConvert.iranToGregorian(time):time;
			SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date terminalTime = null;
			try {
				terminalTime = sdf.parse(time);
			} catch (ParseException e) {
				log.error(StringUtil.getExceptionDetailInfo(e));
			}
			Date Systemtime=new Date();
			long timedifference=(timeOffset(Systemtime,terminalTime));
			commandItemDb.setTimedifference(timedifference);
			commandItemDb.setTerminalTime(terminalTime);		
		}else{
			return 0;
		}
		if(req.isRelay()){
			commandItemDb.setTn(""+req.getRelayParam().getMeasurePoint());
		}else{
			commandItemDb.setTn("0");
		}
		commandItemDb.setStatus(1);
		commandItemDb.setLogicAddress(MasterDbServiceAssistant.getInstance().getRtuId(req));
		masterDbService.saveAutoTimeResult(commandItemDb);
		//判断是否需要自动对时 ，需要则发送自动对时请求
		String isAuto=(String) req.getAppendParam("bpAutoTimeSyn");
		if("1".equals(isAuto)&&(commandItemDb.getTimedifference()>((Integer)req.getAppendParam("timeDiffThreshold")))){
			TimeAutomatic.timeAutomatic(DlmsEventProcessor.getInstance(), req);
		}
		return 0;
	}
	//保存 国网 浙规 广规 对时结果
	public int saveAutoTimeResult(HostCommandResult result ,MasterDbService masterDbService,String  LogicAddress){
		HostCommandItemDb commandItemDbTime = new HostCommandItemDb();
		Date Systemtime=new Date();
		commandItemDbTime.setTime(Systemtime);
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if(result.getValue()!=null){
			Date terminalTime;
			try {
				terminalTime = sdf.parse(result.getValue());
				long timedifference=(timeOffset(Systemtime,terminalTime));
				commandItemDbTime.setTimedifference(timedifference);
				commandItemDbTime.setTerminalTime(terminalTime);
			} catch (ParseException e) {
				log.error(StringUtil.getExceptionDetailInfo(e));
			}
		}else{
			return 0;
		}
		commandItemDbTime.setTn(result.getTn());
		commandItemDbTime.setStatus(1);
		commandItemDbTime.setLogicAddress(LogicAddress);
		return	masterDbService.saveAutoTimeResult(commandItemDbTime);
	}
	
	public int  updateAutoTimeResult(MasterDbService masterDbService,String Tn ,IMessage msg){
		BizRtu rtu=RtuManage.getInstance().getBizRtuInCache(msg.getRtua());
		HostCommandItemDb item=new HostCommandItemDb(); 
		item.setTn(Tn);
		MeasuredPoint mp=rtu.getMeasuredPoint(Tn);
		String LogicAddress="";
		if(mp!=null){
			 LogicAddress=mp.getTnAddr();
		}
		else {
			 LogicAddress=rtu.getRtuId();
		}
		item.setLogicAddress(LogicAddress);
		item.setStatus(2);
		return masterDbService.updateAutoTimeResult(item);      				
	}
	
	public long timeOffset(Date Systemtime ,Date terminalTime  ){
		long l=Systemtime.getTime()-terminalTime.getTime();
		long timedifference=l/1000;
		return Math.abs(timedifference);
		
	}
}
