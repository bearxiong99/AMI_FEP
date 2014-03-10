/**
 * 定义存储过程的输入输出参数。
 */
package cn.hexing.db.procedure;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleTypes;
import oracle.sql.ARRAY;
import oracle.sql.ArrayDescriptor;

import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.jdbc.support.nativejdbc.C3P0NativeJdbcExtractor;

/**
 *
 */
public class ProcParam {
	public static final int MODE_IN = 0;
	public static final int MODE_OUT = 1;
	public static final int MODE_INOUT = 2;
	private static final Map<String,Integer> typeMap = new HashMap<String,Integer>();
	static{
		typeMap.put("CHAR", Types.CHAR);
		typeMap.put("NUMERIC", Types.NUMERIC);
		typeMap.put("DECIMAL", Types.DECIMAL);
		typeMap.put("INTEGER", Types.INTEGER);
		typeMap.put("SMALLINT", Types.SMALLINT);
		typeMap.put("FLOAT", Types.FLOAT);		//6
		typeMap.put("REAL", Types.REAL);
		typeMap.put("DOUBLE", Types.DOUBLE);	//8
		typeMap.put("VARCHAR", Types.VARCHAR);	//12
		typeMap.put("TINYINT", Types.TINYINT);	//-6
		typeMap.put("BIGINT", Types.BIGINT);	//-5
		typeMap.put("DATE", Types.DATE);	//91
		typeMap.put("TIME", Types.TIME);	//92
		typeMap.put("TIMESTAMP", Types.TIMESTAMP);	//93
		typeMap.put("BOOLEAN", Types.BOOLEAN);	//16
		typeMap.put("CURSOR",Types.OTHER);
		typeMap.put("ORACLECURSOR",OracleTypes.CURSOR);
		typeMap.put("ORACLEARRAY",OracleTypes.ARRAY);
	}
	private String pname;
	private int jdbcType = Types.VARCHAR;
	private int index;
	private int mode = MODE_IN;
	private String dbRealType = null;
	
	public ProcParam(String paramName,String jtype,String pmode,int index){
		pname = paramName;
		if(jtype.toUpperCase().startsWith("ORACLEARRAY|")){
			/************数组类型，javaType=ARRAY|数据库定义的实际内型*************/
			dbRealType = jtype.substring(jtype.indexOf("|")+1);
			jtype = "ORACLEARRAY";
		}else{
			jtype = jtype.toUpperCase();
		}
		Integer t = typeMap.get(jtype);
		if( null != t )
			jdbcType = t;
		pmode = pmode.toUpperCase();
		if( pmode.equals("IN") )
			mode = MODE_IN;
		else if( pmode.equals("OUT") )
			mode = MODE_OUT;
		else if( pmode.equals("INOUT"))
			mode = MODE_INOUT;
		this.index = index;
	}
	
	public String getParamName(){
		return pname;
	}
	
	public int getJdbcType(){
		return jdbcType;
	}
	
	public int getParamIndex(){
		return index;
	}
	
	public void setParamIndex(int i){
		index = i;
	}
	
	public int getParamMode(){
		return mode;
	}
	
	public void setInputValueByBean(CallableStatement stmt,Object bean )throws SQLException{
		try{
			Object pval = PropertyUtils.getProperty(bean, pname);
			setInputValue(stmt,pval);
		}catch(Exception e){
			throw new RuntimeException("setInputValueByBeanError,beanName:"+pname);
		}
	}
	
	public void setInputValue(CallableStatement stmt,Object val )throws SQLException{
		switch( jdbcType ){
		case Types.VARCHAR:
		case Types.CHAR:
			stmt.setString(index, val.toString());
			break;
		case Types.INTEGER:
		case Types.NUMERIC:
		case Types.DECIMAL:
		case Types.SMALLINT:
			if( val instanceof Integer )
				stmt.setInt(index,(Integer)val);
			else if( val instanceof Long )
				stmt.setLong(index, (Long)val);
			else if( val instanceof Short )
				stmt.setShort(index, (Short)val);
			else if( val instanceof Byte )
				stmt.setByte(index, (Byte)val);
			else if( val instanceof String )
				stmt.setInt(index, Long.valueOf((String)val).intValue());
			else
				throw new RuntimeException("传入的参数，与配置不兼容。参数名称[name="+pname+",index="+index+"],value type="+val.getClass().getName());
			break;
		case Types.FLOAT:
		case Types.REAL:
		case Types.DOUBLE:
			if( val instanceof Float )
				stmt.setFloat(index, (Float)val);
			else if( val instanceof Double )
				stmt.setDouble(index, (Double)val);
			else if( val instanceof Integer ){
				int v = (Integer)val;
				stmt.setDouble(index, v);
			}
			else if( val instanceof Long ){
				long v = (Long)val;
				stmt.setDouble(index, v);
			}
			else if( val instanceof String ){
				double d = Double.parseDouble((String)val);
				stmt.setDouble(index, d);
			}
			else
				throw new RuntimeException("传入的参数，与配置不兼容。参数名称[name="+pname+",index="+index+"],value type="+val.getClass().getName());
			break;
		case Types.TINYINT:
			if( val instanceof Byte )
				stmt.setByte(index, (Byte)val);
			else if( val instanceof Integer )
				stmt.setByte(index, ((Integer)val).byteValue());
			else if( val instanceof Short )
				stmt.setByte(index, ((Short)val).byteValue());
			else if( val instanceof String ){
				Integer ib = Integer.parseInt((String)val);
				stmt.setByte(index, ib.byteValue());
			}
			else
				throw new RuntimeException("传入的参数，与配置不兼容。参数名称[name="+pname+",index="+index+"],value type="+val.getClass().getName());
			break;
		case Types.BIGINT:
			if( val instanceof Integer )
				stmt.setInt(index, (Integer)val);
			else if( val instanceof Long )
				stmt.setLong(index, (Long)val);
			else if( val instanceof String ){
				Long l = Long.parseLong((String)val);
				stmt.setLong(index, l);
			}
			else
				throw new RuntimeException("传入的参数，与配置不兼容。参数名称[name="+pname+",index="+index+"],value type="+val.getClass().getName());
			break;
		case Types.DATE:
			if( val instanceof java.util.Date ){
				if( val instanceof java.sql.Date )
					stmt.setDate(index, (java.sql.Date)val);
				else
					stmt.setDate( index, new java.sql.Date(((java.util.Date)val).getTime()) );
			}
			else if( val instanceof Calendar ){
				Calendar cal = (Calendar) val;
				stmt.setDate(index, new java.sql.Date( cal.getTimeInMillis() ) );
			}
			else if( val instanceof Long || val.getClass() == long.class ){
				long lo = ((Long)val);
				stmt.setDate(index, new java.sql.Date( lo ) );
			}
			else if (val instanceof String ){
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				try{
					java.util.Date udate = sdf.parse((String)val);
					java.sql.Date sqlDate = new java.sql.Date( udate.getTime() );
					stmt.setDate(index, sqlDate);
				}catch(ParseException e){
					String err = "字符串["+(String)val+"]转换为Date错误。参数字段:name="+pname+",index="+index;
					throw new RuntimeException(err);
				}
			}
			break;
		case Types.TIME:
			if( val instanceof java.util.Date ){
				if( val instanceof java.sql.Time )
					stmt.setTime(index, (java.sql.Time)val);
				else
					stmt.setTime( index, new java.sql.Time(((java.util.Date)val).getTime()) );
			}
			else if( val instanceof Calendar ){
				Calendar cal = (Calendar) val;
				stmt.setTime(index, new java.sql.Time( cal.getTimeInMillis() ) );
			}
			else if( val instanceof Long || val.getClass() == long.class ){
				long lo = ((Long)val);
				stmt.setTime(index, new java.sql.Time( lo ) );
			}
			else if (val instanceof String ){
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				try{
					java.util.Date udate = sdf.parse((String)val);
					java.sql.Time sqlDate = new java.sql.Time( udate.getTime() );
					stmt.setTime(index, sqlDate);
				}catch(ParseException e){
					String err = "字符串["+(String)val+"]转换为Date错误。参数字段:name="+pname+",index="+index;
					throw new RuntimeException(err);
				}
			}
			break;
		case Types.TIMESTAMP:
			if( val instanceof java.util.Date ){
				if( val instanceof java.sql.Timestamp )
					stmt.setTimestamp(index, (java.sql.Timestamp)val);
				else
					stmt.setTimestamp( index, new java.sql.Timestamp(((java.util.Date)val).getTime()) );
			}
			else if( val instanceof Calendar ){
				Calendar cal = (Calendar) val;
				stmt.setTimestamp(index, new java.sql.Timestamp( cal.getTimeInMillis() ) );
			}
			else if( val instanceof Long || val.getClass() == long.class ){
				long lo = ((Long)val);
				stmt.setTimestamp(index, new java.sql.Timestamp( lo ) );
			}
			else if (val instanceof String ){
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				try{
					java.util.Date udate = sdf.parse((String)val);
					java.sql.Timestamp sqlDate = new java.sql.Timestamp( udate.getTime() );
					stmt.setTimestamp(index, sqlDate);
				}catch(ParseException e){
					String err = "字符串["+(String)val+"]转换为Date错误。参数字段:name="+pname+",index="+index;
					throw new RuntimeException(err);
				}
			}
			else
				throw new RuntimeException("传入的参数，与配置不兼容。参数名称[name="+pname+",index="+index+"],value type="+val.getClass().getName());
			break;
		case Types.BOOLEAN:
			if( val instanceof Boolean )
				stmt.setBoolean(index, (Boolean)val);
			else if( val instanceof Integer ){
				int i = (Integer)val;
				stmt.setBoolean(index, i!=0);
			}
			else if( val instanceof String ){
				String s = ((String)val).toLowerCase();
				stmt.setBoolean(index, s == "true");
			}
			else
				throw new RuntimeException("传入的参数，与配置不兼容。参数名称[name="+pname+",index="+index+"],value type="+val.getClass().getName());
			break;
		case OracleTypes.ARRAY:
			Connection conn = getCon(stmt);
			//stmt = (CallableStatement) getStmt(stmt);
			ArrayDescriptor desc = ArrayDescriptor.createDescriptor(this.dbRealType.toUpperCase(),conn); 
			ARRAY aArray = new ARRAY(desc,  conn, val);
			stmt.setArray(index, aArray); 
			break;
		default:
			throw new RuntimeException("传入的参数，没有对应的jdbcType。参数名称[name="+pname+",index="+index+"],value type="+val.getClass().getName());
		}
	}
	
	public static Connection getCon(CallableStatement stmt){//换服务器需改这个方法
	   Connection con;
	   try{
		   con=stmt.getConnection();
		   if(con instanceof org.apache.commons.dbcp.PoolableConnection){
			   //其实从stmt中获得连接非常简单...stmt.getConnection就行啦...但是得到的Connection并不是Java.sql.Connection...而是PoolableConnection...所以...惨了...必须转换成org.apache.commons.dbcp.PoolableConnection才能使用
			   con=((org.apache.commons.dbcp.PoolableConnection)stmt.getConnection()).getInnermostDelegate();
		   }else if(con instanceof com.mchange.v2.c3p0.impl.NewProxyConnection){
		   //NewProxyConnection转换为OracleConnection  
			   C3P0NativeJdbcExtractor cp30NativeJdbcExtractor = new C3P0NativeJdbcExtractor();  
			   con = (OracleConnection) cp30NativeJdbcExtractor.getNativeConnection(con);
		   }
		   return con;
	   }catch(SQLException e){
		   e.printStackTrace();
		   return null;
	   }
	}
}
