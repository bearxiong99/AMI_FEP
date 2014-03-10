package frame.send;

import msg.send.Client;
import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fas.model.dlms.DlmsRequest.DLMS_OP_TYPE;

public class ReadKeyVersion {
	public static void main(String[] args) {
		DlmsRequest dr = new DlmsRequest();
		dr.setOperator("ReadKeyVersion");
		DlmsObisItem[] params = new DlmsObisItem[1];
		dr.setMeterId("000391800016");
		params[0] = new DlmsObisItem();
		params[0].classId = 1;
		params[0].obisString = "0.0.96.1.142.255";
		params[0].attributeId = 2; // 21times  22 time
		dr.setParams(params);
		dr.setOperator("Test");
		dr.setOpType(DLMS_OP_TYPE.OP_GET);
		Client.getInstance().sendMsg(dr);
		System.exit(0);
	}
}
