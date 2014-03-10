/**
 * 批量非实时同步。主要用于同步终端上行 (报文类型：心跳...; 上行时间; 报文长度; 主动上行标识 )  (广播同步)
 * 一般收集一批量信息，定时执行同步的策略。如果批量达到上限，则立即同步。
 */
package cn.hexing.fk.fe.cluster;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.ExtendedReceiverAdapter;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.View;
import org.jgroups.util.Util;
import org.springframework.util.StringUtils;

import cn.hexing.fk.FasSystem;
import cn.hexing.fk.common.spi.IModule;

/**
 * 前置机集群：信息同步支撑类。
 */
public class BatchSynchronizer extends ExtendedReceiverAdapter implements IModule{
	private static final Logger log = Logger.getLogger(BatchSynchronizer.class);
	private static final BatchSynchronizer instance = new BatchSynchronizer();
	private static final int MAX_SIZE = 100000;		//批次同步时，如果需要同步的数量超过最大，则丢弃，保持稳定。
	//configurable properties
	private String groupName = "FE.sync.batch";
	private String addressName = null;
	private String jchannelConfig = "udp.xml";
	private int batchSize = 1000;	//当达到批次数量，即可同步。
	private int interval = 30;		//每N秒同步一次
	private String workStateCache = "data/work-state-cache.data";
	private int timeout = 20;		//状态同步的超时（秒）
	//private properties
	private Channel channel = null;
	private final Object mutex = new Object();
	private List<RtuWorkStateItem> workStates = new ArrayList<RtuWorkStateItem>(batchSize);
	private PeriodExecuter periodExecuter = null;
	private volatile boolean running = false;
	//同步效率统计所需属性
	private long startTime = System.currentTimeMillis();
	private long total = 0;
	private int syncTimes = 0;		//同步次数，定时多次同步后，打印日志信息，防止过度打印日志
	//终端工况,定期保存到数据库以及本地缓存文件。
	private final Map<String,WorkState> workStateMap = new HashMap<String,WorkState>();
	private boolean loadCache = false;
	
	private BatchSynchronizer(){}
	public static final BatchSynchronizer getInstance() { return instance; }

	public final WorkState getWorkState(String rtua){
		return workStateMap.get(rtua);
	}
	
	public final void clear(){
		List<WorkState> copy = null;
		synchronized(workStateMap){
			copy = new ArrayList<WorkState>(this.workStateMap.values());
//			workStateMap.clear();
		}
		for(WorkState ws : copy){
			ws.clearStatus();
		}
	}
	
	public void addWorkState(RtuWorkStateItem ws){
		if( !isActive() )
			return;
		ArrayList<RtuWorkStateItem> temp = null;
		if( workStates.size()>= MAX_SIZE ){
			log.warn("sync queue size is MAX_SIZE. Abandon it");
			return;
		}
		synchronized(mutex){
			workStates.add(ws);
			if( workStates.size()>= batchSize ){
				temp = new ArrayList<RtuWorkStateItem>(workStates);
				workStates.clear();
			}
			//更新终端情况.
			WorkState workState = workStateMap.get(ws.getRtua());
			if( null == workState ){
				workState = new WorkState();
				workState.setRtua(ws.getRtua());
				synchronized(workStateMap){
					workStateMap.put(ws.getRtua(), workState);
				}
			}
			workState.update(ws);
		}
		if( null != temp ){
			_syncWorkState(temp);
		}
	}
	
	public boolean isActive(){
		return running && channel.isConnected();
	}
	
	private void _syncWorkState(ArrayList<RtuWorkStateItem> temp){
		if( isActive() ){
			try{
				Message msg = new Message(null,null,temp);
				channel.send(msg);
			}catch(Exception e){
				log.error("["+groupName+"] send message exp:"+e.getLocalizedMessage(),e);
			}
		}
	}
	
	public boolean start(){
		if( null != channel )
			return true;
		try{
			channel = new JChannel(jchannelConfig);
			channel.setReceiver(this);
			if( StringUtils.hasText(addressName) )
				channel.setName(addressName);
			channel.connect(groupName);
			//channel.connect(groupName,null,null,timeout*1000);		//初始化之后，需要同步全部的终端工况
			channel.getState(null, timeout*1000);
			
			running = true;
			periodExecuter = new PeriodExecuter();
			periodExecuter.start();
			startTime = System.currentTimeMillis();
			Runnable shutdownHook = new Runnable(){
				public void run(){
					stop();
				}
			};
			FasSystem.getFasSystem().addShutdownHook(shutdownHook);
		}catch(Exception e){
			log.error("JGroup exp. group="+groupName+",exp="+e.getLocalizedMessage(),e);
			System.exit(-1);
		}
		return true;
	}
	
	public void stop(){
		if( ! running )
			return;
		try{
			running = false;
			Thread.yield();
			if( null != channel )
				channel.close();
			channel = null;
		}catch(Exception e){
			log.error(e.getLocalizedMessage(),e);
		}
		finally{
			//把状态写入到缓存文件
			save2File();
		}
	}
	
	/**
	 * 把终端工况缓存到本地文件。
	 * 由Spring配置定时器，定时执行。
	 */
	public void save2File(){
		List<WorkState> copy = new ArrayList<WorkState>(this.workStateMap.values());
		ObjectOutputStream oos = null;
		try{
			oos = new ObjectOutputStream(new FileOutputStream(this.workStateCache,false));
			try{
				oos.writeObject(copy);
				oos.flush();
			}finally{
				oos.close();
				oos = null;
			}						
		}catch(Throwable e){
			log.error("save work-states to file exp:",e);
		}
	}
	
	public void saveTextFile(){
		List<WorkState> copy = new ArrayList<WorkState>(this.workStateMap.values());
		Collections.sort(copy, new Comparator<WorkState>(){
			public int compare(WorkState o1, WorkState o2) {
				
				return o1.getRtua().compareTo(o2.getRtua());
			}
		});
		try{
			PrintWriter printer = new PrintWriter(new FileOutputStream("workstate.txt",false));
			try{
				for(WorkState ws: copy){
					printer.println(ws);
				}
			}finally{
				printer.close();
				printer=null;
			}			
		}catch(Exception exp){
			log.error(exp.getLocalizedMessage(),exp);
		}
	}
	
	private class PeriodExecuter extends Thread {
		public PeriodExecuter(){
			super(groupName+".periodExec");
			this.setDaemon(true);
		}

		@Override
		public void run() {
			log.info("Thread ["+this.getName()+"] begin.");
			while( running ){
				try{
					Thread.sleep(interval * 1000);
					ArrayList<RtuWorkStateItem> temp = null;
					synchronized(mutex){
						if( workStates.size()>0 ){
							temp = new ArrayList<RtuWorkStateItem>(workStates);
							workStates.clear();
						}
					}
					if( null != temp ){
						_syncWorkState(temp);
					}
				}catch(Exception e){
					log.error("PeriodExecuter exp:"+e.getLocalizedMessage(),e);
				}
			}
			log.info("Thread ["+this.getName()+"] end.");
		}
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public void setJchannelConfig(String jchannelConfig) {
		this.jchannelConfig = jchannelConfig;
	}
	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}
	public void setInterval(int interval) {
		this.interval = interval<=0 ? 1 : interval;
	}
	
	@Override
	public void getState(OutputStream ostream) {
		// 每个WorkState 68 Bytes。
		List<WorkState> copy = new ArrayList<WorkState>(this.workStateMap.values());
        ObjectOutputStream oos=null;
        try {
    		int objStreamLen = Util.objectToByteBuffer(copy).length;
    		int actLen = copy.size()*68;
    		if(log.isInfoEnabled())
    			log.info("getState called. objStreamLen="+objStreamLen+", actual Len="+actLen);
    		
            oos=new ObjectOutputStream(ostream);
            oos.writeObject(copy);
        }
        catch(Throwable ex) {
        	log.error("exception marshalling state: " + ex);
        }
        finally {
        	copy.clear();
            Util.close(oos);
        }
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void receive(Message msg) {
		boolean isMyself = true;
		Object o = msg.getObject();
		if( msg.getSrc() != channel.getAddress() ){
			isMyself = false;
			if(log.isInfoEnabled())
				log.info("*****recv msg from other system. "+ msg.getSrc().toString());
		}
		try{
			ArrayList<RtuWorkStateItem> temp = (ArrayList<RtuWorkStateItem>)o;
			total += temp.size();
			if( syncTimes % 2 == 0 ){
				long speed = (total * 1000) / (System.currentTimeMillis()-startTime + 1 ) ;
				if(log.isDebugEnabled())
					log.debug("speed=" + speed + " Msg length="+msg.getLength()+", actual Len="+temp.size()*18);
			}
			syncTimes++;
			//更新终端工况对象......
			for(RtuWorkStateItem item: temp ){
				WorkState ws = workStateMap.get(item.getRtua());
				if( null == ws ){
					ws = new WorkState();
					ws.setRtua(item.getRtua());
					synchronized(workStateMap){
						workStateMap.put(ws.getRtua(), ws);
					}
				}
				//如果是来自身FE的，不需要更新,这里是需要存数据库的，否则会重复存储
				if(!isMyself)
					ws.update(item);
			}
		}catch(Exception e){
			log.error("onReceive Msg:"+e.getLocalizedMessage(),e);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void setState(InputStream istream) {
		List<WorkState> copy = null;
        ObjectInputStream ois=null;
        try {
        	ois = new ObjectInputStream(istream);
        	copy = (List<WorkState>)ois.readObject();
        	if(log.isInfoEnabled())
        		log.info("setState called. size="+copy.size());
			//更新终端工况对象......
			for(WorkState ws: copy){
				synchronized(workStateMap){
					workStateMap.put(ws.getRtua(), ws);
				}
			}
        }catch(Throwable e){
        	log.error("exception marshalling state: " + e);
        }
        finally {
            Util.close(ois);
        }
	}
	
	@Override
	public void suspect(Address suspectedMbr) {
		if(log.isInfoEnabled())
			log.info("cluster member may lost:" + suspectedMbr);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void viewAccepted(View newView) {
		//第一次连接成功时，如果只有一个member，那么需要加载缓存。
		if( !loadCache && newView.size() == 1 ){
			List<WorkState> copy = new ArrayList<WorkState>();
			ObjectInputStream ois = null;
			try{
				ois = new ObjectInputStream(new FileInputStream(this.workStateCache));
				copy = (List<WorkState>)ois.readObject();
				for( WorkState ws: copy ){
					workStateMap.put(ws.getRtua(), ws);
				}
			}
			catch(FileNotFoundException e){
				log.warn("load state from cache file,but not found: "+workStateCache);
			}
			catch(Throwable e){
				log.error("load work-states from file exp:",e);
			}
		}
		loadCache = true;
		if(log.isInfoEnabled())
			log.info("viewAccepted: "+ newView.printDetails() );
	}
	
	public void setWorkStateCache(String rtuWorkStateCache) {
		this.workStateCache = rtuWorkStateCache;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	public void setAddressName(String addressName) {
		this.addressName = StringUtils.trimAllWhitespace(addressName);
	}
	
	public String getModuleType() {
		return IModule.MODUEL_TYPE_CLUSTER;
	}
	public String getName() {
		return groupName;
	}
	public String getTxfs() {
		return "";
	}
	public String profile() {
		return "<profile>empty</profile>";
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
}
