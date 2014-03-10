package gw.frame;

import msg.send.Client;
import cn.hexing.fas.model.FaalGWNoParamRequest;
import cn.hexing.fas.model.FaalRequestRtuParam;

/**
 * 
 * @author gaoll
 *
 * @time 2012-11-6 ÏÂÎç1:53:22
 *
 * @info ¸æ¾¯×´Ì¬×ÖÉèÖÃ  ANF04   FN:09
 */
public class AlarmSet {
	public static void main(String[] args) {
		FaalGWNoParamRequest request= new FaalGWNoParamRequest();
        FaalRequestRtuParam rtuParam=new FaalRequestRtuParam();
        int[] tn={0};
        request.setTpTimeout(10);
        rtuParam.setTn(tn);
        rtuParam.setRtuId("33334444");
        rtuParam.addParam("04F009", "1111111111111111111111111111111111111111111111111111111111111111#1111111111111111111111111111111111111111111111111111111111111111");
        rtuParam.setCmdId((long) 44);
        request.setType(4);
        request.setProtocol("02");
        request.addRtuParam(rtuParam);
		
		Client.getInstance().sendMsg(request);
		System.exit(0);
	}
}
