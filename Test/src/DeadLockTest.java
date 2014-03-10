import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public class DeadLockTest {

	private Object lock = new Object();

	public static void main(String[] args) throws InterruptedException {
		DeadLockTest dlt = new DeadLockTest();
	}
	public List<Object> webReqList = Collections.synchronizedList(new LinkedList<Object>()); //DlmsEvent Object
	
	public DeadLockTest() throws InterruptedException{
		ThreadTest t1 = new ThreadTest();
		ThreadTest t2 = new ThreadTest();
		t1.start();
		t2.start();
		t1.join();
		t2.join();
	}
	
	private class ThreadTest extends Thread{
		@Override
		public void run(){
			
			webReqList.add("sdfsd");
			synchronized(lock){
				System.out.println("sdf");
				webReqList.remove(0);
			}
		}
	}
	
	
	
	
}
