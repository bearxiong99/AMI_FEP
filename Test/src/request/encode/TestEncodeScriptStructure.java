package request.encode;

import java.io.IOException;
import java.util.Date;

import com.hx.dlms.ASN1ObjectFactory;
import com.hx.dlms.ASN1SequenceOf;
import com.hx.dlms.DlmsData;
import com.hx.dlms.applayer.set.SetRequest;
import com.hx.dlms.applayer.set.SetRequestNormal;

import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fk.bp.dlms.protocol.DlmsProtocolEncoder;
import cn.hexing.util.HexDump;

public class TestEncodeScriptStructure {
	public static void main(String[] args) throws IOException {
		
		DlmsObisItem[] params = new DlmsObisItem[2];
		params[1]= new DlmsObisItem();
		params[1].classId=22;
		params[1].obisString="0.0.15.0.1.255";
		params[1].attributeId=2;
		params[1].data = new DlmsData();
		
		DlmsData[] arrays = new DlmsData[2];
		arrays[0] = new DlmsData();
		arrays[0].setOctetString(HexDump.toArray("00000A006AFF"));
		arrays[1] = new DlmsData();
		arrays[1].setUnsignedLong(1);
		ASN1SequenceOf struct = new ASN1SequenceOf(arrays);
		params[1].data.setStructure(struct);
		
		
		
		
		
		byte[] obis = DlmsProtocolEncoder.convertOBIS(params[1].obisString);
		int clsId = params[1].classId;
		int attId = params[1].attributeId;	
		SetRequestNormal setNormal = new SetRequestNormal(0,clsId,obis,attId,params[1].data);
		SetRequest setReq = new SetRequest();
		setReq.choose(setNormal);

		byte[] apdu = setReq.encode();
		System.out.println(HexDump.toHex(apdu));
	
			
		
	}
}
