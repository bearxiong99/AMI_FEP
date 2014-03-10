/**
 * 数据库状态类
 */
package cn.hexing.db;

import java.sql.Connection;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.util.Assert;

/**
 *
 */
public class DbState {
	private static final Logger log = Logger.getLogger(DbState.class);
	//可配置属性
	private String name = "defaultDb";
	private DataSource dataSource;
	private String testSql = "select * from dual";
	//状态属性
	private boolean available = false;

	private DbState(){}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public String getTestSql() {
		return testSql;
	}

	public void setTestSql(String testSql) {
		this.testSql = testSql;
	}

	public boolean isAvailable() {
		return available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}
	
	public boolean testDbConnection(){
		Assert.notNull(dataSource, "dataSource must not be null");
		
		if( null == testSql || testSql.length()<5 )
			return true;
		
		Connection con = null;
		try{
			con = DataSourceUtils.getConnection(dataSource);
			con.createStatement().executeQuery(this.testSql);
			available=true;
		}catch(Exception e){
			available=false;
			log.error("dbname="+name+" is not available!error:"+e.getLocalizedMessage());
		}
		finally{
			DataSourceUtils.releaseConnection(con, dataSource);
		}
		return available;
	}
	
	
}
