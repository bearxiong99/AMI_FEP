package cn.hexing.dp.bpserver.gg;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import cn.hexing.dp.bpserver.TPConstant;
import cn.hexing.dp.bpserver.dlms.DlmsProcessor;
import cn.hexing.dp.dao.LoadDatasDao;
import cn.hexing.dp.model.RtuTask;
import cn.hexing.dp.queue.GdgyRequestQueue;
import cn.hexing.fas.model.FaalGGKZM12Request;
import cn.hexing.fas.model.FaalRequestRtuParam;
import cn.hexing.fk.clientmod.ClusterClientModule;
import cn.hexing.fk.common.spi.IModule;
import cn.hexing.fk.common.spi.abstra.BaseModule;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.model.RtuManage;
import cn.hexing.fk.model.TaskTemplate;
import cn.hexing.fk.tracelog.TraceLog;
import cn.hexing.fk.utils.State;

public class GgTaskProcessor extends BaseModule{
	private static final Logger log = Logger.getLogger(DlmsProcessor.class);
	private static final TraceLog tracer = TraceLog.getTracer(DlmsProcessor.class);
	//可配置属性
	private String name = "GgTask";	
	//对象内部状态
	private volatile State state = new State();	
	private WorkThread work=null;
	private Timer timer;			
	private ClusterClientModule com=null;
	
	
	private LoadDatasDao loadGgDatasDao;

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
		GdgyRequestQueue.getInstance().initTaskData();
		timer=new Timer();
		TimerTask task=new TimerTask(){
			@Override
			public void run(){
				onTimer();
			}
		};
		timer.schedule(task,0, 2*1000);		
	
		state = State.RUNNING;
		if( log.isInfoEnabled())
			log.info("thread("+name+") start...");
		
		return true;
	}
	private void onTimer(){
		
	}

	private List<FaalGGKZM12Request> getGgRequest(RtuTask req) {
		return terminalTask(req);
	}
	
	/**
	 * 创建终端任务
	 */
	private List<FaalGGKZM12Request> terminalTask(RtuTask req) {
		BizRtu meterRtu = RtuManage.getInstance().getBizRtuInCache(req.getRtuId());
		if(meterRtu==null) return null;
		TaskTemplate taskTemplate = meterRtu.getTaskTemplate(req.getTaskNo());
		if(taskTemplate==null) return null;
		req.setTaskProperty(taskTemplate.getTaskProperty());
		return createTerminalTaskRequest(req,taskTemplate);
	}
	/**
	 *  根据request和加载的任务数据项来组广规request
	 * @param req
	 * @param taskTemplate 
	 * @param dataCodes
	 * @return
	 */
	private List<FaalGGKZM12Request> createTerminalTaskRequest(RtuTask req, TaskTemplate taskTemplate) {
		List<FaalGGKZM12Request> requests=new ArrayList<FaalGGKZM12Request>();
		List<String> codes = taskTemplate.getDataCodes();
		if(codes ==null || codes.size() ==0) return null;
		else{
			for(String code:codes){
				FaalGGKZM12Request request = new FaalGGKZM12Request();
				log.info("create FaalGGKZM12Request,meterId:"+req.getRtuId()+",taskNo:"+req.getTaskNo()+",taskDate"+req.getTaskDate());
				if("42".equals(req.getTaskProperty())){//日冻结任务  根据taskProperty来判断任务类型，本来已经稳定的版本不去修改，针对秘鲁项目，任务轮招修改一下
					request.setProtocol("04");
					request.setTaskNo(req.getTaskNo());
					request.setStartTime(getDateBefore(req.getTaskDate()));
					request.setEndTime(req.getTaskDate());
					FaalRequestRtuParam rtuparam =new FaalRequestRtuParam();
					rtuparam.setCmdId((long)40754);
					rtuparam.setRtuId(req.getRtuId());
					rtuparam.addParam(code, null);
					request.addRtuParam(rtuparam);
//					request.setYhlx("");  //用户类型暂时不写进来
					request.setType(18); // 0X12=18
				    requests.add(request);
				}
				else if("43".equals(req.getTaskProperty())){//月冻结任务
					request.setProtocol("04");
					request.setTaskNo(req.getTaskNo());
					request.setStartTime(getDateBeforeMonth(req.getTaskDate()));
					request.setEndTime(req.getTaskDate());
					FaalRequestRtuParam rtuparam =new FaalRequestRtuParam();
					rtuparam.setCmdId((long)40754);
					rtuparam.setRtuId(req.getRtuId());
					rtuparam.addParam(code, null);
					request.addRtuParam(rtuparam);
//					request.setYhlx("");  //用户类型暂时不写进来
					request.setType(18); // 0X12=18
					requests.add(request);
				}else {//负荷任务   else if("44".equals(req.getTaskProperty()))
					request.setProtocol("04");
					request.setTaskNo(req.getTaskNo());
					request.setStartTime(req.getTaskDate());
					//负荷任务，要读取10:00的数据，那么需要传送时间为10：00-10：01（1分钟），采集器处理时间是一个前闭后开的区间
					//所以这里结束时间加上30秒
					request.setEndTime(getDateAfterNow(req.getTaskDate()));
					FaalRequestRtuParam rtuparam =new FaalRequestRtuParam();
					rtuparam.setCmdId((long)40754);
					rtuparam.setRtuId(req.getRtuId());
					rtuparam.addParam(code, null);
					request.addRtuParam(rtuparam);
//					request.setYhlx("");  //用户类型暂时不写进来
					request.setType(18); // 0X12=18
					requests.add(request);
				}
			}
		}
		return requests;
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
			while( !GgTaskProcessor.this.state.isStopping() && !GgTaskProcessor.this.state.isStopped() ){
				try{
					ArrayList<RtuTask> list=GdgyRequestQueue.getInstance().getRtuTaskRequestList(System.currentTimeMillis());
					if (list!=null&&list.size()>0){
						long sleepWhenOverMaxSendTime = TPConstant.getInstance().getSleepWhenOverMaxSendTime();
						int maxSendOneTime = TPConstant.getInstance().getMaxSendOneTime();
						int sendSize=0;
						//组request,并发送
						for(RtuTask rt:list){
							List<FaalGGKZM12Request> ggRequestList=getGgRequest(rt);
							if(ggRequestList==null) continue;
							if(sendSize++>maxSendOneTime){
								//如果发送的个数超过一次发送最大个数,睡眠1秒
								Thread.sleep(sleepWhenOverMaxSendTime);
								sendSize=0;
							}
							for(FaalGGKZM12Request ggRequest:ggRequestList){
								com.sendRequest(null, null, ggRequest);
								try{
									Thread.sleep(500);//休息一会儿发下一帧
								}catch(Exception e){
									e.printStackTrace();
								}
							}
						}																		
					}
					else{
						Thread.sleep(5000);
					}				
				}catch(Exception exp){
					log.error("广规任务轮招下行处理器处理线程处理出错", exp);
					continue;
				}
			}
		}				
	}

	public final void setLoadGgDatasDao(LoadDatasDao loadGgDatasDao) {
		this.loadGgDatasDao = loadGgDatasDao;
	}
	public Date getDateBefore(Date date){
		Calendar calendar = Calendar.getInstance(); //得到日历
		calendar.setTime(date);//把当前时间赋给日历
		calendar.add(Calendar.DAY_OF_MONTH, -1);  //设置为前一天
		return  calendar.getTime();   //得到前一天的时间
	}
	public Date getDateBeforeMonth(Date date){
		Calendar calendar = Calendar.getInstance(); //得到日历
		calendar.setTime(date);//把当前时间赋给日历
		calendar.add(Calendar.MONTH, -1);  //设置为当前的上一个月
		return  calendar.getTime();   //得到前一天的时间
	}
	public Date getDateAfterNow(Date date){
		Calendar calendar = Calendar.getInstance(); //得到日历
		calendar.setTime(date);//把当前时间赋给日历
		calendar.add(Calendar.MINUTE, +1);  //设置为当前的上一个月
		return  calendar.getTime();   //得到前一天的时间
	}
}

