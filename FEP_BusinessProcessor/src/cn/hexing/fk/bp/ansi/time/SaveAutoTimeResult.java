package cn.hexing.fk.bp.ansi.time;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import cn.hexing.db.bizprocess.MasterDbService;
import cn.hexing.fas.model.AnsiRequest;
import cn.hexing.fas.model.HostCommandResult;
import cn.hexing.fk.bp.ansi.AnsiEventProcessor;
import cn.hexing.fk.bp.model.HostCommandItemDb;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.model.MeasuredPoint;
import cn.hexing.fk.model.RtuManage;
import cn.hexing.fk.utils.StringUtil;

import com.hx.ansi.ansiElements.AnsiDataItem;

public class SaveAutoTimeResult {
	private static final SaveAutoTimeResult instance = new SaveAutoTimeResult();
	protected static final Logger log = Logger.getLogger(SaveAutoTimeResult.class);

	public final static SaveAutoTimeResult getInstance(){return instance;};
	
	private SaveAutoTimeResult(){};
	
	// 保存ANSI 对时结果
	public int  saveAutoTimeResult(AnsiRequest req , AnsiDataItem param,MasterDbService masterDbService) {
		// save result to tj_zddssj
		HostCommandItemDb commandItemDb = new HostCommandItemDb();
		commandItemDb.setTime(new Date());
		if(param.resultData !=null){
				String time=param.resultData;
				// iran? convert(date):date
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
		commandItemDb.setTn("0");
		commandItemDb.setStatus(1);
		commandItemDb.setLogicAddress(req.getMeterId());
		masterDbService.saveAutoTimeResult(commandItemDb);
		//判断是否需要自动对时 ，需要则发送自动对时请求
		String isAuto=(String) req.getAppendParam("bpAutoTimeSyn");
		if("1".equals(isAuto)&&(commandItemDb.getTimedifference()>((Integer)req.getAppendParam("timeDiffThreshold")))){
			TimeAutomatic.timeAutomatic(AnsiEventProcessor.getInstance(), req);
		}
		return 0;
	}
	
	public long timeOffset(Date Systemtime ,Date terminalTime  ){
		long l=Systemtime.getTime()-terminalTime.getTime();
		long timedifference=l/1000;
		return Math.abs(timedifference);
		
	}
}
