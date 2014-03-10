package dlms.test;

import java.nio.ByteBuffer;

public class TestCreateHeartBeat {
	public static void main(String[] args) {
		createHeartBeat("100000000000");
	}
	
	
	public static ByteBuffer createHeartBeat(String logicAddress){
		
		ByteBuffer heart=ByteBuffer.allocate(26);
		//--------帧头------------
		heart.putShort((short) 0x0001);
		heart.putShort((short) 0x0001);
		heart.putShort((short) 0x0010);
		//--------帧头------------
		heart.putShort((short)0x0018); //长度
		heart.put((byte) 0xDD);
		heart.put((byte)0x10);
		heart.put((byte)0x00);
		heart.put((byte)0x00);
		heart.put((byte)0x00);
		heart.put((byte)0x00);
		logicAddress="100000000000";
		byte[] b_logic=logicAddress.getBytes();
		heart.put(b_logic);
		heart.flip();
		return heart;
		
		
	}
}
