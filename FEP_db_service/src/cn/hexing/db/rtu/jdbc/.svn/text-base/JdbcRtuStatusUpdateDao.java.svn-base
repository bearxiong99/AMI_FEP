package cn.hexing.db.rtu.jdbc;

import java.util.ArrayList;
import java.util.Collection;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import cn.hexing.db.rtu.RtuStatusUpdateDao;
import cn.hexing.fk.model.ComRtu;

public class JdbcRtuStatusUpdateDao implements RtuStatusUpdateDao {
	private static final Logger log = Logger.getLogger(JdbcRtuStatusUpdateDao.class);
	private SimpleJdbcTemplate simpleJdbcTemplate;
	private String sqlInsert,sqlUpdate;
	private int batchSize = 10000;

	public void setDataSource(DataSource dataSource) {
		this.simpleJdbcTemplate = new SimpleJdbcTemplate(dataSource);
	}

	public void update(Collection<ComRtu> rtus) {
		ArrayList<Object> ulist = new ArrayList<Object>(batchSize);
		int count;
		for(ComRtu rtu: rtus ){
			if( ulist.size() < batchSize ){
				ulist.add(rtu);
			}
			else{
				count = batchUpdate(ulist.toArray());
				log.info("终端工况批量更新，成功条数="+count);
				ulist.clear();
			}
		}
		if( ulist.size()>0 ){
			count = batchUpdate(ulist.toArray());
			log.info("终端工况批量更新，成功条数="+count);
		}
	}

	/**
	 * 批量更新。如果失败则批量插入。
	 * @param pojoArray
	 * @return 返回更新条数
	 */
	private int batchUpdate(Object[] pojoArray ){
		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(pojoArray);
		int[] updateCounts = simpleJdbcTemplate.batchUpdate(sqlUpdate,batch);
		ArrayList<Object> ulist = new ArrayList<Object>(pojoArray.length);
		for( int i=0; i< updateCounts.length; i++ )
			ulist.add(pojoArray[i]);
		batch = SqlParameterSourceUtils.createBatch(ulist.toArray());
		updateCounts = simpleJdbcTemplate.batchUpdate(sqlInsert,batch);
		int totalCount = 0;
		for(int i=0; i<updateCounts.length; i++ )
			totalCount += updateCounts[i];
		return totalCount;
	}

	public void setSqlInsert(String sqlInsert) {
		sqlInsert = StringUtils.strip(sqlInsert);
		this.sqlInsert = sqlInsert;
	}

	public void setSqlUpdate(String sqlUpdate) {
		sqlUpdate = StringUtils.strip(sqlUpdate);
		this.sqlUpdate = sqlUpdate;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}
}
