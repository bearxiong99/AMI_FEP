package test;

import java.nio.ByteBuffer;

import cn.hexing.fk.message.gate.MessageGate;
import cn.hexing.fk.utils.HexDump;

import com.hx.dlms.message.DlmsMessage;

public class TestMessageGate {
	public static void main(String[] args) {
			
			DlmsMessage dm = new DlmsMessage();
			dm.setPeerAddr("127.0.0.1");
			dm.setApdu(HexDump.toArray("00010001001000636161A109060760857405080103A203020100A305A10302010EA40A040848584501353EC60988020780890760857405080205AA0A80084630433537353942BE230421281F3000000DF7EC9933D1A0BE2DF245B2768E39EB37E3869E960F795B6861BEAB"));
			System.out.println(HexDump.toHex(dm.getRawPacket()));
			MessageGate mg = new MessageGate();
			mg.setUpInnerMessage(dm);
			ByteBuffer writeBuffer = ByteBuffer.allocateDirect(1000);
			mg.write(writeBuffer );
			writeBuffer.flip();
			String str=HexDump.hexDump(writeBuffer);
			if(str.contains("00 01 00 01 00 01 00 6B 00 01 00 01 00 10 00 63 61 61 A1 09 06 07 60 85 74 05 08 01 03 A2 03 02 01 00 A3 05 A1 03 02 01 0E A4 0A 04 08 48 58 45 01 35 3E C6 09 88 02 07 80 89 07 60 85 74 05 08 02 05 AA 0A 80 08 46 30 43 35 37 35 39 42 BE 23 04 21 28 1F 30 00 00 0D F7 EC 99 33 D1 A0 BE 2D F2 45 B2 76 8E 39 EB 37 E3 86 9E 96 0F 79 5B 68 61 BE AB")){
				System.out.println("yes");
			}else{
				System.out.println("no");
			}
	}
}
