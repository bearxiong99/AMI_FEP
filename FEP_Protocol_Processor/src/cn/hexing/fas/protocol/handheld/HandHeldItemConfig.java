package cn.hexing.fas.protocol.handheld;

import java.util.HashMap;
import java.util.Map;

public class HandHeldItemConfig {

	public static Map<String,String> itemMap = new HashMap<String, String>();
	
	public static Map<String,String> itemUpMap = new HashMap<String, String>();
	
	
	static{
		itemMap.put("2", "ASC8#ASC8#ASC9#ASC9#ASC9#ASC9#ASC9#ASC9#ASC9#ASC9#ASC9#ASC9#ASC7#ASC7#ASC7#ASC7#ASC7#ASC9#ASC9#ASC9#ASC9#ASC9#ASC4#ASC8#ASC8#ASC4#ASC7#ASC4");
		
		itemMap.put("3", "ASC8#ASC8#ASC7#ASC7#ASC7#ASC7#ASC7#ASC7#ASC7#ASC7#ASC7#ASC7#ASC7#ASC7#ASC7#ASC7#ASC7#ASC4#ASC4#ASC4#ASC5#ASC4#ASC7#ASC6#ASC4#ASC4#ASC4#ASC4#ASC4#ASC7#ASC7#ASC7#ASC7#ASC7#ASC4#ASC4#ASC4#ASC4#ASC4");
		
		itemMap.put("4", "ASC1#ASC10#ASC8#ASC15#ASC10#ASC8#ASC15#ASC10#ASC8#ASC15#ASC10#ASC8#ASC15#ASC10#ASC8#ASC15#ASC10#ASC8#ASC15#ASC10#ASC8#ASC15");
		//itemMap.put("4", "N2#ASC10");
		
		itemUpMap.put("4", "ASC16");
		
		itemUpMap.put("0", "ASC16"); //确认,参数为掌机ID
		//itemUpMap.put("1", "ASC16"); //否认,参数为掌机ID
		//[潘超]和[李志英]确认之后，将否认抹除，换成与2个格式是一样 的。
		itemUpMap.put("1", "ASC8#ASC8#ASC9#ASC9#ASC9#ASC9#ASC9#ASC9#ASC9#ASC9#ASC9#ASC9#ASC7#ASC7#ASC7#ASC7#ASC7#ASC9#ASC9#ASC9#ASC9#ASC9#ASC4#ASC8#ASC8#ASC4#ASC7#ASC4");
		itemUpMap.put("2", "ASC8#ASC8#ASC9#ASC9#ASC9#ASC9#ASC9#ASC9#ASC9#ASC9#ASC9#ASC9#ASC7#ASC7#ASC7#ASC7#ASC7#ASC9#ASC9#ASC9#ASC9#ASC9#ASC4#ASC8#ASC8#ASC4#ASC7#ASC4");
		itemUpMap.put("3", "ASC8#ASC8#ASC7#ASC7#ASC7#ASC7#ASC7#ASC7#ASC7#ASC7#ASC7#ASC7#ASC7#ASC7#ASC7#ASC7#ASC7#ASC4#ASC4#ASC4#ASC5#ASC4#ASC7#ASC6#ASC4#ASC4#ASC4#ASC4#ASC4#ASC7#ASC7#ASC7#ASC7#ASC7#ASC4#ASC4#ASC4#ASC4#ASC4");

		

	}
	
	
}
