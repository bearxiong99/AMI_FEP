package gw.frame;

import msg.send.Client;
import cn.hexing.fas.model.FaalGWNoParamRequest;
import cn.hexing.fas.model.FaalRequestRtuParam;
/**
 * 
 * @author gaoll
 *
 * @time 2012-11-6 下午1:47:20
 *
 * @info 设置终端主动上报 ANF-05 -F029
 */
public class TerminalReport {
	public static void main(String[] args) {
		FaalGWNoParamRequest request= new FaalGWNoParamRequest();
        FaalRequestRtuParam rtuParam=new FaalRequestRtuParam();
        int[] tn={0};
        request.setTpTimeout(10);
        rtuParam.setTn(tn);
        rtuParam.setRtuId("22220000");
        rtuParam.addParam("05F001", "11110000");
        request.setType(5);
        request.setProtocol("02");
        request.addRtuParam(rtuParam);
        Client.getInstance();
		Client.getInstance().sendMsg(request);
		System.exit(0);
	}
}
