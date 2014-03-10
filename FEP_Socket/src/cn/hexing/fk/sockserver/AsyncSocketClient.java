/**
 * SocketServer所管理的异步SocketClient.
 * 读写消息(IMessage）对象的方式：
 *    调用IClientIO接口实现（如果Server没有实现类，则调用SimpleIoHandler类）。
 *    读过程：channel-> bufRead -> IMessage.read(bufRead);
 *    写过程: IMessage->client发送队列->通知ioThread； ioThread回调IClientIO接口。
 */
package cn.hexing.fk.sockserver;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import cn.hexing.fk.common.events.GlobalEventHandler;
import cn.hexing.fk.common.spi.socket.IServerSideChannel;
import cn.hexing.fk.common.spi.socket.ISocketServer;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.sockserver.event.MessageSendFailEvent;
import cn.hexing.fk.sockserver.io.SocketIoThread;
import cn.hexing.fk.utils.HexDump;

/**
 */
public class AsyncSocketClient implements IServerSideChannel,Comparable<AsyncSocketClient>{
	//专有属性
	protected SocketChannel channel;
	protected String peerIp; //对方IP地址
	protected int peerPort;  //对方port
	protected String peerAddr;
	protected String localIp;//本地IP地址
	protected int localPort;
	protected String localAddr; //ip+":"+port;
	
	//IO支撑属性
	protected ByteBuffer bufRead,bufWrite;
	protected IMessage curReadingMsg,curWritingMsg;		//当前正在读写的消息。
	protected List<IMessage> sendList = new LinkedList<IMessage>();
	
	//发送优先控制属性。记录发送事件触发次数。
	private int lastingWrite = 0;
	
	//辅助属性
	private static final Logger log = Logger.getLogger(AsyncSocketClient.class);
	protected ISocketServer server;
	protected SocketIoThread ioThread;
	private int intKey = 0;				// 可以放置 RTUA
	private int maxSendQueueSize = 1000;	//异步发送，最大等待发送的报文数量

	//状态属性
	private long lastIoTime = System.currentTimeMillis();
	private long lastReadTime = System.currentTimeMillis();
	private boolean bufferHasRemaining = false;
	
	//客户端请求服务器发送的报文数量
	private int requestNum = -1;

	public AsyncSocketClient(){}
	
	public AsyncSocketClient(SocketChannel c,ISocketServer s){
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

	public boolean send(IMessage msg){
		if( sendList.size()>= this.maxSendQueueSize ){
			log.warn(toString()+"-Send Queue Size>maxSendQueueSize,This Message Be Discarded");//log.warn(toString()+"-发送队列长度>maxSendQueueSize，本消息被丢弃");
			//通知消息丢弃事件。
			GlobalEventHandler.postEvent(new MessageSendFailEvent(msg,this));
			return false;
		}
		synchronized(sendList){
			if( this.requestNum>0 ){
				synchronized(this){
					this.requestNum--;
				}
			}
			sendList.add(msg);
		}
		ioThread.clientWriteRequest(this);
		return true;
	}
	
	public int sendQueueSize(){
		synchronized(sendList){
			return sendList.size();
		}
	}
	
	public void setMaxSendQueueSize(int maxSendQueueSize){
		this.maxSendQueueSize = maxSendQueueSize;
	}
	
	public IMessage getNewSendMessage(){
		synchronized(sendList){
			if( sendList.size() == 0 )
				return null;
			return sendList.remove(0);
		}
	}

	/**
	 * 关闭socketChannel 对象
	 */
	public void close(){
		try {
			channel.socket().shutdownInput();
			channel.socket().shutdownOutput();
		} catch (Exception exp) {
		}
		try {
			channel.close();
			channel = null;
			
		} catch (Exception exp) {
		}
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
	
	public SocketChannel getChannel() {
		return channel;
	}
	
	public SocketAddress getSocketAddress(){
		return channel.socket().getRemoteSocketAddress();
	}
	
	public void setChannel(SocketChannel channel) {
		this.channel = channel;
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

	public String getLocalIp() {
		return localIp;
	}

	public void setLocalIp(String localIp) {
		this.localIp = localIp;
	}

	public String getLocalAddr() {
		return localAddr;
	}

	public void setLocalAddr(String localAddr) {
		this.localAddr = localAddr;
	}

	public final SocketIoThread getIoThread() {
		return ioThread;
	}

	public void setIoThread(Object ioThread) {
		this.ioThread = (SocketIoThread)ioThread;
	}

	public ISocketServer getServer() {
		return server;
	}

	public void setServer(TcpSocketServer server) {
		this.server = server;
	}
	
	public void closeRequest(){
		ioThread.closeClientRequest(this);
	}

	public IMessage getCurReadingMsg() {
		return curReadingMsg;
	}

	public void setCurReadingMsg(IMessage curReadingMsg) {
		this.curReadingMsg = curReadingMsg;
		if( null != curReadingMsg )
			server.incRecvMessage();			//接收到每条报文，都会调用这个函数。
	}

	public IMessage getCurWritingMsg() {
		return curWritingMsg;
	}

	public void setCurWritingMsg(IMessage curWritingMsg) {
		this.curWritingMsg = curWritingMsg;
		if( null != curWritingMsg )
			server.incSendMessage();
	}

	public ByteBuffer getBufRead() {
		return bufRead;
	}

	public ByteBuffer getBufWrite() {
		return bufWrite;
	}
	
	public String toString(){
		return this.peerAddr;
	}

	public int getIntKey() {
		return intKey;
	}

	public void setIntKey(int intKey) {
		this.intKey = intKey;
	}

	public final String getPeerAddr() {
		return peerAddr;
	}
	
	public long getLastIoTime(){
		return lastIoTime;
	}
	
	public long getLastReadTime(){
		return lastReadTime;
	}
	
	public void setLastIoTime(){
		lastIoTime = System.currentTimeMillis();
	}
	
	public void setLastReadTime(){
		lastReadTime = System.currentTimeMillis();
		lastIoTime = lastReadTime;
	}
	
	public int getLastingWrite() {
		return lastingWrite;
	}

	public void setLastingWrite(int lastingWrite) {
		this.lastingWrite = lastingWrite;
	}

	public int getLocalPort() {
		return localPort;
	}

	public void setLocalPort(int localPort) {
		this.localPort = localPort;
	}

	public int getRequestNum() {
		return requestNum;
	}

	public void setRequestNum(int requestNum) {
		synchronized(this){
			this.requestNum = requestNum;
		}
	}
	
	public boolean bufferHasRemaining(){
		return bufferHasRemaining;
	}
	
	public void setBufferHasRemaining(boolean hasRemaining){
		bufferHasRemaining = hasRemaining;
	}

	@Override
	public int compareTo(AsyncSocketClient o) {
		return this.hashCode()-o.hashCode();
	}
}
