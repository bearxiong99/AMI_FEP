package request.encode;

import java.io.IOException;

import com.hx.dlms.ASN1ObjectFactory;
import com.hx.dlms.ASN1SequenceOf;
import com.hx.dlms.DlmsData;
import com.hx.dlms.applayer.set.SetRequest;
import com.hx.dlms.applayer.set.SetRequestNormal;

import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fk.bp.dlms.protocol.DlmsProtocolEncoder;
import cn.hexing.util.HexDump;

public class TestEncodeChannelTemplate {
	public static void main(String[] args) throws IOException {
		
		DlmsObisItem[] params = new DlmsObisItem[1];
		params[0]= new DlmsObisItem();
		params[0].classId=7;
		params[0].obisString="0.1.24.3.0.255";
		params[0].attributeId=3;
		
		
		DlmsData[] array = new DlmsData[6];
		for(int i = 0 ;i<array.length;i++){
			array[i]= new DlmsData();
		}
			//-------------时间作为第一个元素(固定的)--------------
			DlmsData[] array0= new DlmsData[4];
			array0[0] = new DlmsData();
			array0[0].setUnsignedLong(8);
			array0[1] = new DlmsData();
			array0[1].setOctetString(HexDump.toArray("0000010000FF"));
			array0[2] = new DlmsData();
			array0[2].setDlmsInteger((byte) 2);
			array0[3] = new DlmsData();
			array0[3].setUnsignedLong(0);
			ASN1SequenceOf struct0 = new ASN1SequenceOf(array0);
			
			array[0].setStructure(struct0 );
			//-------------时间作为第一个元素--------------
			
//---------------------------------------------------------
//			DlmsData[] array1= new DlmsData[4];
//			array1[0] = new DlmsData();
//			array1[0].setUnsignedLong(1);   //--classId
//			array1[1] = new DlmsData();
//			array1[1].setOctetString(HexDump.toArray("0000600A02FF"));//--obisString
//			array1[2] = new DlmsData();
//			array1[2].setDlmsInteger((byte) 2); //attrId
//			array1[3] = new DlmsData();
//			array1[3].setUnsignedLong(0);
//			ASN1SequenceOf struct1 = new ASN1SequenceOf(array1);
//			array[1].setStructure(struct1 );
//---------------------------------------------------------			
			
//---------------------------------------------------------				
			DlmsData[] array2= new DlmsData[4];
			array2[0] = new DlmsData();
			array2[0].setUnsignedLong(3);
			array2[1] = new DlmsData();
			array2[1].setOctetString(HexDump.toArray("0100010800FF"));
			array2[2] = new DlmsData();
			array2[2].setDlmsInteger((byte) 2);
			array2[3] = new DlmsData();
			array2[3].setUnsignedLong(0);
			ASN1SequenceOf struct2 = new ASN1SequenceOf(array2);
			
			array[1].setStructure(struct2 );
//---------------------------------------------------------		
			
			DlmsData[] array3= new DlmsData[4];
			array3[0] = new DlmsData();
			array3[0].setUnsignedLong(3);
			array3[1] = new DlmsData();
			array3[1].setOctetString(HexDump.toArray("0100010801FF"));
			array3[2] = new DlmsData();
			array3[2].setDlmsInteger((byte) 2);
			array3[3] = new DlmsData();
			array3[3].setUnsignedLong(0);
			ASN1SequenceOf struct3 = new ASN1SequenceOf(array3);
			
			array[2].setStructure(struct3 );
//---------------------------------------------------------				
			DlmsData[] array4= new DlmsData[4];
			array4[0] = new DlmsData();
			array4[0].setUnsignedLong(3);
			array4[1] = new DlmsData();
			array4[1].setOctetString(HexDump.toArray("0100010802FF"));
			array4[2] = new DlmsData();
			array4[2].setDlmsInteger((byte) 2);
			array4[3] = new DlmsData();
			array4[3].setUnsignedLong(0);
			ASN1SequenceOf struct4 = new ASN1SequenceOf(array4);
			
			array[3].setStructure(struct4 );
//---------------------------------------------------------			
			DlmsData[] array5= new DlmsData[4];
			array5[0] = new DlmsData();
			array5[0].setUnsignedLong(3);
			array5[1] = new DlmsData();
			array5[1].setOctetString(HexDump.toArray("0100010803FF"));
			array5[2] = new DlmsData();
			array5[2].setDlmsInteger((byte) 2);
			array5[3] = new DlmsData();
			array5[3].setUnsignedLong(0);
			ASN1SequenceOf struct5 = new ASN1SequenceOf(array5);
			
			array[4].setStructure(struct5 );

//---------------------------------------------------------
			DlmsData[] array6= new DlmsData[4];
			array6[0] = new DlmsData();
			array6[0].setUnsignedLong(3);
			array6[1] = new DlmsData();
			array6[1].setOctetString(HexDump.toArray("0100010804FF"));
			array6[2] = new DlmsData();
			array6[2].setDlmsInteger((byte) 2);
			array6[3] = new DlmsData();
			array6[3].setUnsignedLong(0);
			ASN1SequenceOf struct6 = new ASN1SequenceOf(array6);
			
			array[5].setStructure(struct6 );
//---------------------------------------------------------			
		params[0].data.setArray(array );
			
		
		DlmsRequest dr = new DlmsRequest();
		dr.setParams(params);
		//....设置其他的
		
		
		
		
		
		
		
		byte[] obis = DlmsProtocolEncoder.convertOBIS(params[0].obisString);
		int clsId = params[0].classId;
		int attId = params[0].attributeId;	
		SetRequestNormal setNormal = new SetRequestNormal(0,clsId,obis,attId,params[0].data);
		SetRequest setReq = new SetRequest();
		setReq.choose(setNormal);

		byte[] apdu = setReq.encode();
		System.out.println(HexDump.toHex(apdu));
	
			
		
	}
}
