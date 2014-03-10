package date;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SetDate {
	public static void main(String[] args) {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.MINUTE, -5);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		System.out.println(sdf.format(c.getTime()));
	}
}
