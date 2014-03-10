/**
 * 数据库监控。
 */
package cn.hexing.db;

import java.util.ArrayList;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

/**
 *
 */
public class DbMonitor {
	private static final Logger log = Logger.getLogger(DbMonitor.class);
//	private static final TraceLog tracer = TraceLog.getTracer();
	private static DbMonitor instance ;
	private  ArrayList<DbState> dbStates;
	//可配置属性
	private int testInterval = 60;			//连接测试间隔（秒）.
	//内部属性
	//多个数据库的监控，需要定时检测数据库是否恢复。
	private static final DbMonitorThread daemonThread = new DbMonitorThread();

	private DbMonitor(){}
	
	public static DbMonitor getInstance() {
		if (instance == null) {
			synchronized (DbMonitor.class) {
				if (instance == null) {
					instance = new DbMonitor();
				}
			}
		}
		return instance;
	}
	
	public DbState getMonitor(DataSource ds){
		for(DbState dbState : dbStates ){
			if( dbState.getDataSource() == ds )
				return dbState;
		}
		return null;
	}
		
	public void initialize(){
		this.testDbConnection();
		daemonThread.setInterval(testInterval);
		for(DbState dbState : dbStates ){
			daemonThread.add(dbState);
		}
	}
	
	public void testDbConnection(){
		for(DbState dbState : dbStates ){
			dbState.testDbConnection();
		}
	}

	public final void setTestInterval(int testInterval) {
		this.testInterval = testInterval;
	}
	
	public void setDbStates(ArrayList<DbState> dbStates) {
		this.dbStates = dbStates;
	}
	
	static class DbMonitorThread extends Thread{
		private static ArrayList<DbState> dbStates=new ArrayList<DbState>();
		private int interval=60;
		public DbMonitorThread(){
			super("DbMonitorDaemonThread");
			this.setDaemon(true);
			this.start();
		}
		public void setInterval(int interval){
			this.interval=interval;
		}
		public void add(DbState dbState){
			dbStates.add(dbState);
		}
		@Override
		public void run() {
			while(true){
				try{
					if( dbStates.size()==0 ){
						Thread.sleep(3*1000);
						continue;
					}
					if(interval<60||interval>3600){
						interval=60;
					}
					Thread.sleep(interval*1000);
					for(DbState ds: dbStates){
						ds.testDbConnection();
					}					
				}catch(Exception e){
					log.warn("dbMonitor test exception:"+e.getLocalizedMessage(),e);
				}
			}
		}
	}


	
}
