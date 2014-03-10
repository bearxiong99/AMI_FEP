package cn.hexing.reread.utils;

import java.util.Calendar;
import java.util.Date;

public class TimeUtils {
	/**
	 * 获取当年某个月最后一个星期几
	 * @param month：起始值为0（0——11）
	 * @param week：SUNDAY=1...SATURDAY=7
	 * @return java.util.Date
	 */
	public static Calendar getLastDateInMonthByWeek(int month, int week) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MONTH, month);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		for(int day=getDays(calendar.get(Calendar.YEAR),month);;day--){ 
			calendar.set(Calendar.DAY_OF_MONTH, day);
			if (calendar.get(Calendar.DAY_OF_WEEK) == week) {
				break;
			}
		}
		return calendar;
	}

	public static void main(String[] args) {
		System.out.println(isDaySaving(new Date()));
	}
	private static boolean isDaySaving(Date now){
			Calendar startTime = TimeUtils.getLastDateInMonthByWeek(2, 1);
			startTime.set(Calendar.HOUR_OF_DAY, 14);
			Calendar endTime = TimeUtils.getLastDateInMonthByWeek(9, 1);
			endTime.set(Calendar.HOUR_OF_DAY, 5);
			if(now.getTime()>=startTime.getTime().getTime() && now.getTime()<=endTime.getTime().getTime()){
				return true;
			}else{
				return false;
			}
	}
	// 判断闰年
	private static boolean isLeap(int year) {
		if (((year % 100 == 0) && year % 400 == 0)
				|| ((year % 100 != 0) && year % 4 == 0))
			return true;
		else
			return false;
	}

	// 返回当月天数
	private static int getDays(int year, int month) {
		int days;
		int FebDay = 28;
		if (isLeap(year))
			FebDay = 29;
		switch (month) {
		case 0:
		case 2:
		case 4:
		case 6:
		case 7:
		case 9:
		case 11:
			days = 31;
			break;
		case 3:
		case 5:
		case 8:
		case 10:
			days = 30;
			break;
		case 1:
			days = FebDay;
			break;
		default:
			days = 0;
			break;
		}
		return days;
	}
}
