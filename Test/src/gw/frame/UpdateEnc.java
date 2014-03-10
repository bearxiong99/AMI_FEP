package gw.frame;

import msg.send.Client;
import cn.hexing.fas.model.FaalRequestRtuParam;
import cn.hexing.fas.model.UpdateCollectorKeyRequest;


public class UpdateEnc {
	public static void main(String[] args) {
		UpdateCollectorKeyRequest faal = new UpdateCollectorKeyRequest();
		FaalRequestRtuParam frrp = new FaalRequestRtuParam();
		frrp.setCmdId((long)0);
		frrp.setRtuId("33334444");
		frrp.addParam("10F001", "");
		faal.addRtuParam(frrp);
		faal.setType(16);
		frrp.setTn(new int[]{2});
		faal.setProtocol("02");
		faal.setKeyVersion(0);
		faal.setCollectorNo("1111111111");
		Client.getInstance().sendMsg(faal);
		System.exit(0);
	}
}
