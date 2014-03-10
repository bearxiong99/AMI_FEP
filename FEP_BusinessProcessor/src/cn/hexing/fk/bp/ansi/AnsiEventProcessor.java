package cn.hexing.fk.bp.ansi;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import cn.hexing.db.batch.AsyncService;
import cn.hexing.db.bizprocess.MasterDbService;
import cn.hexing.fas.model.AnsiRequest;
import cn.hexing.fas.model.AnsiRequest.ANSI_OP_TYPE;
import cn.hexing.fas.model.FaalRequest;
import cn.hexing.fk.bp.ansi.events.AnsiEvent;
import cn.hexing.fk.bp.ansi.protocol.AnsiProtocolDecoder;
import cn.hexing.fk.bp.ansi.protocol.AnsiProtocolEncoder;
import cn.hexing.fk.bp.ansi.protocol.EncodeServerMessage;
import cn.hexing.fk.bp.ansi.upgrade.AnsiUpgradeAssisant;
import cn.hexing.fk.bp.ansi.upgrade.AnsiUpgradeHandler;
import cn.hexing.fk.bp.model.AlarmData;
import cn.hexing.fk.common.events.BasicEventHook;
import cn.hexing.fk.common.simpletimer.ITimerFunctor;
import cn.hexing.fk.common.simpletimer.TimerData;
import cn.hexing.fk.common.simpletimer.TimerScheduler;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.common.spi.IMessageQueue;
import cn.hexing.fk.common.spi.socket.IChannel;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.gate.MessageGate;
import cn.hexing.fk.model.MeasuredPoint;
import cn.hexing.fk.model.RtuManage;
import cn.hexing.fk.model.UpgradeInfo;
import cn.hexing.fk.tracelog.TraceLog;
import cn.hexing.fk.utils.HexDump;
import cn.hexing.fk.utils.StringUtil;

import com.hx.ansi.ansiElements.AnsiContext;
import com.hx.ansi.ansiElements.AnsiContext.AAState;
import com.hx.ansi.ansiElements.ansiElements.AnsiTableItem;
import com.hx.ansi.ansiElements.ansiElements.UserInformation;
import com.hx.ansi.element.CalledAPTitleElement;
import com.hx.ansi.element.CallingAPTitleElement;
import com.hx.ansi.element.UserInformationElement.EPSEM_CONTROL;
import com.hx.ansi.message.AnsiMessage;
import com.hx.ansi.message.AnsiMessageElement;
import com.hx.ansi.model.AnsiMeterRtu;
import com.hx.ansi.parse.AnsiDataSwitch;

/** 
 * @Description  xxxxx
 * @author  Rolinbor
 * @Copyright 2013 hexing Inc. All rights reserved
 * @time：2013-3-14 下午03:02:23
 * @version 1.0 
 */

public class AnsiEventProcessor extends BasicEventHook{
	private static final Logger log = Logger.getLogger(AnsiEventProcessor.class);
	private static final TraceLog tracer = TraceLog.getTracer("ANSI");
	
	private static final AnsiEventProcessor instance = new AnsiEventProcessor();
	public static final AnsiEventProcessor getInstance(){ return instance; }
	private ANSIContextManager contextManager = LocalAnsiContext.getInstance();
	private LinkedList<AnsiContext> sendingList = new LinkedList<AnsiContext>();
	private MasterDbService masterDbService;  //spring 配置实现。
	private IMessageQueue messageQueue = null;//spring 配置实现。
	private AsyncService asycService;	//spring 配置实现。
	private boolean isCheckSession = false;
	private int maxResend = 1;			//Max re-send counts
	private long sessionTimeout = 60;	//Minutes
	private int resendInterval = 45000;	//re-send interval, milliseconds
	private int relayResendInterval = 45000;	//relay message re-send interval, milliseconds
	
	
	private AnsiEventProcessor(){
		super();
	}
	
	
	public void postWebRequest(FaalRequest webReq, IChannel client) {
		AnsiRequest req = (AnsiRequest)webReq;
		if(req.getTable()==23){
			postReadEnergyRequest(webReq,client);
		}else if(req.getTable()==26||req.getTable()==2050){
			postReadTaskEnergy(webReq,client);
		}else if(req.getTable()==28){
			postReadCurrentRequest(webReq,client);
		}else if(req.getTable()==54){
			postTOURequest(webReq,client);
		}else if(req.getTable()==62){
			postSetLoadProfile(webReq,client);
		}else if(req.getTable()==64){
			postReadLoadProfile(webReq,client);
		}else if(req.getOpType()==ANSI_OP_TYPE.OP_UPGRADE){//软件升级
			if(!req.containsKey("UpgradeId")){
				log.error("when you upgrade,make sure appendparam contains UpgradeId");
				return ;
			}
			if(!AnsiUpgradeHandler.getInstance().upgradeProcessor(req)){
				return ;
			}else {
				postRequest(webReq,client);
			}
		}
		else{
			postRequest(webReq,client);
		}
		
	}
	private void postRequest(FaalRequest webReq, IChannel client){
		AnsiEvent evt = new AnsiEvent(webReq);
		evt.setSource(client);
		postEvent( evt );
	}
	/**
	 * 处理读电量请求
	 * @param webReq
	 * @param client
	 */
	private void postReadEnergyRequest(FaalRequest webReq, IChannel client){
		AnsiRequest req = (AnsiRequest)webReq;
		AnsiContext context = contextManager.getContext(req.getMeterId());
		if(null==context){
			context=new AnsiContext();
			AnsiEvent notOnlineEvent = new AnsiEvent(webReq);
//			contextManager.updateOrSetContext(req.getMeterId(),context);
			handleOffLineState(notOnlineEvent);
			return;
		}
		AnsiTableItem tableItem[]=new AnsiTableItem[6];
		int i=0;
		if(null==context.table0){
			tableItem[i]=new AnsiTableItem();
			tableItem[i].table=0;
			req.messageCount.add(0);
			i++;
		}
		if(null==context.table12){
			tableItem[i]=new AnsiTableItem();
			tableItem[i].table=12;
			req.messageCount.add(12);
			i++;
			}
		if(null==context.table16){
			tableItem[i]=new AnsiTableItem();
			tableItem[i].table=16;
			req.messageCount.add(16);
			i++;
		}
		if(null==context.table21){
			tableItem[i]=new AnsiTableItem();
			tableItem[i].table=21;
			req.messageCount.add(21);
			i++;
		}
		if(null==context.table22){
			tableItem[i]=new AnsiTableItem();
			tableItem[i].table=22;
			req.messageCount.add(22);
			i++;
		}
		tableItem[i]=new AnsiTableItem();
		tableItem[i].table=23;
		req.messageCount.add(23);
		req.setTableItem(tableItem);
		AnsiEvent evt = new AnsiEvent(req);
		evt.setSource(client);
		postEvent(evt);
	}
	/**
	 * 处理读实时量请求
	 * @param webReq
	 * @param client
	 */
	private void postReadCurrentRequest(FaalRequest webReq, IChannel client){
		AnsiRequest req = (AnsiRequest)webReq;
		AnsiContext context = contextManager.getContext(req.getMeterId());
		if(null==context){
			context=new AnsiContext();
			AnsiEvent notOnlineEvent = new AnsiEvent(webReq);
//			contextManager.updateOrSetContext(req.getMeterId(),context);
			handleOffLineState(notOnlineEvent);
			return;
		}
		AnsiTableItem tableItem[]=new AnsiTableItem[6];
		int i=0;
		if(null==context.table0){
			tableItem[i]=new AnsiTableItem();
			tableItem[i].table=0;
			req.messageCount.add(0);
			i++;
		}
		if(null==context.table12){
			tableItem[i]=new AnsiTableItem();
			tableItem[i].table=12;
			req.messageCount.add(12);
			i++;
			}
		if(null==context.table16){
			tableItem[i]=new AnsiTableItem();
			tableItem[i].table=16;
			req.messageCount.add(16);
			i++;
		}
		if(null==context.table21){
			tableItem[i]=new AnsiTableItem();
			tableItem[i].table=21;
			req.messageCount.add(21);
			i++;
		}
		if(null==context.table27){
			tableItem[i]=new AnsiTableItem();
			tableItem[i].table=27;
			req.messageCount.add(27);
			i++;
		}
		tableItem[i]=new AnsiTableItem();
		tableItem[i].table=28;
		req.messageCount.add(28);
		req.setTableItem(tableItem);
		AnsiEvent evt = new AnsiEvent(req);
		evt.setSource(client);
		postEvent(evt);
	}
	/**
	 * 处理设置负荷任务请求
	 * @param webReq
	 * @param client
	 */
	private void postSetLoadProfile(FaalRequest webReq, IChannel client){
		AnsiRequest req = (AnsiRequest)webReq;
		AnsiContext context = contextManager.getContext(req.getMeterId());
		if(null==context){
			context=new AnsiContext();
			AnsiEvent notOnlineEvent = new AnsiEvent(webReq);
//			contextManager.updateOrSetContext(req.getMeterId(),context);
			handleOffLineState(notOnlineEvent);
			return;
		}
		AnsiTableItem tableItem[]=new AnsiTableItem[5];
		int i=0;
		if(null==context.table0){
			tableItem[i]=new AnsiTableItem();
			tableItem[i].table=0;
			req.messageCount.add(0);
			i++;
		}
		if(null==context.table12){
			tableItem[i]=new AnsiTableItem();
			tableItem[i].table=12;
			req.messageCount.add(12);
			i++;
		}
		//表计不提供负荷记录的通道数和记录间隔设置。
//		tableItem[i]=new AnsiTableItem();
//		tableItem[i].table=61;
//		req.messageCount.add(61);
//		i++;
		tableItem[i]=new AnsiTableItem();
		tableItem[i].table=62;
		req.messageCount.add(62);
		i++;
		tableItem[i]=new AnsiTableItem();
		tableItem[i].table=7;
		req.messageCount.add(7);
		req.setTableItem(tableItem);
		AnsiEvent evt = new AnsiEvent(req);
		evt.setSource(client);
		postEvent(evt);
	}
	/**
	 * 处理读取负荷数据请求
	 * @param webReq
	 * @param client
	 */
	private void postReadLoadProfile(FaalRequest webReq, IChannel client){
		AnsiRequest req = (AnsiRequest)webReq;
		AnsiContext context = contextManager.getContext(req.getMeterId());
		if(null==context){
			context=new AnsiContext();
			AnsiEvent notOnlineEvent = new AnsiEvent(webReq);
//			contextManager.updateOrSetContext(req.getMeterId(),context);
			handleOffLineState(notOnlineEvent);
			return;
		}
		AnsiTableItem tableItem[]=new AnsiTableItem[6];
		int i=0;
		if(null==context.table0){
			tableItem[i]=new AnsiTableItem();
			tableItem[i].table=0;
			req.messageCount.add(0);
			i++;
		}
		if(null==context.table12){
			tableItem[i]=new AnsiTableItem();
			tableItem[i].table=12;
			req.messageCount.add(12);
			i++;
		}
		tableItem[i]=new AnsiTableItem();
		tableItem[i].table=61;
		req.messageCount.add(61);
		i++;
		tableItem[i]=new AnsiTableItem();
		tableItem[i].table=62;
		req.messageCount.add(62);
		i++;
//		tableItem[i]=new AnsiTableItem();
//		tableItem[i].table=63;
//		context.messageCount.add(63);
//		i++;
		tableItem[i]=new AnsiTableItem();
		tableItem[i].table=64;
		tableItem[i].readLatestDate=true;
		req.messageCount.add(64);
		i++;
		
		tableItem[i]=new AnsiTableItem();
		tableItem[i].table=64;
		tableItem[i].readLoadProfileData=true;
		req.messageCount.add(64);
		
		req.setTableItem(tableItem);
		AnsiEvent evt = new AnsiEvent(req);
		evt.setSource(client);
		postEvent(evt);
	}
	/**
	 * 处理读结算数据请求
	 * @param webReq
	 * @param client
	 */
	private void postReadTaskEnergy(FaalRequest webReq, IChannel client){

		AnsiRequest req = (AnsiRequest)webReq;
		AnsiContext context = contextManager.getContext(req.getMeterId());
		if(null==context){
			context=new AnsiContext();
			AnsiEvent notOnlineEvent = new AnsiEvent(webReq);
//			contextManager.updateOrSetContext(req.getMeterId(),context);
			handleOffLineState(notOnlineEvent);
			return;
		}
		AnsiTableItem tableItem[]=new AnsiTableItem[6];
		int i=0;
		if(null==context.table0){
			tableItem[i]=new AnsiTableItem();
			tableItem[i].table=0;
			req.messageCount.add(0);
			i++;
		}
		if(null==context.table12){
			tableItem[i]=new AnsiTableItem();
			tableItem[i].table=12;
			req.messageCount.add(12);
			i++;
			}
		if(null==context.table16){
			tableItem[i]=new AnsiTableItem();
			tableItem[i].table=16;
			req.messageCount.add(16);
			i++;
		}
		if(null==context.table21){
			tableItem[i]=new AnsiTableItem();
			tableItem[i].table=21;
			req.messageCount.add(21);
			i++;
		}
		if(null==context.table22){
			tableItem[i]=new AnsiTableItem();
			tableItem[i].table=22;
			req.messageCount.add(22);
			i++;
		}
		tableItem[i]=new AnsiTableItem();
		tableItem[i].table=req.getTable();
		req.messageCount.add(req.getTable());
		req.setTableItem(tableItem);
		AnsiEvent evt = new AnsiEvent(req);
		evt.setSource(client);
		postEvent(evt);
	}
	/**
	 * 处理TOU请求
	 * @param webReq
	 * @param client
	 */
	private void postTOURequest(FaalRequest webReq,IChannel client){

		AnsiRequest req = (AnsiRequest)webReq;
		AnsiContext context = contextManager.getContext(req.getMeterId());
		if(null==context){
			context=new AnsiContext();
			AnsiEvent notOnlineEvent = new AnsiEvent(webReq);
//			contextManager.updateOrSetContext(req.getMeterId(),context);
			handleOffLineState(notOnlineEvent);
			return;
		}
		switch(req.getOpType()){
		case OP_READ:
			postReadTOU(context,req,client);
			break;
		case OP_WRITE:
			postSetTOU(context,req,client);
			break;
		default:
			log.error("unsuport OP,OP="+req.getOpType());
			break;
		}
	}
	/**
	 * 设置TOU
	 * @param req
	 * @param client
	 */
	private void postSetTOU(AnsiContext context,AnsiRequest req,IChannel client){
		AnsiTableItem tableItem[]=new AnsiTableItem[2];
		int i=0;
//		if(null==context.table51){
//			tableItem[i]=new AnsiTableItem();
//			tableItem[i].table=51;
//			req.messageCount.add(51);
//			i++;
//			}
		tableItem[i]=new AnsiTableItem();
		tableItem[i].table=54;
		req.messageCount.add(54);
		req.setTableItem(tableItem);
		AnsiEvent evt = new AnsiEvent(req);
		evt.setSource(client);
		postEvent(evt);
	}
	/**
	 * 读取TOU
	 * @param req
	 * @param client
	 */
	private void postReadTOU(AnsiContext context,AnsiRequest req,IChannel client){
		
		AnsiTableItem tableItem[]=new AnsiTableItem[4];
		int i=0;
		if(null==context.table0){
			tableItem[i]=new AnsiTableItem();
			tableItem[i].table=0;
			req.messageCount.add(0);
			i++;
		}
		if(null==context.table51){
			tableItem[i]=new AnsiTableItem();
			tableItem[i].table=51;
			req.messageCount.add(51);
			i++;
			}
		tableItem[i]=new AnsiTableItem();
		tableItem[i].table=52;
		req.messageCount.add(52);
		i++;
		tableItem[i]=new AnsiTableItem();
		tableItem[i].table=54;
		req.messageCount.add(54);
		req.setTableItem(tableItem);
		AnsiEvent evt = new AnsiEvent(req);
		evt.setSource(client);
		postEvent(evt);
	}
	@Override
	public void handleEvent(IEvent event) {
		try{
			if( event instanceof AnsiEvent ){
				AnsiEvent evt = (AnsiEvent)event;
				switch(evt.eventType()){
				case UNDEF:
					break;
				case UP_MSG:
					handleUpMessage(evt);
					break;
				case WEB_REQ:
					handleWebReq(evt);
					break;
				case LOGON:
					handleLogon(evt);
					break;
				case LOGOFF:
					handleLogoff(evt);
					break;
				case TERMINATE:
					handleTerminate(evt);
					break;
				case WAIT:
					handleWait();
					break;
				case DISCONNECT:
					handleDisconnect();
					break;
				default:
					break;
				}
			}
			else{
				super.handleEvent(event);
			}
		}catch(Exception exp){
			
		}
	}
	
	@Override
	public void init(){
		if( ! initialized ){
			sessionTimeout = sessionTimeout * 60 * 1000;
			AnsiProtocolDecoder.getInstance().setEventProcessor(this);
			if( resendInterval > 500 ){
				ITimerFunctor secondTimer = new ITimerFunctor(){
					@Override
					public void onTimer(int id) {
						AnsiContext cxt;
						synchronized( sendingList ){
							while( sendingList.size()>0 ){
								cxt = sendingList.getFirst();
								if( cxt.curDownMessage == null ){
									sendingList.removeFirst();
									continue;
								}
								long interval = 0;
								//这里应该加一个最大超时时间，否则设置太长的话，会出问题。
								if(cxt.webReqList.size()==0){
									interval = cxt.isRelay ?  relayResendInterval : resendInterval;
								}else{
									AnsiEvent de = (AnsiEvent) cxt.webReqList.get(0);
									AnsiRequest dr=(AnsiRequest) de.getRequest();
									//long maxInterval = cxt.isRelay? maxRelayResendInterval:maxResendInterval;
									interval= cxt.isRelay ?  relayResendInterval : resendInterval;
//									interval =dr.getRequestTimeOut()==0?( cxt.isRelay ?  relayResendInterval : resendInterval):dr.getRequestTimeOut();
								}

								if( System.currentTimeMillis()- cxt.lastSendTime < interval )
									break;
								cxt = sendingList.removeFirst();
								
								//AnsiEvent evt = (AnsiEvent)context.webReqList.get(0);
								
								if( ++ cxt.resendCount >= maxResend ){
									//Send-failed
									try {
										
										if(((AnsiRequest)((AnsiEvent)cxt.webReqList.get(0)).getRequest()).containsKey("MeterUpgrade") && cxt.resendCount<3){
											log.info("send upgrade msg again... send time:"+cxt.resendCount+",meterId:"+cxt.getMeterId()+"\n msg:"+cxt.curDownMessage);
											sendMessage(cxt.curDownMessage,cxt);
										}
										else {
											onRequestFailed(cxt);
										}
										
									} catch (IOException e) {
										log.warn(e.getLocalizedMessage(),e);
									}
								}
								else{
									cxt.waitReply.set(false);
									//log.info("send msg again... send time:"+cxt.resendCount+",meterId:"+cxt.getMeterId()+"\n msg:"+cxt.curDownMessage);
									//sendMessage(cxt.curDownMessage,cxt);
								}
							}
						}
					}
				};
				TimerScheduler.getScheduler().addTimer(new TimerData(secondTimer,1,2));
			}
			super.init();
		}
	}

	private void handleDisconnect() {
		// TODO Auto-generated method stub
		
	}

	private void handleWait() {
		// TODO Auto-generated method stub
		
	}

	private void handleTerminate(AnsiEvent evt) throws IOException{
		//判断是否在会话状态
		String meterAddr=evt.getAnsiRequest().getMeterId();
		AnsiContext context = contextManager.getContext(meterAddr);
		if(null==context){
			context=new AnsiContext();
//			contextManager.updateOrSetContext(meterAddr,context);
			handleOffLineState(evt);
			return;
		}
		String userInformation="21";
		if(null==context.peerAddr||null==context.meterId){
			handleOffLineState(evt);
			log.debug("找不到对应的表计信息");
		}
		AnsiMessage msg = EncodeServerMessage.getInstance().encodeTerminateMessage(context, userInformation);
		msg.setPeerAddr(context.peerAddr);
		msg.setLogicalAddress(context.meterId);
		sendMessage(msg,context);
//		context.webReqList.add(evt);
//		if(context.aaState==AAState.SESSION){
//			if(!checkContextTimeout(context)){
//				sendNextMessage(context);
//				return;
//			}
//		}
//		if(context.aaState == AAState.IDLE || checkContextTimeout(context)){
//			handleOffLineState(evt);
//			return;
			//添加一个logon Event
//			AnsiEvent logon =new AnsiEvent();
//			logon.setEventType(ANSIEventType.LOGON);
//			context.webReqList.add(0,logon);
//			context.webReqList.add(1,evt);
//			handleLogon(evt);
//		}
		
	}

	private void handleLogoff(AnsiEvent evt) {
		// TODO Auto-generated method stub
		
	}
	/**
	 * 登陆
	 * @param evt
	 * @throws  
	 */
	private void handleLogon(AnsiEvent evt) throws IOException {
		
		String meterAddr=evt.getAnsiRequest().getMeterId();
		AnsiContext context = contextManager.getContext(meterAddr);
		if(null==context){
			 context=new AnsiContext();
//			contextManager.updateOrSetContext(meterAddr,context);
			handleOffLineState(evt);
			return;
		}
		
		UserInformation ui=new UserInformation();
//		ui=masterDbService.getUserInformationByMeterAddr(meterAddr);
//		String userInformation=DataItemCoder.constructor(ui.getUserID(),"HTB4")
//									+DataItemCoder.constructor(ui.getUserName(),"ASC10");//ID:0001 user:"USERNAME"
		String userInformation="50"+"000155534552204E414D4520"+"003C";//ID:0001 user:"USERNAME"
		setEPSEM_CONTROL(ui.getSecurityMode(),context);//设置加密类型,从数据库获取
		if(null==context.peerAddr||null==context.meterId){
			handleOffLineState(evt);
			log.debug("can't find meter...");
		}
		AnsiMessage msg = EncodeServerMessage.getInstance().encodeLogonMessage(context, userInformation);
		msg.setPeerAddr(context.peerAddr);
		msg.setLogicalAddress(context.meterId);
		sendMessage(msg,context);
	}

	private void handleWebReq(AnsiEvent evt) throws IOException {
		//判断是否在会话状态
		String meterAddr=evt.getAnsiRequest().getMeterId();
		AnsiContext context = contextManager.getContext(meterAddr);
		if(null==context){
			context=new AnsiContext();
//			contextManager.updateOrSetContext(meterAddr,context);
			handleOffLineState(evt);
			return;
		}
		context.webReqList.add(evt);
		//测试：
		context.aaState=AAState.SESSION;
		if(context.aaState==AAState.SESSION){
			if(!checkContextTimeout(context)){
				sendNextMessage(context);
				return;
			}
		}
		if(context.aaState == AAState.IDLE || checkContextTimeout(context)){
			handleOffLineState(evt);
			return;
			//添加一个logon Event
//			AnsiEvent logon =new AnsiEvent();
//			logon.setEventType(ANSIEventType.LOGON);
//			context.webReqList.add(0,logon);
//			context.webReqList.add(1,evt);
//			handleLogon(evt);
		}
	}
	/**
	 * sendnextMessage
	 * @param context
	 * @throws IOException 
	 */
	public void sendNextMessage(AnsiContext context) throws IOException {
		if(context.webReqList.size()<=0) 
			return;
//		if( context.waitReply.compareAndSet(false, true) ){
			AnsiEvent evt = (AnsiEvent)context.webReqList.get(0);
			
			if( context.reqDownMessages.size() == 0  ){
				if(context.webReqList.size()>0 ){
					//Build ANoSI messages for this request event.
					try {
						AnsiProtocolEncoder.getInstance().build(evt.getRequest(), context);
					} catch (Exception e) {
						onRequestFailed(context);
						log.error("sendNextMessage occur exception.Build frame error!",e);
					}
				}
			}
			if( context.reqDownMessages.size()>0 ){
				if(context.waitReply.compareAndSet(false, false)){
					IMessage msg = context.reqDownMessages.remove(0);//这里主站下发一个请求，但是在读取数据的时候可能要分多个请求下发，
																	//所以这里reqDownMessages的前面n个message可能就是对应主站的一个请求，
																	//要发送完reqDownMessages里面的对应请求的消息
					
					if( sendMessage(msg,context)){
						
						context.waitReply.set(true);
					}
					else {
						context.waitReply.set(false);
					}
				}
			}
			else{
				context.waitReply.set(false);
			}
//		}
	}


	public void onRequestFailed(AnsiContext context)throws IOException {
		//First, remove the completed request.
		log.info("request failed.");
		AnsiEvent evt = null;
		if( context.webReqList.size()>0 )
			evt = (AnsiEvent)context.webReqList.remove(0);
		int resendCount = context.resendCount;
		if(context.aaState!=AAState.SESSION){
			//request failed then reset aastate
			context.aaState = AAState.IDLE;
		}
		context.onRequestFinished();
		
		//Second, send next message to METER if possible.
		try {
			sendNextMessage(context);
		} catch (IOException exp) {
			log.warn("onRequestComplete. sendNextMessage:",exp);
		}
		
		if( null == evt ){
			return;
		}
		AnsiRequest req = evt.getAnsiRequest();
		if(null!=req.getOperator()&&req.getOperator().contains("UPGRADE")){
			AnsiUpgradeAssisant.getInstance().updateUpgradeInfo(req, UpgradeInfo.UPGRADE_PAUSE);//设置为等待补发
			AnsiUpgradeAssisant.getInstance().updateUpgradeStatus(req,"03");
			return;
		}
	}


	private boolean checkContextTimeout(AnsiContext context) {
		boolean timeout = false;
		if( isCheckSession && !context.isRelay && context.lastSendTime !=0 && Math.abs(System.currentTimeMillis()-context.lastSendTime)> this.sessionTimeout){
			//Session Timeout
			context.reset();
			timeout = true;
		}
		return timeout;
	}


	private void handleOffLineState(AnsiEvent evt) {
		String meterAddr=evt.getAnsiRequest().getMeterId();
		String peerIp = evt.getAnsiRequest().getPeerIp();
		log.warn("Not online: meter="+meterAddr+", peerId="+peerIp);
		//The best way is to reply WEB with not-online.
		//TODO: save error code
	}

	public void postUpAnsiMessage(AnsiMessage msg){
		AnsiEvent evt = new AnsiEvent(msg);
		if( !msg.isHeartbeat() && tracer.isEnabled() ){
			StringBuilder sb = new StringBuilder();
			sb.append("<<uplink meterid="+msg.getLogicalAddress()+", peerAddr="+msg.getPeerAddr()+", msg="+msg );
			tracer.trace(sb.toString());
		}
		evt.setSource(msg.getSource());
		postEvent( evt );
	}
	
	/**
	 * 处理终端上行消息
	 * @param evt
	 * @throws IOException 
	 */
	private void handleUpMessage(AnsiEvent evt) throws IOException {
		AnsiMessage msg = evt.upMessage();
		/* 上行消息网关会通过链接的peerAddr来获取消息的context，context里面带有logicalAddress。此处是根据通信的过程中的A6来获取peerAddr
		          或者上行upMessage经过网关处理，里面带有peerAddr，可以直接从message里面获取peerAddr。
		String logicalAddress = evt.getMessage().getLogicalAddress();
		String peerAddr=evt.getMessage().getPeerAddr();*/
		ByteBuffer apdu = msg.getApdu();
		String data=HexDump.hexDumpCompact(apdu);
		AnsiMessageElement ame=new AnsiMessageElement();
		ame.decodeMessage(data);
		AnsiContext context=contextManager.getContext(msg.getLogicalAddress());//从报文中获取表计地址
		if(null==context){
			 context=new AnsiContext();
			contextManager.updateOrSetContext(msg.getLogicalAddress(),context);//如果不能从ame中获取表号，那么从msg里面获取。
		}
		//如果上行是心跳或者登陆消息，直接下发主站回复。
		if(ame.getServerTag()!=null&&(ame.getServerTag().equalsIgnoreCase("FF")||ame.getServerTag().equalsIgnoreCase("FE"))){
			creatRepaly(ame,context,msg);
			return;
		}
		if(ame.getCallingInvocId()>127){
			try {
				handleAlarmMessage(context,ame,msg);
			} catch (ParseException e) {
				log.error(StringUtil.getExceptionDetailInfo(e));
			}
			return;
		}
//		UserInformationElement userInformation=new UserInformationElement();
//		String userData=userInformation.decode(context,ame.getUserData());//解析数据区，加密的话先解密
		//InvocationId 暂时不考虑。如果下发和收到的一致再加入判断----->calledAPInvocationId.getCalledAPInvocationId()
		AnsiProtocolDecoder.getInstance().handleUpMessage(context,ame.getUserData(),ame.getSEQ(),ame.getCalledInvocId(),ame.getcalledTotalMessage(),ame.getA4());
	}
	/**
	 * 告警处理
	 * @param context
	 * @param ame
	 * @param msg
	 * @throws ParseException
	 */
	private void handleAlarmMessage(AnsiContext context,AnsiMessageElement ame,AnsiMessage msg) throws ParseException{
		//BE 1A 28 18 81 16 80 14 40 00 07 00 0F 01 0D 00 
		//0D 01 1C 0E 14 15
		//00 02 00 00 00 64
		//BE 1A 28 18 81 16 80 14 40 00 07 00 0F 01 0D 00 0D011C0E141500020000006
		AnsiMeterRtu  meterRtu = RtuManage.getInstance().getAnsiMeterRtuById(context.meterId);
		MeasuredPoint mp=meterRtu.getMeasuredPoint("0");
		String dataSaveId = mp.getDataSaveID();
		String alarmTime=ame.getUserData().substring(32, 44);
		alarmTime=AnsiDataSwitch.hexToString(alarmTime);
		String alarmType=ame.getUserData().substring(44, 46);//00-表计告警   01-模块告警
		String alarmCode1=ame.getUserData().substring(46, 50);
		String alarmCode2=ame.getUserData().substring(50, 54);
		SimpleDateFormat sdf=new SimpleDateFormat("yyMMddHHmmss");
		Date date=sdf.parse(alarmTime);
		creatAlarmRepaly(context,ame.getA8());
		if(!alarmCode2.equals("0000")){
			Calendar calendar = Calendar.getInstance(); 
			calendar.setTime(date);
			calendar.add(Calendar.SECOND, +10); 
			AlarmData alarmData2=new AlarmData();	
			alarmData2.setDataSaveID(new Long(dataSaveId));
			alarmData2.setAlertTime(calendar.getTime());
			alarmData2.setAlertCodeHex("06"+AnsiDataSwitch.ReverseStringByByte(alarmCode2));
			alarmData2.setReceiveTime(new Date());
			asycService.addToDao(alarmData2, 4000);
		}
		AlarmData alarmData1=new AlarmData();	
		alarmData1.setDataSaveID(new Long(dataSaveId));
		alarmData1.setAlertTime(date);
		alarmData1.setAlertCodeHex("06"+AnsiDataSwitch.ReverseStringByByte(alarmCode1));
		alarmData1.setReceiveTime(new Date());
		asycService.addToDao(alarmData1, 4000);
	}
	
	private void creatAlarmRepaly(AnsiContext context,String A8){
		String apdu="600E"+A8+"BE0728058103800100";
		AnsiMessage nextmsg=new AnsiMessage();
		nextmsg.setApdu(HexDump.toArray(apdu));
		nextmsg.setPeerAddr(context.peerAddr);
		nextmsg.setLogicalAddress(context.meterId);
//		sendMessage(nextmsg, context);
		MessageGate gate = new MessageGate();
		gate.setDownInnerMessage(nextmsg);
		if( null != nextmsg.getStatus() )
			gate.setStatus(nextmsg.getStatus());
		this.messageQueue.sendMessage(gate);
	}
	
	/**
	 * 终端登陆或者心跳 主站回复。
	 * @param ame
	 * @param context
	 */
	private void creatRepaly(AnsiMessageElement ame,AnsiContext context,AnsiMessage msg){
			if(null==context.acseTitle){//如果context里面没有带有acseTitle的信息，则要组一个acseTitle
				CalledAPTitleElement calledAPTitle=new CalledAPTitleElement();
				calledAPTitle.setPeerAddr(msg.getPeerAddr());
				calledAPTitle.encode();
				CallingAPTitleElement callingAPTitle=new CallingAPTitleElement();
				callingAPTitle.setPeerAddr(msg.getServerAddress());
				callingAPTitle.encode();
				String apduTitle=calledAPTitle.getCalledAPTitleElement()+callingAPTitle.getCallingAPTitleElement();
				context.acseTitle=apduTitle;
				}
			context.peerAddr=msg.getPeerAddr();
			context.localAddr=msg.getServerAddress();
			context.meterId=ame.getMeterAddr();
			context.aaState=AAState.SESSION;
	}
	
	//统一消息下发送队列
	public boolean sendMessage(IMessage msg,AnsiContext cxt){
		
		cxt.curDownMessage = msg;
		cxt.lastSendTime = System.currentTimeMillis();
		
		synchronized(sendingList){
			sendingList.addLast(cxt);
		}
		if( msg instanceof MessageGate )
			return messageQueue.sendMessage(msg);
		else{
			MessageGate gate = new MessageGate();
			gate.setDownInnerMessage(msg);
			if( null != msg.getStatus() )
				gate.setStatus(msg.getStatus());
			boolean result = this.messageQueue.sendMessage(gate);
			return result;
		}
	}
	
	public void onRequestComplete(AnsiContext context)throws IOException{

		 //First, remove the completed request.
		AnsiEvent evt = null;
		synchronized (sendingList) {
			if(sendingList.size()>0 && context == sendingList.get(0)){
				sendingList.removeFirst();
			}			
		}
		if( context.webReqList.size()>0 )
			evt = (AnsiEvent)context.webReqList.remove(0);
		context.onRequestFinished();
		log.info("request finished,meterId:"+context.getMeterId()+",remaing sendlist size:"+context.webReqList.size());
		
		
		//Second, send next message to METER if possible.
		if( null == evt ){
			return;
		}
		
		AnsiRequest req = evt.getAnsiRequest();
		
		if(null!=req.getOperator()&&req.getOperator().contains("UPGRADE")){
			AnsiUpgradeHandler.getInstance().handleUpgrade(this, req,context);
			return;
		}
		
		try {
			sendNextMessage(context);
		} catch (IOException exp) {
			log.warn("onRequestComplete. sendNextMessage:",exp);
		}
		
//		if(!MasterDbServiceAssistantance.getInstance().operationRequest(context, req)) return;
		IChannel channel = (IChannel)evt.getSource();
		MessageGate mgate = new MessageGate();
		mgate.setDataObject(req);
		mgate.getHead().setCommand(MessageGate.FE_MASTER_REP);
		if( ! channel.send(mgate) ){
			log.error("Send reply to WEB failed.");
		}
	}
	
	//设置加密方式
	public  void setEPSEM_CONTROL(int i,AnsiContext context){
		switch(i){
		case 0:
			context.epsem_control = EPSEM_CONTROL.NO_SECURITY;
			break;
		case 1:
			context.epsem_control = EPSEM_CONTROL.SECURITY_MODE_1;
			break;
		case 2:
			context.epsem_control = EPSEM_CONTROL.SECURITY_MODE_2;
			break;
		default:
			context.epsem_control = EPSEM_CONTROL.NO_SECURITY;
			break;
		}
	}
	public String  parseInt2HexString(int i){
		String ss=Integer.toHexString(i);
		if(1==(ss.length()%2)){
			ss=0+ss;
		 }
		return ss;
	}
	
	public final void setMessageQueue(IMessageQueue bpMessageQueue) {
		this.messageQueue = bpMessageQueue;
	}
	
	public void setMasterDbService(MasterDbService masterDbService) {
		this.masterDbService = masterDbService;
	}
	public MasterDbService getMasterDbService() {
		return masterDbService;
	}
	public AsyncService getAsycService() {
		return asycService;
	}
	public void setAsycService(AsyncService asycService) {
		this.asycService = asycService;
	}
	
}

