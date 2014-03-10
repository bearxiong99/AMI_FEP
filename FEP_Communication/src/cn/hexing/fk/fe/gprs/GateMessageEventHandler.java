/**
 * ����������ͨ��ǰ�û�֮�䱨���շ��¼�����
 * ���б��Ľ������ȼ����У��Ա�ҵ����������
 * ע�⴦������ͳ�ơ������ȡ�
 */
package cn.hexing.fk.fe.gprs;

import org.apache.log4j.Logger;

import cn.hexing.db.batch.AsyncService;
import cn.hexing.db.bizprocess.MasterDbService;
import cn.hexing.fk.FasSystem;
import cn.hexing.fk.clientmod.ClientModule;
import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.common.spi.IEventHandler;
import cn.hexing.fk.fe.cluster.BatchSynchronizer;
import cn.hexing.fk.fe.cluster.RealtimeSynchronizer;
import cn.hexing.fk.fe.cluster.RtuState;
import cn.hexing.fk.fe.cluster.RtuWorkStateItem;
import cn.hexing.fk.fe.cluster.WorkState;
import cn.hexing.fk.fe.msgqueue.FEMessageQueue;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.MessageConst;
import cn.hexing.fk.message.MessageType;
import cn.hexing.fk.message.bengal.BengalMessage;
import cn.hexing.fk.message.gate.GateHead;
import cn.hexing.fk.message.gate.MessageGate;
import cn.hexing.fk.message.gw.MessageGw;
import cn.hexing.fk.message.zj.MessageZj;
import cn.hexing.fk.model.RtuConnectItem;
import cn.hexing.fk.sockserver.event.ReceiveMessageEvent;
import cn.hexing.fk.sockserver.event.SendMessageEvent;
import cn.hexing.fk.tracelog.TraceLog;
import cn.hexing.ws.logic.WsGlobalMap;

import com.hx.ansi.message.AnsiMessage;
import com.hx.dlms.message.DlmsMessage;
/**
 *
 */
public class GateMessageEventHandler implements IEventHandler {
	private static final Logger log = Logger.getLogger(GateMessageEventHandler.class);
	private static final TraceLog tracer = TraceLog.getTracer(GateMessageEventHandler.class);
//	private static HeartBeatMessage bate = new HeartBeatMessage();
	private FEMessageQueue msgQueue;	//spring ����ʵ�֡�
	private AsyncService asyncDbService;		//������������ԭʼ����
//	private MasterDbService masterDbService;  //spring ����ʵ�֡�
	private ClientModule intfClient;
	
//	private HeartBeatMessage heartBeat;//spring ����ʵ�֣���������
	
	public void handleEvent(IEvent event) {
		if( event.getType().equals(EventType.MSG_RECV) ){
			onRecvMessage( (ReceiveMessageEvent)event);
		}
			
		else if( event.getType().equals(EventType.MSG_SENT) )
			onSendMessage( (SendMessageEvent)event );
	}
	/**
	 * �յ�GPRS���ص����б��ġ�
	 * @param e
	 */
	private void onRecvMessage(ReceiveMessageEvent e){
		IMessage msg = e.getMessage();
		if( msg.getMessageType() == MessageType.MSG_GATE ){
			MessageGate mgate = (MessageGate)msg;
			//����֧�ֿͻ��������Ĺ��ܡ���������������client���ͱ��ġ�HREQ���������������á�
			if( mgate.getHead().getCommand() == MessageGate.CMD_GATE_HREPLY ){
				//�ͻ�������ı���������Ӧ��
//				log.info(mgate);
				return;		//�����������
			}
			else if(mgate.getHead().getCommand() == MessageGate.CMD_GATE_REPLY || mgate.getHead().getCommand() == MessageGate.CMD_WRAP){
				IMessage imsg = mgate.getInnerMessage();
				
//				RtuInfoServer.getInstance().setCurrentMsg(MessageConst.DIR_UP, imsg);
				
				if(log.isDebugEnabled())
					log.debug("receive msg from gate:"+imsg);
				if( null != imsg ){
					if( imsg.getMessageType() == MessageType.MSG_ZJ ){
						MessageZj zjmsg = (MessageZj)imsg;
						_handleZjMessage(zjmsg,e);
					}
					else if( imsg.getMessageType() == MessageType.MSG_GW_10 ){
						MessageGw gwmsg = (MessageGw)imsg;
						_handleGwMessage(gwmsg,e);
					}else if( imsg.getMessageType() == MessageType.MSG_DLMS ){					
						DlmsMessage dlmsmsg = (DlmsMessage)imsg;
						try {
							_handleDlmsMessage(dlmsmsg,e);
						} catch (Exception e1) {
							log.error("_handleDlmsMessage error!! handleDlmsMessage again.",e1);
							_handleDlmsMessage(dlmsmsg, e);
						}
					}else if( imsg.getMessageType() == MessageType.MSG_ANSI){
						AnsiMessage amsg=(AnsiMessage)imsg;
						_handleAnsiMessage(amsg,e);
					}
					
					else if( imsg.getMessageType() == MessageType.MSG_BENGAL ){
						BengalMessage bmsg = (BengalMessage)imsg;
						bmsg.setTxfs( mgate.getTxfs());
						
						_handleBengalMessage(bmsg,e);
					}
				}
			}
			else if(mgate.getHead().getCommand() == MessageGate.CMD_GATE_SENDFAIL ){
				//GPRS��������ʧ�ܣ���Ҫ������ͨ������ͨ�����е��նˡ�
				IMessage imsg = mgate.getInnerMessage();
				if( imsg.getMessageType() == MessageType.MSG_ZJ ){
					MessageZj zjmsg = (MessageZj)imsg;
					//�粻�߶���:�����Զ��屨��
					if( zjmsg.head.c_func == MessageConst.ZJ_FUNC_USER_DEFINE 
							|| zjmsg.head.c_func == MessageConst.ZJ_FUNC_HEART )
						return;

					//GPRS����ʧ�ܣ���Ҫת���š��ƶ��������в���.
					if( null != zjmsg && log.isDebugEnabled() )
						log.debug("��������ʧ�ܱ���,ת����ͨ��:"+zjmsg);
					msgQueue.sendMessageByUms(zjmsg);
					return;
				}
				else if( imsg.getMessageType() == MessageType.MSG_GW_10 ){
					MessageGw gwmsg = (MessageGw)imsg;
					if( gwmsg.afn() == MessageConst.GW_FUNC_CONTROL ||
							gwmsg.afn() == MessageConst.GW_FUNC_GET_DATA1 ||
							gwmsg.afn() == MessageConst.GW_FUNC_GET_DATA2 ||
							gwmsg.afn() == MessageConst.GW_FUNC_GET_DATA3 ||
							gwmsg.afn() == MessageConst.GW_FUNC_GET_TASK ||
							gwmsg.afn() == MessageConst.GW_FUNC_GETPARAM ||
							gwmsg.afn() == MessageConst.GW_FUNC_RELAY_READ ||
							gwmsg.afn() == MessageConst.GW_FUNC_RESET ||
							gwmsg.afn() == MessageConst.GW_FUNC_SETPARAM ||
							gwmsg.afn() == MessageConst.GW_FUNC_REQ_CASCADE_UP ||
							gwmsg.afn() == MessageConst.GW_FUNC_REQ_RTU_CFG ){
						//GPRS����ʧ�ܣ���Ҫת���š��ƶ��������в���.
						msgQueue.sendMessageByUms(gwmsg);
						return;
					}
				}
			}
			else if( mgate.getHead().getCommand() == MessageGate.REP_MONITOR_RELAY_PROFILE ){
				String gateProfile = new String(mgate.getData().array());
				FasSystem.getFasSystem().addGprsGateProfile(e.getClient().getPeerAddr(), gateProfile);
				return;
			}
			else if( mgate.getHead().getCommand() == MessageGate.CMD_RTU_CLOSE ){
				String logicalAddress = mgate.getHead().getAttributeAsString(GateHead.ATT_LOGICALADDR);
				log.warn("RTU_CLOSED: "+logicalAddress );
				WorkState workState = BatchSynchronizer.getInstance().getWorkState(logicalAddress);
				if(workState!=null)  workState.setConnect(false);					
				RtuConnectItem rci = new RtuConnectItem();
				rci.setLogicAddress(logicalAddress);
				rci.setPeerAddress(mgate.getHead().getAttributeAsString(GateHead.ATT_SRCADDR));
				rci.setStatus(1);
				asyncDbService.addToDao(rci, 6003);
			}
			else {
				//������������
				log.error("other command!");//log.error("�����������");
			}
		}
		else if( msg.getMessageType() == MessageType.MSG_ZJ ){
			_handleZjMessage((MessageZj)msg,e);
		}
	}
	
	private void _handleAnsiMessage(AnsiMessage amsg, ReceiveMessageEvent event) {

		if( log.isDebugEnabled() )
			log.debug("�������б���:"+amsg);
		/** ͨ��ǰ�û��յ��������б��ģ���Ҫ���⴦��
		 *  ��1���������Ĵ�MySQL��
		 *  ��2���ն˹���������
		 *  ��3�������ն�����������IP��port������վ���ò�һ�´���
		 */
		//1. ���ն�ͨ�Ų�������������ҵ��ն˲�������
		RtuState  state = RealtimeSynchronizer.getInstance().getRtuState(amsg.getLogicalAddress());
		boolean updateState = false;
		if( null == state ){
			state = RealtimeSynchronizer.getInstance().loadFromDb(amsg.getLogicalAddress());
			updateState = true;
		}
		
		//2 & 3. ���¹��� ������ͳ�ơ������Զ��ظ������Գ���*2,��¼�ظ�
		int flow = amsg.length();
		
		RtuWorkStateItem rws = new RtuWorkStateItem();
		rws.setLastCommunicationIp(amsg.getPeerAddr());
		rws.setIoTime(System.currentTimeMillis());
		rws.setRtua(state.getRtua());
		rws.setLen(flow);
		
		if(amsg.isHeartbeat()){
			rws.setFunc(RtuWorkStateItem.FUNC_DLMS_HEART);
//		}else if(amsg.isEventNeedReply()){
//			rws.setFunc(RtuWorkStateItem.FUNC_DLMS_NEED_CFM);
		}else{
			rws.setFunc(RtuWorkStateItem.FUNC_REPLY);
		}
		BatchSynchronizer.getInstance().addWorkState(rws);
		
		//4. �ն˹������� ��ϵ����
		try{
			String gateAddr = event.getClient().getPeerAddr();	//���ص�ǰ�û��ӿڵ�ַ��
			if( ! gateAddr.equals(state.getActiveGprs()) ){
				state.setActiveGprs(gateAddr);
				updateState = true;
			}
			if( updateState )
				RealtimeSynchronizer.getInstance().setRtuState(state);
		}catch(Exception err){
			log.error("update activeGprs exp:"+err.getLocalizedMessage(),err);
		}
		RealtimeSynchronizer.getInstance().saveCommState(state);
		//5.2 ԭʼ���ı���,���������
		if(isSaveHeartbeat(amsg.getLogicalAddress()) || !amsg.isHeartbeat()){
			//��������������߲����������ű���
			asyncDbService.log2Db(amsg);
		}
		
		
		msgQueue.offer(amsg);
//		//6 �����ݲ�ѯ���ն˵�ǰ��Чbp��ַ
//		IServerSideChannel bpChannel=Msg2BpByMod.getInstance().getIdleChannel();
//		if ( null == bpChannel )
//			bpChannel = Msg2BpByMod.getInstance().getBpChannel(0);
//		if (bpChannel!=null){
//			MessageGate gateMsg = new MessageGate();
//			gateMsg.setUpInnerMessage(dlmsmsg);
//			boolean success = bpChannel.send(gateMsg);
//			if( !success ){
//				log.warn("send DLMS message to BP failed. meterId="+dlmsmsg.getLogicalAddress());
//			}else{
//				if(log.isDebugEnabled())
//					log.debug("send to bp success."+"\n\t recvFrom:"+dlmsmsg.getPeerAddr()+"\t logicAddress:"+dlmsmsg.getLogicalAddress()+"\n\t msg:"+dlmsmsg);
//			}
//		}
//		else
//			log.warn("can not find  bp client of addr="+dlmsmsg.getLogicalAddress() );
	
		
		
		
		
		
	}
	private void _handleBengalMessage(BengalMessage bmsg, ReceiveMessageEvent event){
		if( log.isDebugEnabled() )
			log.debug("�������б���:"+bmsg);
		/** ͨ��ǰ�û��յ��������б��ģ���Ҫ���⴦��
		 *  ��1���������Ĵ�MySQL��
		 *  ��2���ն˹���������
		 *  ��3�������ն�����������IP��port������վ���ò�һ�´���
		 */
		//1. ���ն�ͨ�Ų�������������ҵ��ն˲�������
		RtuState  state = RealtimeSynchronizer.getInstance().getRtuState(bmsg.getLogicalAddress());
		boolean updateState = false;
		if( null == state ){
			state = RealtimeSynchronizer.getInstance().loadFromDb(bmsg.getLogicalAddress());
			updateState = true;
		}

		//2 & 3. ���¹��� ������ͳ�ơ������Զ��ظ������Գ���*2,��¼�ظ�
		int flow = bmsg.length();
		
		RtuWorkStateItem rws = new RtuWorkStateItem();
		rws.setIoTime(System.currentTimeMillis());
		rws.setRtua(state.getRtua());
		rws.setLen(flow);
		if(bmsg.getFuncCode()==1){
			rws.setFunc(RtuWorkStateItem.FUNC_HEART);
		}
		else if( bmsg.isTask() ){
			rws.setFunc(RtuWorkStateItem.FUNC_TASK);
		}
		else{
			rws.setFunc(RtuWorkStateItem.FUNC_REPLY);
		}
		BatchSynchronizer.getInstance().addWorkState(rws);
		
		//4. �ն˹������� ��ϵ����
		try{
			String gateAddr = event.getClient().getPeerAddr();	//���ص�ǰ�û��ӿڵ�ַ��
			if( ! gateAddr.equals(state.getActiveGprs()) ){
				state.setActiveGprs(gateAddr);
				updateState = true;
			}
			if( updateState )
				RealtimeSynchronizer.getInstance().setRtuState(state);
		}catch(Exception err){
			log.error("update activeGprs exp:"+err.getLocalizedMessage(),err);
		}

		RealtimeSynchronizer.getInstance().saveCommState(state);
		//5.2 ԭʼ���ı���,���������
		if(isSaveHeartbeat(bmsg.getLogicalAddress()) || !bmsg.isHeartbeat()){
			//��������������߲����������ű���
			asyncDbService.log2Db(bmsg);
		}
		
		msgQueue.offer(bmsg);
//		//6 �����ݲ�ѯ���ն˵�ǰ��Чbp��ַ
//			IServerSideChannel bpChannel=Msg2BpByMod.getInstance().getIdleChannel();
//			if ( null == bpChannel )
//				bpChannel = Msg2BpByMod.getInstance().getBpChannel(0);
//			if (bpChannel!=null){
//				MessageGate gateMsg = new MessageGate();
//				gateMsg.setUpInnerMessage(bmsg);
//				boolean success = bpChannel.send(gateMsg);
//				if( !success ){
//					log.warn("send DLMS message to BP failed. meterId="+bmsg.getLogicalAddress());
//				}
//			}
//			else
//				log.warn("can not find  bp client of addr="+bmsg.getLogicalAddress() );
	}
	
	private void _handleZjMessage(MessageZj zjmsg,ReceiveMessageEvent event){
		if( log.isDebugEnabled() )
			log.debug("�������б���:"+zjmsg);
		/** ͨ��ǰ�û��յ��������б��ģ���Ҫ���⴦��
		 *  ��1���������Ĵ�MySQL��
		 *  ��2���ն˹���������
		 *  ��3�������ն�����������IP��port������վ���ò�һ�´���
		 */
		//1. ���ն�ͨ�Ų�������������ҵ��ն˲�������
		RtuState  state = RealtimeSynchronizer.getInstance().getRtuState(zjmsg.getLogicalAddress());
		boolean updateState = false;
		if( null == state ){
			state = RealtimeSynchronizer.getInstance().loadFromDb(zjmsg.getLogicalAddress());
			updateState = true;
		}

		//2 & 3. ���¹��� ������ͳ�ơ������Զ��ظ������Գ���*2,��¼�ظ�
		int flow = zjmsg.length();
		
		RtuWorkStateItem rws = new RtuWorkStateItem();
		rws.setIoTime(System.currentTimeMillis());
		rws.setRtua(state.getRtua());
		rws.setLen(flow);
		if( zjmsg.head.c_func == MessageConst.ZJ_FUNC_HEART ||
				zjmsg.head.c_func == MessageConst.ZJ_FUNC_LOGOUT ){
			rws.setFunc(RtuWorkStateItem.FUNC_HEART);
		}
		else if( zjmsg.head.c_func == MessageConst.ZJ_FUNC_LOGIN ){
			rws.setFunc(RtuWorkStateItem.FUNC_LOGIN);
		}
		else if( zjmsg.head.c_func == MessageConst.ZJ_FUNC_READ_TASK ){
			rws.setFunc(RtuWorkStateItem.FUNC_TASK);
		}
		else{
			if( zjmsg.head.c_func == MessageConst.ZJ_FUNC_EXP_ALARM )
				rws.setFunc(RtuWorkStateItem.FUNC_ALARM);
			else
				rws.setFunc(RtuWorkStateItem.FUNC_REPLY);
		}
		BatchSynchronizer.getInstance().addWorkState(rws);
		
		//4. �ն˹������� ��ϵ����
		try{
			String gateAddr = event.getClient().getPeerAddr();	//���ص�ǰ�û��ӿڵ�ַ��
			if( ! gateAddr.equals(state.getActiveGprs()) ){
				state.setActiveGprs(gateAddr);
				updateState = true;
			}
			if( updateState )
				RealtimeSynchronizer.getInstance().setRtuState(state);
		}catch(Exception err){
			log.error("update activeGprs exp:"+err.getLocalizedMessage(),err);
		}

		
		/*//5.1 �������ı���
		if( zjmsg.head.c_func == MessageConst.ZJ_FUNC_HEART ){
			//������������ҵ���¼�
			if(WsGlobalMap.getInstance().isSaveHeartbeat2Db(zjmsg.getRtua()))
				asyncDbService.log2Db(zjmsg);
			return;
		}*/
		//5.2 ԭʼ���ı���,���������
		RealtimeSynchronizer.getInstance().saveCommState(state);

		if(zjmsg.isHeartbeat() && !zjmsg.isLogin()){
			//���������֡����Ҫ��������
			if(isSaveHeartbeat(zjmsg.getLogicalAddress())){
				asyncDbService.log2Db(zjmsg);
			}
		}else{
			asyncDbService.log2Db(zjmsg);			
		}
		
		//6. �����Զ��屨�ģ���Ҫֱ�ӷ��͸����ҡ����ܰ���Ŀǰ������ȡ���ģʽ��
		if( zjmsg.head.c_func == MessageConst.ZJ_FUNC_USER_DEFINE ){
			MessageGate gatemsg = new MessageGate();
			gatemsg.setUpInnerMessage(zjmsg);
			intfClient.sendMessage(gatemsg);
		}
		else
			msgQueue.offer(zjmsg);
	}
	private void _handleDlmsMessage(DlmsMessage dlmsmsg,ReceiveMessageEvent event){
		if( log.isDebugEnabled() )
			log.debug("Gate Up Message:"+dlmsmsg);
		/** ͨ��ǰ�û��յ��������б��ģ���Ҫ���⴦��
		 *  ��1���������Ĵ�MySQL��
		 *  ��2���ն˹���������
		 *  ��3�������ն�����������IP��port������վ���ò�һ�´���
		 */
		//1. ���ն�ͨ�Ų�������������ҵ��ն˲�������
		RtuState  state = RealtimeSynchronizer.getInstance().getRtuState(dlmsmsg.getLogicalAddress());
		boolean updateState = false;
		if( null == state ){
			state = RealtimeSynchronizer.getInstance().loadFromDb(dlmsmsg.getLogicalAddress());
			updateState = true;
		}
		
		//2 & 3. ���¹��� ������ͳ�ơ������Զ��ظ������Գ���*2,��¼�ظ�
		int flow = dlmsmsg.length();
		
		RtuWorkStateItem rws = new RtuWorkStateItem();
		rws.setLastCommunicationIp(dlmsmsg.getPeerAddr());
		rws.setIoTime(System.currentTimeMillis());
		rws.setRtua(state.getRtua());
		rws.setLen(flow);
		
		if(dlmsmsg.isHeartbeat()){
			rws.setFunc(RtuWorkStateItem.FUNC_DLMS_HEART);
		}else if(dlmsmsg.isAA()){
			rws.setFunc(RtuWorkStateItem.FUNC_DLMS_AA);
		}else if(dlmsmsg.isEventNeedReply()){
			rws.setFunc(RtuWorkStateItem.FUNC_DLMS_NEED_CFM);
		}else{
			rws.setFunc(RtuWorkStateItem.FUNC_REPLY);
		}
		BatchSynchronizer.getInstance().addWorkState(rws);
		
		//4. �ն˹������� ��ϵ����
		try{
			String gateAddr = event.getClient().getPeerAddr();	//���ص�ǰ�û��ӿڵ�ַ��
			if( ! gateAddr.equals(state.getActiveGprs()) ){
				state.setActiveGprs(gateAddr);
				updateState = true;
			}
			if( updateState )
				RealtimeSynchronizer.getInstance().setRtuState(state);
		}catch(Exception err){
			log.error("update activeGprs exp:"+err.getLocalizedMessage(),err);
		}
		
		RealtimeSynchronizer.getInstance().saveCommState(state);
		
		//5.2 ԭʼ���ı���,���������
		if(isSaveHeartbeat(dlmsmsg.getLogicalAddress()) || !dlmsmsg.isHeartbeat()){
			//��������������߲����������ű���
			asyncDbService.log2Db(dlmsmsg);
		}
		
		
		msgQueue.offer(dlmsmsg);
//		//6 �����ݲ�ѯ���ն˵�ǰ��Чbp��ַ
//		IServerSideChannel bpChannel=Msg2BpByMod.getInstance().getIdleChannel();
//		if ( null == bpChannel )
//			bpChannel = Msg2BpByMod.getInstance().getBpChannel(0);
//		if (bpChannel!=null){
//			MessageGate gateMsg = new MessageGate();
//			gateMsg.setUpInnerMessage(dlmsmsg);
//			boolean success = bpChannel.send(gateMsg);
//			if( !success ){
//				log.warn("send DLMS message to BP failed. meterId="+dlmsmsg.getLogicalAddress());
//			}else{
//				if(log.isDebugEnabled())
//					log.debug("send to bp success."+"\n\t recvFrom:"+dlmsmsg.getPeerAddr()+"\t logicAddress:"+dlmsmsg.getLogicalAddress()+"\n\t msg:"+dlmsmsg);
//			}
//		}
//		else
//			log.warn("can not find  bp client of addr="+dlmsmsg.getLogicalAddress() );
	}	
	private void _handleGwMessage(MessageGw gwmsg,ReceiveMessageEvent event){
		if( log.isDebugEnabled() )
			log.debug("�������б���:"+gwmsg);
		/** ͨ��ǰ�û��յ��������б��ģ���Ҫ���⴦��
		 *  ��1���������Ĵ�MySQL��
		 *  ��2���ն˹���������
		 *  ��3�������ն�����������IP��port������վ���ò�һ�´���
		 */
		//1. ���ն˶���������ҵ��ն˶���
		//1. ���ն�ͨ�Ų�������������ҵ��ն˲�������
		RtuState  state = RealtimeSynchronizer.getInstance().getRtuState(gwmsg.getLogicalAddress());
		boolean updateState = false;
		if( null == state ){
			state = RealtimeSynchronizer.getInstance().loadFromDb(gwmsg.getLogicalAddress());
			updateState = true;
		}
			
		//2 & 3. ���¹��� ������ͳ�ơ������Զ��ظ������Գ���*2,��¼�ظ�
		int flow = gwmsg.length();
		if(gwmsg.isNeedConfirm()){
			flow+=(20+WorkState.tcpIpLen);//������Ҫȷ�ϵ�֡��Ҫ��ȷ��֡�ĳ��ȼ���
		}
		RtuWorkStateItem rws = new RtuWorkStateItem();
		rws.setIoTime(System.currentTimeMillis());
		rws.setRtua(state.getRtua());
		rws.setLen(flow);
		
		byte afn = gwmsg.afn();
		if( gwmsg.isLogin()){
			rws.setFunc(RtuWorkStateItem.FUNC_GW_LOGIN);
		}else if( gwmsg.isHeartbeat() )
			rws.setFunc(RtuWorkStateItem.FUNC_GW_HEART);
		else if( afn == MessageConst.GW_FUNC_GET_DATA2 )
			rws.setFunc(RtuWorkStateItem.FUNC_TASK );
		else if( gwmsg.isNeedConfirm() )
			rws.setFunc(RtuWorkStateItem.FUNC_GW_NEED_CFM);
		else
			rws.setFunc(RtuWorkStateItem.FUNC_REPLY);
		BatchSynchronizer.getInstance().addWorkState(rws);
		
		//4. �ն˹������� ��ϵ����
		try{
			String gateAddr = event.getClient().getPeerAddr();	//���ص�ǰ�û��ӿڵ�ַ��
			if( ! gateAddr.equals(state.getActiveGprs()) ){
				state.setActiveGprs(gateAddr);
				updateState = true;
				
			}
			if( updateState ){				
				RealtimeSynchronizer.getInstance().setRtuState(state);
			}
		}catch(Exception err){
			log.error("update activeGprs exp:"+err.getLocalizedMessage(),err);
		}	
		
		/*//5.1 �������ı���
		if( gwmsg.afn() == MessageConst.GW_FUNC_HEART ){
			//������������ҵ���¼�
			if(WsGlobalMap.getInstance().isSaveHeartbeat2Db(gwmsg.getRtua()))
				asyncDbService.log2Db(gwmsg);
			return;
		}*/
		RealtimeSynchronizer.getInstance().saveCommState(state);
		//5.2 ԭʼ���ı���,���������
		if(gwmsg.isHeartbeat() && !gwmsg.isLogin()){
			//���������֡����Ҫ��������
			if(isSaveHeartbeat(gwmsg.getLogicalAddress())){
				asyncDbService.log2Db(gwmsg);
			}
		}else{
			asyncDbService.log2Db(gwmsg);			
		}
		
		//6. �����Զ��屨�ģ���Ҫֱ�ӷ��͸����ҡ����ܰ���Ŀǰ������ȡ���ģʽ��
//		if( gwmsg.afn() == MessageConst.GW_FUNC_FILE ){
//			MessageGate gatemsg = new MessageGate();
//			gatemsg.setUpInnerMessage(gwmsg);
//			intfClient.sendMessage(gatemsg);
//		}
//		else
			msgQueue.offer(gwmsg);
	}
	private boolean isSaveHeartbeat(String logicalAddress) {
		return WsGlobalMap.getInstance().isSaveHeartbeat2Db(logicalAddress);
	}

	/**
	 * �Ƿ񱣴汨��
	 * @param gwmsg
	 * @return
	 */
//	private boolean isSaveHeartbeat() {
//		String isSave=System.getProperty("fe.saveHearBeat");
//		if(isSave==null || "".equals(isSave)) return true;
//		return Boolean.parseBoolean(isSave);
//	}
	private void onSendMessage(SendMessageEvent e){
		IMessage message = e.getMessage();
		IMessage rtuMsg = null;
		if( message.getMessageType() == MessageType.MSG_GATE ){
			MessageGate gateMsg = (MessageGate)message;
			//����֧�ֿͻ��������Ĺ��ܡ���������������client���ͱ��ġ�HREQ���������������á�
			if( gateMsg.getHead().getCommand() == MessageGate.CMD_GATE_HREQ ){
				//�ͻ�������ı���������Ӧ��
				return;
			}
			else if(gateMsg.getHead().getCommand() == MessageGate.CMD_GATE_REQUEST ){
				rtuMsg = gateMsg.getInnerMessage();
				rtuMsg.setTxfs(gateMsg.getTxfs());
				rtuMsg.setIoTime(gateMsg.getIoTime());
				rtuMsg.setSource(gateMsg.getSource());
				rtuMsg.setServerAddress(gateMsg.getHead().getAttributeAsString(GateHead.ATT_DESTADDR));
			}
			else
				return;
		}
		else if( message.getMessageType() == MessageType.MSG_ZJ || message.getMessageType() == MessageType.MSG_GW_10|| message.getMessageType() == MessageType.MSG_DLMS ||message.getMessageType()==MessageType.MSG_BENGAL ){
			rtuMsg = message;
		}
		if( null == rtuMsg )
			return;
		
		//2. ����ͳ�ơ�
		int flow = rtuMsg.length();
		RtuState  state = RealtimeSynchronizer.getInstance().getRtuState(rtuMsg.getLogicalAddress());
		if( null == state ){
			state = RealtimeSynchronizer.getInstance().loadFromDb(rtuMsg.getLogicalAddress());
			RealtimeSynchronizer.getInstance().setRtuState(state);
		}
		
		RtuWorkStateItem rws = new RtuWorkStateItem();
		rws.setIoTime(System.currentTimeMillis());
		rws.setRtua(state.getRtua());
		rws.setLen(flow);
		byte func = RtuWorkStateItem.FUNC_DOWN_REQ;
		if( rtuMsg instanceof MessageZj ){
			MessageZj zjmsg = (MessageZj)rtuMsg;
			if( zjmsg.head.c_func == MessageConst.ZJ_FUNC_ALARM_CONFIRM )
				func = RtuWorkStateItem.FUNC_DOWN_CFM;
		}
		
		rws.setFunc( func );
		BatchSynchronizer.getInstance().addWorkState(rws);
		
		//3. ԭʼ���ı��棬ע�⣬������ݿⷱæ��ԭʼ���Ŀ��ܻᶪ����
		if( null != asyncDbService ){
			if(rtuMsg instanceof DlmsMessage){
				((DlmsMessage) rtuMsg).setDirection(IMessage.DIRECTION_DOWN);
			}if(rtuMsg instanceof AnsiMessage){
				((AnsiMessage) rtuMsg).setDirection(IMessage.DIRECTION_DOWN);
			}
			asyncDbService.log2Db(rtuMsg);
		}
	}

	public void setMsgQueue(FEMessageQueue msgQueue) {
		this.msgQueue = msgQueue;
	}

	public void setHeartBeat(Object heartBeat) {
//		this.heartBeat = (HeartBeatMessage)heartBeat;
	}
	public final void setAsyncDbService(AsyncService asyncDbService) {
		this.asyncDbService = asyncDbService;
	}
	public void setIntfClient(ClientModule intfClient) {
		this.intfClient = intfClient;
	}
	public void setMasterDbService(MasterDbService masterDbService) {
//		this.masterDbService = masterDbService;
	}
	
}
