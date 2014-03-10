package test;

import java.nio.ByteBuffer;

import com.hx.dlms.message.DlmsMessage;

import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.MultiProtoRecognizer;
import cn.hexing.fk.utils.HexDump;


public class TestMessageRecognizer {
	public static void main(String[] args) {
		byte[]  b = new byte[]{105, 111, 116, 105, 109, 101, 61, 48, 124, 112, 101, 101, 114, 97, 100, 100, 114, 61, 49, 55, 50, 46, 49, 54, 46, 50, 52, 49, 46, 52, 51, 58, 53, 49, 52, 53, 51, 58, 84, 124, 116, 120, 102, 115, 61, 124, 0, 1, 0, 1, 0, 1, 0, 32, -56, 30, 48, 0, 0, 60, -55, -69, 16, 40, -90, -42, 106, 23, 18, 123, -11, 78, -90, 62, 104, 82, 65, 73, -60, -9, -65, 104, -103, -26, -93, -2};
		ByteBuffer bb = ByteBuffer.wrap(b);
		MultiProtoRecognizer.recognize(bb);
		
		IMessage s = MultiProtoRecognizer.recognize(HexDump.toByteBuffer("000100010001003DC3010C001200002C0000FF010102020927FD0001464D3334434A444C535444453030313032563031F61DF8BDF258261C8855E9DB9BBD74A506000246C0"));
		System.out.println(s instanceof DlmsMessage);
	}
}
