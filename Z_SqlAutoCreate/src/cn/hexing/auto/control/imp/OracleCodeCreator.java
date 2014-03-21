package cn.hexing.auto.control.imp;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import cn.hexing.auto.db.AutoCreatorDao;
import cn.hexing.auto.model.Constant;
import cn.hexing.auto.model.FileUtils;


public class OracleCodeCreator extends AbstractCodeCreator{

	public OracleCodeCreator(DataSource dataSource) {
		super(dataSource);
	}

	@Override
	public int create(String destPath, String tableName) {
		
		AutoCreatorDao acd = new AutoCreatorDao(this.dataSource);
		List<Map<String, Object>> results = acd.getTabCol(tableName);
		
		StringBuilder insertTempSql=new StringBuilder("INSERT INTO TEMP_"+tableName.toUpperCase()+"(SJID,SJSJ,JSSJ,CT,PT,ZHBL,BQBJ");
		StringBuilder insertValues=new StringBuilder("VALUES(:SJID,:SJSJ,:JSSJ,:CT,:PT,:ZHBL,:BQBJ");

		StringBuilder mergeInto=new StringBuilder("MERGE INTO "+tableName+" t \n"
        		+"USING (select sjid, sjsj, max(jssj) as jssj, max(ct) AS ct, max(pt) AS pt, max(zhbl) as zhbl, max(bqbj) AS bqbj,max(xgbj) AS xgbj");
		StringBuilder mergeAppendMatched= new StringBuilder("\n"+"from TEMP_"+tableName+" group by sjid, sjsj) s \n");
		mergeAppendMatched.append(" ON (t.sjid = s.sjid AND t.sjsj = s.sjsj)")
				   .append("WHEN MATCHED THEN \n")
				   .append("UPDATE SET t.jssj=nvl(s.jssj, sysdate), t.ct=s.ct, t.pt=s.pt, t.zhbl=s.zhbl, t.bqbj=s.bqbj, t.xgbj=s.xgbj \n");
		StringBuilder mergeAppendNotMathed=new StringBuilder("\n WHEN NOT MATCHED THEN\n");
		mergeAppendNotMathed.append("INSERT (sjid, sjsj, jssj, ct, pt, zhbl, bqbj,xgbj ");
		
		StringBuilder mergeAppendNotMathedAppned = new StringBuilder("VALUES (s.sjid, s.sjsj, nvl(s.jssj, sysdate), s.ct, s.pt, s.zhbl, s.bqbj,0 ");
		
		StringBuilder javaBeanAppend = new StringBuilder();
		
		for(Map<String,Object> map:results){
			String column=(String) map.get(Constant.COLUMN_NAME.toUpperCase());
			if("SJID,SJSJ,JSSJ,CT,PT,ZHBL,BQBJ,XGBJ".contains(column.toUpperCase()))
				continue;
			if(column.toUpperCase().contains("VEE"))
				continue;
			
			
			insertTempSql.append(",").append(column);
			String dataType=(String) map.get(Constant.COLUMN_TYPE.toUpperCase());
			insertValues.append(",:").append(column);
			mergeInto.append(",max(").append(column).append(") AS ").append(column);
			if(isTimeValue(dataType)){
				mergeAppendMatched.append(",").append("t."+column+"=NVL(TO_DATE(S."+column+",'YYYY-MM-DD HH24:MI:SS'),T."+column+")");
				mergeAppendNotMathedAppned.append(",TO_DATE(S."+column+",'YYYY-MM-DD HH24:MI:SS')");
			}else{
				mergeAppendMatched.append(",").append("t.").append(column).append("=nvl(s."+column+", t."+column+")");
				mergeAppendNotMathedAppned.append(",s."+column);
			}
			mergeAppendNotMathed.append(",").append(column);
			javaBeanAppend.append("private String "+column.toUpperCase()+";\n");
		}
		insertTempSql.append(")");
		insertValues.append(")");
		mergeAppendNotMathed.append(")");
		mergeAppendNotMathedAppned.append(")");
		mergeAppendNotMathedAppned.append("\nLOG ERRORS INTO "+tableName+"_log (to_char(sysdate, 'YYYYMMDDHH24MISS')) REJECT LIMIT UNLIMITED");

		
		insertTempSql.append("\n").append(insertValues);
		mergeInto.append(mergeAppendMatched).append(mergeAppendNotMathed).append(mergeAppendNotMathedAppned);
		
		FileUtils.writeTo(new File(destPath+File.separator+"insertTempSql.txt"), insertTempSql.toString());
		
		FileUtils.writeTo(new File(destPath+File.separator+"mergeSql.txt"), mergeInto.toString());
		
		FileUtils.writeTo(new File(destPath+File.separator+"javaBean.txt"), javaBeanAppend.toString());

		
		return 3;
	}

	private boolean isTimeValue(String dataType) {
		return dataType.equalsIgnoreCase("DATE")?true:false;
	}

}
