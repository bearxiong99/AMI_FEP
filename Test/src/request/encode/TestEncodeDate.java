package request.encode;

import java.io.IOException;
import java.util.Date;

import cn.hexing.util.HexDump;

import com.hx.dlms.ASN1Oid;
import com.hx.dlms.DlmsData;
import com.hx.dlms.applayer.set.SetRequest;
import com.hx.dlms.applayer.set.SetRequestNormal;

public class TestEncodeDate {
	public static void main(String[] args) throws IOException {
		DlmsData dd = new DlmsData();
		
		dd.setDlmsDateTime("2012-09-03 00:00:00");
		
		SetRequestNormal setNormal = new SetRequestNormal(0,8,convertOBIS("0.0.1.0.0.255"),2,dd);
		SetRequest setReq = new SetRequest();
		setReq.choose(setNormal);

		byte[] apdu = setReq.encode();		
		System.out.println(HexDump.toHex(apdu));
	}
	
	public static final byte[] convertOBIS(String obis){
		int[] intOids = ASN1Oid.parse(obis);
		if( null == intOids || intOids.length != 6 )
			throw new RuntimeException("Invalid OBIS:"+obis);
		byte[] ret = new byte[6];
		for(int i=0; i<ret.length; i++ )
			ret[i] = (byte) intOids[i];
		return ret;
	}
}
