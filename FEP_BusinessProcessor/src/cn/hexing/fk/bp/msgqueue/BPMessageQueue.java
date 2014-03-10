/**
 * ҵ�����������б��Ķ��С�
 * ҵ���������У�ֱ�ӵ���sendMessage������
 * 
 * ���б��ģ�
 * ���Ķ��е������ߣ����룩��FEMessageEventHandle
 * ���Ķ��е������ߣ�ȡ�ߣ���????
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
	private CacheQueue cacheQueue;	//spring ����ʵ�֡�
	private DataSource dataSource;						//����DbMonitor
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
	
	//��Ϣ����ͳһ�����ն�����
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

	//���涨����Ϣ���еķ���
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
	 * ��ҵ�������յ�ͨ��ǰ�û����б���ʱ�����ô˺�������������Ϣ������С�
	 * ҵ��������Ҫ�����������ȡ���ݽ��д������ö��̷߳�ʽ���д���
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
