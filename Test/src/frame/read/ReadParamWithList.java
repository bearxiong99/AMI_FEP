package frame.read;

import java.io.IOException;

import com.hx.dlms.applayer.CosemAttributeDescriptorSelection;
import com.hx.dlms.applayer.get.GetRequest;
import com.hx.dlms.applayer.get.GetRequestWithList;

import convert.ObisConvert;

import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fk.bp.dlms.protocol.DlmsProtocolDecoder;
import cn.hexing.fk.bp.dlms.protocol.DlmsProtocolEncoder;
import cn.hexing.util.HexDump;

public class ReadParamWithList {
	public static void main(String[] args) throws IOException {
		DlmsRequest dr = new DlmsRequest();
		DlmsObisItem[] params = new DlmsObisItem[2];
		params[0]= new DlmsObisItem();
		params[1]= new DlmsObisItem();
		params[0].classId = 2;
		params[0].obisString = "1-0:1.8.15.255";
		params[0].attributeId=3;
		params[1].classId = 7;
		params[1].obisString = "1-0:1.8.15.255";
		params[1].attributeId=3;
		GetRequestWithList getWithList = new GetRequestWithList();
		getWithList.setInvokeId(0);
		CosemAttributeDescriptorSelection[] attrs = new CosemAttributeDescriptorSelection[params.length];
		for( int i=0; i<params.length; i++ ){
			attrs[i] = new CosemAttributeDescriptorSelection(params[i].classId,DlmsProtocolEncoder.convertOBIS(params[i].obisString),params[i].attributeId);
			if( params[i].accessSelector != -1 ){
				attrs[i].setSelector(params[i].accessSelector, params[i].data);
			}
		}
		getWithList.setAttributeList(attrs);
		GetRequest getReq = new GetRequest(getWithList);
		byte[] apdu = getReq.encode();
		System.out.println(HexDump.toHex(apdu));
		
	}
}
