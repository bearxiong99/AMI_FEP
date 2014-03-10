package dcsml.base;

import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.hx.dlms.DecodeStream;

import cn.hexing.fk.utils.HexDump;

public class DcsmlTime extends DcsmlUnsigned32{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4173499608655545093L;
	
	
	public DcsmlTime(){}
	
	public DcsmlTime(Date dateTime){
		this.setDateTime(dateTime);
	}
	
	public void setDateTime(Date dateTime){
		long time=dateTime.getTime();
		this.setValue((int) (time/1000));
	}
	
	@Override
	public String toString(){
		
		if(this.getValue()==null || this.getValue().length<=0) return null;
		
		BigInteger bi = new BigInteger(this.getValue());
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(new Long(	bi.intValue()+"000"));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		return sdf.format(c.getTime());
	}
	
	public static void main(String[] args) throws IOException {
		DcsmlTime dt = new DcsmlTime();
		dt.setDateTime(new Date());
		dt.decode(DecodeStream.wrap(HexDump.toHex(dt.encode())));
		System.out.println(dt);
	}
}
