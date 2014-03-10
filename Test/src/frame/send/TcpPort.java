package frame.send;

import model.ReqDecription;
import msg.send.Client;
import cn.hexing.fas.model.dlms.DlmsRequest.DLMS_OP_TYPE;

import com.hx.dlms.DlmsData;

public class TcpPort {
	public static void main(String[] args) {

//		Client.getInstance().sendMsg(dr, DLMS_OP_TYPE.OP_SET);

		ReqDecription od = new ReqDecription();
		
		od.classId = 41;
		od.obis = "0.11.25.0.0.255";
		od.attrId = 2;
		
		DlmsData data = new DlmsData();
		data.setOctetString("03069".getBytes());
		Client s = null;
		s = Client.getInstance();
		s.sendMsg(od,data ,"Test" ,
				"000012261570",DLMS_OP_TYPE.OP_SET);
		System.exit(0);
	}
}
