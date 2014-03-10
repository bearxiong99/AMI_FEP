package dcsml.base;

import java.io.IOException;

import com.hx.dlms.DecodeStream;

public class SmlValue extends DcsmlType{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4074441722776348014L;
	
	public SmlValue(){
		super(TAG_ANY);
		forceEncodeTag = true;
	}

	@Override
	public boolean decode(DecodeStream input) throws IOException {
		super.decodeTL(input);
		
		value = new byte[length];
		input.read(value);
		return true;
	}

	
	
	

}
