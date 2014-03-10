package zj;

import msg.send.Client;
import cn.hexing.fas.model.FaalRequestRtuParam;
import cn.hexing.fas.model.FaalWriteParamsRequest;

public class Set {
	public static void main(String[] args) {
		FaalWriteParamsRequest fwpr = new FaalWriteParamsRequest();
		fwpr.setProtocol("04");
		FaalRequestRtuParam frrp = new FaalRequestRtuParam();
		frrp.addParam("8023", "0");
		frrp.setTn(new int[]{0});
		frrp.setRtuId("12079802");
		frrp.setCmdId(250042L);
		fwpr.addRtuParam(frrp);
		Client.getInstance().sendMsg(fwpr);

		
		System.exit(0);
	}
}
