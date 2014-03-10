/**
 * 简化系统配置。配置文件为application.properties。
 * 格式：
 * [ip:]port [?参数名1][=参数值1][&参数名2][=参数值2]... 
 * gate.tcp.servers=127.0.0.1:1002?name=gprs-t-31&bufLength=10240&requestNum=500;127.0.0.1:10004?name=gprs-t-32
 * gate.udp.servers=127.0.0.1:1003?name=gprs-u-31;127.0.0.1:1005?name=gprs-u-32
 * 
 */
package cn.hexing.fk.fe.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import cn.hexing.fk.FasSystem;
import cn.hexing.fk.clientmod.ClientModule;
import cn.hexing.fk.common.events.BasicEventHook;
import cn.hexing.fk.common.spi.IModule;
import cn.hexing.fk.common.spi.socket.IClientIO;
import cn.hexing.fk.common.spi.socket.abstra.BaseSocketServer;
import cn.hexing.fk.fe.ChannelManage;
import cn.hexing.fk.fe.gprs.GateMessageEventHandler;
import cn.hexing.fk.fe.ums.UmsGateEventHandler;
import cn.hexing.fk.message.IMessageCreator;
import cn.hexing.fk.message.gate.MessageGateCreator;
import cn.hexing.fk.sockserver.SyncTcpServer;
import cn.hexing.fk.sockserver.TcpSocketServer;
import cn.hexing.fk.sockserver.io.SimpleIoHandler;
import cn.hexing.fk.utils.ApplicationContextUtil;

/**
 */
public class ApplicationPropertiesConfig {
	private static final Logger log = Logger.getLogger(ApplicationPropertiesConfig.class);
	private static final ApplicationPropertiesConfig config = new ApplicationPropertiesConfig();

	//GPRS网关连接的公共属性设置
	private int bufLength = 10240;
	private int timeout = 2;
	private int heartInterval = 10;
	private int requestNum = 500;
	
	//UMS网关连接的公共属性设置
	private String umsServerAddr;

	private String gprsGateClients;
	private String bpServer;
	private String monitorServer;
	
	private GateMessageEventHandler gprsMessageEventHandler;
	private String gprsMessageEventHandlerId = "fe.event.handle.gprs";
	private UmsGateEventHandler umsGateEventHandler;
	private String umsGateEventHandlerId = "fe.event.handle.umsgate";
	
	private BasicEventHook bpMessageEventHandler,monitorEventHandler;
	private String bpMessageEventHandlerId = "bpserver.event.hook";
	private String monitorEventHandlerId = "monitor.event.handler";
	private String monitorMessageCreator = "messageCreator.Monitor";

	private List<ClientModule> gprsClientModules = new ArrayList<ClientModule>();
	private ClientModule umsGateModule = null;
	private List<BaseSocketServer> socketServers = new ArrayList<BaseSocketServer>();
	private List<BasicEventHook> eventHandlers = new ArrayList<BasicEventHook>();
	//对于SocketServer来说，需要配置的默认属性定义如下
	//1.name属性，如果没有配置，则按照gprs-ip(最后一段）-port-t(or-u);
	//2.bufLength 512 对于终端,10240对于其它
	//3.requestNum 500
	//4.

	private ApplicationPropertiesConfig(){}
	
	public static final ApplicationPropertiesConfig getInstance(){
		return config;
	}
	
	public void setGprsGateClients(String gprsClients) {
		this.gprsGateClients = gprsClients.trim();
	}
	
	public boolean addGprsGates(String clientsUrl){
		List<ClientModule> gateClients = createGprsGateClients(clientsUrl);
		boolean result = false;
		for(ClientModule gate: gateClients ){
			gate.init();
			ChannelManage.getInstance().addGprsClient(gate);
			FasSystem.getFasSystem().addModule(gate);
			gate.start();
			result = true;
		}
		return result;
	}
	
	public boolean addGprsGate(String hostIp,int port, String gateName ){
		String url = hostIp + ":" + port ;
		if( null != gateName && gateName.length()>=1 ){
			url += "?name=" + gateName;
		}
		return addGprsGates(url);
	}
	
	@SuppressWarnings("unchecked")
	public List<ClientModule> createGprsGateClients(String clientsUrl){
		List<ClientModule> clients = new ArrayList<ClientModule>(); 
		
		if( null == clientsUrl || clientsUrl.length()<2 )
			return clients;

		String[] urls = clientsUrl.split(";");
		for(String url: urls ){
			Map<String,String> result = parseUrlConfig(url);
			String ip=null, param = null,gateIpPostFix=null;
			int port = 0;
			try{
				ip = result.get("ip");
				int index = ip.lastIndexOf(".");
				if( index>0 ){
					gateIpPostFix = ip.substring(index+1);
				}
				else{
					gateIpPostFix = ip;
				}
				param = result.get("port");
				if( null == param ){
					log.error("TCP Socket Server config miss port");
					continue;
				}
				port = Integer.parseInt(param);
			}catch(Exception e){
				log.error("gprs client config exception,port="+param,e);
				continue;
			}
			
			ClientModule gprsClient = new ClientModule();
			gprsClient.setModuleType(IModule.MODULE_TYPE_GPRS_CLIENT);
			gprsClient.setHostIp(ip);
			gprsClient.setHostPort(port);

			//开始设置tcpSocketServer的参数.
			param = result.get("name");
			if( null == param ){
				param = "gprs-"+gateIpPostFix;
			}
			gprsClient.setName(param);
			
			param = result.get("bufLength");
			if( null != param ){
				try{
					bufLength = Integer.parseInt(param);
				}catch(Exception e){
					log.error("bufLength config err:"+param);
				}
			}
			gprsClient.setBufLength(bufLength);
			
			IMessageCreator messageCreator = new MessageGateCreator();
			param = result.get("messageCreator");
			if( null != param ){
				//只能定义bean id，从spring取。
				IMessageCreator mc = (IMessageCreator)ApplicationContextUtil.getBean(param);
				if( null == mc ){
					try{
						Class clz = Class.forName(param);
						mc = (IMessageCreator)clz.newInstance();
					}catch(Exception e){}
				}
				if( null != mc ){
					messageCreator = mc;
				}
			}
			gprsClient.setMessageCreator(messageCreator);
			
			param = result.get("txfs");
			if( null != param )
				gprsClient.setTxfs(param);
			else
				gprsClient.setTxfs("02");
			
			param = result.get("timeout");
			if( null != param ){
				try{
					timeout = Integer.parseInt(param);
				}catch(Exception e){}
			}
			gprsClient.setTimeout(timeout);
			
			if( null == gprsMessageEventHandler )
				gprsMessageEventHandler = (GateMessageEventHandler)ApplicationContextUtil.getBean(gprsMessageEventHandlerId);
			if( null == gprsMessageEventHandler ){
				log.error("gprsMessageEventHandler == null.");
				return clients;
			}
			gprsClient.setEventHandler(gprsMessageEventHandler);
	
			param = result.get("heartInterval");
			if( null != param ){
				try{
					heartInterval = Integer.parseInt(param);
				}catch(Exception e){}
			}
			gprsClient.setHeartInterval(heartInterval);
			
			//还需要设置公共属性
			gprsClient.setRequestNum(requestNum);
			gprsClient.init();
			clients.add(gprsClient);
		}
		return clients;
	}
	
	private void parseGprsGateClients() {
		gprsClientModules.addAll(createGprsGateClients(gprsGateClients) );
	}

	public void setBpServer(String bpServers) {
		this.bpServer = bpServers.trim();
	}
	
	@SuppressWarnings("unchecked")
	private void parseBpServer() {
		Map<String,String> result = parseUrlConfig(this.bpServer);
		int port = 0, bufLength = 10240;
		String param = null;
		try{
			param = result.get("port");
			if( null == param ){
				log.error("Business Processor TCP Socket Server config miss port");
				return;
			}
			port = Integer.parseInt(param);
		}catch(Exception e){
			log.error("Business Processor TCP Socket Server config exception,port="+param,e);
			return;
		}
		
		BaseSocketServer socketServer = new SyncTcpServer();
		socketServer.setPort(port);
		param = result.get("ip");
		if( null != param )
			socketServer.setIp(param);
		//开始设置tcpSocketServer的参数.
		param = result.get("name");
		if( null == param ){
			param = "bp-"+port;
		}
		socketServer.setName(param);
		
		param = result.get("bufLength");
		if( null != param ){
			try{
				bufLength = Integer.parseInt(param);
			}catch(Exception e){
				log.error("bufLength config err:"+param);
			}
		}
		socketServer.setBufLength(bufLength);
		
		int ioThreadSize = 2;
		param = result.get("ioThreadSize");
		if( null != param ){
			try{
				ioThreadSize = Integer.parseInt(param);
				socketServer.setIoThreadSize(ioThreadSize);
			}catch(Exception e){}
		}
		
		IMessageCreator messageCreator = new MessageGateCreator();
		param = result.get("messageCreator");
		if( null != param ){
			//只能定义bean id，从spring取。
			IMessageCreator mc = null;
			try{
				mc = (IMessageCreator)ApplicationContextUtil.getBean(param);
			}catch(Exception e){}
			if( null == mc ){
				try{
					Class clz = Class.forName(param);
					mc = (IMessageCreator)clz.newInstance();
				}catch(Exception e){}
			}
			if( null != mc ){
				messageCreator = mc;
			}
		}
		socketServer.setMessageCreator(messageCreator);
		socketServer.setModuleType(IModule.MODULE_TYPE_BP);
		IClientIO ioHandler = new SimpleIoHandler();
		param = result.get("ioHandler");
		if( null != param ){
			IClientIO ioh = null;
			try{
				ioh = (IClientIO)ApplicationContextUtil.getBean(param);
			}catch(Exception e){}
			if( null == ioh ){
				try{
					Class clz = Class.forName(param);
					ioh = (IClientIO)clz.newInstance();
				}catch(Exception e){}
			}
			if( null != ioh )
				ioHandler = ioh;
		}
		socketServer.setIoHandler(ioHandler);
		
		param = result.get("timeout");
		int timeout = 3*60;
		if( null != param ){
			try{
				timeout = Integer.parseInt(param);
			}catch(Exception e){}
		}
		socketServer.setTimeout(timeout);
		
		socketServers.add(socketServer);
		if( null == bpMessageEventHandler ){
			bpMessageEventHandler = (BasicEventHook)ApplicationContextUtil.getBean(bpMessageEventHandlerId);
		}
		bpMessageEventHandler.setSource(socketServer);
		eventHandlers.add(bpMessageEventHandler);
	}

	public void setMonitorServer(String monitorServers) {
		this.monitorServer = monitorServers.trim();
	}
	
	@SuppressWarnings("unchecked")
	private void parseMonitorServer() {
		if( null == monitorServer || monitorServer.length()<2 )
			return;
		Map<String,String> result = parseUrlConfig(this.monitorServer);
		int port = 0, bufLength = 1024*50;
		String param = null;
		try{
			param = result.get("port");
			if( null == param ){
				log.error("Monitor Socket Server config miss port");
				return;
			}
			port = Integer.parseInt(param);
		}catch(Exception e){
			log.error("Monitor Socket Server config exception,port="+param,e);
			return;
		}
		
		TcpSocketServer mServer = new TcpSocketServer();
		mServer.setPort(port);
		param = result.get("ip");
		if( null != param )
			mServer.setIp(param);
		//开始设置tcpSocketServer的参数.
		param = result.get("name");
		if( null == param ){
			param = "monitor-"+port;
		}
		mServer.setName(param);
		
		param = result.get("bufLength");
		if( null != param ){
			try{
				bufLength = Integer.parseInt(param);
			}catch(Exception e){
				log.error("bufLength config err:"+param);
			}
		}
		mServer.setBufLength(bufLength);
		
		int ioThreadSize = 1;
		param = result.get("ioThreadSize");
		if( null != param ){
			try{
				ioThreadSize = Integer.parseInt(param);
				mServer.setIoThreadSize(ioThreadSize);
			}catch(Exception e){}
		}
		
		IMessageCreator messageCreator = (IMessageCreator)ApplicationContextUtil.getBean(monitorMessageCreator);
		param = result.get("messageCreator");
		if( null != param ){
			//只能定义bean id，从spring取。
			IMessageCreator mc = null;
			try{
				mc = (IMessageCreator)ApplicationContextUtil.getBean(param);
			}catch(Exception exp){
				mc = null;
			}
			if( null == mc ){
				try{
					Class clz = Class.forName(param);
					mc = (IMessageCreator)clz.newInstance();
				}catch(Exception e){
					mc = null;
				}
			}
			if( null != mc ){
				messageCreator = mc;
			}
		}
		mServer.setMessageCreator(messageCreator);
		
		IClientIO ioHandler = new SimpleIoHandler();
		param = result.get("ioHandler");
		if( null != param ){
			IClientIO ioh = null;
			try{
				ioh = (IClientIO)ApplicationContextUtil.getBean(param);
			}catch(Exception e){
				ioh = null;
			}
			if( null == ioh ){
				try{
					Class clz = Class.forName(param);
					ioh = (IClientIO)clz.newInstance();
				}catch(Exception e){}
			}
			if( null != ioh )
				ioHandler = ioh;
		}
		mServer.setIoHandler(ioHandler);
		
		param = result.get("timeout");
		int timeout = 30*60;
		if( null != param ){
			try{
				timeout = Integer.parseInt(param);
			}catch(Exception e){}
		}
		mServer.setTimeout(timeout);
		
		socketServers.add(mServer);
		if( null == monitorEventHandler ){
			monitorEventHandler = (BasicEventHook)ApplicationContextUtil.getBean(monitorEventHandlerId);
		}
		monitorEventHandler.setSource(mServer);
		eventHandlers.add(monitorEventHandler);
	}

	private Map<String,String> parseUrlConfig(String url){
		Map<String,String> result = new HashMap<String,String>();
		//[ip:]port [?参数名1][=参数值1][&参数名2][=参数值2]... 
		String hostAddr = null, params = null;
		int index =  url.indexOf("?");
		if( index>0 ){
			hostAddr = url.substring(0, index);
			params = url.substring(index+1);
		}
		else{
			hostAddr = url;
		}
		if( null != hostAddr ){
			index = hostAddr.indexOf(":");
			if( index>0 ){
				String ip = hostAddr.substring(0, index);
				String port = hostAddr.substring(index+1);
				result.put("ip", ip);
				result.put("port", port);
			}
			else{
				result.put("port", hostAddr);
			}
		}
		if( null != params ){
			String[] paramArray = params.split("&");
			if( null != paramArray ){
				for( String param: paramArray ){
					index = param.indexOf("=");
					if( index<0 ){
						log.error("Server config malformed,miss'=' :"+param);
						continue;
					}
					String name = param.substring(0, index);
					String value = param.substring(index+1);
					result.put(name, value);
				}
			}
		}
		return result;
	}

	public final List<BaseSocketServer> getSocketServers() {
		return socketServers;
	}

	public final List<BasicEventHook> getEventHandlers() {
		return eventHandlers;
	}

	public final void setMonitorEventHandler(BasicEventHook monitorEventHandler) {
		this.monitorEventHandler = monitorEventHandler;
	}

	public final void setMonitorEventHandlerId(String monitorEventHandlerId) {
		this.monitorEventHandlerId = monitorEventHandlerId;
	}

	public final void setMonitorMessageCreator(String monitorMessageCreator) {
		this.monitorMessageCreator = monitorMessageCreator;
	}
	
	public void parseConfig(){
		this.parseGprsGateClients();
		this.parseBpServer();
		this.parseMonitorServer();
	}

	public final void setBufLength(int bufLength) {
		this.bufLength = bufLength;
	}

	public final void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public final void setHeartInterval(int heartInterval) {
		this.heartInterval = heartInterval;
	}

	public final void setRequestNum(int requestNum) {
		this.requestNum = requestNum;
	}

	public final void setGprsMessageEventHandler(
			GateMessageEventHandler gprsMessageEventHandler) {
		this.gprsMessageEventHandler = gprsMessageEventHandler;
	}

	public final void setGprsMessageEventHandlerId(String gprsMessageEventHandlerId) {
		this.gprsMessageEventHandlerId = gprsMessageEventHandlerId;
	}

	public final void setBpMessageEventHandler(BasicEventHook bpMessageEventHandler) {
		this.bpMessageEventHandler = bpMessageEventHandler;
	}

	public final void setBpMessageEventHandlerId(String bpMessageEventHandlerId) {
		this.bpMessageEventHandlerId = bpMessageEventHandlerId;
	}

	public final List<ClientModule> getGprsClientModules() {
		return gprsClientModules;
	}

	@SuppressWarnings("unchecked")
	public final void setUmsServerAddr(String umsServerAddr) {
		this.umsServerAddr = umsServerAddr;
		Map<String,String> params = this.parseUrlConfig(this.umsServerAddr);
		String ip=null, param = null,gateIpPostFix=null;
		int port = 0;
		try{
			ip = params.get("ip");
			int index = ip.lastIndexOf(".");
			if( index>0 ){
				gateIpPostFix = ip.substring(index+1);
			}
			else{
				gateIpPostFix = ip;
			}
			param = params.get("port");
			if( null == param ){
				log.error("ums-gate config miss port");
			}
			port = Integer.parseInt(param);
		}catch(Exception e){
			log.error("ums client config exception,port="+param,e);
			return;
		}
		
		ClientModule client = new ClientModule();
		client.setModuleType(IModule.MODULE_TYPE_UMS_CLIENT);
		client.setHostIp(ip);
		client.setHostPort(port);

		//开始设置tcpSocketServer的参数.
		param = params.get("name");
		if( null == param ){
			param = "ums-gate-"+gateIpPostFix;
		}
		client.setName(param);
		
		param = params.get("bufLength");
		if( null != param ){
			try{
				bufLength = Integer.parseInt(param);
			}catch(Exception e){
				log.error("bufLength config err:"+param);
			}
		}
		client.setBufLength(bufLength);
		
		IMessageCreator messageCreator = new MessageGateCreator();
		param = params.get("messageCreator");
		if( null != param ){
			//只能定义bean id，从spring取。
			IMessageCreator mc = (IMessageCreator)ApplicationContextUtil.getBean(param);
			if( null == mc ){
				try{
					Class clz = Class.forName(param);
					mc = (IMessageCreator)clz.newInstance();
				}catch(Exception e){}
			}
			if( null != mc ){
				messageCreator = mc;
			}
		}
		client.setMessageCreator(messageCreator);
		
		param = params.get("txfs");
		if( null != param )
			client.setTxfs(param);
		else
			client.setTxfs("01");
		
		param = params.get("timeout");
		if( null != param ){
			try{
				timeout = Integer.parseInt(param);
			}catch(Exception e){}
		}
		client.setTimeout(timeout);
		
		if( null == umsGateEventHandler )
			umsGateEventHandler = (UmsGateEventHandler)ApplicationContextUtil.getBean(umsGateEventHandlerId);
		if( null == umsGateEventHandler ){
			log.error("umsGateEventHandler == null.");
			umsGateEventHandler = new UmsGateEventHandler();
		}
		client.setEventHandler(umsGateEventHandler);

		param = params.get("heartInterval");
		if( null != param ){
			try{
				heartInterval = Integer.parseInt(param);
			}catch(Exception e){}
		}
		client.setHeartInterval(heartInterval);
		
		//还需要设置公共属性
		client.setRequestNum(requestNum);
		client.init();
		this.umsGateModule = client;
	}

	public ClientModule getUmsGateModule() {
		return umsGateModule;
	}

	public void setUmsGateModule(ClientModule umsGateModule) {
		this.umsGateModule = umsGateModule;
	}

	public void setUmsGateEventHandler(UmsGateEventHandler umsGateEventHandler) {
		this.umsGateEventHandler = umsGateEventHandler;
	}

	public void setUmsGateEventHandlerId(String umsGateEventHandlerId) {
		this.umsGateEventHandlerId = umsGateEventHandlerId;
	}
}
