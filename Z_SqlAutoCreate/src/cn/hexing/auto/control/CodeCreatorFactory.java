package cn.hexing.auto.control;

import javax.sql.DataSource;

import cn.hexing.auto.control.imp.MysqlCodeCreator;
import cn.hexing.auto.control.imp.OracleCodeCreator;
import cn.hexing.auto.model.Constant;

public class CodeCreatorFactory {

	private CodeCreatorFactory(){}
	
	private static CodeCreatorFactory instance;
	
	public static CodeCreatorFactory getInstance(){
		if(instance==null)
			instance = new CodeCreatorFactory();
		return instance;
	}
	
	
	public ICodeCreator getCodeCreator(String dbType,DataSource dataSource){
		
		if(Constant.ORACLE.equalsIgnoreCase(dbType)){
			
			return new OracleCodeCreator(dataSource);
			
		}else if(Constant.MYSQL.equalsIgnoreCase(dbType)){
			return new MysqlCodeCreator(dataSource);
		}
		return null;
	}
}
