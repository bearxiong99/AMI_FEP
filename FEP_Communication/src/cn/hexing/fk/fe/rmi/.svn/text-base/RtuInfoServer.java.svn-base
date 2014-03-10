package cn.hexing.fk.fe.rmi;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

import org.apache.log4j.Logger;

import cn.hexing.fk.fe.cluster.WorkState;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.rmi.fe.model.RtuCurrentMessage;
import cn.hexing.fk.rmi.fe.model.RtuCurrentWorkState;
import cn.hexing.fk.utils.StringUtil;

/**
 * 
 * @author gaoll
 *
 * @time 2013-9-6 ÏÂÎç05:06:40
 *
 * @info 
 */
public class RtuInfoServer {

	private static RtuInfoServer instance ;
	
	private String url;
	private static final Logger log = Logger.getLogger(RtuInfoServer.class);

	private RtuInfoServer() {
		
	}
	
	public void init() {
		try {
			serverObject = new RtuInfoServerObject();
			
			String[] els=url.split(":");
			int port = Integer.parseInt(els[1]);
			LocateRegistry.createRegistry(port);
			Naming.rebind("//"+url+"/SERVER" , serverObject);
		} catch (Exception e) {
			log.error(StringUtil.getExceptionDetailInfo(e));
		}  
	}
	
	private RtuInfoServerObject serverObject;
	
	
	public static RtuInfoServer getInstance(){
		
		if(instance == null){
			instance = new RtuInfoServer();
		}
		return instance;
	}
	public void setWorkState(WorkState workState){

		try {
			RtuCurrentWorkState rtuWorkState = new RtuCurrentWorkState();
			rtuWorkState.setLogicAddress(workState.getRtua());
			rtuWorkState.setLastGprsRecvTime(workState.getLastGprsRecvTime());
			rtuWorkState.setLastHeartBeatTime(workState.getLastHeartbeatTime());
			
			serverObject.setWorkState(rtuWorkState);
		} catch (Exception e) {}
	}
	
	public void setCurrentMsg(int dir,IMessage msg){
		try {
			RtuCurrentMessage curMsgInfo = new RtuCurrentMessage();
			
			curMsgInfo.setContent(msg.toString());
			curMsgInfo.setDir(dir);
			curMsgInfo.setLogicAddress(msg.getLogicalAddress());
			curMsgInfo.setTime(msg.getIoTime());
			curMsgInfo.setPeerAddr(msg.getPeerAddr());
			serverObject.setCurrentMsg(curMsgInfo);
		} catch (Exception e) {}
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
}
