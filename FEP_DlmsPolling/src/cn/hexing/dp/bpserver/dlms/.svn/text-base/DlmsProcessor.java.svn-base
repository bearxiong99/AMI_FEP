/**
 
 */
package cn.hexing.dp.bpserver.dlms;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.quartz.CronExpression;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.StatefulJob;
import org.quartz.impl.StdSchedulerFactory;

import cn.hexing.dp.bpserver.TPConstant;
import cn.hexing.dp.dao.LoadDatasDao;
import cn.hexing.dp.model.RtuTask;
import cn.hexing.dp.queue.DlmsReqestQueue;
import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fas.model.dlms.DlmsRequest.DLMS_OP_TYPE;
import cn.hexing.fk.clientmod.ClusterClientModule;
import cn.hexing.fk.common.spi.IModule;
import cn.hexing.fk.common.spi.abstra.BaseModule;
import cn.hexing.fk.model.DlmsItemRelated;
import cn.hexing.fk.model.DlmsMeterRtu;
import cn.hexing.fk.model.RtuManage;
import cn.hexing.fk.model.TaskTemplate;
import cn.hexing.fk.utils.DateConvert;
import cn.hexing.fk.utils.State;
import cn.hexing.fk.utils.StringUtil;
import cn.hexing.util.HexDump;

import com.hx.dlms.ASN1SequenceOf;
import com.hx.dlms.DlmsData;
import com.hx.dlms.applayer.SelectiveAccessDescriptor;

/**
 *
 */
public class DlmsProcessor extends BaseModule implements StatefulJob{
	private static final Logger log = Logger.getLogger(DlmsProcessor.class);
	//可配置属性
	private String name = "dlmsBp";	
	//对象内部状态
	private volatile State state = new State();	
	private WorkThread work=null;
	private Timer timer;			
	private ClusterClientModule com=null;
	
	private String oldProtocols = "";
	
	private String freezeDayObiss=null;
	
	private String freezeMonthObiss =null;
	
	//数据项与obis对应关系表  [obis,code]
	private static Map<String,String> dlmsItemRelated = new HashMap<String, String>();
	private LoadDatasDao loadDatasDao;
	
	private boolean isIranTime;
	
	public enum TaskType { Terminal,Master,Event};
	
	public void setCom(ClusterClientModule com) {
		this.com = com;
	}
	public String getName() {
		return name;
	}
	
	public boolean isActive() {
		return state.isActive();
	}

	public boolean start() {
		if( !state.isStopped() )
			return false;
		state = State.STARTING;
		work=new WorkThread();
		work.start();
		DlmsReqestQueue.getInstance().initTaskData();
		timer=new Timer();
		TimerTask task=new TimerTask(){
			@Override
			public void run(){
				onTimer();
			}
		};
		timer.schedule(task,0, 2*1000);	
		
		freezeDayObiss = System.getProperty("dlms.ids.freezeDay");
		freezeMonthObiss = System.getProperty("dlms.ids.freezeMonth");
		
		try {
			eventTaskInit();
		} catch (SchedulerException e) {
			log.error(StringUtil.getExceptionDetailInfo(e));
		} catch (ParseException e) {
			log.error(StringUtil.getExceptionDetailInfo(e));
		}
		
		state = State.RUNNING;
		if( log.isInfoEnabled())
			log.info("thread("+name+") start...");
		return true;
	}

	private void onTimer(){
		
	}

	private DlmsRequest getDlmsRequest(RtuTask rt){
			try {
				if(rt.getTaskType()==TaskType.Master){
					return masterTask(rt);
				}else{
					return channelTask(rt);
				}
			} catch (Exception e) {
				log.error("create request error!"+StringUtil.getExceptionDetailInfo(e));
				return null;
			}
	}
	
	private List<DlmsRequest> buildEventRequest(String rtuId,String startTime,String endTime){
		String eventParams[]=System.getProperty("dlms.event.obis").split(";");
		List<DlmsRequest> request=new ArrayList<DlmsRequest>();
		DlmsRequest req = new DlmsRequest();
		DlmsObisItem[] params = new DlmsObisItem[eventParams.length];
		SelectiveAccessDescriptor sad = new SelectiveAccessDescriptor();
		startTime=isIranTime?DateConvert.gregorianToIran(startTime):startTime;
		endTime=isIranTime?DateConvert.gregorianToIran(endTime):endTime;
		sad.selectByPeriodOfTime(startTime, endTime);
		for(int i=0;i<eventParams.length;i++){
			String obis = eventParams[i];
			params[i] = new DlmsObisItem();
			params[i].accessSelector = 1;
			params[i].obisString=obis;
			params[i].classId = 7;
			params[i].attributeId = 2;
			params[i].data.assignValue(sad.getParameter());
		}
		req.setParams(params);
		req.setOperator("EVENT_READING_AUTO");
		req.setOpType(DLMS_OP_TYPE.OP_GET);
		req.setMeterId( rtuId);
		req.setProtocol("03");
		request.add(req);
		return request;
	}
	/**
	 * 通道任务
	 * @param req
	 * @return
	 */
	private DlmsRequest channelTask(RtuTask req) {
		DlmsObisItem[] params = new DlmsObisItem[1];
		params[0] = new DlmsObisItem();
		params[0].attributeId = 2;
		params[0].classId = 7;
		params[0].obisString = "0." + req.getTaskNo() + ".24.3.0.255";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date timePoint = req.getTaskDate();
		
		Date endDate = req.getTaskDate();
		//由于表计存储时钟不一定是00秒，所以读取的时候要偏移1分钟
		Calendar c = Calendar.getInstance();
		c.setTime(endDate);
		c.add(Calendar.MINUTE, 1);
		endDate = c.getTime();
		
		String endTime= sdf.format(endDate);
		req.getSampleIntervalUnit();
		Date startDate=getStartTime(req.getSampleInterval(),req.getSampleIntervalUnit(),timePoint);
		String startTime=sdf.format(startDate);
		startTime=isIranTime?DateConvert.gregorianToIran(startTime):startTime;
		endTime = isIranTime ? DateConvert.gregorianToIran(endTime): endTime;
		DlmsRequest dr=createRequestWithTime(req, params,new String[]{startTime},new String[]{endTime});
		dr.setOperator("DlmsPolling");
		return dr;
	}
	
	/**
	 * 读主站任务不再一个时间点去读，读一个时间段
	 * @param sampleInterval
	 * @param sampleIntervalUnit
	 * @param endTime
	 * @return
	 */
	private Date getStartTime(int sampleInterval, String sampleIntervalUnit,Date endTime) {
		Calendar c = Calendar.getInstance();
		c.setTime(endTime);
		int unit=0;
		if (sampleIntervalUnit.equals("02")){//采样间隔时间单位：分钟
			unit=Calendar.MINUTE;
		}
		else if(sampleIntervalUnit.equals("03")){//采样间隔时间单位：时
			unit=Calendar.HOUR;
		}
		else if(sampleIntervalUnit.equals("04")){//采样间隔时间单位：日
			unit=Calendar.DATE;
		}
		else if(sampleIntervalUnit.equals("05")){//采样间隔时间单位：月
			unit=Calendar.MONTH;
		}
		c.add(unit, -sampleInterval);
		return c.getTime();
	}
	/**
	 * 创建一个带时间读的对象。 类似读通道。
	 * @return
	 */
	private DlmsRequest createRequestWithTime(RtuTask req,DlmsObisItem[] params,String[] startTime,String[] endTime){

		log.info("Start Create RequestWithTime,Task No is:"+req.getTaskNo()+",MeterId is:"+req.getRtuId());
		DlmsRequest dlmsRequest = null;
		try {
			for(int i=0;i<params.length;i++){
				
				DlmsData[] array = new DlmsData[4];
				array[0] = new DlmsData();
				DlmsData[] array2 = new DlmsData[4];
				array2[0] = new DlmsData();
				array2[0].setUnsignedLong(8);
				array2[1] = new DlmsData();
				array2[1].setOctetString(HexDump.toArray("0000010000FF"));
				array2[2] = new DlmsData();
				array2[2].setDlmsInteger((byte) 2);
				array2[3] = new DlmsData();
				array2[3].setUnsignedLong(0);
				array[1] = new DlmsData();
				array[2] = new DlmsData();
				array[3] = new DlmsData();
				ASN1SequenceOf struct1 = new ASN1SequenceOf(array2);
				array[0].setStructure(struct1);
				DlmsMeterRtu meterRtu = RtuManage.getInstance().getDlmsMeterRtu(req.getRtuId());
				dlmsRequest = new DlmsRequest();
				boolean isEncodeLength = true;
				if(meterRtu.getSubProtocol()==null ||meterRtu.getSubProtocol().equals("0")|| oldProtocols.indexOf(meterRtu.getSubProtocol())<0){
					array[1].setDlmsDateTime(startTime[i]);
					array[2].setDlmsDateTime(endTime[i]);
					DlmsData[] dd = null;
					array[3].setArray(dd);
				}else{
					array[1].setOldDlmsDateTime(startTime[i]);
					array[2].setOldDlmsDateTime(endTime[i]);
					dlmsRequest.setSubprotocol(meterRtu.getSubProtocol());
					isEncodeLength = false;
				}
				ASN1SequenceOf struct = new ASN1SequenceOf(array);
				struct.isEncodeLength = isEncodeLength;
				params[i].data.setStructure(struct);
				params[i].accessSelector = 1;
			}
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			dlmsRequest.addAppendParam("taskNo", req.getTaskNo());
			dlmsRequest.addAppendParam("taskDate", sdf.format(req.getTaskDate()));
			dlmsRequest.setMeterId(req.getRtuId());
			dlmsRequest.setProtocol("03");
			dlmsRequest.setOpType(DlmsRequest.DLMS_OP_TYPE.OP_GET);
			dlmsRequest.setParams(params);
			log.info("send dlms request. taskNo:" + req.getTaskNo() + ",time:"+ sdf.format(req.getTaskDate()) + ",meterId:" + req.getRtuId());
		} catch (Exception ex) {
			log.error("getDlmsRequest err:" + StringUtil.getExceptionDetailInfo(ex));
		}
		return dlmsRequest;
	
		
	}
	
	/**
	 * 主站任务
	 * @param req
	 * @return
	 */
	private DlmsRequest masterTask(RtuTask req) {
		DlmsMeterRtu meterRtu = RtuManage.getInstance().getDlmsMeterRtu(req.getRtuId());
		if(meterRtu ==null) return null;
		TaskTemplate taskTemplate = meterRtu.getTaskTemplate(req.getTaskNo());
		if(taskTemplate ==null) return null;
		List<String> dataCodes=taskTemplate.getDataCodes();
		req.setTaskProperty(taskTemplate.getTaskProperty());
		return createMasterTaskRequest(req,dataCodes);
	}
	
	private DlmsRequest createMasterTaskRequest(RtuTask req,
			List<String> dataCodes) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat daySdf = new SimpleDateFormat("yyyy-MM-dd");
		if(dlmsItemRelated.size()==0){
			initDlmsItemRelated();
		}
		int size=dataCodes.size();
		DlmsObisItem[] params = new DlmsObisItem[size];
		String[] startTimes = new String[size];
		String[] endTimes = new String[size]; 
		for(int i=0;i<size;i++){
			String code = dataCodes.get(i);
			String itemId=dlmsItemRelated.get(code);
			if(itemId==null) {
				log.warn("rtuId:"+req.getRtuId()+",taskNo:"+req.getTaskNo()+",dlms item can't related,code is "+code);
				return null;	
			}
			String[] attrDescs=itemId.split("#");
			params[i] = new DlmsObisItem();
			params[i].classId = Integer.parseInt(attrDescs[0]);
			params[i].attributeId = Integer.parseInt(attrDescs[2]);
			params[i].obisString = attrDescs[1];
			//1.如果是日冻结数据。那么开始实际那就是当前的任务时间，结束时间就是之后一天
			//  例如,任务时间是2012-11-10 00:00:00 , 那么结束时间就是2012-11-11 00:00:00
			//	这个时间读到的数据时  10冻结9号的数据。
			//2.如果是月冻结数据。任务开始时间为2012-11-10  00:00:00
			//	如果要抄到10月冻结数据，那么开始时间，2012-10-15 00:00:00 结束时间为 2012-11-15 00:00:00
			//3.如果是伊朗历...
			// 	任务时间为2012-11-10 00:00:00 ,伊朗历为：1391-08-20 00:00:00
			//  月冻结:那么开始时间，为 1391-07-15 00:00:00 ,结束时间为:1391-08-15 00:00:00
			//	日冻结:那么开始时间，为1391-08-20 00:00:00,结束时间为:1391-08-21 00:00:00
			String s_startTime="";
			String s_endTime="";
			Date d_startTime=null;
			Date d_endTime  =null;
			if(freezeDayObiss!=null && freezeDayObiss.contains(itemId)){
				//日冻结,从当前日零点开始读，到第二天0点
				Calendar c = Calendar.getInstance();
				c.setTime(req.getTaskDate());
				d_startTime = c.getTime();
				c.add(Calendar.DATE, 1);
				d_endTime = c.getTime();
				s_startTime = daySdf.format(d_startTime)+" 00:00:00";
				s_endTime = daySdf.format(d_endTime)+" 00:00:00";
				s_startTime=isIranTime?DateConvert.gregorianToIran(s_startTime):s_startTime;
				s_endTime = isIranTime?DateConvert.gregorianToIran(s_endTime):s_endTime;
				startTimes[i]= s_startTime;
				endTimes[i] = s_endTime;
				
			}else if(freezeMonthObiss!=null && freezeMonthObiss.contains(itemId)){
				//月冻结 
				if(isIranTime){
					Calendar c = Calendar.getInstance();
					d_startTime=c.getTime();
					s_startTime= daySdf.format(d_startTime)+" 00:00:00";
					c.add(Calendar.MONTH, 1);
					d_endTime = c.getTime();
					s_endTime =sdf.format(d_endTime)+" 00:00:00";
					s_startTime=DateConvert.gregorianToIran(s_startTime);
					startTimes[i]=s_startTime.substring(0, 8)+"01"+s_startTime.substring(10, s_startTime.length());
					s_endTime = DateConvert.gregorianToIran(s_endTime);
					endTimes[i]=s_endTime.substring(0, 8)+"01"+s_endTime.substring(10, s_startTime.length());					
					
				}else{
					Calendar c =Calendar.getInstance();
					c.setTime(req.getTaskDate());
					c.set(Calendar.DATE, 1);
					d_startTime = c.getTime();
					c.add(Calendar.MONTH, 1);
					d_endTime=c.getTime();
					startTimes[i] = daySdf.format(d_startTime)+" 00:00:00";
					endTimes[i] = daySdf.format(d_endTime)+" 00:00:00";
				}
			}else { //否则全按小时冻结
				Calendar c = Calendar.getInstance();
				c.setTime(req.getTaskDate());
				d_startTime = c.getTime();
				s_startTime = sdf.format(d_startTime);
				startTimes[i]= s_startTime;
				endTimes[i] = s_startTime;
			}
		}
		DlmsRequest dr = createRequestWithTime(req, params, startTimes, endTimes);
		dr.setParams(params);
		dr.setMeterId(req.getRtuId());
		dr.setOpType(DLMS_OP_TYPE.OP_GET);
		dr.setOperator("MasterTask");
		return dr;
	}
	private void initDlmsItemRelated() {
		List<DlmsItemRelated> list = loadDatasDao.loadDlmsItemRelated();
		for(DlmsItemRelated item : list){
			dlmsItemRelated.put( item.getCode(),item.getAttribute());
		}
	}
	public void stop() {
		state = State.STOPPING;
		work.interrupt();
		if( log.isInfoEnabled())
			log.info("thread("+name+") stop...");
		state = State.STOPPED;
	}
	
	public String getModuleType() {
		return IModule.MODULE_TYPE_BP;
	}
	
	private class WorkThread extends Thread{
		public WorkThread(){
		}
		public void run() {
			log.info("work running:"+this.getName());
			while( !DlmsProcessor.this.state.isStopping() && !DlmsProcessor.this.state.isStopped() ){
				try{
					ArrayList<RtuTask> list=DlmsReqestQueue.getInstance().getRtuTaskRequestList(System.currentTimeMillis());
					if (list!=null&&list.size()>0){//组request,并发送
						List<DlmsRequest> requests = new ArrayList<DlmsRequest>();
						
						//----暂时这么处理，未来对于表多的情况再另外处理。
						List<DlmsMeterRtu> onlineMeters = loadDatasDao.get24HourOnlineMeter();
						List<String> onlineAddress = new ArrayList<String>();
						for(DlmsMeterRtu dmr:onlineMeters){
							onlineAddress.add(dmr.getLogicAddress());
						}
						
						for(RtuTask rt:list){
							//查询最近在线的表，如果不在线，不发送请求
							if(!onlineAddress.contains(rt.getRtuId())){
								log.info(rt.getRtuId()+"not online,so don't send request");
								continue;
							}
							DlmsRequest dlmsReq=getDlmsRequest(rt);
							if(dlmsReq == null) continue;
							requests.add(dlmsReq);
						}																		
						sendRequest(requests,com);
					}
					else{
						Thread.sleep(5000);
					}				
				}catch(Exception exp){
					log.error("dlms下行处理器处理线程处理出错", exp);
					continue;
				}
			}
		}
	}
	private void sendRequest(List<DlmsRequest> requests,ClusterClientModule com) throws InterruptedException {
		if(requests==null || requests.size()<=0 ) return ;
		long sleepWhenOverMaxSendTime = TPConstant.getInstance().getSleepWhenOverMaxSendTime();
		int maxSendOneTime = TPConstant.getInstance().getMaxSendOneTime();
		int sendSize=0;
		for(DlmsRequest req : requests){
			if(sendSize++>maxSendOneTime){
				//如果发送的个数超过一次发送最大个数,睡眠1秒
				Thread.sleep(sleepWhenOverMaxSendTime);
				sendSize=0;
			}
			com.sendRequest(null, null, req);
		}
	}
	
	public void setIranTime(String isIranTime){
		this.isIranTime = isIranTime==null||!isIranTime.equals("true")?false:true;
	}
	public final void setLoadDatasDao(LoadDatasDao loadDatasDao) {
		this.loadDatasDao = loadDatasDao;
	}
	public final void setOldProtocols(String oldProtocols) {
		this.oldProtocols = oldProtocols;
	}
	
	private void eventTaskInit() throws SchedulerException, ParseException {
		String eventTaskSwitch = System.getProperty("dlms.event.task");
		if(! "true".equals(eventTaskSwitch)) return;
		String cron = System.getProperty("event.task.cronExpression");
		JobDetail jd = new JobDetail("evntTask", "gEventTask",getClass());
		CronExpression expression= new CronExpression(cron);
		CronTrigger triger = new CronTrigger("eventTriger","gEventTriger");
		triger.setCronExpression(expression);
		int eventInterval = Integer.parseInt(System.getProperty("dlms.event.sampleInterval"));
		jd.getJobDataMap().put("EventInterval", eventInterval);
		jd.getJobDataMap().put("DataDao", loadDatasDao);
		jd.getJobDataMap().put("ClusterModule", com);
		SchedulerFactory sf = new StdSchedulerFactory();
		Scheduler scheduler = sf.getScheduler();
		scheduler.scheduleJob(jd, triger);
	}

	@Override
	public void execute(JobExecutionContext ctx) throws JobExecutionException {
		JobDataMap map = ctx.getMergedJobDataMap();
		LoadDatasDao loadDatasDao = (LoadDatasDao) map.get("DataDao");
		ClusterClientModule com = (ClusterClientModule)map.get("ClusterModule");
		List<DlmsMeterRtu> meters = loadDatasDao.get24HourOnlineMeter();
		int interval = (Integer) map.get("EventInterval");
		Date endTime = Calendar.getInstance().getTime();
		Calendar startC = Calendar.getInstance();
		startC.add(Calendar.HOUR, -interval);
		Date startTime = startC.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String sStartTime = sdf.format(startTime);
		String sEndTime = sdf.format(endTime);

		List<DlmsRequest> requests = new ArrayList<DlmsRequest>();
		for (DlmsMeterRtu rtu : meters) {
			requests.addAll(buildEventRequest(rtu.getLogicAddress(), sStartTime,
					sEndTime));
		}
		try {
			sendRequest(requests,com);
		} catch (InterruptedException e) {
			log.error(StringUtil.getExceptionDetailInfo(e));
		}
	}

}
