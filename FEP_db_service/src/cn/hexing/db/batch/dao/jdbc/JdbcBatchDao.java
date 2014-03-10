package cn.hexing.db.batch.dao.jdbc;

import java.io.File;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import cn.hexing.db.DbMonitor;
import cn.hexing.db.DbState;
import cn.hexing.db.batch.AsyncService;
import cn.hexing.db.batch.dao.IBatchDao;
import cn.hexing.fk.tracelog.TraceLog;
import cn.hexing.fk.utils.FileUtil;


/**
 * 思路：
 * 	  1）每个业务逻辑Service包含多个带Key的DAO对象。
 * 	  2）业务逻辑在线程池中执行报文处理，然后把结果放入到相应DAO对象；
 * 	  3）在业务逻辑线程中，必须定时（delay秒）调用add(null);
 */
public class JdbcBatchDao implements IBatchDao {
	private static final Logger log = Logger.getLogger(JdbcBatchDao.class);
	//数据库操作跟踪日志
	private static final TraceLog tracer = TraceLog.getTracer("jdbcBatchdao");
	//可配置属性
	private SimpleJdbcTemplate simpleJdbcTemplate;		//对应dataSource属性
	private DataSource dataSource;						//用于DbMonitor
	private String sql,sqlAlt,additiveSql;
	private Object additiveParameter;
	private int key = 0;
	private int batchSize = 2000;
	private long delay = 5000;		//不足批量，最迟5秒必须保存
	//内部属性
	private List<Object> objList = new ArrayList<Object>();
	private List<Object[]> paramArrayList = new ArrayList<Object[]>();
	private Object batchDaoLock = new Object(), objListLock = new Object();;
	private long lastIoTime = System.currentTimeMillis();
	private String executeThreadName = null;
	private boolean executing = false;
	
	private int objListSize = 0;
	private String tempFile;
	private String tempPath;
	
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		this.simpleJdbcTemplate = new SimpleJdbcTemplate(this.dataSource);
	}

	private int[] batchUpdateByPojo(String sqlStr,List<Object> pojoList){
		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(pojoList.toArray());
		int[] updateCounts = simpleJdbcTemplate.batchUpdate(sqlStr,batch);
		if( null != this.additiveSql )
			simpleJdbcTemplate.update(this.additiveSql);
		return updateCounts;
	}

	private int[] batchUpdateByParams(String sqlStr,List<Object[]> arrayList){
		int[] updateCounts =  simpleJdbcTemplate.batchUpdate(sqlStr,arrayList);
		if( null != this.additiveSql )
			simpleJdbcTemplate.update(this.additiveSql);
		return updateCounts;
	}

	private void _doBatchUpdate(){
		if( log.isDebugEnabled())
			log.debug("Start Executing Dao,key="+key+",sql="+sql);
		int[] result = null;
		long time0 = System.currentTimeMillis();
		
		List<Object> temp1 = null;
		List<Object[]> temp2 = null;
		
		String oldFile = tempFile; 
		FileLock fileLock = null;
		synchronized(objListLock){
			if(objListSize>0 || objList.size()>0){
				if(AsyncService.isFileCache){
					File lockFile = FileUtil.openFile(tempPath, oldFile+".lock");
					fileLock= FileUtil.tryLockFile(lockFile);
					temp1 = _read(oldFile,fileLock);					
				}else{
					temp1 = objList;
					objList = new ArrayList<Object>();
				}
			} else if( paramArrayList.size()>0 ){
				temp2 = paramArrayList;
				paramArrayList = new ArrayList<Object[]>();
			}
		}
		
		
		if( null != temp1 ){
			result = batchUpdateByPojo( sql,temp1 );
			if( log.isInfoEnabled() ){
				long timeTake = System.currentTimeMillis() - time0;
				if (timeTake>1000)//批量保存任务超过1秒打印信息
					log.info("key="+key+",Success Count="+ result.length +",Use Millisecond="+timeTake);
				tracer.trace("key="+key+",batchUpdate takes(milliseconds):"+timeTake);
			}
			if( null != sqlAlt ){
				List<Object> listAlt = new ArrayList<Object>();
				for(int i=0; i<result.length; i++ ){
					if( result[i]<=0 ){
						listAlt.add(temp1.get(i));
					}
				}
				if( listAlt.size()>0 )
					batchUpdateByPojo(sqlAlt,listAlt);
			}
			lastIoTime = System.currentTimeMillis();
			if(AsyncService.isFileCache){
				FileUtil.deleteFile(tempPath, oldFile+".data");
				FileUtil.unlockFile(fileLock);
				FileUtil.deleteFile(tempPath, oldFile+".lock");				
			}
		}
		else if( null != temp2  ){
			result = batchUpdateByParams(sql,temp2 );
			if( log.isInfoEnabled() ){
				long timeTake = System.currentTimeMillis() - time0;
				if (timeTake>1000)//批量保存任务超过1秒打印信息
					log.info("key="+key+",Success Count="+ result.length +",Use Millisecond="+timeTake);
				tracer.trace("key="+key+",batchUpdate takes(milliseconds):"+timeTake);
			}

			if( null != sqlAlt ){
				List<Object[]> listAlt = new ArrayList<Object[]>();
				for(int i=0; i<result.length; i++ ){
					if( result[i]<=0 ){
						listAlt.add(temp2.get(i));
					}
				}
				if( listAlt.size()>0 )
					batchUpdateByParams(sqlAlt,listAlt);
			}
			lastIoTime = System.currentTimeMillis();
		}
	}
	
	private List<Object> _read(String oldFile, FileLock fileLock) {
		List<Object> temp1 = null;
		tempFile = ""+UUID.randomUUID();
		File file = FileUtil.openFile(tempPath, oldFile+".data");
		if(fileLock != null){
			temp1 = FileUtil.readObjectFromFile(file);
		}else{
			//打印日志，输出，说明文件已经被打开，无法读取
		}
		objListSize = 0;
		return temp1;
	}

	public void batchUpdate() {
		if( executing )
			return;
		synchronized(batchDaoLock){
			//数据库不可用，则直接退出批量保存。
			DbState ds = DbMonitor.getInstance().getMonitor(dataSource);
			if( null != ds && !ds.isAvailable() )
				return;
			if( null != executeThreadName ){
				log.error("BatchDao[key="+key+"] has already been executed by : "+ executeThreadName);
			}
			executeThreadName = Thread.currentThread().getName();
			//取消保存成功标志判断，以免保存异常后临时保存队列无法清空
			//boolean success = false;
			try{
				executing = true;
				_doBatchUpdate();
				//success = true;
			}
			catch(CannotGetJdbcConnectionException e){
				//数据库连接异常，通知监控模块
				tracer.trace("batch dao1 exception: CannotGetJdbcConnectionException");
				if( null != ds )
					ds.setAvailable(false);
			}
			catch(BadSqlGrammarException e){
				//语法错误，必须纠正。正确情况下，系统不应该打印此错误信息
				tracer.trace("batch dao1 exception:"+e.getLocalizedMessage(),e);
			}
			catch(Exception e){
				tracer.trace("batch dao1 exception:"+e.getLocalizedMessage(),e);
				log.warn("batch dao1 exception:"+e.getLocalizedMessage(),e);
			}
			finally{
				executing = false;
			}
			executeThreadName = null;
		}
	}

	public int getKey() {
		return key;
	}

	public void setKey(int k){
		key = k;
	}

	public boolean add(Object pojo) {
		if( null != pojo ){
			synchronized(objListLock){
				if( size() > batchSize*10 ){
					tracer.trace("batchDao1 can not add object,key="+key+",size="+size()+",batchSize="+batchSize);
					return false;
				}
				
				if(AsyncService.isFileCache)
					_add(pojo);
				else
					objList.add(pojo);
				
			}
			if( size()>= batchSize )
				batchUpdate();
		}
		else{
			delayExec();
		}
		return true;
	}

	private void _add(Object pojo) {
		
		if(tempPath ==null)
			tempPath = "data"+File.separator+key;
		if(tempFile==null){
			tempFile = ""+UUID.randomUUID();
		}
		
		//创建一个lock文件
		File lockFile = FileUtil.openFile(tempPath, tempFile+".lock");
		//将文件加锁
		FileLock fileLock = FileUtil.tryLockFile(lockFile);
		if(fileLock == null){
			tempFile = ""+UUID.randomUUID();
			lockFile = FileUtil.openFile(tempPath, tempFile+".lock");
			fileLock = FileUtil.tryLockFile(lockFile);
		}
		
		//将temp文件打开
		File file = FileUtil.openFile(tempPath, tempFile+".data");
		
		FileUtil.writeObjectToFile(pojo, file, true);
		//存储完毕，将文件解锁
		FileUtil.unlockFile(fileLock);
		objListSize ++;
	}

	/**
	 * Object[] params,代表的是表的一行数据。其顺序按照配置定义。
	 */
	public boolean add(Object[] params){
		if( null != params ){
			synchronized(objListLock){
				if( size() > batchSize*10 ){
					tracer.trace("batchDao1 can not add object,size="+size()+",batchSize="+batchSize);
					return false;
				}
				paramArrayList.add(params);
			}
			if( size()>= batchSize )
				batchUpdate();
		}
		else{
			delayExec();
		}
		return true;
	}

	public void setSql(String sql) {
		this.sql = sql.trim();
	}

	public void setSqlAlt(String sqlAlt){
		this.sqlAlt = sqlAlt.trim();
	}
	
	public void setAdditiveSql(String adSql ){
		this.additiveSql = adSql;
	}
	
	public int size(){
		return Math.max(Math.max(objListSize,objList.size()), paramArrayList.size());
	}
	
	public void setBatchSize(int batchSize){
		this.batchSize = batchSize;
	}
	
	public long getLastIoTime(){
		return lastIoTime;
	}
	
	public void setDelaySecond(int delaySec){
		delay = delaySec*1000;
	}
	
	public long getDelayMilliSeconds(){
		return delay;
	}
	
	public boolean hasDelayData(){
		DbState ds = DbMonitor.getInstance().getMonitor(dataSource);
		boolean result = System.currentTimeMillis() - lastIoTime >= delay && size()>0 ;
		if( null != ds )
			result = result && ds.isAvailable();
		return result;
	}
	
	private void delayExec(){
		if( hasDelayData() ){
			batchUpdate();
		}
	}

	public void setAdditiveParameter(Object additiveParameter) {
		this.additiveParameter = additiveParameter;
	}

	public Object getAdditiveParameter() {
		return additiveParameter;
	}
}
