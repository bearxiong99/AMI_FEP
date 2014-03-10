package frame.tariff.encode;

import java.io.IOException;

import msg.send.Client;

import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fas.model.dlms.DlmsRequest.DLMS_OP_TYPE;
import cn.hexing.util.HexDump;

import com.hx.dlms.DlmsData;

import frame.set.SetParamNormal;

public class SetTariffName {
	public static void main(String[] args) throws IOException {
		
		String calendar="TOU";
		DlmsData data = new DlmsData();
		
		data.setOctetString((calendar+"      ".substring(0, 6-calendar.length())).getBytes());
		
		
		
		DlmsRequest dr = new DlmsRequest();
		DlmsObisItem[] params = new DlmsObisItem[1];
		params[0] = new DlmsObisItem();
		params[0].classId = 20;
		params[0].attributeId=6;
		params[0].obisString = "0.0.13.0.0.255";
		params[0].data = data;
		dr.setParams(params );
		dr.setMeterId("000020130708");
		dr.setOperator("Test");
		Client.getInstance().sendMsg(dr, DLMS_OP_TYPE.OP_SET);
		byte[] apdu =SetParamNormal.buildSetFrame(20, "0.0.13.0.0.255", 2, data);
		System.out.println(HexDump.toHex(apdu));
		System.exit(0);
	}
}
