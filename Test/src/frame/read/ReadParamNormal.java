package frame.read;

import java.io.IOException;

import com.hx.dlms.applayer.get.GetRequest;
import com.hx.dlms.applayer.get.GetRequestNormal;

import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.fk.bp.dlms.protocol.DlmsProtocolEncoder;
import cn.hexing.util.HexDump;

public class ReadParamNormal {
	public static void main(String[] args) throws IOException {
		byte[] apdu = buildReadFrame(11, "0-0:11.0.0.255",2);
		System.out.println(HexDump.toHex(apdu ));
	}
	
	public static byte[] buildReadFrame(int classId,String strObis,int attrId) throws IOException{
		DlmsObisItem[] params = new DlmsObisItem[1];
		params[0]= new DlmsObisItem();
		params[0].classId=classId;
		params[0].obisString=strObis;   //1ÊÇÍ¨µÀºÅ
		params[0].attributeId=attrId;
		byte[] obis = DlmsProtocolEncoder.convertOBIS(params[0].obisString);
		GetRequestNormal getNormal = new GetRequestNormal(0,params[0].classId,obis,params[0].attributeId);
		GetRequest getReq = new GetRequest(getNormal);
		byte[] apdu = getReq.encode();
		return apdu;
	}
}
