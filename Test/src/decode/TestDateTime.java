package decode;

import java.io.IOException;
import java.util.Date;

import cn.hexing.fk.utils.HexDump;

import com.hx.dlms.DlmsData;
import com.hx.dlms.DlmsDateTime;

public class TestDateTime {
	public static void main(String[] args) throws IOException {
		byte[] apdu = HexDump.toArray("0C07DD031AFF0F000000800001");
//		
		DlmsDateTime ddt = new DlmsDateTime();
		ddt.setDlmsDataValue(apdu,1);
		System.out.println(ddt.toString());
		
	}
}
