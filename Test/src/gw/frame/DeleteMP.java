package gw.frame;

import msg.send.Client;
import cn.hexing.fas.model.FaalGWNoParamRequest;
import cn.hexing.fas.model.FaalRequestRtuParam;
/**
 * 
 * @author gaoll
 *
 * @time 2012-11-6 下午2:05:18
 *
 * @info 删除测量点参数 AFN:04 F010
 */
public class DeleteMP {
	public static void main(String[] args) {
		for(int i = 0; i <1 ; i++){
			
			FaalGWNoParamRequest request= new FaalGWNoParamRequest();
	        FaalRequestRtuParam rtuParam=new FaalRequestRtuParam();
	        int[] tn={0};
	//        request.setTpSendTime("02 16:25:00");
	        request.setTpTimeout(10);
	        rtuParam.setTn(tn);
	        rtuParam.setRtuId("22221111");
	        rtuParam.addParam("04F010", "1#1#0#0#0#0#0#0#0#0#0");
	        request.setType(4);
	        request.setProtocol("02");
	        request.addRtuParam(rtuParam);
			Client.getInstance().sendMsg(request);
		}
		System.exit(0);
	}
}
