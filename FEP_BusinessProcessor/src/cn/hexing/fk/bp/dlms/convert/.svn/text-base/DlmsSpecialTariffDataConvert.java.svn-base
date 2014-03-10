package cn.hexing.fk.bp.dlms.convert;

import java.io.IOException;

import cn.hexing.fk.bp.dlms.protocol.IDlmsScaleConvert;
import cn.hexing.fk.utils.HexDump;

import com.hx.dlms.ASN1Type;
import com.hx.dlms.DecodeStream;
import com.hx.dlms.DlmsData;
import com.hx.dlms.applayer.get.GetResponse;
import com.hx.dlms.applayer.get.GetResponseNormal;

/**
 * array[max=100]
	spec_day_entry  structure[3]
	{
	   index;   long-unsigned,
	   special day_date;  octet-string[5],
	   day_id;  unsigned,
	}
   @example 1;2012-06-29;1@1;2012-06-29;1
 * @author gll
 *
 */
public class DlmsSpecialTariffDataConvert extends DlmsAbstractConvert implements IDlmsScaleConvert{

	public static void main(String[] args) throws IOException {

		String frame = "C401000001010203120001090507DC061DFF1101";
		GetResponse gr = new GetResponse();
		gr.decode(new DecodeStream(HexDump.toArray(frame)));
	
		
		GetResponseNormal result= (GetResponseNormal) gr.getDecodedObject();
		DlmsData resutlData=result.getResult().getData();
		DlmsData[] array =resutlData.getArray();
		DlmsSpecialTariffDataConvert dstdc = new DlmsSpecialTariffDataConvert();
		System.out.println(dstdc.getStringValue(array));
		
	}
	@Override
	public String getStringValue(DlmsData[] results){
		StringBuilder sb = new StringBuilder();
		for(DlmsData struct:results){
			ASN1Type[] members = struct.getStructure().getMembers();
			DlmsData index = (DlmsData) members[0];
			DlmsData specialDayDate = (DlmsData) members[1];
			DlmsData dayId = (DlmsData)members[2];
			sb.append(index.getUnsignedLong()).append(";")
			  .append(specialDayDate.getDateTime()).append(";")
			  .append(dayId.getUnsigned()).append("@");
		}
		if(sb.length()!=0){
			sb.deleteCharAt(sb.length()-1);
		}
		return sb.toString();
	}
	
}
