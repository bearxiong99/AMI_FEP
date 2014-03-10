package dcsml.base;

import java.io.IOException;

import cn.hexing.fk.utils.HexDump;

public class DcsmlUnsigned64 extends DcsmlUnsigned{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5699835440185567804L;

	
	public DcsmlUnsigned64(){
		super();
		fixedLength(9);
	}
	
	public DcsmlUnsigned64(int initValue){
		this();
		this.setValue(initValue);
	}
	
	
	public static void main(String[] args) throws IOException {
		DcsmlUnsigned64 du = new DcsmlUnsigned64(18);
		System.out.println(HexDump.toHex(du.encode()));
		
	}
	
	
}
