package frame.decode;

import java.io.IOException;

import cn.hexing.util.HexDump;

import com.hx.dlms.ASN1Type;
import com.hx.dlms.DecodeStream;
import com.hx.dlms.applayer.action.ActionResponse;
import com.hx.dlms.applayer.action.ActionResponseNormal;
import com.hx.dlms.applayer.action.ResponseOptionalData;

public class ActionDecode {
	public static void main(String[] args) throws IOException {
		byte[] apdu = HexDump.toArray("C7018100");
		ActionResponse ar  =new ActionResponse();
		ar.decode(DecodeStream.wrap(apdu));
		ActionResponseNormal selObj = (ActionResponseNormal) ar.getDecodedObject();
		ResponseOptionalData respData = selObj.getRespWithOptionalData();
		System.out.println(respData.getActionResultEnum().getEnumValue());
		ar.toString();
	}
}
