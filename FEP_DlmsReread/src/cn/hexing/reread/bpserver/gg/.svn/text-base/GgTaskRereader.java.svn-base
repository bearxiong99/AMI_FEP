package cn.hexing.reread.bpserver.gg;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import cn.hexing.fas.model.FaalGGKZM12Request;
import cn.hexing.fas.model.FaalRequestRtuParam;
import cn.hexing.fk.clientmod.ClusterClientModule;
import cn.hexing.reread.bpserver.parent.Rereader;
import cn.hexing.reread.model.RereadPoint;

/**
 * 
 * @author gaoll
 *
 * @time 2012-11-2 上午10:31:05
 *
 * @info 广州规约任务补招  根据获得的漏点，创建Request对象，发送给BP
 */
public class GgTaskRereader implements Rereader{
	private static final Logger log = Logger.getLogger(GgTaskRereader.class);
	public static final String PROTOCOL = "04";
	public static final String OPERATOR = "GgPolling";
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
	
	private String jobIndex;
	public void read(List<RereadPoint> points, String rwlx , Map<String,String> dlmsItemRelated,String sampleIntervalUnit,int sampleInterva
			,String jobIndex){
		this.jobIndex = jobIndex;
		
		if(interval!=null) try{intervalValue = Integer.parseInt(interval);}catch(Exception e){log.warn(e); };//使用配置的参数
		if(intervalReqCnt!=null) try{intervalReqCntValue = Integer.parseInt(intervalReqCnt);}catch(Exception e){log.warn(e); };//使用配置的参数
		if(sendTimes!=null) try{sendTimesValue = Integer.parseInt(sendTimes);}catch(Exception e){log.warn(e); }; //使用配置的参数
		if(sendTimesValue<1) sendTimesValue = 1;//最小一次
		log.info(jobIndex + "intervalValue="+intervalValue+",sendTimesValue="+sendTimesValue
				+",intervalReqCntValue="+intervalReqCntValue+"。Beging call BP... ");
		log.info(jobIndex + points.size() + " points begin reread...");
		for(int i=0; i<sendTimesValue; i++){
			log.info(jobIndex + "time " +(i+1) + ":" + "begin send request.......");
			int count = 0;
			for(RereadPoint point:points){
				if(!point.isSendFlag()){
					FaalGGKZM12Request request =new FaalGGKZM12Request();  //创建对应的Request
					request = getGgRequest(point.getDataItemId(),point.getTimePoint(),point.getTaskNo(),point.getTerminalAddr());
					count ++;
					if(count%intervalReqCntValue ==0){
						log.info(jobIndex + "time " +(i+1) + ":" + count + " records of request is sended...");
						try {
							Thread.sleep(intervalValue);
						} catch (InterruptedException e) {
							log.warn(jobIndex + "thread sleep error!", e);
						}
					}
					point.setSendFlag(com.sendRequest(null, null, request));
				}
			}
			log.info(jobIndex + "time " +(i+1) + ":" + count + " records of request is sended...");
			log.info(jobIndex + "time " +(i+1) + ":" + "end send request.......");
		}
		log.info(jobIndex + points.size() + " points finish send reread request...");
	}
	
	private FaalGGKZM12Request getGgRequest( String dataItemId ,String timePoint,String taskNo,String terminalAddr){
		if(log.isDebugEnabled())log.debug(jobIndex + "create gg reread request. time:"+timePoint+",dataItemId:"+dataItemId);
		FaalGGKZM12Request request =new FaalGGKZM12Request();  //创建对应的Request
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try{
			request.setProtocol(PROTOCOL);
			request.setTaskNo(taskNo);
			request.setStartTime(sdf.parse(timePoint));
			request.setEndTime(getDateAfterNow(sdf.parse(timePoint)));
			FaalRequestRtuParam rtuparam =new FaalRequestRtuParam();
			rtuparam.setCmdId((long)40754);
			rtuparam.setRtuId(terminalAddr);
			rtuparam.addParam(dataItemId, null);
			request.addRtuParam(rtuparam);
//			request.setYhlx("");  //用户类型暂时不写进来
			request.setType(18); // 0X12=18
		}catch(Exception ex){
			log.error(jobIndex + "create dlms reread request error!", ex);
		}
		return request;
	}
	public ClusterClientModule getCom() {
		return com;
	}
	public void setCom(ClusterClientModule com) {
		this.com = com;
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
	
	public Date getDateAfterNow(Date date){
		Calendar calendar = Calendar.getInstance(); //得到日历
		calendar.setTime(date);//把当前时间赋给日历
		calendar.add(Calendar.MINUTE, +1);  //设置为当前时间之后一分钟
		return  calendar.getTime();   //得到前一天的时间
	}
	
}
