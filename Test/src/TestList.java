import java.util.ArrayList;
import java.util.List;


public class TestList {
	public static void main(String[] args) {
		List<String> test = new ArrayList<String>();
		
		String t = "t1";
		String t2 = "t2";
		test.add(t);
		test.add(t2);
		test.add(t2);
		System.out.println(test.indexOf(t2));
	}
}
