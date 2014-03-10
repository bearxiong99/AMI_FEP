package cn.hexing.auto.db;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import cn.hexing.auto.model.Constant;

public class AutoCreatorDao {

	
	private SimpleJdbcTemplate simpleJdbcTemplate;
	
	private String dbType=Constant.ORACLE;
	
	public AutoCreatorDao(DataSource dataSource){
		this.setDataSource(dataSource);
	}
	public List<Map<String,Object>> getTabCol(String tabName){
		return simpleJdbcTemplate.queryForList(SqlCreator.getTabColSql(dbType), tabName);
	}
	
	
	public void setDataSource(DataSource dataSource){
		simpleJdbcTemplate = new SimpleJdbcTemplate( dataSource);
	}


	public String getDbType() {
		return dbType;
	}


	public void setDbType(String dbType) {
		this.dbType = dbType;
	}
	
}
