package gw.frame;

import msg.send.Client;
import cn.hexing.fas.model.FaalGWupdateKeyRequest;
import cn.hexing.fas.model.FaalRequestRtuParam;

public class UpdateKey {
	public static void main(String[] args) {
		
		FaalGWupdateKeyRequest gwUpdate = new FaalGWupdateKeyRequest();
		gwUpdate.setProtocol("02");
		gwUpdate.setType(6);
		FaalRequestRtuParam param = new FaalRequestRtuParam();
		param.setRtuId("22114433");
		param.setTn(new int[]{0});
		param.addParam("06F021", "");
		gwUpdate.addRtuParam(param);
		
		Client.getInstance().sendMsg(gwUpdate);
		System.exit(0);
		
		
	}
}
