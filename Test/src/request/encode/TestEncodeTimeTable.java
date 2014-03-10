package request.encode;

import java.io.IOException;
import java.util.Date;

import com.hx.dlms.ASN1ObjectFactory;
import com.hx.dlms.ASN1SequenceOf;
import com.hx.dlms.DlmsData;
import com.hx.dlms.applayer.set.SetRequest;
import com.hx.dlms.applayer.set.SetRequestNormal;

import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fk.bp.dlms.protocol.DlmsProtocolEncoder;
import cn.hexing.util.HexDump;

public class TestEncodeTimeTable {
	public static void main(String[] args) throws IOException {
		
		DlmsObisItem[] params = new DlmsObisItem[2];
		params[0]= new DlmsObisItem();
		params[0].classId=22;
		params[0].obisString="0.0.15.0.1.255";
		params[0].attributeId=4;
		params[0].data = new DlmsData();
		
		
		DlmsData[] array = new DlmsData[1];
			array[0] = new DlmsData();
			DlmsData[] array0= new DlmsData[2];
			array0[0] = new DlmsData();
			array0[0].setDlmsTime("00:00:00");  //时间
			array0[1] = new DlmsData();
			array0[1].setDlmsDate("1990-02-02");  //日期
			ASN1SequenceOf struct0 = new ASN1SequenceOf(array0);
			array[0].setStructure(struct0 );
		
		params[0].data.setArray(array );
			
		
		DlmsRequest dr = new DlmsRequest();
		dr.setParams(params);
		//....设置其他的
		params[1]= new DlmsObisItem();
		params[1].classId=22;
		params[1].obisString="0.0.15.0.1.255";
		params[1].attributeId=2;
		params[1].data = new DlmsData();
		
		DlmsData[] arrays = new DlmsData[2];
		arrays[0] = new DlmsData();
		arrays[0].setOctetString(HexDump.toArray("0.0.10.0.106.255"));
		arrays[1] = new DlmsData();
		arrays[1].setUnsignedLong(1);
		ASN1SequenceOf struct = new ASN1SequenceOf(arrays);
		params[1].data.setStructure(struct);
		
		
		
		
		
		
		
		byte[] obis = DlmsProtocolEncoder.convertOBIS(params[0].obisString);
		int clsId = params[0].classId;
		int attId = params[0].attributeId;	
		SetRequestNormal setNormal = new SetRequestNormal(0,clsId,obis,attId,params[0].data);
		SetRequest setReq = new SetRequest();
		setReq.choose(setNormal);

		byte[] apdu = setReq.encode();
		System.out.println(HexDump.toHex(apdu));
	
			
		
	}
}
