package gw.frame;

import msg.send.Client;
import cn.hexing.fas.model.FaalGWAFN10Request;
import cn.hexing.fas.model.FaalRequestRtuParam;

public class GwRelay {
	public static void main(String[] args) throws InterruptedException {
		FaalGWAFN10Request request =  new FaalGWAFN10Request();
		FaalRequestRtuParam param = new FaalRequestRtuParam();
		param.setTn(new int[]{2}); //������
		param.setRtuId("22113333");
		param.addParam("0700001608", null); //����ֵ��token
		request.setTransmitType("F01");
		request.setFixProto("02");  
		request.addRtuParam(param);
		request.setFixAddre("000020130300"); //���
		request.setProtocol("02");
		request.setType(16);
		Client.getInstance().sendMsg(request);
		Thread.sleep(1000);
		System.exit(0);
		
	}
}
