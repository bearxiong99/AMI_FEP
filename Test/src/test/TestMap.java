package test;

import java.util.HashMap;
import java.util.Map;

public class TestMap {
	
	private Map<String,String> maps = new HashMap<String, String>();
	
	public static void main(String[] args) {
		TestMap tm = new TestMap();
		Map<String, String> map = tm.getMaps();
		map.put("123", "321");
		System.out.println(tm.getMaps().get("123"));
	}

	public Map<String, String> getMaps() {
		return maps;
	}

	public void setMaps(Map<String, String> maps) {
		this.maps = maps;
	}
}
