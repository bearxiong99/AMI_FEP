/**
 * TCP客户端连接模块。主要用于业务处理器->通信前置机；通信前置机->gprs网关等；
 * 每个连接作为一个可监控模块。
 * 通信前置机，包含N个网关连接Module，以及N个短信连接Module。
 * 多个网关clientModule，上行消息一致处理，因此需要考虑特殊的事件处理泵。
 */
package cn.hexing.fk.clientmod;

import org.apache.log4j.Logger;

import cn.hexing.fk.common.events.BasicEventHook;
import cn.hexing.fk.common.simpletimer.ITimerFunctor;
import cn.hexing.fk.common.simpletimer.TimerData;
import cn.hexing.fk.common.simpletimer.TimerScheduler;
import cn.hexing.fk.common.spi.IClientModule;
import cn.hexing.fk.common.spi.IEventHandler;
import cn.hexing.fk.common.spi.IModule;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.IMessageCreator;
import cn.hexing.fk.message.MessageType;
import cn.hexing.fk.message.gate.MessageGate;
import cn.hexing.fk.message.gate.MessageGateCreator;
import cn.hexing.fk.sockclient.JSocket;
import cn.hexing.fk.sockclient.JSocketListener;
import cn.hexing.fk.sockclient.async.event.ClientConnectedEvent;
import cn.hexing.fk.sockserver.event.ReceiveMessageEvent;
import cn.hexing.fk.sockserver.event.SendMessageEvent;
import cn.hexing.fk.tracelog.TraceLog;
import cn.hexing.fk.utils.CalendarUtil;

/**
 */
public class ClientModule implements JSocketListener, IClientModule, ITimerFunctor {
	private static final Logger log = Logger.getLogger(ClientModule.class);
	//可配置属性
	private String name = "GPRS网关客户端";
	private String moduleType = IModule.MODULE_TYPE_SOCKET_CLIENT;
	private String hostIp="127.0.0.1";
	private int hostPort = 10001;
	private int bufLength = 256;		//默认缓冲区长度
	private IMessageCreator messageCreator = new MessageGateCreator();
	private int timeout = 10;			//读或者写超时，单位秒
	private String txfs = "02";			//通信方式配置。
	private IEventHandler eventHandler;	//通过spring配置事件处理器，例如GateMessageEventHandler
	private JSocket socket = null;
	//心跳管理
	private int heartInterval = 0;		//心跳间隔(秒)。如果间隔时间内无上行报文，则发送心跳。0表示取消心跳。
	private int requestNum = 200;		//客户端向服务器请求接收报文数量。-1表示请求服务器永远异步推送报文
	private long lastHeartbeat = System.currentTimeMillis();	//上次心跳应答时间

	//统计属性,初始化自动为0
	private long lastReceiveTime = System.currentTimeMillis();							//最新接收报文时间
	private long lastSendTime = 0;							//最近发送成功的时间
	private long totalRecvMessages=0,totalSendMessages=0;	//总共收、发消息总数
	private int msgRecvPerMinute=0,msgSendPerMinute=0;		//每分钟收、发报文个数
	private Object statisticsRecv = new Object() ,statisticsSend = new Object();

	//内部属性
	private boolean active = false;
	private IMessage heartMsg = null;		//定时请求报文。以心跳的形式。
	private int curRecv = 200;				//对收到服务器报文计数，收到requestNum报文后，发送请求。
	
	/**当连接不上的时候是否自动重连,默认是自动重连*/
	private boolean isAutoReconnect=true;

	public boolean sendMessage(IMessage msg){
		if( ! active )
			return false;
		boolean result = socket.sendMessage(msg);
		if(result && msg.isHeartbeat() ){
			TraceLog _tracer = TraceLog.getTracer(socket.getClass());
			if( _tracer.isEnabled() )
				_tracer.trace("send heart-beat ok. "+this.socket.getPeerAddr());
			log.debug("send heart-beat ok. "+this.socket.getPeerAddr());
		}
		
		return result;
	}
	
	public void onClose(JSocket client) {
		active = false;
	}

	public void onConnected(JSocket client) {
		active = true;
		if( heartInterval>0 && requestNum>0 ){
			sendMessage(heartMsg);
			this.lastReceiveTime = System.currentTimeMillis();
			if( log.isDebugEnabled() ){
				log.debug("request frame size="+requestNum);//log.debug("连接时，请求报文数量="+requestNum);
			}
			curRecv = requestNum;
		}
		ClientConnectedEvent ce = new ClientConnectedEvent(null,client);
		try{
			eventHandler.handleEvent(ce);
		}catch(Exception e){}
	}

	public void onReceive(JSocket client, IMessage msg) {
		synchronized(statisticsRecv){
			msgRecvPerMinute++;
			totalRecvMessages++;
			if( requestNum > 0 ){
				if( msg.isHeartbeat() ){
					this.lastHeartbeat = System.currentTimeMillis();
					TraceLog _tracer = TraceLog.getTracer(socket.getClass());
					if( _tracer.isEnabled() )
						_tracer.trace("receive heart-beat,lastHeartbeat="+this.lastHeartbeat+",client="+socket.getPeerAddr()+",curRecv="+curRecv);
				}

				if( --curRecv == 0 ){
					sendMessage(heartMsg);
					if( log.isDebugEnabled() )
						log.debug("When onReceive,server's Transfer number equals requestNum,resize requestNum="+requestNum);
					curRecv = requestNum;
				}
				else{
					if( log.isDebugEnabled() ){
						IMessage amsg = msg;
						if( msg.getMessageType() == MessageType.MSG_GATE ){
							amsg = ((MessageGate)msg).getInnerMessage();
							if( null != amsg )
								log.debug("Remaining Frame Size="+curRecv+",msg="+amsg+",meterId:"+amsg.getLogicalAddress());//log.debug("剩余报文数量="+curRecv+",msg="+amsg);
						}
						else
							log.debug("Remaining Frame Size="+curRecv+",msg="+amsg+",meterId:"+amsg.getLogicalAddress());
					}
				}
			}
		}
		lastReceiveTime = System.currentTimeMillis();
		try{
			ReceiveMessageEvent re = new ReceiveMessageEvent(msg,client);
			if( eventHandler instanceof BasicEventHook ){
				((BasicEventHook)eventHandler).postEvent(re);
			}
			else
				eventHandler.handleEvent(re);
		}catch(Exception e){
			log.warn("ClintModule handle onReceive Exp:"+e.getLocalizedMessage(),e);
		}
		long timeSpand = System.currentTimeMillis() - lastReceiveTime;
		if( timeSpand > 100 ){
			TraceLog _tracer = TraceLog.getTracer(socket.getClass());
			if( _tracer.isEnabled() )
				_tracer.trace("ClientModule fire onReceive event, it takes "+ timeSpand +" milliseconds.");
		}
	}

	public void onSend(JSocket client, IMessage msg) {
		synchronized(statisticsSend){
			msgSendPerMinute++;
			totalSendMessages++;
		}
		lastSendTime = System.currentTimeMillis();
		try{
			eventHandler.handleEvent(new SendMessageEvent(msg,client));
		}catch(Exception e){}
		long timeSpand = System.currentTimeMillis() - lastSendTime;
		if( timeSpand > 100 ){
			TraceLog _tracer = TraceLog.getTracer(socket.getClass());
			if( _tracer.isEnabled() )
				_tracer.trace("ClientModule fire onSend event, it takes "+ timeSpand +" milliseconds.");
		}
	}

	public String getModuleType() {
		return this.moduleType;
	}
	
	public void setModuleType(String modType){
		this.moduleType = modType;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String nm){
		name = nm;
	}

	public String getTxfs() {
		return txfs;
	}

	public boolean isActive() {
		return active;
	}

	public void init(){
		if( null == socket ){
			//如果没有通过spring配置client对象
			socket = new JSocket();
			socket.setHostIp(hostIp);
			socket.setHostPort(hostPort);
		}
	}
	
	public boolean start() {
		if( heartInterval>0 && null == heartMsg ){
			heartMsg = messageCreator.createHeartBeat(requestNum);
			curRecv = requestNum;
		}
		init();
		socket.setBufLength(bufLength);
		socket.setMessageCreator(messageCreator);
		socket.setTimeout(timeout);
		socket.setListener(this);
		socket.setTxfs(txfs);
		socket.setAutoReconnect(isAutoReconnect);
		socket.init();
		if( eventHandler instanceof BasicEventHook ){
			((BasicEventHook)eventHandler).addSource(socket);
		}
		//启动定时服务
		TimerScheduler.getScheduler().addTimer(new TimerData(this,0,60));	//定时器0，每分钟定时器
		//心跳间隔.检测心跳发送情况
		if( heartInterval>0 ){
			//心跳发送及检测定时器执行间隔必须小于判断时间间隔，否则容易错过执行内容
			TimerScheduler.getScheduler().addTimer(new TimerData(this,1,2));	//定时器1
		}
		lastHeartbeat = System.currentTimeMillis();
		return true;
	}

	public void stop() {
		TimerScheduler.getScheduler().removeTimer(this, 0);
		if( null != socket )
			socket.close();
	}
	public void restart() {
		if( null != socket )
			socket.reConnect();
	}
	public String profile() {
		StringBuffer sb = new StringBuffer(1024);
		sb.append("\r\n    <sockclient-profile type=\"").append(getModuleType()).append("\">");
		sb.append("\r\n        ").append("<name>").append(getName()).append("</name>");
		sb.append("\r\n        ").append("<port>").append(hostIp+":"+hostPort).append("</port>");
		sb.append("\r\n        ").append("<state>").append(isActive()).append("</state>");
		sb.append("\r\n        ").append("<timeout>").append(timeout).append("</timeout>");

		sb.append("\r\n        ").append("<txfs>").append(txfs).append("</txfs>");
		sb.append("\r\n        ").append("<totalRecv>").append(totalRecvMessages).append("</totalRecv>");
		sb.append("\r\n        ").append("<totalSend>").append(totalSendMessages).append("</totalSend>");
		sb.append("\r\n        ").append("<perMinuteRecv>").append(msgRecvPerMinute).append("</perMinuteRecv>");
		sb.append("\r\n        ").append("<perMinuteSend>").append(msgSendPerMinute).append("</perMinuteSend>");

		String stime = CalendarUtil.getTimeString(lastReceiveTime);
		sb.append("\r\n        ").append("<lastRecv>").append(stime).append("</lastRecv>");
		stime = CalendarUtil.getTimeString(lastSendTime);
		sb.append("\r\n        ").append("<lastSend>").append(stime).append("</lastSend>");
		sb.append("\r\n    </sockclient-profile>");
		return sb.toString();
	}
	
	public void onTimer(int id){
		//先检测心跳定时
		if( 1 == id && heartInterval>0 ){
			long interval = System.currentTimeMillis() - this.lastHeartbeat ;
			if( interval>= heartInterval ){
				curRecv = requestNum;
				sendMessage(messageCreator.createHeartBeat(this.requestNum));
			}
			//超过两个心跳间隔没有收到任何上行报文则重启链路
			interval = System.currentTimeMillis() - this.lastReceiveTime ;
			if( interval> (heartInterval*3) ){
				TraceLog _trace = TraceLog.getTracer(socket.getClass());
				if( _trace.isEnabled() )
					_trace.trace("no up frame within 2 heartbeat intervals，connection will be reset. client="+socket.getPeerAddr()+",heartInterval="+heartInterval+",interval="+interval+",lastReceiveTime="+lastReceiveTime);
				socket.reConnect();
				//Attention, last heart-beat time must update when socket is reconnected every time. 
				this.lastReceiveTime = System.currentTimeMillis();
			}
		}
		else if( 0 ==  id ){
			//每分钟定时器
//			GlobalEventHandler.postEvent(new ModuleProfileEvent(this));
			synchronized(statisticsRecv){
				msgRecvPerMinute = 0;
			}
			synchronized(statisticsSend){
				msgSendPerMinute = 0;
			}
		}
	}

	public long getLastReceiveTime() {
		return lastReceiveTime;
	}

	public long getLastSendTime() {
		return lastSendTime;
	}

	public int getMsgRecvPerMinute() {
		return this.msgRecvPerMinute;
	}

	public int getMsgSendPerMinute() {
		return this.msgSendPerMinute;
	}

	public long getTotalRecvMessages() {
		return this.totalRecvMessages;
	}

	public long getTotalSendMessages() {
		return this.totalSendMessages;
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

	public int getBufLength() {
		return bufLength;
	}

	public void setBufLength(int bufLength) {
		this.bufLength = bufLength;
	}

	public IMessageCreator getMessageCreator() {
		return messageCreator;
	}

	public void setMessageCreator(IMessageCreator messageCreator) {
		this.messageCreator = messageCreator;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public JSocket getSocket() {
		return socket;
	}

	public void setSocket(JSocket socket) {
		this.socket = socket;
	}

	public void setTxfs(String txfs) {
		this.txfs = txfs;
	}

	public void setEventHandler(IEventHandler eventHandler) {
		this.eventHandler = eventHandler;
	}

	public void setHeartInterval(int heartInterval) {
		this.heartInterval = heartInterval*1000;
	}

	public int getRequestNum() {
		return requestNum;
	}

	public void setRequestNum(int requestNum) {
		this.requestNum = requestNum;
	}

	@Override
	public String toString() {
		return profile();
	}

	public void setHeartMsg(IMessage heartMsg) {
		this.heartMsg = heartMsg;
	}

	public final boolean isAutoReconnect() {
		return isAutoReconnect;
	}

	public final void setAutoReconnect(boolean isAutoReconnect) {
		this.isAutoReconnect = isAutoReconnect;
	}

}
