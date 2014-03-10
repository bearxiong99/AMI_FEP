package frame.tariff.decode;

import java.io.IOException;

import cn.hexing.fk.utils.HexDump;

import com.hx.dlms.ASN1Type;
import com.hx.dlms.DecodeStream;
import com.hx.dlms.DlmsData;
import com.hx.dlms.DlmsDataType;
import com.hx.dlms.applayer.get.GetResponse;
import com.hx.dlms.applayer.get.GetResponseNormal;

/**
 * 	array[max=4]
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
 * C40100000102020809067765656B6C6C1101110111041101110111021102020809067765656B6B6B1102110211041103110111011101
 */
public class DecodeWeekTable {
	public static void main(String[] args) throws IOException {
		String frame = "C40100000102020809067765656B6C6C1101110111041101110111021102020809067765656B6B6B1102110211041103110111011101";
		GetResponse gr = new GetResponse();
		gr.decode(new DecodeStream(HexDump.toArray(frame)));
	
		GetResponseNormal result= (GetResponseNormal) gr.getDecodedObject();
		DlmsData resutlData=result.getResult().getData();
		DlmsData[] array =resutlData.getArray();
		StringBuilder sb = new StringBuilder();
		for(DlmsData struct:array){
			for(ASN1Type members:struct.getStructure().getMembers()){
				DlmsData member = (DlmsData) members;
				if(member.type() == DlmsDataType.OCTET_STRING){
					sb.append(member.getVisiableString()).append(";");
				}else if(member.type() == DlmsDataType.UNSIGNED){
					sb.append(member.getUnsigned()).append(";");
				}
			}
			sb.setCharAt(sb.length()-1, '@');
		}
		sb.deleteCharAt(sb.length()-1);
		System.out.println(sb);
	}
}
