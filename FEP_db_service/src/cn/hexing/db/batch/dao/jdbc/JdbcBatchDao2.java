package cn.hexing.db.batch.dao.jdbc;

import java.io.File;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;

import cn.hexing.db.DbMonitor;
import cn.hexing.db.DbState;
import cn.hexing.db.batch.AsyncService;
import cn.hexing.db.batch.dao.IBatchDao;
import cn.hexing.db.batch.dao.jdbc.springwrap.NamedParameterUtils2;
import cn.hexing.fk.tracelog.TraceLog;
import cn.hexing.fk.utils.FileUtil;


/**
 * ˼·��
 * 	  1��ÿ��ҵ���߼�Service���������Key��DAO����
 * 	  2��ҵ���߼����̳߳���ִ�б��Ĵ���Ȼ��ѽ�����뵽��ӦDAO����
 * 	  3����ҵ���߼��߳��У����붨ʱ��delay�룩����add(null);
 */
public class JdbcBatchDao2 implements IBatchDao {
	private static final Logger log = Logger.getLogger(JdbcBatchDao2.class);
	private static final TraceLog tracer = TraceLog.getTracer("jdbcBatchdao2");
	//����������
	private BatchSimpleJdbcTemplate simpleJdbcTemplate;		//��ӦdataSource����
	private DataSource dataSource;
	private String sql,sqlPre=null,additiveSql=null;
	private Object additiveParameter;		//���ڸ���SQL�Ĳ������롣
	private int key;
	private int batchSize = 2000;
	private long delay = 5000;		//�������������5����뱣��
	//�ڲ�����
	private List<Object> objList = new ArrayList<Object>();
	private List<Object[]> paramArrayList = new ArrayList<Object[]>();
	private Object batchDaoLock = new Object(), objListLock = new Object();
	private long lastIoTime = System.currentTimeMillis();
	private String executeThreadName = null;
	private boolean executing = false;
	
	private int objListSize = 0;
	private String tempFile;
	private String tempPath;
	
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		this.simpleJdbcTemplate = new BatchSimpleJdbcTemplate(dataSource);
	}

	private int[] batchUpdateByPojo(String sqlStr,List<Object> pojoList){
		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(pojoList.toArray());
		int[] updateCounts ;
		if( null != this.additiveSql ){
			if( null != this.additiveParameter ){
				final SqlParameterSource sqlParaSource = new BeanPropertySqlParameterSource(additiveParameter);
				String sqlToUse = NamedParameterUtils2.substituteNamedParameters(additiveSql, sqlParaSource );
				updateCounts = simpleJdbcTemplate.batchUpdate(sqlPre,sqlStr,batch, sqlToUse);
			}
			else
				updateCounts = simpleJdbcTemplate.batchUpdate(sqlPre,sqlStr,batch, additiveSql);
		}
		else
			updateCounts = simpleJdbcTemplate.batchUpdate(sqlPre,sqlStr,batch, additiveSql);
		return updateCounts;
	}

	private int[] batchUpdateByParams(String sqlStr,List<Object[]> arrayList){
		int[] updateCounts;
		if( null != this.additiveSql )
			updateCounts =  simpleJdbcTemplate.batchUpdate(sqlPre,sqlStr,arrayList,additiveSql);
		else
			updateCounts =  simpleJdbcTemplate.batchUpdate(sqlStr,arrayList);
		return updateCounts;
	}

	private void _doBatchUpdate(){
		if( log.isDebugEnabled() )
			log.debug("Start Executing Dao,key="+key+",sql="+sql);
		int[] result = null;
		long time0 = System.currentTimeMillis();
		List<Object> temp1 = null;
		List<Object[]> temp2 = null;
		
		String oldFile = tempFile; 
		FileLock fileLock = null;
		synchronized(objListLock){
			
			if(objListSize>0 || objList.size()>0 ){
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
			result = batchUpdateByPojo(sql,temp1);
			if( log.isInfoEnabled() ){
				long timeTake = System.currentTimeMillis() - time0;
				if (timeTake>1000)//�����������񳬹�1���ӡ��Ϣ
					log.info("key="+key+",Success Count ="+ result.length +",Use Millisecond="+timeTake);
				tracer.trace("key="+key+",batchUpdate takes(milliseconds):"+timeTake);
			}
			lastIoTime = System.currentTimeMillis();
			if(AsyncService.isFileCache){
				FileUtil.deleteFile(tempPath, oldFile+".data");
				FileUtil.unlockFile(fileLock);
				FileUtil.deleteFile(tempPath, oldFile+".lock");				
			}
		}
		else if( null != temp2 ){
			result = batchUpdateByParams(sql,temp2 );
			if( log.isInfoEnabled() ){
				long timeTake = System.currentTimeMillis() - time0;
				if (timeTake>1000)//�����������񳬹�1���ӡ��Ϣ
					log.info("key="+key+",Success Count ="+result.length+",Use Millisecond="+timeTake);
				tracer.trace("key="+key+",batchUpdate takes(milliseconds):"+timeTake);
			}
			lastIoTime = System.currentTimeMillis();
		}
	}
	
	public void batchUpdate() {
		if( executing )
			return;
		synchronized(batchDaoLock){
			//���ݿⲻ���ã���ֱ���˳��������档
			DbState ds = DbMonitor.getInstance().getMonitor(dataSource);
			if( null != ds && !ds.isAvailable() )
				return;
			if( null != executeThreadName ){
				log.error("BatchDao2[key="+key+"] has already been executed by : "+ executeThreadName);
			}
			executeThreadName = Thread.currentThread().getName();
			//ȡ������ɹ���־�жϣ����Ᵽ���쳣����ʱ��������޷����
			//boolean success = false;
			try{
				executing = true;
				_doBatchUpdate();
				//success = true;
			}
			catch(CannotGetJdbcConnectionException e){
				//���ݿ������쳣��֪ͨ���ģ��
				tracer.trace("batch dao2 exception: CannotGetJdbcConnectionException");
				if( null != ds )
					ds.setAvailable(false);
			}
			catch(BadSqlGrammarException e){
				//�﷨���󣬱����������ȷ����£�ϵͳ��Ӧ�ô�ӡ�˴�����Ϣ
				tracer.trace("batch dao2 exception:"+e.getLocalizedMessage(),e);
			}
			catch(Exception e){
				tracer.trace("batch dao2 exception:"+e.getLocalizedMessage(),e);
				log.warn("batch dao2 exception:"+e.getLocalizedMessage(),e);
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

	public boolean add(Object pojo){
		if( null != pojo ){
			synchronized(objListLock){
				if( size() > batchSize*10 ){
					tracer.trace("batchDao2 can not add object,key="+key+",size="+size()+",batchSize="+batchSize);
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

	/**
	 * ��������ӵ��ļ���
	 * @param pojo
	 */
	private void _add(Object pojo) {
		if(tempPath ==null)
			tempPath = "data"+File.separator+key;
		if(tempFile==null){
			tempFile = ""+UUID.randomUUID();
		}
		
		//����һ��lock�ļ�
		File lockFile = FileUtil.openFile(tempPath, tempFile+".lock");
		//���ļ�����
		FileLock fileLock = FileUtil.tryLockFile(lockFile);
		if(fileLock == null){
			tempFile = ""+UUID.randomUUID();
			lockFile = FileUtil.openFile(tempPath, tempFile+".lock");
			fileLock = FileUtil.tryLockFile(lockFile);
		}
		
		//��temp�ļ���
		File file = FileUtil.openFile(tempPath, tempFile+".data");
		
		FileUtil.writeObjectToFile(pojo, file, true);
		//�洢��ϣ����ļ�����
		FileUtil.unlockFile(fileLock);
		objListSize ++;
	}
	/**
	 * �������ļ��ж�ȡ����
	 * @param temp1
	 * @param oldFile
	 * @return
	 */
	private List<Object> _read(String oldFile,FileLock fileLock) {
		List<Object> temp1 = null;
		tempFile = ""+UUID.randomUUID();
		File file = FileUtil.openFile(tempPath, oldFile+".data");
		if(fileLock != null){
			temp1 = FileUtil.readObjectFromFile(file);
		}else{
			//��ӡ��־�������˵���ļ��Ѿ����򿪣��޷���ȡ
		}
		objListSize = 0;
		return temp1;
	}
	/**
	 * Object[] params,������Ǳ��һ�����ݡ���˳�������ö��塣
	 */
	public boolean add(Object[] params){
		if( null != params ){
			synchronized(objListLock){
				if( size() > batchSize*10 ){
					tracer.trace("batchDao2 can not add object,size="+size()+",batchSize="+batchSize);
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
	}
	
	public void setAdditiveSql(String adSql ){
		adSql = StringUtils.strip(adSql);
		adSql = StringUtils.remove(adSql, '\t');
		adSql = StringUtils.replaceChars(adSql, '\n', ' ');
		this.additiveSql = adSql;
	}
	
	public int size(){
		return Math.max(Math.max(objList.size(),objListSize), paramArrayList.size());
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
		long spa=System.currentTimeMillis() - lastIoTime;
		boolean result = spa>= delay && size()>0 ;
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

	public void setSqlPre(String sqlPre) {
		this.sqlPre = sqlPre;
	}
}
