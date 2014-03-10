package reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * 
 * @author gaoll
 *
 * @time 2012-11-6 ÏÂÎç2:14:21
 *
 * @info ·´Éä£¬Ref   reflectasm-1.05-all.jar
 */
public class TestReflect {
	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SecurityException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
		
		@SuppressWarnings("unchecked")
		Class<Person> c = (Class<Person>) Class.forName("reflect.Person");
		Constructor<Person> s = c.getConstructor(String.class);
		Person p = s.newInstance("123");
		p.speak();
//		Person person = new Person();
//		MethodAccess access=MethodAccess.get(Person.class);
//		access.invoke(person, "speak");
	}
}
class Person{
	public Person(String name){
		System.out.println("construct");
	}
	public void speak(){
		System.out.println("hello");
	}
	
}