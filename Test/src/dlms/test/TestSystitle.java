package dlms.test;

import java.nio.ByteBuffer;

public class TestSystitle {
	public static void main(String[] args) {
		ByteBuffer sysBuffer = ByteBuffer.allocate(8);
		sysBuffer.put((byte) 0x48);
		sysBuffer.put((byte) 0x58);
		sysBuffer.put((byte) 0x45);
		int meterType = Integer.parseInt("01");
//		if(meterType ==1){
//			cxt.meterType = meterType;
//		}else if(meterType==3 || meterType ==4){
//			cxt.meterType = 3;
//		}else{
//			cxt.meterType = 0;
//		}
		sysBuffer.put((byte) meterType);
		System.out.println(Integer.toHexString(88000021).toUpperCase());
	}
}
