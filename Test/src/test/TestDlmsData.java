package test;

import com.hx.dlms.DlmsData;

public class TestDlmsData {
	public static void main(String[] args) {
		DlmsData dd = new DlmsData();
		dd.setDoubleLongUnsigned(0xFFFFFFFF);
		System.out.println(dd.getDoubleLongUnsigned()>>24 & 0x01 );
		
	}
}
