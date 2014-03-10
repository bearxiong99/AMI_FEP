package test;

public class TestClassLoader extends ClassLoader{

	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve)
			throws ClassNotFoundException {
		
		findClass(name);
		return super.loadClass(name, resolve);
	}

	
	public static void main(String[] args) {
	}
}
