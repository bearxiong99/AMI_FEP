import java.util.LinkedHashMap;
import java.util.Map;


public class TestMap {
	public static void main(String[] args) {
		Map<String, Integer> map = new LinkedHashMap<String, Integer>();
		map.put("1#1.0.0.4.2.255#2", 321);
		map.put("1#1.0.0.4.5.255#2", 321);
		map.put("1#1.0.0.4.3.255#2", 321);
		map.put("1#1.0.0.4.6.255#2", 321);

		for(String name:map.keySet()){
			System.out.println(name);
		}
		
		
	}
}
