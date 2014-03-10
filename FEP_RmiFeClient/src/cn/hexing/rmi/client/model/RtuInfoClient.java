package cn.hexing.rmi.client.model;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.UUID;

import cn.hexing.fk.rmi.fe.client.IRtuInfoClientObject;
import cn.hexing.fk.rmi.fe.model.RtuCommunicationInfo;
import cn.hexing.fk.rmi.fe.server.IRtuInfoServerObject;
import cn.hexing.rmi.client.view.MainFrame;

public class RtuInfoClient {

	private static RtuInfoClient instance ;
	
	
	private IRtuInfoServerObject serverInfo;
	
	private IRtuInfoClientObject clientInfo;
	
	private String id ;
	
	private String ip;
	
	private int port;
	
	private RtuInfoClient(){
		
	};
	
	public void init(){
		String url = "//"+ip+":"+port+"/SERVER";   
        try {
        	id = UUID.randomUUID().toString();
			serverInfo = (IRtuInfoServerObject) Naming.lookup(url);
			clientInfo = new RtuInfoClientObject();
			serverInfo.register(id, clientInfo);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}   
	}
	
	public RtuCommunicationInfo getRtuState(String rtuId){
		RtuCommunicationInfo info =null;
		try {
			info= serverInfo.pollRtuState(id, rtuId);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return info;
	}
	
	
	public static RtuInfoClient getInstance(){
		if(instance ==null){
			instance = new RtuInfoClient();
		}
		return instance;
	}
	
	private MainFrame mf ;
	public void setMainFrame(MainFrame mf){
		this.mf = mf;
	}
	
	public void logout(){
		try {
			serverInfo.logout(this.id);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void rtuStateChange(RtuCommunicationInfo info) {
		mf.rtuStateChange(info);
	}

	public void newMsgComing(RtuCommunicationInfo info) {
		mf.newMsgComing(info);
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	
}
