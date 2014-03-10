package cn.hexing.fk.bp.dlms.protocol;

import java.io.IOException;

import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.util.HexDump;

import com.hx.dlms.ASN1SequenceOf;
import com.hx.dlms.DlmsData;
import com.hx.dlms.applayer.get.GetRequest;
import com.hx.dlms.applayer.get.GetRequestNormal;

public class SelectiveGetTest {


	private static void testSelective(){
		DlmsObisItem[] params = new DlmsObisItem[1];
		params[0] = new DlmsObisItem();
		
		params[0].attributeId= 2;
		params[0].classId = 7;
		params[0].obisString="0.1.24.3.0.255";
		
		
		DlmsData [] array = new DlmsData[2];
		array [0] = new DlmsData();
		array [1] = new DlmsData();
		
		array[0].setDlmsDateTime("2012-08-09 00:00:00");  //这里简单的添加了DlmsDateTime
		array[1].setDlmsDateTime("2012-08-09 00:00:00");  
		
		byte[] obis = DlmsProtocolEncoder.convertOBIS(params[0].obisString);
		GetRequestNormal getNormal = new GetRequestNormal(1,params[0].classId,obis,params[0].attributeId);		
		ASN1SequenceOf struct = new ASN1SequenceOf(array);
		params[0].data.setStructure(struct );
		getNormal.getSelectiveAccess().setParameter(2, params[0].data);  //这里我把selectiveAccess暂时设置为public
		GetRequest getReq = new GetRequest(getNormal);
		try {
			byte[] apdu = getReq.encode();
			System.out.println("APDU="+HexDump.toHex(apdu));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		testSelective();
	}

}
