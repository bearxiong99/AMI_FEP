package cn.hexing.fk.sockclient.async;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import cn.hexing.fk.common.spi.socket.ISocketServer;
import cn.hexing.fk.sockserver.AsyncSocketClient;
import cn.hexing.fk.utils.HexDump;

public class JAsyncSocket extends AsyncSocketClient {
	private static final Logger log = Logger.getLogger(JAsyncSocket.class);
	private long lastConnectTime = System.currentTimeMillis() - 1000*60*10;
	private Object attachment;
	
	public JAsyncSocket(ISocketServer s) {
		AsyncSocketPool sp = (AsyncSocketPool)s;
		server = s;
		peerIp = sp.getPeerIp();
		peerPort = sp.getPeerPort();
		peerAddr = peerIp + ":" + HexDump.toHex((short)peerPort)+":A";
		bufRead = ByteBuffer.allocateDirect(s.getBufLength());
		bufWrite = ByteBuffer.allocateDirect(s.getBufLength());
//		createChannel();
	}
	
	public Object attachment(){
		return attachment;
	}
	
	public void attach(Object attach){
		attachment = attach;
	}
	
	public void createChannel(){
		try{
			channel =  SocketChannel.open();
		}
		catch(IOException e){
			log.error(e.getLocalizedMessage(),e);
		}
	}

	public boolean isConnected(){
		synchronized(this){
			return null!=channel ? channel.isConnected() : false;
		}
	}

	public long getLastConnectTime() {
		return lastConnectTime;
	}

	public void setLastConnectTime(long lastConnectTime) {
		this.lastConnectTime = lastConnectTime;
	}
	
	public String toString(){
		return "async socket:peer="+peerIp+",localport="+localPort;
	}
}
