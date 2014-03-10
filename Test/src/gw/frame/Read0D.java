package gw.frame;

import java.util.ArrayList;
import java.util.List;

import msg.send.Client;

import cn.hexing.fas.model.FaalGWAFN0DRequest;
import cn.hexing.fas.model.FaalRequestParam;
import cn.hexing.fas.model.FaalRequestRtuParam;

public class Read0D {
	public static void main(String[] args) {
		FaalGWAFN0DRequest req = new FaalGWAFN0DRequest();
		req.setStartTime("2012-11-02");
		req.setCount(100);
		req.setProtocol("07");
		req.setType(0x0D);
//		req.setOperator("gwldbz");
		req.setTxfs(2);
//		req.setInterval(15);
		FaalRequestRtuParam p = new FaalRequestRtuParam();
		p.setCmdId((long)0);
		p.setRtuId("91000012");
		p.setTn(new int[]{2,3});
		List<FaalRequestParam> params = new ArrayList<FaalRequestParam>();
		params.add(new FaalRequestParam("0DF161",null));
		p.setParams(params);
		req.addRtuParam(p);
		Client.getInstance().sendMsg(req);
		System.exit(0);
	}
}
