package gw.frame;

import msg.send.Client;
import cn.hexing.fas.model.FaalGWAFN0ERequest;
import cn.hexing.fas.model.FaalRequestRtuParam;

public class ReadEvent {
	public static void main(String[] args) {
		FaalGWAFN0ERequest read = new FaalGWAFN0ERequest();
        FaalRequestRtuParam rtuParam=new FaalRequestRtuParam();
        int[] tn={0};
        //request.setTpTimeout(10);
        rtuParam.setTn(tn);
        rtuParam.setCmdId((long)0);
        rtuParam.setRtuId("12345555");
        rtuParam.addParam("0EF001", null);
        read.setType(14);
        read.setPm(0);
        read.setPn(12);
        read.setProtocol("02");
        read.addRtuParam(rtuParam);
		
		Client.getInstance().sendMsg(read);
		System.exit(0);
	}
}
