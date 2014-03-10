package cn.hexing.fk.bp.processor;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.axis.utils.StringUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;

import cn.hexing.db.batch.AsyncService;
import cn.hexing.db.batch.event.adapt.BaseReadTaskHandler;
import cn.hexing.fas.framework.message.MessageGg;
import cn.hexing.fas.model.FaalRereadTaskResponse;
import cn.hexing.fas.model.RtuData;
import cn.hexing.fas.model.RtuDataItem;
import cn.hexing.fas.protocol.Protocol;
import cn.hexing.fas.protocol.handler.ProtocolHandler;
import cn.hexing.fas.protocol.handler.ProtocolHandlerFactory;
import cn.hexing.fk.bp.model.MessageLogErr;
import cn.hexing.fk.bp.model.TaskBYQWD;
import cn.hexing.fk.bp.model.TaskDLSJ;
import cn.hexing.fk.bp.model.TaskDLSJCBR;
import cn.hexing.fk.bp.model.TaskDLSJYDJ;
import cn.hexing.fk.bp.model.TaskData;
import cn.hexing.fk.bp.model.TaskFHSJ;
import cn.hexing.fk.bp.model.TaskXBJBL;
import cn.hexing.fk.bp.model.TaskYFFSJ;
import cn.hexing.fk.bp.model.TaskYFFSJYDJ;
import cn.hexing.fk.bp.processor.gg.GgMessageProcessor;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.MessageType;
import cn.hexing.fk.message.gw.MessageGw;
import cn.hexing.fk.message.zj.MessageZj;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.model.MeasuredPoint;
import cn.hexing.fk.model.RtuManage;
import cn.hexing.fk.model.RtuTask;
import cn.hexing.fk.model.TaskDbConfig;
import cn.hexing.fk.model.TaskDbConfigItem;
import cn.hexing.fk.tracelog.TraceLog;
import cn.hexing.fk.utils.Counter;
import cn.hexing.fk.utils.HexDump;
import cn.hexing.fk.utils.StringUtil;

public class TaskMessageHandler extends BaseReadTaskHandler {
	private static final Logger log = Logger.getLogger(TaskMessageHandler.class);
	private static final TraceLog tracer = TraceLog.getTracer(TaskMessageHandler.class);
	//专变上报电量数据表名
	private static final String TABLE_NAME_DLSJ="TEMP_SB_DLSJ";
	//专变上报负荷数据表名
	private static final String TABLE_NAME_FHSJ="TEMP_SB_FHSJ";
	//上报日冻结数据
	private static final String TABLE_NAME_DLSJCBR="TEMP_SB_DLSJ_CBR";
	
	private static final String TABLE_NAME_YFFSJ="TEMP_SB_YFFSJ";
	
	private static final String TABLE_NAME_DLSJYDJ="TEMP_SB_DLSJ_YDJ";
	
	private static final String TABLE_NAME_YFFSJ_YDJ="TEMP_SB_YFFSJ_YDJ";
	
	private static final String TABLE_NAME_BYQWD="TEMP_SB_BYQWD";
	
	private static final String TABLE_NAME_XBJBL="TEMP_SB_XBJBL";
	
	//任务后处理针对的数据编码
//    private String[] taskCodes=new String[]{"0200111201","0200110000","0122100000"};	
    private Counter taskCounter=new Counter(5000,"TaskMessageHandler");
    
	public void handleReadTask(AsyncService service,IMessage msg){
		MessageZj zjmsg=null;
		MessageGw gwmsg=null;
		int rtua=0,kzm=0;
		if (msg.getMessageType()==MessageType.MSG_ZJ){
			zjmsg=(MessageZj)msg;
			rtua=zjmsg.head.rtua;
			kzm=zjmsg.head.c_func;
		}				
		else if (msg.getMessageType()==MessageType.MSG_GW_10){
			gwmsg=(MessageGw)msg;
			rtua=gwmsg.head.rtua;
			kzm=gwmsg.getAFN();
		}	
		//分辨终端规约:浙江规约或浙江配变规约
		BizRtu rtu=(RtuManage.getInstance().getBizRtuInCache(rtua));
		if (rtu==null){
			log.error("not find rtu in cache:"+HexDump.toHex(rtua));
			return;
		}
		try {  						
			Class messageType = MessageZj.class;;                    
	    	if(msg instanceof MessageGw){            			    		
	        	if (rtu!=null&&rtu.getRtuProtocol().equals(Protocol.G04)){
	        		messageType = MessageGw.class;                  		              	
	        	}             	
	    	}else if(msg instanceof MessageZj){
	        	if (rtu!=null&&rtu.getRtuProtocol().equals(Protocol.GG)){
	        		messageType = MessageGg.class;       
	        		if(GgMessageProcessor.isG3MeterBox){
	        			IMessage ggMsg=GgMessageProcessor.getInstance().onRequestComplete(rtu.getLogicAddress());
	        			if(ggMsg!=null){
	        				//由于有些解析用到fseq,所以讲fseq赋值
	        				zjmsg.head.fseq = ((MessageZj)ggMsg).head.fseq;
	        			}
	        		}
	        	}           
	    	}
	    	
	    	//调用规约解析报文
	    	ProtocolHandlerFactory factory = ProtocolHandlerFactory.getInstance();
	        ProtocolHandler handler = factory.getProtocolHandler(messageType);                	
	        Object value = handler.process(msg);  
	        if (value!=null){
	        	taskCounter.add();
	        	int bqbj=0;
	        	if (zjmsg!=null&&zjmsg.head.msta!=0)//浙规主站地址用于判断报文是否为主动上送:0终端主动上送;非0召测返回
	        		bqbj=1;
	        	else if (gwmsg!=null&&gwmsg.head.c_prm==0)//国网是否为主动上送:1终端主动上送;0召测返回
	        		bqbj=1;
	        	taskDbConfigAnalyze(rtu,service,(List<RtuData>)value,bqbj,msg,rtua,kzm);
	        }
		}catch (Exception ex) {//任务解析失败需要保存非法任务帧处理
			saveErrMsgToDb(msg,rtua,kzm,rtu.getRtuType());
            log.error("Error to processing task message:"+msg);
            log.error("Detail Message is "+ex.getMessage());
        }            
	}
	
	public void saveErrMsgToDb(IMessage msg,int rtua,int kzm ,String rtuType){
		try{
			MessageLogErr msgLogErr=new MessageLogErr();
			msgLogErr.setLogicAddress(HexDump.toHex(rtua));
			msgLogErr.setQym(msgLogErr.getLogicAddress().substring(0,2));
			msgLogErr.setKzm(Integer.toString(kzm, 16));			
			msgLogErr.setTime(new Date(msg.getIoTime()));
			msgLogErr.setBody(msg.getRawPacketString());	
			if (rtuType.equals("02"))
				service.addToDao(msgLogErr,Integer.parseInt("5012"));
			else if (rtuType.equals("03"))
				service.addToDao(msgLogErr,Integer.parseInt("5022"));
			else
				service.addToDao(msgLogErr,Integer.parseInt("5002"));
		}catch(Exception ex){
			log.error("save ErrMsg To Db error:"+ex);
		}
	}
	/*
	 * 把任务解析结果RtuData队列根据任务属性分别保存到长表或短表;
	 * 长表保存需要把每个数据项插入对应的数据库表映射对象的属性,
	 * 不同类型任务形成多个或单个数据库表映射对象,分11地市保存01~11
	 * 99表示短表类型key=1000,其他都为长表key={1001~1011,2001~2011,3001~3011}.
	 * */
	public void taskDbConfigAnalyze(BizRtu rtu,AsyncService service,List<RtuData> rtuDatas,int bqbj,IMessage msg,int rtua,int kzm){
		for (int i = 0; i < rtuDatas.size(); i++) {
			try{
				boolean dateTag=true;				
				String deptCode=rtu.getDeptCode();
				if (deptCode!=null&&deptCode.length()>=5)//单位代码必须是5位,前两位作为分地市保存的key
					deptCode=deptCode.substring(3,5);
				else{
					log.error("终端"+rtu.getLogicAddress()+"的单位代码"+deptCode+"非法");
					saveErrMsgToDb(msg,rtua,kzm,rtu.getRtuType());
					continue;
				}				
				RtuData data= (RtuData) rtuDatas.get(i);
				//在解析的时候，任务可能已经刷新，这里再从缓存取出来
				rtu=RtuManage.getInstance().getBizRtuInCache(rtu.getLogicAddress());
				if(null!=data.getTaskNum()){
					String[] tasks = data.getTaskNum().split(",");
					for(String taskNum:tasks){
						RtuTask rt=rtu.getRtuTask(taskNum);
						if (rt==null){
							log.error("终端"+rtu.getLogicAddress()+"的任务"+data.getTaskNum()+"不存在");
							saveErrMsgToDb(msg,rtua,kzm,"");
							continue;
						}
						else if (data.getTime()==null){
							tracer.trace("终端="+rtu.getLogicAddress()+",测量点号="+data.getTn()+"的任务"+data.getTaskNum()+"的任务时间非法");
							continue;
						}
						else{					
							//任务模版属性为03的需量任务要特殊处理：插入上报电量任务时的时间得把零点30点改为零点
//							if(rt.getTaskTemplateProperty().equals("03")){
//								String date=data.getTimeStr();
//								//时间格式=yyyy-MM-dd HH:mm,并且时间为零点30
//								if (date.length()==16&&date.substring(11,16).equals("00:30")){
//									date=date.substring(0,11)+"00:00";
//									data.setTime(date);
//								}
//							}
							//专变和公变的国网规约电量任务时间需要加1，以保证和浙规电量任务时间的一致性
//							else if (rt.getTaskTemplateProperty().equals("02")
//									&&rtu.getRtuProtocol().equals(Protocol.G04)){
//								data.setTime(data.getNextday());
//							}
						}
						MeasuredPoint mp=null;
						if(rtu.getRtuProtocol().equals(Protocol.ZJ)){//浙规任务号和测量点号一一对应
							mp=rtu.getMeasuredPoint(rt.getTn());
							//浙规规约任务上报信息，需要发给漏点补招服务进行任务打点，国网规约不需要
							FaalRereadTaskResponse rereadTask=new FaalRereadTaskResponse();
							rereadTask.setLogicAddress(rtu.getLogicAddress());
							rereadTask.setDeptCode(rtu.getDeptCode());
							rereadTask.setTaskNum(data.getTaskNum());
							rereadTask.setTaskTemplateID(rt.getTaskTemplateID());
							rereadTask.setSJSJ(data.getTime());
							rereadTask.setRereadTag(bqbj);
							BPLatterProcessor.getInstance().rereadTaskAdd(rereadTask);
						}else if(rtu.getRtuProtocol().equals(Protocol.GG)){//广规取测量点 任务时间为集中器冻结数据时间比表计中冻结时间晚一天
							mp=rtu.getMeasuredPoint(data.getTn());
//							data.setTime(data.getDateBefore());
						}
						else	//国网任务号和测量点号一对多，所以国网任务测量点号需要从解析后的任务数据中取
							mp=rtu.getMeasuredPoint(data.getTn());
						if (mp==null){
							if(rtu.getRtuProtocol().equals(Protocol.ZJ)) 
								tracer.trace("Rtua "+rtu.getLogicAddress()+" Tn "+rt.getTn()+" unexist");
							else
								tracer.trace("Rtua "+rtu.getLogicAddress()+" Tn "+data.getTn()+" unexist");
							saveErrMsgToDb(msg,rtua,kzm,rtu.getRtuType());
							continue;
						}
						else{
							if (tracer.isEnabled())
								tracer.trace("rtua="+rtu.getLogicAddress()+",taskNum="+rt.getRtuTaskNum()+",tn="+mp.getTn()+",saveID="+mp.getDataSaveID());
						}				
						dataSave(service, rtu, data, mp, msg, rt, bqbj, deptCode);
					}
				}
				
			}catch(Exception ex){
				log.error("Error to taskDbConfigAnalyze:"+rtuDatas, ex);
			}
		}			
	}
	public void dataSave(AsyncService service,BizRtu rtu,RtuData data,MeasuredPoint mp,IMessage msg,RtuTask rt,int bqbj,String deptCode){
		TaskDLSJ taskSBDL=null;
		TaskFHSJ taskFHSJ=null;
		TaskDLSJCBR taskDLSJCBR=null;
		TaskDLSJYDJ taskDLSJYDJ=null;
		TaskYFFSJ taskYFFSJ = null;
		TaskYFFSJYDJ taskYFFSJYDJ=null;
		TaskBYQWD taskBYQWD=null;
		TaskXBJBL taskXBJBL=null;
		
		boolean dateTag=true;
		try{
			for (int j = 0; j < data.getDataList().size(); j++) {
				RtuDataItem dataItem=(RtuDataItem)data.getDataList().get(j);
				//任务后处理
				/*if (ArraysUtil.contains(taskCodes,dataItem.getCode())) {
					TaskItemData taskItemData=new TaskItemData();
					taskItemData.setDeptCode(rtu.getDeptCode());
					taskItemData.setCustomerNo(rtu.getMeasuredPoint(mp.getTn()).getCustomerNo());
					taskItemData.setStationNo(rtu.getMeasuredPoint(mp.getTn()).getStationNo());													
					taskItemData.setRtuId(rtu.getRtuId());
					taskItemData.setCode(dataItem.getCode());
					taskItemData.setValue(dataItem.getValue());
					taskItemData.setTxfs(msg.getTxfs());
					taskItemData.setCt(mp.getCt());
					taskItemData.setPt(mp.getPt());
					taskItemData.setTime(data.getTime());
					if (taskItemData.getValue()!=null&&taskItemData.getValue()!=""&&!rtu.getRtuType().equals("03"))
						service.addToDao(taskItemData,5100);
		        }*/
				if (rt.getTaskTemplateProperty().equals("99")){//其他任务类型保存短表
					TaskData taskData=new TaskData();
					taskData.setSJID(mp.getDataSaveID());
					taskData.setSJSJ(data.getTime());
					taskData.setBQBJ(bqbj);
					taskData.setCT(mp.getCt());
					taskData.setPT(mp.getPt());
					taskData.setSJBH(dataItem.getCode());
					taskData.setSJZ(dataItem.getValue());
					service.addToDao(taskData,Integer.parseInt("1000"));
				}
				else{//保存长表,算法:把每个数据项插入对应的数据库表映射对象的属性
					TaskDbConfig taskDbConfig=RtuManage.getInstance().getTaskDbConfigInCache(dataItem.getCode());
					if (taskDbConfig==null){
						//运行过程中，返现这里打印大量日志。所以取消
//						if (log.isDebugEnabled())
//							tracer.trace("sjbm="+dataItem.getCode()+",not find in getSjxMap");
						continue;
					}
					for (int k = 0; k < taskDbConfig.getTaskDbConfigItemList().size(); k++) {
						TaskDbConfigItem taskDbConfigItem=(TaskDbConfigItem)taskDbConfig.getTaskDbConfigItemList().get(k);
						//任务分类要和终端用途一致才能入库
						//任务属性匹配，任务模板属性不在当前任务数据属性列表则不保存
						if (taskDbConfigItem.taskPropertyContains(rt.getTaskTemplateProperty())){
							//这里需要增加判断TaskDbConfigItem.tag,"00":表示只有零点数据才保存;"01"表示只有整点数据才保存
							if (taskDbConfigItem.getTag().equals("00")||taskDbConfigItem.getTag().equals("01")){								
								dateTag=saveJudgeByHourTag(taskDbConfigItem.getTag(),data.getTime());
								//if (!dateTag)
									//continue;					
							}
							if(StringUtils.isEmpty(dataItem.getValue()))
								dataItem.setValue(null);
							if (taskDbConfigItem.getTableName().equals(TABLE_NAME_DLSJ)){
								if (taskSBDL==null){
									taskSBDL=new TaskDLSJ();
									taskSBDL.setSJID(mp.getDataSaveID());
									taskSBDL.setSJSJ(data.getTime());
									taskSBDL.setBQBJ(bqbj);
									taskSBDL.setCT(mp.getCt());
									taskSBDL.setPT(mp.getPt());			
								}
								if(dataItem.getCode().equals("0400122000")){
									taskSBDL.setJSSJ(dataItem.getValue());
									continue;
								}
								PropertyUtils.setProperty(taskSBDL,taskDbConfigItem.getFieldName(),dataItem.getValue());
							}
							else if (taskDbConfigItem.getTableName().equalsIgnoreCase(TABLE_NAME_FHSJ)){
								if (taskFHSJ==null){
									taskFHSJ=new TaskFHSJ();
									taskFHSJ.setSJID(mp.getDataSaveID());
									taskFHSJ.setSJSJ(data.getTime());
									taskFHSJ.setBQBJ(bqbj);
									taskFHSJ.setCT(mp.getCt());
									taskFHSJ.setPT(mp.getPt());			
								}
								PropertyUtils.setProperty(taskFHSJ,taskDbConfigItem.getFieldName(),dataItem.getValue());
							}else if(taskDbConfigItem.getTableName().equalsIgnoreCase(TABLE_NAME_DLSJCBR)){
								if(taskDLSJCBR==null){
									taskDLSJCBR = new TaskDLSJCBR();
									taskDLSJCBR.setSJID(mp.getDataSaveID());
									taskDLSJCBR.setSJSJ(data.getTime());
									taskDLSJCBR.setBQBJ(bqbj);
									taskDLSJCBR.setCT(mp.getCt());
									taskDLSJCBR.setPT(mp.getPt());
								}
								PropertyUtils.setProperty(taskDLSJCBR, taskDbConfigItem.getFieldName(), dataItem.getValue());
								
							}
							else if(taskDbConfigItem.getTableName().equalsIgnoreCase(TABLE_NAME_YFFSJ)){
								if(taskYFFSJ==null){
									taskYFFSJ = new TaskYFFSJ();
									taskYFFSJ.setSJID(mp.getDataSaveID());
									taskYFFSJ.setSJSJ(data.getTime());
									taskYFFSJ.setBQBJ(bqbj);
									taskYFFSJ.setCT(mp.getCt());
									taskYFFSJ.setPT(mp.getPt());
								}
								PropertyUtils.setProperty(taskYFFSJ, taskDbConfigItem.getFieldName(), dataItem.getValue());
							}
							else if(taskDbConfigItem.getTableName().equalsIgnoreCase(TABLE_NAME_YFFSJ_YDJ)){
								if(taskYFFSJYDJ==null){
									taskYFFSJYDJ = new TaskYFFSJYDJ();
									taskYFFSJYDJ.setSJID(mp.getDataSaveID());
									taskYFFSJYDJ.setSJSJ(data.getTime());
									taskYFFSJYDJ.setBQBJ(bqbj);
									taskYFFSJYDJ.setCT(mp.getCt());
									taskYFFSJYDJ.setPT(mp.getPt());
								}
								PropertyUtils.setProperty(taskYFFSJYDJ, taskDbConfigItem.getFieldName(), dataItem.getValue());
							}else if(taskDbConfigItem.getTableName().equalsIgnoreCase(TABLE_NAME_DLSJYDJ)){
								if(taskDLSJYDJ==null){
									taskDLSJYDJ = new TaskDLSJYDJ();
									taskDLSJYDJ.setSJID(mp.getDataSaveID());
									taskDLSJYDJ.setSJSJ(data.getTime());
									taskDLSJYDJ.setBQBJ(bqbj);
									taskDLSJYDJ.setCT(mp.getCt());
									taskDLSJYDJ.setPT(mp.getPt());
								}
								PropertyUtils.setProperty(taskDLSJYDJ, taskDbConfigItem.getFieldName(), dataItem.getValue());
							}else if(taskDbConfigItem.getTableName().equalsIgnoreCase(TABLE_NAME_BYQWD)){
								if(taskBYQWD==null){
									taskBYQWD = new TaskBYQWD();
									taskBYQWD.setSJID(mp.getDataSaveID());
									taskBYQWD.setSJSJ(data.getTime());
									taskBYQWD.setBQBJ(bqbj);
									taskBYQWD.setCT(mp.getCt());
									taskBYQWD.setPT(mp.getPt());
								}
								PropertyUtils.setProperty(taskBYQWD, taskDbConfigItem.getFieldName(), dataItem.getValue());
							}else if(taskDbConfigItem.getTableName().equalsIgnoreCase(TABLE_NAME_XBJBL)){
								if(taskXBJBL==null){
									taskXBJBL = new TaskXBJBL();
									taskXBJBL.setSJID(mp.getDataSaveID());
									taskXBJBL.setSJSJ(data.getTime());
									taskXBJBL.setBQBJ(bqbj);
									taskXBJBL.setCT(mp.getCt());
									taskXBJBL.setPT(mp.getPt());
								}
								PropertyUtils.setProperty(taskXBJBL, taskDbConfigItem.getFieldName(), dataItem.getValue());
							}					
						}																				
					}
				}
			}
			if (taskSBDL!=null){//电量
				rebuildTimeValue(taskSBDL);
				service.addToDao(taskSBDL,1000);
			}			
			if (taskFHSJ!=null){//负荷
				service.addToDao(taskFHSJ,2000);
			}
			if(taskBYQWD!=null){
				service.addToDao(taskBYQWD, 4005);
			}
			if(taskXBJBL!=null){
				service.addToDao(taskXBJBL, 4006);
			}
			if (taskDLSJCBR!=null){//电量抄表日数据
				rebuildTimeValue(taskDLSJCBR);
				service.addToDao(taskDLSJCBR, 1001);
			}
			if (taskYFFSJ!=null){//预付费数据
				service.addToDao(taskYFFSJ, 1003);
			}
			if (taskYFFSJYDJ!=null){//预付费数据月冻结
				rebuildTimeValue(taskYFFSJYDJ);
				service.addToDao(taskYFFSJYDJ, 1004);
			}
			if (taskDLSJYDJ!=null){//电量数据月冻结
				rebuildTimeValue(taskDLSJYDJ);
				service.addToDao(taskDLSJYDJ, 1002);
			}
		}catch(Exception ex){
			log.error("Error to dataSave:" +ex);
		}
		
	}
	
	/**
	 * 将任务数据里的时间数据重新组装，使用反射原理，处理数据名称末尾带有时间的属性值
	 * @param o
	 */
	public void rebuildTimeValue(Object o){
		try {
			Class<? extends Object> clazz = o.getClass();
			Field[] fields = clazz.getDeclaredFields();
			for(Field field : fields){
				String fieldName = field.getName();
				if(field.getType() == String.class && fieldName.endsWith("SJ")){
					PropertyDescriptor  pd = new PropertyDescriptor(fieldName,clazz);
					Method readMethod = pd.getReadMethod();
					pd.getWriteMethod().invoke(o,processTimeValue((String) readMethod.invoke(o)));
				}
			}
		} catch (Exception e) {
			log.error(StringUtil.getExceptionDetailInfo(e));
		} 
		
	}
	
	/**
	 * 处理需量时间
	 * @param string
	 * @return
	 */
	private String processTimeValue(String demandTime) {
		if(demandTime==null || "".equals(demandTime)) return null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			sdf.parse(demandTime);
			return demandTime;
		} catch (ParseException e) {
			//说明不是yyyy-MM-dd HH:mm:ss这样的格式
			sdf=new SimpleDateFormat("MM-dd HH:mm");
			try {
				sdf.parse(demandTime);
				return Calendar.getInstance().get(Calendar.YEAR)+"-"+demandTime+":00";
			} catch (ParseException e1) {
				log.error("demandTime can't match yyyy-MM-dd HH:mm:ss and MM-dd HH:mm");
			}
		}
		return demandTime;
	}
	/*
	 * hourTag,"00":表示只有零点数据才保存;"01"表示只有整点数据才保存
	 */
	public boolean saveJudgeByHourTag(String hourTag,Date date){
		boolean result=false;
		SimpleDateFormat sdf = new SimpleDateFormat("HHmm");	
		try{
			String sDate=sdf.format(date);
			if (sDate!=null&&sDate.length()==4){
				if (hourTag.equals("00")){//零点零分才保存
					if (sDate.substring(0,4).equals("0000"))//HH=00零点
						result=true;					
				}
				else if(hourTag.equals("01")){
					if (sDate.substring(2,4).equals("00"))//mm=00整点
						result=true;
				}
			}		
		}catch(Exception ex){
			log.error("save task date Judge By HourTag err:"+ex);
		}
		return result;
	}
}
