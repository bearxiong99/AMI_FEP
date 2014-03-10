package request.encode;

import java.io.IOException;

import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.fk.bp.dlms.protocol.DlmsProtocolEncoder;
import cn.hexing.util.HexDump;

import com.hx.dlms.DlmsData;
import com.hx.dlms.applayer.get.GetRequest;
import com.hx.dlms.applayer.get.GetRequestNormal;

public class TestEncodeChannelRead {
	public static void main(String[] args) throws IOException {
		DlmsObisItem[] params = new DlmsObisItem[1];
		params[0]= new DlmsObisItem();
		params[0].classId=3;
		params[0].obisString="1.0.1.8.0.255";
		params[0].attributeId=2;
		
		params[0].data = new DlmsData();
		
		byte[] obis = DlmsProtocolEncoder.convertOBIS(params[0].obisString);
		GetRequestNormal getNormal = new GetRequestNormal(0,params[0].classId,obis,params[0].attributeId);
		GetRequest getReq = new GetRequest(getNormal);
		byte[] apdu = getReq.encode();
		System.out.println(HexDump.toHex(apdu));
	}
}
