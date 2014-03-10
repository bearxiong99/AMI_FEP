package cn.hexing.reread.test;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.dbcp.BasicDataSource;

import cn.hexing.db.procedure.DbProcedure;
import cn.hexing.db.resultmap.ResultMapper;
import cn.hexing.reread.model.RereadPoint;
import cn.hexing.reread.utils.IntervalUnit;
import cn.hexing.reread.utils.TimePointUtils;

public class LoadDatasDaoImplTest {
	/**
	 * ���Դ���
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			
			//testOracle1();
			//testMysql();
			new LoadDatasDaoImplTest.TestThread(0).start();
			new LoadDatasDaoImplTest.TestThread(1).start();
			new LoadDatasDaoImplTest.TestThread(0).start();
			new LoadDatasDaoImplTest.TestThread(1).start();
			new LoadDatasDaoImplTest.TestThread(0).start();
			new LoadDatasDaoImplTest.TestThread(1).start();
			new LoadDatasDaoImplTest.TestThread(0).start();
			new LoadDatasDaoImplTest.TestThread(1).start();
			new LoadDatasDaoImplTest.TestThread(0).start();
			new LoadDatasDaoImplTest.TestThread(1).start();
			new LoadDatasDaoImplTest.TestThread(0).start();
			new LoadDatasDaoImplTest.TestThread(1).start();
			new LoadDatasDaoImplTest.TestThread(0).start();
			new LoadDatasDaoImplTest.TestThread(1).start();
			new LoadDatasDaoImplTest.TestThread(0).start();
			new LoadDatasDaoImplTest.TestThread(1).start();
			new LoadDatasDaoImplTest.TestThread(0).start();
			new LoadDatasDaoImplTest.TestThread(1).start();
			new LoadDatasDaoImplTest.TestThread(0).start();
			new LoadDatasDaoImplTest.TestThread(1).start();
			new LoadDatasDaoImplTest.TestThread(0).start();
			new LoadDatasDaoImplTest.TestThread(1).start();
			new LoadDatasDaoImplTest.TestThread(0).start();
			new LoadDatasDaoImplTest.TestThread(1).start();
			new LoadDatasDaoImplTest.TestThread(0).start();
			new LoadDatasDaoImplTest.TestThread(1).start();
			new LoadDatasDaoImplTest.TestThread(0).start();
			new LoadDatasDaoImplTest.TestThread(1).start();
			new LoadDatasDaoImplTest.TestThread(0).start();
			new LoadDatasDaoImplTest.TestThread(1).start();
			new LoadDatasDaoImplTest.TestThread(0).start();
			new LoadDatasDaoImplTest.TestThread(1).start();
			new LoadDatasDaoImplTest.TestThread(0).start();
			new LoadDatasDaoImplTest.TestThread(1).start();
			new LoadDatasDaoImplTest.TestThread(0).start();
			new LoadDatasDaoImplTest.TestThread(1).start();
			new LoadDatasDaoImplTest.TestThread(0).start();
			new LoadDatasDaoImplTest.TestThread(1).start();
			new LoadDatasDaoImplTest.TestThread(0).start();
			new LoadDatasDaoImplTest.TestThread(1).start();
			new LoadDatasDaoImplTest.TestThread(0).start();
			new LoadDatasDaoImplTest.TestThread(1).start();
			new LoadDatasDaoImplTest.TestThread(0).start();
			new LoadDatasDaoImplTest.TestThread(1).start();
			new LoadDatasDaoImplTest.TestThread(0).start();
			new LoadDatasDaoImplTest.TestThread(1).start();
			new LoadDatasDaoImplTest.TestThread(0).start();
			new LoadDatasDaoImplTest.TestThread(1).start();
			new LoadDatasDaoImplTest.TestThread(0).start();
			new LoadDatasDaoImplTest.TestThread(1).start();
			new LoadDatasDaoImplTest.TestThread(0).start();
			new LoadDatasDaoImplTest.TestThread(1).start();
			new LoadDatasDaoImplTest.TestThread(0).start();
			new LoadDatasDaoImplTest.TestThread(1).start();
			new LoadDatasDaoImplTest.TestThread(0).start();
			new LoadDatasDaoImplTest.TestThread(1).start();
			new LoadDatasDaoImplTest.TestThread(0).start();
			new LoadDatasDaoImplTest.TestThread(1).start();
			new LoadDatasDaoImplTest.TestThread(0).start();
			new LoadDatasDaoImplTest.TestThread(1).start();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	public static void testOracle1() throws ParseException{
		SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		List<Date> timePoints = TimePointUtils.createTimePointsByRange(15, IntervalUnit.DAY.value(), 
				formatDate.parse("2013-08-26 00:00:00"),formatDate.parse("2013-08-30 00:01:00"));
		ResultMapper<RereadPoint> tempMapper = new ResultMapper<RereadPoint>();
		tempMapper.setResultClass(RereadPoint.class.getName());
		tempMapper.setColumnSequence("dataItemId,timePoint,taskNo,commAddr,terminalAddr,cldh,taskType");
	
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		List<String> timePointsStr = new ArrayList<String>();
		for(int i=0; i<timePoints.size(); i++){
			timePointsStr.add(df.format(timePoints.get(i)));
		}
		String mbid="11733";
		String taskNo = "111";
		String gylx="03";
		float zxsj = 256f;
		String rwlx="02";
		DbProcedure procGetRereadPoint = new DbProcedure();
		 BasicDataSource ds = new BasicDataSource();
		    ds.setDriverClassName(oracle.jdbc.driver.OracleDriver.class.getName());
		    ds.setUrl("jdbc:oracle:thin:@192.168.2.176:1521:ami");
		    ds.setUsername("AMI3");
		    ds.setPassword("AMI3");
		   
		    procGetRereadPoint.setDataSource(ds);
		 /**
		  * {call PKG_FEP_SERVICES.audit_omissive_data(
		  * 	#p_mbid,jdbcType=VARCHAR,mode=IN#,
		  * 	#sjsj_table,jdbcType=ORACLEARRAY|array_varchar2,mode=IN#,
		  * 	#ref_cursor,jdbcType=ORACLECURSOR,mode=OUT#,
		  * 	#p_dwdm,jdbcType=VARCHAR,mode=IN#,
		  * 	#p_dbgylx,jdbcType=VARCHAR,mode=IN#,
		  * 	#p_jssj,jdbcType=FLOAT,mode=IN#,
		  * 	#p_rwlx,jdbcType=VARCHAR,mode=IN#)}
		  */
		 String callStr = "{call PKG_FEP_SERVICES.audit_omissive_data(" +
	 		"#p_mbid,jdbcType=VARCHAR,mode=IN#," +
	 		"#sjsj_table,jdbcType=ORACLEARRAY|array_varchar2,mode=IN#," +
	 		"#ref_cursor,jdbcType=ORACLECURSOR,mode=OUT#," +
	 		"#p_dwdm,jdbcType=VARCHAR,mode=IN#," +
	 		"#p_dbgylx,jdbcType=VARCHAR,mode=IN#," +
	 		"#p_jssj,jdbcType=FLOAT,mode=IN#"+
	 		",#p_rwlx,jdbcType=VARCHAR,mode=IN#"+
	 		")}"
			;
		procGetRereadPoint.setCallString(callStr);
		//System.out.println(callStr);
		//System.out.println(timePointsStr);
		try {
			@SuppressWarnings("unchecked")
			List<RereadPoint> res = (List<RereadPoint>) procGetRereadPoint.executeList(tempMapper,  mbid, timePointsStr.toArray(),"",gylx,zxsj,rwlx);
			//System.out.println("==============SIZE:" + res.size() +"=====================") ;
			for(RereadPoint point:res){
				if(!point.getTaskNo().equals(taskNo))
					System.out.println("testOracle1*********************************:" + point.toString());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public static void testOracle() throws ParseException{
		SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		List<Date> timePoints = TimePointUtils.createTimePointsByRange(15, IntervalUnit.MUNITE.value(), 
				formatDate.parse("2013-08-26 17:05:00"),formatDate.parse("2013-08-26 17:25:00"));
		ResultMapper<RereadPoint> tempMapper = new ResultMapper<RereadPoint>();
		tempMapper.setResultClass(RereadPoint.class.getName());
		tempMapper.setColumnSequence("dataItemId,timePoint,taskNo,commAddr,terminalAddr,cldh,taskType");
	
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		List<String> timePointsStr = new ArrayList<String>();
		for(int i=0; i<timePoints.size(); i++){
			timePointsStr.add(df.format(timePoints.get(i)));
		}
		String mbid="12213";
		String taskNo = "4";
		String gylx="03";
		float zxsj = 256f;
		String rwlx="01";
		DbProcedure procGetRereadPoint = new DbProcedure();
		 BasicDataSource ds = new BasicDataSource();
		    ds.setDriverClassName(oracle.jdbc.driver.OracleDriver.class.getName());
		    ds.setUrl("jdbc:oracle:thin:@192.168.2.176:1521:ami");
		    ds.setUsername("AMI3");
		    ds.setPassword("AMI3");
		   
		    procGetRereadPoint.setDataSource(ds);
		 /**
		  * {call PKG_FEP_SERVICES.audit_omissive_data(
		  * 	#p_mbid,jdbcType=VARCHAR,mode=IN#,
		  * 	#sjsj_table,jdbcType=ORACLEARRAY|array_varchar2,mode=IN#,
		  * 	#ref_cursor,jdbcType=ORACLECURSOR,mode=OUT#,
		  * 	#p_dwdm,jdbcType=VARCHAR,mode=IN#,
		  * 	#p_dbgylx,jdbcType=VARCHAR,mode=IN#,
		  * 	#p_jssj,jdbcType=FLOAT,mode=IN#,
		  * 	#p_rwlx,jdbcType=VARCHAR,mode=IN#)}
		  */
		 String callStr = "{call PKG_FEP_SERVICES.audit_omissive_data(" +
	 		"#p_mbid,jdbcType=VARCHAR,mode=IN#," +
	 		"#sjsj_table,jdbcType=ORACLEARRAY|array_varchar2,mode=IN#," +
	 		"#ref_cursor,jdbcType=ORACLECURSOR,mode=OUT#," +
	 		"#p_dwdm,jdbcType=VARCHAR,mode=IN#," +
	 		"#p_dbgylx,jdbcType=VARCHAR,mode=IN#," +
	 		"#p_jssj,jdbcType=FLOAT,mode=IN#"+
	 		",#p_rwlx,jdbcType=VARCHAR,mode=IN#"+
	 		")}"
			;
		procGetRereadPoint.setCallString(callStr);
		//System.out.println(callStr);
		//System.out.println(timePointsStr);
		try {
			@SuppressWarnings("unchecked")
			List<RereadPoint> res = (List<RereadPoint>) procGetRereadPoint.executeList(tempMapper,  mbid, timePointsStr.toArray(),"",gylx,zxsj,rwlx);
			//System.out.println("==============SIZE:" + res.size() +"=====================") ;
			for(RereadPoint point:res){
				if(!point.getTaskNo().equals(taskNo))
				System.out.println("testOracle*********************************:" + point.toString());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void testMysql() throws ParseException{
		SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		ResultMapper<RereadPoint> tempMapper = new ResultMapper<RereadPoint>();
		tempMapper.setResultClass(RereadPoint.class.getName());
		tempMapper.setColumnSequence("dataItemId,timePoint,taskNo,commAddr,terminalAddr,cldh,taskType");
		int interval = 1;
		String intervalUnit =  IntervalUnit.DAY.value();
		int sjjg = TimePointUtils.transIntervalToMinute(interval, intervalUnit);
		String mbid = "19";
		String dwdm = "334010101";
		String gylx = "03";
		float zxsj = 300f;//在线时间
		String rwlx="02";
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		List<String> timePointsStr = new ArrayList<String>();
		List<Date> timePoints = TimePointUtils.createTimePointsByRange(interval, intervalUnit, 
				formatDate.parse("2013-10-17 15:20:00"),formatDate.parse("2013-10-18 15:20:00"));
		for(int i=0; i<timePoints.size(); i++){
			timePointsStr.add(df.format(timePoints.get(i)));
		}
		DbProcedure procGetRereadPoint = new DbProcedure();
		 BasicDataSource ds = new BasicDataSource();
		    ds.setDriverClassName(com.mysql.jdbc.Driver.class.getName());
		    ds.setUrl("jdbc:mysql://192.168.2.170:3306/ami4?characterEncoding=utf8");
		    ds.setUsername("root");
		    ds.setPassword("root");
		   
		    procGetRereadPoint.setDataSource(ds);
		 String callStr = "{call audit_omissive_data(" +
	 		"#p_mbid,jdbcType=VARCHAR,mode=IN#," +
	 		"#startTime,jdbcType=VARCHAR,mode=IN#," +
	 		"#endTime,jdbcType=VARCHAR,mode=IN#," +
	 		"#sjjg,jdbcType=INTEGER,mode=IN#," +
	 		"#p_dwdm,jdbcType=VARCHAR,mode=IN#," +
	 		"#p_dbgylx,jdbcType=VARCHAR,mode=IN#," +
	 		"#p_jssj,jdbcType=FLOAT,mode=IN#"+
	 		",#p_rwlx,jdbcType=VARCHAR,mode=IN#"+
	 		")}"
			;
		procGetRereadPoint.setCallString(callStr);
		System.out.println(callStr);
		System.out.println(timePointsStr);
		try {
			List<RereadPoint> res = (List<RereadPoint>) procGetRereadPoint.executeList_mysql(tempMapper, mbid ,timePointsStr.get(0),timePointsStr.get(timePointsStr.size()-1),sjjg,dwdm,gylx,zxsj,rwlx);
			System.out.println("==============SIZE:" + res.size() +"=====================") ;
			for(RereadPoint point:res){
				System.out.println(point.toString());
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static class TestThread extends Thread{
		private int i;
		
		public TestThread(int i) {
			super();
			this.i = i;
		}

		public synchronized void run() {
				try {
					if(i==0) testOracle();
					else if(i==1) testOracle1();
				} catch (ParseException e) {
					e.printStackTrace();
				}
		}
		
	}
}
