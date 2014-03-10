package frame.send;

import model.ReqDecription;
import msg.send.Client;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fas.model.dlms.DlmsRequest.DLMS_OP_TYPE;
import frame.assiant.RequestBuilder;

//1#0.0.97.98.10.255#2  读事件过滤 
//8#0.0.1.0.0.255#2  时间
public class Time {
	public static void main(String[] args) throws InterruptedException {
		
		TestThread t1 = new TestThread();
		TestThread t2 = new TestThread();
		t1.c = Client.getInstance();
//		t2.c = Client.getInstance();
		t1.start();
		//t2.start();
	}
	
}

class TestThread extends Thread{
	
	Client  c = null;
	
	@Override
	public void run(){
		for(int i= 0 ; i < 1; i++){
			ReqDecription od= new ReqDecription("18#0.0.44.0.0.255#7");
			//od.meterId = "10"+"0000000000".substring((""+(i+1)).length())+(i+1);
			//od.data.setDoubleLongUnsigned(Long.parseLong("FFFFFFFF",16));
			od.meterId = "000020120716";
			//od.data.setDlmsDateTime(new Date());
			DlmsRequest request=RequestBuilder.getInstance().build(od);
			request.setOpType(DLMS_OP_TYPE.OP_GET);
			Client.getInstance().sendMsg(request);
		}
		System.exit(0);
	}
	
}
