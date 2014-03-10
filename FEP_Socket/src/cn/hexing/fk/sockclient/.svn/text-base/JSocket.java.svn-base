/**
 * JAVA阻塞式SOCKET客户端对象。
 */
package cn.hexing.fk.sockclient;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

import cn.hexing.fk.common.spi.socket.abstra.BaseClientChannel;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.IMessageCreator;
import cn.hexing.fk.message.gate.MessageGateCreator;
import cn.hexing.fk.tracelog.TraceLog;
import cn.hexing.fk.utils.HexDump;
import cn.hexing.fk.utils.State;

/**
 */
public class JSocket extends BaseClientChannel {
	private static final Logger log = Logger.getLogger(JSocket.class);
	private static final TraceLog trace = TraceLog.getTracer(JSocket.class);
	private static int THREAD_SEQ = 1;
	//可配置属性
	private String hostIp="127.0.0.1";
	private int hostPort = 10001;
	private int bufLength = 256;		//默认缓冲区长度
	private IMessageCreator messageCreator = new MessageGateCreator();
	private int timeout = 10;			//读或者写超时，单位秒
	private JSocketListener listener = new DumyJSocketListener();
	private String txfs;

	//对象内部属性
	private volatile Socket socket;
	private final Object sendLock = new Object();		//并发写对象锁
	private ByteBuffer sendBuffer,readBuffer;
	private volatile State _state = State.STOPPED;

	//辅助属性
	private ReadThread reader;
	private long lastReadTime = System.currentTimeMillis();	//长时间未接收消息检测机制。
	private int connectWaitTime = 2;	//连接不上时，等待时间(秒)。按照2倍规则递增。最长5分钟重连间隔。
	private long lastConnectTime = System.currentTimeMillis()-1000*10;
	private String peerAddr;
	
	/**当连接不成功的时候是否自动重连,默认是重连*/
	private boolean isAutoReconnect=true;
	public void init(){
		if( State.STOPPED != _state )
			return;
		_state = State.STARTING;
		readBuffer = ByteBuffer.allocate(bufLength);
		sendBuffer = ByteBuffer.allocate(bufLength);
		peerAddr = hostIp + ":" + hostPort;
		reader = new ReadThread();
		reader.start();
	}
	
	public void close(){
		_state = State.STOPPING;
		reader.interrupt();
		int cnt = 100;
		while( State.STOPPED != _state && cnt-->0 ){
			Thread.yield();
			try{
				Thread.sleep(10);
			}catch(Exception e){}
		}
	}
	
	public void reConnect(){
		try{
			if( trace.isEnabled() )
				trace.trace("do reConnect. "+ getPeerAddr());
			_closeSocket();
		}catch(Exception e){
			log.warn("reConnect exception:"+e.getLocalizedMessage(),e);
		}
	}
	
	public boolean isActive(){
		return _state.isRunning();
	}
	
	public boolean isConnected(){
		return isActive() && null!= socket && socket.isConnected();
	}
	
	private boolean _connect(){
		try{
			trace.trace("client socket is connecting to server :"+this.getPeerAddr());
			lastConnectTime = System.currentTimeMillis();
			socket = new Socket();
			InetSocketAddress ar = new InetSocketAddress(hostIp,hostPort);
			socket.setSoTimeout(timeout*1000);
			socket.connect(ar,timeout*1000);
			connectWaitTime = 2;
		}catch(ConnectException e){
			log.error("Can't connect to:"+hostIp+"@"+hostPort+",reason="+e.getLocalizedMessage());
			socket = null;
			return false;
		}
		catch(Exception e){
			log.error("Can't connect to:"+hostIp+"@"+hostPort);
			if( null != socket ){
				try{
					socket.close();
				}catch(Exception e1){}
			}
			socket = null;
			return false;
		}
		log.info("client socket connect to server successfully:"+this.getPeerAddr());
		return true;
	}

	private void _closeSocket(){
		synchronized(sendLock){
			long interval = System.currentTimeMillis() - lastConnectTime;
			if( interval < connectWaitTime * 1000 ){
				trace.trace("can not close socket within "+connectWaitTime+" sec");
				return;
			}
			try{
				try{
					socket.shutdownInput();
					socket.shutdownOutput();
				}catch(Exception e){}
				socket.close();
			}catch(Exception e){}
			finally{
				lastConnectTime = System.currentTimeMillis();
				connectWaitTime = 1;
				socket = null;
				listener.onClose(this);
				readBuffer.clear();
				sendBuffer.clear();
				if( State.STOPPING == _state || State.STOPPED == _state ){
					reader = null;
					return;
				}			
				_state = State.STARTING;
				
			}
		}
	}
	
	public boolean send(byte[] output){
		if( null == output || !isConnected() )
			return false;
		synchronized(sendLock){
			try{
				socket.getOutputStream().write(output,0,output.length);
				socket.getOutputStream().flush();
			}catch(Exception e){
				String info = "client closed in 'send' reason is"+e.getLocalizedMessage()+", peer="+getPeerAddr();
				log.error(info,e);
				trace.trace(info, e);
				_closeSocket();
				return false;
			}
		}
		return true;
	}

	public boolean sendMessage(IMessage msg){
		boolean ret = false;
		synchronized(sendLock){
			ret = _send(msg);
		}
		if( ret ){
			msg.setSource(JSocket.this);
			msg.setIoTime(System.currentTimeMillis());
			msg.setTxfs(txfs);
			listener.onSend(this,msg);
		}
		return ret;
	}
	
	private boolean _send(IMessage msg){
		if( null == socket )
			return false;
		boolean done = false;
		sendBuffer.clear();
		int cnt = 100;
		while( !done && --cnt>0 ){
			done = msg.write(sendBuffer);
			sendBuffer.flip();
			byte[] out = sendBuffer.array();
			int len = sendBuffer.remaining();
			int off = sendBuffer.position();
			try{
				socket.getOutputStream().write(out, off, len);
				socket.getOutputStream().flush();
			}catch(Exception e){
				String info = "client closed in '_send' reason is"+e.getLocalizedMessage()+", peer="+getPeerAddr();
				log.error(info,e);
				trace.trace(info, e);
				_closeSocket();
				return false;
			}
		}
		return true;
	}

	class ReadThread extends Thread{
		IMessage message = null;
		
		public ReadThread(){
			super("sock-"+hostIp+"@"+hostPort+":"+(THREAD_SEQ++));
		}
		
		public void run(){
			while(cn.hexing.fk.utils.State.STOPPING != _state && 
					cn.hexing.fk.utils.State.STOPPED != _state){
				try{
					while( null == socket ){
						if( System.currentTimeMillis()-lastConnectTime < connectWaitTime*1000 ){
							try{
								Thread.sleep(1000);
							}catch(InterruptedException e){
								trace.trace("thread sleep interrupted");
								break;
							}
							continue;
						}
						if( _connect() ){
							if( trace.isEnabled() )
								trace.trace("connect to server ok. "+getPeerAddr() );
							_state = cn.hexing.fk.utils.State.RUNNING;
		//						synchronized(listener)
							{
								listener.onConnected(JSocket.this);
							}
						}
						else{
							if(isAutoReconnect){
								connectWaitTime *= 2;
								if( connectWaitTime> 5*60 )
									connectWaitTime = 300;
								trace.trace("client socket cannot connected to server :"+hostIp+"@"+hostPort);
							}else{
								trace.trace("client can't auto reconnect.retrun");
								return;
							}
						}
					}
					//通讯连接成功
					int ret = _doReceive();
					if( ret<0 ){
						//对方Socket连接异常。
						if( trace.isEnabled() )
							trace.trace("client closed by _doReceive return="+ret+",peer="+getPeerAddr() );
						_closeSocket();
					}
				}catch(Exception ex){
					trace.trace("client error,peer="+getPeerAddr()+",err:"+ex.getLocalizedMessage());
					_closeSocket();
				}
			}
			try{
				if( null != socket )
					_closeSocket();
			}catch(Exception e){}
			_state = cn.hexing.fk.utils.State.STOPPED;
			trace.trace("client socket stoped :"+hostIp+"@"+hostPort);
		}
		
		private int _doReceive(){
			try{
				int len = readBuffer.remaining();
				if( len == 0 ){
					readBuffer.position(0);
					log.warn("缓冲区满，不能接收数据。dump="+HexDump.hexDumpCompact(readBuffer));
					readBuffer.clear();
					len = readBuffer.remaining();
				}
				byte[] in = readBuffer.array();
				int off = readBuffer.position();
				int n = socket.getInputStream().read(in,off,len);
				if( n<=0 )
					return -1;
				lastReadTime = System.currentTimeMillis();
				readBuffer.position(off+n);
				readBuffer.flip();
			}
			catch(SocketTimeoutException te){
				return 0;
			}
			catch(IOException ioe){
				log.warn("client IO exception,"+ioe.getLocalizedMessage()+",peer="+getPeerAddr() );
				return -2;
			}
			catch(Exception e){
				log.warn("client socket["+getPeerAddr()+"] _doReceive:"+e.getLocalizedMessage(),e);
				return -3;
			}
			try{
				_handleBuffer();
			}
			catch(Exception e){
				log.error(e.getLocalizedMessage(),e);
				return 0;	//处理异常，非通信异常。
			}
			return 0;	//成功
		}
		
		private void _handleBuffer(){
			while( readBuffer.hasRemaining() ){
				if( null == message ){
					message = messageCreator.create();
				}
				boolean down = false;
				try{
					down = message.read(readBuffer);
				}catch(Exception e){
					log.warn("消息对象读取数据异常:"+e.getLocalizedMessage(),e);
					message = null;
					break;
				}
				if( down ){		//消息已经完整读取。
					try{
						message.setSource(JSocket.this);
						message.setIoTime(System.currentTimeMillis());
						message.setTxfs(txfs);
						//终端上行的MessageZj对象的peerAddr，保存的是终端本身的IP。
						//下面的判断是正确的，别修改为错误代码。
						if( null==message.getPeerAddr() || 0==message.getPeerAddr().length() ){
							message.setPeerAddr(peerAddr);
						}
						listener.onReceive(JSocket.this,message);
					}catch(Exception e){
						log.error(e.getLocalizedMessage(),e);
					}
					message = null;
				}
				else
					break;
			}
			if( readBuffer.hasRemaining() )
				readBuffer.compact();
			else
				readBuffer.clear();
		}
	}

	public IMessageCreator getMessageCreator() {
		return messageCreator;
	}

	public void setMessageCreator(IMessageCreator messageCreator) {
		this.messageCreator = messageCreator;
	}

	public String getHostIp() {
		return hostIp;
	}

	public void setHostIp(String hostIp) {
		this.hostIp = hostIp;
	}

	public int getHostPort() {
		return hostPort;
	}

	public void setHostPort(int hostPort) {
		this.hostPort = hostPort;
	}

	public int getBufLength() {
		return bufLength;
	}

	public void setBufLength(int bufLength) {
		this.bufLength = bufLength;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public JSocketListener getListener() {
		return listener;
	}

	public void setListener(JSocketListener listener) {
		this.listener = listener;
	}

	public long getLastReadTime() {
		return lastReadTime;
	}

	public String getTxfs() {
		return txfs;
	}

	public void setTxfs(String txfs) {
		this.txfs = txfs;
	}

	public String getPeerAddr() {
		if( null != this.peerAddr )
			return this.peerAddr;
		else
			return this.hostIp + ":" + this.hostPort;
	}

	public String getPeerIp() {
		return hostIp;
	}

	public int getPeerPort() {
		return hostPort;
	}

	public SocketAddress getSocketAddress() {
		return new InetSocketAddress(hostIp,hostPort);
	}

	public boolean send(IMessage msg) {
		return sendMessage(msg);
	}

	public long getLastIoTime() {
		return this.lastReadTime;
	}

	public final boolean isAutoReconnect() {
		return isAutoReconnect;
	}

	public final void setAutoReconnect(boolean isAutoReconnect) {
		this.isAutoReconnect = isAutoReconnect;
	}
}
