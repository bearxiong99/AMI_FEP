package cn.hexing.reread.bpserver.gw;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

import cn.hexing.fas.model.FaalGWAFN0DRequest;
import cn.hexing.fas.model.FaalRequestParam;
import cn.hexing.fas.model.FaalRequestRtuParam;
import cn.hexing.fk.clientmod.ClusterClientModule;
import cn.hexing.reread.bpserver.parent.Rereader;
import cn.hexing.reread.model.RereadPoint;
import cn.hexing.reread.utils.IntervalUnit;
import cn.hexing.util.CalendarUtil;
/**
 * 补召消息发送——BP客户端
 * @ClassName:DlmsRereader
 * @Description:TODO
 * @author kexl
 * @date 2012-9-24 上午10:31:52
 *
 */
public class GwTaskRereader implements Rereader{
	private static final Logger log = Logger.getLogger(GwTaskRereader.class);
	public static final String PROTOCOL = "02";
	public static final String OPERATOR_ZD = "gwldbz"; 
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
	
	private static DateFormat dfFull = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static DateFormat dfDay = new SimpleDateFormat("yyyy-MM-dd");
	private static DateFormat dfMonth = new SimpleDateFormat("yyyy-MM");
	
	private String jobIndex;
	public void read(List<RereadPoint> points , String rwlx , Map<String,String> dlmsItemRelated,String sampleIntervalUnit,int sampleInterva
			,String jobIndex){
		this.jobIndex = jobIndex;
		if("true".equals(iranTime))isIranTime = true;//使用配置的参数
		if(interval!=null) try{intervalValue = Integer.parseInt(interval);}catch(Exception e){log.warn(e); };//使用配置的参数
		if(intervalReqCnt!=null) try{intervalReqCntValue = Integer.parseInt(intervalReqCnt);}catch(Exception e){log.warn(e); };//使用配置的参数
		if(sendTimes!=null) try{sendTimesValue = Integer.parseInt(sendTimes);}catch(Exception e){log.warn(e); }; //使用配置的参数
		if(sendTimesValue<1) sendTimesValue = 1;//最小一次
		log.info(jobIndex + "isIranTime="+isIranTime+",intervalValue="+intervalValue+",sendTimesValue="+sendTimesValue
				+",intervalReqCntValue="+intervalReqCntValue+"。Beging call BP... ");
		//对漏点（终端逻辑地址、测量点号、任务号、数据时间）进行归总，重复的去掉
		Map<String,List<RereadPoint>> realRereadPoints = new HashMap<String , List<RereadPoint>>();
		if(points!=null && points.size()>0){
			boolean isCbr = "01".equals(points.get(0).getTaskType());
			for(RereadPoint point : points){
				String key = point.getTerminalAddr()+"_" + point.getCldh()+"_"+point.getTaskNo()+"_";
				String timePoint = point.getTimePoint();
				if(isCbr){ //如果是抄表日任务，则按照终端逻辑地址、测量点号、任务号、数据时间_月份进行归总，重复的去掉
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
		
		log.info(jobIndex + "isIranTime="+isIranTime+",intervalValue="+intervalValue+",sendTimesValue="+sendTimesValue+"。Beging call BP... ");
		int totalSize = realRereadPoints.keySet().size();
		log.info(jobIndex + totalSize + " points begin reread...");
		int filterSize = 0;
		String today = dfDay.format(new Date());
		String currMonth = dfMonth.format(new Date());
		for(int i=0; i<sendTimesValue; i++){
			log.info(jobIndex + "time " +(i+1) + ":" + "begin send request.......");
			int count = 0;
			for(String key:realRereadPoints.keySet()){
				RereadPoint point = realRereadPoints.get(key).get(0);
				if(!point.isSendFlag()){
					String taskType = point.getTaskType(); //01-抄表日，02-日冻结
					String time = point.getTimePoint();
					if("02".equals(taskType)){
						//国网日冻结数据，召测传的时标需要在数据时标上减去1天
						try{
							time = dfFull.format(DateUtils.addDays(CalendarUtil.parse(point.getTimePoint()).getTime(), -1));
						}catch(Exception e){
							if(today.equals(time.substring(0,10))){
								point.setSendFlag(true);
								filterSize++;
								continue;
							}
						}
					}else if("01".equals(taskType)){ 
						//国网抄表日冻结数据在下一月才生成，所以要过滤掉漏点中与当月的漏点
						try{
							time = dfFull.format(DateUtils.addMonths(CalendarUtil.parse(point.getTimePoint()).getTime(), -1));
						}catch(Exception e){
							if(currMonth.equals(time.substring(0,7))){
								point.setSendFlag(true);
								filterSize++;
								continue;
							}
						}
					}
					HashSet<String> dataItems = new HashSet<String>();
					for(int tempI=0;tempI<realRereadPoints.get(key).size();tempI++){
						dataItems.add(realRereadPoints.get(key).get(tempI).getDataItemId());
					}
					String[] dataItemsArray = new String[dataItems.size()];
					FaalGWAFN0DRequest req = getGwRequest(point.getCommAddr(),point.getTaskNo(), time
							, point.getTerminalAddr() , point.getCldh() , dataItems.toArray(dataItemsArray)
							,sampleInterva ,sampleIntervalUnit);
					count ++;
					if(count%intervalReqCntValue ==0){
						log.info(jobIndex + "time " +(i+1) + ":" + count + " records of request is sended...");
						try {
							Thread.sleep(intervalValue);
						} catch (InterruptedException e) {
							log.warn(jobIndex + "thread sleep error!", e);
						}
					}
					point.setSendFlag(com.sendRequest(null, null, req));
				}
			}
			log.info(jobIndex + "time " +(i+1) + ":" + count + " records of request is sended...");
			log.info(jobIndex + "time " +(i+1) + ":" + "end send request.......");
		}
		log.info(jobIndex + (totalSize-filterSize) + " points finish send reread request...");
	}
	private FaalGWAFN0DRequest getGwRequest(String commAddr , String taskNo , String timePoint ,String terminalAddr , int cldh , String[] dataItems
			,int interval ,String intervalUnit){
		FaalGWAFN0DRequest req = new FaalGWAFN0DRequest();
		req.setCount(1);
		if(intervalUnit.equals(IntervalUnit.MUNITE.value())){
			req.setInterval(interval);
		}
		if(intervalUnit.equals(IntervalUnit.HOUR.value())){
			req.setInterval(interval*60);
		}
		req.setStartTime(timePoint);
		req.setProtocol(PROTOCOL);
		req.setType(0x0D);
		req.setOperator("gwldbz");
		req.setTxfs(2);
		
		FaalRequestRtuParam p = new FaalRequestRtuParam();
		p.setCmdId((long)0);
		p.setRtuId(terminalAddr);
		p.setTn(new int[]{cldh});
		List<FaalRequestParam> params = new ArrayList<FaalRequestParam>();
		for(String dataItem:dataItems){
			params.add(new FaalRequestParam(dataItem,""));
		}
		p.setParams(params);
		req.addRtuParam(p);

		return req;
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
	public String getIntervalReqCnt() {
		return intervalReqCnt;
	}
	public void setIntervalReqCnt(String intervalReqCnt) {
		this.intervalReqCnt = intervalReqCnt;
	}
	
}
