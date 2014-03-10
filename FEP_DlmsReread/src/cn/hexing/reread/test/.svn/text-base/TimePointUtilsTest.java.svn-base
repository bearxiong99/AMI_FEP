package cn.hexing.reread.test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cn.hexing.reread.utils.TimePointUtils;

public class TimePointUtilsTest {
	public static void main(String[] args) {
//		Calendar now = Calendar.getInstance();
//		Calendar begin = Calendar.getInstance();
//		begin.add(Calendar.DATE, -1);
//		Calendar end = Calendar.getInstance();
//		end.add(Calendar.DATE, 1);
//		List<Date> list = createTimePointsByGivenDate(23,IntervalUnit.getByValue("03"),Calendar.getInstance(), begin ,end);
//		for(Date date:list){
//			System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:sss").format( date));
//		}
		List<Date> list = TimePointUtils.createTimePointsByRange(1440, "02", new Date(), -37, 0);
		for(Date date:list){
			System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:sss").format( date));
		}
	}
}
