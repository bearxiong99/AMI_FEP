import java.math.BigDecimal;


public class TestBigDecimal {
	public static void main(String[] args) {
		BigDecimal bd  =new BigDecimal("1.78e-002");
//		   DecimalFormat df = new DecimalFormat("0.0000");
//	        String res = df.format(bd);
		int i=bd.intValue();
		System.out.println(i);
	}
}
