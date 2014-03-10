import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class Test11 {
	public static void main(String[] args) throws ParseException {
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DATE, 19);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		System.out.println(sdf.format(c.getTime()));
		
		
		String date="2014-02-15";
		Date d = sdf.parse(date);
		c.setTime(d);
		c.add(Calendar.DATE, -19);
		System.out.println(sdf.format(c.getTime()));
		
	}
}
