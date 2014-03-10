package test;

import cn.hexing.fk.message.MultiProtoRecognizer;
import cn.hexing.fk.utils.HexDump;

public class TestRecognizer {
	public static void main(String[] args) {
		MultiProtoRecognizer.recognize(HexDump.toByteBuffer("696F74696D653D313335333437343133383733357C70656572616464723D3137322E31362E3235312E3233393A31373433303A547C747866733D30327C0001000100100012DD1000000000303030303838303030303039"));
	}
}
