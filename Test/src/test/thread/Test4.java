package test.thread;

import java.util.ArrayList;
import java.util.List;

public class Test4 extends Thread{

	private List<String> s = new ArrayList<String>();
	Object o  = new Object();
	@Override
	public void run() {
		while(true){
			System.out.println("your are to weak");
			if(s.size()==0){
				try {
					synchronized (s) {
						s.wait();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println(s.get(0));
				break;
			}
		}
	}
	
	public void add(String str){
		s.add(str);
		synchronized (s) {
			s.notifyAll();
		}

	}
	public static void main(String[] args) throws InterruptedException {
		Test4 t = new Test4();
		t.start();
		Thread.sleep(1000);
		t.add("去年买了个表");
	}
	
	
}
