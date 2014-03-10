package test._byte;



public class TestByteBuffer {
	public static void main(String[] args) {
		
		Short s = new Short("2009");
		System.out.println(s>>8);
		
		int i = 2009-(7<<8);
		s= new Short((short) i);
		System.out.println(Short.decode(""+i));
	}
}
