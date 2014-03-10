/**
 * 上行报文（GPRS、UMS等）发送到业务处理器BP。
 * 按照地市码区分，分配client。
 * 关系维护：
 * 		bpClient对象找到所有地市码（允许发送报文）
 * 		按照消息的地市码，找到bpClient；找不到则把地市码分配到bpClient
 */
package cn.hexing.fk.fe.msgqueue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.apache.log4j.Logger;

import cn.hexing.fk.common.spi.IProfile;
import cn.hexing.fk.common.spi.socket.IServerSideChannel;

/**
 *
 */
public class Msg2BpByMod implements IProfile{
	private static final Logger log = Logger.getLogger(Msg2BpByMod.class);
	private static final Msg2BpByMod instance = new Msg2BpByMod();
	private ArrayList<IServerSideChannel> clients = new ArrayList<IServerSideChannel>();
	private final Comparator<IServerSideChannel> bpComparator = new Comparator<IServerSideChannel>(){

		public int compare(IServerSideChannel bp1, IServerSideChannel bp2) {
			return bp1.getPeerIp().compareTo(bp2.getPeerIp());
		}
		
	}; 

	private Msg2BpByMod(){}
	
	public static final Msg2BpByMod getInstance(){
		return instance;
	}
	
	private boolean removeByChannel(IServerSideChannel bpChannel){
		boolean result = false;
		synchronized(clients){
			clients.remove(bpChannel);
		}
		return result;
	}
	
	public void clearTimeoutChannel(){
		synchronized(clients){
			for(int i=0; i<clients.size(); i++ ){
				IServerSideChannel client = clients.get(i);
				if( System.currentTimeMillis()-client.getLastIoTime() > 1000*60*2 ){
					clients.remove(i);
					log.warn("garbage BP client of 2BpByMod removed:"+client);
				}
			}
		}
	}

	public void onBpClientConnected(IServerSideChannel bpChannel){
		if( clients.contains(bpChannel) )
			return ;
		if( log.isInfoEnabled() )
			log.info("BP client connected:"+bpChannel);
		synchronized(clients){
			boolean handled = false;
			
			for(int i=0; i<clients.size(); i++){
				IServerSideChannel old = clients.get(i);
				if( old.getPeerIp().equalsIgnoreCase(bpChannel.getPeerIp())){
					clients.set(i, bpChannel);
					handled = true; 
					break;
				}
			}
			
			if( ! handled ){
				clients.add(bpChannel);
				Collections.sort(clients, bpComparator);
			}
		}
		if( log.isInfoEnabled() )
			log.info(profile());
	}
	
	public void onBpClientClosed(IServerSideChannel bpChannel){
		if( log.isInfoEnabled() )
			log.info("BP client closed:"+bpChannel);
		synchronized(clients){
			removeByChannel(bpChannel);
		}
		if( log.isInfoEnabled() )
			log.info(profile());
	}
	
	/**
	 * According to city code, get business processor client channel.
	 * @param a1
	 * @return
	 */
	public int getBpMode(int rtua){
		if( clients.size() <=1 )
			return -1;
		return Math.abs(rtua % clients.size());
	}
	public IServerSideChannel getBpChannel(int rtua){
		IServerSideChannel client = null;
		synchronized(clients){
			if( clients.size() == 1 )
				client = clients.get(0);
			else if( clients.size()> 1 ){
				int mod = Math.abs(rtua % clients.size());
				client = clients.get(mod);
			}
		}
		if( null != client && client.sendQueueSize() == 0 && client.getRequestNum() != 0 )
			return client;
		else
			return null;
	}
	
	public IServerSideChannel getIdleChannel(){
		synchronized(clients){
			for(IServerSideChannel c: clients){
				if(c.sendQueueSize() == 0 && c.getRequestNum() != 0 )
					return c;
			}
		}
		return null;
	}
	
	public IServerSideChannel getChannelByPeerIp(String peerIp){
		synchronized(clients){
			for(IServerSideChannel c: clients){
				if(c.getPeerIp().equals(peerIp) )
					return c;
			}
		}
		return null;
	}
	
	public String profile() {
		StringBuffer sb = new StringBuffer(256);
		sb.append("\r\n    <bp-mod>");
		for(int i=0; i<clients.size(); i++ ){
			sb.append("\r\n        <bp addr=\"").append(clients.get(i).getPeerAddr()).append("\" index=\"");
			sb.append(i).append("\" />");
		}
		sb.append("\r\n    </bp-mod>");
		return sb.toString();
	}
}
