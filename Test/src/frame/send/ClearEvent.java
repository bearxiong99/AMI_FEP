package frame.send;

import java.util.HashMap;
import java.util.Map;

import msg.send.Client;
import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fas.model.dlms.DlmsRequest.DLMS_OP_TYPE;

import com.hx.dlms.DlmsData;

public class ClearEvent {
	public static void main(String[] args) throws InterruptedException {
		Map<String,String> map = new HashMap<String, String>();
//		map.put("0", "000391850017");
//		map.put("1", "000391850016");
		map.put("0", "000090990724");
//		map.put("3", "000391800017");
//		map.put("4", "000020120913");
//		map.put("5", "000391400048");
//		map.put("6", "000088000009");
		for(int i = 0 ; i<map.size();i++){
			DlmsData data = new DlmsData();
			data.setUnsigned(4);
			DlmsRequest dr = new DlmsRequest();
			
			DlmsObisItem[] params = new DlmsObisItem[1];
			params[0] = new DlmsObisItem();
			params[0].classId = 1;
			params[0].obisString = "1.0.144.128.0.255";
			params[0].attributeId = 2;
			params[0].data = data;
			dr.setOperator("Test");
			dr.setMeterId(map.get(""+i));
			dr.setParams(params);
			Client.getInstance().sendMsg(dr, DLMS_OP_TYPE.OP_SET);
			Thread.sleep(100);
		}
		
		for(int i = 0 ; i<map.size();i++){
			DlmsRequest dr = new DlmsRequest();
			DlmsObisItem[] params = new DlmsObisItem[1];
			params[0] = new DlmsObisItem();
			params[0].classId = 1;
			params[0].obisString = "0.0.97.98.0.255";
			params[0].attributeId = 2;
			DlmsData data1 = new DlmsData();
			data1.setDoubleLongUnsigned(0);
			params[0].data = data1;
			dr.setOperator("Test");
			dr.setMeterId(map.get(""+i));
			dr.setParams(params);
			Client.getInstance().sendMsg(dr, DLMS_OP_TYPE.OP_SET);
			Thread.sleep(100);
		}
		
		System.exit(0);
	}
}
