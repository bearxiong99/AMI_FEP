package frame.send;

import model.ReqDecription;
import msg.send.Client;
import cn.hexing.fas.model.dlms.DlmsRequest.DLMS_OP_TYPE;

import com.hx.dlms.DlmsData;
import com.hx.dlms.applayer.SelectiveAccessDescriptor;

public class Get {
	public static void main(String[] args) throws InterruptedException {
//		Client.getInstance().sendMsg(dr, DLMS_OP_TYPE.OP_SET);
		
		//040002800201FF02
		//4   0.2.128.2.1.255 2
		//000100010001000DC0010500080000010000FF0200
		for(int i = 0 ; i <1;i++){
		ReqDecription od = new ReqDecription();
		od.classId =7;
		od.obis = "1.0.99.1.0.255";
		od.attrId =2;
//		od.classId = 7;
//		od.obis = "0-0:98.1.0.255";
//		od.attrId =2;
		DlmsData data = new DlmsData();
		data.setDoubleLongUnsigned(1);
//		data.setDlmsDateTime("2013-12-02 23:59:f00");	
//		data.setVisiableString("Read Water Meter");
//		data.setVisiableString("2;20;0#3;20;0");
//		data.setBool(false);
//		data.setUnsigned(0x5A);
		SelectiveAccessDescriptor sad = new SelectiveAccessDescriptor();
//		sad.selectByIndex(556, 557);
//		sad.selectByPeriodOfTime("2012-01-02 00:00:00", "2012-01-02 00:00:00");
//		data.assignValue(sad.getParameter());
//		data.setVisiableString(("2013-01-18 00:00:00"));
//		Client.getInstance().sendMsg(od,data ,"Test" ,
//				"10000000"+"0000".substring((""+i).length())+(""+i%10),DLMS_OP_TYPE.OP_GET);
		Client.getInstance().sendMsg(od,data ,"Test" ,
				"000010850002",DLMS_OP_TYPE.OP_GET);

		}
		Thread.sleep(3000);
		System.exit(0);
	}
}