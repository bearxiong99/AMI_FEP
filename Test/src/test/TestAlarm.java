package test;

import cn.hexing.fas.model.RtuAlert;
import cn.hexing.util.HexDump;

public class TestAlarm {
	public static void main(String[] args) {
		
		System.out.println(HexDump.toHex(1));
		
		RtuAlert ad = new RtuAlert();
		ad.setAlertCode(Integer.parseInt("03022F",16));
		System.out.println(ad.getAlertCodeHex());
	}
}
