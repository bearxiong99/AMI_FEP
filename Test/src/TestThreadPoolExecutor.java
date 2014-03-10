

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TestThreadPoolExecutor {
	public static void main(String[] args) {
		LinkedBlockingQueue<Runnable> lbq = new LinkedBlockingQueue<Runnable>();
		ThreadPoolExecutor tpe = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, lbq);
		tpe.execute(new ThreadTask1());
		tpe.execute(new ThreadTask2());
		System.out.println("¶ÑÕ»ÖÐµÄ¸ö"+tpe.getPoolSize());
	}
	
	static class ThreadTask1 implements Runnable{
		@Override
		public void run() {
			try {
				System.out.println("abc");
				TimeUnit.SECONDS.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	static class ThreadTask2 implements Runnable{
		@Override
		public void run() {
			try {
				System.out.println("bcd");
				TimeUnit.SECONDS.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}	
