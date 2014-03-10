package test;

public class TestClass {
	
	
	
	public void test(Persion p){
		Persion p1 = p;
		p1.setId(5);
		p1.setName("sdf");
	}
	
	public static void main(String[] args) {
		System.out.println("sdfsf".equals(null));
		Persion p = new Persion();
		p.setId(1);
		p.setName("abc");
		TestClass tc = new TestClass();
		tc.test(p);
		System.out.println(p.getId());
	}
	
}
