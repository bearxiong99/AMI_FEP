package cn.hexing.fk.bp.dlms.util;

import java.util.HashMap;
import java.util.Map;

import cn.hexing.fk.bp.dlms.persisit.JdbcDlmsDao;



public class DlmsConstant {
	/**是否是伊朗历*/
	public  boolean isIranTime;
	
	/**读事件，电表的时间跟主站的时间可能有些偏差，所以这里读当前时间前后x分钟的一个区间*/
	public  int alarmTimeOffset;
	
	public int alarmTimeReadInterval;
	
	public Map<String, String> meterModeMapVersion = new HashMap<String, String>();
	
	private JdbcDlmsDao dao ;
	
	private static DlmsConstant instance = null;	
	
	private DlmsConstant(){}
	
	public void init(){
	}
	
	public static final DlmsConstant getInstance(){
		if( null == instance )
			instance = new DlmsConstant();
		return instance;
	}
	
	public void setAlarmTimeOffset(String timeOffset){
		alarmTimeOffset = Integer.parseInt(timeOffset)*1000*60;
	}

	public  void setIranTime(String isIranTime) {
		this.isIranTime =Boolean.parseBoolean(isIranTime);
	}

	public JdbcDlmsDao getDao() {
		return dao;
	}

	public void setDao(JdbcDlmsDao dao) {
		this.dao = dao;
	}

	public int getAlarmTimeReadInterval() {
		return alarmTimeReadInterval;
	}

	public void setAlarmTimeReadInterval(int alarmTimeReadInterval) {
		this.alarmTimeReadInterval = alarmTimeReadInterval;
	}

}
