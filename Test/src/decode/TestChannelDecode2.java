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
 * 一个array数据 包含多个数据
 * @author Administrator
 *
 */
public class TestChannelDecode2 {
	public static void main(String[] args) throws IOException {
		DlmsScaleItem dsi = new DlmsScaleItem();
		
		dsi.callingDataType =  1;
		byte[] apdu = HexDump.toArray("C401010001010203090C07DC0802FF0B00000080000106000002E50600000000");
		GetResponse resp = new GetResponse();
		resp.decode(DecodeStream.wrap(apdu));
		
	
		GetResponseNormal grn = (GetResponseNormal) resp.getDecodedObject();
		
		GetDataResult dd = grn.getResult();
		DlmsData data = dd.getData();
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
