package frame.send.relay;

import msg.send.Client;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fas.model.dlms.RelayParam;
import cn.hexing.fas.model.dlms.DlmsRequest.DLMS_OP_TYPE;

public class KeyChannge {
	public static void main(String[] args) {
		RelayParam relayParam = new RelayParam();
		relayParam.setDcLogicalAddress("33334444");//集中器logicalAddress
		relayParam.setMeasurePoint(2); //测量点
		DlmsRequest dr = new DlmsRequest();
		dr.setRelayParam(relayParam);
		dr.setESAMGcmKeyChange();
		dr.setOpType(DLMS_OP_TYPE.OP_CHANGE_KEY);
		Client.getInstance().sendMsg(dr);
		System.exit(0);
	}
}
