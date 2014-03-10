import java.io.IOException;
import java.nio.channels.Selector;
import java.util.concurrent.TimeUnit;


public class Test111 {
	public static void main(String[] args) throws IOException, InterruptedException {
		TimeUnit.SECONDS.sleep(1);
		Selector select = Selector.open();
		select.select();
		
	}
}
