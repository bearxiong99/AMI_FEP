package dcsml.base.applayer;

import dcsml.base.DcsmlSequence;
import dcsml.base.DcsmlTagAdjunct;
import dcsml.base.DcsmlType;
import dcsml.base.SmlSignature;

public class DcsmlCloseRequest extends DcsmlSequence{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2796108798356627352L;

	
	private SmlSignature globalSignature  = new SmlSignature();

	
	public DcsmlCloseRequest(){
		
		members = new DcsmlType[]{globalSignature};
		globalSignature.setOptional(true);
		adjunct = DcsmlTagAdjunct.contextSpecificExplicit(0x00000200);
		memberSize =1;
	}
	
	public DcsmlCloseRequest(byte[] signature){
		this();
		this.setSignature(signature);
	}
	
	public void setSignature(byte[] signature){
		globalSignature.setValue(signature);

	}
	
	
	
}
