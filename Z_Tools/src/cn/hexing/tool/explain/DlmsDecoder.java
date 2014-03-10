package cn.hexing.tool.explain;

import java.io.IOException;
import java.nio.ByteBuffer;

import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.MultiProtoRecognizer;
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
import com.hx.dlms.message.DlmsMessage;

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
		//C4010E0001020207090C07DD0418FF110F00008000011200001200001200001200DB1200D41200680207090C07DD0418FF111E00008000011200001200001200001200BA1200BC1200D8
		String s = dd.decode("C4010E0001020207090C07DD0418FF110F00008000011200001200001200001200DB1200D41200680207090C07DD0418FF111E00008000011200001200001200001200BA1200BC1200D8");
		System.out.println(s);
	}
	
	//Decoder
	
	public String decode(String rawFrame) throws Exception{
		
		if(!"00".equals(rawFrame.substring(0, 2))){
			rawFrame ="0001000100010000"+rawFrame;
		}
		
		IMessage msg = MultiProtoRecognizer.recognize(HexDump.toByteBuffer(rawFrame));

		if(msg==null || !(msg instanceof DlmsMessage)){
			throw new RuntimeException("不是DLMS帧");
		}
		
		ByteBuffer apdu = ByteBuffer.allocate(rawFrame.length()/2+8);
		msg.read(HexDump.toByteBuffer(rawFrame));
		apdu=((DlmsMessage)msg).getApdu();
		int applicationTag = apdu.get(0) & 0xFF ;
		String result =null;
		switch(applicationTag){
		case 0xC4:
			result=onGetResponse(HexDump.toHex(apdu.array()));
			break;
		case 0xC5:
			result=onSetResponse(HexDump.toHex(apdu.array()));
			break;
		case 0xC7:
			result=onActionResponse(HexDump.toHex(apdu.array()));
			break;
		case 0xC2:
			break;
		default:
			throw new RuntimeException("不支持解析");
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
			return handleGetResponseWithBlock(selObj);
		}else if(selObj instanceof GetResponseWithList){
			throw new RuntimeException("暂时不支持GetResponseWithList");

		}
		return null;
	}

	private String handleGetResponseWithBlock(ASN1Type selObj) {
		GetResponseWithBlock block = (GetResponseWithBlock) selObj;
		ASN1Type[] members = block.getMembers();
		return null;
	}

	private String  handleGetResponseNormal(ASN1Type selObj) {
		GetResponseNormal normal = (GetResponseNormal) selObj;
		GetDataResult dataResult = normal.getResult();
		ASN1Type resultObj = dataResult.getDecodedObject();
		if(resultObj instanceof DlmsData){
			DlmsData data = dataResult.getData();
			return format(data,0);
		}else{
			ASN1Enum aEnum = (ASN1Enum) resultObj;
			return DataAccessResult.parseResult(aEnum.getEnumValue()).toString();
		}
	}
	
	public String format(DlmsData data,int space){
		StringBuilder sb = new StringBuilder();
		StringBuilder strSpace = new StringBuilder();
		space++;
		for(int i=0;i<space;i++){
			strSpace.append("    ");
		}
		
		switch(data.type()){
		case DlmsData.ARRAY:
			DlmsData[] arrays = data.getArray();
			sb.append("Type:Array,Size:"+arrays.length+",value:");
			for(int i=0;i<arrays.length;i++){
				sb.append("\n").append(strSpace).append(format(arrays[i],space));
			}
			return sb.toString();
		case DlmsData.STRUCTURE:
			ASN1SequenceOf struct = data.getStructure();
			ASN1Type[] asn1Members = struct.getMembers();
			sb.append("Type:Structure,Size:"+asn1Members.length+",value:");
			for(int i=0;i<asn1Members.length;i++){
				DlmsData memeber = (DlmsData)asn1Members[i];
				sb.append("\n").append(strSpace).append(format(memeber,space));
			}
			return sb.toString();
		default:
				return dlmsDataToString(data);
				
		
		}
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
		throw new RuntimeException("暂不支持写返回解析");
	}

	/**
	 * 执行返回
	 * @param rawFrame
	 * @return
	 */
	private String onActionResponse(String rawFrame) {
		throw new RuntimeException("暂不支持执行返回解析");
	}
	
	
	
	
	
	
	
	
}
