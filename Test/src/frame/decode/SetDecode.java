package frame.decode;

import java.io.IOException;

import cn.hexing.fk.utils.HexDump;

import com.hx.dlms.ASN1SequenceOf;
import com.hx.dlms.ASN1Type;
import com.hx.dlms.DecodeStream;
import com.hx.dlms.DlmsData;
import com.hx.dlms.applayer.set.SetResponse;
import com.hx.dlms.applayer.set.SetResponseNormal;

public class SetDecode {
	public static void main(String[] args) throws IOException {
		byte[] apdu = HexDump.toArray("C5014F00");
		SetResponse setResp = new SetResponse();
		setResp.decode(DecodeStream.wrap(apdu));
		System.out.println(setResp);
		SetResponseNormal set = (SetResponseNormal) setResp.getDecodedObject();
		ASN1SequenceOf ss = set.getOptionalData().getStructure();
		DlmsData meter=(DlmsData) ss.getMembers()[2];
		System.out.println(meter.getStringValue());
	
		System.out.println();
	}
}
