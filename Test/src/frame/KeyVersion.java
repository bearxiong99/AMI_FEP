package frame;

import java.io.IOException;

import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fas.model.dlms.DlmsRequest.DLMS_OP_TYPE;
import cn.hexing.fk.bp.dlms.protocol.DlmsProtocolEncoder;
import cn.hexing.fk.utils.HexDump;

import com.hx.dlms.applayer.get.GetRequest;
import com.hx.dlms.applayer.get.GetRequestNormal;

public class KeyVersion {
	public static void main(String[] args) throws IOException {
		DlmsRequest dr = new DlmsRequest();
		dr.setOperator("ReadKeyVersion");
		DlmsObisItem[] params = new DlmsObisItem[1];
		params[0] = new DlmsObisItem();
		params[0].classId = 1;
		params[0].obisString = "0.0.96.1.142.255";
		params[0].attributeId = 2; 
		dr.setParams(params);
		dr.setOpType(DLMS_OP_TYPE.OP_GET);
		byte[] obis = DlmsProtocolEncoder.convertOBIS(params[0].obisString);
		GetRequestNormal getNormal = new GetRequestNormal(1,params[0].classId,obis,params[0].attributeId);
		GetRequest getReq = new GetRequest(getNormal);
		byte[] apdu = getReq.encode();
		System.out.println(HexDump.toHex(apdu));
	}
}
