package test;

import java.io.IOException;
import java.util.ArrayList;


public class Test1 {
	
	public static void main(String[] args) throws ClassNotFoundException, IOException {
		int bcd2 = 0;
		byte[] value = new byte[]{0,21,82};
		for(int i=1; i<value.length; i++ ){
			bcd2 = (bcd2 * 100) + (( (value[i]>>4) & 0x0F)*10) + (value[i]&0x0F);
		}
		System.out.println(bcd2);
		System.out.println(1500+50+2);
		byte[] bcd = value;
		int val = 0, b = 0;
		for (int i = 0; i < bcd.length; i++) {
			b = (bcd[i] >> 4 & 0x0F) * 10 + (bcd[i] & 0x0F);
			val = val * 100 + b;
		}
		System.out.println(val);
	}
}
