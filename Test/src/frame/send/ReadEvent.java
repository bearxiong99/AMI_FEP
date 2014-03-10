package frame.send;

import msg.send.Client;
import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fas.model.dlms.DlmsRequest.DLMS_OP_TYPE;

import com.hx.dlms.DlmsData;
import com.hx.dlms.applayer.SelectiveAccessDescriptor;

public class ReadEvent {
	public static void main(String[] args) {
		DlmsObisItem[] params = new DlmsObisItem[]{ new DlmsObisItem() };
		DlmsRequest req = new DlmsRequest();
		req.setOperator("Event_Read");
		req.setOpType(DLMS_OP_TYPE.OP_GET);
		params[0].obisString = "0.0.99.98.2.255"; 
		params[0].accessSelector = 1;
		params[0].classId = 7;
		params[0].attributeId = 2;
		req.setMeterId("000009850031");
		SelectiveAccessDescriptor sad = new SelectiveAccessDescriptor();
		sad.selectByPeriodOfTime("2013-05-08 00:00:00", "2013-05-10 00:00:00",true);
		params[0].data.assignValue(sad.getParameter());
		DlmsData dd = new DlmsData();
		dd.assignTag(sad.getParameter());
		req.setSubprotocol("101");
		req.setParams(params);
		Client.getInstance().sendMsg(req);
		System.exit(0);
	}
}
