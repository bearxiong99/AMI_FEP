package frame.send.relay;

import msg.send.Client;
import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fas.model.dlms.DlmsRequest.BUSINESS_CATEGORY;
import cn.hexing.fas.model.dlms.DlmsRequest.DLMS_OP_TYPE;
import cn.hexing.fas.model.dlms.RelayParam;

public class ReadEnery {
	public static void main(String[] args) throws InterruptedException {
		for(int i = 0 ; i < 1; i ++ ){
			RelayParam relayParam = new RelayParam();
			relayParam.setDcLogicalAddress("33334444");//集中器logicalAddress
			relayParam.setMeasurePoint(5); //测量点
			DlmsRequest request=new DlmsRequest();
			request.setCategory(BUSINESS_CATEGORY.BIZ_RELAY);
			request.setRelayParam(relayParam);
			DlmsRequest dr = new DlmsRequest();
			dr.setRelayParam(relayParam);
			DlmsObisItem[] params = new DlmsObisItem[1];
			params[0] = new DlmsObisItem();
			params[0].classId = 3;
			params[0].obisString = "1.0.1.8.0.255";
			params[0].attributeId = 2;
			dr.setParams(params);
			dr.setOperator("Test");
			dr.setOpType(DLMS_OP_TYPE.OP_GET);
			Client.getInstance().sendMsg(dr);
			Thread.sleep(1000);
		}
		System.exit(0);
	}
}
