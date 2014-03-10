package cn.hexing.fk.bp.dlms.convert;

import java.io.IOException;

import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fk.bp.dlms.protocol.IDlmsScaleConvert;
import cn.hexing.fk.utils.HexDump;

import com.hx.dlms.ASN1Type;
import com.hx.dlms.DecodeStream;
import com.hx.dlms.DlmsData;
import com.hx.dlms.applayer.get.GetResponse;
import com.hx.dlms.applayer.get.GetResponseNormal;
/**
 * array[max=4]
	season structure[3]
	{
	   season_profile_name; octet-string[6],
	   season_start:  octet-string[12],
	   week_name: octet-string[6], 
	}
   @example s1    ;06-01;weekll@s1    ;06-01;weekll
 * @author gll
 *
 */
public class DlmsSeasonTariffDataConvert extends DlmsAbstractConvert implements IDlmsScaleConvert{

	@Override
	public String getStringValue(DlmsData[] results){
		StringBuilder sb = new StringBuilder();
		for(DlmsData struct:results){
			ASN1Type[] members = struct.getStructure().getMembers();
			DlmsData index = (DlmsData) members[0];
			DlmsData specialDayDate = (DlmsData) members[1];
			DlmsData dayId = (DlmsData)members[2];
			sb.append(index.getVisiableString()).append(";")
			  .append(specialDayDate.getDateTime()).append(";")
			  .append(dayId.getVisiableString()).append("@");
		}
		sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}
	
	public static void main(String[] args) throws IOException {
		String frame = "C4010000010102030906733120202020090CFFFF0601FFFFFFFFFF80000009067765656B6C6C";
		GetResponse gr = new GetResponse();
		gr.decode(new DecodeStream(HexDump.toArray(frame)));
	
		
		GetResponseNormal result= (GetResponseNormal) gr.getDecodedObject();
		DlmsData resutlData=result.getResult().getData();
		DlmsSeasonTariffDataConvert dstdc = new DlmsSeasonTariffDataConvert();
		dstdc.upLinkConvert(new DlmsRequest(), resutlData, null);
	}

}
