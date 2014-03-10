package cn.hexing.fas.model.dlms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.hexing.fk.utils.HexDump;

import com.hx.dlms.ASN1Oid;
import com.hx.dlms.DlmsData;
import com.hx.dlms.applayer.set.SetRequest;
import com.hx.dlms.applayer.set.SetRequestFirstBlock;
import com.hx.dlms.applayer.set.SetRequestNextBlock;
import com.hx.dlms.applayer.set.SetRequestNormal;

public class DlmsFrameCreator {
	private static DlmsFrameCreator instance = new DlmsFrameCreator();
	
	private DlmsFrameCreator(){}
	
	public static DlmsFrameCreator getInstance(){return instance;}
	
	public String createRequestNormal(int classId,String strObis,int attrId,DlmsData data) throws IOException{
		SetRequestNormal srn = new SetRequestNormal(0, 	classId, convetObis(strObis),attrId , data);
		SetRequest sr = new SetRequest();
		sr.choose(srn);
		return HexDump.toHex(sr.encode());
	}
	
	public List<String> createRequestNormal(int classId,String strObis,int attrId,DlmsData data,int maxApdu)throws IOException{
		
		List<String> message = new ArrayList<String>();
		byte[] paramData = data.encode();
		if(paramData.length>=maxApdu){

			//SetRequestNormal exceeds max-pdu-size
			int blockNum = 1;
			int offset = 0;
			while( offset< paramData.length ){
				int len = Math.min(maxApdu, paramData.length-offset);
				byte[] blockData = new byte[len];
				for(int j=0; j<len; j++ ){
					blockData[j] = paramData[ offset++ ];
				}
				byte[] apdu = null;
				if( blockNum == 1 ){
					SetRequestFirstBlock first = new SetRequestFirstBlock(0,classId,convetObis(strObis),attrId,blockData);
					SetRequest setReq = new SetRequest();
					setReq.choose(first);
					apdu = setReq.encode();
				}
				else{
					SetRequestNextBlock next = new SetRequestNextBlock(0, paramData.length==offset, blockNum,blockData);
					SetRequest setReq = new SetRequest();
					setReq.choose(next);
					apdu = setReq.encode();
				}
				blockNum++;
				message.add(HexDump.toHex(apdu));
			}
		
			
		}else{
			message.add(createRequestNormal(classId,strObis,attrId,data));
		}
		return message;
	}
	
	public String createRequestNormal(DlmsObisItem item) throws IOException{
		return this.createRequestNormal(item.classId, item.obisString, item.attributeId, item.data);
	}
	
	public List<String> createRequestNormal(DlmsObisItem item,int maxApdu) throws IOException{
		return this.createRequestNormal(item.classId, item.obisString, item.attributeId, item.data, maxApdu);
	}

	public static byte[] convetObis(String obis){
		int[] intOids = ASN1Oid.parse(obis);
		if( null == intOids || intOids.length != 6 )
			throw new RuntimeException("Invalid OBIS:"+obis);
		byte[] ret = new byte[6];
		for(int i=0; i<ret.length; i++ )
			ret[i] = (byte) intOids[i];
		return ret;
	}
}
