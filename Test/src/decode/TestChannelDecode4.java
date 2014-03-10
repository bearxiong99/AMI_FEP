package decode;

import java.io.IOException;

import cn.hexing.fk.bp.dlms.protocol.DlmsScaleItem;
import cn.hexing.fk.utils.HexDump;

import com.hx.dlms.ASN1Type;
import com.hx.dlms.DecodeStream;
import com.hx.dlms.DlmsData;
import com.hx.dlms.applayer.get.GetDataResult;
import com.hx.dlms.applayer.get.GetResponse;
import com.hx.dlms.applayer.get.GetResponseNormal;
/**
 *	Ω·À„»’∆⁄
 * @author Administrator
 *
 */
public class TestChannelDecode4 {
	public static void main(String[] args) throws IOException {
		DlmsScaleItem dsi = new DlmsScaleItem();
		
		dsi.callingDataType =  1;
		byte[] apdu = HexDump.toArray("C4010C00010102020904000000000905FFFFFF01FF");
		GetResponse resp = new GetResponse();
		resp.decode(DecodeStream.wrap(apdu));
		
	
		GetResponseNormal grn = (GetResponseNormal) resp.getDecodedObject();
		
		GetDataResult dd = grn.getResult();
		DlmsData data = (DlmsData) dd.getDecodedObject();
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
