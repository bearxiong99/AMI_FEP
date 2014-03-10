package dcsml.base;

import cn.hexing.fk.utils.HexDump;

public class DcsmlCrc16 extends DcsmlUnsigned16{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2664603157314356538L;
	public DcsmlCrc16(){
		super();
		fixedLength(3);
	}
	
	public DcsmlCrc16(int initValue){
		this();
		this.setValue(initValue);
	}
	
	@Override
	public String toString(){
		if(value==null || value.length<=0)  return null; 
		
		return HexDump.toHex(value);
	}
	
	
}
