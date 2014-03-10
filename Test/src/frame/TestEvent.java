package frame;

import java.io.IOException;

import cn.hexing.util.HexDump;

import com.hx.dlms.DecodeStream;
import com.hx.dlms.DlmsDateTime;
import com.hx.dlms.applayer.eventnotification.EventNotificationRequest;

public class TestEvent {
	public static void main(String[] args) throws IOException {
		EventNotificationRequest enr = new EventNotificationRequest();
		enr.decode(DecodeStream.wrap(HexDump.toArray("C20107DC09137A0906090000000000010000616200FF020600200000")));
		
		
		System.out.println(HexDump.toHex(enr.getMembers()[0].getValue()));
		DlmsDateTime ddt = new DlmsDateTime();
		ddt.setDlmsDataValue(enr.getMembers()[0].getValue(), 0);
		System.out.println(ddt);
	}
}
