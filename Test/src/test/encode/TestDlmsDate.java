package test.encode;

import java.util.Date;

import cn.hexing.util.HexDump;

import com.hx.dlms.DlmsDateTime;

public class TestDlmsDate {
	public static void main(String[] args) {
		DlmsDateTime ddt = new DlmsDateTime(new Date());
		byte[] dateTimeValue = ddt.getDateTimeValue();
		System.out.println(HexDump.toHex(dateTimeValue));
		
		
	}

}
