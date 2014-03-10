package cn.hexing.fk.fe.cluster;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.JChannel;
import org.jgroups.View;
import org.jgroups.blocks.ReplicatedHashMap;
import org.jgroups.blocks.ReplicatedHashMap.Notification;
import org.springframework.util.StringUtils;

import cn.hexing.db.batch.AsyncService;
import cn.hexing.db.bizprocess.MasterDbService;
import cn.hexing.db.managertu.ManageRtu;
import cn.hexing.fk.FasSystem;
import cn.hexing.fk.common.spi.IModule;
import cn.hexing.fk.common.spi.abstra.BaseModule;
import cn.hexing.fk.fe.ChannelManage;
import cn.hexing.fk.model.ComRtu;
import cn.hexing.fk.model.RtuSynchronizeItem;

/**
 * Realtime synchronize for RTU's gate-relationship; RTU's mobile-change and RTU's ums-channel change.
 *
 */
public class RealtimeSynchronizer extends BaseModule{
	private static final Logger log = Logger.getLogger(RealtimeSynchronizer.class);
	private static final RealtimeSynchronizer instance = new RealtimeSynchronizer();
	//configurable properties
	private String jchannelConfig = "tcp.xml";
	private String groupName = "FE.sync.realtime";
	private String addressName = null;
	private String paramStateCache = "data/param-state-cache.data";
	private AsyncService dbService = null;
	private int updateCommStateKey = 6001;
	private int updateCommFlowmeterKey = 6002;
	
	//private properties
	private Channel channel = null;
	private ReplicatedHashMap<String,RtuState> states = null;
	
	/** 最近一次刷新档案时间 */
	private Calendar lastRefreshTime = Calendar.getInstance();
	private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private MasterDbService masterDbService; // spring 配置实现。
	
	static {
		//检测是否存在data目录
		try{
			File file = new File("data");
			file.mkdirs();
		}catch(Exception exp){
			log.error(exp);
		}
	}

	private RealtimeSynchronizer(){}
	public static final RealtimeSynchronizer getInstance(){ return instance; }
	
	public RtuState getRtuState(String logicalAddr){
		return states.get(logicalAddr);
	}

	/**
	 * get state object from cache. Create it if not present in cache.
	 * @param rtua
	 * @return
	 */
	public RtuState getRtuStateCreate(String logicalAddr){
		RtuState state = states.get(logicalAddr);
		return null == state ? new RtuState(logicalAddr) : state;
	}
	
	public RtuState loadFromDb(String logicalAddr){
		if(null==logicalAddr || "".equals(logicalAddr)) return null;
		RtuState state = states.get(logicalAddr);
		if( null == state || "".equals(state.getDwdm())){
			log.debug("Terminal not in Catch，need ReloadDoc rtua=" + logicalAddr );
			ComRtu param = ManageRtu.getInstance().reloadComRtu(logicalAddr) ;
			if( null == param ){
				log.error("ReloadDoc Failed RTUA=" + logicalAddr +",Reconstruct a State.");
				if(state==null){
					state = new RtuState(logicalAddr);
				}
			}
			else{
				if(state==null)
					state = new RtuState(logicalAddr);
				state.setUsage(param.getRtuType());
				state.setSimNum(param.getSimNum());
				state.setDwdm(param.getDeptCode());
				state.setTerminalType(param.getTerminalType());
				state.setCommunicationMode(param.getCommunicationMode());
				String ums = ChannelManage.getUmsAppId(param);
				if ( ums != null ){
					int index = ums.indexOf("95598");
					if ( index >= 0 )
						ums=ums.substring(index+5);
				}
				if( null != ums && ums.length() >= 4 ){
					if( ums.length() > 4 ){
						String appid = ums.substring(0,4);
						String subid = ums.substring(4);
						state.setActiveUms(appid);
						state.setActiveSubAppId(subid);
					}
					else
						state.setActiveUms(ums);
				}
			}
		}
		return state;
	}
	
	public void setRtuState(RtuState state){
		states.put(state.getRtua(), state);
	}
	
	public boolean isPrimary(){
		if( null == channel || null==channel.getView() )
			return true;
		if( channel.getView().getMembers().size() == 1 || 
				channel.getView().getMembers().firstElement() == channel.getAddress() )
			return true;
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public boolean start() {
		if( null != channel )
			return true;
		try{
			channel = new JChannel(jchannelConfig);
			if( StringUtils.hasText(addressName))
				channel.setName(addressName);
			states = new ReplicatedHashMap<String,RtuState>(channel,false);
			Notification<String,RtuState> notif = new Notification<String,RtuState>(){

				public void contentsCleared() {
					
				}

				public void contentsSet(Map<String, RtuState> newEntries) {
					log.info("all state synchronized.");
				}

				public void entryRemoved(String key) {
					
				}

				public void entrySet(String key, RtuState value) {
					//打印日志过多，影响性能，降低级别，
					if (log.isDebugEnabled())
						log.debug("state synchronized : " + value);
				}

				public void viewChange(View view, Vector<Address> newMbrs,
						Vector<Address> oldMbrs) {
					log.info("view changed: new members="+newMbrs.toString()+", old="+oldMbrs.toString());
				}
			};
			states.addNotifier(notif);
			channel.connect(groupName);
			states.start(10000);
			if( isPrimary() ){
				List<RtuState> copy = new ArrayList<RtuState>();
				ObjectInputStream ois = null;
				try{
					ois = new ObjectInputStream(new FileInputStream(paramStateCache));
					copy = (List<RtuState>)ois.readObject();
					for( RtuState rs: copy ){
						states.put(rs.getRtua(), rs);
					}
				}
				catch(FileNotFoundException e){
					log.info("load state from cache file,but not found: "+ paramStateCache);
				}
				catch(Throwable e){
					log.error("load param-states from file exp:",e);
				}
			}
			log.info("realtime.sync STATE Objects size=" + states.size()+" , members size="+channel.getView().size());
			log.info(channel.getView().printDetails());
			Runnable shutdownHook = new Runnable(){
				public void run(){
					stop();
				}
			};
			FasSystem.getFasSystem().addShutdownHook(shutdownHook);
		}catch(Exception e){
			log.error("create JGroup EXP:"+e.getLocalizedMessage(),e);
			System.exit(-1);
		}
		log.info("start FE cluster synchronizer:"+groupName);
		return true;
	}
	
	public void stop(){
		try{
			if( null != channel )
				channel.close();
			channel = null;
			try{
				states.stop();
			}catch(Exception e2){
				log.warn("stop FE states synchornize exp:"+e2.getLocalizedMessage(),e2);
			}
		}catch(Exception e){
			log.error(e.getLocalizedMessage(),e);
		}
		finally{
			save2File();
			BatchSynchronizer.getInstance().saveTextFile();
		}
	}
	
	/**
	 * 把终端通信参数缓存到本地文件。
	 * 由Spring配置定时器，定时执行。
	 */
	public void save2File(){
		try{
			List<RtuState> copy = new ArrayList<RtuState>(this.states.values());
			ObjectOutputStream oos = null;
			oos = new ObjectOutputStream(new FileOutputStream(this.paramStateCache,false));
			try{
				oos.writeObject(copy);
				oos.flush();
			}finally{
				oos.close();
				oos=null;
			}						
		}catch(Throwable e){
			log.error("save communication param-states to file exp:",e);
		}
	}
	
	private void save2Db(int key){
		if( null == dbService&&dbService.isMasterDbActive() ){
			log.error("dbService is not Active, key="+key+" save2Db exit!");
			return;
		}
		else{
			log.info("start to save2Db key=]"+key);
		}
		try{
			List<RtuState> copy = new ArrayList<RtuState>(this.states.values());
			int cnt = 0 ;
			int count=0;
			for(RtuState obj: copy){
				PojoCommState pojo = new PojoCommState();
				pojo.setRtuState(obj);
				pojo.setAddress( getAddressName() );
				WorkState wsobj = BatchSynchronizer.getInstance().getWorkState(obj.getRtua());
				if( null != wsobj )
					pojo.setWorkState(wsobj);
				else
					continue;
				if(pojo.getLastGprsRecvTime()==null && pojo.getLastSmsRecvTime()==null && pojo.getLastHeartbeatTime()==null){
					log.error("Time is null:"+key+",Address:"+pojo.getLogicalAddress()+",time:"+new Date());
					continue;
				}
				if (!dbService.addToDao(pojo, key)){
					log.debug("dbService deal with the key="+key+" data2Db too slowly!,sleep 1 s");					
					if(dbService.isMasterDbActive()){
						count++;
						try{
							Thread.sleep(1000);
						}catch(Exception ex){}
					}
					else{
						log.error("MasterDb is not Active,[key=]"+key+" save2Db exit!");
						return;
					}
				}
				else
					cnt++;
			}
			log.info("save2Db RtuState.size="+copy.size()+"保存到数据库[key=]"+key+" 成功记录数="+cnt);
			if(count>0)
				log.warn("数据库保存速度过慢[key=]"+key+" ,失败记录数="+count);
		}catch(Throwable e){
			log.error("save communication param-states to DB exp:",e);
		}
	}
	
	public void saveCommState(RtuState state){
		if(state == null) {
			log.warn("saveCommState error ,state is null");
			return;
		}
		int key =  updateCommStateKey;
		PojoCommState pojo = new PojoCommState();
		pojo.setRtuState(state);
		pojo.setAddress( getAddressName() );
		WorkState wsobj = BatchSynchronizer.getInstance().getWorkState(state.getRtua());
		if( null != wsobj )
			pojo.setWorkState(wsobj);
		else{
			return;
		}
		dbService.addToDao(pojo, key);
	
	}
	
	public void saveCommState(){
		save2Db(this.updateCommStateKey);
//		reloadUnDocRtu();
	}
	
	public void reloadUnDocRtu(){
		try{
			List<RtuState> list = new ArrayList<RtuState>();
			for(RtuState rs : this.states.values() ){
				if( !StringUtils.hasText(rs.getDwdm()) || ! StringUtils.hasText(rs.getUsage()) )
					list.add(rs);
			}
			log.warn(list.size()+" Terminal File Unload,Reload now.");//log.warn(list.size()+"个终端档案未加载，重新加载。");
			for(RtuState rs : list ){		
				log.debug("Terminal File Unload,Reload now.rtua=" + rs.getRtua() );//log.debug("终端档案未加载，重新加载。rtua=" + rs.getRtua() );
				ComRtu param = ManageRtu.getInstance().reloadComRtu(rs.getRtua()) ;
				if( null == param )
					continue;
				rs.setUsage(param.getRtuType());
				rs.setSimNum(param.getSimNum());
				rs.setDwdm(param.getDeptCode());
				rs.setTerminalType(param.getTerminalType());
				String ums = ChannelManage.getUmsAppId(param);
				if ( ums != null ){
					int index = ums.indexOf("95598");
					if ( index >= 0 )
						ums=ums.substring(index+5);
				}
				if( null != ums && ums.length() >= 4 ){
					if( ums.length() > 4 ){
						String appid = ums.substring(0,4);
						String subid = ums.substring(4);
						rs.setActiveUms(appid);
						rs.setActiveSubAppId(subid);
					}
					else
						rs.setActiveUms(ums);
				}
			}
		}catch(Exception e){
			log.warn("reload un-documented RTU exception.",e);
		}
	}
	
	public void resetFlowmeter(){
		try{
			for(RtuState obj : this.states.values() ){
				WorkState wsobj = BatchSynchronizer.getInstance().getWorkState(obj.getRtua());
				if( null != wsobj ){
					wsobj.clearStatus();
				}
			}
		}catch(Exception exp){
			log.error("reset flowmeters catch exception:"+exp.getLocalizedMessage(),exp);
		}
	}

	/**
	 * 刷新通讯档案
	 */
	public void refreshComRtuCache(){
    	try{
    		if (log.isInfoEnabled())
    			log.info("refresh rtu cache job excute");
    		List<RtuSynchronizeItem> rsts=masterDbService.getRtuSycItem(lastRefreshTime.getTime());
    		if (rsts!=null && rsts.size()>0){
    			log.info("refresh size="+rsts.size());
    		}
    		else {
    			log.info("no record need refresh");
    			return;
    		}
    		long time=lastRefreshTime.getTimeInMillis();
    		for (RtuSynchronizeItem rst:rsts){
    			if (rst.getSycType()==0){//刷新终端档案
    				String rtuId=rst.getRtuId();
    				states.remove(rtuId);
    				loadFromDb(rtuId);
    			}else{
    				// do nothing
    			}
    			Date dt=null;
    			try{
    				dt=format.parse(rst.getSycTime());
    			}catch(Exception ex){
    				log.error("rtu syctime parse error:"+ex);
    				continue;
    			}
        		if (time<dt.getTime())
    				time=dt.getTime();   	
        		Thread.sleep(100);
    		}  		
    		lastRefreshTime.setTimeInMillis(time);
    	}catch(Exception e){
    		log.error("getRtuSycItem error:"+e);
    	}
    	
    }
	
	public void saveCommFlowmeter(){
		save2Db(this.updateCommFlowmeterKey);
	}
	
	public void resetCommFlowmeter(){
		BatchSynchronizer.getInstance().clear();
	}
	
	public void setJchannelConfig(String jchannelConfig) {
		this.jchannelConfig = jchannelConfig;
	}
	
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	
	public void setParamStateCache(String stateCache) {
		this.paramStateCache = stateCache;
	}
	@Override
	public String getName() {
		return groupName;
	}
	public String getModuleType() {
		return IModule.MODUEL_TYPE_CLUSTER;
	}
	public AsyncService getDbService() {
		return dbService;
	}
	public void setDbService(AsyncService dbService) {
		this.dbService = dbService;
	}
	public void setUpdateCommStateKey(int updateCommStateKey) {
		this.updateCommStateKey = updateCommStateKey;
	}
	public void setUpdateCommFlowmeterKey(int updateCommFlowmeterKey) {
		this.updateCommFlowmeterKey = updateCommFlowmeterKey;
	}
	public void setAddressName(String addressName) {
		if( StringUtils.hasText(addressName))
			this.addressName = StringUtils.trimAllWhitespace(addressName);
	}
	public String getAddressName() {
		return null != addressName ? addressName : channel.getAddress().toString();
	}
	public MasterDbService getMasterDbService() {
		return masterDbService;
	}
	public void setMasterDbService(MasterDbService masterDbService) {
		this.masterDbService = masterDbService;
	}

}
