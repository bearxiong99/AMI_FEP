package test;


public class TestBit {
	public static void main(String[] args) {
		
		
		long i = new Long("4294967295");
		System.out.println(i>>24);
		
		System.out.println(Long.toBinaryString(0x7fffffffL));
		
		System.out.println(Long.toBinaryString(6621103815L));
		
		System.out.println(Long.toBinaryString(178652871L));
		
		System.out.println(6621103815L & 0x7fffffffL);
		
		byte[]  b = new byte[]{105, 111, 116, 105, 109, 101, 61, 48, 124, 112, 101, 101, 114, 97, 100, 100, 114, 61, 49, 55, 50, 46, 49, 54, 46, 50, 52, 49, 46, 52, 51, 58, 53, 49, 52, 53, 51, 58, 84, 124, 116, 120, 102, 115, 61, 124, 0, 1, 0, 1, 0, 1, 0, 32, -56, 30, 48, 0, 0, 60, -55, -69, 16, 40, -90, -42, 106, 23, 18, 123, -11, 78, -90, 62, 104, 82, 65, 73, -60, -9, -65, 104, -103, -26, -93, -2};
	
	}
}
