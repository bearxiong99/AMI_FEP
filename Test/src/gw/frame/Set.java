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
public class Set {
	public static void main(String[] args) {
		FaalGWNoParamRequest request= new FaalGWNoParamRequest();
        FaalRequestRtuParam rtuParam=new FaalRequestRtuParam();
        int[] tn={1};
        rtuParam.setTn(tn);
        rtuParam.setRtuId("10000005");
        rtuParam.addParam("0CF179", null);
        request.setType(4);
        request.setProtocol("02");
        request.addRtuParam(rtuParam);
		
		Client.getInstance().sendMsg(request);
		System.exit(0);
	}
}
