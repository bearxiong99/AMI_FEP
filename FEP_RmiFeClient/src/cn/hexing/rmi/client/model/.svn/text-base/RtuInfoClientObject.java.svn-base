package cn.hexing.rmi.client.model;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import cn.hexing.fk.rmi.fe.client.IRtuInfoClientObject;
import cn.hexing.fk.rmi.fe.model.RtuCommunicationInfo;

public class RtuInfoClientObject extends UnicastRemoteObject implements IRtuInfoClientObject {
	

	public RtuInfoClientObject() throws RemoteException {
		super();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -7644132103763269216L;

	@Override
	public void rtuStateChange(RtuCommunicationInfo info)  throws RemoteException{
		RtuInfoClient.getInstance().rtuStateChange(info);
	}

	@Override
	public void rtuMsgComing(RtuCommunicationInfo info) throws RemoteException {
		RtuInfoClient.getInstance().newMsgComing(info);
	}
	
}
