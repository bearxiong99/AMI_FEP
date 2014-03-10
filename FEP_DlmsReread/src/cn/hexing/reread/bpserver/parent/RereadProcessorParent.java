package cn.hexing.reread.bpserver.parent;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

import cn.hexing.reread.model.RereadPoint;
import cn.hexing.reread.model.RereadStrategy;
import cn.hexing.reread.service.LoadDatasServiceParent;
import cn.hexing.reread.utils.TimePointUtils;

public class RereadProcessorParent  implements StatefulJob {
	private static final Logger log = Logger.getLogger(RereadProcessorParent.class);

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		String jobIndex = "RereadJob" + UUID.randomUUID().toString() + " ";
		try{
			Date now = new Date();
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			log.info(jobIndex +" start at " + df.format(now)  + "****************************");
			
			JobDataMap data = context.getJobDetail().getJobDataMap(); 
			RereadStrategy strategy = (RereadStrategy)data.get(RereadMainParent.PARAMNAME_STRATEGY);
			log.info(jobIndex+" RereadStrategy:" + strategy);
			//数据库服务
			LoadDatasServiceParent service = (LoadDatasServiceParent)data.get(RereadMainParent.PARAMNAME_SERVICE);
			//补召
			Rereader reader = (Rereader)data.get(RereadMainParent.PARAMNAME_READER);
			//
			@SuppressWarnings("unchecked")
			Map<String,String> dlmsItemRelated = (Map<String,String>)data.get(RereadMainParent.PARAMNAME_DLMSITEMRELATED);
			
			List<Date> timePoints = TimePointUtils.createTimePointsByRange(strategy.getSampleInterval(), strategy.getSampleIntervalUnit(), 
					now, -strategy.getDataBegin(), -strategy.getDataEnd());
			List<String> strTimePoints = new ArrayList<String>();
			for(int i=0; i<timePoints.size(); i++){
				strTimePoints.add(df.format(timePoints.get(i)));
			}
			if(log.isDebugEnabled()){
				log.debug(jobIndex + "strTimePoints=[" + strTimePoints +"]");
			}
			List<RereadPoint> rereadPoints = service.getRereadPointByTime(strategy.getTaskTemplateID(), strTimePoints.toArray(),strategy.getRwlx()
					,TimePointUtils.transIntervalToMinute(strategy.getSampleInterval(), strategy.getSampleIntervalUnit()));
			if(log.isDebugEnabled()){
				log.debug(jobIndex + "return "+rereadPoints.size() + " reread points[" + rereadPoints +"]");
			}
			//连接BP发送漏点
			log.info(jobIndex+" begin call BP...");
			reader.read(rereadPoints , strategy.getRwlx() , dlmsItemRelated,strategy.getSampleIntervalUnit(), strategy.getSampleInterval() 
					, jobIndex); //rwlx:01-终端任务，02-主站任务
			log.info(jobIndex+" end at " + df.format(new Date()));
		}catch (Exception e) {
			log.error(jobIndex , e);
		}
	}
}
