package frame.tariff.encode;

import java.io.IOException;

import msg.send.Client;

import cn.hexing.fas.model.dlms.DlmsFrameCreator;
import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fas.model.dlms.DlmsRequest.DLMS_OP_TYPE;
import cn.hexing.util.HexDump;

import com.hx.dlms.ASN1SequenceOf;
import com.hx.dlms.DlmsData;

import frame.set.SetParamNormal;
/**
 	array[max=4]
	week structure[8]
	{
	   week_profile_name; octet-string[6],
	   Monday:  day_id unsigned,
	   tuesday:  day_id unsigned,
	   wednesday:  day_id unsigned,
	   thursday:  day_id unsigned,
	   friday:  day_id unsigned,
	   saturday:  day_id unsigned,
	   sunday:  day_id unsigned,
	}
 * @author Administrator
 *C10100001400000D0000FF0400
 *01 01
 * 	 0208 
 *   09067765656B2020 
 *   1101
 *   1102
 *   1103
 *   1104
 *   1103
 *   1102
 *   1101
 */
public class SetWeekTable {
	public static void main(String[] args) throws IOException {
		DlmsData data = new DlmsData();
		DlmsData[] weeks = new DlmsData[25];
		for(int j=0;j<weeks.length;j++){
			
	
			DlmsData[] week = new DlmsData[8];
			for(int i=0;i<8;i++){
				week[i]=new DlmsData();
			}
			week[0].setOctetString(("week0"+j).getBytes()); //week profile name 长度为6，不足6位，以空格补足
			week[1].setUnsigned(4);//day_id
			week[2].setUnsigned(5);//day_id
			week[3].setUnsigned(2);//day_id
			week[4].setUnsigned(1);//day_id
			week[5].setUnsigned(2);//day_id
			week[6].setUnsigned(4);//day_id
			week[7].setUnsigned(1);//day_id
			weeks[j] = new DlmsData();
			ASN1SequenceOf struct=new ASN1SequenceOf(week);
			weeks[j].setStructure(struct);
		}
		data.setArray(weeks);
		
		DlmsObisItem[] params = new DlmsObisItem[1];
		params[0] = new DlmsObisItem();
		params[0].classId = 20;
		params[0].obisString = "0.0.13.0.0.255";
		params[0].attributeId = 8;
		params[0].data = data;
		
		DlmsFrameCreator.getInstance().createRequestNormal(20, "0.0.13.0.0.255", 8, data, 200);
		
		DlmsRequest dr = new DlmsRequest();
		dr.setParams(params);
		dr.setMeterId("000020130708");
		dr.setOperator("Tariff");
		Client.getInstance().sendMsg(dr, DLMS_OP_TYPE.OP_SET);
		byte[] apdu = SetParamNormal.buildSetFrame(params[0]);
		System.out.println(HexDump.toHex(apdu));
		System.exit(0);
	}
}
