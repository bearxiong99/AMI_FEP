package dcsml.base;

import java.math.BigInteger;

public class SmlProfObjPeriodEntry extends DcsmlSequence{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5618252599283750883L;

	
	private DcsmlTime valTime =  new DcsmlTime();
	
	private DcsmlUnsigned64 status = new DcsmlUnsigned64();
	
	private ListOfSmlValueEntry valueList = new ListOfSmlValueEntry();
	
	private SmlSignature periodSignature  = new SmlSignature(); //optional
	
	public SmlProfObjPeriodEntry(){
		members = new DcsmlType[]{valTime,status,valueList};  //,periodSignature Î´Ìí¼Ó
		periodSignature.setOptional(true);
	}
	

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("\n\t\t\tvalTime:"+valTime);
		sb.append("\n\t\t\tstatus:"+new BigInteger(status.getValue()));
		sb.append("\n\t\t\tvalueList:"+valueList.toString());
		return sb.toString();
	}
	
	
	
}
