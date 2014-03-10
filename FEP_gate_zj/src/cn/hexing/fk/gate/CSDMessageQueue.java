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
 * @time 2013-3-22 上午09:42:48
 *
 * @info 包含两个队列，一个消息队列，一个链路队列
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
	 * 将当前连接过来的client加入到idleClients队列
	 */
	public void onCSDClientIdle(CSDSyncTcpSocketClient client){
		synchronized (clients) {
			clients.notifyAll();
		}
	}
	
	/**
	 * 将当前关闭掉的client从idleClient队列中移走
	 */
	public void onCSDClientClose(CSDSyncTcpSocketClient client){
		synchronized (clients){
			clients.remove(client);
		}
	}
	
	/**
	 * 将上行消息发来的数据添加到列表中
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
				// 一直循环，从列表中取出消息
				IMessage msg = msgQueue.take();
				if (msg == null) {
					continue;
				}
				synchronized (clients) {
					CSDSyncTcpSocketClient client = findIdleClient(msg);
					int time = 0;
					while(client==null && time <5){
						try {
							clients.wait(1000); //等一秒，循环5次，如果还没有clients,将消息扔掉
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
			//简单循环队列算法
			//从curIndex开始检测到最后
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
			//从0到curIndex
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
