package gw.frame;

import msg.send.Client;
import cn.hexing.fas.model.FaalGWAFN10Request;
import cn.hexing.fas.model.FaalRequestRtuParam;

public class RelayTime {
	public static void main(String[] args) {
		FaalGWAFN10Request request10 = new FaalGWAFN10Request();
		request10.setProtocol("02");
		request10.setFixAddre("0700000100");
		request10.setType(16);
		request10.setTransmitType("F1");
		request10.setFixProto("02");
		FaalRequestRtuParam params = new FaalRequestRtuParam();
		params.setRtuId("22114433");
		params.setTn(new int[]{0});
		params.addParam("0500000002", "");
		request10.addRtuParam(params);
		request10.setEndata("12:00:00");
		Client.getInstance().sendMsg(request10);
		System.exit(0);
	}
}
