import java.util.Formatter;
import java.util.Locale;


public class Test1111 {
	public static void main(String[] args) {
		Locale.setDefault(Locale.CHINESE);
		System.out.println(System.getProperty("file.encoding"));
        String ft="%."+4+"f";
        System.out.println(Locale.getDefault());
        Formatter f = new Formatter();
        System.out.println(f.format(Locale.CANADA,ft, Double.parseDouble("000034.5800")));
        System.out.println(String.format(ft, Double.parseDouble("000034.5800")));
	}
}
