package dcsml.base;

import java.io.IOException;

import cn.hexing.fk.utils.HexDump;

public class DcsmlUnsigned8 extends DcsmlUnsigned{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5699835440185567804L;

	
	public DcsmlUnsigned8(){
		super();
		fixedLength(2);
	}
	
	public DcsmlUnsigned8(int initValue){
		this();
		this.setValue(initValue);
	}
	
	
	public static void main(String[] args) throws IOException {
		DcsmlUnsigned8 du = new DcsmlUnsigned8(18);
		System.out.println(HexDump.toHex(du.encode()));
		
	}
	
	
}
