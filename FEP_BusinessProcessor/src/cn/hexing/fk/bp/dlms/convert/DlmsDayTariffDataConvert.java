package cn.hexing.fk.bp.dlms.convert;

import java.io.IOException;
import java.nio.ByteBuffer;

import cn.hexing.fk.bp.dlms.protocol.IDlmsScaleConvert;
import cn.hexing.fk.utils.HexDump;

import com.hx.dlms.ASN1SequenceOf;
import com.hx.dlms.ASN1Type;
import com.hx.dlms.DecodeStream;
import com.hx.dlms.DlmsData;
import com.hx.dlms.DlmsDataType;
import com.hx.dlms.DlmsDateTime;

/**
 *	day tariff data convert
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
	</br>
   example :
    		1;01:00:255,00000A0064FF,1!01:00:255,00000A0064FF,1@
   			2,04:00:255,00000A0064FF,2@
   			3,08:00:255,00000A0064FF,3@
   			4,14:00:255,00000A0064FF,4
 * @author gaoll
 *
 */
public class DlmsDayTariffDataConvert extends DlmsAbstractConvert implements IDlmsScaleConvert {


	
	public static void main(String[] args) throws IOException {
		ByteBuffer buffer = ByteBuffer.wrap(HexDump.toArray("01"));
		DlmsData resultData = new DlmsData();
		resultData.decode(new DecodeStream(buffer));
		DlmsData[] results=resultData.getArray();
		DlmsDayTariffDataConvert dtdc = new DlmsDayTariffDataConvert();
		System.out.println(dtdc.getStringValue(results));
	}
	@Override
	public String getStringValue(DlmsData[] results) {
		StringBuilder sb = new StringBuilder();
		for(DlmsData dd: results){
			ASN1SequenceOf asnStruct = dd.getStructure();
			ASN1Type[] members = asnStruct.getMembers();
			for(ASN1Type asn:members){
				DlmsData member = (DlmsData)asn;
				if(member.type() == DlmsDataType.ARRAY){
					DlmsData[] arrays=member.getArray();
					for(DlmsData d : arrays){
						if(d.type()==DlmsDataType.STRUCTURE){
							DlmsDateTime ddt = new DlmsDateTime();
							ASN1SequenceOf asnStruct1 = d.getStructure();
							ASN1Type[] members1 = asnStruct1.getMembers();
							ddt.setDlmsDataValue(((DlmsData)members1[0]).getValue(), 1);
							sb.append(ddt).append(",").append(HexDump.toHex(((DlmsData)members1[1]).getOctetString())).append(",")
							.append(((DlmsData)members1[2]).getUnsignedLong());
							sb.append("!");
						}
					}
					sb.deleteCharAt(sb.length()-1);
				}else{
					sb.append(member.getUnsigned()).append(";");
				}
			}
			sb.append("@");
		}
		sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}

}
