package gw.frame;

import msg.send.Client;
import cn.hexing.fas.model.FaalGWAFN0ARequest;
import cn.hexing.fas.model.FaalRequestRtuParam;

/**
 * 
 * @author gaoll
 *
 * @time 2012年11月9日14:37:43
 *
 * @info 读测量点参数
 */
public class Read {
	public static void main(String[] args) throws InterruptedException {
		FaalGWAFN0ARequest request= new FaalGWAFN0ARequest();
        FaalRequestRtuParam rtuParam=new FaalRequestRtuParam();
        int[] tn={0};
        //request.setTpTimeout(10);
        rtuParam.setTn(tn);
//        request.setParam(new int[]{2});
        rtuParam.setCmdId((long) 238664);
        rtuParam.setRtuId("22220000");
        rtuParam.addParam("04F010",null);
        request.setType(10);
        request.setProtocol("02");
        request.addRtuParam(rtuParam);
//		request.setParam(new int[]{2,3,4});
		Client.getInstance().sendMsg(request);
		System.exit(0);
	}
}
