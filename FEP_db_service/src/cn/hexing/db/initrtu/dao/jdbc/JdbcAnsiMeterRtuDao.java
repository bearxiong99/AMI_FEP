package cn.hexing.db.initrtu.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import cn.hexing.db.initrtu.dao.AnsiMeterRtuDao;
import cn.hexing.db.resultmap.ResultMapper;
import cn.hexing.fk.model.MeasuredPoint;
import cn.hexing.fk.model.RtuTask;

import com.hx.ansi.model.AnsiMeterRtu;

/** 
 * @Description  xxxxx
 * @author  Rolinbor
 * @Copyright 2013 hexing Inc. All rights reserved
 * @time：2013-5-13 上午10:06:15
 * @version 1.0 
 */

public class JdbcAnsiMeterRtuDao implements AnsiMeterRtuDao{

	private String sqlLoadAnsiMeterRtu;
	private ResultMapper<AnsiMeterRtu> mapperLoadAnsiMeterRtu;
	private SimpleJdbcTemplate simpleJdbcTemplate;		//对应dataSource属性
	private String sqlLoadRtuTask;
	private ResultMapper<RtuTask> mapperLoadRtuTask;
	private String sqlLoadMasterTask;
	private String sqlLoadMeasurePoints;
	private ResultMapper<MeasuredPoint> mapperLoadMeasurePoints;
	
	
	public List<AnsiMeterRtu> loadAnsiMeterRtu(){
		ParameterizedRowMapper<AnsiMeterRtu> rowMap = new ParameterizedRowMapper<AnsiMeterRtu>(){
			public AnsiMeterRtu mapRow(ResultSet rs, int rowNum) throws SQLException {
				return mapperLoadAnsiMeterRtu.mapOneRow(rs);
			}
		};
		return this.simpleJdbcTemplate.query(sqlLoadAnsiMeterRtu, rowMap);
	}
	public final void setSqlLoadAnsiMeterRtu(String sqlLoadAnsiMeterRtu) {
		this.sqlLoadAnsiMeterRtu = sqlLoadAnsiMeterRtu;
	}

	public final void setMapperLoadAnsiMeterRtu(
			ResultMapper<AnsiMeterRtu> mapperLoadAnsiMeterRtu) {
		this.mapperLoadAnsiMeterRtu = mapperLoadAnsiMeterRtu;
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
