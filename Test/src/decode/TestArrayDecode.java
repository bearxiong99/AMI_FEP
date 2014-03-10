package decode;

import java.io.IOException;

import cn.hexing.fk.bp.dlms.protocol.DlmsScaleItem;
import cn.hexing.fk.utils.HexDump;

import com.hx.dlms.ASN1BitString;
import com.hx.dlms.ASN1Type;
import com.hx.dlms.DecodeStream;
import com.hx.dlms.DlmsData;
import com.hx.dlms.applayer.get.GetDataResult;
import com.hx.dlms.applayer.get.GetResponse;
import com.hx.dlms.applayer.get.GetResponseNormal;

public class TestArrayDecode {
	public static void main(String[] args) throws IOException {
		DlmsScaleItem dsi = new DlmsScaleItem();
		
		dsi.callingDataType =  1;   // C40181000202090C07DD030DFF0D1921008000011140
		byte[] apdu = HexDump.toArray("C4010F0001020205090C076C5C0B2F9C3EDB233E23D751A5C52E7A95DFE989D72CFBA78B7D066BA511AECBC0D7121C7C6FA3627151058D352D1B7D0AACC60DB7A723CD5A32F97ED2BA08665D9CA2");
		GetResponse resp = new GetResponse();
		try {
			resp.decode(DecodeStream.wrap(apdu));
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		GetResponseNormal grn = (GetResponseNormal) resp.getDecodedObject();
		
		GetDataResult dd = grn.getResult();
		DlmsData data = dd.getData();
		System.out.println(data.getStringValue());
		ASN1BitString s = data.getBitString();
		dsi.upLinkConvert(null,data,null);
		DlmsData[] members = data.getArray();
		ASN1Type[] mem = members[0].getStructure().getMembers();
		DlmsData[] mems  = new DlmsData[mem.length];
		for(int i = 0; i < mems.length;i++){
			mems[i]=(DlmsData) mem[i];
		}
		
		
		mems[0]=dsi.upLinkConvert(null,mems[0],null);
		
		System.out.println(mems[0].getStringValue());
		
		
	}
}
