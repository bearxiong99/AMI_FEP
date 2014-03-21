package cn.hexing.fk.bp.dlms.persisit;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import cn.hexing.db.batch.AsyncService;
import cn.hexing.db.managertu.ManageRtu;
import cn.hexing.fas.model.RtuData;
import cn.hexing.fas.model.RtuDataItem;
import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fk.bp.dlms.convert.DlmsChannelItemConvert;
import cn.hexing.fk.bp.dlms.protocol.DlmsScaleManager;
import cn.hexing.fk.bp.dlms.protocol.IDlmsScaleConvert;
import cn.hexing.fk.bp.dlms.util.DlmsConstant;
import cn.hexing.fk.bp.processor.TaskMessageHandler;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.model.DlmsItemRelated;
import cn.hexing.fk.model.DlmsMeterRtu;
import cn.hexing.fk.model.MeasuredPoint;
import cn.hexing.fk.model.RtuManage;
import cn.hexing.fk.model.RtuTask;
import cn.hexing.fk.model.TaskTemplate;
import cn.hexing.fk.utils.DateConvert;
import cn.hexing.fk.utils.StringUtil;

import com.hx.dlms.ASN1Type;
import com.hx.dlms.DlmsData;
import com.hx.dlms.DlmsDataType;
import com.hx.dlms.aa.DlmsContext;

/**
 * 任务轮招返回message处理
 * @author Administrator
 *
 */
public class TaskMessageService {
	
	private static final TaskMessageService instance = new TaskMessageService();
	private static final Logger log = Logger.getLogger(TaskMessageService.class);
	
	private AsyncService service;
	private JdbcDlmsDao itemRelatedDao;
	
	private Map<String,List<String>> blockRelatedSmall =null;
	
	/**
	 * 数据项与obis对应关系表  [obis,code]
	 */
	private static Map<String,String> dlmsItemRelated = new HashMap<String, String>();
	
	
	private TaskMessageService(){};
	
	public static TaskMessageService getInstance(){return instance;};
	
	public void init(){
		List<DlmsItemRelated> list = itemRelatedDao.loadDlmsItemRelated();
		for(DlmsItemRelated item : list){
			dlmsItemRelated.put( item.getCode(),item.getAttribute());
		}
		blockRelatedSmall=itemRelatedDao.getBlockRelatedSmall();
	}
	/**
	 * 任务消息转换
	 * @param req
	 * @return
	 * @throws IOException 
	 */
	public DlmsData taskMessageConvert(DlmsContext context,DlmsRequest req,DlmsObisItem item) throws IOException{
	
		DlmsData result = item.resultData;
		String logicAddress = req.getMeterId();
		String taskNum = getTaskNum(item.obisString);
		
		if (item.resultData.type()!=1 || item.resultData.getArray().length == 0) {
			log.info("task message no data,taskNum:"+taskNum+",meterId:"+logicAddress);
			return result;
		}
		
		if("ChannelReadSave".equals(req.getOperator())){
			DlmsObisItem[] params = req.getParams();
			List<String> codes = null;
			if(params.length==2){
				for(int i=0;i<2;i++){
					if(params[i].attributeId == 3){
						DlmsChannelItemConvert dcic = new DlmsChannelItemConvert();
						DlmsData itemData=dcic.upLinkConvert(req, params[i].resultData, params[i]);
						String itemCodes = itemData.getStringValue();
						codes = new ArrayList<String>();
						for(String code:itemCodes.split(",")){
							codes.add(code);
						}
						break;
					}
				}
			}
			if(codes!=null){
				return explainChannelData(req, item, result, logicAddress, taskNum,codes);
			}
		}
		
		TaskTemplate task = null;
		if(req.isRelay()){
			// 1.根据logicAddress获得BizRtu
			BizRtu bizRtu = RtuManage.getInstance().getBizRtuInCache(
					(int) Long.parseLong(logicAddress, 16));
			task = bizRtu.getTaskTemplate(taskNum);
			if(task==null){
				//在这里重新刷新模板
				Date refreshDate = bizRtu.getLastRefreshTime();
				boolean canRefresh = false;
				if(refreshDate==null){
					canRefresh = true;
				}else{
					canRefresh = canRefresh(refreshDate, canRefresh);
				}
				if(canRefresh){
					ManageRtu.getInstance().refreshBizRtu((int) Long.parseLong(logicAddress, 16));
					bizRtu = RtuManage.getInstance().getBizRtuInCache(
							(int) Long.parseLong(logicAddress, 16));
					Map<Integer, RtuTask> taskMap = bizRtu.getTasksMap();
					RtuTask rtuTask = taskMap.get(Integer.parseInt(taskNum));
					if(rtuTask!=null){
						ManageRtu.getInstance().refreshMasterTaskTemplate(rtuTask.getTaskTemplateID());
						task=bizRtu.getTaskTemplate(taskNum);
					}
				}
			}
		}else{
			//1.根据logicAddress获得meterRtu
			DlmsMeterRtu dlmsRtu = RtuManage.getInstance().getDlmsMeterRtu(logicAddress);
			if(dlmsRtu==null){
				throw new RuntimeException("Can't get DlmsRtu by LogicAddress:"+logicAddress);
			}
			task = dlmsRtu.getTaskTemplate(taskNum);
			if(task==null){
				//刷新模板
				Date refreshDate = dlmsRtu.getLastRefreshTime();
				boolean canRefresh = false;
				if(refreshDate==null){
					canRefresh = true;
				}else{
					canRefresh = canRefresh(refreshDate, canRefresh);
				}
				if(canRefresh){
					//先刷新终端，将所有的任务刷新
					dlmsRtu=ManageRtu.getInstance().refreshDlmsMeterRtu(logicAddress);
					Map<Integer, RtuTask> taskMap = dlmsRtu.getTasksMap();
					RtuTask rtuTask = taskMap.get(Integer.parseInt(taskNum));
					if(rtuTask!=null){
						ManageRtu.getInstance().refreshMasterTaskTemplate(rtuTask.getTaskTemplateID());
						task=dlmsRtu.getTaskTemplate(taskNum);
					}
					if(task ==null){
						log.error("task refreshed,but can't find tasktemplate. logicAddress:"+logicAddress+",taskNo is "+taskNum);
					}
				}else{
					log.error("taskTemp is null and now can't refresh. logicAddress:"+logicAddress+",taskNo is "+taskNum);
				}
			}
		}


		if(task == null ){
			log.warn("no this task template ,taskNum is "+taskNum+",meterId:"+logicAddress);
			return result;
		}
		List<String> codes = task.getDataCodes();
		return explainChannelData(req, item, result, logicAddress, taskNum,codes);
	}

	private DlmsData explainChannelData(DlmsRequest req, DlmsObisItem item,
			DlmsData result, String logicAddress, String taskNum,
			List<String> codes) throws IOException {
		DlmsData[] members = item.resultData.getArray();
		StringBuilder sb = new StringBuilder();
		for (DlmsData data : members) {
			
			if(data.getStructure()==null) continue;
			
			ASN1Type[] asn1Mems = data.getStructure().getMembers();
			if (codes.size() != (asn1Mems.length - 1)) {
				log.info("the back data can not match taskTemplate. TaskNum:"+taskNum+",meterId:"+logicAddress);
				return result;
			}

			for (int i = 0; i < asn1Mems.length; i++) {
				DlmsData mem = (DlmsData) asn1Mems[i];
				if (i == 0) {
					//这 里就是固定对时间的解析
					IDlmsScaleConvert cvt = DlmsScaleManager.getInstance()
							.getConvert("8.0.0.1.0.0.255.2");
					mem = cvt.upLinkConvert(req,mem,item);
					String time = mem.getStringValue();
					sb.append(time).append("#");
				} else {
					String code = codes.get(i - 1);
					String obis = dlmsItemRelated.get(code);
					String keyItem = "";
					if(obis==null || "".equals(obis)){
						keyItem=code.replaceAll("#", ".");
						log.error("Can't find obis from P_DLMSSJXGX , Code is "+code);
					}else{
						keyItem=(dlmsItemRelated.get(code)).replaceAll("#", ".");
					}
					IDlmsScaleConvert cvt = DlmsScaleManager.getInstance()
							.getConvert(keyItem);
					if(cvt!=null){
						mem = cvt.upLinkConvert(req,mem,item);
					}
					String strData = mem.getStringValue();
					sb.append(strData).append("#");
				}
			}
			sb.replace(sb.length()-1, sb.length(), ";");
		}
		sb.deleteCharAt(sb.length()-1);
		result.setVisiableString(sb.toString());
		return result;
	}

	private boolean canRefresh(Date refreshDate, boolean canRefresh) {
		Calendar calendar=Calendar.getInstance();
		calendar.setTime(refreshDate);
		String strTaskRefreshInterval=System.getProperty("bp.task.refreshInterval");
		try {
			calendar.add(Calendar.MINUTE, Integer.parseInt(strTaskRefreshInterval));
		} catch (NumberFormatException e) {
			calendar.add(Calendar.MINUTE, 15);
		}
		Calendar nowCalendar = Calendar.getInstance();
		nowCalendar.setTime(new Date());
		if(calendar.before(nowCalendar)){
			canRefresh=true;
		}
		return canRefresh;
	}
	/**
	 *  Dlmspolling to db
	 * @param context
	 * @param req
	 * @throws IOException
	 */
	public  void operationDataBase(DlmsContext context, DlmsRequest req) throws IOException{
		if(req.getOperator().equals("MasterTask")){
			operationMasterTask(context,req);
		}else{
			operationDlmsPolling(context,req);
		}

	}

	private void operationDlmsPolling(DlmsContext context, DlmsRequest req) {
		String logicAddress = req.getMeterId();
		//如果是中继,从BizRtu中获取
		BizRtu bizRtu = RtuManage.getInstance().getBizRtuInCache((int)Long.parseLong(logicAddress,16));
		//如果是普通的，从DlmsMeterRtu获得
		DlmsMeterRtu meterRtu = RtuManage.getInstance().getDlmsMeterRtu(logicAddress);
		
		
		TaskMessageHandler taskHandler = new TaskMessageHandler();
		DlmsObisItem[] items = req.getParams();
		for(int n=0;n<items.length;n++){
			DlmsObisItem item = items[n];
			if(item.attributeId!=2) continue;
			String taskNum = getTaskNum(item.obisString);
			TaskTemplate task = null;
			if(req.isRelay()){
				task = bizRtu.getTaskTemplate(taskNum);
			}else{
				task = meterRtu.getTaskTemplate(taskNum);
			}
			if(task==null){
				log.error("TaskTemplate is null,check database.taskNum:"+taskNum+",meterId:"+logicAddress);
				return;
			}
			List<String> codes=task.getDataCodes();  //获得对应的obis
			RtuData data = new RtuData();
			DlmsData resultData=item.resultData;
			if(resultData==null ||resultData.type()!=DlmsDataType.VISIABLE_STRING){
				//由于通道数据是经过DlmsChannelDataConvert获得的,所有从里面解析的value,都是VISIABLE_STRING
				//如果不是VISIABLE_STRING,说明没有值,或者其他原因导致.为了补招策略获得漏点,空数据也存.
				saveNullData(req, bizRtu, meterRtu, taskHandler, taskNum,codes, data);
				return;
			}
			String[] resMembers=resultData.getStringValue().split(";");

			for(String mem : resMembers){
				String time = "";
				String[] results=mem.split("#");
				for(int i=0;i<results.length;i++){
					if(i==0){//时间
						time = results[i];
					}else{//value
						String code = codes.get(i-1);
						RtuDataItem dataItem = new RtuDataItem();
						dataItem.setValue(results[i]);
						dataItem.setCode(code);
						data.addDataList(dataItem);
					}
				}
				SimpleDateFormat sdf =null;
				if(time.length() == 10){
					sdf= new SimpleDateFormat("yyyy-MM-dd");
				}else{
					sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				}
				
				//TODO: iran? convert(date):date
				time =  DlmsConstant.getInstance().isIranTime?time=DateConvert.iranToGregorian(time):time;
				
				Date date=null;
				try {
					date=sdf.parse(time);
				} catch (ParseException e) {
					log.error(StringUtil.getExceptionDetailInfo(e));
				}
				data.setTime(date);
				
				data.setTaskNum(taskNum);
				dataSave(req, bizRtu, meterRtu, taskHandler, taskNum, data);
			}
		}
	}
	
	/**
	 *  对于数据为空的数据,仍然保存
	 * @param req
	 * @param bizRtu
	 * @param meterRtu
	 * @param taskHandler
	 * @param taskNum
	 * @param codes
	 * @param data
	 */
	private void saveNullData(DlmsRequest req, BizRtu bizRtu,
			DlmsMeterRtu meterRtu, TaskMessageHandler taskHandler,
			String taskNum, List<String> codes, RtuData data) {
		for(String code:codes){
			RtuDataItem dataItem = new RtuDataItem();
			dataItem.setCode(code);
			data.addDataList(dataItem);
		}
		String strTaskDate=(String) req.getAppendParam("taskDate");
		
		if(strTaskDate==null) {
			log.warn("lost taskDate,so can't save null data");
			return;
		}
		data.setTime(strTaskDate);
		dataSave(req, bizRtu, meterRtu, taskHandler, taskNum, data);
	}

	private void dataSave(DlmsRequest req, BizRtu bizRtu,
			DlmsMeterRtu meterRtu, TaskMessageHandler taskHandler,
			String taskNum, RtuData data) {
		RtuTask rt = null;
		MeasuredPoint mp=null;
		String deptCode = "";
		if(req.isRelay()){//中继
			rt = bizRtu.getRtuTask(taskNum);
			data.setTn(""+req.getRelayParam().getMeasurePoint());
			mp = bizRtu.getMeasuredPoint(data.getTn());
			deptCode = bizRtu.getDeptCode();
		}else{
			rt = meterRtu.getRtuTask(taskNum);
			data.setTn("0");	
			deptCode = meterRtu.getDeptCode();
			mp = meterRtu.getMeasuredPoint(data.getTn());
		}
		int isReread = 0;
		if(req.getAppendParam("isReread") != null){
			isReread = 1;
		}

		taskHandler.dataSave(service, null, data, mp, null, rt, isReread, deptCode);
	}
	
	/**
	 * 操作主站任务
	 */
	public void operationMasterTask(DlmsContext context,DlmsRequest req){
		String logicAddress = req.getMeterId();
		String taskNo = (String) req.getAppendParam("taskNo");
		DlmsMeterRtu bizRtu = RtuManage.getInstance().getDlmsMeterRtu(logicAddress);
		TaskMessageHandler taskHandler = new TaskMessageHandler();
		DlmsObisItem[] params =req.getParams();
		TaskTemplate taskTemp = bizRtu.getTaskTemplate(taskNo);
		//获得当前任务的数据项(大项)
		if(taskTemp==null){
			//刷新模板
			Date refreshDate = bizRtu.getLastRefreshTime();
			boolean canRefresh = false;
			if(refreshDate==null){
				canRefresh = true;
			}else{
				canRefresh = canRefresh(refreshDate, canRefresh);
			}
			if(canRefresh){
				//先刷新终端，将所有的任务刷新
				bizRtu=ManageRtu.getInstance().refreshDlmsMeterRtu(logicAddress);
				Map<Integer, RtuTask> taskMap = bizRtu.getTasksMap();
				RtuTask rtuTask = taskMap.get(Integer.parseInt(taskNo));
				if(rtuTask!=null){
					ManageRtu.getInstance().refreshMasterTaskTemplate(rtuTask.getTaskTemplateID());
					taskTemp=bizRtu.getTaskTemplate(taskNo);
				}
				if(taskTemp ==null){
					log.error("task refreshed,but can't find tasktemplate. logicAddress:"+logicAddress+",taskNo is "+taskNo);
				}
			}else{
				log.error("taskTemp is null and now can't refresh. logicAddress:"+logicAddress+",taskNo is "+taskNo);
			}
		}
		List<String> codes = taskTemp.getDataCodes();
		for(int i=0;i<params.length;i++){
			DlmsData resultData=params[i].resultData;
			if(resultData==null ||resultData.type()==DlmsDataType.NULL||resultData.type()==DlmsDataType.ARRAY){
				//如果结果数据为NULL,或者结果为ARRAY,不进行存储,下面的代码全是以getStringValue(),开始,ARRAY不能由getStringValue()获得值
				log.info("Meter Task No Data,TaskNo is "+taskNo+",logicAddr:"+logicAddress+",obis is:"+params[i].classId+"#"+params[i].obisString+"#"+params[i].attributeId);
				continue;
			}
			//主站任务是以数据块的形式上送。
			String stringValues = params[i].resultData.getStringValue();
			//获得大项编码
			String bigCode = codes.get(i);
			//获得当前大项所有小项编码
			List<String> smallCodes=blockRelatedSmall.get(bigCode);
			if(smallCodes==null){
				RtuData data = new RtuData();
				RtuDataItem dataItem = new RtuDataItem();
				if(isDateTimeValue(stringValues)){
					stringValues =  DlmsConstant.getInstance().isIranTime?stringValues=DateConvert.iranToGregorian(stringValues):stringValues;
				}
				dataItem.setValue(stringValues);
				dataItem.setCode(bigCode);
				data.addDataList(dataItem);
				continue;
			}
			String[] values = stringValues.split(";");
			for (String stringValue : values) {
				RtuData data = new RtuData();
				String[] paramValues = stringValue.split("#");
				
				//模板和value可能不对应,找到最小的长度
				int saveSize = smallCodes.size()<paramValues.length?smallCodes.size():paramValues.length;
				boolean isTimeInTemplate = false;
				
				//use for template can't not match value. modify by gaoll 2013-5-6 9:40:48
				String time = "";
				for(int j=0;j<saveSize;j++){
					RtuDataItem dataItem = new RtuDataItem();
					if(j>= paramValues.length) break;
					String value = paramValues[j];
					if(j==0 && "8#0.0.1.0.0.255#2".equals(smallCodes.get(j))&& isDateTimeValue(value)){
						//如果在模板里，第一个配置的时间
						// iran time --> gregorian
						value = DlmsConstant.getInstance().isIranTime ? value = DateConvert
								.iranToGregorian(value) : value;
						time = value;
						isTimeInTemplate = true;
					}else if(isDateTimeValue(value)){
						value = DlmsConstant.getInstance().isIranTime ? value = DateConvert
								.iranToGregorian(value) : value;
						if (j == 0){
							time = value;
							saveSize = saveSize+1;
						} 
					}
					//two mode .
					if(isTimeInTemplate){
						//mode 1: template contains time
						//value match code
						dataItem.setValue(value);
						dataItem.setCode(smallCodes.get(j));
						data.addDataList(dataItem);
					}else{
						//mode 2: template uncontains time
						//value match code-1
						//first loop continue
						if(j!=0){
							dataItem.setValue(value);
							dataItem.setCode(smallCodes.get(j-1));
							data.addDataList(dataItem);
						}
					}
				}
				data.setTime(time);
				dataSave(req, null, bizRtu, taskHandler, taskNo, data);
			}
		}
	}
	
	/**
	 * 是否是日期-时间格式
	 * @param value
	 * @return
	 */
	public boolean isDateTimeValue(String value){
		String regex = "[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}|[0-9]{4}-[0-9]{2}-[0-9]{2}";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(value);
		return m.matches();
	}
	
	private String getTaskNum(String obisString) {
		obisString=obisString.replace(".", ":");
		String[] strs = obisString.split(":");
		return strs[1];
	}

	public AsyncService getService() {
		return service;
	}

	public void setService(AsyncService service) {
		this.service = service;
	}

	public JdbcDlmsDao getItemRelatedDao() {
		return itemRelatedDao;
	}

	public void setItemRelatedDao(JdbcDlmsDao itemRelatedDao) {
		this.itemRelatedDao = itemRelatedDao;
	}


	
}
