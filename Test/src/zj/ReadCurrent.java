package zj;

import msg.send.Client;
import cn.hexing.fas.model.FaalReadCurrentDataRequest;
import cn.hexing.fas.model.FaalRequestRtuParam;

public class ReadCurrent {
	
	
	public static void main(String[] args) {
		FaalReadCurrentDataRequest frcdr = new FaalReadCurrentDataRequest();
		
		frcdr.setProtocol("04");
		FaalRequestRtuParam frrp = new FaalRequestRtuParam();
		frrp.setTn(new int[]{0});
		frrp.setCmdId((long) 252722);
		frrp.setRtuId("12079802");
		frcdr.addRtuParam(frrp);
		frcdr.setType(0x15);
		Client.getInstance().sendMsg(frcdr);	
		
		System.exit(0);
	}
}
