package test.request;

public class TestRequest {
	
	
	//数组是传址
	public static void main(String[] args) {
		
		Person p = new Person();
		String[] names = p.names;
		String name1 = p.name;
		name1 = "wang2";
		names[0]= "li4";
		names[1]="zhang3";
		String[] newNames = p.names;
		for(String name:newNames)
		{
			System.out.println(name);
		}
		System.out.println(p.name);
	}
	
}

class Person
{
	int id;
	
	String [] names = new String[2];
	
	String name;
}
