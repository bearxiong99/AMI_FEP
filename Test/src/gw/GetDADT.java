package gw;

import cn.hexing.fas.protocol.gw.parse.DataItemCoder;
import cn.hexing.fas.protocol.gw.parse.DataItemParser;

public class GetDADT {
	public static void main(String[] args) {
		int[] tn=DataItemParser.measuredPointParser("401D");
		String[] codes= DataItemParser.dataCodeParser("401D", "06");
		System.out.println(tn[0]);
		System.out.println(codes[0]);//1E010115
		
		codes [0] ="0DF161";
		tn[0]=9;
		System.out.println(DataItemCoder.getCodeFrom1To1(tn[0], codes[0]));
	}
}
