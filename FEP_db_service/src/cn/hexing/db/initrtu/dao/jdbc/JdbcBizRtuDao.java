package cn.hexing.db.initrtu.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import cn.hexing.db.initrtu.dao.BizRtuDao;
import cn.hexing.db.resultmap.ResultMapper;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.model.MeasuredPoint;
import cn.hexing.fk.model.RtuAlertCode;
import cn.hexing.fk.model.RtuAlertCodeArg;
import cn.hexing.fk.model.RtuTask;
import cn.hexing.fk.model.TaskDbConfig;
import cn.hexing.fk.model.TaskTemplate;
import cn.hexing.fk.model.TaskTemplateItem;

public class JdbcBizRtuDao implements BizRtuDao {
	//可配置属性
	private String sqlLoadRtu;
	private String sqlLoadGwRtu;
	private ResultMapper<BizRtu> mapperLoadRtu;
	
	private String sqlLoadMeasurePoints;
	private ResultMapper<MeasuredPoint> mapperLoadMeasurePoints;

	private String sqlLoadAlertCode;
	private ResultMapper<RtuAlertCode> mapperLoadAlertCode;
	
	private String sqlLoadAlertCodeArgs; 
	private ResultMapper<RtuAlertCodeArg> mapperLoadAlertCodeArgs;

	private String sqlLoadMasterTask;
	private String sqlLoadRtuTask;
	private ResultMapper<RtuTask> mapperLoadRtuTask;
	
	private String sqlLoadTaskDbConfig;
	private ResultMapper<TaskDbConfig> mapperLoadTaskDbConfig;
	
	private String sqlLoadMasterTemplate;
	private ResultMapper<TaskTemplate> mapperLoadMasterTaskTemplate;
	private String sqlLoadTaskTemplate;
	private ResultMapper<TaskTemplate> mapperLoadTaskTemplate;
	private String sqlLoadTaskTemplateItem;
	private String sqlLoadMasterTemplateItem;
	private ResultMapper<TaskTemplateItem> mapperLoadTaskTemplateItem;

	private SimpleJdbcTemplate simpleJdbcTemplate;		//对应dataSource属性
	
	public void setDataSource(DataSource dataSource) {
		this.simpleJdbcTemplate = new SimpleJdbcTemplate(dataSource);
	}

	public List<BizRtu> loadBizRtu() {
		ParameterizedRowMapper<BizRtu> rowMap = new ParameterizedRowMapper<BizRtu>(){
			public BizRtu mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperLoadRtu.mapOneRow(rs);
			}
		};
		return this.simpleJdbcTemplate.query(sqlLoadRtu, rowMap);
	}
	
	public List<BizRtu> loadBizGwRtu() {
		ParameterizedRowMapper<BizRtu> rowMap = new ParameterizedRowMapper<BizRtu>(){
			public BizRtu mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperLoadRtu.mapOneRow(rs);
			}
		};
		return this.simpleJdbcTemplate.query(sqlLoadGwRtu, rowMap);
	}

	public List<MeasuredPoint> loadMeasuredPoints() {
		ParameterizedRowMapper<MeasuredPoint> rowMap = new ParameterizedRowMapper<MeasuredPoint>(){
			public MeasuredPoint mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperLoadMeasurePoints.mapOneRow(rs);
			}
		};
		return this.simpleJdbcTemplate.query(sqlLoadMeasurePoints, rowMap);
	}

	private List<RtuAlertCodeArg> loadRtuAlertCodeArgs() {
		ParameterizedRowMapper<RtuAlertCodeArg> rowMap = new ParameterizedRowMapper<RtuAlertCodeArg>(){
			public RtuAlertCodeArg mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperLoadAlertCodeArgs.mapOneRow(rs);
			}
		};
		return this.simpleJdbcTemplate.query(sqlLoadAlertCodeArgs, rowMap);
	}

	public List<RtuAlertCode> loadRtuAlertCodes() {
		ParameterizedRowMapper<RtuAlertCode> rowMap = new ParameterizedRowMapper<RtuAlertCode>(){
			public RtuAlertCode mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperLoadAlertCode.mapOneRow(rs);
			}
		};
		List<RtuAlertCode> alertCodeList = this.simpleJdbcTemplate.query(sqlLoadAlertCode, rowMap);
		HashMap<String,RtuAlertCode> map = new HashMap<String,RtuAlertCode>();
		for( RtuAlertCode acode: alertCodeList){
			map.put(acode.getCode(), acode);
		}
		List<RtuAlertCodeArg> args = loadRtuAlertCodeArgs();
		for( RtuAlertCodeArg arg : args ){
			RtuAlertCode acode = map.get(arg.getCode());
			if( null != acode )
				acode.getArgs().add(arg.getSjx());
		}
		map.clear();
		args.clear();
		return alertCodeList;
	}

	public List<RtuTask> loadRtuTasks() {
		ParameterizedRowMapper<RtuTask> rowMap = new ParameterizedRowMapper<RtuTask>(){
			public RtuTask mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperLoadRtuTask.mapOneRow(rs);
			}
		};
		return this.simpleJdbcTemplate.query(sqlLoadRtuTask, rowMap);
	}
	@Override
	public List<RtuTask> loadMasterTasks() {
		ParameterizedRowMapper<RtuTask> rowMap = new ParameterizedRowMapper<RtuTask>(){
			public RtuTask mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperLoadRtuTask.mapOneRow(rs);
			}
		};
		return this.simpleJdbcTemplate.query(sqlLoadMasterTask, rowMap);
	}

	public List<TaskDbConfig> loadTaskDbConfig() {
		ParameterizedRowMapper<TaskDbConfig> rowMap = new ParameterizedRowMapper<TaskDbConfig>(){
			public TaskDbConfig mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperLoadTaskDbConfig.mapOneRow(rs);
			}
		};
		return this.simpleJdbcTemplate.query(sqlLoadTaskDbConfig, rowMap);
	}

	public List<TaskTemplate> loadTaskTemplate() {
		ParameterizedRowMapper<TaskTemplate> rowMap = new ParameterizedRowMapper<TaskTemplate>(){
			public TaskTemplate mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperLoadTaskTemplate.mapOneRow(rs);
			}
		};
		Map<String,TaskTemplate> map = new HashMap<String,TaskTemplate>();
		List<TaskTemplate> taskTemps = this.simpleJdbcTemplate.query(sqlLoadTaskTemplate, rowMap);
		for(TaskTemplate tt: taskTemps ){
			map.put(tt.getTaskTemplateID(), tt);
		}
		
		rowMap = new ParameterizedRowMapper<TaskTemplate>(){
			public TaskTemplate mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperLoadMasterTaskTemplate.mapOneRow(rs);
			}
		};
		List<TaskTemplate> taskTemp=this.simpleJdbcTemplate.query(sqlLoadMasterTemplate,rowMap);
	
		for(TaskTemplate tt: taskTemp ){
			map.put(tt.getTaskTemplateID(), tt);
		}
		taskTemps.addAll(taskTemp);
		List<TaskTemplateItem> taskTempItems = loadTaskTemplateItem();
		for( TaskTemplateItem ttItem: taskTempItems ){
			TaskTemplate tt = map.get(ttItem.getTaskTemplateID());
			if( null != tt )
				tt.addDataCode(ttItem.getCode());
		}
		taskTempItems = loadMasterTaskTemplateItem();
		for( TaskTemplateItem ttItem: taskTempItems ){
			TaskTemplate tt = map.get(ttItem.getTaskTemplateID());
			if( null != tt )
				tt.addDataCode(ttItem.getCode());
		}
		return taskTemps;
	}

	private List<TaskTemplateItem> loadMasterTaskTemplateItem() {
		ParameterizedRowMapper<TaskTemplateItem> rowMap = new ParameterizedRowMapper<TaskTemplateItem>(){
			public TaskTemplateItem mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperLoadTaskTemplateItem.mapOneRow(rs);
			}
		};
		return this.simpleJdbcTemplate.query(sqlLoadMasterTemplateItem, rowMap);
	}

	private List<TaskTemplateItem> loadTaskTemplateItem() {
		ParameterizedRowMapper<TaskTemplateItem> rowMap = new ParameterizedRowMapper<TaskTemplateItem>(){
			public TaskTemplateItem mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperLoadTaskTemplateItem.mapOneRow(rs);
			}
		};
		return this.simpleJdbcTemplate.query(sqlLoadTaskTemplateItem, rowMap);
	}

	public void setSqlLoadRtu(String sqlLoadRtu) {
		this.sqlLoadRtu = sqlLoadRtu;
	}

	public void setSqlLoadMeasurePoints(String sqlLoadMeasurePoints) {
		this.sqlLoadMeasurePoints = sqlLoadMeasurePoints;
	}

	public void setSqlLoadAlertCode(String sqlLoadAlertCode) {
		this.sqlLoadAlertCode = sqlLoadAlertCode;
	}

	public void setSqlLoadRtuTask(String sqlLoadRtuTask) {
		this.sqlLoadRtuTask = sqlLoadRtuTask;
	}

	public void setSqlLoadTaskDbConfig(String sqlLoadTaskDbConfig) {
		this.sqlLoadTaskDbConfig = sqlLoadTaskDbConfig;
	}

	public void setSqlLoadTaskTemplate(String sqlLoadTaskTemplate) {
		this.sqlLoadTaskTemplate = sqlLoadTaskTemplate;
	}

	public void setSqlLoadTaskTemplateItem(String sqlLoadTaskTemplateItem) {
		this.sqlLoadTaskTemplateItem = sqlLoadTaskTemplateItem;
	}

	public void setSqlLoadAlertCodeArgs(String sqlLoadAlertCodeArgs) {
		this.sqlLoadAlertCodeArgs = sqlLoadAlertCodeArgs;
	}

	public void setMapperLoadRtu(ResultMapper<BizRtu> mapperLoadRtu) {
		this.mapperLoadRtu = mapperLoadRtu;
	}

	public void setMapperLoadMeasurePoints(
			ResultMapper<MeasuredPoint> mapperLoadMeasurePoints) {
		this.mapperLoadMeasurePoints = mapperLoadMeasurePoints;
	}

	public void setMapperLoadAlertCode(
			ResultMapper<RtuAlertCode> mapperLoadAlertCode) {
		this.mapperLoadAlertCode = mapperLoadAlertCode;
	}

	public void setMapperLoadAlertCodeArgs(
			ResultMapper<RtuAlertCodeArg> mapperLoadAlertCodeArgs) {
		this.mapperLoadAlertCodeArgs = mapperLoadAlertCodeArgs;
	}

	public void setMapperLoadRtuTask(ResultMapper<RtuTask> mapperLoadRtuTask) {
		this.mapperLoadRtuTask = mapperLoadRtuTask;
	}

	public void setMapperLoadTaskDbConfig(
			ResultMapper<TaskDbConfig> mapperLoadTaskDbConfig) {
		this.mapperLoadTaskDbConfig = mapperLoadTaskDbConfig;
	}

	public void setMapperLoadTaskTemplate(
			ResultMapper<TaskTemplate> mapperLoadTaskTemplate) {
		this.mapperLoadTaskTemplate = mapperLoadTaskTemplate;
	}

	public void setMapperLoadTaskTemplateItem(
			ResultMapper<TaskTemplateItem> mapperLoadTaskTemplateItem) {
		this.mapperLoadTaskTemplateItem = mapperLoadTaskTemplateItem;
	}

	public void setSqlLoadGwRtu(String sqlLoadGwRtu) {
		this.sqlLoadGwRtu = sqlLoadGwRtu;
	}
	public void setSqlLoadMasterTask(String sqlLoadMasterTask){
		this.sqlLoadMasterTask = sqlLoadMasterTask;
	}

	public final void setSqlLoadMasterTemplate(String sqlLoadMasterTemplate) {
		this.sqlLoadMasterTemplate = sqlLoadMasterTemplate;
	}

	public final void setSimpleJdbcTemplate(SimpleJdbcTemplate simpleJdbcTemplate) {
		this.simpleJdbcTemplate = simpleJdbcTemplate;
	}

	public final void setMapperLoadMasterTaskTemplate(
			ResultMapper<TaskTemplate> mapperLoadMasterTaskTemplate) {
		this.mapperLoadMasterTaskTemplate = mapperLoadMasterTaskTemplate;
	}

	public final void setSqlLoadMasterTemplateItem(String sqlLoadMasterTemplateItem) {
		this.sqlLoadMasterTemplateItem = sqlLoadMasterTemplateItem;
	}

}
