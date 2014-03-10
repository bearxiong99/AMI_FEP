package cn.hexing.fk.clientmod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import cn.hexing.fk.common.spi.IEventHandler;
import cn.hexing.fk.common.spi.IModule;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.IMessageCreator;
import cn.hexing.fk.message.gate.MessageGateCreator;
import cn.hexing.fk.utils.ApplicationContextUtil;

/**
 * Support clustered-clients function.
 * Group supporting: 
 *
 */
public class ClusterClientModule implements IModule {
	private static final Logger log = Logger.getLogger(ClusterClientModule.class);
	//可配置属性
	private String name = "集群客户端";
	private String moduleType = IModule.MODULE_TYPE_SOCKET_CLIENT;
	private int bufLength = 10240;		//默认缓冲区长度
	private IMessageCreator messageCreator = new MessageGateCreator();
	private int timeout = 2;			//读或者写超时，单位秒
	private IEventHandler eventHandler = new ClusterClientDefaultHandler();	//通过spring配置事件处理器，例如GateMessageEventHandler
	//心跳管理
	private int heartInterval = 0;		//心跳间隔(秒)。如果间隔时间内无上行报文，则发送心跳。0表示取消心跳。
	private int requestNum = 200;		//客户端向服务器请求接收报文数量。-1表示请求服务器永远异步推送报文
	
	/**
	 * 分组集群客户端：通过终端用途以及单位代码进行定位。
	 */
	private List<ClusterClientItem> clients = new ArrayList<ClusterClientItem>();
	private Map<String,List<ClusterClientItem>> mapGroupClients = new HashMap<String,List<ClusterClientItem>>();
	/**
	 * 所有属性设置完毕后，需要初始化。
	 */
	public void init(){
		if( mapGroupClients.size()>0 ){
			//load from properties file.
			return;
		}
		int cnt = 0;
		for(ClusterClientItem item : clients ){
			cnt++;
			item.getClient().setBufLength(bufLength);
			item.getClient().setEventHandler(eventHandler);
			item.getClient().setHeartInterval(heartInterval);
			item.getClient().setMessageCreator(messageCreator);
			item.getClient().setRequestNum(requestNum);
			item.getClient().setName(name+":"+cnt);
			item.getClient().setTimeout(timeout);
			item.getClient().setModuleType(moduleType);
			item.getClient().init();
			List<ClusterClientItem> list = mapGroupClients.get(item.getUsage());
			if( null == list ){
				list = new ArrayList<ClusterClientItem>();
				mapGroupClients.put(item.getUsage(), list);
			}
			list.add(item);
		}
	}
	
	public boolean sendMessage(IMessage msg){
		return sendMessage(null,null,msg);
	}
	
	public boolean sendMessage(String rtuUsage, String dwdm, IMessage msg){
		if( null == rtuUsage )
			rtuUsage = "any";
		List<ClusterClientItem> list = mapGroupClients.get(rtuUsage);
		if( null == list ){
			StringBuilder sb = new StringBuilder();
			sb.append("clients usages:");
			for(String str: mapGroupClients.keySet() ){
				sb.append(str).append(",");
			}
			log.error("RTU usage="+ rtuUsage+",dwdm="+dwdm+", can not find and client. "+sb.toString());
			return false;
		}

		List<ClusterClientItem> availableClients = new LinkedList<ClusterClientItem>();
		for(ClusterClientItem item : list){
			if( ( null == dwdm || null==item.getDeptCode() || item.getDeptCode().indexOf(dwdm)>=0 ) && item.getClient().isActive() ){
				int pos = 0;
				for(int i=0; i<availableClients.size(); i++ ){
					if( item.count< availableClients.get(i).count )
						break;
					pos = i;
				}
				availableClients.add(pos, item);
			}
		}
		if( availableClients.size() == 0 ){
			log.warn("Cann't find any client to send. required USAGE="+rtuUsage+",DWDM="+dwdm);
			if( log.isDebugEnabled() ){
				log.debug("available list: " + list.toString());
			}
			return false;
		}
		for(ClusterClientItem item : availableClients){
			boolean ret = item.getClient().sendMessage(msg);
			if( ret ){
				item.count++;
				return true;
			}
			log.warn("Send Failed. item="+item);
		}
		return false;
	}
	
	public boolean sendRequest(String rtuUsage, String dwdm, Object req){
		FaalRequestMessage msg = new FaalRequestMessage(req);
		return sendMessage(rtuUsage,dwdm,msg);
	}
	
	public void broadcastBP(Object req){
		for(ClusterClientItem item : clients ){
			FaalRequestMessage msg = new FaalRequestMessage(req);
			try{
				item.getClient().sendMessage(msg);
				log.info("send message. "+item.getClient().getHostIp()+"@"+item.getClient().getHostPort());
			}catch(Exception exp){}
		}
	}
	
	public String getModuleType() {
		return moduleType;
	}
	public String getName() {
		return name;
	}
	public String getTxfs() {
		return "";
	}
	public boolean isActive() {
		int cnt = 0;
		for(ClusterClientItem item : clients ){
			if( item.getClient().isActive() )
				cnt++;
		}
		log.info("Active Connections count="+cnt);
		return cnt>0;
	}
	
	public boolean start() {
		//application.properties process. fixed property is "cluster.clients.url"
		init();
		for(ClusterClientItem item : clients ){
			item.getClient().start();
		}
		return true;
	}
	public void stop() {
		for(ClusterClientItem item : clients ){
			item.getClient().stop();
		}
	}
	
	public void restart() {
		for(ClusterClientItem item : clients ){
			item.getClient().restart();
		}
	}
	public long getLastReceiveTime() {
		return 0;
	}
	public long getLastSendTime() {
		return 0;
	}
	public int getMsgRecvPerMinute() {
		return 0;
	}
	public int getMsgSendPerMinute() {
		return 0;
	}
	public long getTotalRecvMessages() {
		return 0;
	}
	public long getTotalSendMessages() {
		return 0;
	}
	public String profile() {
		return "";
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setBufLength(int bufLength) {
		this.bufLength = bufLength;
	}

	public void setMessageCreator(IMessageCreator messageCreator) {
		this.messageCreator = messageCreator;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public void setEventHandler(IEventHandler eventHandler) {
		this.eventHandler = eventHandler;
	}

	public void setHeartInterval(int heartInterval) {
		this.heartInterval = heartInterval;
	}

	public void setRequestNum(int requestNum) {
		this.requestNum = requestNum;
	}

	public void setClients(List<ClusterClientItem> clients) {
		this.clients = clients;
	}

	public void setClientsUrl(String clientsUrl){
		clients.clear();
		String[] urls = clientsUrl.split(";");
		for(String url: urls ){
			Map<String,String> result = parseUrlConfig(url);
			String ip=null, param = null;
			int port = 0;
			try{
				ip = result.get("ip");
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
			ClusterClientItem cci = new ClusterClientItem();
			cci.setHostIp(ip);
			cci.setHostPort(port);
			
			param = result.get("usage");
			if( null != param )
				cci.setUsage(param);
			param = result.get("deptCode");
			if( null != param )
				cci.setDeptCode(param);
			//public parameters setting
			param = result.get("bufLength");
			if( null != param ){
				try{
					bufLength = Integer.parseInt(param);
				}catch(Exception e){
					log.error("bufLength config err:"+param);
				}
			}
			param = result.get("eventHandler");
			if( null != param ){
				//只能定义bean id，从spring取。
				try{
					IEventHandler eh = (IEventHandler)ApplicationContextUtil.getBean(param);
					if( null == eh ){
						Class<?> clz = Class.forName(param);
						eh = (IEventHandler)clz.newInstance();
					}
					if( null != eh ){
						eventHandler = eh;
					}
				}catch(Exception e){
					log.warn("cluster client eventHandler not found:"+param);
				}
			}
			param = result.get("messageCreator");
			if( null != param ){
				//只能定义bean id，从spring取。
				try{
					IMessageCreator mc = (IMessageCreator)ApplicationContextUtil.getBean(param);
					if( null == mc ){
						Class<?> clz = Class.forName(param);
						mc = (IMessageCreator)clz.newInstance();
					}
					if( null != mc ){
						messageCreator = mc;
					}
				}catch(Exception e){
					log.warn("cluster client messageCreator not found:"+param);
				}
			}
			param = result.get("timeout");
			if( null != param ){
				try{
					timeout = Integer.parseInt(param);
				}catch(Exception e){}
			}
			param = result.get("heartInterval");
			if( null != param ){
				try{
					heartInterval = Integer.parseInt(param);
				}catch(Exception e){}
			}
			
			clients.add(cci);
		}
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
}
