package cn.hexing.db.heartbeat;

import java.sql.SQLException;
import java.util.List;

public interface HeartBeatDao {
//	初始化
	public boolean doInit(int weekNo, int weekNum) throws SQLException;
//	获取日志信息
	public  List<HeartBeatLog> getLogResult(int weekNO) throws SQLException;
//	更新日志
	public void updateLogResult(boolean b,long curTime,int id,int nInitWeekNo) throws SQLException;
	
	public void insertLogResult(int nInitWeekNo, long curTime) throws SQLException;
	
	public int[] batchUpdate(List<HeartBeat> orHeartBeats,int columnIndex);
	public void batchInsert(List<HeartBeat> insertHeartBeats,int columnIndex);

}
