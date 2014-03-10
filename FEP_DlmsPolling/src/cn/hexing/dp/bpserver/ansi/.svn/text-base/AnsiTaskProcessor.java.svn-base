package cn.hexing.dp.bpserver.ansi;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import cn.hexing.dp.bpserver.TPConstant;
import cn.hexing.dp.dao.LoadDatasDao;
import cn.hexing.dp.model.RtuTask;
import cn.hexing.dp.model.TaskTemplate;
import cn.hexing.dp.queue.AnsiRequestQueue;
import cn.hexing.fas.model.AnsiRequest;
import cn.hexing.fas.model.AnsiRequest.ANSI_OP_TYPE;
import cn.hexing.fk.clientmod.ClusterClientModule;
import cn.hexing.fk.common.spi.IModule;
import cn.hexing.fk.common.spi.abstra.BaseModule;
import cn.hexing.fk.tracelog.TraceLog;
import cn.hexing.fk.utils.State;

import com.hx.ansi.ansiElements.AnsiDataItem;

/** 
 * @Description  xxxxx
 * @author  Rolinbor
 * @Copyright 2013 hexing Inc. All rights reserved
 * @time：2013-6-8 上午09:26:36
 * @version 1.0 
 */

public class AnsiTaskProcessor extends BaseModule{
	private static final Logger log=Logger.getLogger(AnsiTaskProcessor.class);
	private static final TraceLog trace=TraceLog.getTracer(AnsiTaskProcessor.class);
	
	private String name="AnsiTask";
	
	private volatile State state =new State();
	private WorkThread work=null;
	private Timer timer;			
	private ClusterClientModule com=null;
	
	private LoadDatasDao loadAnsiDatasDao;
	public enum AnsiTaskType { Terminal,Master};
	private static String PROTOCOL="06";
	
	public void setCom(ClusterClientModule com) {
		this.com = com;
	}
	public String getName() {
		return name;
	}
	
	public boolean isActive() {
		return state.isActive();
	}
	
	@Override
	public String getModuleType() {
		return IModule.MODULE_TYPE_BP;
	}
	
	@Override
	public boolean start() {
		if( !state.isStopped() )
			return false;
		state = State.STARTING;
		work=new WorkThread();
		work.start();
		AnsiRequestQueue.getInstance().initTaskData();
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
	/**
	 * getAnsiRequest
	 * @param rt
	 * @return
	 */
	private AnsiRequest getAnsiRequest(RtuTask rt){
		try {
			if(rt.getAnsiTaskType()==AnsiTaskType.Master){
				return masterTask(rt);
			}else{
				return terminalTask(rt);
			}
		} catch (Exception e) {
			log.error("create request error!"+e);
			return null;
		}
	}
	/**
	 * 结算任务
	 * @param rt
	 * @return
	 */
	private AnsiRequest masterTask(RtuTask rt){
		log.info("create AnsiTaskRequest,meterId:"+rt.getRtuId()+",taskNo:"+rt.getTaskNo()+",taskDate"+rt.getTaskDate());
		if("02".equals(rt.getTaskProperty())){
			AnsiRequest req =new AnsiRequest();
			req.setMeterId(rt.getRtuId());
			req.setProtocol(PROTOCOL);
			req.setOpType(ANSI_OP_TYPE.OP_READ);
			req.setTable(26);
			req.setServiceTag("30");
			req.setFull(true);
			AnsiDataItem[] datas=new AnsiDataItem[1];
			datas[0]=new AnsiDataItem();
			datas[0].date=rt.getTaskDate();
			req.setDataItem(datas);
			req.addAppendParam("taskNo", rt.getTaskNo());
			req.addAppendParam("startTime", rt.getTaskDate());
			req.addAppendParam("endTime", rt.getTaskDate());
			return req;
		}else if("05".equals(rt.getTaskProperty())){
			AnsiRequest req =new AnsiRequest();
			req.setMeterId(rt.getRtuId());
			req.setProtocol(PROTOCOL);
			req.setOpType(ANSI_OP_TYPE.OP_READ);
			req.setTable(2050);
			req.setServiceTag("30");
			req.setFull(true);
			AnsiDataItem[] datas=new AnsiDataItem[1];
			datas[0]=new AnsiDataItem();
			datas[0].date=rt.getTaskDate();
			req.setDataItem(datas);
			req.addAppendParam("taskNo", rt.getTaskNo());
			req.addAppendParam("startTime", rt.getTaskDate());
			req.addAppendParam("endTime", rt.getTaskDate());
			return req;
			}
		else {
			log.error("error...can't get right taskProperty");
			}
		return null;

	}
	/**
	 * 终端任务
	 * @param rt
	 * @return
	 */
	private AnsiRequest terminalTask(RtuTask rt){
		
		
		
		log.info("create AnsiTaskRequest,meterId:"+rt.getRtuId()+",taskNo:"+rt.getTaskNo()+",taskDate"+rt.getTaskDate());
		AnsiRequest req =new AnsiRequest();
		req.setMeterId(rt.getRtuId());
		req.setProtocol(PROTOCOL);
		req.setOpType(ANSI_OP_TYPE.OP_READ);
		req.setTable(64);
		req.setServiceTag("30");
		req.setFull(true);
		
//		
//		datas[1]=new AnsiDataItem();
//		datas[1].date=rt.getTaskDate();
//		// by fangjianming on 2013-10-10,to add the time interval of task
//		datas[1].startTime=calendar.getTime();
//		datas[1].endTime=calendar2.getTime();
//		datas[1].dataCode="00005200";
		req.setDataItem(getAnsiDataItems(rt));
		req.addAppendParam("taskNo", rt.getTaskNo());
		
		//modified by fangjianming on 2013-10-10,to modify the startTime of the task
		//add the sample Interval for caculating the offset
		//req.addAppendParam("startTime",rt.getTaskDate());

		req.addAppendParam("sampleInterval", rt.getSampleInterval());
		req.addAppendParam("sampleIntervalUnit", rt.getSampleIntervalUnit());
		return req;
	}
	
	private AnsiDataItem[] getAnsiDataItems(RtuTask rt) {
		if( rt.getTaskNo().equals("15")){
			
			String string="";
			string="";
		}
		//added by fangjianming on 2013-10-10 ,to add the time interval of task
		SimpleDateFormat simpleDateFormat2=new SimpleDateFormat("yyyyMMdd");
		Calendar calendar=Calendar.getInstance();
		Calendar calendar2=Calendar.getInstance();
		Calendar calendar3=calendar.getInstance();
		calendar.setTime(rt.getTaskDate());
		calendar2.setTime(rt.getTaskDate());
		calendar3.setTime(rt.getTaskDate());
		switch (Integer.parseInt(rt.getSampleIntervalUnit())) {
		case 2://minute
			calendar.add(Calendar.MINUTE, -rt.getSampleInterval());
			break;
		case 3://hour
			calendar.add(Calendar.HOUR, -rt.getSampleInterval());
			break;
		case 4://day
			calendar.add(Calendar.DATE, -rt.getSampleInterval());
			break;
		case 5://month
			calendar.add(Calendar.MONTH, -rt.getSampleInterval());
			break;
		default:
			break;
		}
		
		long count1=0;
		try {
			long time1=simpleDateFormat2.parse(simpleDateFormat2.format(calendar2.getTime())).getTime();
			long time2=simpleDateFormat2.parse(simpleDateFormat2.format(calendar.getTime())).getTime();
			count1= (time1-time2)/(1000*60*60*24)+1;
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		int count=(int)count1;
		//int count= (int)(calendar2.getTime().getTime()-calendar.getTime().getTime())/(1000*60*60*24)+1;
		
		AnsiDataItem[] datas=new AnsiDataItem[1+count];
		datas[0]=new AnsiDataItem();
		datas[0].date=rt.getTaskDate();
		// by fangjianming on 2013-10-10,to add the time interval of task
		datas[0].startTime=calendar.getTime();
		datas[0].endTime=calendar2.getTime();
		
		SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat sdf2=new SimpleDateFormat("yyyyMMddHHmmss");
	
		for (int i = 0; i < count; i++) {
			if(i>0){
				calendar3.add(Calendar.DATE, -1);
			}
			if(i==0 && i==count-1){//all the data in one day
				datas[1+i]=new AnsiDataItem();
				datas[1+i].dataCode="00005200";
				datas[1+i].date=rt.getTaskDate();
				datas[1+i].startTime=calendar.getTime();
				datas[1+i].endTime=calendar2.getTime();
				break;
			}
			
			if(i==0){//first block
				datas[1+i]=new AnsiDataItem();
				datas[1+i].dataCode="00005200";
				datas[1+i].date=rt.getTaskDate();
				try {
					datas[1+i].startTime=sdf2.parse(sdf.format(calendar2.getTime())+"000000");
				} catch (Exception e) {
					e.printStackTrace();
				}
				datas[1+i].endTime=calendar2.getTime();
			}
			else if(i==count-1){//last block
				datas[1+i]=new AnsiDataItem();
				datas[1+i].dataCode="00005200";
				datas[1+i].date=rt.getTaskDate();
				datas[1+i].startTime=calendar.getTime();
				try {
					datas[1+i].endTime=sdf2.parse(sdf.format(calendar3.getTime())+"234500");
				} catch (Exception e) {
					e.printStackTrace();
				}
				System.out.println("startTime"+sdf2.format(datas[i+1].startTime));
				System.out.println("endTime"+sdf2.format(datas[i+1].endTime));
			}
			else {// midle block
				datas[1+i]=new AnsiDataItem();
				datas[1+i].dataCode="00005200";
				datas[1+i].date=rt.getTaskDate();
				try {
					datas[1+i].startTime=sdf2.parse(sdf.format(calendar3.getTime())+"000000");
					datas[1+i].endTime=sdf2.parse(sdf.format(calendar3.getTime())+"234500");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			
		}
		return datas;
	}
	
	
	
	private class WorkThread extends Thread{
		public WorkThread(){
		}
		public void run() {
			log.info("work running:"+this.getName());
			while( !AnsiTaskProcessor.this.state.isStopping() && !AnsiTaskProcessor.this.state.isStopped() ){
				try{
					ArrayList<RtuTask> list=AnsiRequestQueue.getInstance().getRtuTaskRequestList(System.currentTimeMillis());
					if (list!=null&&list.size()>0){//组request,并发送
						long sleepWhenOverMaxSendTime = TPConstant.getInstance().getSleepWhenOverMaxSendTime();
						int maxSendOneTime = TPConstant.getInstance().getMaxSendOneTime();
						int sendSize=0;
						
						for(RtuTask rt:list){
								AnsiRequest ansiRequest=getAnsiRequest(rt);
								if(ansiRequest == null) continue;
								if(sendSize++>maxSendOneTime){
									//如果发送的个数超过一次发送最大个数,睡眠1秒
									Thread.sleep(sleepWhenOverMaxSendTime);
									sendSize=0;
								}
								com.sendRequest(null, null, ansiRequest);
						}																		
					}
					else{
						Thread.sleep(5000);
					}				
				}catch(Exception exp){
					log.error("ANSI下行处理器处理线程处理出错", exp);
					continue;
				}
			}
		}				
	}
	
	@Override
	public void stop() {
		state = State.STOPPING;
		work.interrupt();
		if( log.isInfoEnabled())
			log.info("thread("+name+") stop...");
		state = State.STOPPED;
	}
	/**
	 * ANSI  从rt里面获取的rt.getTaskDate()是上报的间隔时间（主站读取任务的间隔时间）
	 *   而下发请求里面带的时间应该是采样间隔时间（终端冻结任务的间隔时间）
	 * @param rt
	 * @return
	 */
	public Date getTaskDate(RtuTask rt){
		List<TaskTemplate> taskTemplateList=loadAnsiDatasDao.getTaskTemplateById(PROTOCOL,rt.getTaskTemplateID());
		int n=0;
		return new Date();	
	}
	
	public final void setLoadAnsiDatasDao(LoadDatasDao loadAnsiDatasDao) {
		this.loadAnsiDatasDao = loadAnsiDatasDao;
	}
	
}
