package send;

public class TestControlWord {
	public static void main(String[] args) {
		int controlWord = 0xFE;
		System.out.println((controlWord>>5)&0x0F);
		int sss=((controlWord&0x0F)>>1)&0x0F;
		System.out.println(sss);
		System.out.println(((0x02&0x0F)>>1)&0x0F);
		
		int rrr=3;
		System.out.println((0xFE & (rrr<<5)|(1<<4))|(sss<<1));
	}
}
