package frame.tariff.encode;

import java.io.IOException;

import msg.send.Client;

import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fas.model.dlms.DlmsRequest.DLMS_OP_TYPE;
import cn.hexing.util.HexDump;

import com.hx.dlms.ASN1SequenceOf;
import com.hx.dlms.DlmsData;

import frame.set.SetParamNormal;

/**
 *  array[max=4]
	season structure[3]
	{
	   season_profile_name; octet-string[6],
	   season_start:  octet-string[12],
	   week_name: octet-string[6], 
	}
 * @author Administrator
 *C10100000B00000B0000FF0200010102030906736561736F6E090C07DC081B011314393480000009047765656B
 *
  01 01
 	0203
 		0906736561736F6E
	 	090C07DC081B0113143934800000
	 	09047765656B
 */
public class SetSeasonTable {
	public static void main(String[] args) throws IOException {
		DlmsData data = new DlmsData();
			DlmsData[] seasons = new DlmsData[0];
			for(int i=0;i<seasons.length;i++){
				DlmsData[] season = new DlmsData[3];
				season[0] = new DlmsData();
				season[0].setOctetString(("seas"+i+"n").getBytes());  //总长度为6，不足6位，以空格补足
				season[1] = new DlmsData();
				season[1].setDlmsDateTime("2012-11-00 FF:FF:FF"); //时间
				season[2] = new DlmsData();
				season[2].setOctetString(("week0"+i+" ").getBytes()); //总长度为6，不足6位，以空格补足 
			seasons[i] = new DlmsData();
			ASN1SequenceOf struct = new ASN1SequenceOf(season);
			seasons[i].setStructure(struct );
			}
				
		data.setArray(seasons);
		DlmsObisItem[] params = new DlmsObisItem[1];
		params[0] = new DlmsObisItem();
		params[0].classId = 20;
		params[0].obisString = "0.0.13.0.0.255";
		params[0].attributeId = 7;
		params[0].data = data;
		
		DlmsRequest dr = new DlmsRequest();
		dr.setParams(params);
		dr.setMeterId("000020120716");
		Client.getInstance().sendMsg(dr, DLMS_OP_TYPE.OP_SET);
		
		byte[] apdu = SetParamNormal.buildSetFrame(params[0]);
		BinaryReadWrite brw = new BinaryReadWrite();
		brw.writeBinaryStream(HexDump.toHex(apdu));
		System.out.println(HexDump.toHex(apdu));
		
		System.exit(0);
		
	}
}
