package frame.send;

import model.ReqDecription;
import msg.send.Client;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fas.model.dlms.DlmsRequest.DLMS_OP_TYPE;
import frame.assiant.RequestBuilder;

public class ReadDayFreeze {
	public static void main(String[] args) {


		ReqDecription od= new ReqDecription("7#0.0.98.1.128.255#2");
		od.meterId = "000020120716";
		//od.data.setDoubleLongUnsigned(Long.parseLong("FFFFFFFF",16));
		//od.data.setDlmsDateTime(new Date());
		DlmsRequest request=RequestBuilder.getInstance().build(od);
		request.setOperator("Test");
		request.setOpType(DLMS_OP_TYPE.OP_GET);
		Client.getInstance().sendMsg(request);
		
	
	}
}
