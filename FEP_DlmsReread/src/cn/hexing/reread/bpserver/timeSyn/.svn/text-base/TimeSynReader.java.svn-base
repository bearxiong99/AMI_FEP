package cn.hexing.reread.bpserver.timeSyn;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import cn.hexing.fas.model.AnsiRequest;
import cn.hexing.fas.model.AnsiRequest.ANSI_OP_TYPE;
import cn.hexing.fas.model.FaalGGKZM11Request;
import cn.hexing.fas.model.FaalGGKZM14Request;
import cn.hexing.fas.model.FaalGWNoParamRequest;
import cn.hexing.fas.model.FaalReadCurrentDataRequest;
import cn.hexing.fas.model.FaalRequest;
import cn.hexing.fas.model.FaalRequestParam;
import cn.hexing.fas.model.FaalRequestRtuParam;
import cn.hexing.fas.model.FaalWriteParamsRequest;
import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fas.model.dlms.DlmsRequest.BUSINESS_CATEGORY;
import cn.hexing.fas.model.dlms.RelayParam;
import cn.hexing.fas.protocol.Protocol;
import cn.hexing.fk.clientmod.ClusterClientModule;
import cn.hexing.reread.model.ReadTimeModel;
import cn.hexing.reread.service.TimeSynService;
import cn.hexing.reread.utils.TimeUtils;

import com.hx.ansi.ansiElements.AnsiDataItem;
/**
 * 补召消息发送——BP客户端
 * @ClassName:DlmsRereader
 * @Description:TODO
 * @author kexl
 * @date 2012-9-24 上午10:31:52
 *
 */
public class TimeSynReader {
	private static final Logger log = Logger.getLogger(TimeSynReader.class);
	public static final String OPERATOR = "TimeSyn";
	public static final String OPERATOR_TIME = "TimeSyn";
	public static final String CZYID = "admin";
	//发送请求间隔时间
	private String interval = null; //毫秒
	private int intervalValue = 100;
	
	private ClusterClientModule com=null;
	
	private TimeSynService service ;
	
	//是否要求BP自动对时（0-否，1-是）
	private String bpAutoTimeSyn = "0";
	//BP自动对时时间差阀值（秒）
	private String timeDiffThreshold = "0";
	//夏令时标志
	private String dstFlag = "0";
	private String dstRule = "";
	
	private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public void read(List<ReadTimeModel> models , String rwdwdm , String rwzxsj , String rwlx) {
		if(interval!=null) try{intervalValue = Integer.parseInt(interval);}catch(Exception e){log.warn(e); };//使用配置的参数
		// 按照终端局号进行分类
		Map<String, List<ReadTimeModel>> modelsMap = new HashMap<String, List<ReadTimeModel>>();
		for (ReadTimeModel model : models) {
			String zdjh = model.getZdjh();
			if (modelsMap.get(zdjh) == null)
				modelsMap.put(zdjh, new ArrayList<ReadTimeModel>());
			modelsMap.get(zdjh).add(model);
		}
		int count = 0;
		while (!modelsMap.isEmpty()) {
			Iterator<String> zdjhs = modelsMap.keySet().iterator();
			int tempCount = 0;
			while(zdjhs.hasNext()) {
				String zdjh = zdjhs.next();
				List<ReadTimeModel> zdModels = modelsMap.get(zdjh);
				if (zdModels.size() == 0) {
					zdjhs.remove();
				} else {
					ReadTimeModel model = zdModels.get(0);
					FaalRequest req = getRequest(model , rwdwdm,rwzxsj,rwlx);
					if(req!=null){
						com.sendRequest(null, null, req);
						tempCount++;
						count++;
					}
					zdModels.remove(0);
				}
			}
			if(tempCount>0){
				try {
					log.info("It's sending "+tempCount+" request in this round.All request size is " + count + ".Sleep a moment...");
					Thread.sleep(intervalValue); //遍历全部终端一次后需要等待，防止中继对时命令太多一次性发给集中器
				} catch (InterruptedException e) {
					log.warn("thread sleep error!", e);
				}
			}
		}
		log.info("send " + count + " records read time requests to BP...");
	}
	
	private FaalRequest getRequest(ReadTimeModel model , String rwdwdm , String rwzxsj , String rwlx){
		FaalRequest req = null;
		try{
			int timeDiffThresholdValue = 0;
			try{timeDiffThresholdValue = Integer.parseInt(timeDiffThreshold);}catch (Exception e) {}
			String autoTimeSynFlag = bpAutoTimeSyn; //0-只召不对、1-先召再对、2-不召直接对（0、1属于系统参数，2由程序判断是否将1转为2）
			if(Protocol.GG.equals(model.getZdgylx())  && "1".equals(model.getIszj())){ //广规中继0,2
				if("1".equals(bpAutoTimeSyn))autoTimeSynFlag = "2";
				req = getGgZjRequest(model, autoTimeSynFlag);
			}else if((Protocol.DLMS.equals(model.getZdgylx())&&!"1".equals(model.getIszj())) || 
					(Protocol.DLMS.equals(model.getTxgy())&&"1".equals(model.getIszj()))){ //DLMS终端、DLMS中继0,1
				req = getDlmsRequest(model, autoTimeSynFlag);
			}else if(Protocol.G04.equals(model.getZdgylx()) && !"1".equals(model.getIszj())) { // 国网终端0,2
				if("1".equals(bpAutoTimeSyn))autoTimeSynFlag = "2";
				req = getGwRequest(model, autoTimeSynFlag);
			}else if (Protocol.ZJ.equals(model.getZdgylx())  && !"1".equals(model.getIszj())) { // 浙规终端0,2
				if("1".equals(bpAutoTimeSyn))autoTimeSynFlag = "2";
				req = getZgRequest(model, autoTimeSynFlag);
			}else if (Protocol.GG.equals(model.getZdgylx())  && !"1".equals(model.getIszj())) { // 广规终端0,2
				if("1".equals(bpAutoTimeSyn))autoTimeSynFlag = "2";
				req = getGgRequest(model, autoTimeSynFlag);
			}else if(Protocol.ANSI.equals(model.getZdgylx()) && !"1".equals(model.getIszj()) ){ //ANSI终端0,1
				req = getAnsiRequest(model, autoTimeSynFlag);
			}
			else{
				log.error("Protocol of readTimeModel=" + model +" is nonsupport!");
			}
			if(req!=null)
				service.addTimeSynLog(rwdwdm, rwlx, rwzxsj, model.getZdljdz(), model.getDwdm(), Integer.parseInt(model.getCldh())
						, df.format(new Date()) , model.getIszj(), autoTimeSynFlag, timeDiffThresholdValue);
		}catch (Exception e) {
		
		}
		return req;
	}
	
	private FaalRequest getAnsiRequest(ReadTimeModel model, String autoTimeSynFlag) {
		String ansiSjx = "00005200";
		FaalRequest fr = null;
		AnsiRequest req =new AnsiRequest();
		req.setMeterId(model.getZdljdz());
		req.setOpType(ANSI_OP_TYPE.OP_READ);
		req.setTable(52);
		req.setServiceTag("30");
		req.setCommId(initMlId(model, false , ansiSjx));
		req.setOperator(OPERATOR);
		if("1".equals(autoTimeSynFlag)){//是否自动对时参数
			req.addAppendParam("bpAutoTimeSyn", "1");
			int timeDiffThresholdValue = 0;
			try{timeDiffThresholdValue = Integer.parseInt(timeDiffThreshold);}catch (Exception e) {}
			req.addAppendParam("timeDiffThreshold", timeDiffThresholdValue);
		}else req.addAppendParam("bpAutoTimeSyn", "0");
		
		req.setFull(true);
		AnsiDataItem[] datas=new AnsiDataItem[1];
		datas[0]=new AnsiDataItem();
		datas[0].dataCode = ansiSjx;
		datas[0].data = df.format(new Date());
		req.setDataItem(datas);
		fr=req;	
		fr.setYhlx(model.getYhlx());
		fr.setDwdm(model.getDwdm());
		fr.setProtocol(Protocol.ANSI);
		return fr;
	}

	private long initMlId(ReadTimeModel model , boolean isCalling , String sjx){
		long taskId = service.getTaskId();
		service.insTask(taskId, CZYID);
		long mlId = service.getMlId();
		service.insMl(mlId, taskId, model.getZdjh(), Integer.parseInt(model.getCldh()));
		if(!isCalling){
			service.insSzjg(mlId, model.getZdjh(), Integer.parseInt(model.getCldh()) , sjx);
		}
		return mlId;
	}
	//DLMS终端、DLMS表计（国网集中器、广规集中器）
	private FaalRequest getDlmsRequest(ReadTimeModel model , String  autoTimeSynFlag){
		FaalRequest faalReq=null;
		try{
			if(Protocol.GG.equals(model.getZdgylx())){ //广规中继
				faalReq = getGgZjRequest(model, autoTimeSynFlag);
			}else{
				DlmsRequest dlmsRequest = new DlmsRequest();
				DlmsObisItem obisItem = new DlmsObisItem();
				obisItem.classId = 8;
				obisItem.obisString = "0.0.1.0.0.255";
				obisItem.attributeId = 2;
				DlmsObisItem[] params = new DlmsObisItem[]{obisItem};
				dlmsRequest.setParams(params);
				if("1".equals(model.getIszj())){
					RelayParam relayParam = new RelayParam();
					relayParam.setDcLogicalAddress(model.getZdljdz());// 集中器logicalAddress
					relayParam.setMeasurePoint(Integer.parseInt(model.getCldh())); // 测量点
					dlmsRequest.setRelayParam(relayParam);
					dlmsRequest.setCategory(BUSINESS_CATEGORY.BIZ_RELAY);
				}else{
					//设置表计通讯地址{commAddr}
					dlmsRequest.setMeterId(model.getZdljdz());
				}
				dlmsRequest.setProtocol(Protocol.DLMS);
				dlmsRequest.setOpType(DlmsRequest.DLMS_OP_TYPE.OP_GET);
				dlmsRequest.setOperator(OPERATOR);
				dlmsRequest.setType(DlmsRequest.TYPE_OTHER);
				//是否自动对时参数
				if("1".equals(bpAutoTimeSyn)){
					dlmsRequest.addAppendParam("bpAutoTimeSyn", "1");
					int timeDiffThresholdValue = 0;
					try{timeDiffThresholdValue = Integer.parseInt(timeDiffThreshold);}catch (Exception e) {}
					dlmsRequest.addAppendParam("timeDiffThreshold", timeDiffThresholdValue);
				}else dlmsRequest.addAppendParam("bpAutoTimeSyn", "0");
				if("1".equals(dstFlag) && isDaySaving(new Date())){
					dlmsRequest.addAppendParam("isDaySaving", true); 
				}else{
					dlmsRequest.addAppendParam("isDaySaving", false); 
				}
				
				faalReq = dlmsRequest;
			}
		}catch(Exception ex){
			log.error("create dlms reread request error!", ex);
		}
		return faalReq;
	}
	
	private boolean isDaySaving(Date now){
		if("1".equals(dstRule)){ //阿塞拜疆规则（起始时间：每年3月最后1个星期日04:00:00 ，结束时间：每年10月最后1个星期日05:00:00）
			Calendar startTime = TimeUtils.getLastDateInMonthByWeek(2, 1);
			startTime.set(Calendar.HOUR_OF_DAY, 4);
			Calendar endTime = TimeUtils.getLastDateInMonthByWeek(9, 1);
			endTime.set(Calendar.HOUR_OF_DAY, 5);
			if(now.getTime()>=startTime.getTime().getTime() && now.getTime()<=endTime.getTime().getTime()){
				return true;
			}else{
				return false;
			}
		}else{
			log.warn("The system parameter [dstRule] is null!");
			return false;
		}
	}
	//国网终端读取终端时间
	private FaalRequest getGwRequest(ReadTimeModel model , String  autoTimeSynFlag) throws Exception{
		boolean isCalling = true;
		FaalRequest req = null;
		if("0".equals(autoTimeSynFlag) || "1".equals(autoTimeSynFlag)){
			req = new FaalGWNoParamRequest();
			req.setType(new Integer(0x0c));
			req.setOperator(OPERATOR);
		}else if("2".equals(autoTimeSynFlag)){
			isCalling = false;
			req = new FaalGWNoParamRequest();
			req.setType(new Integer(0x05));
			req.setOperator(OPERATOR_TIME);
		}
		req.setRtuParams(getGwRtuParam(model , isCalling));
		req.setDwdm(model.getDwdm());
		req.setProtocol(Protocol.G04);
		req.setYhlx(model.getYhlx());
		return req;
	}
	private List<FaalRequestRtuParam> getGwRtuParam(ReadTimeModel model, boolean isCalling) throws Exception {
		if(isCalling){
			return this.getRtuParam(model, "0CF002", isCalling);
		}else{
			return this.getRtuParam(model, "05F031", isCalling);
		}
	}
	/**
	 * 浙规终端
	 * @param model
	 * @param autoTimeSynFlag
	 * @return
	 * @throws Exception
	 */
	private FaalRequest getZgRequest(ReadTimeModel model, String  autoTimeSynFlag) throws Exception{
		boolean isCalling = true;
		FaalRequest req = null;
		if("0".equals(autoTimeSynFlag) || "1".equals(autoTimeSynFlag)){
			req = new FaalReadCurrentDataRequest();
			req.setOperator(OPERATOR);
		}else if("2".equals(autoTimeSynFlag)){
			req = new FaalWriteParamsRequest();
			isCalling = false;
			req.setOperator(OPERATOR_TIME);
		}
		req.setRtuParams(getZgRtuParam(model , isCalling));
		req.setDwdm(model.getDwdm());
		req.setProtocol(Protocol.ZJ);
		req.setYhlx(model.getYhlx());
		return req;
	}
	
	private List<FaalRequestRtuParam> getZgRtuParam(ReadTimeModel model , boolean isCalling) throws Exception {
		return this.getRtuParam(model, "8030", isCalling);
	}
	private List<FaalRequestRtuParam> getRtuParam(ReadTimeModel model , String sjx , boolean isCalling) throws Exception {
		List<FaalRequestRtuParam> frpList = new ArrayList<FaalRequestRtuParam>();
		FaalRequestRtuParam frp = new FaalRequestRtuParam();
		frp.setRtuId(model.getZdjh());
		frp.setTn(new int[]{Integer.parseInt(model.getCldh())});
		List<FaalRequestParam> pmlist = new ArrayList<FaalRequestParam>();
		pmlist.add(new FaalRequestParam(sjx, isCalling? null:new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
		frp.setParams(pmlist);
		frp.setCmdId(initMlId(model,isCalling , sjx));
		frpList.add(frp);
		return frpList;
	}
	/**
	 * 广规集中器、终端
	 * @param model
	 * @param autoTimeSynFlag
	 * @return
	 */
	private FaalRequest getGgRequest(ReadTimeModel model , String  autoTimeSynFlag){
		if("0".equals(autoTimeSynFlag) || "1".equals(autoTimeSynFlag)){
			FaalReadCurrentDataRequest req =new FaalReadCurrentDataRequest();
			req.setProtocol(Protocol.GG);
			req.addRtuParam(this.getGgRtuParam(initMlId(model,true,"8030"), model.getZdljdz(), new int[]{0}, true));
			req.setOperator(OPERATOR);
			req.setYhlx(model.getYhlx());
			req.setType(01);//
			return req;
		}else if("2".equals(autoTimeSynFlag)){
			FaalWriteParamsRequest req = new FaalWriteParamsRequest();
			req.setProtocol(Protocol.GG);
			req.addRtuParam(this.getGgRtuParam(initMlId(model,false,"8030"), model.getZdljdz(), new int[]{0}, false));
			req.setOperator(OPERATOR_TIME);
			req.setYhlx(model.getYhlx());
			req.setType(8);
			return req;
		}
		return null;
	}
	private FaalRequestRtuParam getGgRtuParam(Long mlid, String zdljdz , int[] cldh , boolean isCalling){
		FaalRequestRtuParam rtuparam = new FaalRequestRtuParam();
		rtuparam.setCmdId(mlid);
		rtuparam.setRtuId(zdljdz);
		rtuparam.setTn(cldh);
		rtuparam.addParam("8030", isCalling? null:new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())); 
		return rtuparam;
	}
	/**
	 * 广规中继
	 * @param model
	 * @param autoTimeSynFlag
	 * @return
	 */
	private FaalRequest getGgZjRequest(ReadTimeModel model , String  autoTimeSynFlag){
		FaalRequest req;
		boolean isCalling = true;
		if("0".equals(autoTimeSynFlag) || "1".equals(autoTimeSynFlag)){
			FaalGGKZM11Request req11 =new FaalGGKZM11Request();//召测
			req11.setDataTime(new Date());
			req = req11;;
			req.setType(17);//0x11-->17//召测
			req.setOperator(OPERATOR);
		}else if("2".equals(autoTimeSynFlag)){
			isCalling = false;//对时
			FaalGGKZM14Request req14 = new FaalGGKZM14Request(); //对时
			req14.setDataTime(new Date());
			req = req14;;
			req.setType(20);//0x11-->17//召测
			req.setOperator(OPERATOR_TIME);
		}else{
			return null;
		}
		req.setProtocol(Protocol.GG);
		req.setDwdm(model.getDwdm());
		req.setYhlx(model.getYhlx());
		req.addRtuParam(this.getGgZjRtuParam(initMlId(model,isCalling,"8031"), model.getZdljdz(), new int[]{Integer.parseInt(model.getCldh())}, isCalling));
		return req;
	}
	private FaalRequestRtuParam getGgZjRtuParam(Long mlid, String zdljdz , int[] cldh , boolean isCalling){
		FaalRequestRtuParam rtuparam = new FaalRequestRtuParam();
		rtuparam.setCmdId(mlid);
		rtuparam.setRtuId(zdljdz);
		rtuparam.setTn(cldh);
		Date date = new Date();
		rtuparam.addParam("8031", isCalling? null:new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date)); 
		return rtuparam;
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

	public String getBpAutoTimeSyn() {
		return bpAutoTimeSyn;
	}
	public void setBpAutoTimeSyn(String bpAutoTimeSyn) {
		this.bpAutoTimeSyn = bpAutoTimeSyn;
	}
	public String getTimeDiffThreshold() {
		return timeDiffThreshold;
	}
	public void setTimeDiffThreshold(String timeDiffThreshold) {
		this.timeDiffThreshold = timeDiffThreshold;
	}

	public TimeSynService getService() {
		return service;
	}

	public void setService(TimeSynService service) {
		this.service = service;
	}

	public String getDstFlag() {
		return dstFlag;
	}

	public void setDstFlag(String dstFlag) {
		this.dstFlag = dstFlag;
	}

	public String getDstRule() {
		return dstRule;
	}

	public void setDstRule(String dstRule) {
		this.dstRule = dstRule;
	}
}
