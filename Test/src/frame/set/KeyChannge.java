package frame.set;

import msg.send.Client;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fas.model.dlms.DlmsRequest.DLMS_OP_TYPE;

public class KeyChannge {
	public static void main(String[] args) {
		DlmsRequest dr = new DlmsRequest();
		dr.setMeterId("000391800016");
		dr.setESAMGcmKeyChange();
		dr.setOpType(DLMS_OP_TYPE.OP_CHANGE_KEY);
		Client.getInstance().sendMsg(dr);
		System.exit(0);
	}
}
