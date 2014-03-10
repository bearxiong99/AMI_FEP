package cn.hexing.reread.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import cn.hexing.db.procedure.DbProcedure;
import cn.hexing.db.resultmap.ResultMapper;
import cn.hexing.fk.model.DlmsItemRelated;
import cn.hexing.reread.dao.LoadDatasDao;
import cn.hexing.reread.model.MasterReread;
import cn.hexing.reread.model.RereadPoint;
import cn.hexing.reread.model.RereadStrategy;

/**
 * 数据库访问Dao层Oracle实现
 * @ClassName:LoadDatasDaoImpl
 * @Description:TODO
 * @author kexl
 * @date 2012-9-24 上午11:00:40
 *
 */
public class LoadDatasDaoImpl implements LoadDatasDao {
	private static final Logger log = Logger.getLogger(LoadDatasDaoImpl.class);
	private SimpleJdbcTemplate simpleJdbcTemplate;		//对应dataSource属性
	public void setDataSource(DataSource dataSource) {
		this.simpleJdbcTemplate = new SimpleJdbcTemplate(dataSource);
	}
	private String getRereadStrategySql;		//读取所有的补召策略
	private ResultMapper<RereadStrategy> mapperGetRereadStrategy;
	private DbProcedure procGetRereadPoint;			//获取任务模板、时间点对应的漏点
	private ResultMapper<RereadPoint> mapperGetRereadPoint;
	private String getMasterRereadSql;			//读取主站发起的补召
	private ResultMapper<MasterReread> mapperGetMasterReread;
	private String setMasterRereadSuccessSql;  //设置主站补召状态为完成
	
	private String sqlLoadItemRelated; //读取DLMS数据项
	private ResultMapper<DlmsItemRelated>mapperLoadItemRelated;
	
	private String setRereadStrategyXgbjSql; //修改补召策略的xgbj
	private String deleteRereadStrategySql; //删除补召策略
	/**
	 * 读取MB_BZCL表配置的补召策略
	 */
	public List<RereadStrategy> getRereadStrategy(String protocol) {
		try{
			ParameterizedRowMapper<RereadStrategy> rm = new ParameterizedRowMapper<RereadStrategy>(){
				public RereadStrategy mapRow(ResultSet rs, int rowNum) throws SQLException {
					return mapperGetRereadStrategy.mapOneRow(rs);
				}
			};
			return simpleJdbcTemplate.query(this.getRereadStrategySql, rm,protocol,protocol);
		}catch(Exception ex){
			log.error("getRereadStrategy error:"+ex.getLocalizedMessage());
			throw new RuntimeException(ex);
		}
	}
	/**
	 * 调用存储过程PKG_FEP_SERVICES.audit_omissive_data查询对应模板、对应时间点的漏点
	 */
	@SuppressWarnings("unchecked")
	public synchronized List<RereadPoint> getRereadPoint(Object... args) {
		try{
			return (List<RereadPoint>) procGetRereadPoint.executeList(mapperGetRereadPoint, args);
		}catch(Exception ex){
			log.error("getRereadPoint error:"+ex.getLocalizedMessage(), ex);
			throw new RuntimeException(ex);
		}
	}
	@SuppressWarnings("unchecked")
	public List<RereadPoint> getRereadPoint_mysql(Object... args) {
		try{
			return (List<RereadPoint>) procGetRereadPoint.executeList_mysql(mapperGetRereadPoint, args);
		}catch(Exception ex){
			log.error("getRereadPoint error:"+ex.getLocalizedMessage(), ex);
			throw new RuntimeException(ex);
		}
	}
	/**
	 * 读取待完成的主站补召任务
	 */
	public List<MasterReread> getMasterReread(String protocol){
		try{
			ParameterizedRowMapper<MasterReread> rm = new ParameterizedRowMapper<MasterReread>(){
				public MasterReread mapRow(ResultSet rs, int rowNum) throws SQLException {
					return mapperGetMasterReread.mapOneRow(rs);
				}
			};
			return simpleJdbcTemplate.query(this.getMasterRereadSql, rm, protocol , protocol);
		}catch(Exception ex){
			log.error("getMasterReread error:"+ex.getLocalizedMessage());
			throw new RuntimeException(ex);
		}
	}
	/**
	 * 设置主站补召任务状态为成功
	 */
	public int setMasterRereadSuccess(String templateId , Date createTime , String state ,String rwlx){
		try{
			return simpleJdbcTemplate.update(setMasterRereadSuccessSql, state , templateId , createTime ,rwlx);
		}catch(Exception ex){
			log.error("setMasterRereadSuccess error:"+ex.getLocalizedMessage());
			throw new RuntimeException(ex);
		}
	}

	public int setRereadStrategyXgbj(String xgbj , String templateId ,String rwlx){
		try{
			return simpleJdbcTemplate.update(setRereadStrategyXgbjSql, xgbj , templateId ,rwlx);
		}catch(Exception ex){
			log.error("setRereadStrategyXgbj error:"+ex.getLocalizedMessage());
			throw new RuntimeException(ex);
		}
	}
	
	public int deleteRereadStrategy(String templateId ,String rwlx){
		try{
			return simpleJdbcTemplate.update(deleteRereadStrategySql, templateId ,rwlx);
		}catch(Exception ex){
			log.error("deleteRereadStrategy error:"+ex.getLocalizedMessage());
			throw new RuntimeException(ex);
		}
	}
	/**
	 * 获得obis与code 的对应关系
	 * @return
	 */
	public List<DlmsItemRelated> loadDlmsItemRelated(){
		ParameterizedRowMapper<DlmsItemRelated> rowMap = new ParameterizedRowMapper<DlmsItemRelated>(){
			public DlmsItemRelated mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperLoadItemRelated.mapOneRow(rs);
			}
		};
		return this.simpleJdbcTemplate.query(sqlLoadItemRelated, rowMap);
	}
	public SimpleJdbcTemplate getSimpleJdbcTemplate() {
		return simpleJdbcTemplate;
	}
	public void setSimpleJdbcTemplate(SimpleJdbcTemplate simpleJdbcTemplate) {
		this.simpleJdbcTemplate = simpleJdbcTemplate;
	}
	public String getGetRereadStrategySql() {
		return getRereadStrategySql;
	}
	public void setGetRereadStrategySql(String getRereadStrategySql) {
		this.getRereadStrategySql = getRereadStrategySql;
	}
	public ResultMapper<RereadStrategy> getMapperGetRereadStrategy() {
		return mapperGetRereadStrategy;
	}
	public void setMapperGetRereadStrategy(
			ResultMapper<RereadStrategy> mapperGetRereadStrategy) {
		this.mapperGetRereadStrategy = mapperGetRereadStrategy;
	}
	
	public DbProcedure getProcGetRereadPoint() {
		return procGetRereadPoint;
	}
	public void setProcGetRereadPoint(DbProcedure procGetRereadPoint) {
		this.procGetRereadPoint = procGetRereadPoint;
	}
	public ResultMapper<RereadPoint> getMapperGetRereadPoint() {
		return mapperGetRereadPoint;
	}
	public void setMapperGetRereadPoint(
			ResultMapper<RereadPoint> mapperGetRereadPoint) {
		this.mapperGetRereadPoint = mapperGetRereadPoint;
	}
	
	public String getGetMasterRereadSql() {
		return getMasterRereadSql;
	}
	public void setGetMasterRereadSql(String getMasterRereadSql) {
		this.getMasterRereadSql = getMasterRereadSql;
	}
	public ResultMapper<MasterReread> getMapperGetMasterReread() {
		return mapperGetMasterReread;
	}
	public void setMapperGetMasterReread(
			ResultMapper<MasterReread> mapperGetMasterReread) {
		this.mapperGetMasterReread = mapperGetMasterReread;
	}
	public String getSetMasterRereadSuccessSql() {
		return setMasterRereadSuccessSql;
	}
	public void setSetMasterRereadSuccessSql(String setMasterRereadSuccessSql) {
		this.setMasterRereadSuccessSql = setMasterRereadSuccessSql;
	}
	public String getSqlLoadItemRelated() {
		return sqlLoadItemRelated;
	}
	public void setSqlLoadItemRelated(String sqlLoadItemRelated) {
		this.sqlLoadItemRelated = sqlLoadItemRelated;
	}
	public ResultMapper<DlmsItemRelated> getMapperLoadItemRelated() {
		return mapperLoadItemRelated;
	}
	public void setMapperLoadItemRelated(
			ResultMapper<DlmsItemRelated> mapperLoadItemRelated) {
		this.mapperLoadItemRelated = mapperLoadItemRelated;
	}
	public String getSetRereadStrategyXgbjSql() {
		return setRereadStrategyXgbjSql;
	}
	public void setSetRereadStrategyXgbjSql(String setRereadStrategyXgbjSql) {
		this.setRereadStrategyXgbjSql = setRereadStrategyXgbjSql;
	}
	public String getDeleteRereadStrategySql() {
		return deleteRereadStrategySql;
	}
	public void setDeleteRereadStrategySql(String deleteRereadStrategySql) {
		this.deleteRereadStrategySql = deleteRereadStrategySql;
	}
	
	
}
