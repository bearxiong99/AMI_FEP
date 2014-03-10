package test._bytebuffer;

import java.nio.ByteBuffer;

import cn.hexing.fk.utils.HexDump;

public class TestCompact {
	public static void main(String[] args) {
		ByteBuffer b = ByteBuffer.allocate(1000);
		b.put(new byte[]{0x68});
		b.flip();
		b.compact();
		System.out.println(HexDump.hexDump(b));
		b.put(new byte[]{0x61});
		b.flip();
		b.compact();
		System.out.println(HexDump.hexDump(b));
		b.put(new byte[]{0x61,0x61});
		b.flip();
		System.out.println(HexDump.hexDump(b));
//		b.clear();
//		b.compact();
//		read1(b);
//		System.out.println(HexDump.hexDump(b));

	}
	//68 32 00 32 00 68 C9 33 33 44 44 00 02 7C 00 00 04 00 39 16 
	public static void read(ByteBuffer b){
		
		b.put(HexDump.toArray("683200320068C93333"));
		b.flip();
	}
	
	public static void read1(ByteBuffer b){
		
		b.put(HexDump.toArray("444400027C000004003916"));
		b.flip();
	}
}
