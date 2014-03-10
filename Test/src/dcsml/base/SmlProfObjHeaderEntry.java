package dcsml.base;

import java.math.BigInteger;

public class SmlProfObjHeaderEntry extends DcsmlSequence{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5618252599283750883L;

	private DcsmlOctetString objName = new DcsmlOctetString();
	
	private SmlUnit unit = new SmlUnit();
	
	private DcsmlIneteger8 scaler = new DcsmlIneteger8();
	
	public SmlProfObjHeaderEntry(){
		members = new DcsmlType[]{objName,unit,scaler};
		memberSize = 3;
	}
	
	@Override
	public String toString(){

		StringBuilder sb = new StringBuilder();
		sb.append("\n\t\t\t objName:"+new String(objName.getValue()));
		sb.append("\n\t\t\t unit:"+unit.toString());
		sb.append("\n\t\t\t scaler:"+new BigInteger(scaler.getValue()));
		return sb.toString();
	}
	
	
	
}
