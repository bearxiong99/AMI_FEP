import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TestMatch {
	public static void main(String[] args) {
		String ss="";
		String[] sss = ss.split("#");
		System.out.println(sss.length);
		
		String regex = "[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}|[0-9]{4}-[0-9]{2}-[0-9]{2}";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher("2012-12-01 00:00:00");
		System.out.println(m.matches());
	}
}
