package calendar;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TestDate {
	public static void main(String[] args) throws ParseException, InterruptedException {
		
		
		
		
		
		Calendar c2 =Calendar.getInstance();
		System.out.println(c2.getTimeInMillis());
		System.out.println(c2.getTimeInMillis()/1000);

		System.out.println(1369310686*1000);
		c2.setTimeInMillis(-18268009139000L);
		Date d3 = c2.getTime();
		
		SimpleDateFormat sdf = new SimpleDateFormat("ssmmhhddMMyy");
		System.out.println(sdf.format(new Date()));
//		System.out.println(sdf.format(d3));
		Date ddd = sdf.parse("1391-02-02 01:01:01");
		System.out.println(ddd.getTime());
//		Date d1 = new Date();
//		d1.setTime(1223934469);
//		
//		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		System.out.println(df.format(d1));
//		Date date =df.parse("2008-10-13 21:47:49");
//		System.out.println(date.getTime());
//		System.out.println(df.format(date));
//		date = new Date();
//		Thread.sleep(2000);
//		Date d  = new Date();
//		System.out.println((d.getTime()-date.getTime())/1000);
//		Calendar c = Calendar.getInstance();
//		c.setTime(date);
//		Calendar c1 = Calendar.getInstance();
//		c1.setTime(new Date());
//		System.out.println(c.before(c1));
//		

	}
}
