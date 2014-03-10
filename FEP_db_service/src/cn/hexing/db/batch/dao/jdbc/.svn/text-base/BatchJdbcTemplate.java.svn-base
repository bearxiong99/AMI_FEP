package cn.hexing.db.batch.dao.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.InterruptibleBatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterDisposer;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.SqlProvider;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.nativejdbc.NativeJdbcExtractor;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class BatchJdbcTemplate extends JdbcTemplate {
	/** Custom NativeJdbcExtractor */
	private NativeJdbcExtractor nativeJdbcExtractor;

	public void setNativeJdbcExtractor(NativeJdbcExtractor extractor) {
		super.setNativeJdbcExtractor(extractor);
		this.nativeJdbcExtractor = extractor;
	}
	
	public NativeJdbcExtractor getNativeJdbcExtractor() {
		return this.nativeJdbcExtractor;
	}

	public BatchJdbcTemplate() {
		super();
	}

	public BatchJdbcTemplate(DataSource dataSource, boolean lazyInit) {
		super(dataSource, lazyInit);
	}

	public BatchJdbcTemplate(DataSource dataSource) {
		super(dataSource);
	}

	public Object execute(String preSql,PreparedStatementCreator psc,
			PreparedStatementCallback action,String additiveSql) throws DataAccessException {

		Assert.notNull(psc, "PreparedStatementCreator must not be null");
		Assert.notNull(action, "Callback object must not be null");
		if (logger.isDebugEnabled()) {
			String sql = getSql(psc);
			logger.debug("Executing prepared SQL statement"
					+ (sql != null ? " [" + sql + "]" : ""));
		}

		Connection con = DataSourceUtils.getConnection(getDataSource());
		boolean _restore = false;
		boolean _autoCommit = true;
		try{
			_autoCommit = con.getAutoCommit();
		}catch(SQLException e){}
		PreparedStatement ps = null;
		try {
			Connection conToUse = con;
			if (this.nativeJdbcExtractor != null
					&& this.nativeJdbcExtractor
							.isNativeConnectionNecessaryForNativePreparedStatements()) {
				conToUse = this.nativeJdbcExtractor.getNativeConnection(con);
			}
			PreparedStatement pps = null;
			if( StringUtils.hasText(preSql) || StringUtils.hasText(additiveSql))
				conToUse.setAutoCommit(false);
			try{
				if( null != preSql && preSql.length()>5 ){
					pps = conToUse.prepareStatement(preSql);
					pps.execute(preSql);
				}
			}catch(SQLException ex){
				if( null != pps )
					JdbcUtils.closeStatement(pps);
				logger.error("批量保存执行前加语句错误，sql="+preSql,ex);
			}
			//modified by bhw
			ps = psc.createPreparedStatement(conToUse);
			applyStatementSettings(ps);
			PreparedStatement psToUse = ps;
			if (this.nativeJdbcExtractor != null) {
				psToUse = this.nativeJdbcExtractor
						.getNativePreparedStatement(ps);
			}
			Object result = action.doInPreparedStatement(psToUse);
			handleWarnings(ps.getWarnings());
			
			PreparedStatement aps = null;
			try{
				//modified by bhw
				//to-do 增加一条SQL语句的支持。
				if( null != additiveSql && additiveSql.length()>5 ){
					aps = conToUse.prepareStatement(additiveSql);
					aps.execute(additiveSql);
				}
				if( ! conToUse.getAutoCommit() )
					conToUse.commit();
			}catch(SQLException ex){
				if( null != aps )
					JdbcUtils.closeStatement(aps);
				logger.error("批量保存执行附加语句错误，sql="+additiveSql,ex);
			}
			if( ! conToUse.getAutoCommit() )
				conToUse.commit();
			
			return result;
		} catch (SQLException ex) {
			// Release Connection early, to avoid potential connection pool
			// deadlock
			// in the case when the exception translator hasn't been initialized
			// yet.
			if (psc instanceof ParameterDisposer) {
				((ParameterDisposer) psc).cleanupParameters();
			}
			String sql = getSql(psc);
			psc = null;
			JdbcUtils.closeStatement(ps);
			ps = null;
			try{
				con.setAutoCommit(_autoCommit);
			}catch(SQLException exp){ }
			_restore = true;
			DataSourceUtils.releaseConnection(con, getDataSource());
			con = null;
			throw getExceptionTranslator().translate(
					"PreparedStatementCallback", sql, ex);
		} finally {
			if (psc instanceof ParameterDisposer) {
				((ParameterDisposer) psc).cleanupParameters();
			}
			JdbcUtils.closeStatement(ps);
			if( null != con && !_restore ){
				try{
					con.setAutoCommit(_autoCommit);
				}catch(SQLException exp){ }
			}
			DataSourceUtils.releaseConnection(con, getDataSource());
		}
	}


	public Object execute(String preSql,String sql, PreparedStatementCallback action,String additiveSql) throws DataAccessException {
		return execute(preSql,new SimplePreparedStatementCreator(sql), action,additiveSql);
	}

	public int[] batchUpdate(String preSql,String sql, final BatchPreparedStatementSetter pss, String additiveSql)
			throws DataAccessException {
		if (logger.isDebugEnabled()) {
			logger.debug("Executing SQL batch update [" + sql + "]");
		}

		return (int[]) execute(preSql,sql, new PreparedStatementCallback() {
			public Object doInPreparedStatement(PreparedStatement ps) throws SQLException {
				try {
					int batchSize = pss.getBatchSize();
					InterruptibleBatchPreparedStatementSetter ipss =
							(pss instanceof InterruptibleBatchPreparedStatementSetter ?
							(InterruptibleBatchPreparedStatementSetter) pss : null);
					if (JdbcUtils.supportsBatchUpdates(ps.getConnection())) {
						for (int i = 0; i < batchSize; i++) {
							pss.setValues(ps, i);
							if (ipss != null && ipss.isBatchExhausted(i)) {
								break;
							}
							ps.addBatch();
						}
						return ps.executeBatch();
					}
					else {
						List<Integer> rowsAffected = new ArrayList<Integer>();
						for (int i = 0; i < batchSize; i++) {
							pss.setValues(ps, i);
							if (ipss != null && ipss.isBatchExhausted(i)) {
								break;
							}
							rowsAffected.add(new Integer(ps.executeUpdate()));
						}
						int[] rowsAffectedArray = new int[rowsAffected.size()];
						for (int i = 0; i < rowsAffectedArray.length; i++) {
							rowsAffectedArray[i] = ((Integer) rowsAffected.get(i)).intValue();
						}
						return rowsAffectedArray;
					}
				}
				finally {
					if (pss instanceof ParameterDisposer) {
						((ParameterDisposer) pss).cleanupParameters();
					}
				}
			}
		},additiveSql);
	}

	private static String getSql(Object sqlProvider) {
		if (sqlProvider instanceof SqlProvider) {
			return ((SqlProvider) sqlProvider).getSql();
		}
		else {
			return null;
		}
	}

	/**
	 * Simple adapter for PreparedStatementCreator, allowing to use a plain SQL statement.
	 */
	private static class SimplePreparedStatementCreator implements PreparedStatementCreator, SqlProvider {

		private final String sql;

		public SimplePreparedStatementCreator(String sql) {
			Assert.notNull(sql, "SQL must not be null");
			this.sql = sql;
		}

		public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
			return con.prepareStatement(this.sql);
		}

		public String getSql() {
			return this.sql;
		}
	}
}
