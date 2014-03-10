package test.string;

public class TestStringBuilder {
	public static void main(String[] args) {
		StringBuilder sb=new StringBuilder();
		sb.append("123123#");
		sb.replace(sb.length()-1, sb.length(), ";");
		System.out.println(sb);
	}
}
