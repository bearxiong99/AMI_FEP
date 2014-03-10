package frame.tariff.decode;

import java.io.IOException;

import cn.hexing.fk.utils.HexDump;

import com.hx.dlms.ASN1Type;
import com.hx.dlms.DecodeStream;
import com.hx.dlms.DlmsData;
import com.hx.dlms.applayer.get.GetResponse;
import com.hx.dlms.applayer.get.GetResponseNormal;

/**
 * C4010000010102030906733120202020090CFFFF0601FFFFFFFFFF80000009067765656B6C6C
 * @author Administrator
 *  array[max=4]
	season structure[3]
	{
	   season_profile_name; octet-string[6],
	   season_start:  octet-string[12],
	   week_name: octet-string[6], 
	}
 */
public class DecodeSeasonTable {
	public static void main(String[] args) throws IOException {
		String frame = "C4010000010102030906733120202020090CFFFF0601FFFFFFFFFF80000009067765656B6C6C";
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
			sb.append(index.getVisiableString()).append(";")
			  .append(specialDayDate.getDateTime()).append(";")
			  .append(dayId.getVisiableString()).append("@");
		}
		sb.deleteCharAt(sb.length()-1);
		System.out.println(sb);
		
	}
}
