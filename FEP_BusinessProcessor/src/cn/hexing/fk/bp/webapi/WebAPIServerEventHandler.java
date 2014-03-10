/**
 * WEB应用接口Socket服务器的事件处理器
 * 功能概述：
 * 		侦听收到Web应用下行报文、上行报文发送成功事件。
 *      Web下行报文通过BPMessageQueue对象方法直接发送给通信前置机；
 * 技术实现：
 * BasicEventHook派生类。
 * override handleEvent方法，针对ReceiveMessageEvent和SendMessageEvent特别处理。
 * 注意事项：在spring配置文件中，source对象必须是WEB应用接口Socket服务器的SocketServer对象。
 */
package cn.hexing.fk.bp.webapi;


import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import cn.hexing.db.bizprocess.MasterDbService;
import cn.hexing.db.managertu.ManageRtu;
import cn.hexing.exception.MessageDecodeException;
import cn.hexing.exception.MessageEncodeException;
import cn.hexing.exception.ProtocolHandleException;
import cn.hexing.fas.framework.message.MessageGg;
import cn.hexing.fas.model.AnsiRequest;
import cn.hexing.fas.model.FaalGGKZM11Request;
import cn.hexing.fas.model.FaalGGKZM12Request;
import cn.hexing.fas.model.FaalGWAFN0FRequest;
import cn.hexing.fas.model.FaalGWAFN10Request;
import cn.hexing.fas.model.FaalGWupdateKeyRequest;
import cn.hexing.fas.model.FaalReadTaskDataRequest;
import cn.hexing.fas.model.FaalRefreshCacheRequest;
import cn.hexing.fas.model.FaalRequest;
import cn.hexing.fas.model.FaalRequestParam;
import cn.hexing.fas.model.FaalRequestResponse;
import cn.hexing.fas.model.FaalRequestRtuParam;
import cn.hexing.fas.model.FaalSendSmsRequest;
import cn.hexing.fas.model.FaalWriteParamsRequest;
import cn.hexing.fas.model.HostCommand;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fas.protocol.Protocol;
import cn.hexing.fas.protocol.gw.parse.DataSwitch;
import cn.hexing.fas.protocol.handler.ProtocolHandler;
import cn.hexing.fas.protocol.handler.ProtocolHandlerFactory;
import cn.hexing.fk.bp.ansi.AnsiEventProcessor;
import cn.hexing.fk.bp.dlms.DlmsEventProcessor;
import cn.hexing.fk.bp.model.HostCommandDb;
import cn.hexing.fk.bp.model.HostCommandItemDb;
import cn.hexing.fk.bp.model.HostParamResult;
import cn.hexing.fk.bp.model.RtuCommandIdInfo;
import cn.hexing.fk.bp.msgqueue.BPMessageQueue;
import cn.hexing.fk.bp.processor.gg.GgMessageProcessor;
import cn.hexing.fk.bp.processor.gg.GgUpgradeProcessor;
import cn.hexing.fk.bp.ws.HsmService;
import cn.hexing.fk.bp.ws.IdentifyAuthentication;
import cn.hexing.fk.bp.ws.KeyUpdate;
import cn.hexing.fk.bp.ws.UserControl;
import cn.hexing.fk.bp.ws.XmlParser;
import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.events.BasicEventHook;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.common.spi.socket.IChannel;
import cn.hexing.fk.common.spi.socket.IServerSideChannel;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.MultiProtoMessageLoader;
import cn.hexing.fk.message.gate.GateHead;
import cn.hexing.fk.message.gate.MessageGate;
import cn.hexing.fk.message.gw.MessageGw;
import cn.hexing.fk.message.zj.MessageZj;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.model.DlmsMeterRtu;
import cn.hexing.fk.model.Operator;
import cn.hexing.fk.model.RtuManage;
import cn.hexing.fk.sockserver.AsyncSocketClient;
import cn.hexing.fk.sockserver.TcpSocketServer;
import cn.hexing.fk.sockserver.event.ReceiveMessageEvent;
import cn.hexing.fk.sockserver.event.SendMessageEvent;
import cn.hexing.fk.tracelog.TraceLog;
import cn.hexing.fk.utils.ApplicationContextUtil;
import cn.hexing.fk.utils.Counter;
import cn.hexing.fk.utils.HexDump;
import cn.hexing.fk.utils.StringUtil;


public class WebAPIServerEventHandler extends BasicEventHook {
	private static final Logger log = Logger.getLogger(WebAPIServerEventHandler.class);
	private static final TraceLog tracer = TraceLog.getTracer(WebAPIServerEventHandler.class);
	//可配置属性
	private BPMessageQueue msgQueue;				//spring 配置实现。
	private MasterDbService masterDbService;  	//spring 配置实现。
	private ManageRtu manageRtu;				//spring 配置实现。
	private HsmService hsmService;				//spring 配置实现。
	private final WebMessageEncoder encoder = new WebMessageEncoder();
	//private MessageLoader4Zj msgLoader=new MessageLoader4Zj();
    /** Web 接口消息解码器 */
    private final WebMessageDecoder decoder = new WebMessageDecoder();  
    /** Web 接口消息编码器 */
    //private final WebMessageEncoder encoder = new WebMessageEncoder();
    private Counter counter=new Counter(100,"EncodeCounter");
    
	public WebAPIServerEventHandler(){
	}
	
	@Override
	public boolean start() {
		return super.start();
	}
	
	
	public BPMessageQueue getMsgQueue() {
		return msgQueue;
	}

	public void setMsgQueue(BPMessageQueue msgQueue) {
		this.msgQueue = msgQueue;
	}

	/**
	 * 重载该方法。
	 */
	public void handleEvent(IEvent e) {
		if( e.getType() == EventType.MSG_RECV ){
			//当收到业务处理器下行报文
			onRecvMessage( (ReceiveMessageEvent)e);
		}
		else if( e.getType() == EventType.MSG_SENT ){
			//当成功把报文发送给业务处理器
			onSendMessage( (SendMessageEvent)e );
		}
		else
			super.handleEvent(e);
	}

	/**
	 * 收到业务处理器的下行报文
	 * @param e
	 */
	private void onRecvMessage(ReceiveMessageEvent e){
		//对于网关规约报文，需要转换成浙江规约，才可以发送给浙江终端。
		IMessage msgObj = e.getMessage();
		if(!(msgObj instanceof MessageWeb) ){
			log.error("收到非Web调用规约的报文:"+msgObj+"MessageType="+msgObj.getMessageType());
			return;
		}
		MessageWeb msg = (MessageWeb)msgObj;
        if (log.isDebugEnabled()) {
//            log.debug("Receive a WebMessage[head: " + (13 + msg.getHead().getHeadAttrLen())
//                    + " bytes, body: " + msg.getData().limit() + " bytes]"+"MessageType="+msgObj.getMessageType());
        }
        //消息来源
        int msgSrc = msg.getHead().getAttributeAsInt(GateHead.ATT_MSGSRC);
        if(msgSrc==Operator.ZZ_DLMS){
        	AsyncSocketClient client=(AsyncSocketClient)e.getClient();
        	if (client!=null&&client.getIntKey()==Operator.DEFAULT)
        		client.setIntKey(Operator.ZZ_DLMS);
        }
        //消息序号
        int msgSeq = msg.getHead().getAttributeAsInt(GateHead.ATT_MSGSEQ);
        if( msg.getHead().getCommand() == MessageGate.CMD_GATE_HREQ ){//作为服务端收到心跳回确认
        	MessageGate hrep = MessageGate.createHReply();
    		if( 0 != msgSeq ){
    			hrep.getHead().setAttribute(GateHead.ATT_MSGSEQ, msgSeq);
    		}
    		e.getClient().send(hrep);       	
        	return;
        }				
        else if( 0 != msgSeq ){//下行请求需要回确认
        	if (log.isDebugEnabled()) {
        		log.debug("recv web msg. peerAddr:"+msg.getPeerAddr());
        	}
			MessageGate cfm = new MessageGate();
			cfm.getHead().setCommand(MessageGate.CMD_GATE_CONFIRM);
			cfm.getHead().setAttribute(GateHead.ATT_MSGSEQ, msgSeq );
			e.getClient().send(cfm);
		}
		if( msg.getHead().getCommand() == MessageGate.CMD_GATE_HREPLY ){//作为客户端收到心跳确认		
			return;		//心跳处理结束
		}
        try {
            // 将 Web 请求消息解码成 Web 请求对象
            Object obj = decoder.decode(msg);
            if (!(obj instanceof FaalRequest)) {
                log.error("Decode WebMessage, the result is not a FaalRequest: " + obj);
                return;
            } 
            
            FaalRequest request = (FaalRequest) obj;
            if (log.isDebugEnabled()) 
                log.debug("Decode WebMessage to FaalRequest: " + request);  
            
            if (request.getOperator()!=null){
            	if	(request.getOperator().equalsIgnoreCase("zdzj")||
            		request.getOperator().equalsIgnoreCase("sfrz")||
            		request.getOperator().equalsIgnoreCase("dbkz")){//自动装接请求
            		AsyncSocketClient client=(AsyncSocketClient)e.getClient();
            		if (client!=null&&client.getIntKey()==Operator.DEFAULT)//给自动装接请求client设置标记,以区分主站请求,自动装接需要返回
            			client.setIntKey(Operator.ZDZJ);
            	}
            	else if (request.getOperator().equalsIgnoreCase("zdds")){//终端对时
            		AsyncSocketClient client=(AsyncSocketClient)e.getClient();
            		if (client!=null&&client.getIntKey()==Operator.DEFAULT)//给终端对时请求client设置标记
            			client.setIntKey(Operator.HTXF_ZDDS);
            	}
            	else if (request.getOperator().equalsIgnoreCase("zzrw")||		//主站任务分参数招测和数据采集
                		request.getOperator().equalsIgnoreCase("zzrw_sjcj")){	//主站任务的数据采集，上行直接入库无需返回
            		AsyncSocketClient client=(AsyncSocketClient)e.getClient();
            		if (client!=null&&client.getIntKey()==Operator.DEFAULT)//给主站任务请求client设置标记
            			client.setIntKey(Operator.ZZRW);
            	}
            }
                   
            // 如果是刷新缓存请求，则刷新终端缓存中相应的对象
            if (request instanceof FaalRefreshCacheRequest) {
                refreshRtuCache((FaalRefreshCacheRequest) request);
                if (log.isDebugEnabled()) 
                    log.debug("Refresh Terminal Cache Success.");              
                return;
            }
                                   
            //如果是请求发送短信请求
            if (request instanceof FaalSendSmsRequest) {
            	sendSmsRequest((FaalSendSmsRequest)request);
                return;
            }            
            
            if (request.getOperator()!=null&&request instanceof FaalGWAFN10Request){
            	//国网中继身份认证请求
            	if (request.getOperator().equalsIgnoreCase("sfrz")) {
                	identifyAuthenticationRequest((FaalGWAFN10Request)request);
            	}
            	//国网中继控制命令请求
                else if (request.getOperator().equalsIgnoreCase("dbkz")) {
                	if (!userControlRequest((FaalGWAFN10Request)request))
                	/*request.getRtuParams().get(0).getParams().get(0).setName("07000002FF");
                	if (!keyUpdateRequest((FaalGWAFN10Request)request))*/
                		return;
            	}  
            	//国网中继密钥更新请求
                else if (request.getOperator().equalsIgnoreCase("mygx")) {
                	if (!keyUpdateRequest((FaalGWAFN10Request)request))
                		return;
            	}  
            }
            sendRequest((FaalRequest) request, e.getClient());
        }
        catch (MessageDecodeException ex) {
            log.error("Error to decode WebMessage", ex);
        }
        catch (MessageEncodeException ex) {
            log.error("Error to encode FaalRequest", ex);
        }
        catch (Exception ex) {
            log.error("Error to send FaalRequest", ex);
        }
	}
	
	/**
	 * 往业务处理器上行报文成功。
	 * @param e
	 */
	private void onSendMessage(SendMessageEvent e){
		IMessage msg = e.getMessage();
//		if( log.isDebugEnabled() )
//			log.debug("往WEB发送报文成功:"+msg.toString());
	}
    
    /**
     * 调用营销webservice接口获取电表身份认证组包的加密信息 
     * @param request
     */
    private boolean identifyAuthenticationRequest(FaalGWAFN10Request request){
    	boolean rt=false;
    	try{
    		/*StringBuilder sb = new StringBuilder();
			sb.append("request.tpSendTime="+request.getTpSendTime()+",");
			sb.append("request.tpTimeout="+request.getTpTimeout()+",");
			sb.append("request.port="+request.getPort()+",");
			sb.append("request.kzz="+request.getKzz()+",");
			sb.append("request.msgTimeout="+request.getMsgTimeout()+",");
			sb.append("request.byteTimeout="+request.getByteTimeout()+",");
			sb.append("request.fixProto="+request.getFixProto()+",");
			sb.append("request.fixAddre="+request.getFixAddre()+",");
			sb.append("request.transmitType="+request.getTransmitType()+",");
			sb.append("RtuId="+request.getRtuParams().get(0).getRtuId()+",");
			sb.append("code="+request.getRtuParams().get(0).getParams().get(0).getName()+".");
			tracer.trace("FaalGWAFN10Request:"+sb.toString());*/
			String addr=DataSwitch.StrStuff("0",16,request.getFixAddre(),"left");//不足长度左补0
			//addr="0000000000000001";
			tracer.trace("身份认证调用 addr="+addr);
			String result=hsmService.WS_IdentifyAuthentication(XmlParser.xmlToString("IdentifyAuthentication","",addr,"","",""));
			Object object=XmlParser.stringToXml(result, "IdentifyAuthentication");
		    if (object instanceof IdentifyAuthentication){
		    	IdentifyAuthentication identify=(IdentifyAuthentication)object;  
		    	if(identify.getFlag().equals("0"))//调用成功后设置身份认证数据区
		    		request.setEndata(identify.getEndata()+identify.getRand()+addr);
		    		//身份认证表地址要用0补足，不能用A补足
		    		addr=DataSwitch.StrStuff("00",12,request.getFixAddre(),"left");
		    		request.setFixAddre(addr);
		    		rt=true;
		    }
		    else{
        		tracer.trace("身份认证调用加密机接口失败 addr="+addr);
        		rt=false;
        	}
    	}catch(Exception e){
    		tracer.trace("identifyAuthenticationRequest error:",e);
    	}
    	return rt;
    }
    /**
     * 调用营销webservice接口获取电表控制命令组包的加密信息 
     * @param request
     */
    private boolean userControlRequest(FaalGWAFN10Request request){
    	boolean rt=false;
    	try{
    		//控制命令表地址要用0补足，不能用A补足
    		String addr=DataSwitch.StrStuff("0",12,request.getFixAddre(),"left");//不足长度左补0
    		tracer.trace("电表控制命令 addr="+addr);
    		request.setFixAddre(addr);
        	List<FaalRequestRtuParam> rtuParams=request.getRtuParams();
        	for (FaalRequestRtuParam rp:rtuParams){
        		String identification=masterDbService.getGwAmmeterIdentification(rp.getRtuId(), addr);
        		if (identification!=null&&identification.length()==24){    	
        			String rand=DataSwitch.ReverseStringByByte(identification.substring(0,8));
        			String esam=DataSwitch.ReverseStringByByte(identification.substring(8,24));
        			String numData="";
        			for (int i=0;i<rp.getParams().size();i++){
    					FaalRequestParam pm=(FaalRequestParam)rp.getParams().get(i);
    					if (pm.getName().equals("0700000100")){//电表控制命令,格式XX#YYMMDDHHNNSS(XX:1A跳闸,1B合闸)
    						String[] values=pm.getValue().split("#");
    						if (values.length==2)
    							numData=values[0]+"00"+values[1];
    					}
    					if (numData.length()==16){//YYMMDDHHNNSS
    						addr=DataSwitch.StrStuff("0",16,request.getFixAddre(),"left");//不足长度左补0
    						//addr="0000000000000001";
    						String result=hsmService.WS_UserControl(XmlParser.xmlToString("UserControl",rand,addr,esam,numData,""));
    						Object object=XmlParser.stringToXml(result, "UserControl");
    			        	if (object instanceof UserControl){
    			        		UserControl userControl=(UserControl)object;
    			        		if (userControl.getFlag().equals("0")){//加密成功
    			        			request.setEndata(userControl.getDataOut());
    			        			return rt=true;
    			        		}
    			        	}
    			        	else{
    			        		tracer.trace("控制命令加密失败 ：rand="+rand+";+addr="+addr+";esam="+esam+";numData="+numData);
    			        		return rt=false;
    			        	}
    					}
    					else
    						tracer.trace("电表控制命令参数值有误:"+pm.getValue());
    				}
        			
        		}
        	}
    	}catch(Exception e){
    		tracer.trace("userControlRequest error:",e);
    	}
    	return rt;
    }
    /**
     * 调用营销webservice接口获取密钥更新加密信息 
     * @param request
     */
    private boolean keyUpdateRequest(FaalGWAFN10Request request){
    	boolean rt=false;
    	try{
    		//控制命令表地址要用0补足，不能用A补足
    		String addr=DataSwitch.StrStuff("0",12,request.getFixAddre(),"left");//不足长度左补0
    		request.setFixAddre(addr);
    		tracer.trace("密钥更新命令 addr="+addr);
        	List<FaalRequestRtuParam> rtuParams=request.getRtuParams();
        	for (FaalRequestRtuParam rp:rtuParams){
        		String identification=masterDbService.getGwAmmeterIdentification(rp.getRtuId(), addr);
        		if (identification!=null&&identification.length()==24){    	
        			String rand=DataSwitch.ReverseStringByByte(identification.substring(0,8));
        			String esam=DataSwitch.ReverseStringByByte(identification.substring(8,24));
        			String numData="",kid="";
        			for (int i=0;i<rp.getParams().size();i++){
    					FaalRequestParam pm=(FaalRequestParam)rp.getParams().get(i);
    					if (pm.getName().equals("07000002FF")){//主控密钥更新命令
    						kid="4";
    						numData="070204FF";
    					}
    					else if (pm.getName().equals("07000003FF")){//远程控制密钥更新命令
    						kid="2";
    						numData="070201FF";
    					}
    					else if (pm.getName().equals("07000004FF")){//参数密钥更新命令
    						kid="3";
    						numData="070202FF";
    					}
    					else if (pm.getName().equals("07000005FF")){//身份认证密钥更新命令
    						kid="1";
    						numData="070203FF";
    					}
    					if (!kid.equals("")){    						
    						addr=DataSwitch.StrStuff("0",16,request.getFixAddre(),"left");//不足长度左补0
    						tracer.trace("密钥更新信息:code="+numData+",kid="+kid+",addr="+addr);
    						//addr="0000000000000001";
    						String result=hsmService.WS_KeyUpdate(XmlParser.xmlToString("KeyUpdate",rand,addr,esam,numData,kid));
    						Object object=XmlParser.stringToXml(result, "KeyUpdate");
    			        	if (object instanceof KeyUpdate){
    			        		KeyUpdate keyUpdate=(KeyUpdate)object;
    			        		if (keyUpdate.getFlag().equals("0")){//加密成功
    			        			request.setEndata(keyUpdate.getDataKey()+keyUpdate.getDataMac()+keyUpdate.getData());
    			        			return rt=true;
    			        		}
    			        	}
    			        	else{
    			        		tracer.trace("密钥更新加密失败 ：rand="+rand+";+addr="+addr+";esam="+esam+";numData="+numData+";kid="+kid);
    			        		return rt=false;
    			        	}
    					}
    					else
    						tracer.trace("密钥更新标识有误:"+pm.getName());
    				}
        			
        		}
        	}
    	}catch(Exception e){
    		tracer.trace("keyUpdateRequest error:",e);
    	}
    	return rt;
    }
    /**
     * 发送短信请求  
     * 终端局号列表(rtuIds)为空表示给用户发短消息,否则为终端自定义短信
     * 短信ID列表(smsids)不为空表示指定短信通道发送,否则按默认通道发送
     * @param smsrequest
     */
    private void sendSmsRequest(FaalSendSmsRequest smsrequest){
    	try{		    	
	        List<String> smsids=smsrequest.getSmsids();
	        String smsId="";
	        if (smsids!=null){//短信应用号,例如:95598305001
	        	String str=(String)smsids.get(0);
	        	if (str.length()>=9&&str.length()<=12){
	        		//如果传入为095598305001，则消去0
	        		if (str.subSequence(0, 1).equals("0"))
	        			str=str.substring(1,str.length());
	        		if (str.substring(0,5).equals("95598"))
	        			smsId=str;
	        	}
	        	if (smsId.equals(""))
	        		log.error("发送主站短信请求,指定短信通道错误:"+str);
	        }
	        if (smsrequest.getCtype()==0){
	        	//将请求对象编码为特定规约的消息
	    		ProtocolHandlerFactory factory = ProtocolHandlerFactory.getInstance();
		        ProtocolHandler handler = factory.getProtocolHandler(MessageZj.class);
	        	IMessage[] messages = handler.createMessage(smsrequest);
		        //转发消息
		        for (int i = 0; i < messages.length; i++) {
		        	MessageZj msgZj = (MessageZj)messages[i]; 		        		        		
		        	MessageGate gateMsg = new MessageGate();
		        	if (!smsId.equals("")){
		        		gateMsg.getHead().setAttribute(GateHead.ATT_DESTADDR,smsId);
		        	}	
	        		gateMsg.setDownInnerMessage(msgZj);
	                msgQueue.sendMessage(gateMsg);
	                tracer.trace("用户自定义短信:"+gateMsg);
		        }
	        }else{//给终端发送自定义报文，通道方式由主站指定
	        	MultiProtoMessageLoader msgLoader=new MultiProtoMessageLoader();
	        	IMessage msg=msgLoader.loadMessage(smsrequest.getContent());	        	
	        	MessageGate gateMsg = new MessageGate();
        		gateMsg.setDownInnerMessage(msg);
        		gateMsg.getHead().setAttribute(GateHead.ATT_DOWN_CHANNEL,smsrequest.getTxfs());
        		if (smsrequest.getTxfs()!=GateHead.CHANNEL_TYPE_GPRS){
        			if (!smsId.equals(""))
            			gateMsg.getHead().setAttribute(GateHead.ATT_DESTADDR,smsId);
            		if(smsrequest.getMobiles()!=null&&smsrequest.getMobiles().length>0){
            			String mobile="";
            			for (int i=0;i<smsrequest.getMobiles().length;i++){
            				mobile=mobile+smsrequest.getMobiles()[i]+",";
            			}            		
            			gateMsg.getHead().setAttribute(GateHead.ATT_SRCADDR,mobile.substring(0,mobile.length()-1));
            		}
        		}            	
                msgQueue.sendMessage(gateMsg);
                tracer.trace("终端自定义短信:"+gateMsg+",txfs="+smsrequest.getTxfs());
	        }	        	        	      	        
    	}catch(Exception e){
    		log.error("发送主站短信请求",e);
    	}
    }
    /**
     * 发送 FAAL 通讯请求
     * @param request FAAL 通讯请求
     */
    private void sendRequest(FaalRequest request,IChannel client) {
        // 确定规约消息类型：判断是浙江规约还是国网96规约，确定规约消息类型
        Class<?> messageType = null;
        if (Protocol.ZJ.equals(request.getProtocol())) {
            messageType = MessageZj.class;
        }else if(Protocol.GG.equals(request.getProtocol())){
        	messageType = MessageGg.class; 
        	if(request.getType()==0x30){
        		//广规软件升级
        		GgUpgradeProcessor.getInstance().processWebRequest(request,client);
        		return ;
        	}
        }else if(Protocol.G04.equals(request.getProtocol())&&request.getType()==15){
        	final FaalRequest req=request;
        	final IChannel c = client;
			WebMessageFileTransfer.getInstance().postFileTransferRequest((FaalGWAFN0FRequest) req,c);
			return;
		}else if(Protocol.G04.equals(request.getProtocol())){
			messageType = MessageGw.class;			
		}else if(Protocol.DLMS.equals(request.getProtocol())){
			if(request instanceof DlmsRequest){
				DlmsEventProcessor.getInstance().postWebRequest(request, client);
				return;
			}			
		}else if(Protocol.ANSI.equals(request.getProtocol())){
			if(request instanceof AnsiRequest){
				AnsiEventProcessor.getInstance().postWebRequest(request, client);
				return;
			}
		}else {
            throw new ProtocolHandleException("Unsported protocol: " + request.getProtocol());
        }
        
        /*//为北京融合度检测临时处理，初始化不加载终端相关档案
        for (FaalRequestRtuParam frp:request.getRtuParams()){
        	BizRtu rtu=(RtuManage.getInstance().getBizRtuInCache(frp.getRtuId()));	        	
        	if(rtu==null){
        		boolean refresh=manageRtu.refreshBizRtu(frp.getRtuId());     
        		if (log.isInfoEnabled())  
                	log.info("rtuId="+frp.getRtuId()+" refresh tag="+refresh);
        	}	
        }*/
        
        // 将请求对象编码为特定规约的消息
        try{
        	int[] fseqs = null;
        	if(request instanceof  FaalGWupdateKeyRequest){
        		//对于gw集中器更新密钥,由于调用加密机,需要用到帧序号,这里先将帧序号获得,存储在request里
        		if(((FaalGWupdateKeyRequest) request).getFseq()!=null){
        			fseqs = ((FaalGWupdateKeyRequest) request).getFseq();
        		}else{
        			List<FaalRequestRtuParam> params = request.getRtuParams();
            		int size=params.size();
            		fseqs = new int[size];
            		for(int i = 0 ; i <size ; i++){
            			FaalRequestRtuParam param = params.get(i);
            			fseqs[i]=masterDbService.getRtuCommandSeq(HexDump.toHex(Integer.parseInt(param.getRtuId(),16)),"02");
            		}
            		((FaalGWupdateKeyRequest) request).setFseq(fseqs);
        		}
        	}
        	
        	
        	ProtocolHandlerFactory factory = ProtocolHandlerFactory.getInstance();
            ProtocolHandler handler = factory.getProtocolHandler(messageType);
            IMessage[] messages = handler.createMessage(request);
            if (log.isDebugEnabled()) {
            	if(messages==null){
            		log.warn("Encode message error, no message create.");
            	}else{
            		log.debug("Encode to Message, protocol: " + request.getProtocol()
                            + ", message count: " + messages.length);
            	}
                
            }            
            if(messages == null){
            	throw new RuntimeException("Can't Create Messages...");
            }
            // 转发消息
            for (int i = 0; i < messages.length; i++) {
                if(messages[i].getStatus()!=null){
            		if(HostCommand.STATUS_PARA_INVALID.equalsIgnoreCase(messages[i].getStatus())){
            			//组帧失败,回写状态
            			errorHostCommand(messages[i].getCmdId());
            			continue;
            		}                	
                }                 
                counter.add();
                //插入终端逻辑地址,帧序号,命令ID进数据库表,用于上行返回对应
                RtuCommandIdInfo rtuCmd=new RtuCommandIdInfo();
                if (request.getOperator()!=null
                	&&(request.getOperator().equalsIgnoreCase("zdzj")||request.getOperator().equalsIgnoreCase("dbkz")))//自动装接请求
                	rtuCmd.setZdzjbz(Operator.ZDZJ);
                else if (request.getOperator()!=null
                	&&request.getOperator().equalsIgnoreCase("zdds"))		//终端对时请求
                	rtuCmd.setZdzjbz(Operator.HTXF_ZDDS);
                else if (request.getOperator()!=null
                    	&&request.getOperator().equalsIgnoreCase("gwldbz"))	//国网任务漏点补招请求
                    	rtuCmd.setZdzjbz(Operator.GWLDBZ);
                else if (request.getOperator()!=null
                    	&&request.getOperator().equalsIgnoreCase("sfrz"))	//国网中继身份认证
                    	rtuCmd.setZdzjbz(Operator.ZDZJ_SFRZ);
                else if (request.getOperator()!=null
                    	&&request.getOperator().equalsIgnoreCase("htxf"))	//后台下发
                    	rtuCmd.setZdzjbz(Operator.HTXF);
                else if (request.getOperator()!=null
                    	&&request.getOperator().equalsIgnoreCase("zzrw"))	//主站任务轮招
                    	rtuCmd.setZdzjbz(Operator.ZZRW);
                else if (request.getOperator()!=null
                    	&&request.getOperator().equalsIgnoreCase("zzrw_sjcj"))	//主站任务轮招数据采集
                    	rtuCmd.setZdzjbz(Operator.ZZRW_SJCJ);
                else								 				//主站请求
                	rtuCmd.setZdzjbz(Operator.DEFAULT);
                BizRtu rtu=null;
        		if (messages[i] instanceof MessageZj){
        			MessageZj zjmsg=(MessageZj)messages[i];
        			zjmsg.setPeerAddr(client.getPeerAddr());
        			int rtua=zjmsg.head.rtua;
        			rtu = RtuManage.getInstance().getBizRtuInCache(rtua);  
        			int ifseq=0;
                    if("04".equals(rtu.getRtuProtocol())){
                    	ifseq = masterDbService.getRtuCommandSeq(HexDump.toHex(rtua),"01");
                    }else{
                    	ifseq = masterDbService.getRtuCommandSeq(HexDump.toHex(rtua),rtu.getRtuProtocol());
                    }
                    if(request instanceof FaalGGKZM11Request){
                    	FaalGGKZM11Request readTaskReq = (FaalGGKZM11Request) request;
                        rtu.addParamToMap(ifseq, readTaskReq);
                    }
                    if(request instanceof FaalGGKZM12Request){
                    	FaalGGKZM12Request readTask=(FaalGGKZM12Request)request;
                    	rtu.addParamToMap(ifseq, readTask.getTaskNo());
                    }
                    zjmsg.head.fseq=(byte)ifseq;
            		rtuCmd.setLogicAddress(HexDump.toHex(rtua));
            		rtuCmd.setZxh(ifseq);
            		rtuCmd.setCmdId(zjmsg.getCmdId());
            		rtuCmd.setBwsl(zjmsg.getMsgCount());
        		}				
        		else if (messages[i] instanceof MessageGw){
        			MessageGw gwmsg=(MessageGw)messages[i];
        			gwmsg.setPeerAddr(client.getPeerAddr());
        			int rtua=gwmsg.head.rtua;
        			rtu = RtuManage.getInstance().getBizRtuInCache(rtua); 
        			int ifseq = 0;
        			if(request instanceof FaalGWupdateKeyRequest){
        				ifseq=fseqs[i];
        			}else{
        				ifseq=masterDbService.getRtuCommandSeq(HexDump.toHex(rtua),rtu.getRtuProtocol());
        			}
        			gwmsg.setSEQ((byte)ifseq);
            		rtuCmd.setLogicAddress(HexDump.toHex(rtua));
            		rtuCmd.setZxh(ifseq);
            		rtuCmd.setCmdId(gwmsg.getCmdId());
            		rtuCmd.setBwsl(gwmsg.getMsgCount());
        		}	
//        		if (log.isDebugEnabled())  
//                	log.debug("rtu="+rtu.getLogicAddress()+" Framing Success:"+messages[i].toString());
        		
        		//浙规漏点补招不需要更新命令操作列表
        		if (!(request instanceof FaalReadTaskDataRequest)){
        			if (rtuCmd.getCmdId()==null)
        				rtuCmd.setCmdId(new Long(0));
        			masterDbService.insertRtuComdMag(rtuCmd); 
        		}
        		
        		MessageGate gateMsg = new MessageGate();
        		gateMsg.setDownInnerMessage(messages[i]);
        		gateMsg.getHead().setAttribute(GateHead.ATT_DOWN_CHANNEL,request.getTxfs());
        		if(GgMessageProcessor.isG3MeterBox && messageType == MessageGg.class){
        			//G3表箱，进行串行处理
        			GgMessageProcessor.getInstance().addMessage(rtu.getLogicAddress(),messages[i]);
        			GgMessageProcessor.getInstance().sendNextMessage(rtu.getLogicAddress());
        		}else{
        			msgQueue.sendMessage(gateMsg);   
        			if (log.isDebugEnabled())  
                    	log.debug("RtuAddr:="+rtuCmd.getLogicAddress()+";CmdId:"+rtuCmd.getCmdId()+";Zxh:"+rtuCmd.getZxh()+";send message:"+gateMsg.getInnerMessage());
        		}
            }
            if (!GgMessageProcessor.isG3MeterBox && log.isDebugEnabled()) {
                log.debug(messages.length + " messages send");
            }
        }catch(MessageEncodeException e){
        	//组帧错误，返回主站错误码
        	try{
        		if (e.getCode()!=null){//如果是设置内容非法则回返回设置错误的标识
        			List<FaalRequestRtuParam> frps=request.getRtuParams();
        			for(FaalRequestRtuParam frp:frps){
        				Long cmdId=frp.getCmdId();
        				if (cmdId==0)//后台服务设置请求
            				errorBackgroundCommand(request,e.getCode());
            			else{//主站请求
            				if(request.getOperator().equalsIgnoreCase("zdzj"))//自动装接组帧失败通过socket返回信息
            					sendIntfErrorResponse(cmdId,frp.getRtuId());            				
            				errorHostCommand(cmdId);
            			}
        			}
        		}else{
        			log.error(StringUtil.getExceptionDetailInfo(e));
        		}        			
        	}catch(Exception ex){
        		log.error("update error host cmd status",ex);
        	}        	
        }  
        catch(ProtocolHandleException e){  
        	log.error("request to msg",e);
        }
    }
    
    
    
    /**
     * 刷新终端缓存
     * 终端局号列表不为空表示刷新终端相关档案信息(包括资产,测量点,终端任务)
     * 如果任务号为0,重新生成任务数据项表映射信息;非0表示则表示刷新指定模版ID的任务模版
     * @param request 刷新缓存请求对象
     */
    private void refreshRtuCache(FaalRefreshCacheRequest request) {
    	String[] rtuIds = request.getRtuIds();
        if (rtuIds != null ) {
        	for (int i = 0; i < rtuIds.length; i++) {
                String rtuId = rtuIds[i];
                boolean refresh=manageRtu.refreshBizRtu(rtuId);   
                if (tracer.isEnabled())
                	tracer.trace("refresh rtuId="+rtuId+" tag="+refresh);
            }
        }      
        String[] meterAddrs = request.getMeterAddrs();
        if(meterAddrs!=null){
        	for(int i=0;i<meterAddrs.length;i++){
        		String meterAddr=meterAddrs[i];
        		DlmsMeterRtu meterRtu = manageRtu.refreshDlmsMeterRtu(meterAddr);
        		if(tracer.isEnabled()){
        			tracer.trace("refresh meterRtu="+meterAddr+" tag="+(meterRtu==null));
        		}
        	}
        }
        String taskNum = request.getTaskNum();
        if (taskNum!=null){
        	if (taskNum.equals("0"))//重新生成任务数据项表映射信息
        		manageRtu.initializeTaskDbConfig();        		        	  		     	
        	else{					//刷新指定模版ID的任务模版
        		manageRtu.refreshTaskTemplate(taskNum);
        		manageRtu.refreshMasterTaskTemplate(taskNum);
        	}
        	if (tracer.isEnabled())
        		tracer.trace("refresh taskNum ="+taskNum);
        }
    }    
	public void sendIntfErrorResponse(Long cmdId,String rtuId){
		try{
			FaalRequestResponse requestResponse=new FaalRequestResponse();
			requestResponse.setCmdId(cmdId);
			requestResponse.setRtuId(rtuId);
			requestResponse.setCmdStatus(HostCommand.STATUS_PRAR_ERROR);
			MessageWeb msgWeb=encoder.encode(requestResponse);
			if (!sendIntfMsg(msgWeb,1))
				log.info("encode error,not find IntfClient to send!");
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

	public void setManageRtu(ManageRtu manageRtu) {
		this.manageRtu = manageRtu;
	}

	public void setHsmService(HsmService hsmService) {
		this.hsmService = hsmService;
	}

}
