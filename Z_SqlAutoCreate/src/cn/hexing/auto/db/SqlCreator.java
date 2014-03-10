package cn.hexing.auto.db;

import java.util.HashMap;
import java.util.Map;

import cn.hexing.auto.model.Constant;

public class SqlCreator {
	
	private static String TABCOL="tabCol";
	private static String DBDRIVER="dbDriver";
	private static String DBDRIVER_PREFIX="dbDriverPreFix";

	public static String getTabColSql(String dbType) {
		return getSql(dbType, TABCOL);
	}
	
	public static String getDbDriver(String dbType){
		return getSql(dbType,DBDRIVER);
	}

	private static String getSql(String dbType,String sqlName){
		Map<String, String> map = sqlMap.get(dbType);
		
		if(map == null) return null;
		
		return map.get(sqlName);
	}
	
	
	static Map<String,Map<String,String>> sqlMap;
	static{
		sqlMap = new HashMap<String, Map<String,String>>();
		Map<String,String> oracleMap = new HashMap<String, String>();
		oracleMap.put(TABCOL, "	select  distinct column_name,data_type "+
								"from user_tab_columns  t" +
								" where t.TABLE_NAME=? ");
		oracleMap.put(DBDRIVER, "oracle.jdbc.driver.OracleDriver");
		oracleMap.put(DBDRIVER_PREFIX, "jdbc:oracle:thin:@");
		sqlMap.put(Constant.ORACLE, oracleMap);
		
		Map<String,String> mysqlMap = new HashMap<String, String>();
		mysqlMap.put(TABCOL, "SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE table_name = ?");
		mysqlMap.put(DBDRIVER, "com.mysql.jdbc.Driver");
		mysqlMap.put(DBDRIVER_PREFIX, "jdbc:mysql://");
		sqlMap.put(Constant.MYSQL, mysqlMap);
		
	}
	public static String getDbDriverUrl(String sDbType, String sDbIP,
			String sDbPort, String sDbName) {
		if(sDbType.equalsIgnoreCase(Constant.ORACLE)){
			return getSql(sDbType,DBDRIVER_PREFIX)+sDbIP+":"+sDbPort+":"+sDbName;	
		}else if(sDbType.equalsIgnoreCase(Constant.MYSQL)){
			return getSql(sDbType,DBDRIVER_PREFIX)+sDbIP+":"+sDbPort+"/"+sDbName+"?characterEncoding=utf8";	
		}
		return null;
	}

	
}
