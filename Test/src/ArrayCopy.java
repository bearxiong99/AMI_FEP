import java.lang.reflect.InvocationTargetException;

import cn.hexing.util.HexDump;


public class ArrayCopy {
	public static void main(String[] args) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		System.out.println((int)Long.parseLong("029200000003",16));
		

		byte[] bytesContent = HexDump.toArray("1234");
		byte[] dest = new byte[5];
		System.arraycopy(bytesContent, 0, dest, 0, 5);
		System.out.println(HexDump.toHex(dest));
		
		
	}
}
