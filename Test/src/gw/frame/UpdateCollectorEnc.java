package gw.frame;

import msg.send.Client;
import cn.hexing.fas.model.FaalGWupdateKeyRequest;
import cn.hexing.fas.model.FaalRequestRtuParam;


public class UpdateCollectorEnc {
	public static void main(String[] args) {
		FaalGWupdateKeyRequest faal = new FaalGWupdateKeyRequest();
		FaalRequestRtuParam frrp = new FaalRequestRtuParam();
		frrp.setCmdId((long)0);
		frrp.setRtuId("33331111");
		frrp.addParam("06F022", "");
		faal.addRtuParam(frrp);
		faal.setType(6);
		frrp.setTn(new int[]{2});
		faal.setProtocol("02");
		faal.setKeyVersion(3);
		faal.setCollectorNo("11111111111111");
		Client.getInstance().sendMsg(faal);
		System.exit(0);
	}
}
