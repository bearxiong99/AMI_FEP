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
 * @info 参数设置,  用户名,密码以逗号分隔.
 */
public class ParamSet {
	public static void main(String[] args) {
		FaalGWNoParamRequest request= new FaalGWNoParamRequest();
        FaalRequestRtuParam rtuParam=new FaalRequestRtuParam();
        int[] tn={0};
        request.setTpTimeout(10);
        rtuParam.setTn(tn);
        rtuParam.setRtuId("34124433");
        rtuParam.addParam("04F066", "3#123123");
        rtuParam.setCmdId((long) 0);
        request.setType(12);
        request.setProtocol("02");
        request.addRtuParam(rtuParam);
		
		Client.getInstance().sendMsg(request);
		System.exit(0);
	}
}
