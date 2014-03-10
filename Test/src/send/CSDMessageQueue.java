package send;

import java.util.ArrayList;
import java.util.List;

import com.hx.dlms.message.DlmsMessage;

import send.CSDSyncTcpSocketClient.CSD_CLIENT_STATUS;

import cn.hexing.fk.common.queue.CacheQueue;
import cn.hexing.fk.common.spi.socket.IServerSideChannel;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.msgbytes.MessageBytes;

/**
 * 
 * @author gaoll
 *
 * @time 2013-3-22 上午09:42:48
 *
 * @info 包含两个队列，一个消息队列，一个链路队列
 */
public class CSDMessageQueue {

	
	private static CSDMessageQueue instance=null;
	
	private int curIndex;
	
	private CSDMessageQueue(){
		msgQueue = new CacheQueue();
		ProcessThread pt = new ProcessThread();
		pt.start();
	};
	
	CacheQueue msgQueue;
	//msg
	//channel
	private List<CSDSyncTcpSocketClient> clients = new ArrayList<CSDSyncTcpSocketClient>();
	
	public static CSDMessageQueue getInstance(){
		if(instance==null){
			instance = new CSDMessageQueue();
		}
		return instance;
	}
	
	/**
	 * 将当前连接过来的client加入到idleClients队列
	 */
	public void onCSDClientIdle(CSDSyncTcpSocketClient client){
		synchronized (clients) {
			clients.add(client);
			clients.notifyAll();
		}
		DlmsMessage mb = new DlmsMessage();
		mb.setApdu("asd".getBytes());
		offerDownMessage(mb);
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
					CSDSyncTcpSocketClient client = findIdleClient();
					while(client==null){
						try {
							clients.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						client=findIdleClient();
					}
					client.processDownMessage(msg);
				}
			}
		}
		
	}
	
	private CSDSyncTcpSocketClient findIdleClient(){
		
		if( clients.size()==0 )
			return null;
		synchronized(clients){
			CSDSyncTcpSocketClient client;
			//简单循环队列算法
			//从curIndex开始检测到最后
			for(int i=curIndex; i<clients.size(); i++ ){
				client = (CSDSyncTcpSocketClient)clients.get(i);
				if( client.getStatus()==CSD_CLIENT_STATUS.IDLE ){
					curIndex = i+1;
					if( curIndex>= clients.size() )
						curIndex = 0;
					return client;
				}
			}
			//从0到curIndex
			for(int i=0; i<curIndex; i++ ){
				client = (CSDSyncTcpSocketClient)clients.get(i);
				if( client.getStatus()==CSD_CLIENT_STATUS.IDLE ){
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
