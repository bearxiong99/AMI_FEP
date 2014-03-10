/**
 * 支持批处理的DAO
 */
package cn.hexing.db.batch.dao;


/**
 *
 */
public interface IBatchDao{
	/**
	 * 可配置的SQL，如：
	 * update t_actor set first_name = :firstName, last_name = :lastName where id = :id
	 * update t_actor set first_name = ?, last_name = ? where id = ?
	 * @param sql
	 */
	void setSql(String sql);
	
	/**
	 * 对于批量更新dao来说，update失败，需要执行备用sql，如insert。
	 * @param sqlAlt
	 */
	void setSqlAlt(String sqlAlt);
	
	/**
	 * 对于批量保存，在一个批量更新后，可能需要执行一句附加的SQL，如merge语句。
	 * @param aSql
	 */
	void setAdditiveSql(String aSql );
	
	void setAdditiveParameter(Object additiveParameter);
	
	/**
	 * 当 POJO ＝＝ null，表示检测批量保存的时间是否到达delay的时间。
	 * 当等待批处理的队列满了，不能继续往Dao增加对象，return false.
	 * @param pojo
	 */
	boolean add(Object pojo);
	boolean add(Object[] param);
	/**
	 * 批量到对应的KEY。
	 * 任务和告警都需要按照单位代码进行分表保存。因此在放到Dao时，需要依据key来选择合适的DAO。
	 * @return
	 */
	int getKey();
	
	/**
	 * 批量保存的结果情况，在实现类中处理。本方法将在线程池中自动执行，不需要返回值。
	 */
	void batchUpdate();
	
	/**
	 * 返回当前DAO对象等待批量保存的对象数量
	 * @return
	 */
	int size();
	
	/**
	 * 设置批量更新的最大值。
	 * @param batchSize
	 */
	void setBatchSize(int batchSize);
	
	/**
	 * 返回最近一次批量保存的执行时间。
	 * @return
	 */
	long getLastIoTime();
	
	/**
	 * 当增加新对象时，可能不够批次数量。那么无论批次多大，
	 * 到了最大延迟时间，不足批量也要保存。
	 * @param delaySec
	 */
	void setDelaySecond(int delaySec);
	
	long getDelayMilliSeconds();
	
	/**
	 * 检测delay间隔内，是否有数据需要保存。
	 */
	boolean hasDelayData();
}
