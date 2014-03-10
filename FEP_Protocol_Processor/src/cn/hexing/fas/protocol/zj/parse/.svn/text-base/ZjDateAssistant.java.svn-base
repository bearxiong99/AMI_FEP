package cn.hexing.fas.protocol.zj.parse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import cn.hexing.fk.utils.DateConvert;
import cn.hexing.fk.utils.StringUtil;

/**
 * 
 * @author gaoll
 *
 * @time 2013-5-14 下午04:34:45
 *
 * @info 浙江日期助手
 */
public class ZjDateAssistant {

	private static final Logger log = Logger.getLogger(ZjDateAssistant.class);

	public static void main(String[] args) {
		System.setProperty("bp.isIranTime", "true");
		System.out.println(upDateTimeProcess("2013-03-01 00:00:00", "MM-dd"));
	}
	
	public static String upDateTimeProcess(Date date,String format){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String value = sdf.format(date);
		return upDateTimeProcess(value, format);
	}
	
	public static String downDateTimeProcess(String value,String format){

		
		SimpleDateFormat destSdf = new SimpleDateFormat(format);
		SimpleDateFormat rawSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		
		try {
			String strIsIran=System.getProperty("bp.isIranTime");
			if(strIsIran == null || new Boolean(strIsIran)==false){
				return parse(value, destSdf, rawSdf);
			}else{
				String result = DateConvert.gregorianToIran(value);
				return result.substring(0,format.length());

			}
		} catch (Exception e) {
			log.error(StringUtil.getExceptionDetailInfo(e));
			return "";
		}
	
	}
	
	/**
	 * use for read zj data by time. yyyy-MM-dd hh:mm
	 * @param frame
	 * @param loc
	 * @param time
	 */
	public static void constructDateFrame(byte[] frame,int loc,Calendar stime){
		String strIsIran = System.getProperty("bp.isIranTime");
		boolean  isIran = strIsIran==null || new Boolean(strIsIran)==false?false:true;
		if(isIran){
        	//伊朗历时间处理
        	SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd HH:mm:dd");
        	String strDate=sdf.format(stime.getTime());
        	String iranDate=DateConvert.gregorianToIran(strDate);
        	//format is yyyy-mm-dd HH:mm:dd
        	String[] dateTime=iranDate.split(" ");
        	String date = dateTime[0];
        	String time = dateTime[1];
        	String year = date.split("-")[0];
        	String month = date.split("-")[1];
        	String day = date.split("-")[2];
        	String hour = time.split(":")[0];
        	String minute = time.split(":")[1];
            frame[loc+0]=ParseTool.IntToBcd(Integer.parseInt(year)%100);	//year
	        frame[loc+1]=ParseTool.IntToBcd(Integer.parseInt(month)+1);	//month
	        frame[loc+2]=ParseTool.IntToBcd(Integer.parseInt(day));	//day
	        frame[loc+3]=ParseTool.IntToBcd(Integer.parseInt(hour));	//hour
	        frame[loc+4]=ParseTool.IntToBcd(Integer.parseInt(minute));	//minute	        
		}else{
			frame[1] = ParseTool.IntToBcd(stime.get(Calendar.YEAR) % 100); // year
			frame[2] = ParseTool.IntToBcd(stime.get(Calendar.MONTH) + 1); // month
			frame[3] = ParseTool.IntToBcd(stime.get(Calendar.DAY_OF_MONTH)); // day
			frame[4] = ParseTool.IntToBcd(stime.get(Calendar.HOUR_OF_DAY)); // hour
			frame[5] = ParseTool.IntToBcd(stime.get(Calendar.MINUTE)); //minute	      
		}
	}
	
	public static String upDateTimeProcess(String value,String format){
		
		SimpleDateFormat destSdf = new SimpleDateFormat(format);
		SimpleDateFormat rawSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		
		try {
			String strIsIran=System.getProperty("bp.isIranTime");
			if(strIsIran == null || new Boolean(strIsIran)==false){
				return parse(value, destSdf, rawSdf);
			}else{
				value="13"+value.substring(2);
				String result = DateConvert.iranToGregorian(value);
				return parse(result, destSdf, rawSdf);

			}
		} catch (Exception e) {
			log.error(StringUtil.getExceptionDetailInfo(e));
			return "";
		}
	}

	private static String parse(String value, SimpleDateFormat destSdf,
			SimpleDateFormat rawSdf) {
		Date date = null;
		try {
			date = rawSdf.parse(value);
		} catch (ParseException e) {
			log.error(StringUtil.getExceptionDetailInfo(e));
		}
		return destSdf.format(date);
	}
	
}
