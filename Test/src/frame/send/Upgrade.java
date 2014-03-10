package frame.send;

import msg.send.Client;
import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fas.model.dlms.DlmsRequest.DLMS_OP_TYPE;

public class Upgrade {
	public static void main(String[] args) {

//		Client.getInstance().sendM\sg(dr, DLMS_OP_TYPE.OP_SET);

		
		DlmsRequest dr = new DlmsRequest();
		dr.addAppendParam("UpgradeId", "927");
		dr.setOpType(DLMS_OP_TYPE.OP_UPGRADE);
		dr.setMeterId("000012140001");
		dr.setParams(new DlmsObisItem[]{new DlmsObisItem()});
		Client.getInstance().sendMsg(dr);
		System.exit(0);
	}
}
