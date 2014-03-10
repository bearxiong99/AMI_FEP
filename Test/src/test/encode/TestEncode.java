package test.encode;

import java.io.IOException;
import java.util.Date;

import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fk.bp.dlms.protocol.DlmsProtocolEncoder;
import cn.hexing.util.HexDump;

import com.hx.dlms.ASN1SequenceOf;
import com.hx.dlms.DlmsData;
import com.hx.dlms.applayer.get.GetRequestNormal;

public class TestEncode {
	public static void main(String[] args) throws IOException {
		DlmsObisItem[] params = new DlmsObisItem[1];
		params[0] = new DlmsObisItem();
		params[0].attributeId= 2;
		params[0].classId = 7;
		params[0].obisString="0.1.24.3.0.255";
		DlmsData [] array = new DlmsData[4];
		array[0] = new DlmsData();
				DlmsData[] array2= new DlmsData[4];
				array2[0] = new DlmsData();
				array2[0].setUnsignedLong(8);
				array2[1] = new DlmsData();
				array2[1].setOctetString(HexDump.toArray("0000010000FF"));
				array2[2] = new DlmsData();
				array2[2].setDlmsInteger((byte) 2);
				array2[3] = new DlmsData();
				array2[3].setUnsignedLong(0);
		array [1] = new DlmsData();
		array [2] = new DlmsData();
		array [3] = new DlmsData();
		ASN1SequenceOf struct1 = new ASN1SequenceOf(array2);
		array[0].setStructure(struct1);
		array[1].setDlmsDateTime("2012-09-03 00:00:00");
		array[2].setDlmsDateTime("2012-09-03 00:00:00");
		ASN1SequenceOf struct = new ASN1SequenceOf(array);
		params[0].data.setStructure(struct );
		params[0].accessSelector = 1;
		DlmsRequest dlmsRequest = new DlmsRequest();
		dlmsRequest.setParams(params);
		byte[] obis = DlmsProtocolEncoder.convertOBIS(params[0].obisString);
		GetRequestNormal getNormal = new GetRequestNormal(0,params[0].classId,obis,params[0].attributeId);
		getNormal.getSelectiveAccess().setParameter(params[0].accessSelector, params[0].data);
		byte[] apdu = getNormal.encode();
		System.out.println(HexDump.toHex(apdu));
		
	}
}
