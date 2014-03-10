package cn.hexing.explain.decoder;

import java.io.IOException;
import java.nio.ByteBuffer;

import cn.hexing.fk.utils.HexDump;

import com.hx.dlms.ASN1Enum;
import com.hx.dlms.ASN1SequenceOf;
import com.hx.dlms.ASN1Type;
import com.hx.dlms.DecodeStream;
import com.hx.dlms.DlmsData;
import com.hx.dlms.applayer.DataAccessResult;
import com.hx.dlms.applayer.get.GetDataResult;
import com.hx.dlms.applayer.get.GetResponse;
import com.hx.dlms.applayer.get.GetResponseNormal;
import com.hx.dlms.applayer.get.GetResponseWithBlock;
import com.hx.dlms.applayer.get.GetResponseWithList;

public class DlmsDecoder {
	
	private static DlmsDecoder instance = new DlmsDecoder();
	
	private DlmsDecoder(){}
	
	public static DlmsDecoder getInstance(){
		return instance;
	}
	
	public static void main(String[] args) throws Exception {
		DlmsDecoder dd = new DlmsDecoder();
		//C401460104
		//C40146001740400000
		//C40181000202090C07DD030DFF0D1921008000011140
		//C4018100010102020904000000000905FFFFFF01FF
		String s = dd.decode("C40181000202090C07DD030DFF0D1921008000011140");
		System.out.println(s);
	}
	
	//Decoder
	
	public String decode(String rawFrame) throws Exception{
		
		ByteBuffer apdu = HexDump.toByteBuffer(rawFrame);
		int applicationTag = apdu.get(0) & 0xFF ;
		String result =null;
		switch(applicationTag){
		case 0xC4:
			result=onGetResponse(rawFrame);
			break;
		case 0xC5:
			result=onSetResponse(rawFrame);
			break;
		case 0xC7:
			result=onActionResponse(rawFrame);
			break;
		case 0xC2:
			break;
		}
		return result;
	}
	
	
	/**
	 * 读返回
	 * @param rawFrame
	 * @return
	 * @throws IOException 
	 */
	private String onGetResponse(String rawFrame) throws IOException {
		GetResponse resp = new GetResponse();
		resp.decode(DecodeStream.wrap(rawFrame));
		ASN1Type selObj = resp.getDecodedObject();
		if( selObj instanceof GetResponseNormal){
			return handleGetResponseNormal(selObj);
		}else if(selObj instanceof GetResponseWithBlock){
			throw new RuntimeException("暂时不支持GetResponseWithBlock");
		}else if(selObj instanceof GetResponseWithList){
			throw new RuntimeException("暂时不支持GetResponseWithList");

		}
		return null;
	}

	private String  handleGetResponseNormal(ASN1Type selObj) {
		GetResponseNormal normal = (GetResponseNormal) selObj;
		GetDataResult dataResult = normal.getResult();
		ASN1Type resultObj = dataResult.getDecodedObject();
		if(resultObj instanceof DlmsData){
			DlmsData data = dataResult.getData();
			int type = data.type();
			switch(type){
			case DlmsData.ARRAY:{
				DlmsData[] arrays = data.getArray();
				StringBuilder sb = new StringBuilder();
				sb.append("type:array,size:"+arrays.length+",value:{");
				for(DlmsData array:arrays){
					sb.append(structToString(array)).append("#");
				}
				sb.deleteCharAt(sb.length()-1);
				sb.append("}");
				return sb.toString();
			}
			case DlmsData.ARRAY_STRUCT:
				break;
			case DlmsData.STRUCTURE:
				StringBuilder sb = structToString(data);
				return sb.toString(); 
			default:
				return dlmsDataToString(data);
			}
		}else{
			ASN1Enum aEnum = (ASN1Enum) resultObj;
			return DataAccessResult.parseResult(aEnum.getEnumValue()).toString();
		}
		return null;
	}

	private StringBuilder structToString(DlmsData data) {
		ASN1SequenceOf struct = data.getStructure();
		ASN1Type[] asn1Members = struct.getMembers();
		StringBuilder sb = new StringBuilder();
		sb.append("type:structure,size:"+asn1Members.length+",value[");
		for(ASN1Type asn1:asn1Members){
			DlmsData memeber = (DlmsData) asn1;
			sb.append(dlmsDataToString(memeber)).append(";");
		}
		sb.deleteCharAt(sb.length()-1);
		sb.append("]");
		return sb;
	}

	private String dlmsDataToString(DlmsData data) {
		return "type:"+data.getTypeName()+",value:"+data.getStringValue();
	}

	/**
	 * 写返回
	 * @param rawFrame
	 * @return
	 */
	private String onSetResponse(String rawFrame) {
		return null;
	}

	/**
	 * 执行返回
	 * @param rawFrame
	 * @return
	 */
	private String onActionResponse(String rawFrame) {
		return null;
	}
	
	
	
	
	
	
	
	
}
