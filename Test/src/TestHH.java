import java.text.SimpleDateFormat;
import java.util.Date;


public class TestHH {
	public static void main(String[] args) {
		Date d = new Date(1381385354429L);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		System.out.println(sdf.format(d));
	     long now = System.currentTimeMillis();
	        long m1 = now-1000*60*60*24*30*3; //ÕâÊÇ´íµÄ
	        long m2 = now-1000L*60*60*24*30*3;
	         
	        System.out.println("now:"+now);
	        System.out.println("m1:"+m1); 
	        System.out.println("m2:"+m2);
	}
}
