/**
 * 业务处理器的上行报文队列。
 * 业务处理器下行，直接调用sendMessage方法。
 * 
 * 上行报文：
 * 报文队列的生产者（插入）：FEMessageEventHandle
 * 报文队列的消费者（取走）：????
 * 
 */
package cn.hexing.fk.bp.msgqueue;

import java.util.HashMap;

import javax.sql.DataSource;

import cn.hexing.db.DbMonitor;
import cn.hexing.db.DbState;
import cn.hexing.fk.FasSystem;
import cn.hexing.fk.clientmod.ClusterClientModule;
import cn.hexing.fk.common.queue.CacheQueue;
import cn.hexing.fk.common.spi.IMessageQueue;
import cn.hexing.fk.common.spi.IProfile;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.MessageConst;
import cn.hexing.fk.message.gate.MessageGate;
import cn.hexing.fk.message.gw.MessageGw;
import cn.hexing.fk.message.zj.MessageZj;

/**
 */
public class BPMessageQueue implements IMessageQueue, IProfile{
	private CacheQueue cacheQueue;	//spring 配置实现。
	private DataSource dataSource;						//用于DbMonitor
	private ClusterClientModule client;
	private long lastReceiveTime = System.currentTimeMillis();
	private long lastHandleDataTime = System.currentTimeMillis();
	private HashMap<String,String> mapDlmsRelayOfDC = new HashMap<String,String>();

	private Runnable shutdownHook = new Runnable(){
		public void run(){
			BPMessageQueue.this.dispose();
		}
	};

	public void setCacheQueue( CacheQueue queue ){
		cacheQueue = queue;
		FasSystem.getFasSystem().addShutdownHook(shutdownHook);
	}
	
	public boolean isDlmsRelay(String logicalAddr){
		return mapDlmsRelayOfDC.containsKey(logicalAddr);
	}
	
	//消息队列统一管理终端下行
	public boolean sendMessage(IMessage msg){
		//make sure GuoWang relay message must be handled specially.
		if( msg instanceof MessageGate ){
			IMessage imsg = ((MessageGate) msg).getInnerMessage();
			if( null != imsg && imsg instanceof MessageGw ){
				MessageGw gwmsg = (MessageGw)imsg;
				if( gwmsg.getAFN() == MessageConst.GW_FUNC_RELAY_READ &&  MessageConst.DLMS_RELAY_FLAG.equals(gwmsg.getStatus())){
					//Only deal with DLMS relay message.
					if( mapDlmsRelayOfDC.get(gwmsg.getLogicalAddress()) == null ){
						synchronized(mapDlmsRelayOfDC){
							mapDlmsRelayOfDC.put(gwmsg.getLogicalAddress(), MessageConst.DLMS_RELAY_FLAG);
						}
					}
				}else{
					msg=imsg;
				}
			}//||imsg instanceof AnsiMessage
			if(null!=imsg && (imsg instanceof MessageZj)){
				msg=imsg;
			}
		}
		return client.sendMessage(msg);
	}
	
	public boolean getDbIsAvailable(){
		DbState ds = DbMonitor.getInstance().getMonitor(dataSource);
		if (null != ds && ds.isAvailable())
			return true;
		else
			return false;
	}

	//下面定义消息队列的方法
	public IMessage take(){
		DbState ds = DbMonitor.getInstance().getMonitor(dataSource);
		if( null != ds && ds.isAvailable() )
			return cacheQueue.take();
		else
			return null;
	}
	
	public IMessage poll(){
		return cacheQueue.poll();
	}
	
	/**
	 * 当业务处理器收到通信前置机上行报文时，调用此函数，把上行消息放入队列。
	 * 业务处理器需要从这个队列中取数据进行处理。采用多线程方式进行处理。
	 * @param msg
	 */
	public void offer(IMessage msg){
			cacheQueue.offer(msg);
	}
	
	public int size(){
		return cacheQueue.size();
	}

	

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public String profile() {
		StringBuffer sb = new StringBuffer(256);
		sb.append("\r\n    <message-queue type=\"bp\">");
		sb.append("\r\n        <size>").append(size()).append("</size>");
		sb.append("\r\n    </message-queue>");
		return sb.toString();
	}
	
	public void dispose(){
		cacheQueue.dispose();
	}

	public ClusterClientModule getClient() {
		return client;
	}

	public void setClient(ClusterClientModule client) {
		this.client = client;
	}

	public long getLastReceiveTime() {
		return lastReceiveTime;
	}

	public void setLastReceiveTime(long lastReceiveTime) {
		this.lastReceiveTime = lastReceiveTime;
	}

	public long getLastHandleDataTime() {
		return lastHandleDataTime;
	}

	public void setLastHandleDataTime(long lastHandleDataTime) {
		this.lastHandleDataTime = lastHandleDataTime;
	}
	
}
