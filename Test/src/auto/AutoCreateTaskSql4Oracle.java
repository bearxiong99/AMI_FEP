package auto;

/**
 * 
 * @author gaoll
 *
 * @time 2013-5-9 上午10:45:07
 *
 * @info use for oracle.
 */
public class AutoCreateTaskSql4Oracle {

	public static void main(String[] args) {
		
		String table = "SB_DLSJ_YDJ";
		
		String value = "ZXYGZ,ZXYGZ1,ZXYGZ2,ZXYGZ3,ZXYGZ4,FXYGZ,FXYGZ1,FXYGZ2,FXYGZ3,FXYGZ4,ZXWGZ,ZXWGZ1,ZXWGZ2,ZXWGZ3,ZXWGZ4,FXWGZ,FXWGZ1,FXWGZ2,FXWGZ3,FXWGZ4,ZXYGZDXL,ZXYGZDXLFSSJ,ZXYGZDXL1,ZXYGZDXL1FSSJ,ZXYGZDXL2,ZXYGZDXL2FSSJ,ZXYGZDXL3,ZXYGZDXL3FSSJ,ZXYGZDXL4,ZXYGZDXL4FSSJ,FXYGZDXL,FXYGZDXLFSSJ,ZXWGZDXL,ZXWGZDXLFSSJ,ZXWGZDXL1,ZXWGZDXL1FSSJ,ZXWGZDXL2,ZXWGZDXL2FSSJ,ZXWGZDXL3,ZXWGZDXL3FSSJ,ZXWGZDXL4,ZXWGZDXL4FSSJ,FXWGZDXL,FXWGZDXLFSSJ,WGZXX1,WGZXX2,WGZXX3,WGZXX4,FXWGZXX1,FXWGZXX2,FXWGZXX3,FXWGZXX4,ZXYGZXL,ZXYGZXLFSSJ,ZXYGZXL1,ZXYGZXL1FSSJ,ZXYGZXL2,ZXYGZXL2FSSJ,ZXYGZXL3,ZXYGZXL3FSSJ,ZXYGZXL4,ZXYGZXL4FSSJ,ZXYGSZZXL,ZXYGSZZXLFSSJ,ZXYGSZZXL1,ZXYGSZZXL1FSSJ,ZXYGSZZXL2,ZXYGSZZXL2FSSJ,ZXYGSZZXL3,ZXYGSZZXL3FSSJ,ZXYGSZZXL4,ZXYGSZZXL4FSSJ,SYXLJGSJ,XLZQS,ZXYGLJZDXL,ZXYGLJZDXL1,ZXYGLJZDXL2,ZXYGLJZDXL3,ZXYGLJZDXL4,ZJYCXLFWSJ,XLFWCS,ZJHGS,YPJGLYS,YPJGLYS1,YPJGLYS2,YPJGLYS3,YPJGLYS4,FXYGSZZXL,FXYGSZZXLFSSJ,FXYGSZZXL1,FXYGSZZXL1FSSJ,FXYGSZZXL2,FXYGSZZXL2FSSJ,FXYGSZZXL3,FXYGSZZXL3FSSJ,FXYGSZZXL4,FXYGSZZXL4FSSJ,UFERDL1,UFERDL2,UFERDL3,UFERDL4,DMCRXL1,DMCRXL2,DMCRXL3,DMCRXL4,DBYE";
		String insertTemp ="INSERT INTO TEMP_"+table+"(SJID,SJSJ,JSSJ,CT,PT,ZHBL,BQBJ,"+value+")";
		String insertValues="VALUES(:SJID,:SJSJ,:JSSJ,:CT,:PT,:ZHBL,:BQBJ";
		String[] values = value.split(",");
		for(String str:values){
			insertValues+=",:"+str;
		}
		insertValues+=")";
		System.out.println(insertTemp);
		System.out.println(insertValues);
		
		String mergeInto = "MERGE INTO "+table+" t \n"
        		+"USING (select sjid, sjsj, max(jssj) as jssj, max(ct) AS ct, max(pt) AS pt, max(zhbl) as zhbl, max(bqbj) AS bqbj,max(xgbj) AS xgbj";
		for(String str:values){
			mergeInto+=",max("+str+") AS "+str;
		}
		System.out.println();System.out.println();
		mergeInto+="\n"+"from TEMP_"+table+" group by sjid, sjsj) s \n";
		mergeInto+="ON (t.sjid = s.sjid AND t.sjsj = s.sjsj)";
		mergeInto+="WHEN MATCHED THEN \n";
		mergeInto+="UPDATE SET t.jssj=nvl(s.jssj, sysdate), t.ct=s.ct, t.pt=s.pt, t.zhbl=s.zhbl, t.bqbj=s.bqbj, t.xgbj=s.xgbj, \n";
		for(String str:values){
			if(isTimeValue(str)){
				mergeInto+="t."+str+"=NVL(TO_DATE(S."+str+",'YYYY-MM-DD HH24:MI:SS'),T."+str+"),";
			}else{
				mergeInto+="t."+str+"=nvl(s."+str+", t."+str+"),";				
			}
		}
		mergeInto=mergeInto.substring(0, mergeInto.length()-1);
		
		mergeInto+="\n WHEN NOT MATCHED THEN\n";
		mergeInto+="INSERT (sjid, sjsj, jssj, ct, pt, zhbl, bqbj,xgbj ";
		for(String str:values){
			mergeInto+=","+str;
		}
		mergeInto+=")";
		mergeInto+="VALUES (s.sjid, s.sjsj, nvl(s.jssj, sysdate), s.ct, s.pt, s.zhbl, s.bqbj,0 ";
		for(String str:values){
			//TO_DATE(S.ZXYGZDXLFSSJ,'YYYY-MM-DD HH24:MI:SS')
			if(isTimeValue(str)){
				mergeInto+=",TO_DATE(S."+str+",'YYYY-MM-DD HH24:MI:SS')";
			}else{
				mergeInto+=",s."+str;				
			}
		}
		mergeInto+=")";
		mergeInto+="\nLOG ERRORS INTO "+table+"_log (to_char(sysdate, 'YYYYMMDDHH24MISS')) REJECT LIMIT UNLIMITED";
		
		System.out.println(mergeInto);
		
	}

	private static boolean isTimeValue(String str) { //SYXLJGSJ
		return str.toUpperCase().endsWith("SJ") && 
		!str.toUpperCase().equals("DQXLZQSYSJ")&& 
		!str.toUpperCase().equals("SYXLJGSJ") ; //这个字段虽然是以SJ结尾，但不是时间数据
	}
}
