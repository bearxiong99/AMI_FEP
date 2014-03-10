package frame.send;

import model.ReqDecription;
import msg.send.Client;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fas.model.dlms.DlmsRequest.DLMS_OP_TYPE;
import frame.assiant.RequestBuilder;

public class StandardSend {
	public static void main(String[] args) {
		ReqDecription od= new ReqDecription(1,"0.0.97.98.10.255", 2);
		od.meterId = "000391800016";
		//od.data.setDoubleLongUnsigned(Long.parseLong("FFFFFFFF",16));
		DlmsRequest request=RequestBuilder.getInstance().build(od);
		request.setOperator("Test");
		request.setOpType(DLMS_OP_TYPE.OP_GET);
		Client.getInstance().sendMsg(request);
	}
}
