package cn.hexing.db.batch.dao.jdbc;

import javax.sql.DataSource;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class BatchNamedParameterJdbcTemplate extends NamedParameterJdbcTemplate {
	/** The JdbcTemplate we are wrapping */
	private final BatchJdbcTemplate batchJdbcTemplate;

	public BatchNamedParameterJdbcTemplate(DataSource dataSource) {
		super(dataSource);
		this.batchJdbcTemplate = new BatchJdbcTemplate(dataSource);
	}

	/**
	 * Expose the classic Spring JdbcTemplate to allow invocation of
	 * less commonly used methods.
	 */
	public BatchJdbcTemplate getJdbcOperations() {
		return this.batchJdbcTemplate;
	}
}
