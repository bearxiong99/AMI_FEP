package frame;

import java.io.IOException;
import java.nio.ByteBuffer;

import cn.hexing.fk.utils.HexDump;

import com.hx.dlms.ASN1Type;
import com.hx.dlms.DecodeStream;
import com.hx.dlms.DlmsData;
import com.hx.dlms.applayer.get.GetResponse;
import com.hx.dlms.applayer.get.GetResponseNormal;

public class DecodeResponse {
	public static void main(String[] args) throws IOException {
		String frame = "01090202090C056F0514FF0F23360080000111FF0202090C056F051BFF120E3B00800001110F0202090C056F051BFF120F2F00800001110F0202090C056F051BFF12111400800001110F0202090C056F051BFF12113A00800001110F0202090C056F051BFF1218240080000111070202090C056F051BFF1232310080000111070202090C056F051BFF1332140080000111040202090C056F060FFF0F0D25008000011105";
		System.out.println(getStringValue(frame,true));
	}
	
	public static String getStringValue(String frame,boolean isBlock) throws IOException{
		
		DlmsData resultData=null;
		if(isBlock){
			ByteBuffer buffer = ByteBuffer.wrap(HexDump.toArray(frame));
			resultData = new DlmsData();
			resultData.decode(new DecodeStream(buffer));
		}else{
			GetResponse gr = new GetResponse();
			gr.decode(new DecodeStream(HexDump.toArray(frame)));
			GetResponseNormal result= (GetResponseNormal) gr.getDecodedObject();
			resultData=result.getResult().getData();
		}
		
		return decodeData(resultData);
	}

	private static String decodeData(DlmsData resutlData) {
		
		if(resutlData.type()!=1 && resutlData.type()!=2){
			return resutlData.getStringValue();
		}else if(resutlData.type()==1){
			DlmsData[] arrays = resutlData.getArray();
			StringBuilder sb = new StringBuilder();
			for(DlmsData data:arrays){
				sb.append(decodeData(data));
			}
			sb.deleteCharAt(sb.length()-1);
			return sb.toString();
		}else if(resutlData.type()==2){
			StringBuilder sb = new StringBuilder();
			ASN1Type[] members=resutlData.getStructure().getMembers();
	
			for(ASN1Type member:members){
				DlmsData data = (DlmsData) member;
				sb.append(decodeData(data)).append(",");
			}
			sb.deleteCharAt(sb.length()-1);
			sb.append("\n");
			return sb.toString();
		}
		return null;
	}
}
