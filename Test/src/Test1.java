import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Test1 {
	public static void main(String[] args) {
		long t1 = System.currentTimeMillis();
		for(int i=0;i<100000;i++){
			ExecutorService es = Executors.newCachedThreadPool();
			es.execute(new Runnable() {
				
				@Override
				public void run() {
				}
			});
			es.shutdown();
		}
		long t2 = System.currentTimeMillis();
		System.out.println("use time:"+(t2-t1));
		
	}
}
