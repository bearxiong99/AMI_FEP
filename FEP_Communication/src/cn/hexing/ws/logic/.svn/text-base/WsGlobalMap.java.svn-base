package cn.hexing.ws.logic;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cn.hexing.db.bizprocess.MasterDbService;

/**
 * WsGlobal object used for RTU list setting or updating.
 *
 */
public class WsGlobalMap {
	private static final WsGlobalMap instance = new WsGlobalMap();
	private final Map<Integer,Integer> rtusHeartbeatSave2Db = Collections.synchronizedMap(new HashMap<Integer,Integer>());
	private boolean isSaveHeartBeat=false;//默认不开启报文存储

	private List<String> rtusHeartSave2Db = Collections.synchronizedList(new LinkedList<String>());
	private MasterDbService masterDbService ;
	
	public void init(){
		//从数据库加载需要保存的终端
		rtusHeartSave2Db.addAll(masterDbService.getSaveHeartRtu());
	} 
	
	private WsGlobalMap(){}
	
	public static final WsGlobalMap getInstance(){ return instance; }
	
	public void setRtuHeartbeatSaveFlag(String zdljdz,boolean saveFlag){
		if(saveFlag){
			if(!rtusHeartSave2Db.contains(zdljdz))
				rtusHeartSave2Db.add(zdljdz);			
		}
		else
			rtusHeartSave2Db.remove(zdljdz);
	}
	
	public void setRtuHeartbeatSaveFlag(int rtua,boolean saveFlag){
		if( saveFlag )
			rtusHeartbeatSave2Db.put(rtua, rtua);
		else
			rtusHeartbeatSave2Db.remove(rtua);
	}
	
	public boolean isSaveHeartbeat2Db(String logicAddress){
		return rtusHeartSave2Db.contains(logicAddress) || isSaveHeartBeat;
	}
	
	public boolean isSaveHeartbeat2Db(int rtua){
		return rtusHeartbeatSave2Db.containsKey(rtua);
	}
	public boolean isSaveHeartBeat() {
		return isSaveHeartBeat;
	}
	public void setSaveHeartBeat(boolean isSaveHeartBeat) {
		this.isSaveHeartBeat = isSaveHeartBeat;
	}
	public List<String> getRtusHeartSave2Db() {
		return rtusHeartSave2Db;
	}

	public void setMasterDbService(MasterDbService masterDbService) {
		this.masterDbService = masterDbService;
	}
	
}
