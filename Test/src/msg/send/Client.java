package msg.send;

import model.ReqDecription;
import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fas.model.dlms.DlmsRequest.DLMS_OP_TYPE;
import cn.hexing.fk.clientmod.ClusterClientModule;
import cn.hexing.fk.message.msgbytes.MessageBytes;
import cn.hexing.fk.utils.HexDump;

import com.hx.dlms.DlmsData;

public class Client {
	
	public static Client instance ;
	ClusterClientModule ccmm = new ClusterClientModule();
	public static Client getInstance(){
		if(instance==null){
			instance = new Client();
		}
		return instance;
	}
	
	private Client(){
		init();
	}
	public void init(){
		//
//		ccmm.setClientsUrl("192.168.2.167:7778");
		ccmm.setClientsUrl("127.0.0.1:7778");
//		ccmm.setClientsUrl("192.168.2.172:7778;192.168.2.166:7778");
		ccmm.setName("bpClinets");
		ccmm.setBufLength(10240);
		ccmm.setTimeout(4);
		ccmm.setHeartInterval(60);
		ccmm.setRequestNum(500);
		ccmm.start();
		//
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void sendMsg(Object o){
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ccmm.sendRequest(null, null, o);
	}
	
	
	public void sendMsg(DlmsRequest request, DLMS_OP_TYPE opType){
		request.setOpType(opType);
		request.setProtocol("03");
		ccmm.sendRequest(null, null, request);
	}
	public void sendMsg(DlmsRequest request){
		request.setProtocol("03");
		ccmm.sendRequest(null, null, request);
	}
	
	public static void main(String[] args) {
		
		DlmsRequest dr = new DlmsRequest();
		
		dr.setMeterId("000020120716");
		dr.setParams(new DlmsObisItem[]{new DlmsObisItem()});
		dr.getParams()[0].classId=20;
		dr.getParams()[0].attributeId=4;
		dr.getParams()[0].obisString="0.0.13.0.0.255";
		MessageBytes mb = new MessageBytes();
		mb.write(HexDump.toByteBuffer("0001000100100012DD1000000000303030303230313230373136"));
		
		
		Client.getInstance().sendMsg(mb);
		
		//System.exit(0);
	}

	public void sendMsg(ReqDecription od, DlmsData data,
			String operator, String meterId, DLMS_OP_TYPE opType) {
		DlmsRequest dr =new DlmsRequest();
		DlmsObisItem[] params = new DlmsObisItem[]{ new DlmsObisItem() };
		params[0] = new DlmsObisItem();
		params[0].classId = od.classId;
		params[0].obisString = od.obis;
		params[0].attributeId = od.attrId;
		params[0].data = data;
		params[0].cmdId = 404378;
		dr.setCommId(404378);
//		params[0].accessSelector=2;
		dr.setOperator(operator);
		dr.setMeterId(meterId);
		dr.setOpType(opType);
		dr.setParams(params);	
		this.sendMsg(dr);
	}
	
	
}
