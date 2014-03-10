package cn.hexing.reread.bpserver.gw;

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

import cn.hexing.reread.bpserver.parent.RereadMainParent;
import cn.hexing.reread.bpserver.parent.RereadProcessorParent;
import cn.hexing.reread.model.RereadPoint;
import cn.hexing.reread.model.RereadStrategy;
import cn.hexing.reread.service.LoadGwDatasService;
import cn.hexing.reread.utils.TimePointUtils;

/**
 * @author gaoll
 *
 * @time 2012-11-3 下午1:49:46
 *
 * @info 广规补招处理器
 */
public class GwRereadProcessor  extends RereadProcessorParent {
	
}