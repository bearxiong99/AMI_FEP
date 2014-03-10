package cn.hexing.reread.bpserver.parent;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import cn.hexing.reread.bpserver.timeSyn.TimeSynReadProcessor;
import cn.hexing.reread.model.MasterReread;
import cn.hexing.reread.model.RereadPoint;
import cn.hexing.reread.service.LoadDatasService;
import cn.hexing.reread.utils.TimePointUtils;

public class MasterRereadTaskProcessor implements Job{
	private static final Logger log = Logger.getLogger(TimeSynReadProcessor.class);
	public static String PARAMNAME_MASTERREREAD = "masterReread";
	public static String PARAMNAME_SERVICE = "service";
	public static String PARAMNAME_READER = "reader";
	public static String PARAMNAME_DLMSITEMRELATED = "dlmsItemRelated";
	
	public void execute(JobExecutionContext context) throws JobExecutionException {
		Date now = new Date();
		String jobIndex = "RereadJob" + UUID.randomUUID().toString() + " ";
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		log.info(jobIndex+" start at " + df.format(now) + "****************************");
		JobDataMap data = context.getJobDetail().getJobDataMap(); 
		MasterReread masterReread = (MasterReread)data.get(PARAMNAME_MASTERREREAD);
		LoadDatasService service = (LoadDatasService)data.get(PARAMNAME_SERVICE);
		Rereader reader = (Rereader)data.get(PARAMNAME_READER);
		@SuppressWarnings("unchecked")
		Map<String,String> dlmsItemRelated = (Map<String,String>)data.get(PARAMNAME_DLMSITEMRELATED);
		try{
			log.info(jobIndex+"begin excute master reread:" + masterReread + ".........");
			List<Date> timePoints = TimePointUtils.createTimePointsByRange(masterReread.getSampleInterval(), masterReread.getSampleIntervalUnit(), 
					masterReread.getStartTime(), masterReread.getEndTime());
			List<String> strTimePoints = new ArrayList<String>();
			for(int i=0; i<timePoints.size(); i++){
				strTimePoints.add(df.format(timePoints.get(i)));
			}
			if(log.isDebugEnabled()){
				log.debug(jobIndex+"strTimePoints=[" + strTimePoints +"]");
			}
			List<RereadPoint> rereadPoints = service.getRereadPointByTimeDwdm(masterReread.getTaskTemplateID(), strTimePoints.toArray(),masterReread.getDwdm(),masterReread.getRwlx()
					,TimePointUtils.transIntervalToMinute(masterReread.getSampleInterval(), masterReread.getSampleIntervalUnit()));
			if(log.isDebugEnabled()){
				log.debug(jobIndex + "return "+rereadPoints.size() + " reread points[" + rereadPoints +"]");
			}
			if(rereadPoints.size()>0){
				//连接BP发送漏点
				log.info(jobIndex+"Master reread job begin call BP...");
				reader.read(rereadPoints , masterReread.getRwlx(), dlmsItemRelated,masterReread.getSampleIntervalUnit(),masterReread.getSampleInterval()
						,jobIndex);
				log.info("Master reread job end at " + df.format(new Date()));
			}
		}catch (Exception e) {
			log.error(jobIndex+"Master reread job excute failed!" ,e);
			service.setMasterRereadSuccess(masterReread.getTaskTemplateID(), masterReread.getCreateTime(), "2" ,masterReread.getRwlx());//state:1-成功， 2-失败
		}
	}
}
