/**
 * 基于JDBC，提供存储过程或者函数的封装。
 */
package cn.hexing.db.procedure;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.datasource.DataSourceUtils;

import cn.hexing.db.resultmap.ResultMapper;

/**
 *
 */
public class DbProcedure {
	private static final Logger log = Logger.getLogger(DbProcedure.class);
	//可配置属性
	private DataSource dataSource;
	private List<ProcParam> outParams = new ArrayList<ProcParam>();
	private List<ProcParam> inParams = new ArrayList<ProcParam>();
	private String callString;
	//内部属性
	private boolean initialized = false;
	private final Object lock = new Object();
	
	/**
	 * 缺省构造函数。需要设置属性，然后调用initialize()进行初始化。
	 */
	public DbProcedure(){}
	
	public DbProcedure(DataSource ds,String sqlConfig){
		dataSource = ds;
		callString = sqlConfig;
		checkInitialize();
	}
	
	private void initialize(String sqlConfig){
		// sql example:
		//{call comm_service.update_command_status(#id,jdbcType=NUMERIC,mode=IN#, #status,jdbcType=VARCHAR,mode=IN#, #errcode,jdbcType=VARCHAR,mode=IN#,#messageCount,jdbcType=NUMERIC,mode=IN#)}
		try{
			callString = compile(sqlConfig);			
			if( log.isDebugEnabled() )
				log.debug("call string="+callString);
		}catch(Exception e){
			log.error("sql编译错误:"+sqlConfig,e);
			throw new RuntimeException(e);
		}
	}
	
	private void checkInitialize(){
		synchronized(lock){
			if( !initialized ){
				initialized=true;
				initialize(callString);
			}
		}
	}
	
	private boolean isPrimitive(Object obj){
		Class<?> clz = obj.getClass();
		if( clz.isPrimitive() )
			return true;
		if( clz == String.class || clz == Integer.class || clz == Long.class || clz == Short.class
				|| clz == Character.class || clz == Byte.class || clz == Double.class || clz == Float.class )
			return true;
		if( obj instanceof Date )
			return true;
		return false;
	}
	
	public Object executeFunction(Object... args) throws SQLException{	
		checkInitialize();
		long time = System.currentTimeMillis();
		Connection con = DataSourceUtils.getConnection(dataSource);
		long time1 = System.currentTimeMillis();
		if (log.isDebugEnabled())
			log.debug("executeFunction get connect time="+(time1-time));
		try{
			CallableStatement procStmt = con.prepareCall(callString);
			for(ProcParam pout: outParams ){
				procStmt.registerOutParameter(pout.getParamIndex(), pout.getJdbcType());
			}
			ProcParam pin;
			boolean isBean = false;
			if( args.length == 1 ){
				isBean = ! isPrimitive(args[0]);
			}
			if( ! isBean ){
				if( inParams.size() > args.length ){
					String msg = "存储过程需要参数个数="+inParams.size()+",实际传入个数="+args.length;
					log.error(msg);
					throw new RuntimeException(msg);
				}
			}
			for(int i=0; i<inParams.size(); i++ ){
				pin = inParams.get(i);
				if( isBean )
					pin.setInputValueByBean(procStmt, args[0]);
				else
					pin.setInputValue(procStmt, args[i]);
			}
			procStmt.execute();
			Object ret = procStmt.getObject(1);
			procStmt.close();
			return ret;
		}
		catch(SQLException e){
			log.error("存储过程执行错误:"+e.getLocalizedMessage(),e);
			throw e;
		}
		finally{
			DataSourceUtils.releaseConnection(con, dataSource);
		}
	}

	/**
	 * 执行存储过程。不需要返回值的情况。
	 * @param args
	 * @return 执行成功或者失败。
	 * @throws SQLException
	 */
	public boolean execute(Object... args) throws SQLException{
		checkInitialize();
		long time = System.currentTimeMillis();	
		Connection con = DataSourceUtils.getConnection(dataSource);		
		long time1 = System.currentTimeMillis();
		if (log.isDebugEnabled())
			log.debug("execute get connect time="+(time1-time));
		try{
			CallableStatement procStmt = con.prepareCall(callString);
			for(ProcParam pout: outParams ){
				procStmt.registerOutParameter(pout.getParamIndex(), pout.getJdbcType());
			}
			ProcParam pin;
			boolean isBean = false;
			if( args.length == 1 ){
				isBean = ! isPrimitive(args[0]);
			}
			if( ! isBean ){
				if( inParams.size() > args.length ){
					String msg = "Stored Procedure Need Params Size="+inParams.size()+",Actual Size="+args.length;
					log.error(msg);
					throw new RuntimeException(msg);
				}
			}
			for(int i=0; i<inParams.size(); i++ ){
				pin = inParams.get(i);
				if( isBean )
					pin.setInputValueByBean(procStmt, args[0]);
				else
					pin.setInputValue(procStmt, args[i]);
			}
			boolean ret = procStmt.execute();
			procStmt.close();
			return ret;
		}
		catch(SQLException e){
			log.error("Stored Procedure Execute Error:"+e.getLocalizedMessage(),e);
			throw e;
		}
		finally{
			DataSourceUtils.releaseConnection(con, dataSource);
			if (log.isDebugEnabled())
				log.debug("Stored Procedure Execute Complete");
		}
	}

	/**
	 * 执行存储过程。
	 * @param args
	 * @return 存储过程返回一个记录集
	 * @throws SQLException
	 */
	public List<?> executeList(ResultMapper<?>rm,Object... args) throws SQLException{
		checkInitialize();
		long time = System.currentTimeMillis();	
		Connection con = DataSourceUtils.getConnection(dataSource);
		long time1 = System.currentTimeMillis();
		if (log.isDebugEnabled())
			log.debug("executeResultSet get connect time="+(time1-time));
		try{
			System.out.println("execute proc :" + callString );
			con.setAutoCommit(false);
			CallableStatement procStmt = con.prepareCall(callString);
			int outIndex = 0;
			for(ProcParam pout: outParams ){
				outIndex = pout.getParamIndex();
				procStmt.registerOutParameter(pout.getParamIndex(), pout.getJdbcType());
			}
			ProcParam pin;
			boolean isBean = false;
			if( args.length == 1 ){
				isBean = ! isPrimitive(args[0]);
			}
			if( ! isBean ){
				if( inParams.size() > args.length ){
					String msg = "存储过程需要参数个数="+inParams.size()+",实际传入个数="+args.length;
					log.error(msg);
					throw new RuntimeException(msg);
				}
			}
			for(int i=0; i<inParams.size(); i++ ){
				pin = inParams.get(i);
				pin.setInputValue(procStmt, args[i]);
			}
			procStmt.execute();
			//ResultSet rs = (ResultSet)procStmt.getResultSet();//适用于SQL Server
			ResultSet rs = (ResultSet)procStmt.getObject(outIndex);//适用于Oracle
			
			List<?> list =rm.mapAllRows(rs) ;
			rs.close();
			procStmt.close();
			con.commit();
			return list;
		}
		catch(SQLException e){
			log.error("存储过程执行错误:"+e.getLocalizedMessage(),e);
			throw e;
		}
		finally{
			DataSourceUtils.releaseConnection(con, dataSource);
		}
	}
	/**
	 * 执行Mysql存储过程。
	 * @param args
	 * @return 存储过程返回一个记录集
	 * @throws SQLException
	 */
	public List<?> executeList_mysql(ResultMapper<?>rm,Object... args) throws SQLException{
		checkInitialize();
		long time = System.currentTimeMillis();	
		Connection con = DataSourceUtils.getConnection(dataSource);
		long time1 = System.currentTimeMillis();
		if (log.isDebugEnabled())
			log.debug("executeResultSet get connect time="+(time1-time));
		try{
			System.out.println("execute proc :" + callString );
			CallableStatement procStmt = con.prepareCall(callString);
			for(ProcParam pout: outParams ){
				procStmt.registerOutParameter(pout.getParamIndex(), pout.getJdbcType());
			}
			ProcParam pin;
			boolean isBean = false;
			if( args.length == 1 ){
				isBean = ! isPrimitive(args[0]);
			}
			if( ! isBean ){
				if( inParams.size() > args.length ){
					String msg = "存储过程需要参数个数="+inParams.size()+",实际传入个数="+args.length;
					log.error(msg);
					throw new RuntimeException(msg);
				}
			}
			for(int i=0; i<inParams.size(); i++ ){
				pin = inParams.get(i);
				pin.setInputValue(procStmt, args[i]);
			}
			ResultSet rs = procStmt.executeQuery();//适用于Mysql
			return rm.mapAllRows(rs);
		}
		catch(SQLException e){
			log.error("存储过程执行错误:"+e.getLocalizedMessage(),e);
			throw e;
		}
		finally{
			DataSourceUtils.releaseConnection(con, dataSource);
		}
	}

	public int executeFunctionInt(Object... args) throws SQLException{
		Object ret = executeFunction(args);
		if( ret instanceof String ){
			Long lv = Long.parseLong((String)ret);
			return lv.intValue();
		}
		else if( ret instanceof Integer )
			return (Integer)ret;
		else if( ret instanceof Long )
			return ((Long)ret).intValue();
		else if( ret instanceof Short )
			return (Short)ret;
		else{
			StringBuilder sb = new StringBuilder();
			sb.append("返回类型不能转换到int.").append(" retObj.type=").append(ret.getClass().getName());
			sb.append(". retObj.value=").append(ret.toString());
			log.error(sb.toString());
			throw new RuntimeException(sb.toString());
		}
	}
	
	public long executeFunctionLong(Object... args) throws SQLException{
		Object ret = executeFunction(args);
		if( ret instanceof String )
			return Long.parseLong((String)ret);
		else if( ret instanceof Integer )
			return (Integer)ret;
		else if( ret instanceof Long )
			return (Long)ret;
		else if( ret instanceof Short )
			return (Short)ret;
		else
			throw new RuntimeException("返回类型不能转换到long");
	}
	
	public String executeFunctionString(Object... args) throws SQLException{
		Object ret = executeFunction(args);
		if( ret instanceof String )
			return (String)ret;
		else
			return ret.toString();
	}
	
	private String compile(String sqlConfig){
		//对于Ibatis风格存储过程的配置
		sqlConfig = StringUtils.strip(sqlConfig);
		int index = sqlConfig.indexOf("{");
		if( index>0 )
			sqlConfig = sqlConfig.substring(index);
		index = sqlConfig.indexOf("#");
		if( index>0 ){
			int lp = sqlConfig.indexOf("(");
			int rp = sqlConfig.indexOf(")");
			StringBuffer sb = new StringBuffer();
			//参数的左边部分
			String lsql = sqlConfig.substring(0, lp+1);
			String sparam = sqlConfig.substring(lp+1,rp);
			//处理输入、输出参数
			int iq = lsql.indexOf("?");
			int pindex = 1;
			if( iq >0 ){
				//函数调用,支持格式?#NUMERIC= 或者?:#NUMERIC
				sb.append("{?");
				int ieq = lsql.indexOf("=");
				sb.append(lsql.substring(ieq));
				String jtype = lsql.substring(iq+1,ieq);
				int escapteIndex = 0;
				for( escapteIndex=0;escapteIndex<jtype.length();escapteIndex++){
					char c = lsql.charAt(escapteIndex);
					if( c != ':' && c != '#' ){
						jtype = jtype.substring(escapteIndex);
						break;
					}
				}
				ProcParam retParam = new ProcParam("",jtype,"OUT",pindex++);
				outParams.add(retParam);
			}
			else
				sb.append(lsql);
			
			String[] parts = sparam.split(",");
			for( int i=0; i<parts.length; i +=3 ){
				String name = parts[i].substring(1);	//exp: #id
				String jtype = parts[i+1].substring("jdbcType=".length());
				String pmode = parts[i+2].substring("mode=".length(),parts[i+2].length()-1);
				ProcParam param = new ProcParam(name,jtype,pmode,pindex++);
				if( param.getParamMode() == ProcParam.MODE_IN ){
					if( i>0 )
						sb.append(",?");
					else
						sb.append("?");
					inParams.add(param);
				}
				else if( param.getParamMode() == ProcParam.MODE_OUT){
					if( i>0 )
						sb.append(",?");
					else
						sb.append("?");
					outParams.add(param);
				}
				else if( param.getParamMode() == ProcParam.MODE_INOUT){
					if( i>0 )
						sb.append(",?");
					else
						sb.append("?");
					inParams.add(param);
					ProcParam paramOut = new ProcParam(name,jtype,pmode,param.getParamIndex());
					outParams.add(paramOut);
				}
				else{
					String errinfo = "Ibatis格式存储过程配置错误，mode="+pmode;
					log.error(errinfo);
					throw new RuntimeException(errinfo);
				}
			}
			//处理后续部分
			sb.append(")}");
			return sb.toString();
		}
		return sqlConfig;
	}

	public void setOutParams(List<ProcParam> outParams) {
		this.outParams = outParams;
	}

	public void setInParams(List<ProcParam> inParams) {
		this.inParams = inParams;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void setCallString(String callString) {
		this.callString = callString;
	}
	
	
}
