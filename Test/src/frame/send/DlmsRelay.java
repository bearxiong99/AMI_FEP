package frame.send;

import msg.send.Client;
import cn.hexing.fas.model.dlms.DlmsRelayParam;
import cn.hexing.fas.model.dlms.DlmsRelayParam.RELAY_OPERATION;
import cn.hexing.fas.model.dlms.DlmsRelayParam.RELAY_PROTOCOL;
import cn.hexing.fas.model.dlms.DlmsRequest.DLMS_OP_TYPE;
import cn.hexing.fas.model.dlms.DlmsRequest;

public class DlmsRelay {
	public static void main(String[] args) {
		
		
		DlmsRequest dr = new DlmsRequest();
		DlmsRelayParam[] dlmsRelayParams=new DlmsRelayParam[1];
		dlmsRelayParams[0] = new DlmsRelayParam();
		dlmsRelayParams[0].setStartPos("EC");
		dlmsRelayParams[0].setRequestNum(20);
		dlmsRelayParams[0].setOperation(RELAY_OPERATION.OP_GET);
		dlmsRelayParams[0].setRelayProtocol(RELAY_PROTOCOL.MODBUS);
		
		
		dr.setDlmsRelayParams(dlmsRelayParams);
		dr.setMeterId("000000000000");
		dr.setOpType(DLMS_OP_TYPE.OP_HEXINGEXPAND);
//		dr.setOperator("AUTO_READ_RELAY_EVENT");
		Client.getInstance().sendMsg(dr);
		System.exit(0);
	}
}
