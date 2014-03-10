package model;

import com.hx.dlms.DlmsData;

public class ReqDecription {
	public ReqDecription(int classId, String obis, int attrId) {
		this.classId = classId;
		this.obis = obis ;
		this.attrId = attrId;
	}
	/**
	 * classId#obis#attrId
	 * @param obisDesc
	 */
	public ReqDecription(String obisDesc){
		String[] descs=obisDesc.split("#");
		this.classId = Integer.parseInt(descs[0]);
		this.obis = descs[1];
		this.attrId = Integer.parseInt(descs[2]);
	}
	public ReqDecription(){
		super();
	}
	public int classId;
	public String obis;
	public int attrId;
	public String meterId;
	public DlmsData data = new DlmsData();
}
