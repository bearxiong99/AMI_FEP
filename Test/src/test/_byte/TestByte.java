package test._byte;

import java.nio.ByteBuffer;
import java.util.Date;

import cn.hexing.fk.utils.HexDump;

public class TestByte {
	public static void main(String[] args) {
		short b3 = (short) 0xff;
		System.out.println(0xff==b3);
		short[] b  = new short[5];
		b[0] =(short) 0x07;
		b[1] =(short) 0xD9;
		b[2] =(short) 0x01;
		b[3] =(short) 0x13;
		b[4] =(short) 0x01;
		
		byte t = (byte) 0xff;
		Byte b1 = new Byte((byte) 0x11);
		
		System.out.println(Byte.MAX_VALUE);
		short s = b[0];
		s=(short) (s<<8);
		//高字节需 向左移动8位 
		System.out.println(b[1]);
		byte[] b2  = new byte[2];
		b2[0] =(byte) 0x07;
		b2[1] =(byte) 0xD9;
		ByteBuffer bb = ByteBuffer.wrap(b2);
		System.out.println(bb.getShort());
	}
	
	public static void convert(short[] b)
	{
		
		short t = 0x07d9;
		Date d  = new Date();
		
		
	}
}
