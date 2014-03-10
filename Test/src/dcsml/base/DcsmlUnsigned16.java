package dcsml.base;

import java.io.IOException;

import cn.hexing.fk.utils.HexDump;

public class DcsmlUnsigned16 extends DcsmlUnsigned{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5699835440185567804L;

	
	public DcsmlUnsigned16(){
		super();
		fixedLength(3);
	}
	
	public DcsmlUnsigned16(int initValue){
		this();
		this.setValue(initValue);
	}
	
	
	public static void main(String[] args) throws IOException {
		DcsmlUnsigned16 du = new DcsmlUnsigned16(18);
		System.out.println(HexDump.toHex(du.encode()));
	}
	
	
}
