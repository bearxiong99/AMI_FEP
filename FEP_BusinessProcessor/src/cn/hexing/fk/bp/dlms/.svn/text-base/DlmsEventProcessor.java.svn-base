/**
 * 
 */
package cn.hexing.fk.bp.dlms;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.axis.utils.StringUtils;
import org.apache.log4j.Logger;

import cn.hexing.db.batch.AsyncService;
import cn.hexing.db.bizprocess.MasterDbService;
import cn.hexing.db.managertu.ManageRtu;
import cn.hexing.fas.model.FaalRequest;
import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fas.model.dlms.DlmsRequest.BUSINESS_CATEGORY;
import cn.hexing.fas.model.dlms.DlmsRequest.DLMS_OP_TYPE;
import cn.hexing.fas.model.dlms.DlmsRequest.REQUEST_MODE;
import cn.hexing.fas.model.dlms.RelayParam;
import cn.hexing.fas.model.dlms.SecurityKeyParam;
import cn.hexing.fas.model.dlms.SecurityKeyParam.KEY_CHANGE_TYPE;
import cn.hexing.fk.bp.dlms.alarm.DlmsAlarmManager;
import cn.hexing.fk.bp.dlms.cipher.EsamCipher;
import cn.hexing.fk.bp.dlms.cipher.Gcm128SoftCipher;
import cn.hexing.fk.bp.dlms.cipher.IDlmsCipher;
import cn.hexing.fk.bp.dlms.events.DlmsEvent;
import cn.hexing.fk.bp.dlms.persisit.MasterDbServiceAssistant;
import cn.hexing.fk.bp.dlms.protocol.DlmsProtocolDecoder;
import cn.hexing.fk.bp.dlms.protocol.DlmsProtocolEncoder;
import cn.hexing.fk.bp.dlms.upgrade.DlmsUpgradeAssisant;
import cn.hexing.fk.bp.dlms.upgrade.DlmsUpgradeHandler;
import cn.hexing.fk.common.events.BasicEventHook;
import cn.hexing.fk.common.simpletimer.ITimerFunctor;
import cn.hexing.fk.common.simpletimer.TimerData;
import cn.hexing.fk.common.simpletimer.TimerScheduler;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.common.spi.IMessageQueue;
import cn.hexing.fk.common.spi.socket.IChannel;
import cn.hexing.fk.exception.MessageParseException;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.bengal.BengalMessage;
import cn.hexing.fk.message.gate.GateHead;
import cn.hexing.fk.message.gate.MessageGate;
import cn.hexing.fk.message.gw.MessageGw;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.model.DlmsMeterRtu;
import cn.hexing.fk.model.MeasuredPoint;
import cn.hexing.fk.model.RtuConnectItem;
import cn.hexing.fk.model.RtuManage;
import cn.hexing.fk.tracelog.TraceLog;
import cn.hexing.fk.utils.StringUtil;
import cn.hexing.util.HexDump;

import com.hx.dlms.ASN1BitString;
import com.hx.dlms.ASN1OctetString;
import com.hx.dlms.ASN1SequenceOf;
import com.hx.dlms.ASN1Type;
import com.hx.dlms.DecodeStream;
import com.hx.dlms.DlmsData;
import com.hx.dlms.TagAdjunct;
import com.hx.dlms.aa.AareApdu;
import com.hx.dlms.aa.AarqApdu;
import com.hx.dlms.aa.AarqApdu.CipherMechanism;
import com.hx.dlms.aa.DlmsContext;
import com.hx.dlms.aa.DlmsContext.AAState;
import com.hx.dlms.applayer.ActionResult;
import com.hx.dlms.applayer.action.ActionRequest;
import com.hx.dlms.applayer.action.ActionRequestNormal;
import com.hx.dlms.applayer.action.ActionResponse;
import com.hx.dlms.applayer.action.ActionResponseNormal;
import com.hx.dlms.applayer.action.ResponseOptionalData;
import com.hx.dlms.applayer.ex.HexingExResponse;
import com.hx.dlms.applayer.get.GetRequest;
import com.hx.dlms.applayer.get.GetRequestNormal;
import com.hx.dlms.applayer.get.GetResponse;
import com.hx.dlms.applayer.set.SetResponse;
import com.hx.dlms.cipher.AESECB128;
import com.hx.dlms.cipher.CipherUtil;
import com.hx.dlms.message.DlmsMessage;

/**
 * @author Adam Bao,  hbao2k@gmail.com
 *
 */
public class DlmsEventProcessor extends BasicEventHook {
	private static final Logger log = Logger.getLogger(DlmsEventProcessor.class);
	private static final TraceLog tracer = TraceLog.getTracer("DLMS");
	
	private static final DlmsEventProcessor instance = new DlmsEventProcessor();
	public static final DlmsEventProcessor getInstance(){ return instance; }
	
	private IContextManager contextManager = LocalDlmsContextManager.getInstance();
	private IDlmsCipher cipher = Gcm128SoftCipher.getInstance();
	private IMessageQueue messageQueue = null;
	private long sessionTimeout = 50;	//Minutes
	
	private boolean isCheckSession = false;
	
	private boolean isRelayCiphered = false;
	
	/**国网中继是否需要认证*/
	private boolean isGwRelayNeedAuth = false;
	
	//Timeout re-send mechanism
	private LinkedList<String> sendingList = new LinkedList<String>();
	private int maxResend = 3;			//Max re-send counts
	private int resendInterval = 35000;	//re-send interval, milliseconds
	private int relayResendInterval = 35000;	//relay message re-send interval, milliseconds
	private int droppedTimeOut = 10*1000*60; //check meter is online
	
	private MasterDbService masterDbService;  //spring 配置实现。
	
	private AsyncService asycService;	//spring 配置实现。
	
	/**不需要认证的子规约*/
	private String[] withOutAuthSubs=null;
	
	//Master station protocol-related attributes definition
	private final byte[] msSysTitle = new byte[]{ 0x48, 0x58, 0x45, 0x11, 0, 0, 0, 0 };

	private DlmsEventProcessor(){
		super();
		timeoutAlarm = 4;
		name = "DLMS.E.P";
	}
	
	public final byte[] getSysTitle(DlmsContext context){
		if( context.isRelay ){
			ByteBuffer buf = ByteBuffer.allocate(8);
			buf.put((byte)0x48).put((byte)0x58).put((byte)0x45).put((byte)0x11);
			DlmsEvent evt = (DlmsEvent)context.webReqList.get(0);
			RelayParam param = evt.getDlmsRequest().getRelayParam();
			byte[] bytes = HexDump.toArray(param.getDcLogicalAddress());
			bytes[0] = (byte) (bytes[0] & 0x0F);
			buf.put(bytes);
			return buf.array();
		}
		return msSysTitle;
	}
	
	public final byte[] getInitVector( DlmsContext context ){
		ByteBuffer iv = ByteBuffer.allocate(12);
		iv.put(getSysTitle(context));
		int _frameCount = context.nextFrameCounter();
		iv.putInt( _frameCount );
		iv.flip();
		return iv.array();
	}
	
	@Override
	public void init(){
		if( ! initialized ){
			sessionTimeout = sessionTimeout * 60 * 1000;
			String esamCipherUrl = System.getProperty("encryption.service.url");
			if( ! StringUtils.isEmpty(esamCipherUrl) ){
				isRelayCiphered = true;
				cipher = EsamCipher.getInstance();
			}
			String strwithOutAuthSubs  = System.getProperty("bp.protocol.withNoAuth");
			if(strwithOutAuthSubs!=null){
				withOutAuthSubs = strwithOutAuthSubs.split(",");
			}
			DlmsContext.sendInterval = Math.abs(this.relayResendInterval-this.resendInterval);
			DlmsProtocolDecoder.getInstance().setEventProcessor(this);
			if( resendInterval > 500 ){
				ITimerFunctor secondTimer = new ITimerFunctor(){
					@Override
					public void onTimer(int id) {
						synchronized( sendingList ){
							
							while( sendingList.size()>0 ){
								String logicAddress = sendingList.getFirst();
								DlmsContext cxt = contextManager.getContext(logicAddress);
								if(cxt == null){
									//说明当前context已经不存在了
									sendingList.remove();
									continue;
								}
								if( cxt.curDownMessage == null ){
									sendingList.removeFirst();
									cxt.enableSend();
									try {sendNextMessage(cxt);} catch (IOException e) {}
									continue;
								}
								long interval = 0;
								//这里应该加一个最大超时时间，否则设置太长的话，会出问题。
								if(cxt.webReqList.size()==0){
									interval = cxt.isRelay ?  relayResendInterval : resendInterval;
								}else{
									DlmsEvent de = (DlmsEvent) cxt.webReqList.get(0);
									DlmsRequest dr=(DlmsRequest) de.getRequest();
									//long maxInterval = cxt.isRelay? maxRelayResendInterval:maxResendInterval; csd模式下时间也长
									//如果是中继或者链接模式是CSD，或者请求里是CSD
									interval =dr.getRequestTimeOut()==0?( (cxt.isRelay ||cxt.linkMode== IMessage.COMMUNICATION_TYPE_CSD || dr.getRequestMode()==REQUEST_MODE.ONLY_CSD)?  relayResendInterval : resendInterval):dr.getRequestTimeOut();
								}
								
								if( System.currentTimeMillis()- cxt.lastSendTime < interval ){
									break;									
								}
								logicAddress = sendingList.removeFirst();
								if( ++ cxt.resendCount >= maxResend ){
									//Send-failed
									try {
										onRequestFailed(cxt);
									} catch (IOException e) {
										log.warn(e.getLocalizedMessage(),e);
									}
								}
								else{
									cxt.waitReply.set(false);
									if(log.isInfoEnabled())
										log.info("send msg again... send time:"+cxt.resendCount+",meterId:"+cxt.getMeterId()+"\n msg:"+cxt.curDownMessage);
									sendMessage(cxt.curDownMessage,cxt);
								}
							}
						}
					}
				};
				TimerScheduler.getScheduler().addTimer(new TimerData(secondTimer,1,1));
			}
			super.init();
		}
	}
	
	public boolean sendMessage(IMessage msg, DlmsContext cxt){
		boolean result = false;
		try {
			if( !(msg instanceof MessageGw ) )
				msg.setLogicalAddress(cxt.meterId);
			if( cxt.aaMechanism == CipherMechanism.BENGAL && (msg instanceof DlmsMessage) ){
				DlmsMessage dmsg = (DlmsMessage)msg;
				BengalMessage bmsg = new BengalMessage();
				bmsg.setLogicalAddress(cxt.srcMeterId);
				bmsg.setPeerAddr(cxt.peerAddr);
				ByteBuffer iv = ByteBuffer.allocate(12);
				iv.put(getSysTitle(cxt));
				int _frameCount = cxt.nextFrameCounter();
				iv.putInt( _frameCount );
				iv.flip();
				bmsg.setRandom( HexDump.toHex(iv.array()));
				bmsg.setSeq(_frameCount);

				byte[] plain = dmsg.getRawFrame();
				try {
					if(tracer.isEnabled()){
						tracer.trace("Bengal message before encrypt:"+HexDump.toHex(plain));
					}
					//signature
					byte[] sign = cipher.auth(cxt, plain, null);
					bmsg.setSignatureData(HexDump.toHex(sign));
					//encryption
					byte[] enc = cipher.encrypt(cxt, dmsg.getRawFrame(), iv.array());
					bmsg.setData(HexDump.toHex(enc));
				} catch (IOException e) {
					log.error(StringUtil.getExceptionDetailInfo(e));
					result = false;
				}
				msg = bmsg;
			}
			if( tracer.isEnabled() ){
				StringBuilder sb = new StringBuilder();
				sb.append(">>dwlink meterid="+cxt.meterId+", peerAddr="+msg.getPeerAddr()+", msg="+msg );
				tracer.trace(sb.toString());
			}
			if( msg instanceof MessageGate )
				result= messageQueue.sendMessage(msg);
			else{
				MessageGate gate = new MessageGate();
				if(cxt.webReqList.size()!=0){
					DlmsEvent evt=(DlmsEvent) cxt.webReqList.get(0);
					if(evt.getDlmsRequest().getRequestMode()==REQUEST_MODE.ONLY_CSD){
						gate.getHead().setAttribute(GateHead.ATT_TXFS, IMessage.COMMUNICATION_TYPE_CSD);
					}
					if(cxt.phoneNum!=null){
						gate.getHead().setAttribute(GateHead.ATT_SIM_NUM, cxt.phoneNum);				
					}
				}
				gate.getHead().setAttribute(GateHead.ATT_TXFS, cxt.linkMode);
				gate.setDownInnerMessage(msg);
				//As to GuoWang relay message, 'status' attribute is used to transfer additional info.
				if( null != msg.getStatus() )
					gate.setStatus(msg.getStatus());
				result = this.messageQueue.sendMessage(gate);
			}
		} catch (Exception e) {
			log.error(StringUtil.getExceptionDetailInfo(e));
			result = false;
		}
		if(result){
			//只有发送成功，才加入到sendingList
			synchronized(sendingList){
				cxt.curDownMessage = msg;
				cxt.lastSendTime = System.currentTimeMillis();
				if(sendingList.contains(cxt.meterId)){
					sendingList.remove(cxt.meterId);
				}
				sendingList.add(cxt.getMeterId());
			}
		}else{
			log.error("send fail,enableSend. meterId is :"+cxt.meterId);
			cxt.enableSend();
		}
		return result;
	}
	
	public void postWebRequest(FaalRequest webReq, IChannel client){
		try{
			DlmsRequest req = (DlmsRequest)webReq;
			if( req.getOpType() == DLMS_OP_TYPE.OP_CHANGE_KEY ){
				String deviceAddr = req.getMeterId();
				String dcAddr = null;
				boolean dcKeyChange = false;
				if(req.isRelay()){
					if(req.getRelayParam().getMeasurePoint()==0)
						dcKeyChange = true;
				}
				//Special handling for DC KEY-CHANGE action.
				if( dcKeyChange ){
					RelayParam rp = req.getRelayParam();
					if( null == rp ){
						log.error("DC key-change request error, relayParam is null.");
						return;
					}
					if( "gw".equals(rp.getProtocol()) ){
						//construct GW message to update DC's key.
						log.warn("GuoWang DC key-change request under developping.");
						return;
					}
					else{
						log.error("DC key-change can not support protocol:"+rp.getProtocol());
						return;
					}
				}
				if(req.isRelay()){
					RelayParam relayParam = req.getRelayParam();
					BizRtu br=RtuManage.getInstance().getBizRtuInCache((int)Long.parseLong(relayParam.getDcLogicalAddress(),16));
					MeasuredPoint mp = br.getMeasuredPoint(relayParam.getMeasurePoint()+"");
					if(mp == null){
						log.error("no this measurepoint!");
						return;
					}
					deviceAddr = mp.getTnAddr();
					dcAddr = relayParam.getDcLogicalAddress();
					req.setMeterId(deviceAddr);
				}
				DlmsContext cxt = contextManager.getContext(deviceAddr);
				if(dcAddr!=null){
					cxt=contextManager.getContext(dcAddr);
				}
				if( null == cxt && null == req.getRelayParam() ){
					onChangeKeyFailed(req,client);
					return;
				}
				if( null != cxt && cxt.aaMechanism == CipherMechanism.BENGAL ){
					begalKeyChange(client, req, cxt);
					return;
				}
				else if( null != cxt ){
					if(dcAddr!=null){
						cxt=cxt.subContexts.get(deviceAddr);
					}
					cxt.keyChangeRequest = req;
					req.setOperator("KeyChangeRequest");
					//Special action for keyChange
					// Change keys class=64, OBIS = 0-0:43.0.0.255, method=2
					// For ESAM, data is Data=array[3] structure{}
					KEY_CHANGE_TYPE kct = req.getKeyParam().getKeyChangeType();
					switch( kct ){
					case ESAM_GCM_KEYS:
						esamGcmKeyChange(req, cxt);
						break;
					case ESAM_KEYPAIR:
						break;
					case ESAM_KEY_MS:
						break;
					case SOFT_AK:
					case SOFT_EK:
					case SOFT_EKAK:
					default:
						break;
					}
				}
				else{
					// null == context
				}
				//Handle key-change event by DLMS way.
			}else if(req.getOpType() == DLMS_OP_TYPE.OP_UPGRADE){//软件升级
				if(!req.containsKey("UpgradeId")){
					log.error("when you upgrade,make sure appendparam contains UpgradeId");
					return ;
				}
				if(!DlmsUpgradeHandler.getInstance().upgradeProcesser(req)){
					return ;
				}
			}
		}catch(Exception e){
			log.error(StringUtil.getExceptionDetailInfo(e));
		}
		DlmsEvent evt = new DlmsEvent(webReq);
		evt.setSource(client);		//the reply may send back to WEB-client-channel directly.
		postEvent(evt);
	}

	private void onChangeKeyFailed(DlmsRequest req, IChannel client){
		DlmsContext cxt = contextManager.getContext(req.getMeterId());		
		if(req.isRelay()){
			cxt = contextManager.getContext(req.getRelayParam().getDcLogicalAddress());
		}
		if( null == cxt ){
			//TODO:  not online
			return;
		}
		if(cxt.isRelay){
			cxt = cxt.subContexts.get(req.getMeterId());
		}
		cxt.keyChangeRequest = null;
	}
	
	public void postUpBengalMessage(BengalMessage bmsg){
		if( bmsg.isHeartbeat() )
			return;
		//decrypt BengalMessage and create DlmsMessage
		String meterId = "000000000000".substring(bmsg.getLogicalAddress().length())+bmsg.getLogicalAddress();
		String peerAddr = bmsg.getPeerAddr();
		DlmsContext cxt = contextManager.getContext(meterId);
		DlmsContext cxt1 = contextManager.getContextByAddr(peerAddr);
		boolean refeshContext = (null==cxt||null==cxt1 || cxt!=cxt1)?true:false;
		if( refeshContext){
			cxt = new DlmsContext();
			//update AARE context.
			cxt.setMaxPduSize(500);
			ASN1BitString conformance = new ASN1BitString();
			conformance.setInitValue(new byte[]{ (byte)0x00,(byte)0x50,(byte)0x1F});
			cxt.setConformance(conformance);
			cxt.meterId = meterId;
			cxt.peerAddr = bmsg.getPeerAddr();
			cxt.srcMeterId = bmsg.meterAddress;
			cxt.aaMechanism = CipherMechanism.BENGAL;
			cxt.aaState = AAState.AA_OK;
			//TODO: load assets information from DB, including soft-encryption keys
			//TODO: Load cipher-machine information if cipher-machine applied.
			initKey(meterId, cxt);
			cxt.authKey = null;
			contextManager.updateOrSetContext(meterId, cxt);
		}
		if( bmsg.getFuncCode() == 1 )
			return;
		if( bmsg.getFuncCode() == 4 ){ //Confirmation or deny.
			String result = bmsg.getData();
			DlmsRequest req = (DlmsRequest)cxt.keyChangeRequest;
			if( result.length()>=1 && result.substring(0,1).equals("0") && req!=null){
				if( req != null && req.getKeyParam() != null ){
					SecurityKeyParam kparam = req.getKeyParam();
					byte[] nkey = kparam.getNewEncKey();
					log.error("meterId="+meterId+",INFO** key change success. old="+HexDump.toHex(cxt.encryptKey)+",new="+HexDump.toHex(nkey));
					//TODO: Save new key into DB...
					MasterDbServiceAssistant.getInstance().updateNewEncKeyCommand(req, HexDump.toHex(nkey),"0");
					//更新缓存中的数据
					DlmsMeterRtu dlmsRtu=RtuManage.getInstance().getDlmsMeterRtu(meterId);
					dlmsRtu.setHighPassword(HexDump.toHex(nkey));
					cxt.encryptKey = nkey;
				}
				cxt.keyChangeRequest = null;
				return;
			}
			else{
				String reason = "unknow reason";
				if( result.length()>1 ){
					String errCode = result.substring(1, 2);
					if( errCode.equals("1") ){
						log.error("meterID:"+cxt.meterId+",password error,reload db.before encKey="+HexDump.toHex(cxt.encryptKey));
						ManageRtu.getInstance().refreshDlmsMeterRtu(meterId);
						initKey(cxt.meterId, cxt);
						cxt.authKey = null;
						reason = "password error";						
					}
					else if( errCode.equals("2") )
						reason = "auth error";
					else
						reason = "other error";
					if(req!=null){
						MasterDbServiceAssistant.getInstance().updateNewEncKeyCommand(req, null,errCode);						
					}else{
						log.error("meterId"+meterId+",return msg is error. reason="+reason);
					}
				}
	
				if( req != null && req.getKeyParam() != null ){	//Key change
					SecurityKeyParam kparam = req.getKeyParam();
					byte[] nkey = kparam.getNewEncKey();
					log.error("meterId"+meterId+",key change failed. reason="+reason +", old="+HexDump.toHex(cxt.encryptKey)+",new="+HexDump.toHex(nkey));
				}
				cxt.keyChangeRequest = null;
				return;
			}
		}
		byte[] cihperMsg = HexDump.toArray(bmsg.getData());
		byte[] plainMsg = null;
		try {
			plainMsg = cipher.decrypt(cxt, cihperMsg, HexDump.toArray(bmsg.getRandom()));
			if(tracer.isEnabled()){
				tracer.trace("Receive msg after decrypt:"+HexDump.toHex(plainMsg));
			}
		} catch (IOException e) {
			log.error(StringUtil.getExceptionDetailInfo(e));
		}
		if( null == plainMsg )
			return;
		DlmsMessage msg = new DlmsMessage();
		msg.setIoTime(bmsg.getIoTime());
		msg.setLogicalAddress(bmsg.getLogicalAddress());
		msg.setPeerAddr(bmsg.getPeerAddr());
		msg.setSource(bmsg.getSource());
		msg.setTask(bmsg.isTask());
		msg.setTxfs(bmsg.getTxfs());
		
		try {
			msg.read(ByteBuffer.wrap(plainMsg));
		} catch (MessageParseException e) {
			log.error(StringUtil.getExceptionDetailInfo(e));
			return;
		}
		
		DlmsEvent evt = new DlmsEvent(msg);
		evt.setSource(bmsg.getSource());
		postEvent( evt );
	}

	private void initKey(String meterId, DlmsContext cxt) {
		DlmsMeterRtu dlmsRtu=RtuManage.getInstance().getDlmsMeterRtu(meterId);
		if(dlmsRtu==null){
			//如果为空，刷新档案
			dlmsRtu=ManageRtu.getInstance().refreshDlmsMeterRtu(meterId);
			if(dlmsRtu==null){
				log.error("load db failed,no meter="+meterId+",file");
				return ;
			}
		}

		cxt.subProtocol = dlmsRtu.getSubProtocol();
		String encKey=dlmsRtu.getHighPassword();
		if(encKey==null || encKey.length()!=32){
			cxt.encryptKey = CipherUtil.ZERO_KEY;	
			log.warn("meter="+meterId+" possword is null or length!=16bytes,use default password");
		}else{
			cxt.encryptKey = HexDump.toArray(encKey);
			log.warn("meter="+meterId+" load password successful,password:"+encKey);
		}
		
		String authKey = dlmsRtu.getLowPassword();
		if(authKey ==null || authKey.length()!=32){
			cxt.authKey = CipherUtil.ZERO_KEY;	
			log.warn("auth password is null or length!=16bytes,use default auth auth password");
		}else{
			cxt.authKey = HexDump.toArray(authKey);
			log.warn("load auth password success,password:"+authKey);
		}
		//cxt.keyVersion
		int meterType = 0;
		try {
			meterType = Integer.parseInt(dlmsRtu.getWiringMode());
		} catch (NumberFormatException e) {
			log.warn(meterId+" meterType is null!");
		}
		if(meterType ==1){
			cxt.meterType = meterType;
		}else if(meterType==3 || meterType ==4){
			cxt.meterType = 3;
		}else{
			cxt.meterType = 0;
		}
		
		String keyVersion=dlmsRtu.getKeyVersion();
		if(null!=keyVersion && !"".equals(keyVersion)){
			cxt.keyVersion = Integer.parseInt(keyVersion);
		}else{
			cxt.keyVersion = 0;
		}
	}
	
	public void postUpDlmsMessage(final DlmsMessage msg){
		final DlmsEvent evt = new DlmsEvent(msg);
		if( !msg.isHeartbeat() && tracer.isEnabled() ){
			StringBuilder sb = new StringBuilder();
			sb.append("<<uplink meterid="+msg.getLogicalAddress()+", peerAddr="+msg.getPeerAddr()+", msg="+msg );
			tracer.trace(sb.toString());
		}
		evt.setSource(msg.getSource());
		postEvent(evt);
	}
	
	/**
	 * Called by BPProcessor when relay message is replied.
	 * @param relayMessage
	 */
	public boolean postUpRelayMessage(IMessage relayMessage){
		if( relayMessage instanceof MessageGw ){
			MessageGw gwmsg = (MessageGw)relayMessage;
			ByteBuffer data = gwmsg.data;
			//然后消去无用的数据：00000100：数据项标识  + 01：端口号 + 1300: 透明转发内容字节数
			int pos = 7;
			data.position(pos);
			byte firstByte=data.get();
			byte secondByte=data.get();
			if(firstByte == 0x68 && secondByte!=00) return false; //混接处理  
			pos = 7;
			data.position(pos);
			int measurePoint = data.get() & 0xFF;
			if( measurePoint<0xC0 )
				measurePoint = (data.get() & 0xFF)<<8 | measurePoint;
			else{
				data.position(pos);
				measurePoint = 2;
			}
			
			String meterAddr =getRelayMeterAddr(gwmsg.getLogicalAddress(),measurePoint);
			if(meterAddr == null ){
				return false;
			}
			byte[] apdu = new byte[data.remaining()];
			data.get(apdu);
			DlmsMessage msg = new DlmsMessage();
			msg.setIoTime(gwmsg.getIoTime());
			msg.setLogicalAddress(meterAddr);
			msg.setDcLogicAddress(gwmsg.getLogicalAddress());
			msg.setPeerAddr(gwmsg.getPeerAddr());
			msg.setSource(relayMessage.getSource());
			msg.setTxfs(gwmsg.getTxfs());
			msg.setApdu(apdu);
			
			DlmsEvent evt = new DlmsEvent(msg);
			evt.setSource(relayMessage.getSource());
			handleEvent(evt);
			return true;
		}
		return false;
	}
	
	private String getRelayMeterAddr(String dcLogicAddr , int measurePoint){
		BizRtu br=RtuManage.getInstance().getBizRtuInCache((int)Long.parseLong(dcLogicAddr,16));
		if(br==null) {
			throw new RuntimeException("can't not find this terminal,dcLogicAddr:"+dcLogicAddr);
		}
		MeasuredPoint mp = br.getMeasuredPoint(measurePoint+"");
		if(mp == null){
			//重新数据库查询
			ManageRtu.getInstance().refreshBizRtu(dcLogicAddr);
			br=RtuManage.getInstance().getBizRtuInCache((int)Long.parseLong(dcLogicAddr,16));
			mp = br.getMeasuredPoint(measurePoint+"");
			if(mp==null){
				throw new RuntimeException("no this measurepoint! dcLogicAddr:"+dcLogicAddr+",mp:"+measurePoint);
			}
		}
		return mp.getTnAddr();
	}
	
	public void postMeterChannelClosed(String meterId){
		//Take it simple.
		DlmsContext cxt = contextManager.getContext(meterId);
		//TODO: clean context.
		if( null != cxt ){
			if(tracer.isEnabled())
				tracer.trace("Meter closed: "+meterId);
			//auto reply the remaining web request.
		}
		contextManager.removeContext(meterId);
	}
	
	public void onRequestFailed(DlmsContext context) throws IOException {
		//First, remove the completed request.
		DlmsEvent evt = null;
		synchronized (sendingList) {
			if( context.webReqList.size()>0 )
				evt = (DlmsEvent)context.webReqList.remove(0);
		}
		int resendCount = context.resendCount;

		if(context.aaState!=AAState.AA_OK){
			//request failed then reset aastate
			context.aaState = AAState.INIT;
		}
		
		if(evt!=null&&evt.getDlmsRequest()!=null&&evt.getDlmsRequest().getOperator()!=null){
			if(evt.getDlmsRequest().getOperator().contains("UPGRADE")){
				DlmsUpgradeAssisant.getInstance().onUpgradeFailed(evt.getDlmsRequest());
			}
		}
		
		if(contextManager.getContext(context.meterId)==null){
			log.error("contextManager not contains this meter Context,meterId:"+context.getMeterId());
			return;			
		}
		ArrayList<IMessage> downMessages = context.reqDownMessages;
		context.onRequestFinished();
		//Second, send next message to METER if possible.
		try {
			sendNextMessage(context);
		} catch (IOException exp) {
			log.error("onRequestComplete,meterId:"+context.meterId+" sendNextMessage:",exp);
		}
		
		if( null == evt ){
			return;
		}
		DlmsRequest dr = evt.getDlmsRequest();
		dr.setOvertime(true);
		dr.setReqDownMessages(downMessages);
		String meterInfo = "";
		if(dr.isRelay()){
			RelayParam rp=dr.getRelayParam();
			meterInfo=rp.getDcLogicalAddress()+" tn="+rp.getMeasurePoint();
		}else{
			meterInfo =dr.getMeterId();
		}
		log.warn("request failed, meterId="+meterInfo+", operator="+evt.getDlmsRequest().getOperator()+", resendCount="+resendCount+",invokeId:"+evt.getDlmsRequest().getInvokeId()+"size of sendList : "+context.webReqList.size());
		//TODO: mock the request complete action and the result is request failed to avoid web-client waiting.
	}
	
	/**
	 * Called by DlmsProtocolDecoder when GET/SET/ACTION request complete.
	 * In this function, request result is populated and save to DB and/or send to WEB.
	 * @param context
	 * @throws IOException 
	 */
	public void onRequestComplete(DlmsContext context,DlmsEvent event) throws IOException{
		onRequestComplete(context, 0,event);
	}
	public void onRequestComplete(DlmsContext context,int invokeId,DlmsEvent upEvent) throws IOException {
		 //First, remove the completed request.
		DlmsEvent evt = null;

		DlmsEvent currentEvent =null;

		boolean isMatch = false;

		synchronized (sendingList) {
			DlmsMessage dlmsMsg=(DlmsMessage) upEvent.getMessage();
			currentEvent = (DlmsEvent) context.getHistoryEvents(dlmsMsg.getLogicalAddress()).get(invokeId);
			
			if( context.webReqList.size()>0 ){
				evt = (DlmsEvent)context.webReqList.get(0);	
				int expectInvokeId = evt.getDlmsRequest().getInvokeId();
				if(expectInvokeId!=invokeId)
					log.warn("return msg's invokeId:"+invokeId+",expectInvokeId:"+expectInvokeId+",meterId:"+context.meterId);
			}
			if(invokeId == 0x81)
				isMatch =true;
			else
				isMatch = evt==currentEvent;
			
			if (isMatch || currentEvent == null) {
				if (sendingList.contains(context.meterId)) {
					sendingList.remove(context.meterId);
				}
				if(context.webReqList.size()>0){
					context.webReqList.remove(0);
				}
				context.onRequestFinished();

			} else {
				evt = currentEvent;
			}
			
		}
		
		if( null == evt){
			log.error("onRequest complete evt is null,meterId:"+context.getMeterId()+",invokeId:"+invokeId);
			return;
		}
		
		DlmsRequest req = evt.getDlmsRequest();
		
		setRecvTime(upEvent.getMessage(), req, context);

		if("ReadKeyVersion".equals(req.getOperator())){
			if(req.getParams()[0].resultData ==null) return;
			int keyVersion=req.getParams()[0].resultData.getUnsigned();
			if(keyVersion!=context.keyVersion){
				context.keyVersion = keyVersion;
				MasterDbServiceAssistant.getInstance().updateEsamKeyVersion(req, context);
			}
			if(context.aaState !=AAState.AA_OK ){  //if not aa_ok,then send aarq.
				if(context.isRelay){
					if(context.webReqList.size()>0){
						sendAARQ(context);						
					}
				}else{
					sendAARQ(context);
				}
			}
			if(context.aaState==AAState.AA_OK)
				sendNextMessage(context);
			return ;
		}
		//Second, send next message to METER if possible.
		
		if(isMatch){
			try {
				sendNextMessage(context);
			} catch (IOException exp) {
				log.error("onRequestComplete,meterId:"+context.meterId+" sendNextMessage:",exp);
			}			
		}
		
		if(null!=req.getOperator()&&req.getOperator().contains("UPGRADE")){
			DlmsUpgradeHandler.getInstance().handleUpgrade(this, req,context);
			return;
		}
		
		//Key-Change-Request
		if( context.keyChangeRequest != null || "KeyChangeRequest".equals(req.getOperator())){
			//Save to DB
			context.keyChangeRequest = null;
			MasterDbServiceAssistant.getInstance().updateEsamKeyVersion(req,context);
			return;
		}
		
		if(!MasterDbServiceAssistant.getInstance().operationRequest(context, req)) return;
		
		IChannel channel = (IChannel)evt.getSource();
		MessageGate mgate = new MessageGate();
		mgate.setDataObject(req);
		mgate.getHead().setCommand(MessageGate.FE_MASTER_REP);
		if( channel ==null || ! channel.send(mgate) ){
			log.error("Send reply to WEB failed.");
		}
	}

	public final void setContextManager(IContextManager contextManager) {
		this.contextManager = contextManager;
	}

	public final void setMessageQueue(IMessageQueue bpMessageQueue) {
		this.messageQueue = bpMessageQueue;
	}
	
	@Override
	public void handleEvent(IEvent event) {
		try{
			if( event instanceof DlmsEvent ){
				DlmsEvent evt = (DlmsEvent)event;
				switch(evt.eventType()){
				case UNDEF:
					break;
				case UP_DLMS_MSG:
					handleUpDlmsMessage(evt);
					break;
				case WEB_REQ:
					handleWebReq(evt);
					break;
				default:
					break;
				}
			}
			else{
				super.handleEvent(event);
			}
		}catch(Exception exp){
			log.warn(exp.getLocalizedMessage(),exp);
		}
	}
	
	public void sendNextMessage(DlmsContext context) throws IOException{
		if(context.webReqList.size()<=0) 
			return;
		if (context.aaState != AAState.AA_OK) {
			log.warn("sendNextMessage,but AAstate is not OK,so send AARQ.meterId:"+context.meterId);
			sendAARQ(context);
			return;
		}
		
		if( context.waitReply.compareAndSet(false, true) ){
			DlmsEvent evt = (DlmsEvent)context.webReqList.get(0);
			
			DlmsRequest dlmsRequest = evt.getDlmsRequest();
			if(dlmsRequest.isOverTime()){
				context.reqDownMessages = dlmsRequest.getReqDownMessages();
			}
			if( context.reqDownMessages.size() == 0  ){
				if(context.webReqList.size()>0 ){
					//Build DLMS messages for this request event.
					try {
						DlmsProtocolEncoder.getInstance().build(evt.getRequest(), context);
					} catch (Exception e) {
						onRequestFailed(context);
						log.error("sendNextMessage occur exception.Build frame error!",e);
					}
				}
			}
			if( context.reqDownMessages.size()>0 ){
				IMessage msg = context.reqDownMessages.remove(0);
				sendMessage(msg,context);
			}
			else{
				context.waitReply.set(false);
			}
		}
	}
	
	private void handleNotOnline(DlmsEvent evt){
		String meterId = evt.getDlmsRequest().getMeterId();
		String peerIp = evt.getDlmsRequest().getPeerIp();
		log.warn("Not online: meter="+meterId+", peerId="+peerIp);
		//The best way is to reply WEB with not-online.
		//TODO: save error code
		if(evt!=null&&evt.getDlmsRequest()!=null&&evt.getDlmsRequest().getOperator()!=null){
			if(evt.getDlmsRequest().getOperator().contains("UPGRADE")){
				DlmsUpgradeAssisant.getInstance().onUpgradeFailed(evt.getDlmsRequest());
			}
		}
	}
	
	private boolean checkContextTimeout(DlmsContext context){
		boolean timeout = false;
		if( isCheckSession && !context.isRelay && context.lastSendTime !=0 && Math.abs(System.currentTimeMillis()-context.lastSendTime)> this.sessionTimeout){
			//Session Timeout
			context.reset();
			timeout = true;
		}
		return timeout;
	}
	
	private void sendAARQ(DlmsContext context) throws IOException{
		AarqApdu aarq = AarqApdu.create(context.aaMechanism);
		switch( context.aaMechanism ){
		case HLS_2:
			break;
		case HLS_GMAC:{
			//set SysTitle.
			if(cipher instanceof EsamCipher){//对于硬加密的表，发送8个字节的随机数。
				aarq.createRandomAuthenticationValueWith8Bytes();
			}
			aarq.setCallingApTitle(getSysTitle(context));
			ByteBuffer iv = ByteBuffer.allocate(12);
			iv.put(getSysTitle(context));
			int _frameCount = context.nextFrameCounter();
			iv.putInt( _frameCount );
			iv.flip();
			byte[] initRequest = aarq.getInitiateRequest();
			byte[] cipherInitRequest = cipher.encrypt(context, initRequest, iv.array() );
			ByteBuffer cipherContext = ByteBuffer.allocate(cipherInitRequest.length+5);
			//Security head + frameCounter + cipherText + authenticationTag
			cipherContext.put((byte)0x30).putInt(_frameCount).put(cipherInitRequest);
			cipherContext.flip();
			
			//glo-initiateRequest [33] IMPLICIT OCTET STRING,
			ASN1OctetString cipheredRequest = new ASN1OctetString();
			cipheredRequest.setBerCodec();
			TagAdjunct myAdjunct = TagAdjunct.contextSpecificImplicit(33);
			myAdjunct.axdrCodec(true);
			cipheredRequest.setTagAdjunct(myAdjunct);
			cipheredRequest.setValue(cipherContext.array());
			aarq.setInitiateRequest(cipheredRequest.encode());
		}
			break;
		case HLS_MD5:
			break;
		case HLS_SHA1:
			break;
		case LLS:
			break;
		case NO_SECURITY:
			break;
		default:
			break;
		
		}
		
		if( tracer.isEnabled() ){
			StringBuilder sb = new StringBuilder();
			sb.append("AARQ meterid="+ context.meterId + ": " + aarq );
			tracer.trace(sb.toString());
		}
		if(context.isRelay && isRelayCiphered){
			DlmsEvent evt=(DlmsEvent) context.webReqList.get(0);
			DlmsRequest req = evt.getDlmsRequest();
			RelayParam param = req.getRelayParam();
			byte[] apdu = aarq.encode();
			MessageGw gwmsg = DlmsProtocolEncoder.createRelayMsg(context, apdu, param);
			sendMessage(gwmsg, context);
			context.aaState = AAState.WAIT_AARE;

			if(log.isInfoEnabled())
				log.info("send relay AARQ :"+gwmsg+",rtuId:"+context.getMeterId());
			return;
		}
		context.aaState = AAState.WAIT_AARE;
		DlmsMessage msg = new DlmsMessage();
		msg.setApdu(aarq.encode());
		msg.setPeerAddr(context.peerAddr);
		sendMessage(msg,context);
	}
	
	private void handleWebReq(DlmsEvent evt) throws IOException{
		DlmsRequest dlmsRequest = evt.getDlmsRequest();
		boolean isRelay = dlmsRequest.isRelay();
		String meterAddr = null;
		String dcLogicAddr = null;
		if( isRelay ){
			RelayParam relayParam = dlmsRequest.getRelayParam();
			dcLogicAddr = relayParam.getDcLogicalAddress();
			meterAddr =getRelayMeterAddr(relayParam.getDcLogicalAddress(),relayParam.getMeasurePoint());
			if(meterAddr == null){
				return;
			}
		}
		else{
			meterAddr = dlmsRequest.getMeterId();
		}
		DlmsMeterRtu dm = RtuManage.getInstance().getDlmsMeterRtu(meterAddr);

		//csd,external,normal
		requestModeCheck(dlmsRequest, dm);
		
		DlmsContext context = contextManager.getContext(isRelay?dcLogicAddr:meterAddr);
		if( null == context && ! isRelay ){
			if(!nullContextProcess(evt, dlmsRequest, meterAddr, dm,context))
				return ;
		}else if(context!=null){
			if(!contextProcess(evt, dlmsRequest, dm, context))
				return ;
		}
		if( isRelay ){
			if(!relayContextProcess(evt, dlmsRequest, meterAddr, dcLogicAddr))
				return ;
		}
		
		
		context = contextManager.getContext(isRelay?dcLogicAddr:meterAddr);
		
		DlmsMeterRtu rtu=RtuManage.getInstance().getDlmsMeterRtu(meterAddr);
		if(rtu!=null){
			dlmsRequest.setMeterModel(rtu.getMeterMode());	
			context.phoneNum = rtu.getPhoneNum();
		}
		
		context.webReqList.add(evt);
		if( context.aaState == AAState.AA_OK ){
			if(!checkContextTimeout(context)){
				sendNextMessage(context);
				return;
			}
		}
		//如果不是AAOK,发送AARQ
		if(context.aaState!=AAState.AA_OK || checkContextTimeout(context)){
			sendAARQ(context);
		}
	}

	/**
	 * 中继context处理
	 * 一般来说，中继不会出现大批量请求，所以此处方法上加上synchronized
	 * @param evt
	 * @param dlmsRequest
	 * @param meterAddr
	 * @param context
	 * @return true -- continue process or unprocess
	 * @throws IOException
	 */
	private synchronized boolean relayContextProcess(DlmsEvent evt, DlmsRequest dlmsRequest,
			String meterAddr,String dcLogicAddr) throws IOException {
		//中继是以终端逻辑地址作为key放在contextManager
			//每一个终端逻辑地址的context里，是以meterId为key的context
		//当判断某一个终端的context不存在的时候，要加上,要先加上终端的context，然后再造出一个表计的context
		//当某一个终端的context存在的时候，要去寻找subContext下是否有当前测量点请求的meterId,如果没有要加上
		DlmsContext context  = contextManager.getContext(dcLogicAddr);
		DlmsMeterRtu dmr=RtuManage.getInstance().getDlmsMeterRtu(meterAddr);
		if(dmr == null &&(dmr=ManageRtu.getInstance().refreshDlmsMeterRtu(meterAddr))==null){
			throw new RuntimeException("Can't Find File ,Measure Point:"+context.measurePoint+",meterAddr:"+meterAddr);
		}
		
		if(null == context){
			RelayParam relayParam=dlmsRequest.getRelayParam();
			context = new DlmsContext();
			context.meterId = relayParam.getDcLogicalAddress();
			context.isRelay = true;
			context.dcLogicAddr = relayParam.getDcLogicalAddress();
			context.measurePoint = relayParam.getMeasurePoint();
			context.port = dmr.getPort()==0?31:dmr.getPort();
			contextManager.updateOrSetContext(context.meterId, context);
			
			if(isRelayCiphered ){
				//发送AARQ直接从HLS_GMAC开始
				context.aaMechanism = CipherMechanism.HLS_GMAC;
				if(null==dmr.getKeyVersion() || "".equals(dmr.getKeyVersion())) dmr.setKeyVersion("0");
				context.keyVersion = Integer.parseInt(dmr.getKeyVersion());
				
				if(isGwRelayNeedAuth){
					//如果需要认证，发送AARQ
					context.webReqList.add(0,evt);
					sendAARQ(context);
					return false;
				}
				//不需要认证，systitle需要计算得到或从数据库获得。
				context.meterSysTitle = getMeterSystitle(context, dmr);
				if(log.isDebugEnabled())
					log.debug("Relay Message! LogicAddress is "+context.getMeterId()+" MeterSystitle is:"+HexDump.toHex(context.meterSysTitle));
			}
			ASN1BitString conformance = new ASN1BitString();
			conformance.setInitValue(new byte[]{ (byte)0x00,(byte)0x50,(byte)0x1F});
			context.setConformance(conformance);
			context.aaState = AAState.AA_OK;
			DlmsContext plcContext = new DlmsContext();
			plcContext.update(context);
			plcContext.isRelay = true;
			plcContext.setMeterId(meterAddr);
			context.subContexts.put(meterAddr, plcContext);
		}else if(!context.subContexts.containsKey(meterAddr)){
			//存在当前逻辑地址的context,但不存在当前的meterAddr
			RelayParam relayParam=dlmsRequest.getRelayParam();
			DlmsContext plcContext = new DlmsContext();
			plcContext = new DlmsContext();
			plcContext.meterId = meterAddr;
			plcContext.dcLogicAddr = relayParam.getDcLogicalAddress();
			plcContext.measurePoint = relayParam.getMeasurePoint();
			plcContext.port = dmr.getPort()==0?31:dmr.getPort();
			context.subContexts.put(meterAddr, plcContext);
			contextManager.updateOrSetContext(context.meterId, context);
			if(isRelayCiphered ){
				//发送AARQ直接从HLS_GMAC开始
				plcContext.aaMechanism = CipherMechanism.HLS_GMAC;
				if(null==dmr.getKeyVersion() || "".equals(dmr.getKeyVersion())) dmr.setKeyVersion("0");
				plcContext.keyVersion = Integer.parseInt(dmr.getKeyVersion());
				
				if(isGwRelayNeedAuth){
					//如果需要认证，发送AARQ
					context.webReqList.add(0,evt);
					sendAARQ(context);
					return false;
				}
				//不需要认证，systitle需要计算得到或从数据库获得。
				plcContext.meterSysTitle = getMeterSystitle(plcContext, dmr);
				if(log.isDebugEnabled())
					log.debug("Relay Message! LogicAddress is "+plcContext.getMeterId()+" MeterSystitle is:"+HexDump.toHex(plcContext.meterSysTitle));
			}
			ASN1BitString conformance = new ASN1BitString();
			conformance.setInitValue(new byte[]{ (byte)0x00,(byte)0x50,(byte)0x1F});
			plcContext.setConformance(conformance);
			plcContext.aaState = AAState.AA_OK;
			plcContext.update(context);
			plcContext.isRelay = true;
			plcContext.setMeterId(meterAddr);
			context.subContexts.put(meterAddr, plcContext);
		}
		context.port = dmr.getPort();
		DlmsRequest req = dlmsRequest;
		req.setMeterId(meterAddr);
		if( req.getOpType() == DLMS_OP_TYPE.OP_CHANGE_KEY ){
			context.subContexts.get(meterAddr).keyChangeRequest = req;
			req.setOperator("KeyChangeRequest");
		}
		return true;
	}

	/**
	 * 下行消息处理context
	 * @param evt
	 * @param dlmsRequest
	 * @param dm
	 * @param context
	 * @return
	 */
	private boolean contextProcess(DlmsEvent evt, DlmsRequest dlmsRequest,
			DlmsMeterRtu dm, DlmsContext context) {
		if(	context.linkMode==IMessage.COMMUNICATION_TYPE_CSD || 
			dlmsRequest.getRequestMode()!=REQUEST_MODE.ONLY_GPRS){
			
			//if csd_grps mixture check the meter is online. if not use csd 
			if(dlmsRequest.getRequestMode()==REQUEST_MODE.CSD_GPRS_MIXTURE && checkDropped(context)){
				dlmsRequest.setRequestMode(REQUEST_MODE.ONLY_CSD);						
			}
			
			if(	dm!=null && 
				dm.getCsdStatus() !=IMessage.COMMUNICATION_TYPE_CSD && 
				context.linkMode==IMessage.COMMUNICATION_TYPE_CSD){
				//if mode channged, means notOnLine,remove context from manager
				if(log.isInfoEnabled())
					log.info("Current Request Mode is CSD or CSD_GPRS_MIXTURE,But database file Can't Support,meterID:"+context.getMeterId());
				contextManager.removeContext(context.meterId);
				handleNotOnline(evt);
				return false;
			}
		}else if(dm!=null && dm.getLinkMode()==IMessage.COMMUNICATION_TYPE_EXTERNAL && 
				!StringUtil.isEmptyString(dm.getIPandPort())&&
				!dm.getIPandPort().equals(context.peerAddr)){
			//如果请求模式是外拨请求
			//判断请求的peerAddr是否更改,如果更改，相当于重新认证context，重新更新
			context = new DlmsContext();
			context.linkMode = dm.getCsdStatus();
			context.meterId = dm.getLogicAddress();
			context.peerAddr=dm.getFixIp()+":"+dm.getFixPort();
			initKey(dm.getLogicAddress(), context);
			contextManager.updateOrSetContext(dm.getLogicAddress(), context);
		}else{
			if(checkDropped(context)){
				contextManager.removeContext(context.meterId);
				log.warn("Without Communication is too long ,Remove Context.meterId is "+context.meterId);
				handleNotOnline(evt);
				return false;
			}
		}
		return true;
	}

	/**
	 * 下行消息处理空context
	 * @param evt
	 * @param dlmsRequest
	 * @param meterAddr
	 * @param dm
	 * @param context
	 * @return true continue process or return
	 */
	private boolean nullContextProcess(DlmsEvent evt,
			DlmsRequest dlmsRequest, String meterAddr, DlmsMeterRtu dm,
			DlmsContext context) {
		// TODO: Acquire the assets of meter. If the protocol is DLMS and SMS awaking.
		if(dm!=null && 
		(dm.getCsdStatus()==IMessage.COMMUNICATION_TYPE_CSD 
		|| dm.getCsdStatus()==IMessage.COMMUNICATION_TYPE_CSDorGPRS)
		&& dlmsRequest.getRequestMode()!=REQUEST_MODE.ONLY_GPRS){
			dlmsRequest.setRequestMode(REQUEST_MODE.ONLY_CSD);
			synchronized (dm) {
				//如果进入锁定区域，仍然查找不到context，则向contextManager添加数据
				if(contextManager.getContext(meterAddr) != null) return true;
				
				context = new DlmsContext();
				context.meterId = meterAddr;
				context.phoneNum = dm.getPhoneNum();
				context.linkMode = dm.getCsdStatus();
				ASN1BitString conformance = new ASN1BitString();
				conformance.setInitValue(new byte[]{ (byte)0x00,(byte)0x50,(byte)0x1F});
				context.setConformance(conformance);
				context.aaState = AAState.AA_OK;
				contextManager.updateOrSetContext(meterAddr, context);
			}
		}else if(dm!=null && dm.getCsdStatus()==IMessage.COMMUNICATION_TYPE_EXTERNAL && !StringUtil.isEmptyString(dm.getFixIp())){
			synchronized (dm) {
				//如果进入锁定区域，仍然查找不到context，则向contextManager添加数据
				if(contextManager.getContext(meterAddr) != null) return true;
				context = new DlmsContext();
				context.linkMode = dm.getCsdStatus();
				context.meterId = dm.getLogicAddress();
				context.peerAddr=dm.getFixIp()+":"+dm.getFixPort();
				initKey(dm.getLogicAddress(), context);
				contextManager.updateOrSetContext(dm.getLogicAddress(), context);
			}
		}else{
			//Otherwise, the meter not on-line. no-channel to send request.
			handleNotOnline(evt);
			return false;
		}
		return true;
	}

	private void requestModeCheck(DlmsRequest dlmsRequest, DlmsMeterRtu dm) {
		//数据库档案只有是csd或者csd&gprs才可以进行csd的通讯，并且sim卡号不能为空
		if(dm!=null && dm.getCsdStatus()!=IMessage.COMMUNICATION_TYPE_NORMAL && 
				null!=dm.getPhoneNum() && !"".equals(dm.getPhoneNum())){
			switch(dm.getCsdStatus()){
			case IMessage.COMMUNICATION_TYPE_CSD:
				dlmsRequest.setRequestMode(REQUEST_MODE.ONLY_CSD);
				break;
			case IMessage.COMMUNICATION_TYPE_CSDorGPRS:
				dlmsRequest.setRequestMode(REQUEST_MODE.CSD_GPRS_MIXTURE);
				break;
			case IMessage.COMMUNICATION_TYPE_EXTERNAL:
				dlmsRequest.setRequestMode(REQUEST_MODE.GPRS_SERVER);
				break;
			}
		}
	}
	/**
	 * 如果长时间不在线，表示掉线了。
	 * @param context
	 * @return
	 */
	private boolean checkDropped(DlmsContext context) {
		if(context.isRelay || context.aaMechanism==CipherMechanism.BENGAL) return false;
		if(droppedTimeOut <  1000 * 60 *5) return false;
		if(System.currentTimeMillis()-context.lastRecvTime>droppedTimeOut){
			return true;
		}
		return false;
	}

	/**
	 * 读密钥版本号，以明文的形式下发。
	 * @param context
	 * @throws IOException
	 */
	private void readKeyVersion(DlmsContext context) throws IOException {
		DlmsRequest dr = new DlmsRequest();
		dr.setOperator("ReadKeyVersion");
		DlmsObisItem[] params = new DlmsObisItem[1];
		params[0] = new DlmsObisItem();
		params[0].classId = 1;
		params[0].obisString = "0.0.96.1.142.255";
		params[0].attributeId = 2; 
		dr.setParams(params);
		dr.setOpType(DLMS_OP_TYPE.OP_GET);
		byte[] obis = DlmsProtocolEncoder.convertOBIS(params[0].obisString);
		GetRequestNormal getNormal = new GetRequestNormal(1,params[0].classId,obis,params[0].attributeId);
		GetRequest getReq = new GetRequest(getNormal);
		byte[] apdu = getReq.encode();
		if(context.isRelay){
			RelayParam param = new RelayParam();
			param.setDcLogicalAddress(context.dcLogicAddr);//集中器logicalAddress
			param.setMeasurePoint(context.measurePoint); //测量点
			dr.setRelayParam(param);
			dr.setCategory(BUSINESS_CATEGORY.BIZ_RELAY);//00 01 00 02 60 01 8E FF 02 
			MessageGw gwmsg = DlmsProtocolEncoder.createRelayMsg(context, apdu , param);
			DlmsEvent evt = new DlmsEvent(dr);
			context.webReqList.add(0,evt);
			sendMessage(gwmsg, context);
		}else{
			DlmsMessage dm = new DlmsMessage();
			dm.setApdu(apdu);
			dm.setLogicalAddress(context.meterId);
			dm.setPeerAddr(context.peerAddr);
			DlmsEvent evt = new DlmsEvent(dr);
			context.webReqList.add(0,evt);
			sendMessage(dm, context);
		}
	}

	public byte[] cipher(DLMS_OP_TYPE opType,byte[] plainApdu, DlmsContext context) throws Exception{

		int cipherTag = 0;
		switch( opType ){
		case OP_ACTION:
			cipherTag = 203;   //with global ciphering, glo-action-request
			break;
		case OP_GET:
			cipherTag = 200;   //with global ciphering, glo-get-request
			break;
		case OP_SET:
			cipherTag = 201;   //with global ciphering, glo-set-request
			break;
		case OP_EVENT_NOTIFY:
			cipherTag = 202;   //with global ciphering, glo-event-notification-request
			break;
		case OP_HEXINGEXPAND:  //expand return.
			return plainApdu;
		default:
			break;
		}
		
		ASN1OctetString octs = new ASN1OctetString();
		TagAdjunct myAdjunct = TagAdjunct.contextSpecificImplicit(cipherTag);
		octs.forceEncodeTag(true);
		myAdjunct.axdrCodec(true);
		octs.setTagAdjunct(myAdjunct);
		
		ByteBuffer iv = ByteBuffer.allocate(12);
		iv.put(getSysTitle(context));
		int _frameCount = context.nextFrameCounter();
		iv.putInt( _frameCount );
		iv.flip();
		byte[] enc = cipher.encrypt(context, plainApdu, iv.array() );
		ByteBuffer encWithSH = ByteBuffer.allocate(enc.length+5);
		encWithSH.put((byte)0x30).putInt(_frameCount).put(enc);
		
		octs.setValue(encWithSH.array());
		
		return octs.encode();
	}
	
	private ByteBuffer decipher(ByteBuffer apdu, DlmsEvent evt) throws IOException{
		DlmsMessage msg = (DlmsMessage) evt.getMessage();
		DlmsContext cxt = null;
		if(null!=msg.getDcLogicAddress()){
			cxt = contextManager.getContext(msg.getDcLogicAddress());
			cxt=cxt.subContexts.get(msg.getLogicalAddress());
		}else{
			cxt= contextManager.getContext(msg.getLogicalAddress());			
		}
		ASN1OctetString octs = new ASN1OctetString();
		TagAdjunct myAdjunct = TagAdjunct.contextSpecificImplicit(0xFF & apdu.get(0));
		octs.forceEncodeTag(true);
		myAdjunct.axdrCodec(true);
		octs.setTagAdjunct(myAdjunct);
		octs.decode(DecodeStream.wrap(apdu));
		byte[] val = octs.getValue();	// SH + C + T:  means security ctr + FC + cipher text + auth tag
		if( val[0] == 0x30 ){
			byte[] iv = cxt.makeInitVector(val, 1);
			byte[] cipherText = new byte[val.length-5];
			for(int i=0; i<cipherText.length; i++ )
				cipherText[i] = val[i+5];
			byte[] plainApdu = cipher.decrypt(cxt, cipherText, iv );
			if(plainApdu != null && tracer.isEnabled()){
				tracer.trace("After Decript Msg Is:"+HexDump.toHex(plainApdu)+",MeterId:"+evt.getMessage().getLogicalAddress());
			}
			return ByteBuffer.wrap(plainApdu);
		}
		return null;
	}
	
	private void handleUpDlmsMessage(DlmsEvent evt) throws IOException{
		DlmsMessage msg = evt.upMessage();
		
		ByteBuffer apdu = msg.getApdu();
		int applicationTag = apdu.get(0) & 0xFF ;
		String logicalAddress = msg.getLogicalAddress();
		String dcLogicAddress = msg.getDcLogicAddress();
		switch( applicationTag ){
		case 0x61: //AARE
			try {
				handleAARE(apdu,evt);
			} catch (Exception e) {
				//这里偶尔会抛出异常，所以此处，try...catch一下，然后sendAARQ
				String meterId = logicalAddress;
				DlmsContext cxt = contextManager.getContext(meterId);
				if(cxt==null) cxt = new DlmsContext();
				cxt.meterId = meterId;
				cxt.peerAddr = evt.getMessage().getPeerAddr();
				contextManager.updateOrSetContext(meterId, cxt);
				if(isEsamCipher()){  //如果是硬加密，需要去读keyVersion,看是否和数据库里保存相同。
					log.error("\n meterid"+meterId+".Process AARE exception,maybe the keyVersion is uncorrect. so ,read it");
					readKeyVersion(cxt);
				}else{
					log.error("Process aare exception,sendAARQ again! meterId:"+meterId);
					sendAARQ(cxt);
				}
			}

			break;
		case 192:	//get-request
		case 193: 	//set-request
			break;
		case 194: 	//event-notification-request
		{
			if(tracer.isEnabled()){
				tracer.trace("<<upEvent:"+HexDump.hexDump(apdu)+",meterId:"+logicalAddress);
			}
			DlmsContext cxt = contextManager.getContext(logicalAddress);
			if(cxt != null){
				cxt.lastRecvTime = System.currentTimeMillis();
			}
			DlmsAlarmManager.getInstance().onEventNotificationRequest(apdu, evt, cxt);
			break;
		}
		case 195: 	//action-request
			log.error("Up-link message must not be request.");
			break;
		case 196:	//get-response
			handleGetResponse(apdu,evt);
			break;
		case 197: 	//set-response
			handleSetResponse(apdu,evt);
			break;
		case 199:	//action-response
			handleActionResponse(apdu,evt);
			break;
		case 200:	//with global ciphering, glo-get-request
		case 201:	//with global ciphering, glo-set-request
			break;
		case 202:	//with global ciphering, glo-event-notification-request
		{
			ByteBuffer plainApdu = decipher(apdu,evt);
			if( null != plainApdu ){
				DlmsContext cxt = contextManager.getContext(logicalAddress);
				DlmsAlarmManager.getInstance().onEventNotificationRequest(plainApdu, evt, cxt);
			}
			break;
		}
		case 203:{	//with global ciphering, glo-action-request
			log.error("Up-link message should not be request.");
			break;
		}
		case 204:{	//with global ciphering, glo-get-response
			ByteBuffer plainApdu = decipher(apdu,evt);
			if( null != plainApdu ){
				handleGetResponse(plainApdu,evt);
			}
			break;
		}
		case 205:{	//with global ciphering, glo-set-response
			ByteBuffer plainApdu = decipher(apdu,evt);
			if( null != plainApdu ){
				handleSetResponse(plainApdu,evt);
			}
			break;
		}
		case 207:{	//with global ciphering, glo-action-response
			ByteBuffer plainApdu = decipher(apdu,evt);
			if( null != plainApdu ){
				if(tracer.isEnabled()){
					tracer.trace("<<upMsg:"+HexDump.hexDump(plainApdu)+",meterId:"+logicalAddress);
				}
			}
				handleActionResponse(plainApdu,evt);
			break;
		}
		case 216:	//exception-response
			DlmsContext context = null;
			if(dcLogicAddress!=null){
				context=contextManager.getContext(dcLogicAddress);
			}else{
				context=contextManager.getContext(logicalAddress);
			}
			if(isEsamCipher()){
				if(context.webReqList.size()!=0){
					DlmsRequest req = ((DlmsEvent)context.webReqList.get(0)).getDlmsRequest();
					if("ReadKeyVersion".equals(req.getOperator())){
						log.error("this meter may unsupported read key version.");
						context.webReqList.remove(0);
						onRequestFailed(context);
						break;
					}
				}
				log.error("\n\tmeterId:"+context.meterId+" return frame is exception,maybe the keyVersion is uncorrect.so, read it");
				readKeyVersion(context);
				break;
			}
			log.error("return frame is exception");
			break;
		case 247: //Hexing ex transparent response
			handleHexingExResponse(apdu,evt);
			break;
		case 0xDD:  //Heart-beat, decimal value is 221
			handleHeartBeat(apdu,evt);
			break;
		default:
			log.error("other message! tag is "+applicationTag);
			break;
		}
	}

	/**
	 * 处理中继转发响应
	 * @param apdu
	 * @param evt
	 * @throws IOException 
	 */
	private void handleHexingExResponse(ByteBuffer apdu, DlmsEvent evt) throws IOException {
		
		HexingExResponse resp = new HexingExResponse();
		resp.decode(DecodeStream.wrap(apdu));
		DlmsMessage dlmsMsg = (DlmsMessage)evt.getMessage();
		String peerAddr = dlmsMsg.getPeerAddr();
		String logicalAddress = dlmsMsg.getLogicalAddress();
		String dcLogiclAddress = dlmsMsg.getDcLogicAddress();
		DlmsContext cxt = contextManager.getContext(dcLogiclAddress==null?logicalAddress:dcLogiclAddress);
		if( null == cxt ){
			log.warn("TRANSPARENT-RESPONSE not find context:peer="+peerAddr+",msg="+evt.getMessage());
			return;
		}
		if( null == resp.getDecodedObject() ){
			log.warn("TRANSPARENT-RESPONSE decodeObject is null");
			return;
		}
		setRecvTime(cxt,evt);
		DlmsProtocolDecoder.getInstance().onHexingExResponse(resp, cxt,evt);
	}

	private boolean isEsamCipher() {
		return cipher instanceof EsamCipher;
	}
	
	private void handleGetResponse(ByteBuffer apdu, DlmsEvent evt) throws IOException{
		GetResponse resp = new GetResponse();
		resp.decode(DecodeStream.wrap(apdu));
		DlmsMessage dlmsMsg = (DlmsMessage) evt.getMessage();
		String peerAddr = dlmsMsg.getPeerAddr();
		String logicalAddress = dlmsMsg.getLogicalAddress();
		String dcLogiclAddress = dlmsMsg.getDcLogicAddress();
		DlmsContext cxt = contextManager.getContext(dcLogiclAddress==null?logicalAddress:dcLogiclAddress);
		if( null == cxt ){
			log.warn("GET-RESPONSE not find context:peer="+peerAddr+",msg="+evt.getMessage());
			return;
		}
		if( null == resp.getDecodedObject() ){
			log.warn("GET-RESPONSE decodeObject is null");
			return;
		}
		DlmsProtocolDecoder.getInstance().onGetResponse(resp, cxt,evt);
	}
	
	private void setRecvTime(IMessage msg,DlmsRequest dlmsRequest,DlmsContext cxt){
		if((msg!=null && (msg.isHeartbeat()) )|| (dlmsRequest!=null && dlmsRequest.getRequestMode()!=REQUEST_MODE.ONLY_CSD)){
			//dlmsRequest==null means heart beat,csd mode don't set recvTime
			cxt.lastRecvTime = System.currentTimeMillis();
		}
	}
	private void setRecvTime(DlmsContext cxt,DlmsEvent evt) {
		if(evt!=null && cxt!=null){
			DlmsMessage msg=evt.upMessage();
			DlmsEvent lastEvent=null;
			DlmsRequest dlmsRequest= null;
			if(cxt.webReqList.size()>0){
				lastEvent=(DlmsEvent) cxt.webReqList.get(0);
				dlmsRequest = lastEvent.getDlmsRequest();
			}
			if((msg!=null && (msg.isHeartbeat()) )|| (dlmsRequest!=null && dlmsRequest.getRequestMode()!=REQUEST_MODE.ONLY_CSD)){
				//dlmsRequest==null means heart beat,csd mode don't set recvTime
				cxt.lastRecvTime = System.currentTimeMillis();
			}
		}
	}

	private void handleSetResponse(ByteBuffer apdu, DlmsEvent evt) throws IOException{
		SetResponse resp = new SetResponse();
		resp.decode(DecodeStream.wrap(apdu));
		DlmsMessage dlmsMsg = (DlmsMessage) evt.getMessage();
		String peerAddr = dlmsMsg.getPeerAddr();
		String logicalAddress = dlmsMsg.getLogicalAddress();
		String dcLogiclAddress = dlmsMsg.getDcLogicAddress();
		DlmsContext cxt = contextManager.getContext(dcLogiclAddress==null?logicalAddress:dcLogiclAddress);
		if( null == cxt ){
			log.warn("SET-RESPONSE not find context:peer="+peerAddr+",msg="+dlmsMsg);
			return;
		}
		if( null == resp.getDecodedObject() ){
			log.warn("SET-RESPONSE decodeObject is null");
			return;
		}
		DlmsProtocolDecoder.getInstance().onSetResponse(resp, cxt,evt);
	}
	
	private void handleActionResponse(ByteBuffer apdu, DlmsEvent evt) throws IOException{
		ActionResponse resp = new ActionResponse();
		resp.decode(DecodeStream.wrap(apdu));
		DlmsMessage dlmsMsg = (DlmsMessage) evt.getMessage();
		String logicalAddress = dlmsMsg.getLogicalAddress();
		String dcLogiclAddress = dlmsMsg.getDcLogicAddress();
		DlmsContext cxt = contextManager.getContext(dcLogiclAddress==null?logicalAddress:dcLogiclAddress);
		if( null == cxt ){
			log.warn("ACTION-RESPONSE not find context:meterId="+logicalAddress+",msg="+evt.getMessage());
			return;
		}
		if( null == resp.getDecodedObject() ){
			log.warn("ACTION-RESPONSE decodeObject is null");
			return;
		}
		if( cxt.aaState != AAState.AA_OK ){
			cxt.lastRecvTime = System.currentTimeMillis();
			ASN1Type selObj = resp.getDecodedObject();
			if( selObj.identifier() == 1 ){
				ActionResponseNormal repNormal = (ActionResponseNormal)selObj;
				if(repNormal.getInvokeId()!=1){
					log.warn("Recv ActionResponse,actually stoc invokeId=1,but this is not.so unhandle it. meterId:"+cxt.getMeterId()+",msg:"+evt.getMessage());
					return ;
				}
				ResponseOptionalData optData = repNormal.getRespWithOptionalData();
				ActionResult stocResult = optData.getActionResult();
				//TODO: remove debug code in the future.
				DlmsData stocData = optData.getReturnParameters().getData();
				cxt.lastAATime = System.currentTimeMillis();
				cxt.aaState = AAState.AA_OK;
				if(log.isInfoEnabled())
					log.info("StoC result:"+stocResult+",data:"+stocData+",meterId:"+cxt.meterId);
//				if(cxt.isRelay){
				sendNextMessage(cxt);
//				}else{
////					if( stocResult == ActionResult.SUCCESS ){
////						onRequestComplete(cxt);						
////					}else{
////						onRequestFailed(cxt);						
////					}
//				}
				
				return;
			}
		}
		DlmsProtocolDecoder.getInstance().onActionResponse(resp, cxt,evt);
	}
	
	private void sendStoC(DlmsContext context) throws IOException{
		//Action.request操作0-0:40.0.0.255的 method( 1 )，将服务端发送的随机数加密计算后的结果发送给服务端
		ActionRequest request = new ActionRequest();
		String obisString = "0.0.40.0.0.255";
		DlmsData param = new DlmsData();
		byte[] apdu = null;
		if( context.aaMechanism == CipherMechanism.HLS_2 ){
			//HLS使用AES-ECB对16字节随机数进行加密
			byte[] ctosParam = AESECB128.encrypt(context.authenticationValue);
			param.setOctetString(ctosParam);
			ActionRequestNormal reqNormal = new ActionRequestNormal(1,15,DlmsProtocolEncoder.convertOBIS(obisString),1,param);
			request.choose(reqNormal);
			apdu = request.encode();
		}
		else if( context.aaMechanism == CipherMechanism.HLS_GMAC ){
			ByteBuffer iv = ByteBuffer.allocate(12);
			iv.put(getSysTitle(context));
			int _frameCount = context.nextFrameCounter();
			iv.putInt( _frameCount );
			iv.flip();
			
			byte[] authTag = cipher.auth(context, context.authenticationValue, iv.array() );
			ByteBuffer fstoc = ByteBuffer.allocate(authTag.length+5);
			//Security head + frameCounter + authenticationTag
			fstoc.put((byte)0x10).putInt(_frameCount).put(authTag);
			fstoc.flip();
			param.setOctetString(fstoc.array());
			
			ActionRequestNormal reqNormal = new ActionRequestNormal(1,15,DlmsProtocolEncoder.convertOBIS(obisString),1,param);
			request.choose(reqNormal);
			byte[] plainText = request.encode();
			byte[] enc = cipher.encrypt(context, plainText, iv.array() );
			ByteBuffer actStoC = ByteBuffer.allocate(enc.length+5);
			actStoC.put((byte)0x30).putInt(_frameCount).put(enc);
			
			//203:	global ciphering, glo-action-request
			ASN1OctetString cipheredStoC = new ASN1OctetString();
			TagAdjunct myAdjunct = TagAdjunct.contextSpecificImplicit(203);
			myAdjunct.axdrCodec(true);
			cipheredStoC.forceEncodeTag(true);
			cipheredStoC.setTagAdjunct(myAdjunct);
			cipheredStoC.setValue(actStoC.array());
			apdu = cipheredStoC.encode();
		}
		else{
			throw new RuntimeException("CtoS, not supported mechanism:"+context.aaMechanism);
		}
		if( null != apdu ){
			if(context.isRelay && isRelayCiphered){
				DlmsEvent evt=(DlmsEvent) context.webReqList.get(0);
				DlmsRequest req = evt.getDlmsRequest();
				RelayParam relayParam = req.getRelayParam();
				MessageGw gwmsg = DlmsProtocolEncoder.createRelayMsg(context, apdu, relayParam);
				sendMessage(gwmsg, context);
				context.aaState = AAState.WAIT_STOC;
				return;
			}
			DlmsMessage msg = new DlmsMessage();
			msg.setApdu(apdu);
			msg.setPeerAddr(context.peerAddr);
			msg.setLogicalAddress(context.meterId);
			if( sendMessage(msg,context) ){
				context.aaState = AAState.WAIT_STOC;
			}
		}
	}
	
	private void handleAARE(ByteBuffer apdu, DlmsEvent evt) throws IOException{
		AareApdu aare = new AareApdu();
		aare.decode(DecodeStream.wrap(apdu));
		DlmsMessage message = (DlmsMessage) evt.getMessage();
		String peerAddr = message.getPeerAddr();
		DlmsContext cxt = contextManager.getContextByAddr(peerAddr);
		DlmsContext cxt0 = contextManager.getContext(message.getLogicalAddress());
		if( null == cxt && null==cxt0){
			log.warn("AARE not find context:peer="+peerAddr+",msg="+message+"meterId:"+message.getLogicalAddress());
			return;
		}
		if(cxt==null){
			cxt0.peerAddr = peerAddr;
			cxt = cxt0;
			contextManager.updateOrSetContext(message.getLogicalAddress(), cxt0);
		}
		cxt.lastRecvTime = System.currentTimeMillis();
		if( tracer.isEnabled() ){
			StringBuilder sb = new StringBuilder();
			sb.append("AARE meterid="+ cxt.meterId + ": " + aare );
			tracer.trace(sb.toString());
		}
		if(cxt.aaState==AAState.AA_OK){
			log.warn("receve AARE,but aaState is AA_OK,unhandle it. meterId:"+cxt.getMeterId());
			return ;
		}
		if(log.isInfoEnabled())
			log.info("meterID="+cxt.getMeterId()+" AARE:"+aare.toString());

		if( aare.getResultValue() == 0 ){
			if( cxt.aaMechanism == CipherMechanism.NO_SECURITY ){
				cxt.updateAARE(aare);
				cxt.aaState = AAState.AA_OK;
				//Ready to send request to DLMS device.
				sendNextMessage(cxt);
			}
			else if( cxt.aaMechanism == CipherMechanism.HLS_GMAC ){
				cxt.setAuthenticationValue(aare.getRespAuthenticationValue().getAuthValue());
				cxt.meterSysTitle = aare.getRespApTitle();
				if( null == cxt.meterSysTitle ){
					if(log.isInfoEnabled())
						log.info("meter="+cxt.getMeterId()+", mechanism="+cxt.aaMechanism+",systitle=null."+aare.toString());
					cxt.updateAARE(aare);
					cxt.aaState = AAState.AA_OK;
					return;
				}
				
				byte[] cipheredUserInfo = aare.getUserInformation();
				byte[] cInitResp = new byte[cipheredUserInfo.length-7];  //Ciphered Initiate response
				for(int i=0; i<cInitResp.length; i++)
					cInitResp[i] = cipheredUserInfo[i+7];
				byte[] pInitResp = cipher.decrypt(cxt, cInitResp, cxt.makeInitVector(cipheredUserInfo, 3) );
				aare.setDecryptedUserInfo(pInitResp);
				cxt.updateAARE(aare);
				
				sendStoC(cxt);
			}
			else if( cxt.aaMechanism == CipherMechanism.HLS_2 ){
				cxt.updateAARE(aare);
				ASN1BitString conformance = new ASN1BitString();
				conformance.setInitValue(new byte[]{ (byte)0x00,(byte)0x50,(byte)0x1F});
				cxt.setConformance(conformance);
				//Send CtoS.
				sendStoC(cxt);
			}
			else{
				throw new RuntimeException("AARE OK, not supported mechanism:"+cxt.aaMechanism);
			}
			return;
		}

		//if AARE reply failed. try another AA
		if( cxt.aaMechanism == CipherMechanism.NO_SECURITY ){
			cxt.aaMechanism = CipherMechanism.HLS_2;
			if(cxt.isRelay){//如果是集中器中继，直接从HLS_GMAC开始
				cxt.aaMechanism = CipherMechanism.HLS_GMAC;
			}
		}
		else if( cxt.aaMechanism == CipherMechanism.HLS_2 ){
			cxt.aaMechanism = CipherMechanism.HLS_GMAC;
		}
		else{
			cxt.aaMechanism = CipherMechanism.NO_SECURITY;
			log.error("Authentication May Occur an Error,Use No_Sercurity Again.");
		}
		sendAARQ(cxt);
	}
	
	private void handleHeartBeat(ByteBuffer apdu, DlmsEvent evt) throws IOException{
		//heart-beat : 1byte length.
		byte[] buf = apdu.array();
		int begin = 2, end = buf[1];
		while( buf[begin] == 0  )
			begin++;
		while( buf[end] == 0 )
			end--;
		//将所有不是阿拉伯数字的字符,设置为0
		for(int i = begin ; i < end;i++){
			if(buf[i]<48 || buf[i]>57){
				buf[i] = 48;
			}
		}
		final String meterId = new String(buf,begin,end-begin+2);
		DlmsContext cxt = contextManager.getContext(meterId);
		if(cxt!=null && cxt.isRelay){
			//如果当前表是PLC转换为GPRS,将原本的context移除
			contextManager.removeContext(meterId);
			cxt=null;
		}
		if( null == cxt){
			
			//Meter Login
			saveConnectSituation(evt, meterId);
			
			cxt = new DlmsContext();
			//TODO: load assets information from DB, including soft-encryption keys
			//TODO: Load cipher-machine information if cipher-machine applied.
			cxt.meterId = meterId;
			cxt.peerAddr = evt.getMessage().getPeerAddr();
			initKey(meterId, cxt);
			cxt.lastRecvTime = System.currentTimeMillis();;
			//对于特定规约，不发送AARQ
			if(checkIsAuth(cxt)){
				withOutAuth(meterId, cxt);
			}else{
				contextManager.updateOrSetContext(meterId, cxt);
				ExecutorService es = Executors.newCachedThreadPool();
				es.execute(new Runnable() {
					@Override
					public void run() {
						DlmsContext c = contextManager.getContext(meterId);
						try {
							Thread.sleep(500);
							sendAARQ(c);
						} catch (IOException e) {
							e.printStackTrace();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				});
				es.shutdown();
			}
		}
		else{
			DlmsContext cxt0 = contextManager.getContextByAddr(evt.getMessage().getPeerAddr());
			if( null == cxt0 ){
				//Meter login again
				
				saveConnectSituation(evt, meterId);
				
				contextManager.removeContext(meterId);
				cxt.meterId = meterId;
				cxt.linkMode = IMessage.COMMUNICATION_TYPE_NORMAL;
				cxt.peerAddr = evt.getMessage().getPeerAddr();
				initKey(meterId, cxt);
				if(checkIsAuth(cxt)){
					withOutAuth(meterId, cxt);
				}else{
					contextManager.updateOrSetContext(meterId, cxt);
					ExecutorService es = Executors.newCachedThreadPool();
					es.execute(new Runnable() {
						
						@Override
						public void run() {
							DlmsContext c = contextManager.getContext(meterId);
							try {
								Thread.sleep(500);
								sendAARQ(c);
							} catch (IOException e) {
								e.printStackTrace();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					});
					es.shutdown();
					if(log.isInfoEnabled())
						log.info("recv heart beat,maybe login agian,send AARQ again! meterId:"+meterId);
				}
				cxt.lastRecvTime = System.currentTimeMillis();
				return;
			}
			cxt.linkMode = IMessage.COMMUNICATION_TYPE_NORMAL;
			cxt.lastRecvTime = System.currentTimeMillis();
			if( cxt != cxt0 ){
				contextManager.removeContext(cxt.meterId);
				contextManager.removeContext(cxt0.meterId);
				log.error("meterId="+meterId+", has two context. cxt.meterId="+cxt.meterId+",cxt0.meterId="+cxt0.meterId+",peerAddr:"+evt.getMessage().getPeerAddr());
			}
			if( cxt.aaState == AAState.WAIT_AARE ){
				sendAARQ(cxt);
				return;
			}else if( cxt.aaState == AAState.WAIT_STOC ){
				if(log.isInfoEnabled()){
					log.info("handle heart beat ,but current aaState:"+AAState.WAIT_STOC+",send StoC again.meterId:"+cxt.meterId);
				}
				this.sendStoC(cxt);
				return;
			}else if(cxt.aaState!=AAState.AA_OK){
				sendAARQ(cxt);
			}
			if( checkContextTimeout(cxt)){
				sendAARQ(cxt);
				return;
			}
		}
	}
	/**
	 * 保存连接状况
	 * @param evt
	 * @param meterId
	 */
	private void saveConnectSituation(DlmsEvent evt, String meterId) {
		RtuConnectItem rci = new RtuConnectItem();
		rci.setLogicAddress(meterId);
		rci.setPeerAddress(evt.getMessage().getPeerAddr());
		rci.setStatus(0);
		asycService.addToDao(rci, 6003);
	}

	/**
	 * 不通过认证，直接将aaState设置为AA_OK
	 * @param meterId
	 * @param cxt
	 */
	private void withOutAuth(String meterId, DlmsContext cxt) {
		if(log.isInfoEnabled())
			log.info("For "+cxt.subProtocol+" Subprotocol,with out auth,meterId="+cxt.meterId);
		cxt.setMaxPduSize(500);
		ASN1BitString conformance = new ASN1BitString();
		conformance.setInitValue(new byte[]{ (byte)0x00,(byte)0x50,(byte)0x1F});
		cxt.setConformance(conformance);
		cxt.aaMechanism = CipherMechanism.HLS_2;
		cxt.aaState = AAState.AA_OK;
		contextManager.updateOrSetContext(meterId, cxt);
	}

	/**
	 * 检查是否需要认证
	 * 对于某些不加密的老表，返回回来的认证帧 无法解析。
	 * @param cxt
	 * @return
	 */
	private boolean checkIsAuth(DlmsContext cxt) {
		boolean withOutAuth = false;
		if(cxt.subProtocol!=null && withOutAuthSubs!=null){
			for(String sub : withOutAuthSubs){
				if(sub.equals(cxt.subProtocol)){
					withOutAuth = true;
					break;
				}
			}
		}
		return withOutAuth;
	}
	public final void setSessionTimeout(long sessionTimeout) {
		if(sessionTimeout<=0){
			isCheckSession = false;
		}else{
			isCheckSession = true;
		}
		this.sessionTimeout = sessionTimeout*1000*60;
	}

	public MasterDbService getMasterDbService() {
		return masterDbService;
	}

	public void setMasterDbService(MasterDbService masterDbService) {
		this.masterDbService = masterDbService;
	}

	public final void setMaxResend(int maxResend) {
		this.maxResend = maxResend;
	}

	public final void setResendInterval(int resendInterval) {
		if( resendInterval < 500 && resendInterval > 0 )
			resendInterval *= 1000;
		this.resendInterval = resendInterval;
	}

	public void setDroppedTimeOut(int droppedTimeOut) {
		this.droppedTimeOut = droppedTimeOut*1000*60;
	}
	
	public final void setRelayResendInterval(int relayResendInterval) {
		if(relayResendInterval<500 && relayResendInterval>0)
			relayResendInterval*=1000;
		this.relayResendInterval = relayResendInterval;
	}

	public final void setCipher(IDlmsCipher cipher) {
		this.cipher = cipher;
	}

	public final boolean isRelayCiphered() {
		return isRelayCiphered;
	}

	public final void setRelayCiphered(boolean isRelayCiphered) {
		this.isRelayCiphered = isRelayCiphered;
	}
	/**
	 * 孟加拉密钥更新
	 * @param client
	 * @param req
	 * @param cxt
	 */
	private void begalKeyChange(IChannel client, DlmsRequest req,
			DlmsContext cxt) {
		BengalMessage bmsg = new BengalMessage();
		bmsg.setLogicalAddress(cxt.meterId);
		bmsg.setPeerAddr(cxt.peerAddr);
		ByteBuffer iv = ByteBuffer.allocate(12);
		iv.put(getSysTitle(cxt));
		int _frameCount = cxt.nextFrameCounter();
		iv.putInt( _frameCount );
		iv.flip();
		bmsg.setRandom( HexDump.toHex(iv.array()));
		bmsg.setSeq(_frameCount);
		bmsg.setFuncCode(8);  //Key Change function code.
		byte[] cipheredKey = req.getKeyParam().getCipheredEncKey();
		byte[] plainKey = req.getKeyParam().getNewEncKey();
		byte[] oldEncKey = cxt.encryptKey;
		cxt.encryptKey = req.getKeyParam().getRootKey();
		try {
			//signature
			byte[] sign = cipher.auth(cxt, plainKey, null);
			if( null == cipheredKey ){
				cipheredKey = cipher.encrypt(cxt, req.getKeyParam().getNewEncKey(), iv.array());
			}
			bmsg.setSignatureData(HexDump.toHex(sign));
			bmsg.setData(HexDump.toHex(cipheredKey));
		} catch (IOException e) {
			cxt.encryptKey = oldEncKey;
			log.error(e.getLocalizedMessage(), e);
			onChangeKeyFailed(req,client);
			return;
		}
		cxt.encryptKey = oldEncKey;
		
		cxt.keyChangeRequest = req;
		req.setOperator("KeyChangeRequest");
		
		MessageGate gate = new MessageGate();
		gate.setDownInnerMessage(bmsg);
		if( ! this.messageQueue.sendMessage(gate) ){
			onChangeKeyFailed(req,client);
		}
	}
	/**
	 *  Byte1	MC	0x48
		Byte2	MC	0x58
		Byte3	MC	0x45
		Byte4	T1	0x01—单相表
					0x03—三相表
					0x10—公共客户端
					0x11—管理客户端
		Byte5	T2	Byte5的高半字节bit7-bit4
					Bit7—保留
					Bit6—是否有管理其他能量表如气表、水表
					Bit5—是否有负荷管理
					Bit4—是否带继电器
					客户端此半字节固定为0
		Byte6	SN
		Byte7	SN
		Byte8	SN
		SN	表的序列号的16进制字节，当表序列转换为16进制超过3个半字节时，截掉高字节。
		如表的序列号为12345678则此处表达为0 BC 61 4E。客户端的SN字节固定为0
	 * @return
	 */
	public byte[] getMeterSystitle(DlmsContext cxt,DlmsMeterRtu meterRtu){
		ByteBuffer sysBuffer = ByteBuffer.allocate(8);
		sysBuffer.put((byte) 0x48);
		sysBuffer.put((byte) 0x58);
		sysBuffer.put((byte) 0x45);
		int meterType = Integer.parseInt(meterRtu.getWiringMode());
		if(meterType ==1){
			cxt.meterType = meterType;
		}else if(meterType==3 || meterType ==4){
			cxt.meterType = 3;
		}else{
			cxt.meterType = 0;
			log.warn("MeterType is "+0+",Check Out Db File. LogicAddress:"+meterRtu.getLogicAddress());
		}
		sysBuffer.put((byte) cxt.meterType); //接线方式，01单相，03三相
		//固定为3
		String s_hexRtu="3"+Integer.toHexString(Integer.parseInt(cxt.getMeterId()));
		sysBuffer.put(HexDump.toArray(s_hexRtu));
		sysBuffer.flip();
		return sysBuffer.array();
	}
	/**
	 * Esam 密钥变更
	 * @param req
	 * @param cxt
	 * @throws IOException
	 */
	private void esamGcmKeyChange(DlmsRequest req, DlmsContext cxt)
			throws IOException {
		byte[] keys = EsamCipher.getInstance().createGcmNewKey(cxt, new byte[0], new byte[0]);
		int offset = 1;
		byte[] key1 = null;
		DlmsData octKey = null, emKeyId = null;
		
		emKeyId = new DlmsData();
		emKeyId.setEnum(0);
		key1 = new byte[32];
		for(int i=0; i<32; i++)
			key1[i] = keys[i+offset];
		offset += 32;
		octKey = new DlmsData();
		octKey.setOctetString(key1);
		DlmsData struct1 = new DlmsData();
		struct1.setStructure(new ASN1SequenceOf(new ASN1Type[]{emKeyId,octKey}));

		emKeyId = new DlmsData();
		emKeyId.setEnum(1);
		key1 = new byte[32];
		for(int i=0; i<32; i++)
			key1[i] = keys[i+offset];
		offset += 32;
		octKey = new DlmsData();
		octKey.setOctetString(key1);
		DlmsData struct2 = new DlmsData();
		struct2.setStructure(new ASN1SequenceOf(new ASN1Type[]{emKeyId,octKey}));
		
		emKeyId = new DlmsData();
		emKeyId.setEnum(2);
		key1 = new byte[32];
		for(int i=0; i<32; i++)
			key1[i] = keys[i+offset];
		offset += 32;
		octKey = new DlmsData();
		octKey.setOctetString(key1);
		DlmsData struct3 = new DlmsData();
		struct3.setStructure(new ASN1SequenceOf(new ASN1Type[]{emKeyId,octKey}));
		
		DlmsData kcps = new DlmsData();
		kcps.setArray(new DlmsData[]{struct1,struct2,struct3});
		
		DlmsObisItem keyChangeParam = new DlmsObisItem();
		keyChangeParam.classId = 64;
		keyChangeParam.obisString = "0.0.43.0.0.255";
		keyChangeParam.attributeId = 2;
		keyChangeParam.data = kcps;
		req.setParams(new DlmsObisItem[]{keyChangeParam});
		req.setOpType(DLMS_OP_TYPE.OP_ACTION);
	}

	public final void setAsycService(AsyncService asycService) {
		this.asycService = asycService;
	}

	public final void setGwRelayNeedAuth(String isGwRelayNeedAuth) {
		this.isGwRelayNeedAuth=Boolean.parseBoolean(isGwRelayNeedAuth);
	}

}
