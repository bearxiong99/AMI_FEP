import java.io.ByteArrayOutputStream;
import java.util.Calendar;

import MyHTTP.AcceptTypes;
import MyHTTP.GetRequest;
import MyHTTP.Standards;


public class TestMyHttp {

	public static void main(String[] args) throws Exception {
		GetRequest getRequest=new GetRequest();
		getRequest.header_only=true;
		getRequest.lock=false;
		getRequest.accept_types=new AcceptTypes();
		getRequest.accept_types.standards=new Standards(new byte[1],(byte)4);
		getRequest.accept_types.standards.setHtml();
		getRequest.accept_types.standards.setPlain_text();
		getRequest.url="www.asnlab.org";
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2012);
		cal.set(Calendar.MONTH, Calendar.DECEMBER);
		cal.set(Calendar.DAY_OF_MONTH, 21);
		cal.set(Calendar.HOUR_OF_DAY, 12);
		cal.set(Calendar.MINUTE, 12);
		cal.set(Calendar.SECOND, 21);
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.ZONE_OFFSET, 0);
		getRequest.timestamp = cal.getTime();
		ByteArrayOutputStream bos=new ByteArrayOutputStream();
		getRequest.ber_encode(bos);
		byte[] bs=bos.toByteArray();
		for(int i=0; i<bs.length; i++) {
			System.out.printf("%02X ", bs[i] & 0xFF);
		}
	}

}