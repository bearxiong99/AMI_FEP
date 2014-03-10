package cn.hexing.rmi.client.control;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import cn.hexing.db.resultmap.ResultMapper;
import cn.hexing.rmi.client.model.LeftTreeNode;


public class RmiClientDb {
	
	private DataSource dataSource;
	private SimpleJdbcTemplate simpleJdbcTemplate;
	
	private String sqlGetDepartment;
	private String sqlGetRootDepartment;
	private String sqlGetCircuit;
	private String sqlGetDistrict;
	private String sqlGetTransformer;
	private String sqlGetTerminal;
	private ResultMapper<LeftTreeNode> mapperGetDepartment;
	private String sqlQueryTerminal;
	public List<LeftTreeNode> getDepartment(String pid) {

		ParameterizedRowMapper<LeftTreeNode> rm = new ParameterizedRowMapper<LeftTreeNode>() {

			@Override
			public LeftTreeNode mapRow(ResultSet rs, int rowNum)
					throws SQLException {
				return mapperGetDepartment.mapOneRow(rs);
			}
		};
		return simpleJdbcTemplate.query(sqlGetDepartment, rm, pid);
	}
	
	public List<LeftTreeNode> getRootDepartment() {

		ParameterizedRowMapper<LeftTreeNode> rm = new ParameterizedRowMapper<LeftTreeNode>() {

			@Override
			public LeftTreeNode mapRow(ResultSet rs, int rowNum)
					throws SQLException {
				return mapperGetDepartment.mapOneRow(rs);
			}
		};
		return simpleJdbcTemplate.query(sqlGetRootDepartment, rm);
	}
	
	public List<LeftTreeNode> getCircuit(String pid) {

		ParameterizedRowMapper<LeftTreeNode> rm = new ParameterizedRowMapper<LeftTreeNode>() {

			@Override
			public LeftTreeNode mapRow(ResultSet rs, int rowNum)
					throws SQLException {
				return mapperGetDepartment.mapOneRow(rs);
			}
		};
		return simpleJdbcTemplate.query(sqlGetCircuit, rm, pid);
	}
	
	public List<LeftTreeNode> getDistrict(String pid) {

		ParameterizedRowMapper<LeftTreeNode> rm = new ParameterizedRowMapper<LeftTreeNode>() {

			@Override
			public LeftTreeNode mapRow(ResultSet rs, int rowNum)
					throws SQLException {
				return mapperGetDepartment.mapOneRow(rs);
			}
		};
		return simpleJdbcTemplate.query(sqlGetDistrict, rm, pid);
	}
	
	public List<LeftTreeNode> getTtransformer(String pid) {

		ParameterizedRowMapper<LeftTreeNode> rm = new ParameterizedRowMapper<LeftTreeNode>() {

			@Override
			public LeftTreeNode mapRow(ResultSet rs, int rowNum)
					throws SQLException {
				return mapperGetDepartment.mapOneRow(rs);
			}
		};
		return simpleJdbcTemplate.query(sqlGetTransformer, rm, pid);
	}
	

	public List<LeftTreeNode> getTerminal(String pid) {

		ParameterizedRowMapper<LeftTreeNode> rm = new ParameterizedRowMapper<LeftTreeNode>() {

			@Override
			public LeftTreeNode mapRow(ResultSet rs, int rowNum)
					throws SQLException {
				return mapperGetDepartment.mapOneRow(rs);
			}
		};
		return simpleJdbcTemplate.query(sqlGetTerminal, rm, pid);
	}
	
	
	public List<String> queryTerminal(String zdljdz) {
		List<Map<String, Object>> resultsMap = simpleJdbcTemplate.queryForList(sqlQueryTerminal, "%"+zdljdz+"%");
		
		List<String> result = new ArrayList<String>();
		
		for(Map<String,Object> o : resultsMap){
			String str=(String) o.get("ZDLJDZ");
			result.add(str);
		}
		
		return result;
	}
	
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		simpleJdbcTemplate = new SimpleJdbcTemplate(this.dataSource);
	}



	public void setSqlGetDepartment(String sqlGetDepartment) {
		this.sqlGetDepartment = sqlGetDepartment;
	}



	public void setMapperGetDepartment(
			ResultMapper<LeftTreeNode> mapperGetDepartment) {
		this.mapperGetDepartment = mapperGetDepartment;
	}


	public void setSqlGetRootDepartment(String sqlGetRootDepartment) {
		this.sqlGetRootDepartment = sqlGetRootDepartment;
	}

	public void setSqlGetCircuit(String sqlGetCircuit) {
		this.sqlGetCircuit = sqlGetCircuit;
	}

	public void setSqlGetDistrict(String sqlGetDistrict) {
		this.sqlGetDistrict = sqlGetDistrict;
	}

	public void setSqlGetTransformer(String sqlGetTtransformer) {
		this.sqlGetTransformer = sqlGetTtransformer;
	}

	public void setSqlGetTerminal(String sqlGetTerminal) {
		this.sqlGetTerminal = sqlGetTerminal;
	}

	public void setSqlQueryTerminal(String sqlQueryTerminal) {
		this.sqlQueryTerminal = sqlQueryTerminal;
	}


}
