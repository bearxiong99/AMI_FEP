package decode;

import java.io.IOException;

import cn.hexing.fk.bp.dlms.convert.DlmsChannelItemConvert;
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
public class TestChannelDecode3 {
	public static void main(String[] args) throws IOException {
		DlmsScaleItem dsi = new DlmsScaleItem();
		
		dsi.callingDataType =  1;
		byte[] apdu = HexDump.toArray("C40101000107020412000809060000010000FF0F02120000020412000109060000600A02FF0F02120000020412000309060100010800FF0F02120000020412000309060100010801FF0F02120000020412000309060100010802FF0F02120000020412000309060100010803FF0F02120000020412000309060100010804FF0F02120000");
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
		
		DlmsChannelItemConvert c = new DlmsChannelItemConvert();
		c.upLinkConvert(null,data,null);
		mems[0]=dsi.upLinkConvert(null,mems[0],null);
		
		System.out.println(mems[0].getStringValue());
		
		
	}
}
