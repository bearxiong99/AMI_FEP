package cn.hexing.db.batch.dao.jdbc;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import cn.hexing.db.DbMonitor;
import cn.hexing.db.DbState;
import cn.hexing.db.batch.dao.IBatchDao;
import cn.hexing.db.batch.dao.jdbc.springwrap.NamedParameterUtils2;
import cn.hexing.db.resultmap.ResultMapper;
import cn.hexing.fk.tracelog.TraceLog;
import cn.hexing.fk.utils.StringUtil;


/**
 * 思路：
 * 	  1）每个业务逻辑Service包含多个带Key的DAO对象。
 * 	  2）业务逻辑在线程池中执行报文处理，然后把结果放入到相应DAO对象；
 * 	  3）在业务逻辑线程中，必须定时（delay秒）调用add(null);
 */
public class JdbcBatchDao4Mysql implements IBatchDao {
	private static final Logger log = Logger.getLogger(JdbcBatchDao4Mysql.class);
	private static final TraceLog tracer = TraceLog.getTracer("jdbcBatchdao2");
	//可配置属性
	private BatchSimpleJdbcTemplate simpleJdbcTemplate;		//对应dataSource属性
	private DataSource dataSource;
	private String sql,sqlPre=null,additiveSql=null;
	private Object additiveParameter;		//用于附加SQL的参数输入。
	private int key;
	private int batchSize = 2000;
	private long delay = 5000;		//不足批量，最迟5秒必须保存
	//内部属性
	private List<Object> objList = new ArrayList<Object>();
	private List<Object[]> paramArrayList = new ArrayList<Object[]>();
	private Object batchDaoLock = new Object(), objListLock = new Object();
	private long lastIoTime = System.currentTimeMillis();
	private String executeThreadName = null;
	private boolean executing = false;
	
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		this.simpleJdbcTemplate = new BatchSimpleJdbcTemplate(dataSource);
	}

	private int[] batchUpdateByPojo(String sqlStr,List<Object> pojoList){
		
		reconstructedTask(pojoList);
		
		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(pojoList.toArray());
		int[] updateCounts ;
		if( null != this.additiveSql ){
			if( null != this.additiveParameter ){
				final SqlParameterSource sqlParaSource = new BeanPropertySqlParameterSource(additiveParameter);
				String sqlToUse = NamedParameterUtils2.substituteNamedParameters(additiveSql, sqlParaSource );
				updateCounts = simpleJdbcTemplate.batchUpdate(sqlPre,sqlStr,batch, sqlToUse);
			}
			else{
				updateCounts = simpleJdbcTemplate.batchUpdate(additiveSql, batch);
			}
		}
		else
			updateCounts = simpleJdbcTemplate.batchUpdate(sqlPre,sqlStr,batch, additiveSql);
		
		return updateCounts;
	}
	
	private void reconstructedTaskValue(Class<? extends Object> clazz,Object srcObj,Object destObj){

		try {
			Field[] fields=clazz.getDeclaredFields();
			for(Field field:fields){
				String fieldName = field.getName();
				if("SJID".equals(fieldName)||"SJSJ".equals(fieldName)
					||"JSSJ".equals(fieldName)||"CT".equals(fieldName)
					||"PT".equals(fieldName)||"ZHBL".equals(fieldName)
					||"BQBJ".equals(fieldName)){
					continue;
				}
				PropertyDescriptor pd = null;
				try {
					pd = new PropertyDescriptor(fieldName,clazz);
				} catch (Exception e) {
					continue;
				}
				//获得任务数据对应属性的数据
				Method method = pd.getReadMethod();
				if(method == null) continue;
				String source=(String) method.invoke(srcObj);
				String dest=(String) method.invoke(destObj);
				
				if(source==null) continue;
				
				method = pd.getWriteMethod();
				if(dest==null){
					method.invoke(destObj, source);
					continue;
				}
			}
		} catch (SecurityException e) {
			log.error(StringUtil.getExceptionDetailInfo(e));
		} catch (IllegalArgumentException e) {
			log.error(StringUtil.getExceptionDetailInfo(e));
		}  catch (IllegalAccessException e) {
			log.error(StringUtil.getExceptionDetailInfo(e));
		} catch (InvocationTargetException e) {
			log.error(StringUtil.getExceptionDetailInfo(e));
		}
	
	}
	
	/**
	 * 任务数据重构
	 * @param pojoList
	 */
	private void reconstructedTask(List<Object> pojoList) {
		Map<String,Object> mapList = new HashMap<String, Object>();
		for(Object o:pojoList){
			Class<? extends Object> clazz = o.getClass();
			try {
				Method getSJID=clazz.getMethod("getSJID");
				Method getSJSJ=clazz.getMethod("getSJSJ");
				String sjid=(String) getSJID.invoke(o);
				Date sjsj=(Date) getSJSJ.invoke(o);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String key=sjid+"#"+sdf.format(sjsj);
				Object obj = mapList.get(key);
				if(obj!=null){
					reconstructedTaskValue(clazz,o,obj);
					mapList.put(key, obj);
				}else{
					mapList.put(key, o);
				}
			} catch (SecurityException e) {
				log.error(StringUtil.getExceptionDetailInfo(e));
			} catch (IllegalArgumentException e) {
				log.error(StringUtil.getExceptionDetailInfo(e));
			} catch (NoSuchMethodException e) {
				log.error(StringUtil.getExceptionDetailInfo(e));
			} catch (IllegalAccessException e) {
				log.error(StringUtil.getExceptionDetailInfo(e));
			} catch (InvocationTargetException e) {
				log.error(StringUtil.getExceptionDetailInfo(e));
			}
		}
		pojoList.clear();
		for(Iterator<String> it=mapList.keySet().iterator();it.hasNext();){
			String key=it.next();
			pojoList.add(mapList.get(key));
			
		}
		for(Object o:pojoList){
			Class<? extends Object> clazz = o.getClass();
			try {
				Method getSJID=clazz.getMethod("getSJID");
				Method getSJSJ=clazz.getMethod("getSJSJ");
				String sjid=(String) getSJID.invoke(o);
				Date sjsj=(Date) getSJSJ.invoke(o);
				List<Object> tasks=queryTaskData(sjid,sjsj);
				if(tasks.size()==1){
					Object task=tasks.get(0);
					//获得所有属性
					reconstructedTaskValue(clazz,task,o);
				}
				
			} catch (SecurityException e) {
				log.error(StringUtil.getExceptionDetailInfo(e));
			} catch (NoSuchMethodException e) {
				log.error(StringUtil.getExceptionDetailInfo(e));
			} catch (IllegalArgumentException e) {
				log.error(StringUtil.getExceptionDetailInfo(e));
			} catch (IllegalAccessException e) {
				log.error(StringUtil.getExceptionDetailInfo(e));
			} catch (InvocationTargetException e) {
				log.error(StringUtil.getExceptionDetailInfo(e));
			} 
		}
	}
	
	private ResultMapper<Object> mapperLoadTaskData;
	
	private List<Object> queryTaskData(String sjid,Date sjsj) {
		ParameterizedRowMapper<Object> rowMap = new ParameterizedRowMapper<Object>(){
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperLoadTaskData.mapOneRow(rs);
			}
		};
		return this.simpleJdbcTemplate.query(sql, rowMap,sjid,sjsj);
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
			log.debug("开始执行Dao，key="+key+",sql="+sql);
		int[] result = null;
		long time0 = System.currentTimeMillis();
		List<Object> temp1 = null;
		List<Object[]> temp2 = null;
		
		synchronized(objListLock){
			if( objList.size()>0 ){
				temp1 = objList;
				objList = new ArrayList<Object>();
			} else if( paramArrayList.size()>0 ){
				temp2 = paramArrayList;
				paramArrayList = new ArrayList<Object[]>();
			}
		}
		
		if( null != temp1 ){
			result = batchUpdateByPojo(sql,temp1);
			if( log.isInfoEnabled() ){
				long timeTake = System.currentTimeMillis() - time0;
				if (timeTake>1000)//批量保存任务超过1秒打印信息
					log.info("key="+key+",成功条数="+ result.length +",花费毫秒="+timeTake);
				tracer.trace("key="+key+",batchUpdate takes(milliseconds):"+timeTake);
			}
			lastIoTime = System.currentTimeMillis();
		}
		else if( null != temp2 ){
			result = batchUpdateByParams(sql,temp2 );
			if( log.isInfoEnabled() ){
				long timeTake = System.currentTimeMillis() - time0;
				if (timeTake>1000)//批量保存任务超过1秒打印信息
					log.info("key="+key+",成功条数="+result.length+",花费毫秒="+timeTake);
				tracer.trace("key="+key+",batchUpdate takes(milliseconds):"+timeTake);
			}
			lastIoTime = System.currentTimeMillis();
		}
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
				log.error("BatchDao2[key="+key+"] has already been executed by : "+ executeThreadName);
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
				tracer.trace("batch dao2 exception: CannotGetJdbcConnectionException");
				if( null != ds )
					ds.setAvailable(false);
			}
			catch(BadSqlGrammarException e){
				//语法错误，必须纠正。正确情况下，系统不应该打印此错误信息
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
	 * Object[] params,代表的是表的一行数据。其顺序按照配置定义。
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
		return Math.max(objList.size(), paramArrayList.size());
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

	public final void setMapperLoadTaskData(ResultMapper<Object> mapperLoadTaskData) {
		this.mapperLoadTaskData = mapperLoadTaskData;
	}
}
