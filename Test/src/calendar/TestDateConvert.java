package calendar;

import cn.hexing.fk.utils.DateConvert;

public class TestDateConvert {
	public static void main(String[] args) {
		String iranTime = "1391-02-30 00:00:00";
		iranTime = iranTime.substring(2, 4) + iranTime.substring(5, 7)
				+ iranTime.substring(8, 10) + "01" + iranTime.substring(11, 13)
				+ iranTime.substring(14, 16) + iranTime.substring(17, 19);
		iranTime = DateConvert.TOU_IRANToGregorian(iranTime);
		String time = "20" + iranTime.substring(0, 2) + "-"
				+ iranTime.substring(2, 4) + "-" + iranTime.substring(4, 6)
				+ " " + iranTime.substring(8, 10) + ":"
				+ iranTime.substring(10, 12) + ":" + iranTime.substring(12, 14);
		System.out.println(time);
		
		
		time = "2013-03-21 00:00:00";
		
		time = time.substring(2,4) + time.substring(5,7) + time.substring(8,10)
            	+ "01" + time.substring(11,13)+ time.substring(14,16)+ time.substring(17,19);
	    time = DateConvert.TOU_GregorianToIRAN(time);
	    if (Integer.parseInt(time.substring(0,2)) >= 89){
	    	time = "13" + time;
	    }else{
	    	time = "14" + time;
	    }
	    time = time.substring(0,4) + "-" + time.substring(4,6) + "-" + time.substring(6,8)
            	+ " " + time.substring(10,12) + ":" + time.substring(12,14) + ":" + time.substring(14,16);
	    System.out.println(time);
	    
	}
}
