package gw;

import cn.hexing.fas.protocol.gw.parse.DataItemParser;
import cn.hexing.fas.protocol.gw.parse.DataValue;

public class TestParser {
	public static void main(String[] args) {
		DataValue s = DataItemParser.parser("02", "A4", false);
		String value = s.getValue();
		System.out.println(value);
		value="1231232#321";
		System.out.println(value.substring(value.indexOf("#")+1));
	}
}
