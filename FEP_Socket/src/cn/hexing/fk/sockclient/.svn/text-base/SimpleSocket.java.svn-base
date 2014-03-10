/**
 * 对Socket进行简单封装，支持重连接功能。
 */
package cn.hexing.fk.sockclient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

/**
 *
 */
public class SimpleSocket {
	private static final Logger log = Logger.getLogger(SimpleSocket.class);
	//可配置属性
	private String hostIp;
	private int hostPort;
	private int ioTimeout = 20*1000;
	private int readBufferSize = 2048;

	//内部属性
	private Socket socket;
	private byte[] readBuffer = new byte[2048];
	private long lastConnectTime = 0;
	private long lastIoTime = 0;
	
	public SimpleSocket(String ip,int port){
		hostIp = ip;
		hostPort = port;
	}
	
	public SimpleSocket(){}
	
	public boolean connect(){
		socket = new Socket();
		lastConnectTime = System.currentTimeMillis();
		try{
			socket.connect(new InetSocketAddress(hostIp,hostPort));
			socket.setTcpNoDelay(true);
			socket.setSoTimeout(ioTimeout);
			socket.setReceiveBufferSize(this.readBufferSize);
			socket.setSendBufferSize(this.readBufferSize);
		}catch(IOException exp){
			log.warn("连接到UMS服务器失败：hostIp="+hostIp+",port="+hostPort+";原因："+exp.getLocalizedMessage());
			socket = null;
			return false;
		}
		finally{
			lastConnectTime = System.currentTimeMillis();
		}
		return true;
	}
	
	public void close(){
		if( null != socket ){
			try{
				socket.shutdownInput();
				socket.shutdownOutput();
				socket.close();
			}catch(IOException exp){
				log.warn("关闭socket异常，原因："+exp.getLocalizedMessage());
			}
			finally{
				socket = null;
			}
		}
	}

	public boolean reConnect(){
		close();
		return connect();
	}

	public boolean isAlive(){
		return null!=socket && socket.isConnected();
	}
	
	/**
	 * read data into buffer.
	 * @param buffer
	 * @param offset
	 * @param len
	 * @return :The number of bytes read, possibly zero, or -1 if the channel has reached end-of-stream 
	 */
	public int read(byte[] buffer,int offset,int len ){
		try{
			if( ! isAlive() )
				return -1;
			lastIoTime = System.currentTimeMillis();
			return socket.getInputStream().read(buffer, offset, len);
		}
		catch(SocketTimeoutException timeoutExp){
			return 0;
		}
		catch(IOException ioExp){
			close();
			log.warn("socket读数据异常:"+ioExp.getLocalizedMessage());
			return -1;
		}
		catch(Exception exp){
			log.warn("socket读数据异常:"+exp.getLocalizedMessage(),exp);
			return 0;
		}
	}
	
	public int read(byte[] buffer){
		return read(buffer,0,buffer.length);
	}
	
	public int read(ByteBuffer byteBuffer){
		byte[] buffer = new byte[byteBuffer.remaining()];
		int n = read(buffer);
		if( n<=0 )
			return n;
		byteBuffer.put(buffer, 0, n).flip();
		return n;
	}
	
	public int write(byte[] buffer, int offset,int len){
		try{
			if( !isAlive() )
				return -1;
			socket.getOutputStream().write(buffer, offset, len);
			socket.getOutputStream().flush();
		}
		catch(IOException ioExp){
			close();
			log.warn("socket发送数据异常:"+ioExp.getLocalizedMessage());
			return -1;
		}
		catch(Exception exp){
			close();
			log.warn("socket发送数据异常:"+exp.getLocalizedMessage(),exp);
			return 0;
		}
		return len;
	}
	
	public int write(byte[] buffer){
		return write(buffer,0,buffer.length);
	}
	
	public int write(ByteBuffer byteBuffer){
		byte[] buffer = new byte[byteBuffer.remaining()];
		byteBuffer.get(buffer);
		return write(buffer);
	}
	
	public int write(String message){
		try{
			return write(message.getBytes("GBK"));
		}catch(Exception e){
			log.warn("write(message.getBytes(\"GBK\")) exception",e);
			return -1;
		}
	}
	
	public String read(){
		int n = read(readBuffer);
		if( n<=0 )
			return null;
		try{
			return new String(readBuffer,0,n);
		}catch(Exception e){
			log.warn("read from socket exception.",e);
			return null;
		}
	}

	public long getLastConnectTime() {
		return lastConnectTime;
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

	public int getIoTimeout() {
		return ioTimeout;
	}

	public void setIoTimeout(int ioTimeout) {
		this.ioTimeout = ioTimeout;
	}

	public Socket getSocket() {
		return socket;
	}

	public void setReadBufferSize(int readBufferSize) {
		this.readBufferSize = readBufferSize;
	}

	public final long getLastIoTime() {
		return lastIoTime;
	}
	
}
