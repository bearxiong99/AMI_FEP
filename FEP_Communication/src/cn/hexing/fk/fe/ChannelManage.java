/**
 * �ն����ӵ����ص��ⲿͨ�Ŷ˿ڣ�ͨ��ǰ�û����ӵ������ڲ��˿ڡ�
 * �ն��ʲ����ͨ�Ų���Ϊ���ⲿIP���ⲿ�˿�
 * ����ͨ��������˵�����ص�Ψһ�Ա�ʶΪ���ڲ�ip���ڲ��˿ڡ�
 * ���ڶ������У�ͨ����Ψһ��ʶΪ��appid
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
	//����������
	private int rtuHeartbeatInterval = 15*60;	//15�������������
	private int rtuTransferInterval = 60;		//��������1���Ӽ���ж�
	//�ڲ�����
	
	private static ChannelManage cm = new ChannelManage();
	private ChannelManage(){}
	
	public static ChannelManage getInstance(){
		return cm;
	}
	
	//����rtua�ҵ��ն�rtu���󣻸�����Ҫ������activeGprs,activeUms��ȷ��ͨ��
	private final Map<String,BaseClientChannel> mapGprsClients = new HashMap<String,BaseClientChannel>();
	private BaseClientChannel umsGateChannel = null;
	
	public void setRtuHeartbeatInterval(int interval){
		rtuHeartbeatInterval = interval;
	}

	public void setRtuTransferInterval(int rtuTransferInterval) {
		this.rtuTransferInterval = rtuTransferInterval;
	}

	/**
	 * ��ӣ�GPRS���صĿͻ�������
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
	 * �����ʲ����ͨ�ŷ�ʽ��ת��Ϊ����ʹ�õ�ͨ�ŷ�ʽ
	 * 		01:����; 02:GPRS;  03:DTMF;  04:Ethernet;
	 * 		05:����; 06:RS232; 07:CSD;   08:Radio; 	09:CDMA;
	 * @param commType
	 * @return��1 ���ţ� 2 GPRS/CDMA/Ethernet; 0 ������ʽ��ͨ��ǰ�û�����Ҫ֧��
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
	 * �����վ�������У�ֻ��֧��GPRSͨ����
	 * @param rtua
	 * @return
	 */
	public IChannel getGPRSChannel(String logicalAddr){
		//����rtua����RTU����
		RtuState state = RealtimeSynchronizer.getInstance().getRtuState(logicalAddr);
		if( null == state ){
			state = RealtimeSynchronizer.getInstance().loadFromDb(logicalAddr);
			RealtimeSynchronizer.getInstance().setRtuState(state);
		}
		WorkState workState = BatchSynchronizer.getInstance().getWorkState(logicalAddr);
		if( null == workState ){
			log.warn("No GPRS UP Channel for Terminal.rtua="+logicalAddr);//log.warn("�ն�û��GPRS���С�rtua="+logicalAddr);
			return null;
		}
		
		//1. �����ն���������ʱ�䣬ȡ��ǰ���õ�ͨ����
		//������⣺�ն˲���Ϊ���ţ�����ʵ����GPRSͨ����
		long timeSpan = System.currentTimeMillis() - workState.getLastGprsTime();
		long heartTimeSpan = System.currentTimeMillis() - workState.getLastHeartbeat();
		timeSpan = timeSpan<heartTimeSpan?timeSpan:heartTimeSpan;
		if( timeSpan< (rtuHeartbeatInterval*2*1000) && null != state.getActiveGprs()){
			//����������Ϣû��ͬ�����������󷵻��жϹ��ܲ���֧�֣�ֻ�ж�������������Ƿ���ͨ��
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
				return null;		//��UMSͨ��
			}*/
			IChannel channel = this.mapGprsClients.get(state.getActiveGprs());
			return channel;
		}
		return null;
	}
	
	/**
	 * �ն˿���ָ������ͨ�������û��ָ��������RTUA������ѡ��ͨ��
	 * ���ȼ���������ͨ��->��1����ͨ��->��2����ͨ��
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
	
	//���˶���ͨ����ȡӦ��ID����Ӧ�úţ��Ա�ͳһ����095598301501->301501����1065595598301501->301501
	public static final String filterUmsAppId(String ums){
		if (ums!=null){
			int index=ums.indexOf("95598");
			if (index>=0)
				ums=ums.substring(index+5);
		}
		return ums;
	}
	
	public static final String getUmsAppId(ComRtu rtu){
		//1. ����ն���ͨ����GPRS�����ȼ���ӦGPRS����ͨ�����
		int cType = communicationType(rtu.getCommType());
		if( cType <= 0 ){
			//log.error("�ն���ͨ������GPRS/CDMA�����߶��š�RTUA��"+rtu.getLogicAddress());
			return null;
		}
		
		//2. �߶���ͨ��������ʹ�õ�ǰ�����ͨ��
		String activeUms = null;
		
		//3. ��ǰ��Ч����ͨ��û�����ã��������ͨ���Ƕ��ţ���Ҫ����ͨ��ȡ
		if( cType == 1 ){
			//955983501����Ӧ�ú��벻ȡ
			activeUms = rtu.getCommAddress();
			if( null != activeUms && activeUms.length()>2 )
				return activeUms;
		}
		
		//4. GPRS�ն�ת����ͨ��������Լ������ն���ͨ��ʧ��������ӱ�1ͨ��ȡ
		cType = communicationType(rtu.getB1CommType() );
		if( cType == 1 ){
			activeUms = rtu.getB1CommAddress();
			if( null != activeUms && activeUms.length()>2 )
				return activeUms;
		}
		//��ʧ�ܡ���ô�ն˲����߶���ͨ��
		log.warn("�ն˶���ͨ�����ò���ȷ��RTUA="+rtu.getLogicAddress());
		return null;
	}
}
