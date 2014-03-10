/**
 * 简单UDP服务器
 */
package cn.hexing.fk.sockserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import cn.hexing.fk.common.events.GlobalEventHandler;
import cn.hexing.fk.common.simpletimer.ITimerFunctor;
import cn.hexing.fk.common.simpletimer.TimerData;
import cn.hexing.fk.common.simpletimer.TimerScheduler;
import cn.hexing.fk.common.spi.IModule;
import cn.hexing.fk.common.spi.socket.IServerSideChannel;
import cn.hexing.fk.common.spi.socket.abstra.BaseSocketServer;

import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.sockserver.event.ModuleProfileEvent;
import cn.hexing.fk.utils.HexDump;
import cn.hexing.fk.utils.State;

/**
 *
 */
public class SyncUdpServer extends BaseSocketServer implements IModule,ITimerFunctor{
	private static final Logger log = Logger.getLogger(SyncUdpServer.class);

	//可配置参数
	private boolean oneIpLimit = false;	//是否限制同一个IP地址只能有一个连接。对网关程序有意义

	//对象状态属性
	private volatile State state = State.STOPPED; //服务器状态
	private DatagramChannel dgc;
	private Object channelLock = new Object();		//DatagramChannel同步锁
	private boolean channelReady = false;
	private UdpIoThread ioThread;
	private ByteBuffer readBuffer = null, writeBuffer=null;
	
	//辅助属性
	private final HashMap<String,UdpClient> clients = new HashMap<String,UdpClient>(5120); 

	public SyncUdpServer(){
	}

	public String getModuleType() {
		return IModule.MODULE_TYPE_SOCKET_SERVER;
	}

	public boolean isActive() {
		return state.isActive();
	}

	/**
	 * UDP方式同步发送message
	 * @param msg
	 * @param sa
	 * @return
	 */
	public boolean send(IMessage msg,UdpClient client){
		synchronized(channelLock){
			try{
				SocketAddress sa = client.getSocketAddress();
				writeBuffer.clear();
				msg.write(writeBuffer);
				writeBuffer.flip();
				this.dgc.send(writeBuffer, sa);
				client.setLastIoTime();
				return true;
			}catch(Exception e){
				log.error("UDP["+port+"]发送报文异常:"+e.getLocalizedMessage(),e);
			}
		}
		return false;
	}
	
	public boolean start() {
		if ( state.isActive() )
			return false;
		state = State.STARTING;
		readBuffer = ByteBuffer.allocateDirect(bufLength);
		writeBuffer = ByteBuffer.allocateDirect(bufLength);
		
		//启动定时服务
		TimerScheduler.getScheduler().addTimer(new TimerData(this,0,60));	//定时器0，每分钟定时器
		TimerScheduler.getScheduler().addTimer(new TimerData(this,1,timeout));	//定时器1，clientTimeoutTask

		//在UdpIoThread中打开通道: openChannel();
		try{
			ioThread = new UdpIoThread();
			ioThread.start();
			while( state != State.RUNNING )
			{
				Thread.yield();
				Thread.sleep(10);
			}
		} catch (Exception exp) {
			log.fatal(exp.getLocalizedMessage());
			return false;
		}
		if( log.isInfoEnabled() )
			log.info("UDP服务【"+port+ "】已经启动!");
		return true;
	}
	
	public void onTimer(int timerID ){
		if( 0 == timerID ){
			long now = System.currentTimeMillis();
			if( now-lastReceiveTime < 60*1000 || now-lastSendTime < 60*1000 )
				GlobalEventHandler.postEvent(new ModuleProfileEvent(SyncUdpServer.this));
			msgRecvPerMinute = 0;	msgSendPerMinute = 0;
		}
		else if( 1== timerID ){
			ArrayList<UdpClient>list = new ArrayList<UdpClient>(clients.values());
			long now = System.currentTimeMillis();
			for(UdpClient client: list){
				if( now-client.getLastReadTime() > timeout*1000 ){
					synchronized(clients){
						String clientKey = client.getPeerIp()+":"+client.getPeerPort();
						if( oneIpLimit )
							clientKey = client.getPeerIp();
						clients.remove(clientKey);
					}
				}
			}
		}
	}
	
	private boolean openChannel(){
		synchronized(channelLock){
			try {
				dgc = DatagramChannel.open();
				dgc.socket().setReceiveBufferSize(bufLength);
				dgc.socket().setSendBufferSize(bufLength);
				dgc.socket().setReuseAddress(true);
				dgc.configureBlocking(true);
				InetSocketAddress addr = null;
				if( null == ip )
					addr = new InetSocketAddress(port);
				else
					addr = new InetSocketAddress(ip,port);
				dgc.socket().bind(addr);
			} catch (Exception exp) {
				log.fatal("UDP服务器启动失败["+port+"]",exp);
				channelReady = false;
			}
			channelReady = true;
			return channelReady;
		}
	}
	
	private void closeChannel(){
		synchronized(channelLock){
			try {
				if(null != dgc )
					dgc.close();
			} catch (IOException e) {
				log.warn(e.getMessage());
			} finally {
				dgc = null;
			}
			channelReady = false;
		}
	}
	
	public void stop() {
		state = State.STOPPING; // stopping
		if( null != ioThread ){
			ioThread.interrupt();
			try {
				if( ioThread.isAlive() )
				ioThread.join(200);
			} catch (InterruptedException e) {	}
			ioThread = null;
		}
		
		readBuffer = null;	writeBuffer = null;

		TimerScheduler.getScheduler().removeTimer(this, 0);
		TimerScheduler.getScheduler().removeTimer(this, 1);

		if( log.isInfoEnabled() )
			log.info("UDP服务器[" + port + "] 停止运行！");
		state = State.STOPPED;
	}

	private class UdpIoThread extends Thread {
		public UdpIoThread() {
			super("UDP["+port+"]-IO-Thread");
		}

		public void run() {
			if( log.isDebugEnabled() )
				log.debug(this.getName()+"运行...");
			state = cn.hexing.fk.utils.State.RUNNING;
			while ( state != cn.hexing.fk.utils.State.STOPPING) //if state is not stopping
			{
				//检测通道是否正常
				if( !channelReady ){
					if( !openChannel() ){
						//打开通道失败
						closeChannel();
						try{
						Thread.sleep(60*1000);
						}catch(Exception exp){}
						continue;
					}
				}
				try{
					drainChannel();
				}
				catch( ClosedByInterruptException exp){
					log.error("UDP服务器["+port+"]接收数据异常:"+exp.getLocalizedMessage(),exp);
					Thread.interrupted();
					closeChannel();
					continue;
				}
				catch( AsynchronousCloseException exp ){
					log.error("UDP服务器["+port+"]接收数据异常:"+exp.getLocalizedMessage(),exp);
					Thread.interrupted();
					closeChannel();
					continue;
				}
				catch(ClosedChannelException exp){
					log.error("UDP服务器["+port+"]接收数据异常:"+exp.getLocalizedMessage(),exp);
					Thread.interrupted();
					closeChannel();
					continue;
				}catch(Exception exp){
					log.error("UDP服务器["+port+"]接收数据异常:"+exp.getLocalizedMessage(),exp);
				}
			}
			closeChannel();
			state = cn.hexing.fk.utils.State.STOPPED;
		}
		
		void drainChannel() throws Exception {
			SocketAddress sa;
			readBuffer.clear();
			sa = dgc.receive(readBuffer);
			if( null == sa ){
				log.error("UDP服务器接收数据异常，dgc.receive(readBuffer)返回NULL");
				return;
			}
			if (readBuffer.position() == 0)
				return;
			
			//把缓冲区中信息写到某个（些）消息中
			readBuffer.flip();
			
			/**
			 * 对于电力终端，每客户端只能有一个UDP连接到服务器。
			 * 设计上，必须考虑每个客户端机器，多个UDP对象发送数据的情况。
			 */
			String peerip = ((InetSocketAddress)sa).getAddress().getHostAddress();
			int port = ((InetSocketAddress)sa).getPort();
			String key = peerip;
			if( !oneIpLimit )
				key += ":"+port;
			UdpClient client = clients.get(key);
			if( null == client ){
				client = new UdpClient(sa,SyncUdpServer.this);
				synchronized(clients){
					clients.put(key, client);
				}
			}
			if(log.isDebugEnabled()){
				log.debug("UDP-"+port+"-收到["+sa.toString()+"]上行报文:"+HexDump.hexDump(readBuffer));
			}
			//把全局缓冲区数据转移到client对象缓冲区。
			if( client.getBufRead().remaining()< readBuffer.remaining() )
				client.getBufRead().clear();
			client.getBufRead().put(readBuffer);
			client.getBufRead().flip();
			client.setLastReadTime();
			client.getServer().setLastReceiveTime(System.currentTimeMillis());
			ioHandler.onReceive(client);
		}
	}	// end of UdpIoThread

	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("S-UDP[").append(port).append("]");
		return sb.toString();
	}

	public boolean isOneIpLimit() {
		return oneIpLimit;
	}

	public void setOneIpLimit(boolean oneIpLimit) {
		this.oneIpLimit = oneIpLimit;
	}

	public int getClientSize(){
		synchronized(clients){
			return clients.size();
		}
	}
	
	/**
	 * 返回本UDP服务器的所有客户端对象
	 */
	public IServerSideChannel[] getClients(){
		synchronized(clients){
			return clients.values().toArray(new IServerSideChannel[clients.size()]);
		}
	}

	public void removeClient(IServerSideChannel client) {
		synchronized(clients){
			String clientKey = client.getPeerIp()+":"+client.getPeerPort();
			if( oneIpLimit )
				clientKey = client.getPeerIp();
			clients.remove(clientKey);
		}
		super.removeClient(client);
	}
}
