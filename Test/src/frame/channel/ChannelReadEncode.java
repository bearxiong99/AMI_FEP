package frame.channel;

import java.io.IOException;
import java.text.ParseException;

import msg.send.Client;
import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fk.bp.dlms.protocol.DlmsProtocolEncoder;
import cn.hexing.util.HexDump;

import com.hx.dlms.ASN1SequenceOf;
import com.hx.dlms.ASN1Type;
import com.hx.dlms.DlmsData;
import com.hx.dlms.DlmsDateTime;
import com.hx.dlms.applayer.get.GetRequest;
import com.hx.dlms.applayer.get.GetRequestNormal;
/**
 * 组读通道的帧
 * @author Administrator
 *
 */
public class ChannelReadEncode {
	public static void main(String[] args) throws IOException, ParseException {
		
		String startTime="1392-11-30 17:10:00";
		String endTime = "1392-11-30 17:15:00";	
		DlmsObisItem[] params = buildDlmsParam("1",startTime,endTime);
		
		DlmsRequest dlmsRequest = new DlmsRequest();
		dlmsRequest = new DlmsRequest();
		dlmsRequest.setMeterId("000000010005");
		dlmsRequest.setProtocol("03");
		dlmsRequest.setOpType(DlmsRequest.DLMS_OP_TYPE.OP_GET);
		dlmsRequest.setOperator("MasterTask");
		dlmsRequest.setParams(params);
		dlmsRequest.addAppendParam("taskDate", "2012-11-11 00:00:00");
		dlmsRequest.setSubprotocol("101");
		dlmsRequest.addAppendParam("taskNo", "111");
//		DlmsData dd = dlmsRequest.getParams()[0].data;
//		ASN1SequenceOf struct = dd.getStructure();
//		DlmsData[] members = (DlmsData[]) struct.getMembers();
//		members[1].changeToOldTime();
//		members[2].changeToOldTime();
//		struct = new ASN1SequenceOf(members);
//		params[0].data.setStructure(struct);
		
		Client.getInstance().sendMsg(dlmsRequest);
		ASN1SequenceOf struct = params[0].data.getStructure();

		DlmsData[] members = (DlmsData[]) struct.getMembers();
		members[1].changeToOldTime();
		members[2].changeToOldTime();
		members[3] = new DlmsData();
		struct = new ASN1SequenceOf(members);
		struct.isEncodeLength =false;

		params[0].data.setStructure(struct);
		byte[] apdu = encode(params);
		System.out.println(HexDump.toHex(apdu));
		System.exit(0);
	}

	private static byte[] encode(DlmsObisItem[] params) throws IOException {
		byte[] obis = DlmsProtocolEncoder.convertOBIS(params[0].obisString);
		GetRequestNormal getNormal = new GetRequestNormal(0,params[0].classId,obis,params[0].attributeId);
		
		if( params[0].accessSelector != -1 ){
//			getNormal.getSelectiveAccess().setParameter(params[0].accessSelector, params[0].data);
			getNormal.getSelectiveAccess().setParameter( params[0].data);

		}
		GetRequest getReq = new GetRequest(getNormal);
		byte[] apdu = getReq.encode();
		return apdu;
	}
	
	public static DlmsObisItem[] buildDlmsParam(String channelNo,String startTime,String endTime) throws ParseException, IOException{
		DlmsObisItem[] params = new DlmsObisItem[1];
		params[0] = new DlmsObisItem();
		params[0].attributeId= 2;
		params[0].classId = 7;
//		params[0].obisString="0.13.24.3.0.255";
		params[0].obisString="0.0.98.6.128.255";
		DlmsData [] array = new DlmsData[4];
		array[0] = new DlmsData();
				DlmsData[] array2= new DlmsData[4];
				array2[0] = new DlmsData();
				array2[0].setUnsignedLong(8);
				array2[1] = new DlmsData();
				array2[1].setOctetString(HexDump.toArray("0000010000FF"));
				array2[2] = new DlmsData();
				array2[2].setDlmsInteger((byte) 2);
				array2[3] = new DlmsData();
				array2[3].setUnsignedLong(0);
		array [1] = new DlmsData();
		array [2] = new DlmsData();
		array [3] = new DlmsData();
		ASN1SequenceOf struct1 = new ASN1SequenceOf(array2);
		array[0].setStructure(struct1);
		array[1].setOldDlmsDateTime(startTime);   //这里设置老的时间格式
		array[2].setOldDlmsDateTime(endTime);	//设置老的时间格式
//		array[1].setDlmsDateTime(startTime);   
//		array[2].setDlmsDateTime(endTime);	
		DlmsData[] dd = null; 
//		array[3].setArray(dd); //对于老表 这里 注释掉。
		ASN1SequenceOf struct = new ASN1SequenceOf(array);
		
		struct.isEncodeLength = false;  //对于老表，这里设置为false

		params[0].data.setStructure(struct );
		params[0].accessSelector = 1;
		return params;
	}
	
}
