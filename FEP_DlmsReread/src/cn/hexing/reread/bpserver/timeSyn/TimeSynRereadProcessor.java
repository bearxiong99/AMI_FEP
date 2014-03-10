package cn.hexing.reread.bpserver.timeSyn;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import cn.hexing.reread.model.ReadTimeModel;
import cn.hexing.reread.service.TimeSynService;
/**
 * 补召终端时间（处理补召对时任务，表TJ_ZDDSSJ已初始化，但是没有召测成功的记录）
 * @ClassName:DlmsRereadProcessor
 * @Description:TODO
 * @author kexl
 * @date 2012-9-24 上午10:30:37
 *
 */
public class TimeSynRereadProcessor implements Job {
	private static final Logger log = Logger.getLogger(TimeSynRereadProcessor.class);
	public static String PARAMNAME_DWDM = "dwdm";
	public static String PARAMNAME_SERVICE = "service";
	public static String PARAMNAME_READER = "reader";
	public void execute(JobExecutionContext context) throws JobExecutionException {
		Date now = new Date();
		String jobIndex = "Job" + now.getTime();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String rwzxsj = df.format(now);
		log.info(jobIndex+" start at " + rwzxsj);
		
		JobDataMap data = context.getJobDetail().getJobDataMap();
		String dwdm = (String)data.get(PARAMNAME_DWDM);
		log.info(jobIndex+" dwdm=" + dwdm);
		TimeSynService service = (TimeSynService)data.get(PARAMNAME_SERVICE);//数据库服务
		TimeSynReader reader = (TimeSynReader)data.get(PARAMNAME_READER);//调用BP
		List<ReadTimeModel> models = service.getRereadTimeModel(dwdm);
		if(models.size()>0){
			//连接BP发送漏点
			log.info(jobIndex+" begin call BP...");
			reader.read(models , dwdm , rwzxsj , "1");
			log.info(jobIndex+" end at " + df.format(new Date()));
		}else{
			log.info(jobIndex+"has no records need to call BP.End Job at " + df.format(new Date()));
		}
	}
	
}
