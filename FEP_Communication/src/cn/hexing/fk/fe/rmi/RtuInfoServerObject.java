package cn.hexing.fk.fe.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import cn.hexing.fk.rmi.fe.client.IRtuInfoClientObject;
import cn.hexing.fk.rmi.fe.model.RtuCommunicationInfo;
import cn.hexing.fk.rmi.fe.model.RtuCurrentMessage;
import cn.hexing.fk.rmi.fe.model.RtuCurrentWorkState;
import cn.hexing.fk.rmi.fe.server.IRtuInfoServerObject;

/**
 * 
 * @author gaoll
 *
 * @time 2013-9-6 下午02:50:01
 *
 * @info impl
 */
public class RtuInfoServerObject  extends UnicastRemoteObject implements IRtuInfoServerObject {
	
	protected RtuInfoServerObject() throws RemoteException {
		super();
		new Thread(new RtuInfoSendAssist()).start();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -4685060087804809615L;

	/**key-客户端的id,value-用于发送给客户端的对象*/
	private Map<String,IRtuInfoClientObject> clientsMap = new ConcurrentHashMap<String, IRtuInfoClientObject>();
	
	/**key-终端地址,value-终端通信信息*/
	private Map<String,RtuCommunicationInfo> rtusInfoMap=new ConcurrentHashMap<String, RtuCommunicationInfo>();
	
	/**key-终端地址,value-查询改终端的clients. 一个终端可对应多个client*/
	private Map<String,List<String>> rtusClientIdsMap = new ConcurrentHashMap<String, List<String>>();
	
	/**key-id,value-终端地址. 一个client对应一个终端*/
	private Map<String,String> clientIdRtusMap = new ConcurrentHashMap<String, String>();
	
	private BlockingQueue<RtuInfoQueueModel> queue =new  LinkedBlockingQueue<RtuInfoServerObject.RtuInfoQueueModel>();
	
	public enum METHOD{STATECHANGE,MSGSEND};
	
	

	public boolean register(String id, IRtuInfoClientObject client)  throws RemoteException{
		clientsMap.put(id, client);
		return true;
	}



	
	public RtuCommunicationInfo pollRtuState(String id, String rtuId)  throws RemoteException{
		
		if(id == null || id.trim().equals("") || rtuId==null || rtuId.trim().equals("")) return null;
		
		//1.首先判断当前rtusClientsIdsMap是否包含当前终端
		if(!rtusClientIdsMap.containsKey(rtuId)){
			List<String> list = Collections.synchronizedList(new LinkedList<String>());
			rtusClientIdsMap.put(rtuId, list);
		}
		//2.判断当前Client是否包含在当前id里
		List<String> ids = rtusClientIdsMap.get(rtuId);
		if(!ids.contains(ids)){
			//2.1 如果不包含，则去除与别的终端的联系
			if(clientIdRtusMap.containsKey(id)){
				String t_rtuID = clientIdRtusMap.get(id);
				List<String> t_ids = rtusClientIdsMap.get(t_rtuID);
				if(t_ids!=null && t_ids.contains(id)){
					t_ids.remove(id);
				}
			}
		
			
			//2.2 重新与当前终端建立联系
			ids.add(id);
			clientIdRtusMap.put(id, rtuId);
		}
		
		
		//3.获得当前终端的信息，发送
		
		RtuCommunicationInfo info = rtusInfoMap.get(rtuId);
		info.setCurrentTime(System.currentTimeMillis());
		return info;
	}





	public void pushRtuState(RtuCommunicationInfo info) throws RemoteException {
		queue.add(new RtuInfoQueueModel(info,METHOD.STATECHANGE));
	}


	public void setWorkState(RtuCurrentWorkState workState){
		String rtuId = workState.getLogicAddress();
		RtuCommunicationInfo info = rtusInfoMap.get(rtuId);
		if(info == null){
			info = new RtuCommunicationInfo();
		}
		info.setCurrentTime(System.currentTimeMillis());
		info.setRtuId(rtuId);
		info.setState(workState);
		rtusInfoMap.put(rtuId, info);
		try {
			pushRtuState(info);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	public void setCurrentMsg(RtuCurrentMessage curMsg){
		String rtuId = curMsg.getLogicAddress();
		RtuCommunicationInfo info = rtusInfoMap.get(rtuId);
		if(info == null){
			info = new RtuCommunicationInfo();
		}
		
		info.setMsg(curMsg);
		info.setRtuId(rtuId);
		info.setCurrentTime(System.currentTimeMillis());
		rtusInfoMap.put(rtuId, info);
		
		try {
			pushMsg(info);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	

	public void pushMsg(RtuCommunicationInfo info) throws RemoteException {

		queue.add(new RtuInfoQueueModel(info,METHOD.MSGSEND));

	}
	
	private class RtuInfoQueueModel{
		public RtuInfoQueueModel(RtuCommunicationInfo info, METHOD method) {
			this.info = info;
			this.method = method;
		}
		public RtuCommunicationInfo info ;
		public METHOD method;
	}
	
	
	
	private  class RtuInfoSendAssist implements Runnable{

		public void run() {
			while (true) {
				try {
					RtuInfoQueueModel infoModel = queue.take();
					RtuCommunicationInfo info = infoModel.info;
					METHOD method = infoModel.method;
					List<String> list = rtusClientIdsMap.get(info.getRtuId());
					List<String> removeIds = new LinkedList<String>();
					if (list != null && list.size() > 0) {
						for (String id : list) {
							IRtuInfoClientObject client = clientsMap.get(id);
							if (client != null) {
								try {
									switch (method) {
									case MSGSEND:
										client.rtuMsgComing(info);
										break;
									case STATECHANGE:
										client.rtuStateChange(info);
										break;
									}
								} catch (RemoteException e) {
									clientsMap.remove(id);
									removeIds.add(id);
									clientIdRtusMap.remove(id);
								}
							}
						}
						list.removeAll(removeIds);
					}
				}  catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}
	}



	public void logout(String id) throws RemoteException {
		clientsMap.remove(id);
		clientIdRtusMap.remove(id);
	}

}
