package frame.tariff.encode;

import java.io.IOException;

import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.util.HexDump;

import com.hx.dlms.ASN1SequenceOf;
import com.hx.dlms.DlmsData;

import frame.set.SetParamNormal;

/**
 * 设置公共假日费率
 *  array[max=100]
	spec_day_entry  structure[3]
	{
	   index;   long-unsigned,
	   special day_date;  octet-string[5],
	   day_id;  unsigned,
	}
 * @author Administrator
 *
 */
public class SetSpecialTable {
	
	public static void main(String[] args) throws IOException {
		
		
		DlmsData data = new DlmsData();
		DlmsData[] arrays = new DlmsData[1];
			arrays[0] = new DlmsData();
			DlmsData[] array2 = new DlmsData[3];
			for(int i=0;i<3;i++){
				array2[i] = new DlmsData();
			}
			array2[0].setUnsignedLong(1);		//index
			array2[1].setDlmsDate("FFFF-06-21");	//day_date
			array2[2].setUnsigned(1);			//day_id
		ASN1SequenceOf struct = new ASN1SequenceOf(array2);
		arrays[0].setStructure(struct);
		data.setArray(arrays);
		
		DlmsObisItem[] params = new DlmsObisItem[1];
		params[0] = new DlmsObisItem();
		params[0].classId = 11;
		params[0].obisString = "0-0:11.0.0.255";
		params[0].attributeId = 2;
		params[0].data = data;
		
		byte[] apdu = SetParamNormal.buildSetFrame(params[0]);
		System.out.println(HexDump.toHex(apdu));
	}
}
