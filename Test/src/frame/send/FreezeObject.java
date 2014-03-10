package frame.send;

import model.ReqDecription;
import msg.send.Client;
import cn.hexing.fas.model.dlms.DlmsRequest.DLMS_OP_TYPE;

import com.hx.dlms.DlmsData;

public class FreezeObject {
	public static void main(String[] args) {

//		Client.getInstance().sendMsg(dr, DLMS_OP_TYPE.OP_SET);

		ReqDecription od = new ReqDecription();
		
		od.classId = 7;
		od.obis = "0.0.98.1.0.255";
		od.attrId = 2;
		
		DlmsData data = new DlmsData();
		Client.getInstance().sendMsg(od,data ,"Test" ,
				"000012261233",DLMS_OP_TYPE.OP_GET);
		System.exit(0);
	}
}
