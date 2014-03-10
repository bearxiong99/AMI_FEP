package cn.hexing.fk.sockclient.async;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import cn.hexing.fk.common.events.GlobalEventHandler;
import cn.hexing.fk.common.spi.IModule;
import cn.hexing.fk.common.spi.socket.IClientIO;
import cn.hexing.fk.common.spi.socket.IServerSideChannel;
import cn.hexing.fk.common.spi.socket.ISocketServer;

import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.IMessageCreator;
import cn.hexing.fk.message.MultiProtoRecognizer;
import cn.hexing.fk.sockclient.async.event.ClientConnectedEvent;
import cn.hexing.fk.sockserver.io.SocketIoThreadPool;
import cn.hexing.fk.utils.State;

/**
 * 异步socket通讯连接池。AsyncSocketPool类似于SocketServer，管理所有异步SocketChannel。
 * 核心思路：
 * 	每个socketChannel归属于
 *
 */
public class AsyncSocketPool implements ISocketServer {
	private static final Logger log = Logger.getLogger(AsyncSocketPool.class);
	private static int sequence = 1;
	//可配置属性
	private String name = "异步socket连接池";
	private String peerIp;
	private int peerPort;
	private int clientSize = 1;
	private int ioThreadSize = 2;	 //socket IO 线程池大小
	private int bufLength = 256;	 //socket客户端缓冲区大小
	//SocketChannel读写接口实现类路径。
	private IClientIO ioHandler = null;	//SocketChannel读、写操作接口
	//本socket 接收到的报文对象类型。
	private String messageClass = "cn.hexing.fk.sockserver.message.SimpleMessage";
	private IMessageCreator messageCreator;
	private int timeout = 30*60;		//默认30 分钟超时

	/**
	 * 通讯方式定义：
	 * 01:短信; 02:GPRS;  03:DTMF;  04:Ethernet;
	 * 05:红外; 06:RS232; 07:CSD;   08:Radio; 	09:CDMA;
	 */
	private String txfs="02";
	//辅助属性
	private Class<IMessage> msgClassObject;
	
	//统计属性,初始化自动为0
	private long lastReceiveTime=0,lastSendTime=0;			//最新收、发时间
	private long totalRecvMessages=0,totalSendMessages=0;	//总共收、发消息总数
	private int msgRecvPerMinute=0,msgSendPerMinute=0;		//每分钟收、发报文个数

	//对象属性
	private volatile State state = State.STOPPED; //服务器状态
	protected List<JAsyncSocket> clients = Collections.synchronizedList(new ArrayList<JAsyncSocket>(1024));
	private SocketIoThreadPool ioPool = null;
	private AsyncSocketConnectThread connectThread;

	@SuppressWarnings("unchecked")
	public boolean start() {
		try{
			if( null == messageCreator ){
				msgClassObject = (Class<IMessage>)Class.forName(messageClass);
				msgClassObject.newInstance();
			}
			else {
				if( null != messageClass && messageClass.length()>0 ){
					try{
						msgClassObject = (Class<IMessage>)Class.forName(messageClass);
						msgClassObject.newInstance();
					}catch(Exception e){
						log.error("AsyncSocketPool对象创建错误,检查配置文件："+e.getLocalizedMessage(),e);
					}
				}
			}
		}
		catch(Exception exp){
			log.error("AsyncSocketPool对象创建错误,检查配置文件："+exp.getLocalizedMessage(),exp);
			System.exit(-1);
		}
		if( !state.isStopped() ){
			log.warn("AsyncSocketPool 非停止状态，不能启动服务。");
			return false;
		}
		if( ioThreadSize<=0 )
			ioThreadSize = Runtime.getRuntime().availableProcessors()*2;
		state = State.STARTING;
		
		ioPool = new SocketIoThreadPool(peerPort,ioThreadSize,ioHandler);
		ioPool.start();
		
		connectThread = new AsyncSocketConnectThread();
		connectThread.start();
		int cnt = 1000;
		while(! state.isRunning() && cnt-->0 )
		{
			Thread.yield();
			try{
				Thread.sleep(10);
			}catch(InterruptedException e){}
		}
		//创建client socket channel
		for(int i=0;i<clientSize;i++){
			clients.add(new JAsyncSocket(this));
		}
		log.info("AsyncSocketPool启动成功");
//		GlobalEventHandler.postEvent(new ServerStartedEvent(this));
		return true;
	}

	public void stop() {
		if( !state.isRunning() )
			return;
		state = State.STOPPING; // stopping

		//停止Server socket侦听线程
		connectThread.interrupt();
		int cnt = 500;
		while( connectThread.isAlive() && cnt-->0 ){
			Thread.yield();
			try{
				connectThread.join(20);
			}catch(InterruptedException e){}
		}
		connectThread = null;
		
		//停止所有client socket IO 线程
		ioPool.stop();
		ioPool = null;
		
		clients.clear();
		state = State.STOPPED;
		log.info("AsyncSocketPool 停止");
	}
	
	public IMessage createMessage(ByteBuffer buf) {
		if( null != messageCreator ){
			IMessage msg = messageCreator.create();
			if( null == msg )
				msg = MultiProtoRecognizer.recognize(buf);
			return msg;
		}
		try{
			return msgClassObject.newInstance();
		}
		catch(Exception exp){
			return null;
		}
	}

	public int getBufLength() {
		return this.bufLength;
	}

	public int getClientSize() {
		return clientSize;
	}
	
	/**
	 * 返回异步TCP客户端连接池所有Socket Client对象数组
	 */
	public IServerSideChannel[] getClients(){
		return clients.toArray(new IServerSideChannel[0]);
	}

	public long getLastReceiveTime() {
		return this.lastReceiveTime;
	}

	public long getLastSendTime() {
		return this.lastSendTime;
	}

	public int getMaxContinueRead() {
		return 10;
	}

	public int getMsgRecvPerMinute() {
		return this.msgRecvPerMinute;
	}

	public int getMsgSendPerMinute() {
		return this.msgSendPerMinute;
	}

	public int getPort() {
		return 0;
	}

	public long getTotalRecvMessages() {
		return this.totalRecvMessages;
	}

	public long getTotalSendMessages() {
		return this.totalSendMessages;
	}

	public String getTxfs() {
		return txfs;
	}

	public void incRecvMessage() {
		totalRecvMessages++;
	}

	public void incSendMessage() {
		totalSendMessages++;
	}

	public void setTxfs(String txfs) {
		this.txfs = txfs;
	}

	public IClientIO getIoHandler() {
		return this.ioHandler;
	}

	public int getWriteFirstCount() {
		return 0;
	}

	public void removeClient(IServerSideChannel client) {
		boolean found = false;
		for(JAsyncSocket sock : clients){
			if( sock.isConnected() ){
				found = true;
				break;
			}
		}
		if( !found ){
			this.totalRecvMessages = 0;
			this.totalSendMessages = 0;
		}
	}

	public void setLastReceiveTime(long lastRecv) {
		this.lastReceiveTime = lastRecv;
	}

	public void setLastSendTime(long lastSend) {
		this.lastSendTime = lastSend;
	}

	public int getIoThreadSize() {
		return ioThreadSize;
	}

	public void setIoThreadSize(int ioThreadSize) {
		this.ioThreadSize = ioThreadSize;
	}

	public String getMessageClass() {
		return messageClass;
	}

	public void setMessageClass(String messageClass) {
		this.messageClass = messageClass;
	}

	public IMessageCreator getMessageCreator() {
		return messageCreator;
	}

	public void setMessageCreator(IMessageCreator messageCreator) {
		this.messageCreator = messageCreator;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public void setClientSize(int clientSize) {
		this.clientSize = clientSize;
	}

	public void setBufLength(int bufLength) {
		this.bufLength = bufLength;
	}

	public void setIoHandler(IClientIO ioHandler) {
		this.ioHandler = ioHandler;
	}

	class AsyncSocketConnectThread extends Thread{
		private Selector selector;
		private long lastCheckReConnect = System.currentTimeMillis();	//用于检查是否需要重新连接
		private long lastCheckTimeout = System.currentTimeMillis();
		private long now = 0;
		public AsyncSocketConnectThread(){
			super("AsyncSockConnect-"+sequence++);
		}

		public void run() {
			try{
				selector = Selector.open();
			}
			catch(Exception e){
				log.error("AsyncSocketPool 连接线程异常（selector.open)"+e.getMessage());
				return;
			}
			long time1 = System.currentTimeMillis();
			for(JAsyncSocket client: clients){
				toConnect(client);
			}
			long milli = System.currentTimeMillis()-time1;
			log.info(getName()+",开始侦听异步连接事件...[socket创建时间="+milli+"]");
			state = cn.hexing.fk.utils.State.RUNNING;
			while ( state != cn.hexing.fk.utils.State.STOPPING ){
				now = System.currentTimeMillis();
				//执行client socket channel连接到服务器
				try{
					tryConnect();
				}
				catch(Exception e){
					log.error("AsyncSocketPool ConnectThread异常:"+e.getLocalizedMessage(),e);
				}
				//检查socket client 是否长时间没有IO。 1分钟检查一次。
				if( now-lastCheckTimeout> 1000*60 ){
					checkTimeout();
					lastCheckTimeout = now;
				}
			}
			try{
				selector.close();
			}
			catch(IOException ioe){
				log.warn("selector.close异常："+ioe.getLocalizedMessage());
			}
			selector = null;
			state = cn.hexing.fk.utils.State.STOPPED;
		}

		private void tryConnect() throws IOException{
			selector.select(100);		//每隔0.1秒唤醒一次，以便检测client连接超时情况
			//实际上，select每秒出现N多次，阻塞失效。
			if( now-lastCheckReConnect > 1000 ){
				lastCheckReConnect = now;
				for(JAsyncSocket client: clients){
					SocketChannel channel = client.getChannel();
					if( null == channel )
						toConnect(client);
					else if( channel.isConnectionPending() || channel.isConnected() )
						continue;
					else
						toConnect(client);
				}
			}
			Set<SelectionKey> set = selector.selectedKeys();
			if( set.size()>0 )
				log.debug("发现连接成功事件");
			for( SelectionKey key: set ){
				if (key.isConnectable()){
					doConnect(key);
				}
				else{
					//不应该出现这种情况
					log.warn("在Connect时候，SelectionKey非法："+key);
					key.cancel();
				}
			}
			set.clear();
		}
		
		private void doConnect(SelectionKey key){
			JAsyncSocket client = (JAsyncSocket)key.attachment();
			try{
				if( client.getChannel().finishConnect() ){
					//成功连接
					ioPool.addConnectedClient(client);
					GlobalEventHandler.postEvent(new ClientConnectedEvent(AsyncSocketPool.this,client));
					if( log.isDebugEnabled() )
						log.debug("异步连接成功:"+client);
				}
			}catch(Exception e){
				//连接失败。
				toConnect(client);
			}
		}
		
		private void toConnect(JAsyncSocket client){
			try{
				long now = System.currentTimeMillis();
				if( now-client.getLastConnectTime()< 15*1000 ){
					return;
				}
				client.createChannel();
				client.setLastConnectTime(now);
				SocketChannel channel = client.getChannel();
				channel.configureBlocking(false);
				channel.register(selector, SelectionKey.OP_CONNECT, client);
				if( channel.connect(new InetSocketAddress(peerIp,peerPort)) ){
					try{
						if( client.getChannel().finishConnect() ){
							//成功连接
							ioPool.addConnectedClient(client);
							GlobalEventHandler.postEvent(new ClientConnectedEvent(AsyncSocketPool.this,client));
							if( log.isDebugEnabled() )
								log.debug("异步连接成功:"+client);
						}
					}catch(Exception e){
						//连接失败。需要特别考虑 ***
						log.error(e.getLocalizedMessage(),e);
					}
				}
			}
			catch(Exception e){
				log.error(e.getLocalizedMessage(),e);
			}
		}
		
		private void checkTimeout(){
			
		}
		
	}

	public String getPeerIp() {
		return peerIp;
	}

	public void setPeerIp(String peerIp) {
		this.peerIp = peerIp;
	}

	public int getPeerPort() {
		return peerPort;
	}

	public void setPeerPort(int peerPort) {
		this.peerPort = peerPort;
	}
	
	public boolean isRunning(){
		return state.isRunning();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getModuleType() {
		return IModule.MODULE_TYPE_SOCKET_CLIENT;
	}

	public boolean isActive() {
		return false;
	}

	public String profile() {
		return "";
	}

	public String getServerAddress(){
		return "";
	}
	
	public boolean useDirectBuffer(){
		return true;
	}

	public String getMonitedIPs() {
		return "";
	}
}

