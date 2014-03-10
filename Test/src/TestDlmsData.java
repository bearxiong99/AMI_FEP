import java.nio.ByteBuffer;


public class TestDlmsData {
	public static void main(String[] args) {
		
		ByteBuffer buf = ByteBuffer.wrap(new byte[]{(byte) 0xFF,(byte) 0xFF,(byte) 0xFF,(byte) 0xFF});
	
		System.out.println( Long.parseLong(Integer.toHexString( buf.getInt() ),16));
	}
}
