package cn.hexing.reread.bpserver.ansi;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import cn.hexing.fas.model.AnsiRequest;
import cn.hexing.fas.model.AnsiRequest.ANSI_OP_TYPE;
import cn.hexing.fk.clientmod.ClusterClientModule;
import cn.hexing.reread.bpserver.parent.Rereader;
import cn.hexing.reread.model.RereadPoint;
import cn.hexing.util.CalendarUtil;

import com.hx.ansi.ansiElements.AnsiDataItem;
/**
 * ANSI漏电补召-组包发送请求到bp
 * @ClassName:AnsiRereader
 * @author kexl
 * @date 2012-9-24 上午10:31:52
 *
 */
public class AnsiRereader implements Rereader{
	private static final Logger log = Logger.getLogger(AnsiRereader.class);
	public static final String PROTOCOL = "06";
	public static final String OPERATOR_ZD = "DlmsPolling"; 
	public static final String OPERATOR_ZZ = "MasterTask";
	public static final String RWLX_ZD = "01"; //终端任务
	public static final String RWLX_ZZ = "02"; //主站任务
	//是否伊朗历
	private String iranTime = null;
	private boolean isIranTime = false;
	//发送请求间隔时间
	private String interval = null;
	private int intervalValue = 100;
	//发送请求间隔条数
	private String intervalReqCnt = null;
	private int intervalReqCntValue = 1000;
	//最大尝试发送几次
	private String sendTimes = "1";
	private int sendTimesValue = 1;
	private ClusterClientModule com=null;
	
	private String oldProtocols = "";
	
	private String jobIndex;
	public void read(List<RereadPoint> points , String rwlx , Map<String,String> dlmsItemRelated,String sampleIntervalUnit,int sampleInterva
			,String jobIndex){
		this.jobIndex = jobIndex;
		if("true".equals(iranTime))isIranTime = true;//使用配置的参数
		if(interval!=null) try{intervalValue = Integer.parseInt(interval);}catch(Exception e){log.warn(e); };//使用配置的参数
		if(intervalReqCnt!=null) try{intervalReqCntValue = Integer.parseInt(intervalReqCnt);}catch(Exception e){log.warn(e); };//使用配置的参数
		if(sendTimes!=null) try{sendTimesValue = Integer.parseInt(sendTimes);}catch(Exception e){log.warn(e); }; //使用配置的参数
		if(sendTimesValue<1) sendTimesValue = 1;//最小一次
		log.info(jobIndex + "isIranTime="+isIranTime+",intervalValue="+intervalValue+",sendTimesValue="+sendTimesValue +",intervalReqCntValue="+intervalReqCntValue+"。Beging call BP... ");
		//对漏点（终端逻辑地址、测量点号、任务号、数据时间）进行归总，重复的去掉
		Map<String,List<RereadPoint>> realRereadPoints = new HashMap<String , List<RereadPoint>>();
		if(points!=null) log.info(jobIndex + points.size() + " points be queryed from database.");
		if(points!=null && points.size()>0){
			boolean isCbr = "01".equals(points.get(0).getTaskType());
			for(RereadPoint point : points){
				String key = point.getTerminalAddr()+"_" + point.getCldh()+"_"+point.getTaskNo()+"_";
				String timePoint = point.getTimePoint();
				/**kexl 临时增加，在么有排查出原因的情况下限制大于当前时间的漏点不去补召**/
				if(CalendarUtil.parse(timePoint).compareTo(Calendar.getInstance()) > 0){
					log.warn("***************异常的漏点："+point);
					continue; 
				}
				/**end**/
				if(isCbr){ //如果是抄表日任务，则按照终端逻辑地址、测量点号、任务号、数据时间_月份进行归总，重复的去掉
					//timePoint = isIranTime?DateConvert.gregorianToIran(timePoint):timePoint;
					key += timePoint.substring(0,7);
				}else{
					key += timePoint;
				}
				if(realRereadPoints.keySet().contains(key)){
					realRereadPoints.get(key).add(point);
				}else{
					List<RereadPoint> temp = new ArrayList<RereadPoint>();
					temp.add(point);
					realRereadPoints.put(key, temp);
				}
			}
		}
		int totalSize = realRereadPoints.keySet().size();
		log.info(jobIndex + totalSize + " points begin reread...");
		
		for(int i=0; i<sendTimesValue; i++){
			log.info(jobIndex + "time " +(i+1) + ":" + "begin send request.......");
			int count = 0;
			for(String pointKey:realRereadPoints.keySet()){
				List<RereadPoint> temp = realRereadPoints.get(pointKey);
				RereadPoint point;
				if(temp==null || temp.size()==0) continue;
				else  point = temp.get(0);
				if(!point.isSendFlag()){
					AnsiRequest ansiReq;
					if(RWLX_ZD.equals(rwlx))
						ansiReq = getAnsiRequest(point);
					else 
						ansiReq = this.createMasterTaskRequest(point);
					if(ansiReq == null) continue;
					count ++;
					if(count%intervalReqCntValue ==0){
						log.info(jobIndex + "time " +(i+1) + ":" + count + " records of request is sended...");
						try {
							Thread.sleep(intervalValue);
						} catch (InterruptedException e) {
							log.warn(jobIndex + "thread sleep error!", e);
						}
					}
					point.setSendFlag(com.sendRequest(null, null, ansiReq));
				}
			}
			log.info(jobIndex + "time " +(i+1) + ":" + count + " records of request is sended...");
			log.info(jobIndex + "time " +(i+1) + ":" + "end send request.......");
		}
		log.info(jobIndex + totalSize + " points finish send reread request...");
	}
	private AnsiRequest getAnsiRequest(RereadPoint point){
		try {
			AnsiRequest req =new AnsiRequest();
			req.setMeterId(point.getTerminalAddr());
			req.setProtocol(PROTOCOL);
			req.setOpType(ANSI_OP_TYPE.OP_READ);
			req.setTable(64);
			req.setServiceTag("30");
			req.setFull(true);
			Date pointTime = CalendarUtil.parse(point.getTimePoint()).getTime();
			AnsiDataItem[] datas=new AnsiDataItem[2];
			datas[0]=new AnsiDataItem();
			datas[0].date = pointTime;
			datas[1]=new AnsiDataItem();
			datas[1].date=pointTime;
			datas[1].dataCode="00005200";
			req.setDataItem(datas);
			req.addAppendParam("taskNo", point.getTaskNo());
			req.addAppendParam("startTime",pointTime);
			req.addAppendParam("endTime", pointTime);
			return req;
		} catch (Exception e) {
			log.error(jobIndex + point+" getAnsiRequest error!"  , e);
		};
		return null;
	}
	
	private AnsiRequest createMasterTaskRequest(RereadPoint point) {
		try {
			if("02".equals(point.getTaskType())){ //日冻结
				AnsiRequest req =new AnsiRequest();
				req.setMeterId(point.getCommAddr());
				req.setProtocol(PROTOCOL);
				req.setOpType(ANSI_OP_TYPE.OP_READ);
				req.setTable(26);
				req.setServiceTag("30");
				req.setFull(true);
				AnsiDataItem[] datas=new AnsiDataItem[1];
				datas[0]=new AnsiDataItem();
				Date pointTime = CalendarUtil.parse(point.getTimePoint()).getTime();
				datas[0].date=pointTime;
				req.setDataItem(datas);
				req.addAppendParam("taskNo", point.getTaskNo());
				req.addAppendParam("startTime", pointTime);
				req.addAppendParam("endTime", pointTime);
				return req;
			}else if("01".equals(point.getTaskType())){ //月冻结
				AnsiRequest req =new AnsiRequest();
				req.setMeterId(point.getCommAddr());
				req.setProtocol(PROTOCOL);
				req.setOpType(ANSI_OP_TYPE.OP_READ);
				req.setTable(2050);
				req.setServiceTag("30");
				req.setFull(true);
				AnsiDataItem[] datas=new AnsiDataItem[1];
				datas[0]=new AnsiDataItem();
				Date pointTime = CalendarUtil.parse(point.getTimePoint()).getTime();
				datas[0].date=pointTime;
				req.setDataItem(datas);
				req.addAppendParam("taskNo", point.getTaskNo());
				req.addAppendParam("startTime", pointTime);
				req.addAppendParam("endTime", pointTime);
				return req;
			}else{
				throw new RuntimeException(point + "The task type is not support!");
			}
		} catch (Exception e) {
			log.error(jobIndex + point + " createMasterTaskRequest error!"  , e);
		}
		return null;
	}
	
	public ClusterClientModule getCom() {
		return com;
	}
	public void setCom(ClusterClientModule com) {
		this.com = com;
	}
	public String getIranTime() {
		return iranTime;
	}
	public void setIranTime(String iranTime) {
		this.iranTime = iranTime;
	}
	public String getInterval() {
		return interval;
	}
	public void setInterval(String interval) {
		this.interval = interval;
	}
	public String getSendTimes() {
		return sendTimes;
	}
	public void setSendTimes(String sendTimes) {
		this.sendTimes = sendTimes;
	}
	public String getOldProtocols() {
		return oldProtocols;
	}
	public void setOldProtocols(String oldProtocols) {
		this.oldProtocols = (oldProtocols==null?"":oldProtocols);
	}
	public String getIntervalReqCnt() {
		return intervalReqCnt;
	}
	public void setIntervalReqCnt(String intervalReqCnt) {
		this.intervalReqCnt = intervalReqCnt;
	}
	
}
