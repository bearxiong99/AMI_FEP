package frame.tariff.decode;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;

import com.hx.dlms.ASN1SequenceOf;
import com.hx.dlms.ASN1Type;
import com.hx.dlms.DecodeStream;
import com.hx.dlms.DlmsData;
import com.hx.dlms.DlmsDataType;
import com.hx.dlms.DlmsDateTime;

import cn.hexing.fk.utils.HexDump;
/**
 *	dayId:time1,logicName,1;time2,logicName,2
 *	dayId:time1,logicName,1;time2,logicName,2
 * @author Administrator
 * 
 *C40281FF00000002 0052020211020104020309040000FFFF090600000A0064FF120003020309040600FFFF090600000A0064FF120001020309041200FFFF090600000A0064FF120002020309041600FFFF090600000A0064FF120003 
 *
 */
public class DecodeDayTable {
	public static void main(String[] args) throws IOException {
		ByteBuffer buffer = ByteBuffer.wrap(HexDump.toArray("0104020211010102020309040100FFFF090600000A0064FF120001020309040100FFFF090600000A0064FF120001020211020101020309040400FFFF090600000A0064FF120002020211030101020309040800FFFF090600000A0064FF120003020211040101020309040E00FFFF090600000A0064FF120004"));
		
		
		DlmsDateTime ddt = new DlmsDateTime();
		
		DlmsData resultData = new DlmsData();
		resultData.decode(new DecodeStream(buffer));
		StringBuilder sb = new StringBuilder();
		DlmsData[] results=resultData.getArray();
		for(DlmsData dd: results){
			ASN1SequenceOf asnStruct = dd.getStructure();
			ASN1Type[] members = asnStruct.getMembers();
			for(ASN1Type asn:members){
				DlmsData member = (DlmsData)asn;
				if(member.type() == DlmsDataType.ARRAY){
					DlmsData[] arrays=member.getArray();
					for(DlmsData d : arrays){
						if(d.type()==DlmsDataType.STRUCTURE){
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
		System.out.println(sb);
	}
}
