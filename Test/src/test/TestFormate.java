package test;

import java.text.DecimalFormat;

public class TestFormate {
	public static void main(String[] args) {
		DecimalFormat df = new DecimalFormat(".00");
		System.out.println(df.format(21020.2002));
	}
}
