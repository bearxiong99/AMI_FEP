package zj;

import java.util.Date;

import msg.send.Client;
import cn.hexing.fas.model.FaalGGKZM11Request;
import cn.hexing.fas.model.FaalRequestRtuParam;

public class Read {
	
	
	public static void main(String[] args) {
		FaalGGKZM11Request frcdr = new FaalGGKZM11Request();
		
		frcdr.setProtocol("04");
		frcdr.setDataTime(new Date());
		FaalRequestRtuParam frrp = new FaalRequestRtuParam();
		frrp.addParam("EA30",null);
		frrp.setTn(new int[]{2});
		frrp.setCmdId((long) 250024);
		frrp.setRtuId("12079802");
		frcdr.addRtuParam(frrp);
		Client.getInstance().sendMsg(frcdr);	
		
		System.exit(0);
	}
}
