package test.thread;

import java.util.HashMap;
import java.util.Map;



public class Test11 {
	
	Map<String,Integer> map = new HashMap<String, Integer>();
	{
		map.put("abc",0);
	}
	
	public Test11(){
		new Thread1().start();
		new Thread1().start();
	}
	public static void main(String[] args) {
		new Test11();
	}
	
	class Thread1 extends Thread{
		
		public void run(){
			for(int i=0;i<1000;i++){
				synchronized (map) {
					map.put("abc", map.get("abc")+1);				
				}				
			}
			System.out.println(map.get("abc"));
		}
	}
}
