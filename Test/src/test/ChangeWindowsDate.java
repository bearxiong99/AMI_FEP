package test;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChangeWindowsDate {

    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) {/**
    	 * @author crane.ding
    	 * @version 1.0 2009-3-26
    	 */

    	//Operating system name
    	String osName = System.getProperty("os.name");
    	String cmd = "";
    	try {
    	    if (osName.matches("^(?i)Windows.*$")) {// Window ϵͳ
    	    // ��ʽ HH:mm:ss
    	    cmd = "  cmd /c time 22:35:00";
    	    Runtime.getRuntime().exec(cmd);
    	    // ��ʽ��yyyy-MM-dd
    	    cmd = " cmd /c date 2009-03-26";
    	    Runtime.getRuntime().exec(cmd);
    	} else {// Linux ϵͳ
    	   // ��ʽ��yyyyMMdd
    	   cmd = "  date -s 20090326";
    	   Runtime.getRuntime().exec(cmd);
    	   // ��ʽ HH:mm:ss
    	   cmd = "  date -s 22:35:00";
    	   Runtime.getRuntime().exec(cmd);
    	}
    	} catch (IOException e) {
    	    e.printStackTrace();
    	}
}

}