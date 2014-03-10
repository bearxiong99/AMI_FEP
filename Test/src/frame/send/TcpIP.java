package frame.send;

import model.ReqDecription;
import msg.send.Client;
import cn.hexing.fas.model.dlms.DlmsRequest.DLMS_OP_TYPE;

import com.hx.dlms.DlmsData;

public class TcpIP {
	public static void main(String[] args) throws InterruptedException {

//		Client.getInstance().sendMsg(dr, DLMS_OP_TYPE.OP_SET);

		ReqDecription od = new ReqDecription();
		
		od.classId = 1;
		od.obis = "1.0.139.129.0.255";
		od.attrId = 2;
		DlmsData data = new DlmsData();
		data.setVisiableString("44");
		Client s = Client.getInstance();
		Thread.sleep(1000);
		
		long startTime=System.currentTimeMillis();

		for(int i = 0; i < 10000;i++){
			s.sendMsg(od,data ,"Test" ,
					"014019941922",DLMS_OP_TYPE.OP_SET);
		}
		long endTime=System.currentTimeMillis();
		System.out.println("use time : "+(endTime-startTime));
		System.exit(0);
	}
}
