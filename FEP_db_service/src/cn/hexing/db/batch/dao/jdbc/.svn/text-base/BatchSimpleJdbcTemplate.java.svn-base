package cn.hexing.db.batch.dao.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.core.StatementCreatorUtils;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterUtils;
import org.springframework.jdbc.core.namedparam.ParsedSql;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

public class BatchSimpleJdbcTemplate extends SimpleJdbcTemplate {
	/** The NamedParameterJdbcTemplate that we are wrapping */
	private final BatchNamedParameterJdbcTemplate namedParameterJdbcOperations;

	public BatchSimpleJdbcTemplate(DataSource dataSource) {
		super(dataSource);
		this.namedParameterJdbcOperations = new BatchNamedParameterJdbcTemplate(dataSource);
	}

	public int[] batchUpdate(String preSql,String sql, List<Object[]> batchArgs,String additiveSql) {
		return doExecuteBatchUpdate(preSql,sql, batchArgs, new int[0],additiveSql);
	}

	public int[] batchUpdate(String preSql,String sql, SqlParameterSource[] batchArgs,String additiveSql) {
		return doExecuteBatchUpdateWithNamedParameters(preSql,sql, batchArgs,additiveSql);
	}

	/**
	 * Expose the classic Spring JdbcTemplate to allow invocation of
	 * less commonly used methods.
	 */
	public BatchJdbcTemplate getJdbcOperations() {
		return this.namedParameterJdbcOperations.getJdbcOperations();
	}
	
	/**
	 * Expose the Spring NamedParameterJdbcTemplate to allow invocation of
	 * less commonly used methods.
	 */
	public NamedParameterJdbcOperations getNamedParameterJdbcOperations() {
		return this.namedParameterJdbcOperations;
	}

	private int[] doExecuteBatchUpdateWithNamedParameters(String preSql,String sql, final SqlParameterSource[] batchArgs,String additiveSql) {
		if (batchArgs.length <= 0) {
			return new int[] {0};
		}
		final ParsedSql parsedSql = NamedParameterUtils.parseSqlStatement(sql);
		String sqlToUse = NamedParameterUtils.substituteNamedParameters(parsedSql, batchArgs[0]);
		BatchJdbcTemplate jdbcOperation = getJdbcOperations();
		return jdbcOperation.batchUpdate(preSql,
				sqlToUse,
				new BatchPreparedStatementSetter() {

					public void setValues(PreparedStatement ps, int i) throws SQLException {
						Object[] values = NamedParameterUtils.buildValueArray(parsedSql, batchArgs[i], null);
						int[] columnTypes = NamedParameterUtils.buildSqlTypeArray(parsedSql, batchArgs[i]);
						doSetStatementParameters(values, ps, columnTypes);
					}

					public int getBatchSize() {
						return batchArgs.length;
					}
				},additiveSql);
	}

	private void doSetStatementParameters(Object[] values, PreparedStatement ps, int[] columnTypes) throws SQLException {
		int colIndex = 0;
		for (Object value : values) {
			colIndex++;
			if (value instanceof SqlParameterValue) {
				SqlParameterValue paramValue = (SqlParameterValue) value;
				StatementCreatorUtils.setParameterValue(ps, colIndex, paramValue, paramValue.getValue());
			}
			else {
				int colType;
				if (columnTypes == null || columnTypes.length < colIndex) {
					colType = SqlTypeValue.TYPE_UNKNOWN;
				}
				else {
					colType = columnTypes[colIndex - 1];
				}
				StatementCreatorUtils.setParameterValue(ps, colIndex, colType, value);
			}
		}
	}

	private int[] doExecuteBatchUpdate(String preSql,String sql, final List<Object[]> batchValues, final int[] columnTypes, String additiveSql) {
		return getJdbcOperations().batchUpdate(preSql,
				sql,
				new BatchPreparedStatementSetter() {

					public void setValues(PreparedStatement ps, int i) throws SQLException {
						Object[] values = batchValues.get(i);
						doSetStatementParameters(values, ps, columnTypes);
					}

					public int getBatchSize() {
						return batchValues.size();
					}
				},additiveSql);
	}
}
