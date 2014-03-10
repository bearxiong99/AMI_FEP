package zj;

import java.util.Calendar;

import msg.send.Client;

import cn.hexing.fas.model.FaalGGKZM12Request;
import cn.hexing.fas.model.FaalRequestRtuParam;

public class ReadTask {
	public static void main(String[] args) {
		FaalGGKZM12Request request = new FaalGGKZM12Request();
		Calendar c = Calendar.getInstance();
		request.setEndTime(c.getTime());
		c.add(Calendar.DATE, -1);
		request.setProtocol("04");
		request.setStartTime(c.getTime());
		FaalRequestRtuParam rtuparam =new FaalRequestRtuParam();
		rtuparam.setRtuId("12079802");
		rtuparam.addParam("0801", null);
		rtuparam.setCmdId((long) 218143);
		request.addRtuParam(rtuparam);
		request.setType(18); // 0X12=18
		Client.getInstance().sendMsg(request);
		System.exit(0);

	}
}
