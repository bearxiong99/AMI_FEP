/**
 * ҵ������Socket���������¼�������
 * ���ܸ�����
 * 		�����յ�ҵ������(BP)���б��ġ����б��ķ��ͳɹ��¼���
 *      ҵ���������б���ͨ��FEMessageQueue���󷽷�ֱ�ӷ��͸����أ�������ֱ�ӷ��͸��նˣ�
 * ����ʵ�֣�
 * BasicEventHook�����ࡣ
 * override handleEvent���������ReceiveMessageEvent��SendMessageEvent�ر���
 * ע�������spring�����ļ��У�source���������ҵ����������ӿڵ�SocketServer����
 */
package cn.hexing.fk.fe.bpserver;

import java.nio.ByteBuffer;

import cn.hexing.fk.FasSystem;
import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.events.BasicEventHook;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.common.spi.socket.IServerSideChannel;
import cn.hexing.fk.fe.msgqueue.BpBalanceFactor;
import cn.hexing.fk.fe.msgqueue.FEMessageQueue;
import cn.hexing.fk.fe.msgqueue.MessageDispatch2Bp;
import cn.hexing.fk.fe.msgqueue.Msg2BpByMod;
import cn.hexing.fk.fe.rmi.RtuInfoServer;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.MessageConst;
import cn.hexing.fk.message.MessageType;
import cn.hexing.fk.message.gate.GateHead;
import cn.hexing.fk.message.gate.MessageGate;
import cn.hexing.fk.message.zj.MessageZj;
import cn.hexing.fk.model.RtuManage;
import cn.hexing.fk.sockserver.event.ClientCloseEvent;
import cn.hexing.fk.sockserver.event.ReceiveMessageEvent;
import cn.hexing.fk.sockserver.event.SendMessageEvent;
import cn.hexing.fk.tracelog.TraceLog;
import cn.hexing.fk.utils.HexDump;

import org.apache.log4j.Logger;

import com.hx.ansi.message.AnsiMessage;

/**
 */
public class BPServerEventHandler extends BasicEventHook {
	private static final Logger log = Logger.getLogger(BPServerEventHandler.class);
	private static final TraceLog trace = TraceLog.getTracer(BPServerEventHandler.class);
	private FEMessageQueue msgQueue;
	private boolean noConvert = false;		//�Ƿ�ֱ�������㽭��Լԭʼ����.
	private boolean dispatchRandom = true;
	
	//�ڲ�����
	//private List<IServerSideChannel> bpClients = Collections.synchronizedList(new ArrayList<IServerSideChannel>());

	public BPServerEventHandler(){
	}
	
	@Override
	public boolean start() {
		return super.start();
	}
	
	public void setMsgQueue(FEMessageQueue queue) {
		this.msgQueue = queue;
		msgQueue.setDispatchRandom(dispatchRandom);
		msgQueue.setNoConvert(noConvert);
	}
	
	public FEMessageQueue getMsgQueue(){
		return msgQueue;
	}
	
	/**
	 * �յ�ҵ�����������б���
	 * @param e
	 */
	private void onRecvMessage(ReceiveMessageEvent e){
		//�������ع�Լ���ģ���Ҫת�����㽭��Լ���ſ��Է��͸��㽭�նˡ�
		IMessage msg = e.getMessage();
		if( msg.getMessageType() == MessageType.MSG_GATE ){
			MessageGate mgate = (MessageGate)msg;
			//����֧�ֿͻ��������Ĺ��ܡ���������������client���ͱ��ġ�HREQ���������������á�
			if( mgate.getHead().getCommand() == MessageGate.CMD_GATE_HREQ ){
				IServerSideChannel client = (IServerSideChannel)msg.getSource();
				//only business processor can send heart-beat to FE. Manufacture module send MessageZj.
				//If there is a client channel in bpClients, then fire msgQueue onConnect event
				//if( bpClients.remove(client) )
				msgQueue.onBpClientConnected(client);
				
				//��ȡ�ͻ�������ı�������
				ByteBuffer data = mgate.getData();
				int numPackets = data.remaining()<4 ? -1 : data.getInt();
				synchronized(client){
					client.setRequestNum(numPackets);
				}
				//Ӧ������
				MessageGate hreply = MessageGate.createHReply();
				client.send(hreply);
				Msg2BpByMod.getInstance().clearTimeoutChannel();
				MessageDispatch2Bp.getInstance().clearTimeoutChannel();
				return;		//�����������
			}
			else if(mgate.getHead().getCommand() == MessageGate.CMD_GATE_REQUEST ){
				IMessage rtuMsg = mgate.getInnerMessage();
				if(rtuMsg instanceof AnsiMessage){
					String logicalAddr = mgate.getHead().getAttributeAsString(GateHead.ATT_LOGICALADDR);
					rtuMsg.setLogicalAddress(logicalAddr);
				}
				//ָ������ͨ�����У���Ҫ��sendMessage�����ж�MessageGate��
				String peer = rtuMsg.getPeerAddr();
				if( null==peer || peer.length()==0 )
					rtuMsg.setPeerAddr(mgate.getSource().getPeerAddr());	//�����ͳɹ���zjmsg.peerAddr����Ϊ���б�����Դ��ַ
				
//				RtuInfoServer.getInstance().setCurrentMsg(MessageConst.DIR_DOWN, rtuMsg);
				
				boolean success = msgQueue.sendMessage(mgate);
				if(success){
					log.debug("Process down message success!\n\t sendTo:"+peer+"\t logicalAddress:"+mgate.getLogicalAddress()+"\n\t msg:"+rtuMsg);
				}
			}
			else if( mgate.getHead().getCommand() == MessageGate.REP_MONITOR_RELAY_PROFILE ){
				String bpProfile = new String(mgate.getData().array());
				FasSystem.getFasSystem().addBizProcessorProfile(e.getClient().getPeerAddr(), bpProfile);
				return;
			}
			else if( mgate.getHead().getCommand() == MessageGate.CMD_WRAP ){
				//��Լֱ֡�����У������ǳ��ҽ���ģ�顣ֱ����������
				IMessage rtuMsg = mgate.getInnerMessage();
				if( null == rtuMsg )
					return;
				//�����ͳɹ���zjmsg.peerAddr����Ϊ���б�����Դ��ַ
				String peer = rtuMsg.getPeerAddr();
				if( null==peer || peer.length()==0 )
					rtuMsg.setPeerAddr(mgate.getSource().getPeerAddr());	//�����ͳɹ���zjmsg.peerAddr����Ϊ���б�����Դ��ַ
				rtuMsg.setPeerAddr(mgate.getSource().getPeerAddr());
				
//				RtuInfoServer.getInstance().setCurrentMsg(MessageConst.DIR_DOWN, rtuMsg);

				if(rtuMsg.getMessageType()==MessageType.MSG_DLMS){
					String logicalAddr = mgate.getHead().getAttributeAsString(GateHead.ATT_LOGICALADDR);
					rtuMsg.setLogicalAddress(logicalAddr);
				}
				boolean success = msgQueue.sendMessage(rtuMsg);
				if( success)
					log.info("Terminal Down Command:"+rtuMsg);//log.info("�ն���������:"+rtuMsg);
			}
		}
		else if( msg.getMessageType() == MessageType.MSG_ZJ ){
			MessageZj zjmsg = (MessageZj)msg;
			boolean success = msgQueue.sendMessage(zjmsg);
			if( success && log.isDebugEnabled() )
				log.debug("BP Down Command:"+zjmsg);//log.debug("ҵ��������������:"+zjmsg);
		}
	}

	/**
	 * ���ظ÷�����
	 */
	public void handleEvent(IEvent e) {
		if( e.getType() == EventType.MSG_RECV ){
			//���յ�ҵ���������б���
			onRecvMessage( (ReceiveMessageEvent)e);
		}
		else if( e.getType() == EventType.MSG_SENT ){
			//���ɹ��ѱ��ķ��͸�ҵ������
			onSendMessage( (SendMessageEvent)e );
		}
		else if( e.getType() == EventType.ACCEPTCLIENT ){
			/*//���������쳣�Ͽ������CLIENTCLOSE�¼����ܲ��ᷢ����������bpClients�������
			//ÿ��ɾ��1�����ɴﵽ��Ч����������
			for(int i=0; i<bpClients.size(); i++ ){
				try{
					IServerSideChannel client = bpClients.get(i);
					if( System.currentTimeMillis()-client.getLastIoTime() > 1000*60*30 ){
						bpClients.remove(i);
						if( trace.isEnabled() )
							trace.trace("garbage client removed:"+client);
						break;
					}
				}catch(Exception exp){
					break;
				}
			}
			AcceptEvent ae = (AcceptEvent)e;
			//��������֮�����жϵ�ǰ�����Ƿ���Ч���������¼�����ģ�͵������Ե��¿��ܳ���close�¼����д��������
			if (ae.getClient().getChannel().isConnected())
				bpClients.add(ae.getClient());*/
			//���MessageDispatch2Bp.clients��3��������ͨѶ����Ч����
			MessageDispatch2Bp.getInstance().clearTimeoutChannel();
			Msg2BpByMod.getInstance().clearTimeoutChannel();
		}
		else if( e.getType() == EventType.CLIENTCLOSE ){
			ClientCloseEvent ce = (ClientCloseEvent)e;
			//bpClients.remove(ce.getClient());
			msgQueue.onBpClientClosed(ce.getClient());
		}
		else if( e.getType() == EventType.MSG_SEND_FAIL ){
			//��client���رգ���sendList�б��ģ��������ա�
			msgQueue.pushBack(e.getMessage());
		}
		else
			super.handleEvent(e);
	}

	/**
	 * ��ҵ���������б��ĳɹ���
	 * @param e
	 */
	private void onSendMessage(SendMessageEvent e){
		IMessage msg = e.getMessage();
		if( ! msg.isHeartbeat() ){
			if( trace.isEnabled() ){
				String dest = msg.getSource().getPeerAddr();
				trace.trace("send to["+dest+"],rtua="+HexDump.toHex(msg.getRtua()));
			}
		}
		//�ر�ע�⣺����ǳ����Զ��屨�ķ��ͳɹ�������Ҫ�������͡�
		//��Ϊ�յ������Զ��屨��ʱ�����Զ���ס����ͨ���볧�ұ����ϵ��Ӧ�������������ϵ�Զ����͸�����ģ�顣
		if( msg instanceof MessageZj ){
			MessageZj zjmsg = (MessageZj)msg;
			if( zjmsg.head.c_func == MessageConst.ZJ_FUNC_USER_DEFINE ){
				if( log.isDebugEnabled() )
					log.debug("�����ҽ���ģ�鷢�ͱ��ĳɹ�:"+zjmsg.getRawPacketString());
				return;
			}
		}
		if( log.isDebugEnabled() )
			log.debug("send to BP success:"+msg);
		//���ķ��͵�ҵ����������Ҫ���������͡�
		//2009��1��18 modified by bhw��֧�ֶ�ҵ�����������յ��зַ���
		if( dispatchRandom ){
			IServerSideChannel client = (IServerSideChannel)e.getClient();
			trySendNextPacket(client);
		}
		else{
			//���յ��о���ַ���ҵ������
//			for(int i=0; i<3; i++)
				trySendNextPacketByA1Code();
		}
	}
	
	/**
	 * ���ն�����Ϣ�ĵ�������зַ���ҵ��������
	 */
	private void trySendNextPacketByA1Code(){
		IMessage msg = msgQueue.poll();
		if( null == msg )
			return;
		byte districtCode = BpBalanceFactor.getInstance().getDistrictCode(RtuManage.getInstance().getComRtuInCache(msg.getRtua()));
		IServerSideChannel client = MessageDispatch2Bp.getInstance().getBpChannel(districtCode);
		if( null == client ){
			//������ǰ��յ�����ַ��������Ƿ���ȡĪ��ʽ�ַ�
			client = Msg2BpByMod.getInstance().getBpChannel(msg.getRtua());
//			if( null == client )
//				client = Msg2BpByMod.getInstance().getIdleChannel();
		}
		if( null == client || (client.getRequestNum()>0 && client.sendQueueSize()>=client.getRequestNum()) ){
			msgQueue.pushBack(msg);
			if( trace.isEnabled() ){
				String info = "null==client.";
				if( null != client )
					info = "sendQueue.size="+client.sendQueueSize()+",requestNum="+client.getRequestNum();
				trace.trace("Not find bp channel, reason is: "+info);
			}
			return;
		}
		
		boolean success = false;
		if( noConvert ){
			success = client.send(msg);
		}
		else{
			//���㽭��Լ����ת�������ع�Լ�����͸�ǰ�û���
			MessageGate gateMsg = new MessageGate();
			gateMsg.setUpInnerMessage(msg);
			success = client.send(gateMsg);
		}
		String result = " success.";
		if( !success ){
			msgQueue.pushBack(msg);
			result = " failed.";
		}
		
		if(log.isInfoEnabled())
			log.info("send msg to BP "+result+" from pushback, msg:"+msg+",logicAddress:"+msg.getLogicalAddress());
		if( trace.isEnabled() )
			trace.trace("send msg to BP["+client.getPeerAddr()+"],rtua="+HexDump.toHex(msg.getRtua())+result+",mode="+Msg2BpByMod.getInstance().getBpMode(msg.getRtua()));
	}
	
	private void trySendNextPacket(IServerSideChannel client){
		//���client�����������Ƿ�ݼ���0��
		if( 0 >= client.getRequestNum() || client.sendQueueSize()>=client.getRequestNum() ){
			//���ܷ��͡�
			return;
		}
		IMessage msg = msgQueue.poll();
		if( null != msg ){
			boolean success = false;
			if( noConvert )
				success = client.send(msg);
			else{
				//���㽭��Լ����ת�������ع�Լ�����͸�ǰ�û���
				MessageGate gateMsg = new MessageGate();
				gateMsg.setUpInnerMessage(msg);
				success = client.send(gateMsg);
			}
			if( !success ){
				msgQueue.pushBack(msg);
			}
		}
	}
	
	public boolean isNoConvert() {
		return noConvert;
	}

	public void setNoConvert(boolean noConvert) {
		this.noConvert = noConvert;
		if( null != msgQueue )
			msgQueue.setNoConvert(noConvert);
	}
	
	public void setDispatchRandom(boolean dispRandom ){
		dispatchRandom = dispRandom;
		if( null != msgQueue )
			msgQueue.setDispatchRandom(dispatchRandom);
	}
}
