package send;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import cn.hexing.fk.common.events.GlobalEventHandler;
import cn.hexing.fk.common.simpletimer.ITimerFunctor;
import cn.hexing.fk.common.simpletimer.TimerData;
import cn.hexing.fk.common.simpletimer.TimerScheduler;
import cn.hexing.fk.common.spi.IModule;
import cn.hexing.fk.common.spi.socket.IServerSideChannel;
import cn.hexing.fk.common.spi.socket.abstra.BaseSocketServer;
import cn.hexing.fk.sockserver.SyncSocketClient;
import cn.hexing.fk.sockserver.event.AcceptEvent;
import cn.hexing.fk.sockserver.event.ModuleProfileEvent;
import cn.hexing.fk.sockserver.event.ServerStartedEvent;
import cn.hexing.fk.sockserver.event.ServerStoppedEvent;

public class SyncTcpServer extends BaseSocketServer implements IModule {
	private static final Logger log = Logger.getLogger(SyncTcpServer.class);
	private static enum ServerState{ STOPPED, START, RUNNING, STOPPING };
	
	private String clientClass=null;
	
	protected ServerSocketChannel ssc;
	protected Selector selector;		//用于异步Accept客户端连接
	private volatile ServerState state = ServerState.STOPPED;
	protected Map<String,SyncSocketClient> map = Collections.synchronizedMap(new HashMap<String,SyncSocketClient>(1024*50));
	private int requestNum = -1;
	private AcceptThread listenThread = null;
	
	private final ITimerFunctor timer = new ITimerFunctor(){
		public void onTimer(int id) {
			if( 0==id ){	//每分钟定时器
				long now = System.currentTimeMillis();
				if( now-lastReceiveTime < 60*1000 || now-lastSendTime < 60*1000 )
					GlobalEventHandler.postEvent(new ModuleProfileEvent(SyncTcpServer.this));
				synchronized(statisticsRecv){
					msgRecvPerMinute = 0;
				}
				synchronized(statisticsSend){
					msgSendPerMinute = 0;
				}
			}
			else if( 1==id ){
				checkTimeout();
			}
		}
	};
	
	private void checkTimeout(){
		//客户端超时检查定时器
		ArrayList<SyncSocketClient>list = new ArrayList<SyncSocketClient>(map.values());
		long now = System.currentTimeMillis();
		int closedCount = 0;
		for(SyncSocketClient client: list){
			if( now-client.getLastReadTime() > timeout*1000 ){
				forceCloseClient(client);
				closedCount++;
			}
		}
		if( closedCount>0 )
			log.warn("TCP服务["+name+"]超时关闭客户端连接数="+closedCount);
	}
	
	private void forceCloseClient(SyncSocketClient client){
		String clientKey = client.getPeerAddr();
		map.remove(clientKey);
		super.removeClient(client);
		client.close();
	}
	
	@Override
	public int getClientSize() {
		return map.size();
	}

	@Override
	public IServerSideChannel[] getClients() {
		return map.values().toArray(new IServerSideChannel[0]);
	}

	@Override
	public boolean isActive() {
		return state == ServerState.RUNNING;
	}

	@Override
	public boolean start() {
		if( state != ServerState.STOPPED ){
			log.warn("socket server["+port+"]非停止状态，不能启动服务。");
			return false;
		}
		long tm1 = System.currentTimeMillis();
		state = ServerState.START;
		listenThread = new AcceptThread();
		listenThread.start();
		int cnt = 1000;
		while( state != ServerState.RUNNING && cnt-->0 )
		{
			Thread.yield();
			try{
				Thread.sleep(10);
			}catch(InterruptedException e){}
		}
		//启动定时服务
		TimerScheduler.getScheduler().addTimer(new TimerData(timer,0,60));	//定时器0，每分钟定时器
		if( timeout > 0 )
			TimerScheduler.getScheduler().addTimer(new TimerData(timer,1,timeout));	//定时器1，clientTimeoutTask

		GlobalEventHandler.postEvent(new ServerStartedEvent(this));
		long span = System.currentTimeMillis() - tm1;
		log.info("TCP Server Start["+port+"], takes ("+span+") milliseconds.");
		return true;
	}

	@Override
	public void stop() {
		if( state != ServerState.RUNNING )
			return;
		long tm1 = System.currentTimeMillis();
		state = ServerState.STOPPING;
		listenThread.interrupt();
		int cnt = 500;
		while( listenThread.isAlive() && cnt-- > 0 ){
			Thread.yield();
			try{
				listenThread.join(20);
			}catch(InterruptedException e){}
		}
		listenThread = null;

		TimerScheduler.getScheduler().removeTimer(timer, 0);
		if( timeout>0 )
			TimerScheduler.getScheduler().removeTimer(timer, 1);
		state = ServerState.STOPPED;
		GlobalEventHandler.postEvent(new ServerStoppedEvent(this));
		long span = System.currentTimeMillis() - tm1;
		log.info("Server["+port+"] takes ("+span+") milliseconds to stop.");
	}

	@Override
	public String getModuleType() {
		return IModule.MODULE_TYPE_SOCKET_SERVER;
	}

	public void setRequestNum(int requestNum) {
		this.requestNum = requestNum;
	}
	
	private class AcceptThread extends Thread {
		public AcceptThread(){
			super("SyncTcpServer-"+port+"-AcceptThread");
		}
		public void run() {
			//启动Socket服务器
			try {
				ssc = ServerSocketChannel.open();
				ssc.socket().setReuseAddress(true);
				InetSocketAddress addr = null;
				if( null == ip )
					addr = new InetSocketAddress(port);
				else
					addr = new InetSocketAddress(ip,port);
				ssc.socket().bind(addr);
				ssc.configureBlocking(false);
			} catch (Exception exp) {
				//log it;
				log.fatal("SyncTcpServer start failed. "+exp.getLocalizedMessage()+",port="+port);
				return;
			}
			
			log.info("server[" + port + "]listen thread is running");

			try{
				selector = Selector.open();
				ssc.register(selector, SelectionKey.OP_ACCEPT);
			}
			catch(Exception e){
				log.error("socketserver 侦听线程异常（selectorOpen)"+e.getMessage());
				state = ServerState.STOPPED;
				return;
			}
			state = ServerState.RUNNING;
			
			long sign = System.currentTimeMillis();
			int cnt = 0;
			int times = 0;
			//tryAccept每间隔60秒唤醒一次，提供机会检测socket client超时情况。
			while ( state != ServerState.STOPPING){
				try{
					tryAccept();
					
					//利用计数器以及时钟，检测死循环
					cnt++;
					if( cnt>= 200 ){
						long now = System.currentTimeMillis();
						if( now-sign < 1000 ){
							log.warn("server[" + port + "]Accept thread可能进入死循环。");
						}
						cnt = 0;
						sign = System.currentTimeMillis();
					}
					
					//检查socket client 是否长时间没有IO。 1分钟检查一次。
					if( times++ >= 10 ){
						checkTimeout();
					}
				}
				catch(Exception e){
					log.error("server[" + port + "]AcceptThread异常:"+e.getLocalizedMessage(),e);
				}
			}
			try{
				ssc.close();
			}
			catch(IOException ioe){
				log.warn("ssc.close异常："+ioe.getLocalizedMessage());
			}
			ssc = null;
			try{
				selector.close();
			}
			catch(IOException ioe){
				log.warn("selector.close异常："+ioe.getLocalizedMessage());
			}
			selector = null;
			log.info("server[" + port + "]listen thread is stopping");
		}
		
		private void tryAccept() throws IOException,ClosedSelectorException{
			int n = selector.select(50);
			if( n<=0 ){
				//这里不做任何事情，继续检测selectedKeys。
			}
			Set<SelectionKey> set = selector.selectedKeys();
			for( SelectionKey key: set ){
				if (key.isAcceptable()){
					try{
						doAccept();
					}catch(Exception e){
						log.warn("doAccept()异常："+e.getLocalizedMessage(),e);
						key.cancel();
					}
				}
				else{
					//不应该出现这种情况
					log.warn("在Accept时候，SelectionKey非法："+key);
					key.cancel();
				}
			}
			set.clear();
		}
		
		private void doAccept()throws IOException{
			SocketChannel channel = ssc.accept();
			channel.socket().setReceiveBufferSize(bufLength);
			channel.socket().setSendBufferSize(bufLength);
			channel.socket().setTcpNoDelay(true);
			channel.socket().setSoTimeout(2*1000);
			channel.configureBlocking(true);
			SyncSocketClient client = new CSDSyncTcpSocketClient(channel,SyncTcpServer.this);
			if( requestNum>=0 )
				client.setRequestNum(requestNum);
			client.startThread();
			
			String clientKey = client.getPeerAddr();
			map.put(clientKey, client);
			
			//把接受客户端连接事件加入全局事件处理器
			GlobalEventHandler.postEvent(new AcceptEvent(client));
		}
	}

	public void setClientClass(String clientClass) {
		this.clientClass = clientClass;
	}
	
}
