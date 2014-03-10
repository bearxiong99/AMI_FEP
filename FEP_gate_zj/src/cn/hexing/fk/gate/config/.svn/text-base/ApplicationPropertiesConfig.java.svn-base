/**
 * 简化系统配置。配置文件为application.properties。
 * 格式：
 * [ip:]port [?参数名1][=参数值1][&参数名2][=参数值2]... 
 * gate.tcp.servers=127.0.0.1:1002?name=gprs-t-31&bufLength=10240&requestNum=500;127.0.0.1:10004?name=gprs-t-32
 * gate.udp.servers=127.0.0.1:1003?name=gprs-u-31;127.0.0.1:1005?name=gprs-u-32
 * 
 */
package cn.hexing.fk.gate.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import cn.hexing.fk.common.events.BasicEventHook;
import cn.hexing.fk.common.spi.socket.IClientIO;
import cn.hexing.fk.common.spi.socket.abstra.BaseSocketServer;
import cn.hexing.fk.gate.CSDMessageQueue;
import cn.hexing.fk.gate.event.CSDServerEventHandler;
import cn.hexing.fk.gate.event.GateExRTUEventHandler;
import cn.hexing.fk.gate.event.GateFEEventHandler;
import cn.hexing.fk.gate.event.GateRTUEventHandler;
import cn.hexing.fk.message.IMessageCreator;
import cn.hexing.fk.message.MultiProtoRecognizer;
import cn.hexing.fk.message.gate.MessageGateCreator;
import cn.hexing.fk.message.msgbytes.MessageBytesCreator;
import cn.hexing.fk.sockserver.SyncTcpServer;
import cn.hexing.fk.sockserver.SyncUdpServer;
import cn.hexing.fk.sockserver.TcpSocketServer;
import cn.hexing.fk.sockserver.io.SimpleIoHandler;
import cn.hexing.fk.sockserver.io.SimpleUdpIoHandler;
import cn.hexing.fk.utils.ApplicationContextUtil;

import com.hx.dlms.message.DlmsHdlcMessageCreator;

/**
 */
public class ApplicationPropertiesConfig {
	private static final Logger log = Logger.getLogger(ApplicationPropertiesConfig.class);
	private static final ApplicationPropertiesConfig config = new ApplicationPropertiesConfig();
	
	private String gateTcpServers;
	private String gateUdpServers;
	private String gateExTcpServers;
	private String gateCSDTcpServers;
	private String feServer;
	private String monitorServer;
	private String csdClientClass="cn.hexing.fk.sockclient.csd.CSDSyncTcpSocketClient";
	
	
	private GateFEEventHandler feServerEventHandler;
	private String feServerEventHandlerId = "gate.event.handler.fe";
	private GateRTUEventHandler rtuServerEventHandler;
	private String rtuServerEventHandlerId = "gate.event.handler.rtu";
	private CSDServerEventHandler csdServerEventHandler;
	private String csdServerEventHandlerId = "gate.event.handler.csd";
	private GateExRTUEventHandler exRtuServerEventHandler;
	private String exRtuServerEventHandlerId="gate.ex.event.handle.rtu";
	private BasicEventHook monitorEventHandler;
	private String monitorEventHandlerId = "monitor.event.handler";
	private String monitorMessageCreator = "messageCreator.Monitor";

	private List<BaseSocketServer> socketServers = new ArrayList<BaseSocketServer>();
	private List<BaseSocketServer> exSocketServers = new ArrayList<BaseSocketServer>();
	private List<BaseSocketServer> csdSocketServers = new ArrayList<BaseSocketServer>();
	
	private List<BasicEventHook> eventHandlers = new ArrayList<BasicEventHook>();
	private List<BasicEventHook> exEventHandlers = new ArrayList<BasicEventHook>();
	private List<BasicEventHook> csdEventHandlers = new ArrayList<BasicEventHook>();
	//对于SocketServer来说，需要配置的默认属性定义如下
	//1.name属性，如果没有配置，则按照gprs-ip(最后一段）-port-t(or-u);
	//2.bufLength 512 对于终端,10240对于其它
	//3.requestNum 500
	//4.

	private ApplicationPropertiesConfig(){}
	
	public static final ApplicationPropertiesConfig getInstance(){
		return config;
	}
	
	public void setGateTcpServers(String gateTcpServers) {
		this.gateTcpServers = gateTcpServers.trim();
	}
	
	private void parseGateTcpServers() {
		if( null == gateTcpServers || gateTcpServers.length()<2 )
			return;
		String[] urls = this.gateTcpServers.split(";");
		for(String url: urls ){
			Map<String,String> result = parseUrlConfig(url);
			int port = 0, bufLength = 1035;
			String param = null;
			try{
				param = result.get("port");
				if( null == param ){
					log.error("TCP Socket Server config miss port");
					continue;
				}
				port = Integer.parseInt(param);
			}catch(Exception e){
				log.error("TCP Socket Server config exception,port="+param,e);
				continue;
			}
			TcpSocketServer gateServer = new TcpSocketServer();
			gateServer.setPort(port);
			param = result.get("ip");
			if( null != param )
				gateServer.setIp(param);
			//开始设置tcpSocketServer的参数.
			param = result.get("name");
			if( null == param ){
				param = "gprs-"+port+"-t";
			}
			gateServer.setName(param);
			
			param = result.get("bufLength");
			if( null != param ){
				try{
					bufLength = Integer.parseInt(param);
				}catch(Exception e){
					log.error("bufLength config err:"+param);
				}
			}
			gateServer.setBufLength(bufLength);
			
			param = result.get("useDirectBuffer");
			if( null != param ){
				try{
					boolean useDirect = Boolean.parseBoolean(param);
					gateServer.setUseDirectBuffer(useDirect);
				}catch(Exception exp){
					log.error("convert useDirectBuffer exp:"+exp.getLocalizedMessage());
				}
			}
			
			int ioThreadSize = 2;
			param = result.get("ioThreadSize");
			if( null != param ){
				try{
					ioThreadSize = Integer.parseInt(param);
					gateServer.setIoThreadSize(ioThreadSize);
				}catch(Exception e){}
			}
			IMessageCreator messageCreator = new MultiProtoRecognizer();
			param = result.get("messageCreator");
			if( null != param ){
				//只能定义bean id，从spring取。
				IMessageCreator mc = (IMessageCreator)ApplicationContextUtil.getBean(param);
				if( null == mc ){
					try{
						Class<?> clz = Class.forName(param);
						mc = (IMessageCreator)clz.newInstance();
					}catch(Exception e){}
				}
				if( null != mc ){
					messageCreator = mc;
				}
			}
			gateServer.setMessageCreator(messageCreator);
			
			IClientIO ioHandler = new SimpleIoHandler();
			param = result.get("ioHandler");
			if( null != param ){
				IClientIO ioh = (IClientIO)ApplicationContextUtil.getBean(param);
				if( null == ioh ){
					try{
						Class<?> clz = Class.forName(param);
						ioh = (IClientIO)clz.newInstance();
					}catch(Exception e){}
				}
				if( null != ioh )
					ioHandler = ioh;
			}
			gateServer.setIoHandler(ioHandler);
			
			param = result.get("txfs");
			if( null != param )
				gateServer.setTxfs(param);
			
			param = result.get("timeout");
			int timeout = 30*60;
			if( null != param ){
				try{
					timeout = Integer.parseInt(param);
				}catch(Exception e){}
			}
			gateServer.setTimeout(timeout);
			
			socketServers.add(gateServer);
		}
		if( null == rtuServerEventHandler )
			rtuServerEventHandler = (GateRTUEventHandler)ApplicationContextUtil.getBean(rtuServerEventHandlerId);
		if( null != rtuServerEventHandler ){
			rtuServerEventHandler.addSource(socketServers.toArray());
			boolean found = false;
			for(BasicEventHook handler: eventHandlers ){
				if( handler == rtuServerEventHandler ){
					found = true;
					break;
				}
			}
			if( !found )
				eventHandlers.add(rtuServerEventHandler);
		}
	}
	
	private void parseGateCSDServers() {
		if( null == gateCSDTcpServers || gateCSDTcpServers.length()<2)
			return ;
		String[] urls = this.gateCSDTcpServers.split(";");
		for(String url:urls){

			Map<String,String> result = parseUrlConfig(url);
			int port = 0, bufLength = 1035;
			String param = null;
			try{
				param = result.get("port");
				if( null == param ){
					log.error("TCP Socket Server config miss port");
					continue;
				}
				port = Integer.parseInt(param);
			}catch(Exception e){
				log.error("TCP Socket Server config exception,port="+param,e);
				continue;
			}
			SyncTcpServer gateServer = new SyncTcpServer();
			gateServer.setClientClass(csdClientClass);
			gateServer.setPort(port);
			param = result.get("ip");
			if( null != param )
				gateServer.setIp(param);
			//开始设置tcpSocketServer的参数.
			param = result.get("name");
			if( null == param ){
				param = "gprs-"+port+"-t";
			}
			gateServer.setName(param);
			
			param = result.get("pendingTime");
			if( null !=param){
				try {
					gateServer.setPendingTime(Integer.parseInt(param));
				} catch (NumberFormatException e) {}
			}
			param = result.get("bufLength");
			if( null != param ){
				try{
					bufLength = Integer.parseInt(param);
				}catch(Exception e){
					log.error("bufLength config err:"+param);
				}
			}
			gateServer.setBufLength(bufLength);
			
			param = result.get("useDirectBuffer");
			if( null != param ){
				try{
					boolean useDirect = Boolean.parseBoolean(param);
					gateServer.setUseDirectBuffer(useDirect);
				}catch(Exception exp){
					log.error("convert useDirectBuffer exp:"+exp.getLocalizedMessage());
				}
			}
			
			int ioThreadSize = 2;
			param = result.get("ioThreadSize");
			if( null != param ){
				try{
					ioThreadSize = Integer.parseInt(param);
					gateServer.setIoThreadSize(ioThreadSize);
				}catch(Exception e){}
			}
			IMessageCreator messageCreator = new MessageBytesCreator(); //这地方要改
			param = result.get("messageCreator");
			if( null != param ){
				//只能定义bean id，从spring取。
				IMessageCreator mc = (IMessageCreator)ApplicationContextUtil.getBean(param);
				if( null == mc ){
					try{
						Class<?> clz = Class.forName(param);
						mc = (IMessageCreator)clz.newInstance();
					}catch(Exception e){}
				}
				if( null != mc ){
					messageCreator = mc;
				}
			}
			gateServer.setMessageCreator(messageCreator);
			
			IClientIO ioHandler = new SimpleIoHandler();
			param = result.get("ioHandler");
			if( null != param ){
				IClientIO ioh = (IClientIO)ApplicationContextUtil.getBean(param);
				if( null == ioh ){
					try{
						Class<?> clz = Class.forName(param);
						ioh = (IClientIO)clz.newInstance();
					}catch(Exception e){}
				}
				if( null != ioh )
					ioHandler = ioh;
			}
			gateServer.setIoHandler(ioHandler);
			csdServerEventHandler.addSource(gateServer);
			param = result.get("txfs");
			if( null != param )
				gateServer.setTxfs(param);
			
			param = result.get("timeout");
			int timeout = 30*60;
			if( null != param ){
				try{
					timeout = Integer.parseInt(param);
				}catch(Exception e){}
			}
			gateServer.setTimeout(timeout);
			
			csdSocketServers.add(gateServer);
		}
		if( null == csdServerEventHandler )
			csdServerEventHandler = (CSDServerEventHandler)ApplicationContextUtil.getBean(csdServerEventHandlerId);
		if( null != csdServerEventHandler ){
			csdServerEventHandler.addSource(csdSocketServers.toArray());
			boolean found = false;
			for(BasicEventHook handler: csdEventHandlers ){
				if( handler == csdServerEventHandler ){
					found = true;
					break;
				}
			}
			if( !found )
				csdEventHandlers.add(csdServerEventHandler);
		}
		CSDMessageQueue.getInstance();
		
	}
	
	private void parseGateExTcpServers() {
		if( null == gateExTcpServers || gateExTcpServers.length()<2 )
			return;
		String[] urls = this.gateExTcpServers.split(";");
		for(String url: urls ){
			Map<String,String> result = parseUrlConfig(url);
			int port = 0, bufLength = 1035;
			String param = null;
			try{
				param = result.get("port");
				if( null == param ){
					log.error("TCP Socket Server config miss port");
					continue;
				}
				port = Integer.parseInt(param);
			}catch(Exception e){
				log.error("TCP Socket Server config exception,port="+param,e);
				continue;
			}
			TcpSocketServer gateServer = new TcpSocketServer();
			gateServer.setPort(port);
			gateServer.setClientClass("cn.hexing.fk.sockserver.AsyncExHdlcDlmsSocketClient");
			param = result.get("ip");
			if( null != param )
				gateServer.setIp(param);
			//开始设置tcpSocketServer的参数.
			param = result.get("name");
			if( null == param ){
				param = "gprs-"+port+"-t";
			}
			gateServer.setName(param);
			
			param = result.get("bufLength");
			if( null != param ){
				try{
					bufLength = Integer.parseInt(param);
				}catch(Exception e){
					log.error("bufLength config err:"+param);
				}
			}
			gateServer.setBufLength(bufLength);
			
			param = result.get("autoHeart");
			boolean  autoHeart = false;
			if(null!=param){
				try {
					autoHeart=Boolean.parseBoolean(param);
				} catch (Exception e) {
					log.error("autoHeart config err:"+param);
				}
			}
			gateServer.setAutoHearBeart(autoHeart);
			param = result.get("useDirectBuffer");
			if( null != param ){
				try{
					boolean useDirect = Boolean.parseBoolean(param);
					gateServer.setUseDirectBuffer(useDirect);
				}catch(Exception exp){
					log.error("convert useDirectBuffer exp:"+exp.getLocalizedMessage());
				}
			}
			
			int ioThreadSize = 2;
			param = result.get("ioThreadSize");
			if( null != param ){
				try{
					ioThreadSize = Integer.parseInt(param);
					gateServer.setIoThreadSize(ioThreadSize);
				}catch(Exception e){}
			}
			gateServer.setMessageCreator(new DlmsHdlcMessageCreator());
			
			IClientIO ioHandler = new SimpleIoHandler();
			param = result.get("ioHandler");
			if( null != param ){
				IClientIO ioh = (IClientIO)ApplicationContextUtil.getBean(param);
				if( null == ioh ){
					try{
						Class<?> clz = Class.forName(param);
						ioh = (IClientIO)clz.newInstance();
					}catch(Exception e){}
				}
				if( null != ioh )
					ioHandler = ioh;
			}
			gateServer.setIoHandler(ioHandler);
			param=result.get("heartInterval");
			if(null != param){
				System.setProperty("heartInterval", param);
			}
			
			param=result.get("overtime");
			if(null != param){
				System.setProperty("overtime",param);
			}
			
			param = result.get("txfs");
			if( null != param )
				gateServer.setTxfs(param);
			
			param = result.get("timeout");
			int timeout = 30*60;
			if( null != param ){
				try{
					timeout = Integer.parseInt(param);
				}catch(Exception e){}
			}
			gateServer.setTimeout(timeout);
			
			exSocketServers.add(gateServer);
		}
		if( null == exRtuServerEventHandler )
			exRtuServerEventHandler = (GateExRTUEventHandler)ApplicationContextUtil.getBean(exRtuServerEventHandlerId);
		if( null != exRtuServerEventHandler ){
			exRtuServerEventHandler.addSource(exSocketServers.toArray());
			boolean found = false;
			for(BasicEventHook handler: exEventHandlers ){
				if( handler == exRtuServerEventHandler ){
					found = true;
					break;
				}
			}
			if( !found )
				exEventHandlers.add(exRtuServerEventHandler);
		}
	}
	public void setGateUdpServers(String gateUdpServers) {
		this.gateUdpServers = gateUdpServers.trim();
	}
	
	private void parseGateUdpServers() {
		if( null == gateUdpServers || gateUdpServers.length() <2 )
			return;
		String[] urls = this.gateUdpServers.split(";");
		for(String url: urls ){
			Map<String,String> result = parseUrlConfig(url);
			int port = 0, bufLength = 1024;
			String param = null;
			try{
				param = result.get("port");
				if( null == param ){
					log.error("UDP Socket Server config miss port");
					continue;
				}
				port = Integer.parseInt(param);
			}catch(Exception e){
				log.error("UDP Socket Server config exception,port="+param,e);
				continue;
			}
			SyncUdpServer gateServer = new SyncUdpServer();
			gateServer.setPort(port);
			param = result.get("ip");
			if( null != param )
				gateServer.setIp(param);
			//开始设置tcpSocketServer的参数.
			param = result.get("name");
			if( null == param ){
				param = "gprs-"+port+"-u";
			}
			gateServer.setName(param);
			
			param = result.get("bufLength");
			if( null != param ){
				try{
					bufLength = Integer.parseInt(param);
				}catch(Exception e){
					log.error("bufLength config err:"+param);
				}
			}
			gateServer.setBufLength(bufLength);

			IMessageCreator messageCreator = new MultiProtoRecognizer();
			param = result.get("messageCreator");
			if( null != param ){
				//只能定义bean id，从spring取。
				IMessageCreator mc = (IMessageCreator)ApplicationContextUtil.getBean(param);
				if( null == mc ){
					try{
						Class<?> clz = Class.forName(param);
						mc = (IMessageCreator)clz.newInstance();
					}catch(Exception e){}
				}
				if( null != mc ){
					messageCreator = mc;
				}
			}
			gateServer.setMessageCreator(messageCreator);
			
			IClientIO ioHandler = new SimpleUdpIoHandler();
			param = result.get("ioHandler");
			if( null != param ){
				IClientIO ioh = (IClientIO)ApplicationContextUtil.getBean(param);
				if( null == ioh ){
					try{
						Class<?> clz = Class.forName(param);
						ioh = (IClientIO)clz.newInstance();
					}catch(Exception e){}
				}
				if( null != ioh )
					ioHandler = ioh;
			}
			gateServer.setIoHandler(ioHandler);
			
			param = result.get("txfs");
			if( null != param )
				gateServer.setTxfs(param);
			
			param = result.get("timeout");
			int timeout = 30*60;
			if( null != param ){
				try{
					timeout = Integer.parseInt(param);
				}catch(Exception e){}
			}
			gateServer.setTimeout(timeout);
			
			socketServers.add(gateServer);
		}
		if( null == rtuServerEventHandler )
			rtuServerEventHandler = (GateRTUEventHandler)ApplicationContextUtil.getBean(rtuServerEventHandlerId);
		if( null != rtuServerEventHandler ){
			rtuServerEventHandler.addSource(socketServers.toArray());
			boolean found = false;
			for(BasicEventHook handler: eventHandlers ){
				if( handler == rtuServerEventHandler ){
					found = true;
					break;
				}
			}
			if( !found )
				eventHandlers.add(rtuServerEventHandler);
		}
	}

	public void setFeServer(String feServers) {
		this.feServer = feServers.trim();
	}
	
	private void parseFeServer() {
		Map<String,String> result = parseUrlConfig(this.feServer);
		int port = 0, bufLength = 10240;
		String param = null;
		try{
			param = result.get("port");
			if( null == param ){
				log.error("Front End TCP Socket Server config miss port");
				return;
			}
			port = Integer.parseInt(param);
		}catch(Exception e){
			log.error("Front End TCP Socket Server config exception,port="+param,e);
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
			param = "fe-"+port;
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
			IMessageCreator mc = (IMessageCreator)ApplicationContextUtil.getBean(param);
			if( null == mc ){
				try{
					Class<?> clz = Class.forName(param);
					mc = (IMessageCreator)clz.newInstance();
				}catch(Exception e){}
			}
			if( null != mc ){
				messageCreator = mc;
			}
		}
		socketServer.setMessageCreator(messageCreator);
		
		IClientIO ioHandler = new SimpleIoHandler();
		param = result.get("ioHandler");
		if( null != param ){
			IClientIO ioh = (IClientIO)ApplicationContextUtil.getBean(param);
			if( null == ioh ){
				try{
					Class<?> clz = Class.forName(param);
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
		
		param = result.get("requestNum");
//		int reqNum = -1;
//		if( null != param ){
//			try{
//				reqNum = Integer.parseInt(param);
//			}catch(Exception e){}
//		}
//		socketServer.setRequestNum(reqNum);

		socketServers.add(socketServer);
		if( null == feServerEventHandler ){
			feServerEventHandler = (GateFEEventHandler)ApplicationContextUtil.getBean(feServerEventHandlerId);
		}
		feServerEventHandler.setSource(socketServer);
		eventHandlers.add(feServerEventHandler);
	}

	public void setMonitorServer(String monitorServers) {
		this.monitorServer = monitorServers.trim();
	}
	
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
			IMessageCreator mc = (IMessageCreator)ApplicationContextUtil.getBean(param);
			if( null == mc ){
				try{
					Class<?> clz = Class.forName(param);
					mc = (IMessageCreator)clz.newInstance();
				}catch(Exception e){}
			}
			if( null != mc ){
				messageCreator = mc;
			}
		}
		mServer.setMessageCreator(messageCreator);
		
		IClientIO ioHandler = new SimpleIoHandler();
		param = result.get("ioHandler");
		if( null != param ){
			IClientIO ioh = (IClientIO)ApplicationContextUtil.getBean(param);
			if( null == ioh ){
				try{
					Class<?> clz = Class.forName(param);
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
			hostAddr = url.substring(0, index).trim();
			params = url.substring(index+1).trim();
		}
		else{
			hostAddr = url.trim();
		}
		if( null != hostAddr ){
			index = hostAddr.indexOf(":");
			if( index>0 ){
				String ip = hostAddr.substring(0, index).trim();
				String port = hostAddr.substring(index+1).trim();
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
					String name = param.substring(0, index).trim();
					String value = param.substring(index+1).trim();
					result.put(name, value);
				}
			}
		}
		return result;
	}

	public void setFeServerEventHandler(GateFEEventHandler feServerEventHandler) {
		this.feServerEventHandler = feServerEventHandler;
	}

	public void setFeServerEventHandlerId(String feServerEventHandlerId) {
		this.feServerEventHandlerId = feServerEventHandlerId;
	}

	public void setRtuServerEventHandler(GateRTUEventHandler rtuServerEventHandler) {
		this.rtuServerEventHandler = rtuServerEventHandler;
	}

	public void setRtuServerEventHandlerId(String rtuServerEventHandlerId) {
		this.rtuServerEventHandlerId = rtuServerEventHandlerId;
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
		this.parseGateTcpServers();
		this.parseGateUdpServers();
		this.parseGateExTcpServers();
		this.parseGateCSDServers();
		this.parseFeServer();
		this.parseMonitorServer();
	}


	public final String getGateExTcpServers() {
		return gateExTcpServers;
	}

	public final void setGateExTcpServers(String gateExTcpServers) {
		this.gateExTcpServers = gateExTcpServers;
	}

	public final GateExRTUEventHandler getExRtuServerEventHandler() {
		return exRtuServerEventHandler;
	}

	public final void setExRtuServerEventHandler(
			GateExRTUEventHandler exRtuServerEventHandler) {
		this.exRtuServerEventHandler = exRtuServerEventHandler;
	}

	public List<BasicEventHook> getExEventHandlers() {
		return exEventHandlers;
	}

	public void setExEventHandlers(List<BasicEventHook> exEventHandlers) {
		this.exEventHandlers = exEventHandlers;
	}

	public List<BaseSocketServer> getExSocketServers() {
		return exSocketServers;
	}

	public void setExSocketServers(List<BaseSocketServer> exSocketServers) {
		this.exSocketServers = exSocketServers;
	}

	public String getGateCSDTcpServers() {
		return gateCSDTcpServers;
	}

	public void setGateCSDTcpServers(String gateCSDTcpServers) {
		this.gateCSDTcpServers = gateCSDTcpServers;
	}

	public String getCsdServerEventHandlerId() {
		return csdServerEventHandlerId;
	}

	public void setCsdServerEventHandlerId(String csdServerEventHandlerId) {
		this.csdServerEventHandlerId = csdServerEventHandlerId;
	}

	public CSDServerEventHandler getCsdServerEventHandler() {
		return csdServerEventHandler;
	}

	public void setCsdServerEventHandler(CSDServerEventHandler csdServerEventHandler) {
		this.csdServerEventHandler = csdServerEventHandler;
	}

	public List<BasicEventHook> getCsdEventHandlers() {
		return csdEventHandlers;
	}

	public void setCsdEventHandlers(List<BasicEventHook> csdEventHandlers) {
		this.csdEventHandlers = csdEventHandlers;
	}

	public List<BaseSocketServer> getCsdSocketServers() {
		return csdSocketServers;
	}

	public void setCsdSocketServers(List<BaseSocketServer> csdSocketServers) {
		this.csdSocketServers = csdSocketServers;
	}
}
