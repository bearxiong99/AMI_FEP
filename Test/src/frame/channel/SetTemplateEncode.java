package frame.channel;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import msg.send.Client;
import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fas.model.dlms.DlmsRequest.DLMS_OP_TYPE;
import cn.hexing.fk.bp.dlms.protocol.DlmsProtocolEncoder;
import cn.hexing.util.HexDump;

import com.hx.dlms.ASN1SequenceOf;
import com.hx.dlms.DlmsData;
import com.hx.dlms.applayer.set.SetRequest;
import com.hx.dlms.applayer.set.SetRequestNormal;

import convert.ObisConvert;

/**
 * 设置通道任务模板
 * @author Administrator
 *
 */
public class SetTemplateEncode {
	public static void main(String[] args) throws IOException {
		
		DlmsObisItem[] params = new DlmsObisItem[1];
		params[0]= new DlmsObisItem();
		params[0].classId=7;
		params[0].obisString="1.0.99.1.0.255";   //1是通道号
		params[0].attributeId=3;
		
		Map<String,String> map = new HashMap<String, String>();
		map.put("0", "8#0000010000FF#2");
		map.put("1", "3#"+ObisConvert.convert("1-0:1.8.0.255")+"#2");
		map.put("2", "3#"+ObisConvert.convert("1-0:2.8.0.255")+"#2");

//		map.put("2", "3#"+ObisConvert.convert("1-0:1.8.1.255")+"#2");
//		map.put("3", "3#"+ObisConvert.convert("1-0:1.8.0.255")+"#2");
//		map.put("4", "3#"+ObisConvert.convert("1-0:1.8.1.255")+"#2");
//		map.put("5", "3#"+ObisConvert.convert("1-0:1.8.1.255")+"#2");
//		map.put("6", "3#"+ObisConvert.convert("1-0:1.8.1.255")+"#2");
//		map.put("7", "3#"+ObisConvert.convert("1-0:1.8.1.255")+"#2");
//		map.put("8", "3#"+ObisConvert.convert("1-0:1.8.1.255")+"#2");
//		map.put("9", "3#"+ObisConvert.convert("1-0:1.8.1.255")+"#2");
//		map.put("10", "3#"+ObisConvert.convert("1-0:1.8.1.255")+"#2");

//		map.put("1", "3#"+ObisConvert.convert("1.0.72.7.0.255")+"#2");
		
		
		DlmsData[] array = new DlmsData[map.size()];
		for(int i = 0 ;i<array.length;i++){
			array[i]= new DlmsData();
		}
		
		
		for(String key:map.keySet()){
			
			String value=map.get(key);
			String[] values = parase(value);
			DlmsData[] array0= new DlmsData[4];
			array0[0] = new DlmsData();
			array0[0].setUnsignedLong(Integer.parseInt(values[0]));
			array0[1] = new DlmsData();
			array0[1].setOctetString(HexDump.toArray(values[1]));
			array0[2] = new DlmsData();
			array0[2].setDlmsInteger((byte) Integer.parseInt(values[2]));
			array0[3] = new DlmsData();
			array0[3].setUnsignedLong(0);
			ASN1SequenceOf struct0 = new ASN1SequenceOf(array0);
			array[Integer.parseInt(key)].setStructure(struct0 );
		}
		
		params[0].data.setArray(array );
			
		
		DlmsRequest dr = new DlmsRequest();
		dr.setMeterId("000010850002");
		dr.setParams(params);
		dr.setOperator("Test");
		Client.getInstance().sendMsg(dr, DLMS_OP_TYPE.OP_SET);
		byte[] obis = DlmsProtocolEncoder.convertOBIS(params[0].obisString);
		int clsId = params[0].classId;
		int attId = params[0].attributeId;	
		SetRequestNormal setNormal = new SetRequestNormal(0,clsId,obis,attId,params[0].data);
		SetRequest setReq = new SetRequest();
		setReq.choose(setNormal);

		byte[] apdu = setReq.encode();
		System.out.println(HexDump.toHex(apdu));
		System.exit(0);
			
	}

	private static String[] parase(String value) {
		return value.split("#");
	}
}
