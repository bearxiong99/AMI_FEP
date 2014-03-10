import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import cn.hexing.fk.utils.HexDump;


public class TestMD5 {
	public static void main(String[] args) throws NoSuchAlgorithmException, SocketException {
		
		MessageDigest md = MessageDigest.getInstance("MD5");
		String st = HexDump.toHex(md.digest("12".getBytes()));
		System.out.println(st);
	}
}
