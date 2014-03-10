package cn.hexing.reread.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import cn.hexing.db.resultmap.ResultMapper;
import cn.hexing.reread.dao.TimeSynDao;
import cn.hexing.reread.model.ReadTimeModel;
import cn.hexing.reread.model.TimeSynStrategy;
import cn.hexing.reread.model.TimeSynTask;

public class TimeSynDaoImpl implements TimeSynDao {
	private static final Logger log = Logger.getLogger(LoadDatasDaoImpl.class);
	//是否要求BP自动对时（0-否，1-是）
	private String bpAutoTimeSyn = "0";
	
	private SimpleJdbcTemplate simpleJdbcTemplate;		//对应dataSource属性
	public void setDataSource(DataSource dataSource) {
		this.simpleJdbcTemplate = new SimpleJdbcTemplate(dataSource);
	}
	
	private String getTimeSynTasksSql;		//读取所有状态为“未读”的对时任务
	private ResultMapper<TimeSynTask> mapperGetTimeSynTasks;
	private String getReadTimeModelSql;		//读取对应单位下的所有终端表计
	private String getReadTimeModelForMeterBoxSql;
	private ResultMapper<ReadTimeModel> mapperGetReadTimeModel;
	private String getRereadTimeModelSql;		//读取对应单位下需要补召时间的终端表计
	private String getRereadTimeModelSql_autoSyn;
	private ResultMapper<ReadTimeModel> mapperGetRereadTimeModel;
	
	private String setTimeSynTaskSuccessSql;  //设置对时任务的状态为“已读”
	private String deleteReadStatusSql;//删除对应单位下的所有对时数据
	private String initReadStatusSql;//初始化对应终端的召测状态为‘0’
	private String initReadStatusForMeterBoxSql;//初始化对应终端的召测状态为‘0’
	private String getMlIdSql;
	private String getTaskIdSql;
	private String insMlSql , insSzjgSql;
	private String insTaskSql;
	private String addTimeSynLogSql; //插入自动对时日志
	protected ResultMapper<TimeSynStrategy> mapperGetTimeSynStrategy;
	private String getTimeSynStrategySql;
	private String setTimeSynStrategyStateSql;
	private String deleteTimeSynStrategySql;
	/**
	 * 读取所有状态为“未读”的对时任务
	 */
	public List<TimeSynTask> getTimeSynTasks() {
		try{
			ParameterizedRowMapper<TimeSynTask> rm = new ParameterizedRowMapper<TimeSynTask>(){
				public TimeSynTask mapRow(ResultSet rs, int rowNum) throws SQLException {
					return mapperGetTimeSynTasks.mapOneRow(rs);
				}
			};
			return simpleJdbcTemplate.query(this.getTimeSynTasksSql, rm);
		}catch(Exception ex){
			log.error("getTimeSynTasks error:"+ex.getLocalizedMessage());
			throw new RuntimeException(ex);
		}
	}
	/**
	 * 读取对应单位下的档案
	 */
	public List<ReadTimeModel> getReadTimeModel(String dwdm) {
		try{
			ParameterizedRowMapper<ReadTimeModel> rm = new ParameterizedRowMapper<ReadTimeModel>(){
				public ReadTimeModel mapRow(ResultSet rs, int rowNum) throws SQLException {
					return mapperGetReadTimeModel.mapOneRow(rs);
				}
			};
			return simpleJdbcTemplate.query(this.getReadTimeModelSql, rm , dwdm);
		}catch(Exception ex){
			log.error("getReadTimeModel error:"+ex.getLocalizedMessage());
			throw new RuntimeException(ex);
		}
	}
	/**
	 * 读取对应单位下的档案
	 */
	public List<ReadTimeModel> getReadTimeModelForMeterBox(String dwdm) {
		try{
			ParameterizedRowMapper<ReadTimeModel> rm = new ParameterizedRowMapper<ReadTimeModel>(){
				public ReadTimeModel mapRow(ResultSet rs, int rowNum) throws SQLException {
					return mapperGetReadTimeModel.mapOneRow(rs);
				}
			};
			return simpleJdbcTemplate.query(this.getReadTimeModelForMeterBoxSql, rm , dwdm);
		}catch(Exception ex){
			log.error("getReadTimeModelForMeterBox error:"+ex.getLocalizedMessage());
			throw new RuntimeException(ex);
		}
	}
	/**
	 * 读取需要补召时间的设备
	 */
	public List<ReadTimeModel> getRereadTimeModel(String dwdm) {
		try{
			ParameterizedRowMapper<ReadTimeModel> rm = new ParameterizedRowMapper<ReadTimeModel>(){
				public ReadTimeModel mapRow(ResultSet rs, int rowNum) throws SQLException {
					return mapperGetRereadTimeModel.mapOneRow(rs);
				}
			};
			if("1".equals(bpAutoTimeSyn)){
				return simpleJdbcTemplate.query(this.getRereadTimeModelSql_autoSyn, rm, dwdm);
			}else{
				return simpleJdbcTemplate.query(this.getRereadTimeModelSql, rm, dwdm);
			}
		}catch(Exception ex){
			log.error("getRereadTimeModel error:"+ex.getLocalizedMessage());
			throw new RuntimeException(ex);
		}
	}
	/**
	 * 设置对时任务状态为“1-已读”
	 */
	public int setTimeSynTaskSuccess(String dwdm, String rwlx, String zxsj) {
		try{
			return simpleJdbcTemplate.update(setTimeSynTaskSuccessSql, dwdm , rwlx , zxsj);
		}catch(Exception ex){
			log.error("setTimeSynTaskSuccess error:"+ex.getLocalizedMessage());
			throw new RuntimeException(ex);
		}
	}
	/**
	 * 初始化所有设备的时间召测状态为“0-未召测”
	 */
	public int initReadStatus(String dwdm) {
		try{
			return simpleJdbcTemplate.update(initReadStatusSql, dwdm , dwdm);
		}catch(Exception ex){
			log.error("initReadStatus error:"+ex.getLocalizedMessage());
			throw new RuntimeException(ex);
		}
	}
	
	/**
	 * 初始化所有设备的时间召测状态为“0-未召测”
	 */
	public int initReadStatusForMeterBox(String dwdm) {
		try{
			return simpleJdbcTemplate.update(initReadStatusForMeterBoxSql, dwdm , dwdm);
		}catch(Exception ex){
			log.error("initReadStatusForMeterBox error:"+ex.getLocalizedMessage());
			throw new RuntimeException(ex);
		}
	}
	
	public int deleteReadStatus(String dwdm) {
		try{
			return simpleJdbcTemplate.update(deleteReadStatusSql, dwdm);
		}catch(Exception ex){
			log.error("deleteReadStatus error:"+ex.getLocalizedMessage());
			throw new RuntimeException(ex);
		}
	}
	/**
	 * 增加自动对时日志
	 */
	public int addTimeSynLog(String rwdwdm,String rwlx,String rwzxsj, String zdljdz,String dwdm,
			int cldh,String fsqqsj,String iszj,String dsbz, int sjcfz){
		try{
			return simpleJdbcTemplate.update(addTimeSynLogSql, rwdwdm, rwlx, rwzxsj, zdljdz, dwdm, cldh, fsqqsj, iszj, dsbz, sjcfz);
		}catch(Exception ex){
			log.error("addTimeSynLog error:"+ex.getLocalizedMessage());
			throw new RuntimeException(ex);
		}
	}
	/**
	 * 命令、任务
	 */
	public long getMlId() {
		try{
			return simpleJdbcTemplate.queryForLong(getMlIdSql, new HashMap<String,String>());
		}catch(Exception ex){
			log.error("getMlId error:"+ex.getLocalizedMessage());
			throw new RuntimeException(ex);
		}
	}
	public long getTaskId() {
		try{
			return simpleJdbcTemplate.queryForLong(getTaskIdSql, new HashMap<String,String>());
		}catch(Exception ex){
			log.error("getTaskId error:"+ex.getLocalizedMessage());
			throw new RuntimeException(ex);
		}
	}
	@Transactional(propagation=Propagation.NOT_SUPPORTED) 
	public int insMl(long mlId, long taskId , String zdjh , int cldh) {
		try{
			return simpleJdbcTemplate.update(insMlSql, mlId, taskId , zdjh , cldh);
		}catch(Exception ex){
			log.error("insMl error:"+ex.getLocalizedMessage());
			throw new RuntimeException(ex);
		}
	}
	
	public int insSzjg(long mlId, String zdjh , int cldh , String sjx) {
		try{
			return simpleJdbcTemplate.update(insSzjgSql, mlId , zdjh , cldh , sjx);
		}catch(Exception ex){
			log.error("insSzjg error:"+ex.getLocalizedMessage());
			throw new RuntimeException(ex);
		}
	}
	
	@Transactional(propagation=Propagation.NOT_SUPPORTED) 
	public int insTask(long taskId , String czyId) {
		try{
			return simpleJdbcTemplate.update(insTaskSql, taskId , czyId);
		}catch(Exception ex){
			log.error("insTask error:"+ex.getLocalizedMessage());
			throw new RuntimeException(ex);
		}
	}
	/**
	 * 读取自动对时策略
	 */
	public List<TimeSynStrategy> getTimeSynStrategy() {
		try{
			ParameterizedRowMapper<TimeSynStrategy> rm = new ParameterizedRowMapper<TimeSynStrategy>(){
				public TimeSynStrategy mapRow(ResultSet rs, int rowNum) throws SQLException {
					return mapperGetTimeSynStrategy.mapOneRow(rs);
				}
			};
			return simpleJdbcTemplate.query(this.getTimeSynStrategySql, rm);
		}catch(Exception ex){
			log.error("getTimeSynStrategy error:"+ex.getLocalizedMessage());
			throw new RuntimeException(ex);
		}
	}
	/**
	 * 设置对时策略的状态
	 */
	public int setTimeSynStrategyState(String dwdm, String rwlx, String cron,
			String xgbj) {
		try{
			return simpleJdbcTemplate.update(setTimeSynStrategyStateSql, xgbj, dwdm , rwlx , cron );
		}catch(Exception ex){
			log.error("setTimeSynStrategyState error:"+ex.getLocalizedMessage());
			throw new RuntimeException(ex);
		}
	}
	/**
	 * 删除对时策略
	 */
	public int deleteTimeSynstrategy(String dwdm, String rwlx, String cron) {
		try{
			return simpleJdbcTemplate.update(deleteTimeSynStrategySql, dwdm , rwlx , cron);
		}catch(Exception ex){
			log.error("deleteTimeSynstrategy error:"+ex.getLocalizedMessage());
			throw new RuntimeException(ex);
		}
	}
	public SimpleJdbcTemplate getSimpleJdbcTemplate() {
		return simpleJdbcTemplate;
	}
	public void setSimpleJdbcTemplate(SimpleJdbcTemplate simpleJdbcTemplate) {
		this.simpleJdbcTemplate = simpleJdbcTemplate;
	}
	public String getGetTimeSynTasksSql() {
		return getTimeSynTasksSql;
	}
	public void setGetTimeSynTasksSql(String getTimeSynTasksSql) {
		this.getTimeSynTasksSql = getTimeSynTasksSql;
	}
	public ResultMapper<TimeSynTask> getMapperGetTimeSynTasks() {
		return mapperGetTimeSynTasks;
	}
	public void setMapperGetTimeSynTasks(
			ResultMapper<TimeSynTask> mapperGetTimeSynTasks) {
		this.mapperGetTimeSynTasks = mapperGetTimeSynTasks;
	}
	public String getGetReadTimeModelSql() {
		return getReadTimeModelSql;
	}
	public void setGetReadTimeModelSql(String getReadTimeModelSql) {
		this.getReadTimeModelSql = getReadTimeModelSql;
	}
	public ResultMapper<ReadTimeModel> getMapperGetReadTimeModel() {
		return mapperGetReadTimeModel;
	}
	public void setMapperGetReadTimeModel(
			ResultMapper<ReadTimeModel> mapperGetReadTimeModel) {
		this.mapperGetReadTimeModel = mapperGetReadTimeModel;
	}
	public String getGetRereadTimeModelSql() {
		return getRereadTimeModelSql;
	}
	public void setGetRereadTimeModelSql(String getRereadTimeModelSql) {
		this.getRereadTimeModelSql = getRereadTimeModelSql;
	}
	public ResultMapper<ReadTimeModel> getMapperGetRereadTimeModel() {
		return mapperGetRereadTimeModel;
	}
	public void setMapperGetRereadTimeModel(
			ResultMapper<ReadTimeModel> mapperGetRereadTimeModel) {
		this.mapperGetRereadTimeModel = mapperGetRereadTimeModel;
	}
	public String getSetTimeSynTaskSuccessSql() {
		return setTimeSynTaskSuccessSql;
	}
	public void setSetTimeSynTaskSuccessSql(String setTimeSynTaskSuccessSql) {
		this.setTimeSynTaskSuccessSql = setTimeSynTaskSuccessSql;
	}
	public String getInitReadStatusSql() {
		return initReadStatusSql;
	}
	public void setInitReadStatusSql(String initReadStatusSql) {
		this.initReadStatusSql = initReadStatusSql;
	}
	public String getGetMlIdSql() {
		return getMlIdSql;
	}
	public void setGetMlIdSql(String getMlIdSql) {
		this.getMlIdSql = getMlIdSql;
	}
	public String getGetTaskIdSql() {
		return getTaskIdSql;
	}
	public void setGetTaskIdSql(String getTaskIdSql) {
		this.getTaskIdSql = getTaskIdSql;
	}
	public String getInsMlSql() {
		return insMlSql;
	}
	public void setInsMlSql(String insMlSql) {
		this.insMlSql = insMlSql;
	}
	public String getInsTaskSql() {
		return insTaskSql;
	}
	public void setInsTaskSql(String insTaskSql) {
		this.insTaskSql = insTaskSql;
	}
	public String getAddTimeSynLogSql() {
		return addTimeSynLogSql;
	}
	public void setAddTimeSynLogSql(String addTimeSynLogSql) {
		this.addTimeSynLogSql = addTimeSynLogSql;
	}
	public String getDeleteReadStatusSql() {
		return deleteReadStatusSql;
	}
	public void setDeleteReadStatusSql(String deleteReadStatusSql) {
		this.deleteReadStatusSql = deleteReadStatusSql;
	}
	public String getGetReadTimeModelForMeterBoxSql() {
		return getReadTimeModelForMeterBoxSql;
	}
	public void setGetReadTimeModelForMeterBoxSql(
			String getReadTimeModelForMeterBoxSql) {
		this.getReadTimeModelForMeterBoxSql = getReadTimeModelForMeterBoxSql;
	}
	public String getInitReadStatusForMeterBoxSql() {
		return initReadStatusForMeterBoxSql;
	}
	public void setInitReadStatusForMeterBoxSql(String initReadStatusForMeterBoxSql) {
		this.initReadStatusForMeterBoxSql = initReadStatusForMeterBoxSql;
	}
	public String getInsSzjgSql() {
		return insSzjgSql;
	}
	public void setInsSzjgSql(String insSzjgSql) {
		this.insSzjgSql = insSzjgSql;
	}
	public ResultMapper<TimeSynStrategy> getMapperGetTimeSynStrategy() {
		return mapperGetTimeSynStrategy;
	}
	public void setMapperGetTimeSynStrategy(
			ResultMapper<TimeSynStrategy> mapperGetTimeSynStrategy) {
		this.mapperGetTimeSynStrategy = mapperGetTimeSynStrategy;
	}
	public String getGetTimeSynStrategySql() {
		return getTimeSynStrategySql;
	}
	public void setGetTimeSynStrategySql(String getTimeSynStrategySql) {
		this.getTimeSynStrategySql = getTimeSynStrategySql;
	}
	public String getSetTimeSynStrategyStateSql() {
		return setTimeSynStrategyStateSql;
	}
	public void setSetTimeSynStrategyStateSql(String setTimeSynStrategyStateSql) {
		this.setTimeSynStrategyStateSql = setTimeSynStrategyStateSql;
	}
	public String getDeleteTimeSynStrategySql() {
		return deleteTimeSynStrategySql;
	}
	public void setDeleteTimeSynStrategySql(String deleteTimeSynStrategySql) {
		this.deleteTimeSynStrategySql = deleteTimeSynStrategySql;
	}
	public String getBpAutoTimeSyn() {
		return bpAutoTimeSyn;
	}
	public void setBpAutoTimeSyn(String bpAutoTimeSyn) {
		this.bpAutoTimeSyn = bpAutoTimeSyn;
	}
	public String getGetRereadTimeModelSql_autoSyn() {
		return getRereadTimeModelSql_autoSyn;
	}
	public void setGetRereadTimeModelSql_autoSyn(
			String getRereadTimeModelSql_autoSyn) {
		this.getRereadTimeModelSql_autoSyn = getRereadTimeModelSql_autoSyn;
	}
}
