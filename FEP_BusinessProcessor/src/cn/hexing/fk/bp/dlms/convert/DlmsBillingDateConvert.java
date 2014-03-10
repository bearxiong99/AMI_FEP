package cn.hexing.fk.bp.dlms.convert;

import java.io.IOException;

import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fk.bp.dlms.protocol.DlmsProtocolEncoder;
import cn.hexing.fk.bp.dlms.protocol.IDlmsScaleConvert;
import cn.hexing.fk.utils.HexDump;

import com.hx.dlms.ASN1SequenceOf;
import com.hx.dlms.ASN1Type;
import com.hx.dlms.DecodeStream;
import com.hx.dlms.DlmsData;
import com.hx.dlms.applayer.get.GetResponse;
import com.hx.dlms.applayer.get.GetResponseNormal;
import com.hx.dlms.applayer.set.SetRequest;
import com.hx.dlms.applayer.set.SetRequestNormal;
/**
 * 
 * @author gaoll
 *
 * @time 2012-11-19 上午9:40:44
 *
 * @info 结算日期
 */
public class DlmsBillingDateConvert  extends DlmsAbstractConvert implements IDlmsScaleConvert{

	@Override
	public DlmsData downLinkConvert(DlmsRequest request, DlmsData data,
			DlmsObisItem item) throws IOException {
		
		String strValue=data.getStringValue();
		String[] dateTime=strValue.split(" ");
		DlmsData[] arrays = new DlmsData[1];
			DlmsData[] struts = new DlmsData[2];
			struts[0] = new DlmsData();
			struts[0].setDlmsTime(dateTime[1]);
			struts[1] = new DlmsData();
			struts[1].setDlmsDate(dateTime[0]);
		ASN1SequenceOf structure = new ASN1SequenceOf(struts);
		arrays[0] = new DlmsData();
		arrays[0].setStructure(structure);
		data.setArray(arrays);
		return data;
	}

	@Override
	public String getStringValue(DlmsData[] arrays){
		StringBuilder sb = new StringBuilder();
		for(DlmsData array : arrays){
			ASN1Type[] members=array.getStructure().getMembers();
			for(int i = members.length-1 ; i >=0 ;i--){
				DlmsData member=(DlmsData) members[i];
				sb.append(member.getDateTime()).append(" ");
			}
			sb.setCharAt(sb.length()-1, '#');
		}
		if(sb.length()>0){
			sb.deleteCharAt(sb.length()-1);			
		}
		return sb.toString();
	}
	
	public static void main(String[] args) throws IOException {
		DlmsBillingDateConvert dbdc = new DlmsBillingDateConvert();
		byte[] apdu = HexDump.toArray("C4018100010102020904000000000905FFFFFF01FF");
		GetResponse resp = new GetResponse();
		resp.decode(DecodeStream.wrap(apdu));
		GetResponseNormal grn = (GetResponseNormal) resp.getDecodedObject();
		DlmsData dd =grn.getResult().getData();
		System.out.println(dbdc.getStringValue(dd.getArray()));
		DlmsData value = new DlmsData();
		value.setVisiableString(dbdc.getStringValue(dd.getArray()));
		System.out.println(value.getStringValue());
		
		
		DlmsData data= new DlmsData();
		data.setVisiableString("2012-10-05 00:00:45");
		data=dbdc.downLinkConvert(null, data, null);
		DlmsObisItem[] params = new DlmsObisItem[1];
		params[0 ]= new DlmsObisItem();
		params[0].classId=22;
		params[0].obisString="0.0.15.0.0.255";
		params[0].attributeId=4;
		params[0].data = data;
		byte[] obis = DlmsProtocolEncoder.convertOBIS(params[0].obisString);
		SetRequestNormal setNormal = new SetRequestNormal(0,params[0].classId,obis,params[0].attributeId,params[0].data);
		SetRequest setReq = new SetRequest();
		setReq.choose(setNormal);
		apdu = setReq.encode();
		System.out.println(HexDump.toHex(apdu));
		
	}
}
