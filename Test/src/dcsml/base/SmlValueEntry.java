package dcsml.base;

public class SmlValueEntry extends DcsmlSequence{

	/**
	 * 
	 */
	private static final long serialVersionUID = -896404186420301081L;
	
	private SmlValue  smlValue = new SmlValue();
	
	private SmlSignature signature = new SmlSignature();
	
	public SmlValueEntry(){
		members = new DcsmlType[]{smlValue,signature};
		memberSize=2;
	}
	
}
