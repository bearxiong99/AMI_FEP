package zj;

import msg.send.Client;
import cn.hexing.fas.model.FaalReadCurrentDataRequest;
import cn.hexing.fas.model.FaalRequestRtuParam;

public class Test {
	public static void main(String[] args) throws InterruptedException {
		FaalReadCurrentDataRequest request = new FaalReadCurrentDataRequest();
		request.setProtocol("01");
		FaalRequestRtuParam param = new FaalRequestRtuParam();
		param.addParam("CB01", null);
		param.setTn(new int[]{0});
		param.setRtuId("11198853");
		param.setCmdId((long) 0);
		request.addRtuParam(param);
		Client.getInstance().sendMsg(request);
		Thread.sleep(1000);
		System.exit(0);
	}
}
