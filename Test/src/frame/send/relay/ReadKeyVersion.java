package frame.send.relay;


import msg.send.Client;
import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fas.model.dlms.DlmsRequest.BUSINESS_CATEGORY;
import cn.hexing.fas.model.dlms.RelayParam;
import cn.hexing.fas.model.dlms.DlmsRequest.DLMS_OP_TYPE;

public class ReadKeyVersion {
	public static void main(String[] args) {
		DlmsRequest dr = new DlmsRequest();
		RelayParam relayParam = new RelayParam();
		relayParam.setDcLogicalAddress("33334444");//集中器logicalAddress
		relayParam.setMeasurePoint(2); //测量点
		dr.setCategory(BUSINESS_CATEGORY.BIZ_RELAY);
		dr.setRelayParam(relayParam);
		dr.setOperator("ReadKeyVersion");
		DlmsObisItem[] params = new DlmsObisItem[1];
		params[0] = new DlmsObisItem();
		params[0].classId = 1;
		params[0].obisString = "0.0.96.1.142.255";
		params[0].attributeId = 2; // 21times  22 time
		dr.setParams(params);
		dr.setOpType(DLMS_OP_TYPE.OP_GET);
		Client.getInstance().sendMsg(dr);
		System.exit(0);
	}
}
