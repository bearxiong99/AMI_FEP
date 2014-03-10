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
public class TestChannelDecode5 {
	@SuppressWarnings("unused")
	public static void main(String[] args) throws IOException {
		DlmsScaleItem dsi = new DlmsScaleItem();
		
		dsi.callingDataType =  25;
		byte[] apdu = HexDump.toArray("C401410001030203090C07D90A0101000000FF800000060000000106000000030203090C07D90A01010C0000FF800000060000000206000000030203090C07D90A0202000000FF80000006000000030600000003");
		GetResponse resp = new GetResponse();
		resp.decode(DecodeStream.wrap(apdu));
		
	
		GetResponseNormal grn = (GetResponseNormal) resp.getDecodedObject();
		
		GetDataResult dd = grn.getResult();
		DlmsData data = (DlmsData) dd.getDecodedObject();
		dsi.upLinkConvert(null,data,null);
		DlmsData[] members = data.getArray();
		for(DlmsData mem : members){
			ASN1Type[] ms =mem.getStructure().getMembers();
			for(ASN1Type asn : ms){
				DlmsData d = (DlmsData) asn;
			}
		}
		ASN1Type[] mem = members[0].getStructure().getMembers();
		DlmsData[] mems  = new DlmsData[mem.length];
		for(int i = 0; i < mems.length;i++){
			mems[i]=(DlmsData) mem[i];
		}
		
		mems[0]=dsi.upLinkConvert(null,mems[0],null);
		
		System.out.println(mems[0].getStringValue());
		
		
	}
}
