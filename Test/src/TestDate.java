import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class TestDate {
	public static void main(String[] args) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
		try {
			Date s = sdf.parse("201401071624");
			System.out.println(s);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		System.out.println(new Date(1387436431165L));
	}
}
