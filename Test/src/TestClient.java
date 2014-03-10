import java.util.Collections;
import java.util.LinkedList;
import java.util.List;



public class TestClient {
	public static void main(String[] args) {
		
		List<Object> list =  Collections.synchronizedList(new LinkedList<Object>());
		
		list.add("sdfs");
		
	}
}
