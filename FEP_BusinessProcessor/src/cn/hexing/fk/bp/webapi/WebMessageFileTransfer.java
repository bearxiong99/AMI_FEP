package cn.hexing.fk.bp.webapi;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import cn.hexing.db.batch.AsyncService;
import cn.hexing.db.bizprocess.MasterDbService;
import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fas.model.FaalGWAFN0FRequest;
import cn.hexing.fas.model.FaalRequest;
import cn.hexing.fas.model.FaalRequestRtuParam;
import cn.hexing.fas.model.FaalWriteParamsRequest;
import cn.hexing.fas.model.HostCommand;
import cn.hexing.fas.protocol.gw.parse.DataItemCoder;
import cn.hexing.fas.protocol.handler.ProtocolHandler;
import cn.hexing.fas.protocol.handler.ProtocolHandlerFactory;
import cn.hexing.fk.bp.model.HostCommandDb;
import cn.hexing.fk.bp.model.HostCommandItemDb;
import cn.hexing.fk.bp.model.HostParamResult;
import cn.hexing.fk.bp.model.RtuCommandIdInfo;
import cn.hexing.fk.bp.msgqueue.BPMessageQueue;
import cn.hexing.fk.common.events.BasicEventHook;
import cn.hexing.fk.common.simpletimer.ITimerFunctor;
import cn.hexing.fk.common.simpletimer.TimerData;
import cn.hexing.fk.common.simpletimer.TimerScheduler;
import cn.hexing.fk.common.spi.socket.IChannel;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.gate.GateHead;
import cn.hexing.fk.message.gate.MessageGate;
import cn.hexing.fk.message.gw.MessageGw;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.model.Operator;
import cn.hexing.fk.model.RtuManage;
import cn.hexing.fk.model.UpgradeInfo;
import cn.hexing.fk.utils.HexDump;
import cn.hexing.fk.utils.MD5Util;
import cn.hexing.fk.utils.StringUtil;

/**
 * 文件传输请求处理
 * @author Administrator
 *
 */


public class WebMessageFileTransfer extends BasicEventHook {
	private static final String REQUEST = "request";
	private static final String MESSAGES = "messages";
	private static final String CLIENT = "client";
	private static final String RESEND_TIME = "resendTime";
	private static final String CURRENT_MESSAGE = "currentMessage";
	private static final String LAST_SEND_TIME = "lastSendTime";
	private static final Logger log = Logger.getLogger(WebMessageFileTransfer.class);
	private static WebMessageFileTransfer  instance=null;

	public static final WebMessageFileTransfer getInstance() {
		if (null == instance)
			instance = new WebMessageFileTransfer();
		return instance;
	}; 
		
	//可配置属性
	private BPMessageQueue msgQueue;				//spring 配置实现。
	private MasterDbService masterDbService;  	//spring 配置实现。
	private AsyncService asycService;	//spring 配置实现。
	Class<?> messageType= MessageGw.class;		//国网固件升级

    private List<String> sendingList = new LinkedList<String>();
    private long overTime = 15*1000;
    private int resendCount = 3;
	public WebMessageFileTransfer(){
	}
	@Override
	public boolean start() {
		super.start();

		ITimerFunctor overtimeChecker = new ITimerFunctor() {

			@Override
			public void onTimer(int id) {
				synchronized (sendingList) {
					
					int size=sendingList.size();
					for(int i=0;i<size;i++){
						String logicAddress=sendingList.remove(0);
						BizRtu rtu = RtuManage.getInstance().getBizRtuInCache(logicAddress);
						Map<String, Object> upgradeParams = rtu.getUpgradeParams();
						if(rtu==null || upgradeParams==null){
							//没有升级信息
							continue;
						}
						//判断某一帧是否超时
						long lastSendTime = 0;
						try {
							lastSendTime = (Long) upgradeParams.get(LAST_SEND_TIME);
						} catch (Exception e) {
							log.error(StringUtil.getExceptionDetailInfo(e));
						}
						
						if(System.currentTimeMillis()-lastSendTime<overTime){
							//没有超时,继续轮询下一个
							sendingList.add(logicAddress);
							continue;							
						}
						
						//如果超时了,查看当前帧发送了几次
						Integer resendTime = (Integer)upgradeParams.get(RESEND_TIME);
						Integer currentMsg = (Integer) upgradeParams.get(CURRENT_MESSAGE);
						if(resendTime < resendCount){
							//没有超过最大发送次数,继续发送当前帧
							sendNextMessage((Integer) upgradeParams.get(CURRENT_MESSAGE), rtu,resendTime+1);
							log.info("logicAddress:"+logicAddress+",send msg again. send count:"+(resendTime+1));
						}else{
							//超过最大发送次数,保存升级信息
							upgradeUnreturn(rtu,currentMsg);
						}
					}
				}
			}
		};
		TimerScheduler.getScheduler().addTimer(new TimerData(overtimeChecker, 1, 10));

		return true;
	}
	@Override
	public void init(){
		super.init();
	}
	public BPMessageQueue getMsgQueue() {
		return msgQueue;
	}

	public void setMsgQueue(BPMessageQueue msgQueue) {
		this.msgQueue = msgQueue;
	}
	private void upgradeUnreturn(BizRtu rtu,int count) {
		//在补发之后，因为第一帧带有文件信息，如果第一帧不能正确收到，那么升级失败
		FaalGWAFN0FRequest request = (FaalGWAFN0FRequest) rtu.getUpgradeParams().get(REQUEST);
		if(rtu.getFilemessageMap().containsKey(0)){
			updateFileTransferStatus( 0, 255, rtu.getParamFromMessageCountMap(1),request);
			//更新升级记录表，状态03失败
			FaalGWAFN0FRequest req=(FaalGWAFN0FRequest)request;
			masterDbService.updateSoftUpgradeByRjsjId(req.getSoftUpgradeID(), "00");
			log.debug("transfer file fail,because the first message can not be send success");
			return;
		}
		//等待主站补发
		if (request.getOperator() != null && request.getOperator().equals("Reissue")&& request.getOperator().equals("MeterReissue")) {// 对于补发过的则失败
			updateFileTransferStatus(count, 255,rtu.getParamFromMessageCountMap(1), request);
			// 更新升级记录表，状态00失败
			FaalGWAFN0FRequest req = (FaalGWAFN0FRequest) request;
			masterDbService.updateSoftUpgradeByRjsjId(req.getSoftUpgradeID(), "03");
		} else {
			updateFileTransferStatus(count, 4,rtu.getParamFromMessageCountMap(1), request);
			// 更新升级记录表,状态03等待补发
			FaalGWAFN0FRequest req = (FaalGWAFN0FRequest) request;
			masterDbService.updateSoftUpgradeByRjsjId(req.getSoftUpgradeID(), "03");
		}
		log.debug("transfer file fail,because the +" + count+ " th message can not be send success");
	}
    /**
     * 处理文件传输请求
     * @param request
     * @param client
     */
	public void postFileTransferRequest(FaalGWAFN0FRequest request,IChannel client) {
		FaalGWAFN0FRequest req = (FaalGWAFN0FRequest) request;
		log.debug("Receive webRequest, opType: " + request.getOperator()+ ", SoftUpgradeID " + req.getSoftUpgradeID());
		List<FaalRequestRtuParam> rtuParams = req.getRtuParams();
		for (FaalRequestRtuParam rp : rtuParams) {
			BizRtu rtu = (RtuManage.getInstance().getBizRtuInCache(rp.getRtuId()));
			if (rtu == null) {
				throw new MessageEncodeException("can't find rtu:" + rp.getRtuId());
			}
			// 表计升级需要先发送表地址文件，地址文件发送成功之后再发送升级文件
			if ("MeterUpgrade".equals(request.getOperator())||"MeterReissue".equals(request.getOperator())) {
				synchronized (rtu) {
					// 收到主站的表计地址文件传输请求
					if ("FE".equals(req.getFileTag())) {
						rtu.addRequestToMap(1, req);
					} else if ("FD".equals(req.getFileTag())|| "FB".equals(req.getFileTag())) {
						rtu.addRequestToMap(2, req);
					}
					boolean notNull = false;
					notNull = (rtu.getRequestFromMap(1) != null)&& (rtu.getRequestFromMap(2) != null);
					boolean match = false;
					if (notNull) {
						match = (((FaalGWAFN0FRequest) rtu.getRequestFromMap(2))
								.getSoftVersion())
								.equalsIgnoreCase(((FaalGWAFN0FRequest) rtu
										.getRequestFromMap(1)).getSoftVersion());
					}
					if (match && notNull && "MeterUpgrade".equals(request.getOperator())) {
						// 如果已经收到主站文件传输请求中包含有地址文件传输请求和表计升级文件请求，那么先发送地址文件请求
						FaalGWAFN0FRequest meterAddrRequest = (FaalGWAFN0FRequest) rtu
								.getRequestFromMap(1);
						sendRequest(meterAddrRequest, client);
						FileTransferStatus fileTransferStatus = new FileTransferStatus(
								rp.getRtuId(),
								(FaalGWAFN0FRequest) rtu.getRequestFromMap(1),
								(FaalGWAFN0FRequest) rtu.getRequestFromMap(2),
								client);
						fileTransferStatus.start();
					}
				}
				if ("MeterReissue".equals(request.getOperator())) {
					boolean IsnotNull = false;
					IsnotNull = (rtu.getRequestFromMap(1) != null)
							&& (rtu.getRequestFromMap(2) != null);
					if (!IsnotNull) {
						log.error("can't find request from cache. can't reissue");
						masterDbService.updateSoftUpgradeByRjsjId(
								req.getSoftUpgradeID(), "00");
						return;
					}
					FaalGWAFN0FRequest request1 = (FaalGWAFN0FRequest) rtu
							.getRequestFromMap(1);
					FaalGWAFN0FRequest request2 = (FaalGWAFN0FRequest) rtu
							.getRequestFromMap(2);
					masterDbService.updateSoftUpgradeByRjsjId(
							request1.getSoftUpgradeID(), "01");
					masterDbService.updateSoftUpgradeByRjsjId(
							request2.getSoftUpgradeID(), "01");
					// 获取地址文件的传输状态。
					List<UpgradeInfo> infosAddr = masterDbService
							.getUpgradeInfo(request1.getSoftUpgradeID());
					if (infosAddr.size() != 0) {
						UpgradeInfo infoAddr = infosAddr.get(0);
						int addrStatus = infoAddr.getStatus();
						boolean isAgain = true;
						if (11 == addrStatus) {// 地址文件传输成功了，只需要补发升级文件，升级文件请求为request2
							isAgain = false;
							request2.setOperator("Reissue");
							// 如果缓存中找不到总帧数，需要从数据库获取总帧数，并放到request里面，上行从request里面取，这样避免从缓存中不能获取的情况
							List<UpgradeInfo> infosMeter = masterDbService
									.getUpgradeInfo(request2.getSoftUpgradeID());
							if (infosMeter.size() != 0) {// 获取升级文件的传输状态
								UpgradeInfo infoMeters = infosMeter.get(0);
								request2.setTotalMessageCount(infoMeters.getBlockCount());
								request2.setCurrentMessage(infoMeters.getCurBlockNum());
								if (0 == request2.getTotalMessageCount()) {// 这种情况是地址文件发送成功，升级文件的第一帧没有发送成功，所以这里认为需要从新发送升级
									isAgain = true;
								} else {
									sendRequest(request2, client);
								}
							} else {
								log.error("can't find total msg in memory and db,request again.");
							}
						} else if (isAgain) {
							sendRequest(request1, client);
							FileTransferStatus fileTransferStatus = new FileTransferStatus(
									rp.getRtuId(),
									(FaalGWAFN0FRequest) rtu
											.getRequestFromMap(1),
									(FaalGWAFN0FRequest) rtu
											.getRequestFromMap(2), client);
							fileTransferStatus.start();
						}
					} else {
						log.error("can't find total msg in memory and db,request again.");
					}
				}
			} else {// 集中器升级
					// 不带op的为升级请求，带op的集中器补发请求
				if ("Reissue".equals(request.getOperator())) {
					masterDbService.updateSoftUpgradeByRjsjId(
							request.getSoftUpgradeID(), "01");
					FaalGWAFN0FRequest request3 = (FaalGWAFN0FRequest) rtu
							.getRequestFromMap(3);
					if (null == request3) {
						// 对于在缓存中找不到的情况 需要从数据库加载
						log.debug("Can't find upgrade infos in memory,so load from DB.");
						List<UpgradeInfo> infos = masterDbService.getUpgradeInfo(request.getSoftUpgradeID());
						if (infos.size() != 0) {
							request3 = new FaalGWAFN0FRequest();
							UpgradeInfo info = infos.get(0);
							String params = info.getFileHead() + "#"
									+ info.getFtpIp() + "#" + info.getFtpPort()
									+ "#" + info.getFtpUserName() + "#"
									+ info.getFtpPassword() + "#"
									+ info.getFtpDir();
							request3.setCurrentMessage(info.getCurBlockNum());
							request3.setTotalMessageCount(info.getBlockCount());
							request3.setProtocol("02");
							request3.setFileName(info.getFileName());
							request3.setSoftUpgradeID(request.getSoftUpgradeID());
							FaalRequestRtuParam rtuparam = new FaalRequestRtuParam();
							rtuparam.setRtuId(request.getRtuParams().get(0).getRtuId());
							rtuparam.setTn(new int[] { 0 });
							rtuparam.addParam("0FF001", params);
							request3.addRtuParam(rtuparam);
							request3.setFileCommand("00");
							request3.setFileTag("01");
							request3.setTimetag(0);
							request3.setTxfs(0);
							request3.setYhlx("01");
							request3.setType(15);// 12-->0F
						}
					} else {
						// 需要从数据库取出当前第几帧
						List<UpgradeInfo> infos = masterDbService.getUpgradeInfo(request3.getSoftUpgradeID());
						if (infos.size() != 0) {
							UpgradeInfo info = infos.get(0);
							request3.setCurrentMessage(info.getCurBlockNum());
							request3.setTotalMessageCount(info.getBlockCount());
						}
					}
					request3.setOperator("Reissue");
					sendRequest(request3, client);
				} else if ("MeterAddress".equals(request.getOperator())) {
					String param = request.getRtuParams().get(0).getParams()
							.get(0).getValue();
					String MD5 = MD5Util.getMD5String(param);
					String fileHead = "FC" + "00" + "01"
							+ DataItemCoder.constructor("meterAddr", "ASC20")
							+ MD5;
					param = fileHead + "#" + param;
					FaalGWAFN0FRequest request4 = new FaalGWAFN0FRequest();
					request4.setProtocol("02");
					request4.setFileName("meterAddr");
					request4.setSoftUpgradeID(((FaalGWAFN0FRequest) request)
							.getSoftUpgradeID());
					FaalRequestRtuParam rtuparam = new FaalRequestRtuParam();
					rtuparam.setCmdId(request.getRtuParams().get(0).getCmdId());
					rtuparam.setRtuId(request.getRtuParams().get(0).getRtuId());
					rtuparam.setTn(new int[] { 0 });
					rtuparam.addParam("0FF001", param);
					request4.addRtuParam(rtuparam);
					request4.setFileCommand("00");
					request4.setFileTag("FC");
					request4.setTimetag(0);
					request4.setTxfs(0);
					request4.setYhlx("01");
					request4.setType(15);// 12-->0F
					rtu.addRequestToMap(3, request4);
					sendRequest(request4, client);
				} else {
					rtu.addRequestToMap(3, request);
					sendRequest(request, client);
				}

			}
		}
	}
	
	/**
	 * 发送升级文件请求
	 * @param request
	 * @param client
	 */
	public void sendRequest(FaalGWAFN0FRequest request, IChannel client) {
		try {
			BizRtu rtu = (RtuManage.getInstance().getBizRtuInCache(request
					.getRtuParams().get(0).getRtuId()));
			if ("Reissue".equals(request.getOperator())) {
				rtu.addParamToCurrentMessageCountMap(1,
						request.getCurrentMessage());
				rtu.addParamToMessageCountMap(1, request.getTotalMessageCount());
			}

			ProtocolHandlerFactory factory = ProtocolHandlerFactory.getInstance();
			ProtocolHandler handler = factory.getProtocolHandler(messageType);
			IMessage[] messages = handler.createMessage(request);
			if (log.isDebugEnabled()) {
				if (messages == null) {
					log.warn("Encode message error, no message create.");
				} else {
					log.debug("Encode to Message, protocol: "+ request.getProtocol() + ", message count: "+ messages.length);
				}
			}

			Map<String, Object> params = new HashMap<String, Object>();
			params.put(MESSAGES, messages);
			params.put(REQUEST, request);
			params.put(CLIENT, client);
			params.put(RESEND_TIME, 0);
			rtu.setUpgradeParams(params);
			int count = request.getCurrentMessage();
			sendNextMessage(count, rtu,0);
			
		} catch (MessageEncodeException e) {
			// 组帧错误，返回主站错误码
			try {
				if (e.getCode() != null) {// 如果是设置内容非法则回返回设置错误的标识
					List<FaalRequestRtuParam> frps = request.getRtuParams();
					for (FaalRequestRtuParam frp : frps) {
						Long cmdId = frp.getCmdId();
						if (cmdId == 0)// 后台服务设置请求
							errorBackgroundCommand(request, e.getCode());
						else {// 主站请求
							errorHostCommand(cmdId);
						}
					}
				} else {
					log.error(StringUtil.getExceptionDetailInfo(e));
				}
			} catch (Exception ex) {
				log.error("update error host cmd status", ex);
			}
			
			masterDbService.updateSoftUpgradeByRjsjId(request.getSoftUpgradeID(), "00");
		}

	}
    /**
     * 因参数错误，无法组帧的命令
     * @param commandId
     */
    private void errorHostCommand(Long commandId) {   
    	HostCommandDb command = new HostCommandDb();
        command.setId(commandId);
        command.setStatus(HostCommand.STATUS_PRAR_ERROR);
        command.setErrcode(command.getStatus());
        command.setMessageCount(1);
        masterDbService.procUpdateCommandStatus(command);
    } 
    /**
     * 	更新文件传输状态信息
     * @param logicAddr
     * @param tn
     * @param currentMessage
     * @param status
     * @param messageCount
     */
    
    public void updateFileTransferStatus(int currentMessage,int status,int messageCount,FaalRequest request){
      FaalGWAFN0FRequest req=(FaalGWAFN0FRequest)request;
      UpgradeInfo info = new UpgradeInfo();
      info.setStatus(status);
      info.setBlockCount(messageCount);
      info.setCurBlockNum(currentMessage);
      info.setMaxSize(1024);
      info.setSoftUpgradeID(req.getSoftUpgradeID());
      asycService.addToDao(info, 4004);
    	
    }
    /**
     * 文件传输过程中定时查询数据库升级状态
     * 
     */
     class FileTransferStatus implements ITimerFunctor{
    	int meterAddrStatus=1;
    	String logicAddr;
    	FaalGWAFN0FRequest request1;
    	FaalGWAFN0FRequest request2;
    	IChannel client;
    	int count;
    	public FileTransferStatus(String logicAddr,FaalGWAFN0FRequest request1,FaalGWAFN0FRequest request2,IChannel client){
    		this.logicAddr=logicAddr;
    		this.request1=request1;
    		this.request2=request2;
    		this.client=client;
    	}
    	public boolean start(){
    		TimerScheduler.getScheduler().addTimer(new TimerData(this,9,10));
    		return true;
    	}
    	
    	public void stop() {
    		TimerScheduler.getScheduler().removeTimer(this, 9);
    	}
    	
    	@Override
		public void onTimer(int id) {
			if (id == 9) {
				List<UpgradeInfo> infosAddr = masterDbService.getUpgradeInfo(request1.getSoftUpgradeID());
				List<UpgradeInfo> infosMeter = masterDbService.getUpgradeInfo(request2.getSoftUpgradeID());
				if (infosAddr.size() != 0) {
					UpgradeInfo info = infosAddr.get(0);
					meterAddrStatus = info.getStatus();
					if (11 == meterAddrStatus) {
						if ((request1.getSoftUpgradeID() != request2.getSoftUpgradeID())) {
							if (infosMeter != null) {
								UpgradeInfo infoMeter = infosMeter.get(0);
								if (!(2 == infoMeter.getStatus()&& 4 == infoMeter.getStatus())) {
									sendRequest(request2, client);
								}
							}
							stop();
						}
					}
				}
				if (infosMeter.size() != 0) {
					UpgradeInfo info = infosMeter.get(0);
					if (11 == info.getStatus()) {
						BizRtu rtu = (RtuManage.getInstance()
								.getBizRtuInCache(logicAddr));
						rtu.removeRequestFromMap(1);
						rtu.removeRequestFromMap(2);
						stop();
					}
				}
			}
		}
    }
	
    /*
	 * 更新后台设置失败结果
	 * */
	public void errorBackgroundCommand(FaalRequest request,String code) {
		// 如果是写参数命令，且命令没有发生通讯失败，则更新参数设置结果
		try{
			String tn=null;
			if (request instanceof FaalWriteParamsRequest){
				List<FaalRequestRtuParam> frps=request.getRtuParams();	
				for(FaalRequestRtuParam frp:frps){
					HostParamResult paramResult =new HostParamResult();
	        		paramResult.setRtuId((String)frp.getRtuId());
	            	paramResult.setCode(code);	            		            	
            		paramResult.setStatus((HostCommandItemDb.STATUS_FAILED));
            		paramResult.setSbyy(HostCommand.STATUS_PRAR_ERROR);
            		for(int i=0;i<frp.getTn().length;i++){
            			tn=""+frp.getTn()[i];
                		paramResult.setTn(tn);
                		masterDbService.procUpdateParamResult(paramResult); 	 
            		}
				}		
			}									
		}catch(Exception e){
			log.error("error background Command update error:",e);
		}
	}

	public void setMasterDbService(MasterDbService masterDbService) {
		this.masterDbService = masterDbService;
	}
	public void setAsycService(AsyncService asycService) {
		this.asycService = asycService;
	}
	
	public boolean checkReturn(int currentMessage,BizRtu rtu){
		Map<String, Object> params = rtu.getUpgradeParams();
		if(params == null) return false;
		if((Integer) params.get(CURRENT_MESSAGE)+1 != currentMessage){
			log.info("return msg unmatch msg in memory,return num is :"+currentMessage+",num in memory is :"+(Integer) params.get(CURRENT_MESSAGE)+1);
			return false;
		}
		return true;
		
	}
	
	public void sendNextMessage(int currentMessage, BizRtu rtu,int resendTime) {

		Map<String, Object> params = rtu.getUpgradeParams();

		if(params == null){
			throw new RuntimeException("params is null,can't upgrade. logicAddress:"+rtu.getLogicAddress());
		}
		
		params.put(CURRENT_MESSAGE, currentMessage);
		params.put(LAST_SEND_TIME, System.currentTimeMillis());
		params.put(RESEND_TIME, resendTime);
		
		synchronized (sendingList) {
			if(!sendingList.contains(rtu.getLogicAddress()))
				sendingList.add(rtu.getLogicAddress());
		}
		IMessage msg = rtu.getFilemessageMap().get(currentMessage);
		if(msg == null) {
			log.info("can't find this msg:"+currentMessage+",logicAddress:"+rtu.getLogicAddress());
			return ;			
		}
		FaalGWAFN0FRequest request = (FaalGWAFN0FRequest) params.get(REQUEST);
		IChannel client =(IChannel) params.get(CLIENT);
		 //插入终端逻辑地址,帧序号,命令ID进数据库表,用于上行返回对应
        RtuCommandIdInfo rtuCmd=new RtuCommandIdInfo();	
        rtuCmd.setZdzjbz(Operator.DEFAULT);	//主站请求
		MessageGw gwmsg=(MessageGw)msg;
		gwmsg.setSendable();
		gwmsg.setPeerAddr(client.getPeerAddr());
		int rtua=gwmsg.head.rtua;
		int ifseq=masterDbService.getRtuCommandSeq(HexDump.toHex(rtua),rtu.getRtuProtocol());
		gwmsg.setSEQ((byte)ifseq);
		rtuCmd.setLogicAddress(HexDump.toHex(rtua));
		rtuCmd.setZxh(ifseq);
		rtuCmd.setCmdId(gwmsg.getCmdId());
		rtuCmd.setBwsl(gwmsg.getMsgCount());
		if (log.isDebugEnabled())  
        	log.debug("rtu="+rtu.getLogicAddress()+" Framing Success:"+msg.toString());
		MessageGate gateMsg = new MessageGate();
		gateMsg.setDownInnerMessage(msg);
		gateMsg.getHead().setAttribute(GateHead.ATT_DOWN_CHANNEL,request.getTxfs());
		msgQueue.sendMessage(gateMsg);
	}
	public void upgradeFinished(BizRtu rtu) {
		synchronized (sendingList) {
			sendingList.remove(rtu.getLogicAddress());
		}
		rtu.setUpgradeParams(null);
	}
	public void setOverTime(long overTime) {
		this.overTime = overTime*1000;
	}
	public void setResendCount(int resendCount) {
		this.resendCount = resendCount;
	}
}
