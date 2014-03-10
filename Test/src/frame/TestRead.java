package frame;

import msg.send.Client;
import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fas.model.dlms.DlmsRequest.DLMS_OP_TYPE;

public class TestRead {
	public static void main(String[] args) {
		DlmsObisItem[] params = new DlmsObisItem[1];
		params[0] = new DlmsObisItem();
		params[0].classId = 8;
		params[0].obisString = "0.0.1.0.0.255";
		params[0].attributeId = 2;
//		params[0].data.setVisiableString("999910");
		DlmsRequest dr = new DlmsRequest();
		dr.setParams(params);
		dr.setOperator("Test");
		dr.setMeterId("100000024000");
		Client.getInstance().sendMsg(dr, DLMS_OP_TYPE.OP_GET);
		System.exit(0);
	}
}
