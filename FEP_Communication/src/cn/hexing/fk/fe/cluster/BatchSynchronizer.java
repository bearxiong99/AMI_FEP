/**
 * ������ʵʱͬ������Ҫ����ͬ���ն����� (�������ͣ�����...; ����ʱ��; ���ĳ���; �������б�ʶ )  (�㲥ͬ��)
 * һ���ռ�һ������Ϣ����ʱִ��ͬ���Ĳ��ԡ���������ﵽ���ޣ�������ͬ����
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
 * ǰ�û���Ⱥ����Ϣͬ��֧���ࡣ
 */
public class BatchSynchronizer extends ExtendedReceiverAdapter implements IModule{
	private static final Logger log = Logger.getLogger(BatchSynchronizer.class);
	private static final BatchSynchronizer instance = new BatchSynchronizer();
	private static final int MAX_SIZE = 100000;		//����ͬ��ʱ�������Ҫͬ��������������������������ȶ���
	//configurable properties
	private String groupName = "FE.sync.batch";
	private String addressName = null;
	private String jchannelConfig = "udp.xml";
	private int batchSize = 1000;	//���ﵽ��������������ͬ����
	private int interval = 30;		//ÿN��ͬ��һ��
	private String workStateCache = "data/work-state-cache.data";
	private int timeout = 20;		//״̬ͬ���ĳ�ʱ���룩
	//private properties
	private Channel channel = null;
	private final Object mutex = new Object();
	private List<RtuWorkStateItem> workStates = new ArrayList<RtuWorkStateItem>(batchSize);
	private PeriodExecuter periodExecuter = null;
	private volatile boolean running = false;
	//ͬ��Ч��ͳ����������
	private long startTime = System.currentTimeMillis();
	private long total = 0;
	private int syncTimes = 0;		//ͬ����������ʱ���ͬ���󣬴�ӡ��־��Ϣ����ֹ���ȴ�ӡ��־
	//�ն˹���,���ڱ��浽���ݿ��Լ����ػ����ļ���
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
			//�����ն����.
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
			//channel.connect(groupName,null,null,timeout*1000);		//��ʼ��֮����Ҫͬ��ȫ�����ն˹���
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
			//��״̬д�뵽�����ļ�
			save2File();
		}
	}
	
	/**
	 * ���ն˹������浽�����ļ���
	 * ��Spring���ö�ʱ������ʱִ�С�
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
		// ÿ��WorkState 68 Bytes��
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
			//�����ն˹�������......
			for(RtuWorkStateItem item: temp ){
				WorkState ws = workStateMap.get(item.getRtua());
				if( null == ws ){
					ws = new WorkState();
					ws.setRtua(item.getRtua());
					synchronized(workStateMap){
						workStateMap.put(ws.getRtua(), ws);
					}
				}
				//�����������FE�ģ�����Ҫ����,��������Ҫ�����ݿ�ģ�������ظ��洢
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
			//�����ն˹�������......
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
		//��һ�����ӳɹ�ʱ�����ֻ��һ��member����ô��Ҫ���ػ��档
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
