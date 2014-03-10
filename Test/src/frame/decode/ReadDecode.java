package frame.decode;

import java.io.IOException;

import cn.hexing.fk.utils.DateConvert;
import cn.hexing.fk.utils.HexDump;

import com.hx.dlms.ASN1SequenceOf;
import com.hx.dlms.ASN1Type;
import com.hx.dlms.DecodeStream;
import com.hx.dlms.DlmsData;
import com.hx.dlms.applayer.get.GetResponse;
import com.hx.dlms.applayer.get.GetResponseNormal;

public class ReadDecode {
	public static void main(String[] args) throws IOException {
		DlmsData d1 = new DlmsData();
		d1.setOctetString(new byte[]{(byte) 0x99,(byte) 0x99,0x1});
		System.out.println(HexDump.toHex(d1.encode()));
		byte[] apdu = HexDump.toArray("C401060002081200DE120000060000000006000000000600000000060000000006000000001203E8");
		GetResponse resp = new GetResponse();
		resp.decode(DecodeStream.wrap(apdu));
		GetResponseNormal grn = (GetResponseNormal) resp.getDecodedObject();
		ASN1Type S = grn.getResult().getDecodedObject();
		DlmsData dd =grn.getResult().getData();
		int value = dd.getUnsignedLong();
		double dbl = value;
		byte[] bcd = dd.getBcd();
		int val = 0, b = 0;
		for (int i = 0; i < bcd.length; i++) {
			b = (bcd[i] >> 4 & 0x0F) * 10 + (bcd[i] & 0x0F);
			val = val * 100 + b;
		}
		System.out.println(val);
		DlmsData[] arrays=dd.getArray();
		for(DlmsData array : arrays){
			ASN1Type[] members = array.getStructure().getMembers();
			for(ASN1Type member : members){
				DlmsData date = (DlmsData) member;
				System.out.println(date.getDateTime().toString());
			}
		}
		System.out.println(dd.getDateTime());
		System.out.println(DateConvert.iranToGregorian("0000-00-00 00:00:00"));
	}
}
