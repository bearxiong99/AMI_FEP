package cn.hexing.db.initrtu.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import cn.hexing.db.initrtu.dao.DlmsMeterRtuDao;
import cn.hexing.db.resultmap.ResultMapper;
import cn.hexing.fk.model.DlmsMeterRtu;
import cn.hexing.fk.model.MeasuredPoint;
import cn.hexing.fk.model.RtuTask;

public class JdbcDlmsMeterRtuDao implements DlmsMeterRtuDao{

	private String sqlLoadDlmsMeterRtu;
	private ResultMapper<DlmsMeterRtu> mapperLoadDlmsMeterRtu;
	private SimpleJdbcTemplate simpleJdbcTemplate;		//∂‘”¶dataSource Ù–‘
	private String sqlLoadRtuTask;
	private ResultMapper<RtuTask> mapperLoadRtuTask;
	private String sqlLoadMasterTask;
	private String sqlLoadMeasurePoints;
	private ResultMapper<MeasuredPoint> mapperLoadMeasurePoints;
	
	
	public List<DlmsMeterRtu> loadDlmsMeterRtu(){
		ParameterizedRowMapper<DlmsMeterRtu> rowMap = new ParameterizedRowMapper<DlmsMeterRtu>(){
			public DlmsMeterRtu mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperLoadDlmsMeterRtu.mapOneRow(rs);
			}
		};
		return this.simpleJdbcTemplate.query(sqlLoadDlmsMeterRtu, rowMap);
	}
	public final void setSqlLoadDlmsMeterRtu(String sqlLoadDlmsMeterRtu) {
		this.sqlLoadDlmsMeterRtu = sqlLoadDlmsMeterRtu;
	}

	public final void setMapperLoadDlmsMeterRtu(
			ResultMapper<DlmsMeterRtu> mapperLoadDlmsMeterRtu) {
		this.mapperLoadDlmsMeterRtu = mapperLoadDlmsMeterRtu;
	}
	public void setDataSource(DataSource dataSource) {
		this.simpleJdbcTemplate = new SimpleJdbcTemplate(dataSource);
	}
	
	public List<MeasuredPoint> loadMeasuredPoints() {
		ParameterizedRowMapper<MeasuredPoint> rowMap = new ParameterizedRowMapper<MeasuredPoint>(){
			public MeasuredPoint mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperLoadMeasurePoints.mapOneRow(rs);
			}
		};
		return this.simpleJdbcTemplate.query(sqlLoadMeasurePoints, rowMap);
	}

	
	
	public List<RtuTask> loadRtuTasks() {
		ParameterizedRowMapper<RtuTask> rowMap = new ParameterizedRowMapper<RtuTask>(){
			public RtuTask mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperLoadRtuTask.mapOneRow(rs);
			}
		};
		return this.simpleJdbcTemplate.query(sqlLoadRtuTask, rowMap);
	}
	

	
	public List<RtuTask> loadMasterTasks() {
		ParameterizedRowMapper<RtuTask> rowMap = new ParameterizedRowMapper<RtuTask>(){
			public RtuTask mapRow(ResultSet rs, int rowNum) throws SQLException {
	
				return mapperLoadRtuTask.mapOneRow(rs);
			}
		};
		return this.simpleJdbcTemplate.query(sqlLoadMasterTask, rowMap);
	}
	public final void setSimpleJdbcTemplate(SimpleJdbcTemplate simpleJdbcTemplate) {
		this.simpleJdbcTemplate = simpleJdbcTemplate;
	}
	public final void setSqlLoadRtuTask(String sqlLoadRtuTask) {
		this.sqlLoadRtuTask = sqlLoadRtuTask;
	}
	public final void setMapperLoadRtuTask(ResultMapper<RtuTask> mapperLoadRtuTask) {
		this.mapperLoadRtuTask = mapperLoadRtuTask;
	}
	public final void setSqlLoadMasterTask(String sqlLoadMasterTask) {
		this.sqlLoadMasterTask = sqlLoadMasterTask;
	}
	public final void setSqlLoadMeasurePoints(String sqlLoadMeasurePoints) {
		this.sqlLoadMeasurePoints = sqlLoadMeasurePoints;
	}
	public final void setMapperLoadMeasurePoints(
			ResultMapper<MeasuredPoint> mapperLoadMeasurePoints) {
		this.mapperLoadMeasurePoints = mapperLoadMeasurePoints;
	}
}
