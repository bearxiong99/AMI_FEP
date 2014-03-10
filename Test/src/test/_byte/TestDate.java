package test._byte;

import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.Date;

public class TestDate {
	public static void main(String[] args) 
	{
		Date d = new Date();
		Calendar c = Calendar.getInstance();
		c.clear();
		c.set(Calendar.YEAR,2009);
		System.out.println(c.get(Calendar.YEAR));
		System.out.println(d.getTime());
		c.setTime(d);
		System.out.println(c.get(Calendar.YEAR));
		
		int i = 20091;
		
		Short s = Short.valueOf(""+i);
		System.out.println(s>>8);
		
		ByteBuffer buf = ByteBuffer.allocate(4);
		buf.putInt(i);
		for(int i1 = 0 ; i1 < buf.array().length;i1++)
		{
			System.out.println(buf.array()[i1]);
		}
		
		c= Calendar.getInstance();
		

		Calendar c2 = Calendar.getInstance();
		
		System.out.println(c2.get(Calendar.YEAR));
		System.out.println(c2.get(Calendar.MONTH));
		System.out.println(c2.get(Calendar.DAY_OF_MONTH));
		System.out.println(c2.get(Calendar.DAY_OF_WEEK));
		
		System.out.println(c2.get(Calendar.ZONE_OFFSET));
		
		
	}
}
