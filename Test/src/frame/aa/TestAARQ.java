package frame.aa;

import com.hx.dlms.aa.AarqApdu;

public class TestAARQ {
	public static void main(String[] args) {
		AarqApdu aa = new AarqApdu();
		aa.createRandomAuthenticationValue();
		System.out.println(aa.getCallingAuthenticationValue().getAuthValue().length);
		aa.createRandomAuthenticationValueWith8Bytes();
		System.out.println(aa.getCallingAuthenticationValue().getAuthValue().length);
	}
}
