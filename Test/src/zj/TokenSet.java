package zj;

import msg.send.Client;
import cn.hexing.fas.model.FaalGGKZM33Request;
import cn.hexing.fas.model.FaalRequestRtuParam;

public class TokenSet {
	public static void main(String[] args) {

		FaalGGKZM33Request fr = new FaalGGKZM33Request();
		
		FaalRequestRtuParam params = new FaalRequestRtuParam();
		params.setRtuId("12079802");
		params.setTn(new int []{1});
		params.setCmdId((long) 248510);
		params.addParam("EE20", "11111111111111111122");
		fr.addRtuParam(params);
		fr.setProtocol("04");
		Client.getInstance().sendMsg(fr);	
		
		System.exit(0);
		
	
	}
}
