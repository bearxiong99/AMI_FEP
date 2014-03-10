/**
 * ���б��ģ�GPRS��UMS�ȣ����͵�ҵ������BP��
 * ���յ��������֣�����client��
 * ��ϵά����
 * 		bpClient�����ҵ����е����루�����ͱ��ģ�
 * 		������Ϣ�ĵ����룬�ҵ�bpClient���Ҳ�����ѵ�������䵽bpClient;
 * ����һ�ַ���������RTUAȡģ��Ȼ���ҵ�bpclient����bpClient�����ǰ���£����ַ������ڼ�Ⱥ�����Ա���ͬһ���ն˷��͸���ͬ��BP
 */
package cn.hexing.fk.fe.msgqueue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import cn.hexing.fk.common.spi.IProfile;
import cn.hexing.fk.common.spi.socket.IServerSideChannel;
import cn.hexing.fk.fe.msgqueue.BpBalanceFactor.DistrictFactor;
import cn.hexing.fk.tracelog.TraceLog;
import cn.hexing.fk.utils.HexDump;

/**
 *
 */
public class MessageDispatch2Bp implements IProfile{
	private static final TraceLog tracer = TraceLog.getTracer();
	private static final MessageDispatch2Bp instance = new MessageDispatch2Bp();
	private Map<Byte,BpClient> a12ClientMap = new HashMap<Byte,BpClient>();
	private ArrayList<BpClient> clients = new ArrayList<BpClient>();

	private MessageDispatch2Bp(){}
	
	public static final MessageDispatch2Bp getInstance(){
		return instance;
	}
	
	private BpClient findByChannel(IServerSideChannel bpChannel){
		synchronized(a12ClientMap){
			for(BpClient c: clients){
				if( c.channel == bpChannel )
					return c;
			}
		}
		return null;
	}
	
	private boolean removeByChannel(IServerSideChannel bpChannel){
		synchronized(a12ClientMap){
			for(int i=0; i<clients.size(); i++ ){
				if( clients.get(i).channel == bpChannel ){
					clients.remove(i);
					return true;
				}
			}
		}
		return false;
	}
	
	public void clearTimeoutChannel(){
		synchronized(a12ClientMap){
			boolean removeTag=false;
			for(int i=0; i<clients.size(); i++ ){
				IServerSideChannel client = clients.get(i).channel;
				if( System.currentTimeMillis()-client.getLastIoTime() > 1000*60*3 ){
					clients.remove(i);
					removeTag=true;
					if( tracer.isEnabled() )
						tracer.trace("garbage client of MessageDispatch2Bp removed:"+client);
				}				
			}
			if (removeTag)
				divideDistrict();
		}
	}

	public void onBpClientConnected(IServerSideChannel bpChannel){
		if(tracer.isEnabled())
			tracer.trace("client connected:"+bpChannel);
		if( null != findByChannel(bpChannel) )
			return ;
		BpClient client = new BpClient();
		client.channel = bpChannel;
		synchronized(a12ClientMap){
			clients.add(client);
			divideDistrict();
		}
	}
	
	public void onBpClientClosed(IServerSideChannel bpChannel){
		synchronized(a12ClientMap){
			if( removeByChannel(bpChannel) ){
				if(tracer.isEnabled())
					tracer.trace("client closed:"+bpChannel);
				divideDistrict();
			}
		}
	}
	
	/**
	 * ���յ��е��ն��������䵽BP.
	 * ֻ����onBpClientConnected��onBpClientClosed synchronized(clients)�ڲ����á�
	 */
	private void divideDistrict(){
		synchronized(a12ClientMap){
			//���·��䡣�ȳ�ʼ��
			for( BpClient client : clients ){
				client.a1Array.clear();
				client.factor = 0;
			}
			//������Ԥ�ȼ��ص��ն˰��յ���������Ȩ�أ����з���
			BpClient minClient = null;
			for( DistrictFactor df: BpBalanceFactor.getInstance().getAllDistricts() ){
				minClient = getMinFactorClient();
				if( null != minClient ){
					minClient.a1Array.add(df.districtCode);
					minClient.factor += df.rtuCount;
				}
			}
			//����map
			for( BpClient client : clients ){
				for(int i=0; i<client.a1Array.size(); i++)
					a12ClientMap.put(client.a1Array.get(i),client);
			}
		}
		if( tracer.isEnabled() && !clients.isEmpty() )
			tracer.trace("BP divided by A1. profile="+profile());
	}
	
	private BpClient getMinFactorClient(){
		BpClient minClient = null;
		for( BpClient c : clients ){
			if( null == minClient )
				minClient = c;
			else if( c.factor < minClient.factor )
				minClient = c;
		}
		return minClient;
	}
	
	/**
	 * According to city code, get business processor client channel.
	 * @param a1
	 * @return
	 */
	public IServerSideChannel getBpChannel(byte a1){
		BpClient client = null;
		synchronized(a12ClientMap){
			client = a12ClientMap.get(a1);
			if( null == client ){
				//a1������û�з����bpclient ����ĳ��bpChannel�رյ�����Ҫ���·��䡣
				BpBalanceFactor.getInstance().travelRtus();
				divideDistrict();
				client = a12ClientMap.get(a1);
			}
		}
		if( null != client && client.channel.sendQueueSize() == 0 && client.channel.getRequestNum() != 0 )
			return client.channel;
		else
			return null;
//		return null != client ?  client.channel : null;
	}
	
	public IServerSideChannel getIdleChannel(){
		synchronized(a12ClientMap){
			for(BpClient c: clients){
				if( c.channel.getRequestNum()<0 || c.channel.sendQueueSize() < c.channel.getRequestNum() )
					return c.channel;
			}
		}
		return null;
	}
	
	private class BpClient{
		IServerSideChannel channel = null;
		Vector<Byte> a1Array = new Vector<Byte>();	//zhejiang message head's a1 byte
		int factor = 0;
	}

	public String profile() {
		StringBuffer sb = new StringBuffer(1024);
		sb.append("\r\n    <bp-citys>");
		synchronized(a12ClientMap){
			for(BpClient c: clients){
				sb.append("\r\n        <bp addr=\"").append(c.channel.getPeerAddr()).append("\" factor=\"");
				sb.append(c.factor).append("\">");
				boolean first = true;
				for(Byte B: c.a1Array ){
					if( first ){
						sb.append(HexDump.toHex(B.byteValue()));
						first = false;
					}
					else
						sb.append(",").append(HexDump.toHex(B.byteValue()));
				}
				sb.append("</bp>");
			}
		}
		sb.append("\r\n    </bp-citys>");
		return sb.toString();
	}
}
