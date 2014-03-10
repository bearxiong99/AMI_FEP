package frame.send.relay;

import msg.send.Client;
import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fas.model.dlms.DlmsRequest.BUSINESS_CATEGORY;
import cn.hexing.fas.model.dlms.DlmsRequest.DLMS_OP_TYPE;
import cn.hexing.fas.model.dlms.RelayParam;

import com.hx.dlms.applayer.SelectiveAccessDescriptor;

public class ReadEvent {
	public static void main(String[] args) {
		RelayParam relayParam = new RelayParam();
		relayParam.setDcLogicalAddress("22114433");//集中器logicalAddress
		relayParam.setMeasurePoint(3); //测量点
		DlmsObisItem[] params = new DlmsObisItem[]{ new DlmsObisItem() };
		DlmsRequest req = new DlmsRequest();
		req.setOperator("Event_Read");
		req.setRelayParam(relayParam);
		req.setOpType(DLMS_OP_TYPE.OP_GET);
		req.setCategory(BUSINESS_CATEGORY.BIZ_RELAY);
		params[0].obisString = "0.0.99.98.0.255"; 
		params[0].accessSelector = 1;
		params[0].classId = 7;
		params[0].attributeId = 2;
		SelectiveAccessDescriptor sad = new SelectiveAccessDescriptor();
		sad.selectByPeriodOfTime("1391-06-29 00:00:00", "1391-06-29 24:00:00");
		params[0].data.assignValue(sad.getParameter());
		req.setParams(params);
		Client.getInstance().sendMsg(req);
		System.exit(0);
	}
}
