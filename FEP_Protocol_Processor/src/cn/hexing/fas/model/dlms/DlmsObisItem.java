package cn.hexing.fas.model.dlms;

import java.io.Serializable;

import com.hx.dlms.DlmsData;

public class DlmsObisItem implements Serializable {
	private static final long serialVersionUID = 6985447720581018979L;
	public int classId = -1;
	public String obisString = null;
	public int attributeId = -1;
	
	public String sjx;  //The dataItemCode related to (classId,OBIS,attrId).

	//Command-id is used by Master-station, such as TOKEN download.
	public int cmdId = -1;  //The command-id related to this item object.
	
	//attribute or method access ( get or set ) most case use u32/u16/u8 data as value
	//other type of data can only stored in octet-value
	//u32 as double-long-unsigned of DLMS-DATA type.
	public int accessSelector = -1;	// Support 'selectiveGet'
	public DlmsData data = new DlmsData();
	public int resultCode = -1; //DataAccessResult or ActionResult enum type. SET/ACTION result>=0
	public DlmsData resultData = null;
	
	
}
