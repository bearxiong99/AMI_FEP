package cn.hexing.reread.bpserver.dlms;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fas.model.dlms.DlmsRequest.DLMS_OP_TYPE;
import cn.hexing.fk.clientmod.ClusterClientModule;
import cn.hexing.fk.model.DlmsMeterRtu;
import cn.hexing.fk.model.RtuManage;
import cn.hexing.fk.model.TaskTemplate;
import cn.hexing.fk.utils.DateConvert;
import cn.hexing.reread.bpserver.parent.Rereader;
import cn.hexing.reread.model.RereadPoint;
import cn.hexing.util.CalendarUtil;
import cn.hexing.util.HexDump;

import com.hx.dlms.ASN1SequenceOf;
import com.hx.dlms.DlmsData;
/**
 * 补召消息发送——BP客户端
 * @ClassName:DlmsRereader
 * @Description:TODO
 * @author kexl
 * @date 2012-9-24 上午10:31:52
 *
 */
public class DlmsRereader implements Rereader{
	private static final Logger log = Logger.getLogger(DlmsRereader.class);
	public static final String PROTOCOL = "03";
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
	
	private static java.text.DecimalFormat monthDf = new java.text.DecimalFormat("00");
	
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
		if(points!=null) log.info(jobIndex + points.size() + " points be queryed from database.");
		if(points!=null && points.size()>0){
			boolean isCbr = "01".equals(points.get(0).getTaskType());
			for(RereadPoint point : points){
				String key = point.getTerminalAddr()+"_" + point.getCldh()+"_"+point.getTaskNo()+"_";
				String timePoint = point.getTimePoint();
				/**kexl 临时增加，在么有排查出原因的情况下限制大于当前时间的漏点不去补召**/
				if(CalendarUtil.parse(timePoint).compareTo(Calendar.getInstance()) > 0){
					log.warn(jobIndex + "***************异常的漏点："+point);
					continue; 
				}
				/**end**/
				if(isCbr){ //如果是抄表日任务，则按照终端逻辑地址、测量点号、任务号、数据时间_月份进行归总，重复的去掉
					timePoint = isIranTime?DateConvert.gregorianToIran(timePoint):timePoint;
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
					DlmsRequest dlmsReq;
					if(RWLX_ZD.equals(rwlx))
						dlmsReq = getDlmsRequest(point);
					else 
						dlmsReq = this.createMasterTaskRequest(point , dlmsItemRelated);
					if(dlmsReq == null) continue;
					count ++;
					//设置补招标记，BP保存补齐标记为1
					dlmsReq.addAppendParam("isReread", true);
					
					if(count%intervalReqCntValue ==0){
						log.info(jobIndex + "time " +(i+1) + ":" + count + " records of request is sended...");
						try {
							Thread.sleep(intervalValue);
						} catch (InterruptedException e) {
							log.warn(jobIndex + "thread sleep error!", e);
						}
					}
					point.setSendFlag(com.sendRequest(null, null, dlmsReq));
				}
			}
			log.info(jobIndex + "time " +(i+1) + ":" + count + " records of request is sended...");
			log.info(jobIndex + "time " +(i+1) + ":" + "end send request.......");
		}
		log.info(jobIndex + totalSize + " points finish send reread request...");
	}
	private DlmsRequest getDlmsRequest(RereadPoint point){
		try {
			String timePoint = point.getTimePoint();
			String taskNo = point.getTaskNo();
			String commAddr = point.getCommAddr();
			DlmsMeterRtu meterRtu = RtuManage.getInstance().getDlmsMeterRtu(point.getTerminalAddr());
			
			DlmsObisItem[] params = new DlmsObisItem[]{new DlmsObisItem()};
			params[0].attributeId= 2;
			params[0].classId = 7;
			params[0].obisString="0."+ taskNo +".24.3.0.255";//设置OBIS{taskNo}
			
			//通道数据请求，结束时间要偏移1分钟，应对于表计存储数据秒值不是00
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date dTimePoint = sdf.parse(timePoint);
			Calendar cTimePoint = Calendar.getInstance();
			cTimePoint.setTime(dTimePoint);
			cTimePoint.add(Calendar.MINUTE, 1);
			String endTime = sdf.format(cTimePoint.getTime());
			
			
			String readTimePoint = isIranTime?DateConvert.gregorianToIran(timePoint):timePoint;
			String readTimePointEnd  = isIranTime?DateConvert.gregorianToIran(endTime):endTime;
			
			//抄表日任务，则传时间段
			if("01".equals(point.getTaskType())){
				Map<String,String> readTimeRange= getMonthRangeDayForGero(timePoint,isIranTime);
				readTimePoint = readTimeRange.get("startTime");
				readTimePointEnd = readTimeRange.get("endTime");
			}
			DlmsRequest dlmsRequest;
			dlmsRequest = createRequestWithTime(meterRtu, params, readTimePoint, readTimePointEnd);
			dlmsRequest.setProtocol(PROTOCOL);
			dlmsRequest.setMeterId(commAddr);//设置表计通讯地址{commAddr}
			dlmsRequest.setOpType(DlmsRequest.DLMS_OP_TYPE.OP_GET);
			dlmsRequest.setOperator(OPERATOR_ZD);
			
			//传时间点，用于BP在终端返回空数据的时候向上报数据表插入一条空数据
			//dlmsRequest.addAppendParam("taskNo", taskNo);
			dlmsRequest.addAppendParam("taskDate", timePoint);
			if(log.isDebugEnabled())
				log.debug(jobIndex + "create dlms reread request, "+point);
			return dlmsRequest;
		} catch (Exception e) {
			log.error(jobIndex + point+" getDlmsRequest error!"  , e);
		};
		return null;
	}
	
	private DlmsRequest createMasterTaskRequest(RereadPoint point , Map<String,String> dlmsItemRelated) {
		try {
			DlmsMeterRtu meterRtu = RtuManage.getInstance().getDlmsMeterRtu(point.getTerminalAddr());
			TaskTemplate taskTemplate = meterRtu.getTaskTemplate(point.getTaskNo());
			if(taskTemplate == null){
				if(log.isDebugEnabled())log.debug(jobIndex + meterRtu.getTasksMap());
			}
			List<String> dataCodes=taskTemplate.getDataCodes();
			int size=dataCodes.size();
			DlmsObisItem[] params = new DlmsObisItem[size];
			for(int i=0;i<size;i++){
				String code = dataCodes.get(i);
				String itemId=dlmsItemRelated.get(code);
				String[] attrDescs=itemId.split("#");
				params[i] = new DlmsObisItem();
				params[i].classId = Integer.parseInt(attrDescs[0]);
				params[i].attributeId = Integer.parseInt(attrDescs[2]);
				params[i].obisString = attrDescs[1];
			}
			
			String timePoint = isIranTime?DateConvert.gregorianToIran(point.getTimePoint()):point.getTimePoint();
			String timePointEnd = timePoint;
			if(timePointEnd.length()==19) timePointEnd = timePointEnd.substring(0, 17)+"59";//日冻结的数据时标在某些预付费表上会出现多处几秒的问题，所以截止时间+59秒
			//抄表日任务，则传时间段
			if("01".equals(point.getTaskType())){
				Map<String,String> readTimeRange= getMonthRangeDayForGero(point.getTimePoint(),isIranTime);
				timePoint = readTimeRange.get("startTime");
				timePointEnd = readTimeRange.get("endTime");
			}
			DlmsRequest dr;
			dr = createRequestWithTime(meterRtu, params, timePoint, timePointEnd);
			dr.setProtocol(PROTOCOL);
			dr.setMeterId(point.getTerminalAddr());
			dr.setOpType(DLMS_OP_TYPE.OP_GET);
			dr.setOperator(OPERATOR_ZZ);
			
			//传时间点，用于BP在终端返回空数据的时候向上报数据表插入一条空数据
			dr.addAppendParam("taskDate", point.getTimePoint());
			dr.addAppendParam("taskNo", point.getTaskNo());
			if(log.isDebugEnabled())log.debug(jobIndex + "create master taskRequest,"+point);
			return dr;
		} catch (Exception e) {
			log.error(jobIndex + point + " createMasterTaskRequest error!"  , e);
		}
		return null;
	}
	
	/**
	 * 创建一个带时间读的对象
	 * @return
	 * @throws Exception 
	 */
	private DlmsRequest createRequestWithTime(DlmsMeterRtu meterRtu,DlmsObisItem[] params,String startTime,String endTime) throws Exception{

		DlmsRequest dlmsRequest = null;
		try {
			for(int i=0;i<params.length;i++){
				DlmsData[] array = new DlmsData[]{new DlmsData(),new DlmsData(),new DlmsData(),new DlmsData()};
				DlmsData[] array2 = new DlmsData[]{new DlmsData(),new DlmsData(),new DlmsData(),new DlmsData()};
				array2[0].setUnsignedLong(8);
				array2[1].setOctetString(HexDump.toArray("0000010000FF"));
				array2[2].setDlmsInteger((byte) 2);
				array2[3].setUnsignedLong(0);
				ASN1SequenceOf struct1 = new ASN1SequenceOf(array2);
				array[0].setStructure(struct1);
				dlmsRequest = new DlmsRequest();
				boolean isEncodeLength = true;
				if(meterRtu.getSubProtocol()==null ||meterRtu.getSubProtocol().equals("0")|| oldProtocols.indexOf(meterRtu.getSubProtocol())<0){
					array[1].setDlmsDateTime(startTime);
					array[2].setDlmsDateTime(endTime);
					DlmsData[] dd = null;
					array[3].setArray(dd);
				}else{
					array[1].setOldDlmsDateTime(startTime);
					array[2].setOldDlmsDateTime(endTime);
					dlmsRequest.setSubprotocol(meterRtu.getSubProtocol());
					isEncodeLength = false;
				}
				ASN1SequenceOf struct = new ASN1SequenceOf(array);
				struct.isEncodeLength = isEncodeLength;
				params[i].data.setStructure(struct);
				params[i].accessSelector = 1;
			}
			dlmsRequest.setParams(params);
		} catch (Exception ex) {
			throw ex;
		}
		return dlmsRequest;
	}
	/**
	 * 根据公历时间，取月份的起止时间
	 * @param geroTime
	 * @param isIran
	 * @return
	 */
	public static Map<String , String> getMonthRangeDayForGero(String geroTime , boolean isIran){
		Map<String , String> map = new HashMap<String , String>();
		String time = isIran?DateConvert.gregorianToIran(geroTime):geroTime;
		int sYear = Integer.parseInt(time.substring(0,4));
		int sMonth = Integer.parseInt(time.substring(5,7));
		String nextYear = "" + ((sMonth+1>12)?(sYear+1):sYear);
		String nextMonth = monthDf.format((sMonth+1>12)?1:(sMonth+1));
		map.put("startTime", sYear + "-" + monthDf.format(sMonth) + "-01 00:00:00");
		map.put("endTime", nextYear+"-"+nextMonth+"-01 00:00:00");
		return map;
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
