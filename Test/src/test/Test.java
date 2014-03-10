package test;


public class Test {
	private static boolean stop = false;
	
	public static void main(String[] args) throws InterruptedException {
		Thread t1 = new Thread(new Runnable() {
			
			@Override
			public void run() {
				int i = 0;
				while(!stop){
					try {
						i++;
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
			}
		});
		t1.start();
		Thread.sleep(3000);
		stop = true;
	}
}
