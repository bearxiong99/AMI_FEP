/**
 * 数据库异步保存逻辑模块接口。
 * 异步批量保存数据库，需要线程池支持。
 * 思路：
 * 	  1）异步业务逻辑Service，首先把消息对象转换为事件对象放入队列，确保业务处理快速。
 * 	  2）业务消息的处理，通过接口，使得service能够多线程处理业务逻辑。
 * 	  3）特殊设计－系统流量控制：如果业务处理器不断接收消息，导致数据库处理不过来，那么
 * 		 系统将崩溃。因此需要控制最大队列。当达到最大值，需要等待批量处理完毕。
 * 	  3）业务逻辑的处理结果，一般是把一个消息，生成多个DAO对象调用。
 */
package cn.hexing.db.batch;

import java.io.File;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.events.BasicEventHook;
import cn.hexing.fk.common.events.EventQueue;
import cn.hexing.fk.common.queue.CacheQueue;
import cn.hexing.fk.common.simpletimer.ITimerFunctor;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.common.spi.IModule;
import cn.hexing.fk.common.spi.abstra.BaseModule;

import cn.hexing.db.DbMonitor;
import cn.hexing.db.DbState;
import cn.hexing.db.batch.dao.IBatchDao;
import cn.hexing.db.batch.event.BpBatchDelayEvent;
import cn.hexing.db.batch.event.BpExpAlarmEvent;
import cn.hexing.db.batch.event.BpLog2DbEvent;
import cn.hexing.db.batch.event.BpReadTaskEvent;
import cn.hexing.db.batch.event.FeUpdateRtuStatus;
import cn.hexing.db.batch.event.adapt.BatchDelayHandler;
import cn.hexing.fk.FasSystem;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.MessageConst;
import cn.hexing.fk.message.MessageType;
import cn.hexing.fk.message.gw.MessageGw;
import cn.hexing.fk.message.zj.MessageZj;
import cn.hexing.fk.tracelog.TraceLog;
import cn.hexing.fk.utils.FileUtil;
/**
 */
public class AsyncService extends BaseModule implements ITimerFunctor{
	protected static final Logger log = Logger.getLogger(AsyncService.class);
	private static final TraceLog tracer = TraceLog.getTracer();
	
	private static final int DEFAULT_QUEUE_SIZE = 10000; 
	//可配置属性
	private int maxQueueSize = DEFAULT_QUEUE_SIZE;
	private int minThreadSize = 4;
	private int maxThreadSize = 30;
	private int delaySecond = 5;		//如果插入的数量不足批次，最大延迟秒数
	private String name = "batchService";
	private List<IBatchDao> daoList;
	private Map<EventType,BaseBpEventHandler> bpHandlerMap = new HashMap<EventType,BaseBpEventHandler>();

	//内部属性
	private EventQueue queue = new EventQueue(maxQueueSize);
	private BasicEventHook eventHook;
	private BatchDelayHandler batchDelayHandler = new BatchDelayHandler();
	private Map<Integer,IBatchDao> daoMap = new HashMap<Integer,IBatchDao>(127);
	private CacheQueue msgLogCacheQueue = null;
	private DataSource dataSource;
	private DataSource dataSourceRz;
	
	private Timer timer = null;
	
	//是否使用文件缓存存储
	public static boolean isFileCache = false;
	

	public void init(){
		if( null == eventHook ){
			eventHook = new BasicEventHook();
			if( ! eventHook.isActive() ){
				eventHook.setMinSize(minThreadSize);
				eventHook.setMaxSize(maxThreadSize);
				eventHook.setName(name);
				eventHook.setQueue(queue);
			}
		}
		if( null == msgLogCacheQueue ){
			msgLogCacheQueue = new CacheQueue();
			msgLogCacheQueue.setKey("rawmsg");
			msgLogCacheQueue.setMaxFileSize(100);
			msgLogCacheQueue.setFileCount(20);
			msgLogCacheQueue.setMaxSize(100);
			msgLogCacheQueue.setMinSize(10);
		}
		for( EventType type: bpHandlerMap.keySet()){
			eventHook.addHandler(type, bpHandlerMap.get(type));
		}
		eventHook.addHandler(batchDelayHandler.type(), batchDelayHandler);
		eventHook.start();
	}

	public void initData() {
		
		if(!AsyncService.isFileCache) return;
		
		for(IBatchDao dao : daoList){
			int key  = dao.getKey();
			//查找data路径下面是否有key文件夹
			File dir = new File("data"+File.separator+key);
			if(dir.exists() && dir.isDirectory()){
				//列出当前文件夹下面所有文件
				File[] files = dir.listFiles();
				for(File file : files){
					if(file.getName().endsWith(".data")){
						String lockFileName = file.getName().replaceAll("data", "lock");
						
						File lockFile = new File(dir.getPath()+File.separator+lockFileName);
						FileLock fileLock = FileUtil.tryLockFile(lockFile);
						if(fileLock == null) continue; //说明别的线程在使用
						
						try {
							List<Object> datas = FileUtil.readObjectFromFile(file);
							if(datas !=null){
								for(Object data : datas){
									addToDao(data, key);
								}							
							}
						} catch (Exception e) {}
						FileUtil.deleteFile(dir.getPath(), file.getName());
						FileUtil.unlockFile(fileLock);
						FileUtil.deleteFile(dir.getPath(),lockFileName);
						
					}
				}
			}
			
		}
	}

	public boolean isActive(){
		return null != eventHook && eventHook.isActive() && FasSystem.getFasSystem().isDbAvailable();
	}
	//主数据库连接状态
	public boolean isMasterDbActive(){
		DbState ds = DbMonitor.getInstance().getMonitor(dataSource);
		if( null != ds && ds.isAvailable() )
			return true;
		else
			return false;
	}
	//报文据库连接状态
	public boolean isRzDbActive(){
		DbState ds = DbMonitor.getInstance().getMonitor(dataSourceRz);
		if( null != ds && ds.isAvailable() )
			return true;
		else
			return false;
	}
	public String getName() {
		return name;
	}

	public boolean start() {
		if( null != timer )
			return true;
		init();
		for(IBatchDao dao: daoList ){
			dao.setDelaySecond(this.delaySecond);
		}
		timer = new Timer();
		TimerTask task = new TimerTask(){
			@Override
			public void run() {
				onTimer(0);
			}
		};
		timer.schedule(task, 0, this.delaySecond*1000);
		//TimerScheduler.getScheduler().addTimer(new TimerData(this,0,this.delaySecond));
		return true;
	}

	public void stop() {
		if( null != timer ){
			timer.cancel();
			timer = null;
		}
//		TimerScheduler.getScheduler().removeTimer(this, 0);
		if( null != eventHook ){
			eventHook.stop();
		}
	}

	public String getModuleType() {
		return IModule.MODULE_TYPE_DB_SERVICE;
	}

	/**
	 * 需要异步处理的消息对象。
	 * 如果数据库连接异常（数据库不可用），那么不能增加新的消息。
	 * @param msg
	 * @return：true:允许处理消息； false：数据库不可用或者service繁忙。
	 */
	public boolean addMessage(IMessage msg){	
		//处理队列超限值或主数据连接异常则不做后续处理
		if( queue.size()>= maxQueueSize||!isMasterDbActive() )
			return false;

		if( msg.getMessageType() == MessageType.MSG_ZJ){
			MessageZj zjmsg = (MessageZj)msg;
			IEvent event;
			if( zjmsg.head.c_func == MessageConst.ZJ_FUNC_READ_TASK 
					 || zjmsg.head.c_func ==MessageConst.GG_FUNC_READ_TASK2)
				event = new BpReadTaskEvent(this,zjmsg);
		
			else if( zjmsg.head.c_func == MessageConst.ZJ_FUNC_EXP_ALARM ||zjmsg.head.c_func==MessageConst.GG_FUNC_Event )
				event = new BpExpAlarmEvent(this,zjmsg);
			else
				return false;
			try{
				queue.offer(event);
			}catch(Exception exp){
				tracer.trace(exp.getLocalizedMessage(), exp);
				return false;
			}
			return true;
		}
		else if( msg.getMessageType() == MessageType.MSG_GW_10 ){
			MessageGw gwmsg = (MessageGw)msg;
			IEvent event;
			if( gwmsg.afn() == MessageConst.GW_FUNC_GET_TASK || gwmsg.isTask() )
				event = new BpReadTaskEvent(this,msg);
			else if( gwmsg.afn() == MessageConst.GW_FUNC_GET_DATA3 )
				event = new BpExpAlarmEvent(this,msg);
			else
				return false;
			try{
				queue.offer(event);
			}catch(Exception exp){
				tracer.trace(exp.getLocalizedMessage(), exp);
				return false;
			}
			return true;
		}
		return false;
	}
	
	/**
	 * 当需要更新终端对象时。
	 * @param rtu
	 * @return
	 */
	public boolean addRtu(Object rtu){
		if( queue.size()>= maxQueueSize )
			return false;
		IEvent event = new FeUpdateRtuStatus(this,rtu);
		try{
			queue.offer(event);
		}catch(Exception exp){
			tracer.trace(exp.getLocalizedMessage(), exp);
		}
		return true;
	}
	
	public boolean log2Db(IMessage msg){
		if( !isRzDbActive() || queue.size()>= maxQueueSize ){
			msgLogCacheQueue.offer(msg);
			return true;
		}
		try{
			queue.offer(new BpLog2DbEvent(this,msg));
			if( queue.size()*2 < queue.capacity() ){
				//容量不足情况下，尝试加载缓存文件中的原始报文。
				for(int i=0; i<10; i++ ){
					msg = msgLogCacheQueue.poll();
					if( null == msg )
						break;
					queue.offer(new BpLog2DbEvent(this,msg));
				}
			}
			
		}catch(Exception exp){
			tracer.trace(exp.getLocalizedMessage(), exp);
		}
		return true;
	}
	
	public boolean addToDao(Object pojo,int key){
		IBatchDao dao = daoMap.get(key);
		if( null == dao ){
			log.error("数据保存到DAO错误，对象对应的KEY找不到DAO。key="+key);
			return false;
		}
		return dao.add(pojo);
	}
	
	public int getMaxQueueSize() {
		return maxQueueSize;
	}

	public void setMaxQueueSize(int maxQueueSize) {
		this.maxQueueSize = maxQueueSize;
		if( this.maxQueueSize> queue.capacity() )
			queue.setCapacity(maxQueueSize);
	}

	public void setDaoList(List<IBatchDao> list) {
		this.daoList = list;
		for(IBatchDao dao: daoList ){
			daoMap.put(dao.getKey(), dao);
		}
	}

	public void setBpHandlerMap(Map<EventType, BaseBpEventHandler> handlers) {
		this.bpHandlerMap = handlers;
		for(BaseBpEventHandler handler: bpHandlerMap.values() ){
			handler.setService(this);
		}
	}

	public void setEventHook(BasicEventHook eventHook) {
		this.eventHook = eventHook;
	}

	public void setMinThreadSize(int minThreadSize) {
		this.minThreadSize = minThreadSize;
	}

	public void setMaxThreadSize(int maxThreadSize) {
		this.maxThreadSize = maxThreadSize;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void onTimer(int id) {
		if( 0 == id ){
			// 定时检测是否需要触发延迟保存。
			for(IBatchDao dao: daoList ){
				if( dao.hasDelayData() ){
					//需要把事件放入到EventHook来执行。
					try{
						queue.offer(new BpBatchDelayEvent(dao));
					}catch(Exception exp){
						tracer.trace(exp.getLocalizedMessage(), exp);
					}
				}
			}
		}
	}

	public void setDelaySecond(int delaySecond) {
		if( delaySecond<=1 )
			delaySecond = 5;
		this.delaySecond = delaySecond;
	}

	@Override
	public String toString() {
		return "AsyncService";
	}
	
	public Collection<IMessage> revokeEventQueue(){
		boolean takable = queue.enableTake();
		queue.enableTake(false);
		List<IEvent> events = new LinkedList<IEvent>();
		List<IMessage> msgs = new ArrayList<IMessage>();
		queue.drainTo(events, queue.size(), 0);
		for(IEvent ev: events ){
			if( null != ev.getMessage() ){
				msgs.add(ev.getMessage());
			}
		}
		queue.enableTake(takable);
		if( null != msgLogCacheQueue )
			msgLogCacheQueue.asyncSaveQueue();
		return msgs;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void setDataSourceRz(DataSource dataSourceRz) {
		this.dataSourceRz = dataSourceRz;
	}
	public String profile(){
		return this.eventHook.profile();
	}

	public void setFileCache(boolean isFileCache) {
		AsyncService.isFileCache = isFileCache;
	}
}
