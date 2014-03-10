package test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TestCalendar {
	public static void main(String[] args) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		Date d_startTime = c.getTime();
		c.add(Calendar.DATE, 1);
		Date d_endTime = c.getTime();
		System.out.println(sdf.format(d_startTime));
		System.out.println(sdf.format(d_endTime));
		
	}
}
