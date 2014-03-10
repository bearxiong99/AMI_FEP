package cn.hexing.fk.bp.processor;

import java.util.List;

import org.apache.log4j.Logger;

import cn.hexing.db.batch.AsyncService;
import cn.hexing.db.bizprocess.MasterDbService;
import cn.hexing.db.managertu.ManageRtu;
import cn.hexing.fas.model.FaalGWupdateKeyRequest;
import cn.hexing.fas.model.FaalRequestRtuParam;
import cn.hexing.fas.model.HostCommand;
import cn.hexing.fas.model.HostCommandResult;
import cn.hexing.fas.protocol.handler.ProtocolHandler;
import cn.hexing.fas.protocol.handler.ProtocolHandlerFactory;
import cn.hexing.fk.bp.msgqueue.BPMessageQueue;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.MessageType;
import cn.hexing.fk.message.gate.GateHead;
import cn.hexing.fk.message.gate.MessageGate;
import cn.hexing.fk.message.gw.MessageGw;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.model.RtuCmdItem;
import cn.hexing.fk.model.RtuManage;
import cn.hexing.fk.tracelog.TraceLog;
import cn.hexing.fk.utils.HexDump;

/**
 * 
 * @author gaoll
 *
 * @time 2012-12-18 上午9:55:10
 *
 * @info 国网密钥更新处理器
 */
public class GwUpdateKeyHandler {

	private static final Logger log = Logger.getLogger(GwUpdateKeyHandler.class);
	private static final TraceLog tracer = TraceLog.getTracer(GwUpdateKeyHandler.class);
	private final HostCommandHandler hostCommandHandler = new HostCommandHandler();
	private static GwUpdateKeyHandler instance = new GwUpdateKeyHandler();
	private GwUpdateKeyHandler(){}
	
	public static GwUpdateKeyHandler getInstance(){return instance;}
	
	private BPMessageQueue msgQueue ;
	
	public boolean processUpdateKeyMsg(AsyncService asycService,ManageRtu manageRtu,MasterDbService masterDbService,IMessage msg){
		MessageGw gwmsg=null;
		int rtua=0,fseq=0;
		if (msg.getMessageType()==MessageType.MSG_GW_10){
			gwmsg=(MessageGw)msg;
			rtua=gwmsg.head.rtua;
			fseq=gwmsg.getFseq();
		}	
		BizRtu rtu=(RtuManage.getInstance().getBizRtuInCache(rtua));
		if (rtu==null){
			log.warn("not find rtu in cache:"+HexDump.toHex(rtua));
		}
		Class<?> messageType = MessageGw.class; 
		//调用规约解析报文
    	ProtocolHandlerFactory factory = ProtocolHandlerFactory.getInstance();
		ProtocolHandler handler = factory.getProtocolHandler(messageType);                	
        Object value = handler.process(msg); 
        List<RtuCmdItem> rcis=masterDbService.getRtuComdItem(HexDump.toHex(rtua), fseq);
        if (rcis.size()>1)//不应该有多个,只可能是唯一的
			log.error("getGetRtuComdItem size>1:"+HexDump.toHex(rtua)+";"+fseq);
        if(value instanceof String){//集中器上报的随机数
        	
        	FaalGWupdateKeyRequest gwUpdate = createRequest(fseq, rtu, value);
    		
        	IMessage[] messages = handler.createMessage(gwUpdate);
        	
    		MessageGate gateMsg = sendMessageToFe((MessageGw)messages[0],msg,fseq);
        	
    		if (log.isDebugEnabled())  
            	log.debug("update key send second time,RtuAddr:="+rtu.getLogicAddress()+"  send message:"+gateMsg.getInnerMessage());
    		
        }
        
        if(value instanceof HostCommand){//告之主站是否成功
        	HostCommand result = (HostCommand) value;
        	HostCommandResult resultValue = result.getResults().get(0);
        	if("06F021".equals(resultValue.getCode())){ //更新集中器本身公钥
        		if(result.getStatus()==HostCommand.STATUS_SUCCESS){
        			String terminalPubKey=resultValue.getValue();
        			rtu.setPubKey(terminalPubKey);
        			int asymmetricKeyVersion = rtu.getAsymmetricKeyVersion();
        			rtu.setAsymmetricKeyVersion(++asymmetricKeyVersion>255?1:asymmetricKeyVersion);
        			masterDbService.updateGwTerminalPubKey(HexDump.toHex(rtua),terminalPubKey,asymmetricKeyVersion);
        			resultValue.setValue("00");
        		}
        	}
        	hostCommandHandler.updateGwSetResult(masterDbService,(HostCommand) value, gwmsg);
        	hostCommandHandler.updateParaTable(manageRtu, masterDbService, (HostCommand) value, rtu);
        }
		return true;
	}


	private MessageGate sendMessageToFe(MessageGw gwmsg,IMessage msg,int fseq) {
		gwmsg.setPeerAddr(msg.getPeerAddr());
		gwmsg.setSEQ((byte)fseq);
		MessageGate gateMsg = new MessageGate();
		gateMsg.setDownInnerMessage(gwmsg);
		gateMsg.getHead().setAttribute(GateHead.ATT_DOWN_CHANNEL,0);
		msgQueue.sendMessage(gateMsg);
		return gateMsg;
	}

	/**
	 * 生成request对象
	 * @param fseq
	 * @param rtu
	 * @param value
	 * @return
	 */
	private FaalGWupdateKeyRequest createRequest(int fseq, BizRtu rtu,
			Object value) {
		FaalGWupdateKeyRequest gwUpdate = new FaalGWupdateKeyRequest();
		gwUpdate.setProtocol("02");
		gwUpdate.setType(6);
		gwUpdate.setFlag(1);
		FaalRequestRtuParam param = new FaalRequestRtuParam();
		param.setRtuId(rtu.getRtuId());
		param.setTn(new int[]{0});
		param.addParam("06F020", ""+value);
		gwUpdate.setFseq(new int[]{fseq});
		gwUpdate.addRtuParam(param);
		return gwUpdate;
	}

	public final BPMessageQueue getMsgQueue() {
		return msgQueue;
	}

	public final void setMsgQueue(BPMessageQueue msgQueue) {
		this.msgQueue = msgQueue;
	}
}
