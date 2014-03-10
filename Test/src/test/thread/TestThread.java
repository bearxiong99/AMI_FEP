package test.thread;

public class TestThread extends Thread{
	@Override

    public void run() {

        System.out.println("start");

        while (true){
        	System.out.println("xixi");
        	try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
            if (Thread.currentThread().isInterrupted())

                break;
        }

        System.out.println("while exit");

    }
 

    public static void main(String[] args) {

        Thread thread = new TestThread();

        thread.start();

        try {

            sleep(2000);

        } catch (InterruptedException e) {

        }

        thread.interrupt();

    }
}
