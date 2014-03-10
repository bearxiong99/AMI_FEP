package test.thread;

public class SingletonInstance {

	private static SingletonInstance instance = new SingletonInstance();
	
	int i;
	private SingletonInstance(){}
	
	public static SingletonInstance getInstance(){
		return instance;
	}
	
	public void handler(){
		for(int n=0;n<100000;n++){
			System.out.println(++i);			
		}
	}
	
	
	public static void main(String[] args) {
		SingletonInstance.getInstance().handler();
		SingletonInstance.getInstance().handler();
	}
	
	
}
