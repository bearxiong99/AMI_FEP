/**
 * 终端连接到网关的外部通信端口；通信前置机连接到网关内部端口。
 * 终端资产表的通信参数为：外部IP＋外部端口
 * 对于通信下行来说，网关的唯一性标识为：内部ip＋内部端口。
 * 对于短信下行，通道的唯一标识为：appid
 * 
 */
package cn.hexing.fk.fe;

import java.util.HashMap;
import java.util.Map;

import cn.hexing.fk.clientmod.ClientModule;
import cn.hexing.fk.common.spi.socket.IChannel;
import cn.hexing.fk.common.spi.socket.abstra.BaseClientChannel;
import cn.hexing.fk.fe.cluster.BatchSynchronizer;
import cn.hexing.fk.fe.cluster.RealtimeSynchronizer;
import cn.hexing.fk.fe.cluster.RtuState;
import cn.hexing.fk.fe.cluster.WorkState;
import cn.hexing.fk.model.ComRtu;
import cn.hexing.fk.tracelog.TraceLog;

import org.apache.log4j.Logger;

/**
 *
 */
public class ChannelManage {
	private static final Logger log = Logger.getLogger(ChannelManage.class);
	private static final TraceLog trace = TraceLog.getTracer(ChannelManage.class);
	//可配置属性
	private int rtuHeartbeatInterval = 15*60;	//15分钟心跳间隔。
	private int rtuTransferInterval = 60;		//两次请求1分钟间隔判断
	//内部属性
	
	private static ChannelManage cm = new ChannelManage();
	private ChannelManage(){}
	
	public static ChannelManage getInstance(){
		return cm;
	}
	
	//根据rtua找到终端rtu对象；根据需要，依据activeGprs,activeUms来确定通道
	private final Map<String,BaseClientChannel> mapGprsClients = new HashMap<String,BaseClientChannel>();
	private BaseClientChannel umsGateChannel = null;
	
	public void setRtuHeartbeatInterval(int interval){
		rtuHeartbeatInterval = interval;
	}

	public void setRtuTransferInterval(int rtuTransferInterval) {
		this.rtuTransferInterval = rtuTransferInterval;
	}

	/**
	 * 添加：GPRS网关的客户端连接
	 * @param gprsClient
	 */
	public void addGprsClient(ClientModule gprsClient ){
		mapGprsClients.put(gprsClient.getSocket().getPeerAddr(), gprsClient.getSocket());
	}
	
	public void setUmsGateClient(ClientModule umsGateClient){
		this.umsGateChannel = umsGateClient.getSocket();
	}
	
	public void setUmsGateChannel(BaseClientChannel channel ){
		this.umsGateChannel = channel;
	}
	
	public BaseClientChannel getActiveGprsChannel(){
		for( BaseClientChannel channel: mapGprsClients.values() )
			if( channel.isActive() )
				return channel;
		return null;
	}
	
	/**
	 * 根据资产表的通信方式，转换为程序使用的通信方式
	 * 		01:短信; 02:GPRS;  03:DTMF;  04:Ethernet;
	 * 		05:红外; 06:RS232; 07:CSD;   08:Radio; 	09:CDMA;
	 * @param commType
	 * @return：1 短信； 2 GPRS/CDMA/Ethernet; 0 其它方式。通信前置机不需要支持
	 */
	public static int communicationType(String commType){
		if( null == commType )
			return -1;
		if( commType.equals("02") || commType.equals("09") || commType.equals("04") )
			return 2;
		else if( commType.equals("01") )
			return 1;
		else
			return 0;
	}
	
	/**
	 * 针对主站心跳下行，只能支持GPRS通道。
	 * @param rtua
	 * @return
	 */
	public IChannel getGPRSChannel(String logicalAddr){
		//根据rtua来找RTU对象
		RtuState state = RealtimeSynchronizer.getInstance().getRtuState(logicalAddr);
		if( null == state ){
			state = RealtimeSynchronizer.getInstance().loadFromDb(logicalAddr);
			RealtimeSynchronizer.getInstance().setRtuState(state);
		}
		WorkState workState = BatchSynchronizer.getInstance().getWorkState(logicalAddr);
		if( null == workState ){
			log.warn("No GPRS UP Channel for Terminal.rtua="+logicalAddr);//log.warn("终端没有GPRS上行。rtua="+logicalAddr);
			return null;
		}
		
		//1. 根据终端最新上行时间，取当前可用的通道。
		//解决问题：终端参数为短信，但是实际走GPRS通道。
		long timeSpan = System.currentTimeMillis() - workState.getLastGprsTime();
		long heartTimeSpan = System.currentTimeMillis() - workState.getLastHeartbeat();
		timeSpan = timeSpan<heartTimeSpan?timeSpan:heartTimeSpan;
		if( timeSpan< (rtuHeartbeatInterval*2*1000) && null != state.getActiveGprs()){
			//下行请求信息没有同步，所以请求返回判断功能不再支持，只判断两个心跳间隔是否有通信
			/*long lastReq = workState.getLastReqTime();
			//long tspan = Math.max(System.currentTimeMillis() - lastReq, lastReq-rtu.getLastGprsTime() ) ;			
			long tspan = Math.abs(System.currentTimeMillis() - lastReq) ;
			if( tspan> (rtuTransferInterval*1000) && workState.getLastGprsTime() < lastReq  ){
				StringBuilder sb = new StringBuilder();
				sb.append("DOWN by UMS: lastGprs=");
				sb.append(CalendarUtil.getDateTimeString(workState.getLastGprsTime()));
				sb.append(",now-lastReq = ").append(tspan).append(" ms.");
				sb.append(" RTUA=").append(HexDump.toHex(rtua));
				trace.trace(sb.toString());
				return null;		//走UMS通道
			}*/
			IChannel channel = this.mapGprsClients.get(state.getActiveGprs());
			return channel;
		}
		return null;
	}
	
	/**
	 * 终端可能指定短信通道。如果没有指定，依据RTUA的配置选择通道
	 * 优先级：短信主通道->备1短信通道->备2短信通道
	 * @param rtua
	 * @return
	 */
	public IChannel getUmsGateChannel( String logicalAddr ){
		if( logicalAddr != null ){
			RtuState state = RealtimeSynchronizer.getInstance().getRtuState(logicalAddr);
			if( null == state ){
				state = RealtimeSynchronizer.getInstance().loadFromDb(logicalAddr);
				RealtimeSynchronizer.getInstance().setRtuState(state);
			}
		}
		return umsGateChannel;
	}
	
	public IChannel getUmsGateChannel( ){
		return getUmsGateChannel(null);
	}
	
	//过滤短信通道获取应用ID及子应用号，以便统一处理095598301501->301501或者1065595598301501->301501
	public static final String filterUmsAppId(String ums){
		if (ums!=null){
			int index=ums.indexOf("95598");
			if (index>=0)
				ums=ums.substring(index+5);
		}
		return ums;
	}
	
	public static final String getUmsAppId(ComRtu rtu){
		//1. 如果终端主通道是GPRS，首先检测对应GPRS网关通信情况
		int cType = communicationType(rtu.getCommType());
		if( cType <= 0 ){
			//log.error("终端主通道不是GPRS/CDMA，或者短信。RTUA＝"+rtu.getLogicAddress());
			return null;
		}
		
		//2. 走短信通道。优先使用当前活动短信通道
		String activeUms = null;
		
		//3. 当前有效短信通道没有设置，则如果主通道是短信，需要从主通道取
		if( cType == 1 ){
			//955983501，子应用号码不取
			activeUms = rtu.getCommAddress();
			if( null != activeUms && activeUms.length()>2 )
				return activeUms;
		}
		
		//4. GPRS终端转短信通道情况，以及短信终端主通道失败情况。从备1通道取
		cType = communicationType(rtu.getB1CommType() );
		if( cType == 1 ){
			activeUms = rtu.getB1CommAddress();
			if( null != activeUms && activeUms.length()>2 )
				return activeUms;
		}
		//都失败。那么终端不能走短信通道
		log.warn("终端短信通道配置不正确。RTUA="+rtu.getLogicAddress());
		return null;
	}
}
