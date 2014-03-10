package cn.hexing.fk.bp.processor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import cn.hexing.db.batch.AsyncService;
import cn.hexing.db.bizprocess.MasterDbService;
import cn.hexing.db.managertu.ManageRtu;
import cn.hexing.fas.framework.message.MessageGg;
import cn.hexing.fas.model.FaalRequestResponse;
import cn.hexing.fas.model.FaalRequestResponseNew;
import cn.hexing.fas.model.HostCommand;
import cn.hexing.fas.model.HostCommandResult;
import cn.hexing.fas.model.RtuAlert;
import cn.hexing.fas.model.RtuData;
import cn.hexing.fas.model.RtuDataItem;
import cn.hexing.fas.protocol.Protocol;
import cn.hexing.fas.protocol.handler.ProtocolHandler;
import cn.hexing.fas.protocol.handler.ProtocolHandlerFactory;
import cn.hexing.fas.protocol.zj.ErrorCode;
import cn.hexing.fas.protocol.zj.FunctionCode;
import cn.hexing.fk.bp.dlms.time.SaveAutoTimeResult;
import cn.hexing.fk.bp.model.AmmeterIdentification;
import cn.hexing.fk.bp.model.HostCommandDb;
import cn.hexing.fk.bp.model.HostCommandItemDb;
import cn.hexing.fk.bp.model.HostParamResult;
import cn.hexing.fk.bp.processor.gg.GgMessageProcessor;
import cn.hexing.fk.bp.webapi.MessageWeb;
import cn.hexing.fk.bp.webapi.WebMessageEncoder;
import cn.hexing.fk.bp.webapi.WebMessageFileTransfer;
import cn.hexing.fk.common.spi.socket.IServerSideChannel;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.MessageConst;
import cn.hexing.fk.message.MessageType;
import cn.hexing.fk.message.gw.MessageGw;
import cn.hexing.fk.message.zj.MessageZj;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.model.MeasuredPoint;
import cn.hexing.fk.model.Operator;
import cn.hexing.fk.model.RtuCmdItem;
import cn.hexing.fk.model.RtuManage;
import cn.hexing.fk.model.RtuSetValue;
import cn.hexing.fk.model.TaskTemplate;
import cn.hexing.fk.model.UpgradeInfo;
import cn.hexing.fk.sockserver.AsyncSocketClient;
import cn.hexing.fk.sockserver.TcpSocketServer;
import cn.hexing.fk.tracelog.TraceLog;
import cn.hexing.fk.utils.ApplicationContextUtil;
import cn.hexing.fk.utils.HexDump;

public class HostCommandHandler {
	private static final Logger log = Logger.getLogger(HostCommandHandler.class);
	private static final TraceLog tracer = TraceLog.getTracer(HostCommandHandler.class);
	/** Web 接口消息编码器 */
    private final WebMessageEncoder encoder = new WebMessageEncoder();
	public void handleExpNormalMsg(AsyncService asycService,ManageRtu manageRtu,MasterDbService masterDbService,IMessage msg){		
		MessageZj zjmsg=null;
		MessageGw gwmsg=null;
		int rtua=0,fseq=0,kzm=0;
		boolean isFinished=true;
		if (msg.getMessageType()==MessageType.MSG_ZJ){
			zjmsg=(MessageZj)msg;
			rtua=zjmsg.head.rtua;
			fseq=zjmsg.head.fseq;
			kzm=zjmsg.head.c_func;
		}				
		else if (msg.getMessageType()==MessageType.MSG_GW_10){
			gwmsg=(MessageGw)msg;
			rtua=gwmsg.head.rtua;
			fseq=gwmsg.getFseq();
			kzm=gwmsg.getAFN();
			isFinished=false;
		}	
		BizRtu rtu=(RtuManage.getInstance().getBizRtuInCache(rtua));
		if (rtu==null){
			log.warn("not find rtu in cache:"+HexDump.toHex(rtua));
		}
		try{   		
			//分辨终端规约:浙江规约或浙江配变规约
			Class messageType = MessageZj.class;;                    
	    	if(msg instanceof MessageGw){            			    		
	        	if (rtu!=null&&rtu.getRtuProtocol().equals(Protocol.G04)){
	    			//判断当前是否有分帧
	    			if(gwmsg.head.seq_fir==1){
	    				//是第一帧，将当前帧序号存起来
	    				rtu.setFirstFrameSeq(fseq);
	    			}
	    			if(gwmsg.head.seq_fin==1){
	    				//最后一帧
	    				isFinished=true;
	    				fseq=rtu.getFirstFrameSeq();
	    			}
	        		messageType = MessageGw.class;                  		              	
	        	}             	
	    	}
	    	else if(msg instanceof MessageZj){
	    		if (rtu!=null&&rtu.getRtuProtocol().equals(Protocol.GG)){
	        		messageType = MessageGg.class;   
	        		if(GgMessageProcessor.isG3MeterBox){
	        			IMessage ggMsg=GgMessageProcessor.getInstance().onRequestComplete(rtu.getLogicAddress());
	        			if(ggMsg!=null){
	        				fseq=((MessageZj)ggMsg).head.fseq;
	        				//由于有些解析用到fseq,所以讲fseq赋值
	        				zjmsg.head.fseq = (byte) fseq;
	        			}
	        		}
	        	}
	    	}
	    	//调用规约解析报文
	    	ProtocolHandlerFactory factory = ProtocolHandlerFactory.getInstance();
	        ProtocolHandler handler = factory.getProtocolHandler(messageType);                	
	        Object value = handler.process(msg); 
	        List<RtuCmdItem> rcis=masterDbService.getRtuComdItem(HexDump.toHex(rtua), fseq);
			if (rcis.size()>1)//不应该有多个,只可能是唯一的
				log.error("getGetRtuComdItem size>1:"+HexDump.toHex(rtua)+";"+fseq);
	        if (value!=null){
	        	HostCommand cmd = null;
	        	if(!(value instanceof HostCommand)){
	        		cmd = otherResultConvertToHostCommand(value);
	        	}else{
	        		cmd = (HostCommand) value;
	        	}
	        	
	        	//需要通过终端逻辑地址+命令序号查询命令ID,如果命令ID查询结果为NULL或为0,则为后台下发返回,不需要更新命令列表 	
	        	boolean setTag=false;
	        	if (zjmsg!=null&&(zjmsg.head.c_func==FunctionCode.WRITE_PARAMS||zjmsg.head.c_func==FunctionCode.REALTIME_WRITE_PARAMS
	        			||zjmsg.head.c_func==FunctionCode.Action||zjmsg.head.c_func==FunctionCode.pay_token))
	        		setTag=true;
	        	//国网确认/否认报文和数据转发报文中10F010为设置返回。
	        	else if (gwmsg!=null&&(gwmsg.getAFN()==MessageConst.GW_FUNC_REPLY||
	        			 gwmsg.getAFN()==MessageConst.GW_FUNC_FILE||
	        			((gwmsg.getAFN()==MessageConst.GW_FUNC_RELAY_READ)&&
	        			(cmd.getResults()!=null &&
	        			 cmd.getResults().size()!=0 &&
	        			 cmd.getResults().get(0).getCode().equals("10F010")))))//确认返回
	        		setTag=true;
	        	else if(cmd.isSetTag())
	        		setTag=true;
	        	if (rtu.getLogicAddress().startsWith("E")){//现场pda报文上送特殊处理,只需要返回给自动装接
	        		cmd.setId(new Long(0));
	        		sendIntfRequestResponse(cmd,rtu.getRtuId(),setTag,rtu.getRtuType());
	        		return;
	        	}
	        	//判断是否为设置返回	       
	        	for(RtuCmdItem rci:rcis){
	        		long cmdId=rci.getCmdId();
	        		if (cmdId!= 0) {//主站实时请求	        		
		        		cmd.setId(new Long(cmdId));	
		        		cmd.setMessageCount(rci.getBwsl());	
		        		if(setTag){//设置返回
		        			if (gwmsg!=null){//国网
		        				if((gwmsg.getAFN()==MessageConst.GW_FUNC_FILE)){
		        					updateFileTransferStatus(masterDbService,asycService,cmd,msg);
		        					continue;
		        				}
		        					updateGwSetResult(masterDbService,cmd,msg);
		        			}
		        			else if(zjmsg!=null&&rci.getZdzjbz()!=5){//浙规非后台下发需要更新命令结果表
		        				updateZjSetResult(masterDbService,cmd,msg);
		        			}
		        			//更新主站设置请求
		        			updateParaTable(manageRtu,masterDbService,cmd,rtu);
		        		}
		        		else{//招测返回
		        			updateCallResult(masterDbService,cmd,rtu.getRtuType(),rtu.getRtuProtocol(), msg,isFinished);
		        		}		        		
		        	}
	        		else{//后台参数自动下发,不需要回写数据值,只要调用存储过程
	        			if(setTag)
	        				updateParaTable(manageRtu,masterDbService,cmd,rtu);
	        			cmd.setId(new Long(cmdId));	
	        		}
	        		if (rtu.getLogicAddress().startsWith("E"))//现场pda终端逻辑地址E开头，上送报文需要转发给自动装接
	        			rci.setZdzjbz(Operator.ZDZJ);
	        		if (rci.getZdzjbz()==Operator.ZDZJ){//自动装接请求返回
	        			sendIntfRequestResponse(cmd,rtu.getRtuId(),setTag,rtu.getRtuType());
	        		}
	        		else if (rci.getZdzjbz()==Operator.ZDZJ_SFRZ){//自动装接电表认证请求返回
	        			sendIntfRequestResponseForAmmeter(masterDbService,cmd,rtu.getRtuId(),setTag,rtu.getRtuType());
	        		}
	        		else if (rci.getZdzjbz()==Operator.HTXF_ZDDS){//终端对时召测请求返回
	        			if (!setTag)//只要把读取结果返回给终端对时服务端
	        				sendTimeCorrectorRequestResponse(cmd,rtu.getRtuId());
	        		}
	        		else if (rci.getZdzjbz()==Operator.ZZRW){//主站任务轮招请求返回
	        			sendFaalRequestResponse(cmd,rtu.getRtuId(),setTag,rtu.getRtuType());
	        		}
	        		else if (rci.getZdzjbz()==Operator.ZZRW_SJCJ){//主站任务轮招数据采集请求返回
	        			faalRequestResponseSave(asycService,cmd,rtu,msg,kzm);
	        		}
	        		
	        	}
        		if(rcis.size()==0 && gwmsg!=null){
	        		if((gwmsg.getAFN()==MessageConst.GW_FUNC_FILE)){
						updateFileTransferStatus(masterDbService,asycService,cmd,msg);
					}	
	        	}
	        }
	        else{//解析失败,回写状态
	        	for(RtuCmdItem rci:rcis){
	        		long cmdId=rci.getCmdId();
	        		if (cmdId!= 0){ //主站实时请求	
	        			if (rci.getZdzjbz()==1){//自动装接请求返回
		        			sendIntfErrorResponse(cmdId,rtu.getRtuId());
		        		}
	        			else
	        				errorProcessMessage(masterDbService,cmdId,rtu.getRtuType());
	        		}
	        	}
	        }
		}catch(Exception ex){//解析错误,回写状态
			List<RtuCmdItem> rcis=masterDbService.getRtuComdItem(HexDump.toHex(rtua), fseq);
			if (rcis.size()>1)//不应该有多个,只可能是唯一的
				log.error("getGetRtuComdItem size>1:"+HexDump.toHex(rtua)+";"+fseq);
			for(RtuCmdItem rci:rcis){
        		long cmdId=rci.getCmdId();
        		if (cmdId!= 0){ //主站实时请求	 
        			if (rci.getZdzjbz()==1){//自动装接请求返回
	        			sendIntfErrorResponse(cmdId,rtu.getRtuId());
	        		}
        			else
        				errorProcessMessage(masterDbService,cmdId,rtu.getRtuType());
        		}
        	}
			log.error("Error to processing Normal message:"+msg, ex);
		}
	}
	
	/**
	 * 别的结果转换为HostCommand
	 * @param value
	 * @return
	 */
	private HostCommand otherResultConvertToHostCommand(Object value) {
		HostCommand hc = new HostCommand();
		if(value instanceof List){
			for(Object obj : (List)value){
				if(obj instanceof RtuAlert){
					RtuAlert ra = (RtuAlert) obj;
					HostCommandResult hcr=new HostCommandResult();
					hcr.setTn(ra.getTn());
					hcr.setCode(ra.getCodeItem());
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					hcr.setValue(ra.getAlertCodeHex()+"@"+sdf.format(ra.getAlertTime()));
					if(null!=ra.getInnerCode()&& !"".equals(ra.getInnerCode())){
						hcr.setValue(hcr.getValue()+"@"+ra.getInnerCode());
					}
					hc.addResult(hcr);
				}
			}
		}

		hc.setStatus(HostCommand.STATUS_SUCCESS);
		return hc;
	}
	/*
	 * 更新国网设置结果
	 * */
	public void updateGwSetResult(MasterDbService masterDbService,HostCommand cmd,IMessage msg) {
		try{
			List<HostCommandResult> results = cmd.getResults();
			List<HostCommandResult> hrTempList=new ArrayList<HostCommandResult>();	
	    	BizRtu rtu=RtuManage.getInstance().getBizRtuInCache(msg.getRtua());
			String setValue="";
			if(results != null &&results.size()>=1){
				//全部确认/否认不包含数据项标识，需要从数据库查询获得
				if (results.get(0).getCode().equals("00F001")||results.get(0).getCode().equals("00F002")){
					List<RtuSetValue> result=masterDbService.getGwRtuSetValue(cmd.getId());
					results.clear();
					for(RtuSetValue rv:result){
						String[] codes=rv.getSjxzt().split(",");
						for(int i=0;i<codes.length;i++){
        					HostCommandResult hr=new HostCommandResult();
        					hr.setCode(codes[i]);
        					hr.setTn(""+rv.getCldh());
        					if (cmd.getStatus()==HostCommand.STATUS_SUCCESS)//成功
        						hr.setValue("00");
        					else//失败
        						hr.setValue("01");
        					results.add(hr);
        					hrTempList.add(hr);
        				}                 			
						for (HostCommandResult hcr:hrTempList){
							setValue=setValue+hcr.getCode()+":"+hcr.getValue()+",";
	    				}
	    				if(setValue.endsWith(","))//消去最后一个","
	    					setValue=setValue.substring(0,setValue.length()-1);
	    				//更新设置结果
	    				masterDbService.updateGwRtuSetValue(setValue, cmd.getId(),rv.getCldh());
	    				hrTempList.clear();
	    				setValue="";
					}	
				}
				// 国网10F010 转发主站对电表的直接遥控跳闸和允许合闸命令的应答保存到CZ_SZJG表里面
				if(results.get(0).getCode().equals("10F010")){
					Map<String,String> tnMap=new HashMap<String,String>();	
					for (HostCommandResult hcr:results){
						setValue="";
						if (tnMap.get(hcr.getTn())==null){
							setValue=hcr.getCode()+":"+hcr.getValue()+",";
							tnMap.put(hcr.getTn(), setValue);    								
						}
						else{
							setValue=tnMap.get(hcr.getTn())+hcr.getCode()+":"+hcr.getValue()+",";
							tnMap.put(hcr.getTn(), setValue);    	
						}
    				}
					Iterator it=tnMap.entrySet().iterator();
    				while(it.hasNext()){
    					Map.Entry<String,String> entry=(Map.Entry<String,String>)it.next();
    					setValue=entry.getValue();
    					if(setValue.endsWith(","))//消去最后一个","
	    					setValue=setValue.substring(0,setValue.length()-1);
    					//更新设置结果
	    				masterDbService.updateGwRtuSetValue(setValue, cmd.getId(),Integer.parseInt(entry.getKey()));
    				}
				}

				else {//部分确认否认
					Map<String,String> tnMap=new HashMap<String,String>();	
					for (HostCommandResult hcr:results){
						setValue="";
						if (tnMap.get(hcr.getTn())==null){
							if(hcr.getValue().equals("5F")||hcr.getValue().equals("50")){
								hcr.setValue("00");
							}
							setValue=hcr.getCode()+":"+hcr.getValue()+",";
							tnMap.put(hcr.getTn(), setValue);    								
						}
						else{
							setValue=tnMap.get(hcr.getTn())+hcr.getCode()+":"+hcr.getValue()+",";
							tnMap.put(hcr.getTn(), setValue);    	
						}
    				}
    				Iterator it=tnMap.entrySet().iterator();
    				while(it.hasNext()){
    					Map.Entry<String,String> entry=(Map.Entry<String,String>)it.next();
    					setValue=entry.getValue();
    					if(setValue.endsWith(","))//消去最后一个","
	    					setValue=setValue.substring(0,setValue.length()-1);
    					//更新设置结果
	    				masterDbService.updateGwRtuSetValue(setValue, cmd.getId(),Integer.parseInt(entry.getKey()));
	    				//在更新设置结果之后，需要将对时成功的结果修改到TJ_ZDDSSJ 表中
	    				if(Protocol.G04.equals(rtu.getRtuProtocol())&&"05F031:00".equals(setValue)){
	    					SaveAutoTimeResult.getInstance().updateAutoTimeResult(masterDbService,entry.getKey(),msg);
	    				}
    				}
				}
			}
	        HostCommandDb commandDb=new HostCommandDb();
	        commandDb.setId(cmd.getId());
	        commandDb.setMessageCount(cmd.getMessageCount());
	        commandDb.setStatus(cmd.getStatus());
	        commandDb.setErrcode(cmd.getStatus());
	        // 更新命令状态
	        masterDbService.procUpdateCommandStatus(commandDb);
		}catch(Exception ex){
			log.error("updateGwSetResult err:"+ex);
		}
    }
    /**
     * 	更新文件传输状态信息
     * @param logicAddr
     * @param tn
     * @param currentMessage
     * @param status
     * @param messageCount
     */
    
    public void updateFileTransferStatus(MasterDbService masterDbService,AsyncService asycService,HostCommand cmd,IMessage msg){
		List<HostCommandResult> results = cmd.getResults();
    	BizRtu rtu=RtuManage.getInstance().getBizRtuInCache(msg.getRtua());
		//国网文件传输请求保存，收到集中器的第一帧确认帧需要修改设置的命令状态。所有确认帧都需要入库用来查询升级状态
		if(results.get(0).getCode().equals("0FF001")){
			for(HostCommandResult hcr:results){
			      UpgradeInfo info = new UpgradeInfo();
			      info.setStatus(hcr.getStatus());
			      info.setBlockCount(hcr.getMessageCount());
			      info.setCurBlockNum(hcr.getCurrentMessage());
			      info.setMaxSize(1024);
			      info.setSoftUpgradeID(hcr.getSoftUpgradeID());
			      asycService.addToDao(info, 4004);
			      //如果是第一帧收到回复 需要更新命令状态
			      if(hcr.getCurrentMessage()==0){
				      //更新命令状态
				        HostCommandDb commandDb=new HostCommandDb();
				        commandDb.setId(cmd.getId());
				        commandDb.setMessageCount(cmd.getMessageCount());
				        commandDb.setStatus(cmd.getStatus());
				        commandDb.setErrcode(cmd.getStatus());
				        // 更新命令状态
				        masterDbService.procUpdateCommandStatus(commandDb);
			      }
			      masterDbService.updateSoftUpgradeByRjsjId(hcr.getSoftUpgradeID(), "01");
			   
			      if(hcr.getCurrentMessage() != hcr.getMessageCount()){
			    	  //如果不是最后一帧，继续发送
				      log.info("Upgrade Return,total Message:"+hcr.getMessageCount()+",Current Message:"+hcr.getCurrentMessage()+",logicAddress:"+rtu.getLogicAddress());
			    	  WebMessageFileTransfer.getInstance().sendNextMessage(hcr.getCurrentMessage(),rtu,0);			    	  
			      }else{
			    	  WebMessageFileTransfer.getInstance().upgradeFinished(rtu);
			    	  log.info("current msg equals total message,so send over,total Message:"+hcr.getMessageCount()+",Current Message:"+hcr.getCurrentMessage()+",logicAddress:"+rtu.getLogicAddress());
			      }
			}
			return;
		}
    }
	
	/*
	 * 更新浙规设置结果
	 * */
	public void updateZjSetResult(MasterDbService masterDbService,HostCommand cmd,IMessage msg) {
		try{
	        List<HostCommandResult> results = cmd.getResults();   
	        String setValue="";
	        if (results != null &&results.size()>=1) {
	        	Map<String,String> tnMap=new HashMap<String,String>();	
				for (HostCommandResult hcr:results){
					setValue="";
					if (tnMap.get(hcr.getTn())==null){
						setValue=hcr.getCode()+":"+hcr.getValue()+",";
						tnMap.put(hcr.getTn(), setValue);    								
					}
					else{
						setValue=tnMap.get(hcr.getTn())+hcr.getCode()+":"+hcr.getValue()+",";
						tnMap.put(hcr.getTn(), setValue);    	
					}
				}
				Iterator it=tnMap.entrySet().iterator();
				while(it.hasNext()){
					Map.Entry<String,String> entry=(Map.Entry<String,String>)it.next();
					setValue=entry.getValue();
					if(setValue.endsWith(","))//消去最后一个","
    					setValue=setValue.substring(0,setValue.length()-1);
					//插入设置结果
	            	HostCommandItemDb item=new HostCommandItemDb(); 
	            	item.setCommandId(cmd.getId());
	            	item.setTn(entry.getKey());
	            	item.setValue(setValue);
	            	item.setTime(new Date());            	
	            	masterDbService.insertCommandSetResult(item);
	            	//浙规和广规对时设置时间成功之后去修改对时结果到TJ_ZDDSSJ表
	            	if("8030:00".equals(setValue)||"8031:00".equals(setValue)){
	        			SaveAutoTimeResult.getInstance().updateAutoTimeResult(masterDbService,entry.getKey(),msg);
	            	}
				}
	        }                
	        // 更新命令状态
	        HostCommandDb commandDb=new HostCommandDb();
	        commandDb.setId(cmd.getId());
	        commandDb.setMessageCount(cmd.getMessageCount());
	        commandDb.setStatus(cmd.getStatus());
	        commandDb.setErrcode(cmd.getStatus());
	        masterDbService.procUpdateCommandStatus(commandDb);
		}catch(Exception ex){
			log.error("updateHostCommand err:"+ex);
		}
    }
	
	/**
	 * 自动装接数据返回方法
	 * @param command
	 * @param rtuId
	 * @param setTag
	 */
	public void sendIntfRequestResponse(HostCommand command,String rtuId,boolean setTag,String rtuType){
		try{
			FaalRequestResponse requestResponse=new FaalRequestResponse();
			requestResponse.setCmdId(command.getId());
			requestResponse.setRtuType(rtuType);
			requestResponse.setRtuId(rtuId);
			requestResponse.setCmdStatus(command.getStatus());
			Map<String,String> params=new HashMap<String,String>();
			List<HostCommandResult> results = command.getResults(); 
			if (results != null && !results.isEmpty()) {
	            for (int i = 0; i < results.size(); i++) {
	            	HostCommandResult result=(HostCommandResult) results.get(i); 
	            	if (result.getValue()!=null){
	            		if (setTag)//设置返回
	            			params.put(result.getCode(),ErrorCode.toHostCommandStatus(Byte.parseByte(result.getValue())));
	            		else//读取返回
	            			params.put(result.getCode(),result.getValue());
	            	}
	            		           	          
	            }
	            requestResponse.setParams(params);	            
	        }  
			MessageWeb msgWeb=encoder.encode(requestResponse);
			if (!sendIntfMsg(msgWeb,Operator.ZDZJ))
				log.info("not find IntfClient to send!");;
		}catch(Exception ex){
			log.error("IntfChannelManage send host message err:"+ex);
		}
	}
	/**
	 * 主站任务轮招返回
	 * @param command
	 * @param rtuId
	 * @param setTag
	 */
	public void sendFaalRequestResponse(HostCommand command,String rtuId,boolean setTag,String rtuType){
		try{
			FaalRequestResponseNew requestResponse=new FaalRequestResponseNew();
			requestResponse.setCmdId(command.getId());
			requestResponse.setRtuType(rtuType);
			requestResponse.setRtuId(rtuId);
			requestResponse.setCmdStatus(command.getStatus());
			List<HostCommandResult> results = command.getResults(); 
			if (results != null && !results.isEmpty()) {
	            for (int i = 0; i < results.size(); i++) {
	            	HostCommandResult result=(HostCommandResult) results.get(i); 
	            	if (result.getValue()!=null){
	            		if (setTag)//设置返回
	            			requestResponse.addDataItem(result.getTn(),result.getCode(),ErrorCode.toHostCommandStatus(Byte.parseByte(result.getValue())));
	            		else//读取返回
	            			requestResponse.addDataItem(result.getTn(),result.getCode(),result.getValue());
	            	}	            		           	          
	            }           
	        }  
			MessageWeb msgWeb=encoder.encode(requestResponse);
			if (!sendIntfMsg(msgWeb,Operator.ZZRW))
				log.info("not find zzrwIntfClient to send!");         	   
		}catch(Exception ex){
			log.error("zzrwIntfChannelManage send host message err:"+ex);
		}
	}
	/**
	 * 主站任务轮招数据采集入库
	 * @param command
	 * @param rtuId
	 * @param setTag
	 */
	public void faalRequestResponseSave(AsyncService asycService,HostCommand command,BizRtu rtu,IMessage msg,int kzm){
		try{
			List<HostCommandResult> results = command.getResults(); 
			MeasuredPoint mp=null;
			boolean tag=false;//中继返回标志
			List<String> codesTemp=new ArrayList<String>();
			String sTaskNum="",sTaskDate="";
			if (results != null && !results.isEmpty()) {				
				TaskMessageHandler taskHandler=new TaskMessageHandler();
				RtuData data=new RtuData();	
				String deptCode=rtu.getDeptCode();
				if (deptCode!=null&&deptCode.length()>=5)//单位代码必须是5位,前两位作为分地市保存的key
					deptCode=deptCode.substring(3,5);
				else{
					log.error("终端"+rtu.getLogicAddress()+"的单位代码"+deptCode+"非法");
					taskHandler.saveErrMsgToDb(msg,rtu.getRtua(),kzm,rtu.getRtuType());
					return;
				}	
	            for (HostCommandResult result:results) {
	            	if (result.getValue()!=null){
	            		if(result.getCode().equals("8902")){//中继抄表返回
	            			mp=rtu.getMeasuredPointByTnAddr(result.getValue());
	            			if (mp!=null){
	            				tag=true;
	            				continue;	 
	            			}
	            			else
	            				return;
	            		}
	            		else{
	            			if(!tag){//非中继返回
	            				if (mp==null)
		            				mp=rtu.getMeasuredPoint(result.getTn());
		            			else{
		            				if (!result.getTn().equals(mp.getTn())){//多个测量点
		            					sTaskNum=rtu.getTaskNum(codesTemp,rtu.getRtuType());	
		            					codesTemp.clear();
		            					if (!sTaskNum.equals("")&&rtu.getRtuTask(sTaskNum)!=null){//找到任务
		            						TaskTemplate tt=rtu.getTaskTemplate(sTaskNum);
		            						sTaskDate=getTaskDate(tt.getUploadIntervalUnit(),tt.getUploadInterval());
		            						if (sTaskDate.length()>0){//任务时间有效
		            							data.setTime(sTaskDate);
			            						taskHandler.dataSave(asycService, rtu, data, mp, msg, rtu.getRtuTask(sTaskNum), 1, deptCode);
		            						}		            						
		            					}
		            					data=new RtuData();		
		            					mp=rtu.getMeasuredPoint(result.getTn());
		            				}
		            			}
	            			}
	            			
	            		}
	            	}
	            	codesTemp.add(result.getCode());
	            	RtuDataItem dataItem=new RtuDataItem();
	            	dataItem.setCode(result.getCode());
        			dataItem.setValue(result.getValue());
        			data.addDataList(dataItem);	            		           	          
	            }   
	            sTaskNum=rtu.getTaskNum(codesTemp,rtu.getRtuType());	
				codesTemp.clear();
				if (sTaskNum!=null&&!sTaskNum.equals("")&&rtu.getRtuTask(sTaskNum)!=null){//找到任务
					TaskTemplate tt=rtu.getTaskTemplate(sTaskNum);
					sTaskDate=getTaskDate(tt.getUploadIntervalUnit(),tt.getUploadInterval());
					if (sTaskDate.length()>0){//任务时间有效
						data.setTime(sTaskDate);						
						if (data.getTime()!=null)
							taskHandler.dataSave(asycService, rtu, data, mp, msg, rtu.getRtuTask(sTaskNum), 1, deptCode);
					}	
				}
				else
					tracer.trace("rtuAdd="+rtu.getLogicAddress()+"主站轮招返回任务匹配失败");
	        }  

		}catch(Exception ex){
			log.error("faalRequestResponseSave  err:"+ex);
		}
	}
	public String getTaskDate(String interval,int number){
		String dateTime="";;
		try{			
			Calendar time= Calendar.getInstance();
			if(interval.equals("05")){//月
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM");
				dateTime=df.format(time.getTime());
			}else if(interval.equals("04")){//日
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
				dateTime=df.format(time.getTime());
			}else if(interval.equals("03")){//时
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH");
				dateTime=df.format(time.getTime())+":00";
			}else if(interval.equals("02")){//分
				if(number==60){//整点
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH");
					dateTime=df.format(time.getTime())+":00";
				}
				else if(number==30){//半点
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
					dateTime=df.format(time.getTime());
					int minute=Integer.parseInt(dateTime.substring(dateTime.length()-2,dateTime.length()));
					dateTime=dateTime.substring(0,dateTime.length()-3);
					if (minute>=0&&minute<30)
						dateTime=dateTime+":00";
					else
						dateTime=dateTime+":30";
				}
				else if(number==15){//15分钟间隔
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
					dateTime=df.format(time.getTime());
					int minute=Integer.parseInt(dateTime.substring(dateTime.length()-2,dateTime.length()));
					dateTime=dateTime.substring(0,dateTime.length()-3);
					if (minute>=0&&minute<15)
						dateTime=dateTime+":00";
					else if (minute>=15&&minute<30)
						dateTime=dateTime+":15";
					else if (minute>=30&&minute<45)
						dateTime=dateTime+":30";
					else if (minute>=45&&minute<60)
						dateTime=dateTime+":45";
				}
				else if(number==5){//5分钟间隔
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
					dateTime=df.format(time.getTime());
					int minute=Integer.parseInt(dateTime.substring(dateTime.length()-2,dateTime.length()));
					dateTime=dateTime.substring(0,dateTime.length()-3);
					if (minute>=0&&minute<5)
						dateTime=dateTime+":00";
					else if (minute>=5&&minute<10)
						dateTime=dateTime+":05";
					else if (minute>=10&&minute<15)
						dateTime=dateTime+":10";
					else if (minute>=15&&minute<20)
						dateTime=dateTime+":15";
					else if (minute>=20&&minute<25)
						dateTime=dateTime+":20";
					else if (minute>=25&&minute<30)
						dateTime=dateTime+":25";
					else if (minute>=30&&minute<35)
						dateTime=dateTime+":30";
					else if (minute>=35&&minute<40)
						dateTime=dateTime+":35";
					else if (minute>=40&&minute<45)
						dateTime=dateTime+":40";
					else if (minute>=45&&minute<50)
						dateTime=dateTime+":45";
					else if (minute>=50&&minute<55)
						dateTime=dateTime+":50";
					else if (minute>=55&&minute<60)
						dateTime=dateTime+":55";
				}
			}
		}catch(Exception ex){
			log.error("getTaskDate err:"+ex+",interval:"+interval+",number:"+number);
		}
		return dateTime;
	}
	/**
	 * 自动装接电表认证返回
	 * @param command
	 * @param rtuId
	 * @param setTag
	 */
	public void sendIntfRequestResponseForAmmeter(MasterDbService masterDbService,HostCommand command,String rtuId,boolean setTag,String rtuType){
		try{
			FaalRequestResponse requestResponse=new FaalRequestResponse();
			requestResponse.setCmdId(command.getId());
			requestResponse.setRtuType(rtuType);
			requestResponse.setRtuId(rtuId);
			requestResponse.setCmdStatus(command.getStatus());
			Map<String,String> params=new HashMap<String,String>();
			List<HostCommandResult> results = command.getResults(); 
			if (results != null && !results.isEmpty()) {
				AmmeterIdentification ai=new AmmeterIdentification();
	            for (int i = 0; i < results.size(); i++) {
	            	HostCommandResult result=(HostCommandResult) results.get(i); 
	            	if (result.getValue()!=null){	            		            			
            			if (result.getCode().equals("8902")){//表地址
            				ai.setZdjh(rtuId);
            				ai.setTxdz(result.getValue());
            			}
            			else if (result.getCode().equals("07000000FF")){//身份认证返回信息
            				ai.setRzxx(result.getValue());
            				if (result.getValue().length()>8)//身份认证成功，返回自动装接值统一2个字节，0000标识成功
            					result.setValue("0000");
            			}            			
            			params.put(result.getCode(),result.getValue());
	            	}	            		           	          
	            }
	            requestResponse.setParams(params);	  
	            masterDbService.insertGwAmmeterIdentification(ai);
	        }  
			MessageWeb msgWeb=encoder.encode(requestResponse);
			if (!sendIntfMsg(msgWeb,Operator.ZDZJ))
				log.info("not find IntfClient to send!");;
		}catch(Exception ex){
			log.error("intfChannelManage send host message err:"+ex);
		}
	}
	/**
	 * 终端对时召测数据返回方法
	 * @param command
	 * @param rtuId
	 */
	public void sendTimeCorrectorRequestResponse(HostCommand command,String rtuId){
		try{
			FaalRequestResponse requestResponse=new FaalRequestResponse();
			requestResponse.setCmdId(command.getId());
			requestResponse.setRtuId(rtuId);
			requestResponse.setCmdStatus(command.getStatus());
			Map<String,String> params=new HashMap<String,String>();
			List<HostCommandResult> results = command.getResults(); 
			if (results != null && !results.isEmpty()) {
	            for (int i = 0; i < results.size(); i++) {
	            	HostCommandResult result=(HostCommandResult) results.get(i); 
	            	if (result.getValue()!=null){
	            		params.put(result.getCode(),result.getValue());
	            	}
	            		           	          
	            }
	            requestResponse.setParams(params);	            
	        }  
			MessageWeb msgWeb=encoder.encode(requestResponse);
			if (!sendIntfMsg(msgWeb,Operator.HTXF_ZDDS))
				log.info("not find timeCorrectorClient to send!");;
		}catch(Exception ex){
			log.error("TimeCorrectorChannelManage send host message err:"+ex);
		}
	}
	public void sendIntfErrorResponse(Long cmdId,String rtuId){
		try{
			FaalRequestResponse requestResponse=new FaalRequestResponse();
			requestResponse.setCmdId(cmdId);
			requestResponse.setRtuId(rtuId);
			requestResponse.setCmdStatus(HostCommand.STATUS_PARSE_ERROR);
			MessageWeb msgWeb=encoder.encode(requestResponse);
			if (!sendIntfMsg(msgWeb,1))
				log.info("not find IntfClient to send!");
		}catch(Exception ex){
			log.error("IntfChannelManage send host message err:"+ex);
		}
	}
	public boolean sendIntfMsg(MessageWeb msgWeb,int clientTag){
		boolean rt=false;
		try{
			TcpSocketServer server=(TcpSocketServer)ApplicationContextUtil.getBean("bp.server.web");
			for(IServerSideChannel client:server.getClients()){
				if (client!=null&&((AsyncSocketClient)client).getIntKey()==clientTag){
					if (client.send(msgWeb)){
						rt=true;
						break;
					}
				}
			}
		}catch(Exception ex){
			log.error("sendIntfMsg err:"+ex);
		}
		return rt;
	}

	/*
	 * 更新招测命令
	 * */
	public void updateCallResult(MasterDbService masterDbService,HostCommand command,String rtuType,String rtuProtocol,IMessage msg,boolean isFinished) {
		try{
	        List<HostCommandResult> results = command.getResults();   
	        HostCommandDb commandDb=new HostCommandDb();
			BizRtu rtu=RtuManage.getInstance().getBizRtuInCache(msg.getRtua());
	        commandDb.setId(command.getId());
	        commandDb.setRtuType(rtuType);
	        commandDb.setMessageCount(command.getMessageCount());
	        commandDb.setStatus(command.getStatus());
	        commandDb.setErrcode(command.getStatus());
	        if (results != null && !results.isEmpty()) {
	            // 插入每条记录
	            for (int i = 0; i < results.size(); i++) {
	            	HostCommandResult result=(HostCommandResult) results.get(i); 
	            	MeasuredPoint mp=rtu.getMeasuredPoint(result.getTn());
	            	String LogicAddress=rtu.getRtuId();
	            	HostCommandItemDb item=new HostCommandItemDb(); 
	            	item.setCommandId(command.getId());
	            	item.setTn(result.getTn());
	            	item.setCode(result.getCode());
	            	item.setValue(result.getValue());
	            	item.setTime(new Date());
	            	item.setProgramTime(result.getProgramTime());
	            	item.setChannel(result.getChannel());	            	
	            	masterDbService.insertCommandCallResult(item);
	            	// 判断是不是召测时钟请求 ，是的话要入库到TJ_ZDDSSJ表中。这里只能通过数据项编码code来确定是不是对时请求
	            	// 国网终端日历时钟编码 是0400121000    浙归8030  广规 集中器8030  表计8031
	            	if("8030".equals(item.getCode())||(Protocol.GG.equals(rtuProtocol)&&"8031".equals(item.getCode()))
	            			||(Protocol.G04.equals(rtuProtocol)&&"0400121000".equals(item.getCode()))){
	            		SaveAutoTimeResult.getInstance().saveAutoTimeResult(result,masterDbService,LogicAddress);
	            	}
	            }
	        }
	        if(isFinished)//如果是最后一帧,才更新命令状态
	        	masterDbService.procUpdateCommandStatus(commandDb);
		}catch(Exception ex){
			log.error("updateCallResult err:"+ex);
		}
    }
	/*
	 * 更新主站设置请求
	 * */
	public void updateParaTable(ManageRtu manageRtu,MasterDbService masterDbService,HostCommand command,BizRtu rtu) {
		// 如果是写参数命令，且命令没有发生通讯失败，则更新参数设置结果
		try{
			List<HostCommandResult> results = command.getResults();       	        
			if (results != null && !results.isEmpty()) {        			       				
	        	for (int i = 0; i < results.size(); i++) {
	        		HostCommandResult item=(HostCommandResult) results.get(i); 
	        		HostParamResult paramResult =new HostParamResult();
	        		paramResult.setRtuId(rtu.getRtuId());
	        		paramResult.setRtuType(rtu.getRtuType());
	            	paramResult.setCode(item.getCode());
	            	paramResult.setTn(item.getTn());
	            	paramResult.setRtuProtocol(rtu.getRtuProtocol());
	            	String value = item.getValue();
	            	if(value.length()>2){	//设置结果有其他数据
	            		if(value.indexOf(":")!=-1){
	            			value = value.substring(0,value.indexOf(":"));
	            		}else{
	            			value=value.substring(0, 2);	            			
	            		}
	            	}
	            	if (ErrorCode.CMD_OK == Byte.parseByte(value)) {//设置成功
	            		paramResult.setStatus((HostCommandItemDb.STATUS_SUCCESS));
	            		paramResult.setSbyy("");
	            		masterDbService.procUpdateParamResult(paramResult);  
	            	}
	            	else{
	            		paramResult.setStatus((HostCommandItemDb.STATUS_FAILED));
	            		paramResult.setSbyy(ErrorCode.toHostCommandStatus(Byte.parseByte(item.getValue())));
	            		masterDbService.procUpdateParamResult(paramResult);  
	            	}	 
	            	                  	                
	            }
	        }
			else{//终端异常返回，不携带数据项，只提供错误代码
				HostParamResult paramResult =new HostParamResult();
        		paramResult.setRtuId(rtu.getRtuId());
        		paramResult.setRtuProtocol(rtu.getRtuProtocol());
        		paramResult.setRtuType(rtu.getRtuType());
            	paramResult.setCode(null);
            	paramResult.setTn(null);
            	paramResult.setStatus((HostCommandItemDb.STATUS_FAILED));
        		paramResult.setSbyy(command.getStatus());
        		masterDbService.procUpdateParamResult(paramResult);   
			}
		}catch(Exception e){
			log.error("updateParaTable err:",e);
		}
	}
    /**
     * 解析请求返回报文失败
     * @param commandId
     */
    private void errorProcessMessage(MasterDbService masterDbService,Long commandId,String rtuType) {   
    	HostCommandDb command = new HostCommandDb();
        command.setId(commandId);
        command.setRtuType(rtuType);
        command.setStatus(HostCommand.STATUS_PARSE_ERROR);
        command.setErrcode(command.getStatus());
        command.setMessageCount(1);
        masterDbService.procUpdateCommandStatus(command);
    }  
}
