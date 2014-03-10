package cn.hexing.reread.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
/**
 * 根据采样间隔时间、间隔时间单位生成给定时间段内的采样时间点
 * @ClassName:TimePointUtils
 * @Description:TODO
 * @author kexl
 * @date 2012-9-24 上午10:24:49
 *
 */
public class TimePointUtils {
	private static final Logger log = Logger.getLogger(TimePointUtils.class);
	
	public static int transIntervalToMinute(int interval , String intervalUnit){
		IntervalUnit sampleIntervalUnit = IntervalUnit.getByValue(intervalUnit);
		if (sampleIntervalUnit.equals(IntervalUnit.MUNITE)){//采样间隔时间单位：分钟
			return interval;
		}
		else if(sampleIntervalUnit.equals(IntervalUnit.HOUR)){//采样间隔时间单位：时
			return interval*60;
		}
		else if(sampleIntervalUnit.equals(IntervalUnit.DAY)){//采样间隔时间单位：日
			return 1440;
		}
		else if(sampleIntervalUnit.equals(IntervalUnit.MONTH)){//采样间隔时间单位：月
			return 1440;
		}else{
			return interval;
		}
	}
	/**
	 * 获取起止时间内的时间点
	 * @param sampleInterval
	 * @param sampleIntervalUnit
	 * @param beginTime
	 * @param endTime
	 * @return
	 */
	public static List<Date> createTimePointsByRange(int sampleInterval , String sampleIntervalUnit , Date beginTime , Date endTime){
		//对时间段内的每天生成时间点
		List<Date> res = new ArrayList<Date>();
		IntervalUnit intervalUnit = IntervalUnit.getByValue(sampleIntervalUnit);
		
		Calendar beginCal = Calendar.getInstance();
		beginCal.setTime(beginTime);
		Calendar endCal = Calendar.getInstance();
		endCal.setTime(endTime);

		Calendar temp = (Calendar) beginCal.clone();
		//获取当天的0点时间
        temp.set(Calendar.HOUR_OF_DAY, 0);
        temp.set(Calendar.MINUTE, 0);
        temp.set(Calendar.SECOND, 0);
        temp.set(Calendar.MILLISECOND, 0);
		while(temp.compareTo(endCal)<=0){
			res.addAll(createTimePointsByGivenDate(sampleInterval, intervalUnit, (Calendar)temp.clone(), beginCal, endCal));
			temp.add(Calendar.DATE, 1);
		}
		if(log.isDebugEnabled()){
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:sss");
			log.debug("createTimePointsByRange finished! sampleInterval:"+sampleInterval+",sampleIntervalUnit:"+sampleIntervalUnit
					+",beginTime:" + format.format(beginCal.getTime())+",endTime:" + format.format(endCal.getTime()) +",size:"+res.size());
		}
		return res;
	}
	
	/**
	 * 生成一个时间段内的时间点
	 * 如：
	 * 		当前时间前2个小时——当前时间前1个小时，每隔30分钟的时间点
	 * 		createRangeTimePoints(30 , "02" , new Date() , -2 , -1);
	 * @param sampleInterval
	 * @param sampleIntervalUnit
	 * @param date
	 * @param begin
	 * @param end
	 * @return
	 */
	public static List<Date> createTimePointsByRange(int sampleInterval , String sampleIntervalUnit , Date date ,int begin , int end){
		//对时间段内的每天生成时间点
		Calendar baseCal = Calendar.getInstance();
		baseCal.setTime(date);
		Calendar beginCal = (Calendar) baseCal.clone();
		Calendar endCal = (Calendar) baseCal.clone();
//		switch (intervalUnit){
//            case MUNITE:{
//            	beginCal.add(Calendar.MINUTE, begin);
//            	endCal.add(Calendar.MINUTE, end);
//            }
//              	break;
//            case HOUR:{
            	beginCal.add(Calendar.HOUR_OF_DAY, begin);
            	endCal.add(Calendar.HOUR_OF_DAY, end);
//            }
//              	break;
//            case DAY:{
//            	beginCal.add(Calendar.DATE, begin);
//            	endCal.add(Calendar.DATE, end);
//            }
//              	break;
//            case MONTH:{
//            	beginCal.add(Calendar.MONTH, begin);
//            	endCal.add(Calendar.MONTH, end);
//            }
//              	break;
//		}
		return createTimePointsByRange(sampleInterval, sampleIntervalUnit, beginCal.getTime(), endCal.getTime());
	}
	
	/**
	 * 生成给定的某一天的时间点
	 * @param sampleInterval
	 * @param sampleIntervalUnit
	 * @param cal
	 * @return
	 */
	public static List<Date> createTimePointsByGivenDate(int sampleInterval , IntervalUnit sampleIntervalUnit , Calendar cal){
		//begin、end的最大值
		Calendar begin = (Calendar)cal.clone();
		Calendar end = (Calendar)cal.clone();
    	end.set(Calendar.HOUR_OF_DAY, 23);
    	end.set(Calendar.MINUTE, 59);
    	end.set(Calendar.SECOND, 59);
    	end.set(Calendar.MILLISECOND, 999);
    	return createTimePointsByGivenDate(sampleInterval , sampleIntervalUnit , cal , begin , end);
	}
	/**
	 * 生成给定的某一天的时间点
	 * @param sampleInterval
	 * @param sampleIntervalUnit
	 * @param cal
	 * @return
	 */
	public static List<Date> createTimePointsByGivenDate(int sampleInterval , IntervalUnit sampleIntervalUnit , Calendar cal ,Calendar begin , Calendar end){
		List<Date> res = new ArrayList<Date>();
		if (sampleIntervalUnit!=null){
			//获取当天的0点时间
	        cal.set(Calendar.HOUR_OF_DAY, 0);
	        cal.set(Calendar.MINUTE, 0);
	        cal.set(Calendar.SECOND, 0);
	        cal.set(Calendar.MILLISECOND, 0);
	        
			int n=0;
			if (sampleIntervalUnit.equals(IntervalUnit.MUNITE)){//采样间隔时间单位：分钟
				n=24*60/sampleInterval;
			}
			else if(sampleIntervalUnit.equals(IntervalUnit.HOUR)){//采样间隔时间单位：时
				n=24/sampleInterval;
			}
			else if(sampleIntervalUnit.equals(IntervalUnit.DAY)){//采样间隔时间单位：日
				n=1;
			}
			else if(sampleIntervalUnit.equals(IntervalUnit.MONTH)){//采样间隔时间单位：月
				n=1;
			}
			for(int i=0;i<n;i++){
				if(i>=1){
					switch (sampleIntervalUnit){
			            case MUNITE: cal.add(Calendar.MINUTE,sampleInterval);//累加分钟               
			              	break;
			            case HOUR: cal.add(Calendar.HOUR_OF_DAY,sampleInterval);//累加小时 
			              	break;
			            case DAY: cal.add(Calendar.DATE,sampleInterval);//累加日    
			              	break;
			            case MONTH: cal.add(Calendar.MONTH,sampleInterval);//累加月  
			              	break;
					}
				}
				if(cal.compareTo(begin) >= 0){ //大于最小时间
					if(cal.compareTo(end) <= 0){//小于最小时间
						res.add(cal.getTime());
					}else{ //已超过最大时间，跳出循环
						break;
					}
				}
			}
		}
		return res;
	}
}
