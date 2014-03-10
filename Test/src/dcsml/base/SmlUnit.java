package dcsml.base;

import java.math.BigInteger;

public class SmlUnit extends DcsmlUnsigned8{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3197712290851148567L;
	
	
	public String toString(){
		return ""+new BigInteger(this.getValue());
	}
}
