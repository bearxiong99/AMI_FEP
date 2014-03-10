package frame.send;

import msg.send.Client;
import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fas.model.dlms.DlmsRequest.DLMS_OP_TYPE;

public class UpgradeRessuie {
	public static void main(String[] args) {

		DlmsRequest request = new DlmsRequest();
		request.addAppendParam("CurrentBlockNum",6);
		request.addAppendParam("MaxSize", 192);
		DlmsObisItem[]	params = new DlmsObisItem[1];
		params[0] = new DlmsObisItem();
		params[0].classId=18;
		params[0].attributeId=2;
		params[0].obisString="0.0.44.0.0.255";
		request.setMeterId("014000000019");
		request.setOpType(DLMS_OP_TYPE.OP_UPGRADE);
		request.addAppendParam("UpgradeStatus", 3);
		Client.getInstance().sendMsg(request );
		System.exit(0);
	}
}
