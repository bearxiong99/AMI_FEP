package cn.hexing.auto.control.imp;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import cn.hexing.auto.db.AutoCreatorDao;
import cn.hexing.auto.model.Constant;
import cn.hexing.auto.model.FileUtils;



public class MysqlCodeCreator extends AbstractCodeCreator{

	public MysqlCodeCreator(DataSource dataSource) {
		super(dataSource);
	}

	@Override
	public int create(String destPath, String tableName) {
		AutoCreatorDao acd = new AutoCreatorDao(this.dataSource);
		acd.setDbType(Constant.MYSQL);
		List<Map<String, Object>> results = acd.getTabCol(tableName);
		StringBuilder selectSql = new StringBuilder("SELECT ");
		StringBuilder additiveSql = new StringBuilder("INSERT INTO"+tableName+" \n(SJID,SJSJ,JSSJ,CT,PT,ZHBL,BQBJ");
		StringBuilder additiveSqlValue=new StringBuilder("VALUES \n(:SJID, :SJSJ, :JSSJ, :CT, :PT, :ZHBL, :BQBJ ");
		StringBuilder additiveSqlValue2 = new StringBuilder("ON DUPLICATE KEY UPDATE\nJSSJ=now()");
		StringBuilder beanSettingString=new StringBuilder("");
		
		int i = 0;
		for(Map<String,Object> map:results){
			String column=(String) map.get(Constant.COLUMN_NAME.toUpperCase());
			if("SJID,SJSJ,JSSJ,CT,PT,ZHBL,BQBJ,XGBJ".contains(column.toUpperCase()))
				continue;
			selectSql.append(column).append(",");
			additiveSql.append(",").append(column);
			
			additiveSqlValue.append(",:"+column);
			
			additiveSqlValue2.append(","+column+"=:"+column);
			
			
			beanSettingString.append("<bean class=\"cn.hexing.db.resultmap.ColumnMapper\">").append("\n");
			beanSettingString.append("\t"+"<property name=\"property\" value=\""+column+"\"/>").append("\n");
			beanSettingString.append("\t<property name=\"index\" value=\""+((i++)+1)+"\"/>").append("\n");
			beanSettingString.append("</bean>").append("\n");
		}
		additiveSql.append(")\n").append(additiveSqlValue).append(")\n").append(additiveSqlValue2).append(")\n");
		selectSql.deleteCharAt(selectSql.length()-1);
		selectSql.append("\n FROM "+tableName +"\nWHERE SJID=? AND SJSJ =?");
		
		FileUtils.writeTo(new File(destPath+File.separator+"selectSql.txt"), selectSql.toString());
		FileUtils.writeTo(new File(destPath+File.separator+"additiveSql.txt"), additiveSql.toString());
		FileUtils.writeTo(new File(destPath+File.separator+"beanSettingString.txt"), beanSettingString.toString());
		
		
		return 3;
	}

}
