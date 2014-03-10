package gw.frame;

import msg.send.Client;
import cn.hexing.fas.model.FaalGWNoParamRequest;
import cn.hexing.fas.model.FaalRequestRtuParam;

/**
 * 
 * @author gaoll
 *
 * @time 2012-11-6 下午1:53:22
 *
 * @info 清集中器档案  ANF01 F004
 */
public class ClearFile {
	public static void main(String[] args) {
		FaalGWNoParamRequest request= new FaalGWNoParamRequest();
        FaalRequestRtuParam rtuParam=new FaalRequestRtuParam();
        int[] tn={0};
        request.setTpTimeout(10);
        rtuParam.setTn(tn);
        rtuParam.setRtuId("33334444");
        rtuParam.addParam("01F001", "");
        request.setType(1);
        request.setProtocol("02");
        request.addRtuParam(rtuParam);
		
		Client.getInstance().sendMsg(request);
		System.exit(0);
	}
}
