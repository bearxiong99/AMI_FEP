/**
 * UDP通信client 类。UDP服务器一般采用同步写数据。
 */
package cn.hexing.fk.sockserver;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import cn.hexing.fk.common.spi.socket.IServerSideChannel;
import cn.hexing.fk.message.IMessage;

/**
 */
public class UdpClient implements IServerSideChannel{
	private SocketAddress socketAddress;
	private SyncUdpServer server;
	private String peerIp = "0.0.0.0";
	private int peerPort = 0;
	//IO支撑属性
	private ByteBuffer bufRead = null;		//支持负控规约即可
	private IMessage curReadingMsg;		//当前正在读写的消息。
	//状态属性
	private long lastIoTime = System.currentTimeMillis();
	private long lastReadTime = System.currentTimeMillis();
	private boolean bufferHasRemaining = false;
	//发送优先控制属性。记录发送事件触发次数。
	private int lastingWrite = 0;

	//客户端请求服务器发送的报文数量
	private int requestNum = -1;

	public int getRequestNum() {
		return requestNum;
	}

	public void setRequestNum(int requestNum) {
		this.requestNum = requestNum;
	}

	public int getLastingWrite() {
		return lastingWrite;
	}

	public void setLastingWrite(int lastingWrite) {
		this.lastingWrite = lastingWrite;
	}

	public UdpClient(SocketAddress sa,SyncUdpServer udpServer){
		socketAddress = sa;
		server = udpServer;
		bufRead = ByteBuffer.allocate(server.getBufLength());
		if( sa instanceof InetSocketAddress){
			peerIp = ((InetSocketAddress)sa).getAddress().getHostAddress();
			peerPort = ((InetSocketAddress)sa).getPort();
		}else{
			String connstr = sa.toString();
			if(connstr!=null){
				if(connstr.charAt(0)=='/'){
					connstr=connstr.substring(1);
				}
				String[] parts = connstr.split(":");
				if( parts.length>=2 ){
					peerIp = parts[0];
					peerPort = Integer.parseInt(parts[1]);
				}
			}
		}
	}

	public boolean send(IMessage msg) {
		//udp服务端统计发送下行报文信息
		server.incSendMessage();
		server.setLastSendTime(System.currentTimeMillis());
		return server.send(msg, this);
	}
	
	public int sendQueueSize(){
		return 0;
	}
	
	public void setMaxSendQueueSize(int maxSize){
		
	}
	
	public IMessage getCurReadingMsg() {
		return curReadingMsg;
	}

	public void setCurReadingMsg(IMessage curReadingMsg) {
		this.curReadingMsg = curReadingMsg;
		if( null != curReadingMsg )
			server.incRecvMessage();			//接收到每条报文，都会调用这个函数。
	}

	public SyncUdpServer getServer() {
		return server;
	}

	public String getPeerIp() {
		return peerIp;
	}

	public int getPeerPort() {
		return peerPort;
	}
	
	public String getPeerAddr(){
		return peerIp+ ":" + peerPort + ":U";	//表示UDP地址
	}

	public ByteBuffer getBufRead() {
		return bufRead;
	}

	public SocketChannel getChannel(){
		return null;
	}
	public void setIoThread(Object ioThread) {
	}
	
	public SocketAddress getSocketAddress() {
		return socketAddress;
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
	
	//UDP client can not be closed
	public void close() {}

	public ByteBuffer getBufWrite() {
		return null;
	}

	public IMessage getCurWritingMsg() {
		return null;
	}

	public IMessage getNewSendMessage() {
		return null;
	}

	public void setCurWritingMsg(IMessage curWritingMsg) {
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("UDP client,peer=").append(peerIp);
		sb.append(":").append(peerPort);
		return sb.toString();
	}
	
	public boolean bufferHasRemaining(){
		return bufferHasRemaining;
	}
	
	public void setBufferHasRemaining(boolean hasRemaining){
		bufferHasRemaining = hasRemaining;
	}
}
