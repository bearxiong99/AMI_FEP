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
	protected String peerIp; //�Է�IP��ַ
	protected int peerPort;  //�Է�port
	protected String peerAddr;
	protected String localIp;//����IP��ַ
	protected int localPort;
	protected String localAddr; //ip+":"+port;
	
	//IO֧������
	protected ByteBuffer bufRead,bufWrite;
	protected List<IMessage> sendList = Collections.synchronizedList(new LinkedList<IMessage>());
	protected ISocketServer server;
	private int intKey = 0;				// ���Է��� RTUA
	private int maxSendQueueSize = 1000;	//ͬ�����ͣ����ȴ����͵ı�������
	
	//״̬����
	private long lastIoTime = System.currentTimeMillis();
	private long lastReadTime = System.currentTimeMillis();

	//�ͻ���������������͵ı�������
	private int requestNum = -1;
	
	//ͬ������ 
	private final Object sendLock = new Object();
	private final Object messageSignal = new Object();
	//��д�߳�
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
				String s = "channel.write()�쳣��ԭ��"+exp.getLocalizedMessage();
				log.warn(s,exp);
				trace.trace(s);
				throw new SocketClientCloseException(exp);
			}
			if( 0 == bytesWritten ){
				trace.trace("send 0 bytes.");
				return false;		//socket buffer full�����ǻ�������û�з�����
			}
		}
		//������buf����ȫ��д��socket buffer
		bufWrite.clear();		//����������Ա��´�д
		return true;
	}
	
	public boolean syncSend(IMessage msg){
		if( null == msg ){
			log.error("syncSend: msg == null");
			return false;
		}
		synchronized(sendLock){
			boolean done = false ;
			//������ѭ����⹦��
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
					//֪ͨ������Ϣ�Ѿ��������ͳ�ȥ��
					setLastIoTime();
					server.incSendMessage();
					server.setLastSendTime(System.currentTimeMillis());
					if( msg.isHeartbeat() ){
						trace.trace("send heart-beat to"+getPeerAddr());
					}
					GlobalEventHandler.postEvent(new SendMessageEvent(msg,this));
				}
				
				if( ++deadloop > 1000 ){
					log.fatal("Message.write������ѭ������"+msg.getClass().getName());
					return true;			//return true����ʧ����Ϣ��������ϵͳ������
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
					log.info("Client Close["+peerIp+":"+peerPort+",localport:"+localPort+"]");//log.info("�ͻ��˹ر�["+peerIp+":"+peerPort+",localport:"+localPort+"]");
				}
				//δ���͵���Ϣ֪ͨ
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
				if( bytesRead <= 0 ){ //�Է������ر�
					String info = "client close by peer:"+peerAddr;
					log.info(info);
					trace.trace(info);
					throw new SocketClientCloseException(info);
				}
				if( null == channel )
					break;
				//Handle received data. �յ����ݽ��д���
				while(bufRead.hasRemaining()){
					if( null == msg ){
						int rem1 = bufRead.remaining();
						msg = server.createMessage(bufRead);
						int rem2 = bufRead.remaining();
						if( null == msg ){
							if( rem1>=13 && rem1 == rem2 ){
								//����13�ֽڣ�һ������ʶ����� ���ı��ġ���ʹ����ʶ��Ҳ��Ҫ�����Ƿ����ݡ�
								//�����ʾ���������õ�messageCreator���ܴ�������
								String info = "��Ϣ�����������ô���,server port="+server.getPort();
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
						log.warn("Read Message Exception:"+expInfo,mpe);//log.warn("����Ϣ�쳣��"+expInfo,mpe);
						throw new SocketClientCloseException(mpe.getLocalizedMessage());
					}
					if( down ){		//��Ϣ�Ѿ�������ȡ��
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
