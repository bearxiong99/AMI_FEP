package frame.send;

import com.hx.dlms.DlmsData;

import cn.hexing.fas.model.dlms.DlmsRequest.DLMS_OP_TYPE;
import model.ReqDecription;
import msg.send.Client;

public class DisconnectRelay {

	public static void main(String[] args) {
		
//		ObisDecription od = new ObisDecription(70, "0.0.96.3.10.255", 2);//¶Ï¿ª
		ReqDecription od = new ReqDecription(70, "0.0.96.3.10.255", 1);//±ÕºÏ
		Client.getInstance().sendMsg(od, new DlmsData(), "Test", "000090990724", DLMS_OP_TYPE.OP_ACTION);
		System.exit(0);
	}

}
