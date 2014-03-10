package cn.hexing.fk.bp.processor;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import cn.hexing.db.batch.AsyncService;
import cn.hexing.db.batch.event.adapt.BaseExpAlarmHandler;
import cn.hexing.db.bizprocess.MasterDbService;
import cn.hexing.fas.framework.message.MessageGg;
import cn.hexing.fas.model.FaalGWAFN0FRequest;
import cn.hexing.fas.model.FaalRequest;
import cn.hexing.fas.model.FaalRequestResponse;
import cn.hexing.fas.model.RtuAlert;
import cn.hexing.fas.protocol.Protocol;
import cn.hexing.fas.protocol.gw.parse.DataSwitch;
import cn.hexing.fas.protocol.handler.ProtocolHandler;
import cn.hexing.fas.protocol.handler.ProtocolHandlerFactory;
import cn.hexing.fas.protocol.zj.FunctionCode;
import cn.hexing.fk.bp.model.AlarmData;
import cn.hexing.fk.bp.model.AutoRegister;
import cn.hexing.fk.bp.model.MessageLogErr;
import cn.hexing.fk.bp.webapi.MessageWeb;
import cn.hexing.fk.bp.webapi.WebMessageEncoder;
import cn.hexing.fk.common.spi.socket.IChannel;
import cn.hexing.fk.common.spi.socket.IServerSideChannel;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.MessageType;
import cn.hexing.fk.message.gate.MessageGate;
import cn.hexing.fk.message.gw.MessageGw;
import cn.hexing.fk.message.zj.MessageZj;
import cn.hexing.fk.message.zj.MessageZjHead;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.model.RtuManage;
import cn.hexing.fk.model.UpgradeInfo;
import cn.hexing.fk.sockserver.AsyncSocketClient;
import cn.hexing.fk.sockserver.TcpSocketServer;
import cn.hexing.fk.utils.ApplicationContextUtil;
import cn.hexing.fk.utils.HexDump;

public class AlarmMessageHandler extends BaseExpAlarmHandler {
	private static final Logger log = Logger.getLogger(AlarmMessageHandler.class);
	/** Web 接口消息编码器 */
    private final WebMessageEncoder encoder = new WebMessageEncoder();
    /** 需要通知自动装接的告警编码,以逗号隔开 */
    private String notifiedAlertCodes;
    private MasterDbService masterDbService;
	private String[] alertCodes;
	public String getNotifiedAlertCodes() {
		return notifiedAlertCodes;
	}
	public void setNotifiedAlertCodes(String notifiedAlertCodes) {
		this.notifiedAlertCodes = notifiedAlertCodes;
		if (notifiedAlertCodes != null) {
			notifiedAlertCodes = notifiedAlertCodes.trim();
            if (notifiedAlertCodes.length() > 0) {
            	this.alertCodes = notifiedAlertCodes.split(",");
            }
        }	
	}
	public void handleExpAlarm(AsyncService service,IMessage msg){	
		MessageZj zjmsg=null;
		MessageGw gwmsg=null;
		int rtua=0,fseq=0;
		try {  			
			if (msg.getMessageType()==MessageType.MSG_ZJ){
				zjmsg=(MessageZj)msg;
				rtua=zjmsg.head.rtua;
				fseq=zjmsg.head.fseq;
			}				
			else if (msg.getMessageType()==MessageType.MSG_GW_10){
				gwmsg=(MessageGw)msg;
				rtua=gwmsg.head.rtua;
				fseq=gwmsg.getFseq();
			}	
			//分辨终端规约:浙江规约或浙江配变规约
			BizRtu rtu=(RtuManage.getInstance().getBizRtuInCache(rtua));
			if (rtu==null){
				log.error("not find rtu in cache:"+HexDump.toHex(rtua));
			}
			Class messageType = MessageZj.class;;                    
	    	if (msg instanceof MessageGw){            			    		
	        	if (rtu!=null&&rtu.getRtuProtocol().equals(Protocol.G04)){
	        		messageType = MessageGw.class;                  		              	
	        	}             	
	    	} 
	    	else if(msg instanceof MessageZj){
	        	if (rtu!=null&&rtu.getRtuProtocol().equals(Protocol.GG)){
	        		messageType = MessageGg.class;                  		              	
	        	}           
	    	}
	    	//调用规约解析报文
	    	ProtocolHandlerFactory factory = ProtocolHandlerFactory.getInstance();
	        ProtocolHandler handler = factory.getProtocolHandler(messageType);                	
	        Object value = handler.process(msg); 
	        if (value!=null){
				List<RtuAlert> rtuAlerts=(List<RtuAlert>)value;
	        	//回复浙规异常确认
	        	if(zjmsg!=null&&zjmsg.head.msta == 0) {   //主动上送才需要回确认
	        		MessageZj response = createAlertConfirmMessage(zjmsg, rtuAlerts,messageType);
                    if(response!=null){//异常确认回复
                    	IChannel channel=zjmsg.getSource();
                    	if (channel!=null){
                    		MessageGate gateMsg = new MessageGate();
                    		gateMsg.setDownInnerMessage(response);
                    		channel.send(gateMsg);
                    		log.debug("bp response alarm:"+response);
                    	}
                    	else
                    		log.debug("up alarm messageZj' source is null:"+zjmsg);
                    }
                }
	        	for(int i=0;i<rtuAlerts.size();i++){
	        		RtuAlert alert = (RtuAlert) rtuAlerts.get(i);
	        		if(alert.getAlertCode()==Integer.parseInt("C390",16)|| //GW集中器自动注册
	        		   alert.getAlertCode()==Integer.parseInt("1A9",16)||  //GG
	        		   alert.getAlertCode()==Integer.parseInt("C460",16))//表箱上报档案
	        		{
	        			//到这里,是国网数据,对于主站召测,集中器自动注册不存储
	        			if(msg instanceof MessageGw&&((MessageGw)msg).head.c_prm==1){
	        				autoRegister(alert,service,rtua);//集中器自动注册
	        			}else if(msg instanceof MessageZj){
	        				autoRegister(alert,service,rtua);//集中器自动注册,浙归
	        			}
	        		}
	        		if(alert.getAlertCode()==Integer.parseInt("C440",16)){
	        			updateProgram(alert,rtua);  //固件更新
	        			//更行升级记录表
	        			updateSoftRecord(alert,service,rtua);
	        			//集中器升级request在map里面key是3 标记升级是1
//	        			updateFileTransferStatus(rtu.getParamFromMessageCountMap(1), 0,  rtu.getParamFromMessageCountMap(1), (FaalGWAFN0FRequest)rtu.getRequestFromMap(3), service);

	        		}
	        		if(alert.getAlertCode()==Integer.parseInt("C012",16)){//上报版本变更告警，暂时按照集中器或者表计上送的固件更新事件来识别升级是否成功
//	        			//集中器升级request在map里面key是3 标记升级是1
//	        			updateFileTransferStatus(rtu.getParamFromMessageCountMap(1), 0,  rtu.getParamFromMessageCountMap(1), (FaalGWAFN0FRequest)rtu.getRequestFromMap(3), service);
//	        			//更行升级记录表
//	        			updateSoftRecord(alert,service,rtua);
	        		}
	        		AlarmData alarmData=new AlarmData();	        		
	        		alarmData.setDeptCode(alert.getCorpNo());
	        		alarmData.setRtuId(alert.getRtuId());
	        		alarmData.setCustomerNo(alert.getCustomerNo());
	        		alarmData.setStationNo(alert.getStationNo());
	        		alarmData.setRtuId(alert.getRtuId());
	        		alarmData.setGjly(alert.getGjly());
	        		if(alert.getAlertCode()>Integer.parseInt("030000",16) && alert.getAlertCode()<Integer.parseInt("0304FF",16)){
	        			alarmData.setAlertCodeHex("03"+alert.getAlertCodeHex());
	        		}else{
	        			alarmData.setAlertCodeHex(alert.getAlertCodeHex());
	        		}
	        		alarmData.setAlertTime(alert.getAlertTime());	        		
	        		alarmData.setReceiveTime(Calendar.getInstance().getTime());
	        		alarmData.setSbcs(alert.getSbcs());
	        		alarmData.setTxfs(msg.getTxfs());	
	        		String deptCode=alarmData.getDeptCode();					
					if (alert.getDataSaveID()!=null){//测量点未建档，不进行数据库操作
						if (deptCode!=null&&deptCode.length()>=5){//单位代码必须是5位,前两位作为分地市保存的key
							deptCode=deptCode.substring(3,5);
							alarmData.setDataSaveID(new Long(alert.getDataSaveID()));
							service.addToDao(alarmData,4000);
							//告警后处理
							//BPLatterProcessor.getInstance().alertDataAdd(alarmData);
						}							
					}	        								
					/*//需要通知自动装接异常告警编码
					if (ArraysUtil.contains(alertCodes,alarmData.getAlertCodeHex())) {						
						sendIntfRequestResponse(alarmData,rtu.getRtuType());
			        }
					//国网集中器终端F10参数设置变更事件需要通知自动装接
					if (alarmData.getAlertCodeHex().equals("C030")&&
						rtu.getRtuProtocol().equals(Protocol.G04)&&
			        	rtu.getRtuClass()!=null&&rtu.getRtuClass().equals("03")){
						alarmData.setAlertInfo(alert.getAlertInfo());
						sendIntfRequestResponse(alarmData,rtu.getRtuType());
					}*/
	        	}	        	
	        }
		}catch (Exception ex) {//非法异常报文保存
			MessageLogErr msgLogErr=new MessageLogErr();
			msgLogErr.setLogicAddress(HexDump.toHex(rtua));
			msgLogErr.setQym(msgLogErr.getLogicAddress().substring(0,2));
			if (zjmsg!=null)
				msgLogErr.setKzm(Integer.toString(zjmsg.head.c_func, 16));
			else
				msgLogErr.setKzm(Integer.toString(gwmsg.getAFN(), 16));
			msgLogErr.setTime(new Date(msg.getIoTime()));
			msgLogErr.setBody(msg.getRawPacketString());					
			service.addToDao(msgLogErr,Integer.parseInt("5002"));
            log.error("Error to processing alarm message:"+msg, ex);
        }            
	}
	private void updateProgram(RtuAlert alert, int rtua) {
		String[] values=alert.getSbcs().split("#");
		String result  = values[2];
		if("1".equals(result)){
			result = "02";  //成功
		}else{
			result = "00"; //失败
		}
		values[1]=("1".equals(values[1])?"2":(("2".equals(values[1]))?"1":values[1]));
		String logicAddress = HexDump.toHex(rtua);
		if(values.length==4){
			masterDbService.updateConcentratorPro(values[3], logicAddress, values[0], values[1], alert.getAlertTime(), result);			
		}else{
			log.error("AlramCode: C440 ,paramters size !=4");
		}
	}
	/**
	 * 集中器自动注册
	 * @param alert
	 */
	private void autoRegister(RtuAlert alert,AsyncService service,int rtua) {
		log.info("concentrator auto register!!");
		String value=alert.getSbcs();
		String[] params = value.split("#");
		String logicAddress = HexDump.toHex(rtua);
		AutoRegister ar = new AutoRegister();
		ar.setLogicAddress(logicAddress);
		ar.setRegisterTime(alert.getAlertTime());  //这里设置时间没有用，存储的时候直接使用系统时间，请看sql语句
		ar.setMeasurePoint(params[1]);
		ar.setMeterAddress(params[4]);
		ar.setStatus(0);
		ar.setValue("#"+value);
		service.addToDao(ar, 4001);
		
		
	}
    /**
     * 	集中器固件升级成功，版本变更告警处理
     * @param logicAddr
     * @param tn
     * @param currentMessage
     * @param status
     * @param messageCount
     */
    
    public void updateFileTransferStatus(int currentMessage,int status,int messageCount,FaalRequest request,AsyncService service){
      FaalGWAFN0FRequest req=(FaalGWAFN0FRequest)request;
      UpgradeInfo info = new UpgradeInfo();
      info.setStatus(status);
      info.setBlockCount(messageCount);
      info.setCurBlockNum(currentMessage);
      info.setMaxSize(1024);
      info.setSoftUpgradeID(req.getSoftUpgradeID());
      service.addToDao(info, 4004);
    }
    /**
     * 软件升级，升级成功告警处理
     * @param alert
     * @param service
     * @param rtua
     */
    public void updateSoftRecord(RtuAlert alert,AsyncService service,int rtua){
    	String value=alert.getSbcs();
    	String[] params = value.split("#");
    	String newSoftVersion=params[params.length-1];
    	String status="";
    	if("0".equals(params[2])){
    		status="00";
    	}else{
    		status="02";
    	}
//    	//通过上报告警里面上报参数带的bbxx查找软件版本号
//    	String softVersion=	masterDbService.getSoftVersion(newSoftVersion);
    	//更新升级记录表，状态02成功
    	masterDbService.updateSoftUpgrade(HexDump.toHex(rtua), params[0], newSoftVersion, status);
    }
	/**
     * 通知自动装接异常告警编码
     */
	public void sendIntfRequestResponse(AlarmData alarmData,String rtuType){
		try{
			FaalRequestResponse requestResponse=new FaalRequestResponse();
			requestResponse.setCmdId(new Long(0));
			requestResponse.setRtuType(rtuType);
			requestResponse.setRtuId(alarmData.getRtuId());
			Map<String,String> params=new HashMap<String,String>();
			params.put(alarmData.getAlertCodeHex(), alarmData.getAlertInfo());
			requestResponse.setParams(params);
			MessageWeb msgWeb=encoder.encode(requestResponse);
			sendIntfMsg(msgWeb);	
		}catch(Exception ex){
			log.error("IntfChannelManage send alarm message err:"+ex);
		}
	}
	public void sendIntfMsg(MessageWeb msgWeb){
		try{
			TcpSocketServer server=(TcpSocketServer)ApplicationContextUtil.getBean("bp.server.web");
			for(IServerSideChannel client:server.getClients()){
				if (client!=null&&((AsyncSocketClient)client).getIntKey()==1){
					if (client.send(msgWeb))
						break;
				}
			}
		}catch(Exception ex){
			log.error("sendIntfMsg err:"+ex);
		}
	}
	/**
     * 创建告警应答消息
     * @param receivedMessage 接收到的消息
     * @param alerts 告警列表
     * @return 应答消息
     */
    private MessageZj createAlertConfirmMessage(MessageZj receivedMessage, List<RtuAlert> alerts,Class messageType) {
        MessageZj msg = createConfirmMessage(receivedMessage,messageType);
        if (msg == null) {
            return null;
        }
        //浙归告警确认帧
        if(messageType==MessageZj.class){
            msg.head.c_func = FunctionCode.CONFIRM_ALERT;
            ByteBuffer data = ByteBuffer.allocate(alerts.size() * 3);
            data.order(ByteOrder.LITTLE_ENDIAN);
            for (int i = 0; i < alerts.size(); i++) {
                RtuAlert alert = (RtuAlert) alerts.get(i);
                data.put(Byte.parseByte(alert.getTn()));
                data.putShort((short) alert.getAlertCode());
            }
            data.flip();
            msg.head.dlen = (short) data.limit();
            msg.data = data;
            return msg;
        }
        //广规告警确认帧
        else if(messageType==MessageGg.class){
            msg.head.c_func = FunctionCode.CONFIRM_ALERT;
            ByteBuffer data = ByteBuffer.allocate(alerts.size() * 8);
            String sdata=null;
            for (int i = 0; i < alerts.size(); i++) {
                RtuAlert alert = (RtuAlert) alerts.get(i);
        		String value=alert.getSbcs();
        		if(null==value){
        			sdata="FFFFFFFFFFFF"+DataSwitch.ReverseStringByByte(alert.getAlertCodeHex());
        		}
        		else if("01A9".equals(HexDump.toHex((short)(alert.getAlertCode())))){
        			String[] params = value.split("#");
        			sdata=DataSwitch.ReverseStringByByte(params[4])+DataSwitch.ReverseStringByByte(HexDump.toHex((short)(alert.getAlertCode())));
                }
        		else if("01AB".equals(HexDump.toHex((short)(alert.getAlertCode())))){
        			String[] params=value.split("#");
        			sdata=DataSwitch.ReverseStringByByte(params[1])+DataSwitch.ReverseStringByByte(HexDump.toHex((short)(alert.getAlertCode())));
        		}
        		else if("01AC".equals(HexDump.toHex((short)(alert.getAlertCode())))){
        			String[] params=value.split("#");
        			sdata=DataSwitch.ReverseStringByByte(params[2])+DataSwitch.ReverseStringByByte(HexDump.toHex((short)(alert.getAlertCode())));
        		}
            }
            data=HexDump.toByteBuffer(sdata);
            msg.data = data;
            if("true".equals(System.getProperty("isG3MeterBox"))){
            	//G3表箱项目异常返回为0x1A,没有文档定义.艹!!!
            	msg.head.c_func=0x1A;            	
            }
            return msg;
        }
        return msg;
    }
    /**
     * 创建应答消息
     * @param receivedMessage 接收到的消息
     * @return 应答消息
     */
    private MessageZj createConfirmMessage(MessageZj receivedMessage,Class messageType) {
        // 如果是异常消息，则不需要应答
        if (receivedMessage.head.c_expflag == (byte) 0x01) {
            return null;
        }
        
        long txsj=receivedMessage.getIoTime();     
    	try{		
    		long delt=System.currentTimeMillis()-txsj;
    		if(delt>1800000){	//1800秒后不在回告警应答
    			return null;
    		}
    	}catch(Exception e){
    		
    	}      
        MessageZjHead head = new MessageZjHead();
        MessageZj msg=new MessageZj();
        head.rtua = receivedMessage.head.rtua;
        head.msta = receivedMessage.head.msta;
        head.fseq = receivedMessage.head.fseq;
        head.iseq = 0;
        head.c_dir = 0;
        head.c_expflag = 0;
        head.c_func = receivedMessage.head.c_func;
        if(messageType==MessageZj.class){
            head.dlen = 0;
            msg.head = head;
        }
        else{
            head.dlen = 8;
            msg.head = head;
        }
        return msg;
    }
	public final void setMasterDbService(MasterDbService masterDbService) {
		this.masterDbService = masterDbService;
	}
}
