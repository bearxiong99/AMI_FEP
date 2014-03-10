package frame.tariff.encode;

import java.io.IOException;
import java.nio.ByteBuffer;

import msg.send.Client;

import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fas.model.dlms.DlmsRequest.DLMS_OP_TYPE;
import cn.hexing.util.HexDump;

import com.hx.dlms.ASN1SequenceOf;
import com.hx.dlms.DlmsData;

import frame.set.SetParamNormal;

/**
 * 设置日费率
 * @author Administrator
 *
 *	array[max=8]    
	day structure[2]
	{
	   day_id:  unsigned,
	   array[max=8]
	   day_profile_action structure[3]
	   {
	      start_time: octet-string[4],
	      script_logical_name: octet-string[6],
	      0-0:10.0.100.255
	      script_selector: long-unsigned
	      1-tariff 1
	      2-tariff 2
	      3-tariff 3
	      4-tariff 4
	    }
	}
	C10100
	001400000D0000FF0500
	01 01 
	0202
		1101
		01 01
			0203
			090412322952
			090600000D0000FF
			120001
	frame = C10100001400000D0000FF050001010202110101010203090412322952090600000D0000FF120001
 
 */
public class SetDayTable {
	public static void main(String[] args) throws IOException {
		DlmsData data = new DlmsData();
		
		DlmsData[] array = new DlmsData[2];
		for(int i=0;i<array.length;i++){
			DlmsData[] array2 = new DlmsData[2];
			array2[0] = new DlmsData();
			array2[0].setUnsigned(i+1);  //day_id
				DlmsData[] dayProfileActions = new DlmsData[1];
				for(int j=0;j<dayProfileActions.length;j++){
					dayProfileActions[j] = new DlmsData();
					DlmsData[] dayProfileAction = new DlmsData[3];
					dayProfileAction[0] = new DlmsData();
					dayProfileAction[0].setDlmsTime("02:1"+i+":00");  //start_time
					dayProfileAction[1] = new DlmsData();
					dayProfileAction[1].setOctetString(HexDump.toArray("00000A0064FF"));//script_logical_name
					dayProfileAction[2] = new DlmsData();
					dayProfileAction[2].setUnsignedLong(i+1);	//script_selector
					ASN1SequenceOf struct = new ASN1SequenceOf(dayProfileAction);
					dayProfileActions[j].setStructure(struct);
				}
			array2[1]= new DlmsData();
			array2[1].setArray(dayProfileActions);
			ASN1SequenceOf struct1 = new ASN1SequenceOf(array2);
		array[i]= new DlmsData();
		array[i].setStructure(struct1);
		}

		data.setArray(array);
		
		DlmsObisItem[] params = new DlmsObisItem[1];
		params[0] = new DlmsObisItem();
		params[0].classId = 41;
		params[0].obisString = "0.11.25.0.0.255";
		params[0].attributeId = 2;
		params[0].data = data;
		DlmsRequest dr = new DlmsRequest();
		dr.setParams(params);
		dr.setOperator("Test");
		dr.setMeterId("000020130708");
		Client.getInstance().sendMsg(dr, DLMS_OP_TYPE.OP_SET);
		byte[] apdu=SetParamNormal.buildSetFrame(20, "0-0:13.0.0.255", 5, data);
	
		BinaryReadWrite brw = new BinaryReadWrite();
		brw.writeBinaryStream(HexDump.toHex(apdu));
		System.out.println(HexDump.toHex(apdu));
		System.out.println(HexDump.hexDump(ByteBuffer.wrap(apdu)));
		System.exit(0);
		
	}
}
