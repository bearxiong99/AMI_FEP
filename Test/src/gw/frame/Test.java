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
public class Test {
	public static void main(String[] args) {
		FaalGWNoParamRequest request= new FaalGWNoParamRequest();
        FaalRequestRtuParam rtuParam=new FaalRequestRtuParam();
        int[] tn={0};
        request.setTpTimeout(10);
        rtuParam.setTn(tn);
        rtuParam.setRtuId("33334444");
        rtuParam.addParam("04F013", "172.16.241.4#255.255.255.0#172.16.241.1#0#172.16.241.20:201#1#5#x,u,w,e,n#6#1,2,3,4,5,6#8888");
        rtuParam.setCmdId((long) 44);
        request.setType(10);
        request.setProtocol("02");
        request.addRtuParam(rtuParam);
		
		Client.getInstance().sendMsg(request);
		System.exit(0);
	}
}
