package test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public class TestList {
	public static void main(String[] args) {
		List<String> list = Collections.synchronizedList(new LinkedList<String>());
//		List<String> list = new LinkedList<String>();
		for(int i=0;i<1000;i++){
			list.add(""+i);
		}
		Thread1 t1 = new Thread1();
		Thread2 t2 = new Thread2();
		t1.list = list;
		t2.list = list;
		
		Thread1 t3 = new Thread1();
		Thread2 t4 = new Thread2();
		t3.list = list;
		t4.list = list;
		t1.start();t2.start();t3.start();t4.start();
	}
	
	private static class Thread1 extends Thread{
		
		public List<String> list ;
		int i=0;
		@Override
		public void run(){
			while(i<1000){
				list.add(""+i);
				i++;
			}
			System.out.println("Thread1----"+list.size());
		}
	}

	private static class Thread2 extends Thread{
		
		public List<String> list ;
		int i=0;
		@Override
		public void run(){
			while(i<1000){
				list.remove(0);
				i++;
			}
			System.out.println("Thread2----"+list.size());
		}
		
	}
}	

