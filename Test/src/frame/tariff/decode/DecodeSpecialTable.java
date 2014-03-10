package frame.tariff.decode;

import java.io.IOException;

import cn.hexing.fk.utils.HexDump;

import com.hx.dlms.ASN1Type;
import com.hx.dlms.DecodeStream;
import com.hx.dlms.DlmsData;
import com.hx.dlms.applayer.get.GetResponse;
import com.hx.dlms.applayer.get.GetResponseNormal;

/**
 *  设置公共假日费率
 *  array[max=100]
	spec_day_entry  structure[3]
	{
	   index;   long-unsigned,
	   special day_date;  octet-string[5],
	   day_id;  unsigned,
	}
	0001000100100014C401000001010203120001090507DC061DFF1101
 * @author Administrator
 *
 */
public class DecodeSpecialTable {
	public static void main(String[] args) throws IOException {
		
		String frame = "C401000001010203120001090507DC061DFF1101";
		GetResponse gr = new GetResponse();
		gr.decode(new DecodeStream(HexDump.toArray(frame)));
	
		StringBuilder sb = new StringBuilder();
		
		GetResponseNormal result= (GetResponseNormal) gr.getDecodedObject();
		DlmsData resutlData=result.getResult().getData();
		DlmsData[] array =resutlData.getArray();
		
		for(DlmsData struct:array){
			ASN1Type[] members = struct.getStructure().getMembers();
			DlmsData index = (DlmsData) members[0];
			DlmsData specialDayDate = (DlmsData) members[1];
			DlmsData dayId = (DlmsData)members[2];
			sb.append(index.getUnsignedLong()).append(";")
			  .append(specialDayDate.getDateTime()).append(";")
			  .append(dayId.getUnsigned()).append("@");
		}
		sb.deleteCharAt(sb.length()-1);
		System.out.println(sb);
		
		
		
	}
}
