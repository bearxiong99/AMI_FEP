package cn.hexing.fk.gate;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import cn.hexing.fk.common.queue.CacheQueue;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.gate.GateHead;
import cn.hexing.fk.message.gate.MessageGate;
import cn.hexing.fk.sockclient.csd.CSDSyncTcpSocketClient;
import cn.hexing.fk.sockclient.csd.CSDSyncTcpSocketClient.CSD_CLIENT_STATUS;
import cn.hexing.fk.utils.StringUtil;

/**
 * 
 * @author gaoll
 *
 * @time 2013-3-22 ����09:42:48
 *
 * @info �����������У�һ����Ϣ���У�һ����·����
 */
public class CSDMessageQueue {

	private static final Logger log = Logger.getLogger(CSDMessageQueue.class);
	
	private static CSDMessageQueue instance=null;
	
	private int curIndex;
	
	private CSDMessageQueue(){
		msgQueue = new CacheQueue();
		ProcessThread pt = new ProcessThread();
		pt.start();
	};
	
	CacheQueue msgQueue;
	
	public MessageQueue feQueue;
	//msg
	//channel
	private List<CSDSyncTcpSocketClient> clients = new ArrayList<CSDSyncTcpSocketClient>();
	
	public static CSDMessageQueue getInstance(){
		if(instance==null){
			instance = new CSDMessageQueue();
		}
		return instance;
	}
	
	public void onCSDClientConnected(CSDSyncTcpSocketClient client){
		synchronized (clients) {
			clients.add(client);
			clients.notifyAll();
		}
	}
	
	/**
	 * ����ǰ���ӹ�����client���뵽idleClients����
	 */
	public void onCSDClientIdle(CSDSyncTcpSocketClient client){
		synchronized (clients) {
			clients.notifyAll();
		}
	}
	
	/**
	 * ����ǰ�رյ���client��idleClient����������
	 */
	public void onCSDClientClose(CSDSyncTcpSocketClient client){
		synchronized (clients){
			clients.remove(client);
		}
	}
	
	/**
	 * ��������Ϣ������������ӵ��б���
	 * @param message
	 */
	public void offerDownMessage(IMessage message){
		msgQueue.offer(message);
	}
	
	
	private class ProcessThread extends Thread{

		public ProcessThread(){
			this.setDaemon(true);
		}
		
		@Override
		public void run() {
			while (true) {
				// һֱѭ�������б���ȡ����Ϣ
				IMessage msg = msgQueue.take();
				if (msg == null) {
					continue;
				}
				synchronized (clients) {
					CSDSyncTcpSocketClient client = findIdleClient(msg);
					int time = 0;
					while(client==null && time <5){
						try {
							clients.wait(1000); //��һ�룬ѭ��5�Σ������û��clients,����Ϣ�ӵ�
						} catch (InterruptedException e) {
							log.error(StringUtil.getExceptionDetailInfo(e));
						}
						time++;
						client=findIdleClient(msg);
					}
					if(client == null) {
						log.error("Find Idle Client Over Time.Discard Message");
						continue;
					}
					try {
						client.processDownMessage(msg);
					} catch (Exception e) {
						log.error(StringUtil.getExceptionDetailInfo(e));
					}
				}
			}
		}
		
	}
	
	private CSDSyncTcpSocketClient findIdleClient(IMessage msg){
		
		MessageGate gateMsg = (MessageGate) msg;
		
		if( clients.size()==0 )
			return null;
		synchronized(clients){
			CSDSyncTcpSocketClient client;
			//��ѭ�������㷨
			//��curIndex��ʼ��⵽���
			for(int i=curIndex; i<clients.size(); i++ ){
				client = (CSDSyncTcpSocketClient)clients.get(i);
				if( client.getStatus()==CSD_CLIENT_STATUS.IDLE || 
					(client.getStatus()==CSD_CLIENT_STATUS.PENDING && 
					 client.phoneNum.equals(gateMsg.getHead().getAttributeAsString(GateHead.ATT_SIM_NUM)))){
					curIndex = i+1;
					if( curIndex>= clients.size() )
						curIndex = 0;
					return client;
				}
			}
			//��0��curIndex
			for(int i=0; i<curIndex; i++ ){
				client = (CSDSyncTcpSocketClient)clients.get(i);
				if( client.getStatus()==CSD_CLIENT_STATUS.IDLE ||
					(client.getStatus()==CSD_CLIENT_STATUS.PENDING && 
					client.phoneNum.equals(gateMsg.getHead().getAttributeAsString(GateHead.ATT_SIM_NUM)))){
					curIndex = i+1;
					if( curIndex>= clients.size() )
						curIndex = 0;
					return client;
				}
			}
			return null;
		}
	}	
}
