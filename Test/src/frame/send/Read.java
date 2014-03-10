package frame.send;

import msg.send.Client;
import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fas.model.dlms.DlmsRequest.DLMS_OP_TYPE;

import com.hx.dlms.applayer.SelectiveAccessDescriptor;

public class Read {
	public static void main(String[] args) {
		DlmsRequest req = new DlmsRequest();
		DlmsObisItem[] params = new DlmsObisItem[2];
		params[0] = new DlmsObisItem();
		req.setOpType(DLMS_OP_TYPE.OP_GET);
		params[0].accessSelector = 1;
		params[0].classId = 7;
		params[0].attributeId = 2;
		params[0].obisString="0.0.98.1.0.255";
		
		params[1] = new DlmsObisItem();
		req.setOpType(DLMS_OP_TYPE.OP_GET);
		params[1].accessSelector = 1;
		params[1].classId = 7;
		params[1].attributeId = 2;
		params[1].obisString="0.0.99.1.0.255";
		
		req.setMeterId("014019931279");
		SelectiveAccessDescriptor sad = new SelectiveAccessDescriptor();
		//事件上报的帧里，含有时间，可以利用这个时间进行抄读
		String fromTime = "2012-12-01 00:00:00";
		String toTime = "2012-12-03 00:00:00";
		//true|false 要判断是否是老的规约
		sad.selectByPeriodOfTime(fromTime, toTime,false); 
		params[0].data.assignValue(sad.getParameter());
		req.setParams(params);
//		req.setSubprotocol("103"); 
		Client.getInstance().sendMsg(req);
		System.exit(0);
	}
}
