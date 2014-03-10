package cn.hexing.fk.monitor.client;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import cn.hexing.fk.message.IMessageCreator;
import cn.hexing.fk.monitor.client.biz.FileCommand;
import cn.hexing.fk.monitor.client.biz.ProfileCommand;
import cn.hexing.fk.monitor.client.biz.SystemCommand;
import cn.hexing.fk.monitor.client.biz.TraceRTUCommand;
import cn.hexing.fk.monitor.message.MonitorMessageCreator;
import cn.hexing.fk.sockclient.JSocket;

public class MonitorClient {
	private JSocket socket = new JSocket();
	private String hostIp="127.0.0.1";
	private int hostPort = 10006;
	private int bufLength = 32*1024;		//默认缓冲区长度
	private IMessageCreator messageCreator = new MonitorMessageCreator();
	private int timeout = 2;			//读或者写超时，单位秒
	private MonitorSocketListener listener = new MonitorSocketListener();
	private IMonitorReplyListener replyListener = null;
	
	//命令操作对象定义
	private final ProfileCommand profileCommand = new ProfileCommand();
	private final FileCommand fileCommand = new FileCommand();
	private final TraceRTUCommand traceCommand = new TraceRTUCommand();
	private final SystemCommand sysCommand = new SystemCommand();
	
	public void shutdownApplication(){
		if( !isConnected() )
			return;
		sysCommand.shutdown(socket);
	}
	
	public void cmdListLog(){
		if( ! isConnected() )
			return;
		fileCommand.listLog(socket);
	}
	
	public void cmdListConfig(){
		if( ! isConnected() )
			return;
		fileCommand.listConfig(socket);
	}
	
	public void cmdGetFile(String path){
		if( ! isConnected() )
			return;
		fileCommand.getFile(socket, path);
	}
	
	public void cmdPutFile(String path){
		if( ! isConnected() )
			return;
		fileCommand.putFile(socket, path);
	}
	
	public void cmdGetProfile(){
		if( ! isConnected() )
			return;
		profileCommand.getSystemProfile(socket);
	}
	
	public void cmdGetModuleProfile(){
		if( ! isConnected() )
			return;
		profileCommand.getModuleProfile(socket);
	}
	
	public void cmdGetEventHookProfile(){
		if( ! isConnected() )
			return;
		profileCommand.getEventHookProfile(socket);
	}
	
	public void cmdGatherProfile(){
		if( ! isConnected() )
			return;
		profileCommand.gatherProfile(socket);
	}
	
	public void cmdTraceRTUs(String rtus){
		if( ! isConnected() )
			return;
		String[] result = rtus.split(",");
		ArrayList<Integer> array = new ArrayList<Integer>();
		for(int i=0; i<result.length; i++ ){
			if( result[i].length() == 8 ){
				try{
					int rtua = (int)Long.parseLong(result[i],16);
					array.add(rtua);
				}catch(Exception e){
					final Logger log = Logger.getLogger(MonitorClient.class);
					log.error(e.getLocalizedMessage(),e);
				}
			}
		}
		if( array.size() == 0)
			return;
		int [] rtuParams = new int[array.size()];
		for(int i=0; i<array.size(); i++ )
			rtuParams[i] = array.get(i);
		this.traceCommand.startTrace(socket, rtuParams);
	}
	
	public void cmdAbortTrace(){
		if( ! isConnected() )
			return;
		traceCommand.stopTrace(socket);
	}
	
	public MonitorClient(){
		
	}
	public MonitorClient(String ip,int port){
		hostIp = ip;
		hostPort = port;
	}
	
	public void connect(String ip,int port){
		hostIp = ip;
		hostPort = port;
		init();
	}
	
	public void connect(){
		init();
	}
	
	public void close(){
		socket.close();
	}
	
	public void init(){
		if( null == this.replyListener ){
			final Logger log = Logger.getLogger(MonitorClient.class);
			log.warn("MonitorClient对象必须设置IMonitorReplyListener接口实现.");
			this.replyListener = new MockMonitorReplyListener();
			listener.replyListener= new MockMonitorReplyListener();
		}
		socket.setHostIp(hostIp);
		socket.setHostPort(hostPort);
		socket.setBufLength(bufLength);
		socket.setMessageCreator(messageCreator);
		socket.setListener(listener);
		socket.setTimeout(timeout);
		socket.init();
	}
	
	public JSocket getSocket(){
		return socket;
	}
	
	public boolean isConnected(){
		return socket.isConnected();
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
	public int getTimeout() {
		return timeout;
	}
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	public IMonitorReplyListener getReplyListener() {
		return replyListener;
	}
	public void setReplyListener(IMonitorReplyListener replyListener) {
		this.replyListener = replyListener;
		this.listener.replyListener = this.replyListener;
	}
}
