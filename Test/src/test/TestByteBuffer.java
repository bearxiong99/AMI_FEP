package test;

import java.nio.ByteBuffer;

import cn.hexing.fk.utils.HexDump;

public class TestByteBuffer {
	public static void main(String[] args) {
		Thread1 t = new Thread1();
		Thread1 t2 = new Thread1();
		TestMessage1 message = new TestMessage1();
		t.message = message;
		t2.message = message;
		t.start();
		t2.start();
	}
}
class Thread1 extends Thread{
	public TestMessage1 message;
	@Override
	public void run(){
		for(int i = 0 ; i < 10000 ; i++){
			ByteBuffer buffer=ByteBuffer.allocateDirect(1000);
			message.write(buffer);
			buffer.flip();
			String str=HexDump.hexDump(buffer);
			System.out.println(str);
			if(str.equals("00 01 00 01 00 01 00 1C CC 1A 30 00 00 0C 68 D3 D3 98 EA 8F 7D 95 DA 46 6F F9 5E B1 DD FC 59 12 51 5B 99 12")){
				System.out.println("sdf");
			}else{
				System.out.println("nonon");
			}
			
		}
	}
}

class TestMessage1 {
	
	ByteBuffer apdu = HexDump.toByteBuffer("CC1A3000000C68D3D398EA8F7D95DA466FF95EB1DDFC5912515B9912");
	private short version = 0x0001;
	private short srcAddr = 0x0001;
	private short dstAddr = 0x0001;
	public void write(ByteBuffer buffer){
		synchronized (this) 
		{
			buffer.putShort(version);
			buffer.putShort(srcAddr);
			buffer.putShort(dstAddr);
			buffer.putShort((short) apdu.remaining());
			while(apdu.hasRemaining() && buffer.hasRemaining())
				buffer.put(apdu.get());
			apdu.position(0);
		}
	
	}
}