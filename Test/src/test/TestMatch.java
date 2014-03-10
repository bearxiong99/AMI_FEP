package test;

public class TestMatch {
	public static void main(String[] args) {
		String s = "2012-01-05";
		System.out.println(s.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
	}
}
