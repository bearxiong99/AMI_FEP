package frame.set;

import java.io.IOException;

import com.hx.dlms.DlmsData;
import com.hx.dlms.applayer.set.SetRequest;
import com.hx.dlms.applayer.set.SetRequestNormal;

import convert.ObisConvert;

import cn.hexing.fas.model.dlms.DlmsFrameCreator;
import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.util.HexDump;

public class SetParamNormal {
	
	public static void main(String[] args) throws IOException {
		DlmsData data = new DlmsData();
		data.setDlmsDateTime("2013-11-30 23:58:00");
//		data.setOctetString("abcd".getBytes());
		byte[] apdu = buildSetFrame(8, "0.0.1.0.0.255", 2, data );
		System.out.println(DlmsFrameCreator.getInstance().createRequestNormal(8, "0.0.1.0.0.255", 2, data));
		
		System.out.println(HexDump.toHex(apdu));
		
	}
	
	
	public static byte[] buildSetFrame(int classId,String strObis,int attrId,DlmsData data) throws IOException{

		SetRequestNormal srn = new SetRequestNormal(0, 	classId, ObisConvert.convetObis(strObis),attrId , data);
		SetRequest sr = new SetRequest();
		sr.choose(srn);
		return sr.encode();
	}
	public static byte[] buildSetFrame(DlmsObisItem param) throws IOException{

		SetRequestNormal srn = new SetRequestNormal(0, 	param.classId, ObisConvert.convetObis(param.obisString),param.attributeId , param.data);
		SetRequest sr = new SetRequest();
		sr.choose(srn);
		return sr.encode();
	}	
}
