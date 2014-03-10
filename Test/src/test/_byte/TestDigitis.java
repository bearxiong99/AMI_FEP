package test._byte;

public class TestDigitis {
	public static void main(String[] args) {
	      final byte[] digits =
	          { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	      int i;
	      byte[] high = new byte[256];
	      byte[] low = new byte[256];

	      for (i = 0; i < 256; i++) {
	    	  System.out.println(i+":"+(i>>>4)+","+(i & 0x0F));
	          high[i] = digits[i >>> 4];
	          low[i] = digits[i & 0x0F];
	      }
	}
}
