package test;

import java.util.Random;

import cn.hexing.fk.utils.HexDump;

public class TestRandom {
	public static void main(String[] args) {
		byte[] authValue = new byte[16];
		Random random = new Random(System.currentTimeMillis());
		System.out.println(random.nextInt());
		random.nextBytes(authValue);
		System.out.println(HexDump.toHex(authValue));
		HexDump.toArray("41413233423936383433363244393234");
	}
}
