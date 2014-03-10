package cn.hexing.fk.sockserver;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import cn.hexing.fk.common.events.GlobalEventHandler;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.common.spi.socket.IServerSideChannel;
import cn.hexing.fk.common.spi.socket.ISocketServer;

import cn.hexing.fk.exception.MessageParseException;
import cn.hexing.fk.exception.SocketClientCloseException;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.gate.MessageGate;
import cn.hexing.fk.sockserver.event.ClientCloseEvent;
import cn.hexing.fk.sockserver.event.MessageSendFailEvent;
import cn.hexing.fk.sockserver.event.ReceiveMessageEvent;
import cn.hexing.fk.sockserver.event.SendMessageEvent;
import cn.hexing.fk.tracelog.TraceLog;
import cn.hexing.fk.utils.HexDump;

public class SyncSocketClient implements IServerSideChannel {
	private static final Logger log = Logger.getLogger(SyncSocketClient.class);
	private static final TraceLog trace = TraceLog.getTracer(SyncSocketClient.class);
	
	protected SocketChannel channel = null;
	protected String peerIp; //对方IP地址
	protected int peerPort;  //对方port
	protected String peerAddr;
	protected String localIp;//本地IP地址
	protected int localPort;
	protected String localAddr; //ip+":"+port;
	
	//IO支撑属性
	protected ByteBuffer bufRead,bufWrite;
	protected List<IMessage> sendList = Collections.synchronizedList(new LinkedList<IMessage>());
	protected ISocketServer server;
	private int intKey = 0;				// 可以放置 RTUA
	private int maxSendQueueSize = 1000;	//同步发送，最大等待发送的报文数量
	
	//状态属性
	private long lastIoTime = System.currentTimeMillis();
	private long lastReadTime = System.currentTimeMillis();

	//客户端请求服务器发送的报文数量
	private int requestNum = -1;
	
	//同步属性 
	private final Object sendLock = new Object();
	private final Object messageSignal = new Object();
	//读写线程
	private ReadThread readThread = null;
	private WriteThread writeThread = null;

	public SyncSocketClient(SocketChannel c,ISocketServer s){
		channel = c;
		server = s;
		try {
			peerIp = channel.socket().getInetAddress().getHostAddress();
			peerPort = channel.socket().getPort();
			peerAddr = peerIp + ":" + peerPort +":T";
			localIp = channel.socket().getLocalAddress().getHostAddress();
			localPort = channel.socket().getLocalPort();
			localAddr = localIp + ":"
					+ HexDump.toHex((short)localPort);
			trace.trace("new socket connected. "+peerAddr);
		} catch (Exception e) {	}
		if( s.useDirectBuffer() ){
			bufRead = ByteBuffer.allocateDirect(s.getBufLength());
			bufWrite = ByteBuffer.allocateDirect(s.getBufLength());
		}
		else{
			bufRead = ByteBuffer.allocate(s.getBufLength());
			bufWrite = ByteBuffer.allocate(s.getBufLength());
		}
	}
	
	public void startThread(){
		if( null == readThread || null == writeThread ){
			readThread = new ReadThread();
			readThread.start();
			writeThread = new WriteThread();
			writeThread.start();
		}
	}

	public boolean send(IMessage msg) {
		if( null == channel )
			return false;
		if( msg instanceof MessageGate ){
			MessageGate gateMsg = (MessageGate)msg;
			if( gateMsg.isHeartbeat() ){
				return syncSend(msg);
			}
		}
		if( sendList.size()>= this.maxSendQueueSize ){
			log.warn(toString()+"-send list size>maxSendQueueSize,discard this msg. msg:"+msg);
			GlobalEventHandler.postEvent(new MessageSendFailEvent(msg,this));
			return false;
		}
		
		synchronized(messageSignal){
			if( this.requestNum>0 )
				this.requestNum--;
			sendList.add(msg);
			messageSignal.notify();
		}
		return true;
	}
	
	private boolean _flushChannel(){
		int bytesWritten = 0;
		while( bufWrite.hasRemaining() ){
			try{
				bytesWritten = channel.write(bufWrite);
			}catch(IOException exp){
				String s = "channel.write()异常，原因"+exp.getLocalizedMessage();
				log.warn(s,exp);
				trace.trace(s);
				throw new SocketClientCloseException(exp);
			}
			if( 0 == bytesWritten ){
				trace.trace("send 0 bytes.");
				return false;		//socket buffer full，但是还有数据没有发送完
			}
		}
		//缓冲区buf数据全部写到socket buffer
		bufWrite.clear();		//缓冲区清空以便下次写
		return true;
	}
	
	public boolean syncSend(IMessage msg){
		if( null == msg ){
			log.error("syncSend: msg == null");
			return false;
		}
		synchronized(sendLock){
			boolean done = false ;
			//增加死循环检测功能
			int deadloop = 0;
			while( !done ){
				done = msg.write(bufWrite);
				bufWrite.flip();
				_flushChannel();
				
				if( done ){
					msg.setIoTime(System.currentTimeMillis());
					msg.setPeerAddr(getPeerAddr());
					msg.setSource(this);
					msg.setTxfs(this.getServer().getTxfs());
					//通知，该消息已经完整发送出去啦
					setLastIoTime();
					server.incSendMessage();
					server.setLastSendTime(System.currentTimeMillis());
					if( msg.isHeartbeat() ){
						trace.trace("send heart-beat to"+getPeerAddr());
					}
					GlobalEventHandler.postEvent(new SendMessageEvent(msg,this));
				}
				
				if( ++deadloop > 1000 ){
					log.fatal("Message.write方法死循环错误："+msg.getClass().getName());
					return true;			//return true，丢失该消息对象。以免系统崩溃。
				}
			}
		}
		return true;
	}
	
	public boolean bufferHasRemaining() {
		return false;
	}

	public ByteBuffer getBufRead() {
		return bufRead;
	}

	public ByteBuffer getBufWrite() {
		return bufWrite;
	}

	public SocketChannel getChannel() {
		return channel;
	}

	public IMessage getCurReadingMsg() {
		return null;
	}

	public IMessage getCurWritingMsg() {
		return null;
	}

	public int getLastingWrite() {
		return 0;
	}

	public IMessage getNewSendMessage() {
		return null;
	}

	public SocketAddress getSocketAddress() {
		return null != channel ? channel.socket().getRemoteSocketAddress() : null;
	}

	public void setBufferHasRemaining(boolean hasRemaining) {
	}

	public void setCurReadingMsg(IMessage curReadingMsg) {
	}

	public void setCurWritingMsg(IMessage curWritingMsg) {
	}

	public void setLastingWrite(int writeCount) {
	}

	public void close() {
		try {
			if( null != channel ){
				trace.trace("client closed. "+peerAddr);
				channel.socket().shutdownInput();
				channel.socket().shutdownOutput();
				channel.close();
				IEvent event = new ClientCloseEvent(this);
				GlobalEventHandler.postEvent(event);
			}
		} catch (Exception exp) {
		}
		finally{
			if( null != channel ){
				if( log.isInfoEnabled() ){
					log.info("Client Close["+peerIp+":"+peerPort+",localport:"+localPort+"]");//log.info("客户端关闭["+peerIp+":"+peerPort+",localport:"+localPort+"]");
				}
				//未发送的消息通知
				synchronized(sendList){
					for(IMessage msg: sendList){
						GlobalEventHandler.postEvent(new MessageSendFailEvent(msg,this));
					}
					sendList.clear();
				}
			}
			channel = null;
		}
	}

	public long getLastIoTime() {
		return lastIoTime;
	}

	public long getLastReadTime() {
		return lastReadTime;
	}

	public String getPeerAddr() {
		return peerAddr;
	}

	public String getPeerIp() {
		return peerIp;
	}

	public int getPeerPort() {
		return peerPort;
	}

	public int getRequestNum() {
		return requestNum;
	}

	public ISocketServer getServer() {
		return server;
	}

	public int sendQueueSize() {
		return sendList.size();
	}

	public void setIoThread(Object threadObj) {
	}

	public void setLastIoTime() {
		lastIoTime = System.currentTimeMillis();
	}

	public void setLastReadTime() {
		lastReadTime = System.currentTimeMillis();
		lastIoTime = lastReadTime;
	}

	public void setMaxSendQueueSize(int maxSendQueueSize) {
		this.maxSendQueueSize = maxSendQueueSize;
	}

	public void setRequestNum(int reqNum) {
		requestNum = reqNum;
	}

	public int getIntKey() {
		return intKey;
	}

	public void setIntKey(int intKey) {
		this.intKey = intKey;
	}

	public String getLocalIp() {
		return localIp;
	}

	public int getLocalPort() {
		return localPort;
	}

	public String getLocalAddr() {
		return localAddr;
	}

	public int getMaxSendQueueSize() {
		return maxSendQueueSize;
	}

	public void setChannel(SocketChannel channel) {
		this.channel = channel;
	}

	public void setServer(ISocketServer server) {
		this.server = server;
	}

	private class ReadThread extends Thread {
		public ReadThread(){
			super("SyncTcpClient.read."+peerAddr);
		}

		@Override
		public void run() {
			try{
				doReceive();
			}catch(SocketClientCloseException e){
				close();
				return;
			}
			catch(Exception e){
				log.error("SyncClient doReceive exp:"+e.getLocalizedMessage(),e);
			}
		}
		
		private void doReceive(){
			int bytesRead = 0;
			IMessage msg = null;
			while(null != channel ){
				try{
					try{
						bytesRead = channel.read(bufRead);
					}catch(SocketTimeoutException expt){
						continue;
					}
					bufRead.flip();
				}catch(IOException e){
					log.warn("channel.read(bufRead) Exception:"+e.getLocalizedMessage());
					trace.trace("channel.read(bufRead) Exception:"+e.getLocalizedMessage());
					throw new SocketClientCloseException(e);
				}
				if( bytesRead <= 0 ){ //对方主动关闭
					String info = "client close by peer:"+peerAddr;
					log.info(info);
					trace.trace(info);
					throw new SocketClientCloseException(info);
				}
				if( null == channel )
					break;
				//Handle received data. 收到数据进行处理
				while(bufRead.hasRemaining()){
					if( null == msg ){
						int rem1 = bufRead.remaining();
						msg = server.createMessage(bufRead);
						int rem2 = bufRead.remaining();
						if( null == msg ){
							if( rem1>=13 && rem1 == rem2 ){
								//大于13字节，一定可以识别国网 浙规的报文。即使不能识别，也需要丢弃非法数据。
								//否则表示服务器配置的messageCreator不能创建对象。
								String info = "消息对象类型配置错误,server port="+server.getPort();
								log.fatal(info);
								throw new SocketClientCloseException(info);
							}
							else{
								if( bufRead.hasRemaining() )
									bufRead.compact();
								else
									bufRead.clear();
								break;
							}
						}
						msg.setSource(SyncSocketClient.this);
						msg.setServerAddress(server.getServerAddress());
					}
					boolean down = false;
					try{
						down = msg.read(bufRead);
					}catch(MessageParseException mpe){
						String expInfo = mpe.getLocalizedMessage();
						log.warn("Read Message Exception:"+expInfo,mpe);//log.warn("读消息异常："+expInfo,mpe);
						throw new SocketClientCloseException(mpe.getLocalizedMessage());
					}
					if( down ){		//消息已经完整读取。
						msg.setIoTime(System.currentTimeMillis());
						msg.setPeerAddr(getPeerAddr());
						msg.setTxfs(getServer().getTxfs());
						setLastReadTime();
						server.incRecvMessage();
						server.setLastReceiveTime(System.currentTimeMillis());
						if( msg.isHeartbeat() ){
							trace.trace("recevie heart-beat from"+getPeerAddr());
						}
						ReceiveMessageEvent ev = new ReceiveMessageEvent(msg,SyncSocketClient.this);
						msg = null;
						GlobalEventHandler.postEvent( ev );
					}
					else
						break;
				}
				if( bufRead.hasRemaining() )
					bufRead.compact();
				else
					bufRead.clear();
			}
		}
		
	}
	
	private class WriteThread extends Thread{
		public WriteThread(){
			super("SyncTcpClient.write."+peerAddr);
		}
		
		@Override
		public void run() {
			try{
				doSend();
			}catch(SocketClientCloseException e){
				close();
				return;
			}
			catch(Exception e){
				log.error("SyncClient doSend exp:"+e.getLocalizedMessage(),e);
			}
		}
		
		private void doSend() throws InterruptedException{
			while( true ){
				IMessage msg = null;
				while( sendList.size() == 0 && channel!=null){
					synchronized(messageSignal){
						messageSignal.wait(10);
					}
				}
				if(channel ==null) break;
				msg = sendList.remove(0);
				syncSend(msg);
			}
		}
	}
}
