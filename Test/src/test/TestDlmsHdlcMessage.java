package test;

import java.nio.ByteBuffer;

import cn.hexing.fk.utils.HexDump;

import com.hx.dlms.message.DlmsHDLCMessage;

public class TestDlmsHdlcMessage {
	public static void main(String[] args) {
		System.out.println("0001000100010040C0010A00070001180300FF0201010204020412000809060000010000FF0F02120000090C07DD04010100000000800000090C07DD040101000000008000000100".length());
		DlmsHDLCMessage msg = new DlmsHDLCMessage();
		//18#0.0.44.0.0.255#7
		msg.setApdu(HexDump.toArray("C0018100010000600100FF0200"));
		msg.setControlField((byte) 0x10);
		ByteBuffer B = ByteBuffer.allocate(1000);
		msg.write(B);
		B.flip();
		System.out.println(HexDump.hexDump(B));
	}
}
