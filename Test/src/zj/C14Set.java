package zj;

import msg.send.Client;
import cn.hexing.fas.model.FaalGGKZM14Request;
import cn.hexing.fas.model.FaalRequestRtuParam;

public class C14Set {
	public static void main(String[] args) {
		FaalGGKZM14Request fr = new FaalGGKZM14Request();
		
		fr.setEffectiveTime(1);
		
		FaalRequestRtuParam params = new FaalRequestRtuParam();
		params.setRtuId("12079802");
		params.setTn(new int []{1});
		params.setCmdId((long) 218135);
		params.addParam("CA86", "0");
		fr.addRtuParam(params);
		Client.getInstance().sendMsg(fr);	
		
		System.exit(0);
		
	}
}
