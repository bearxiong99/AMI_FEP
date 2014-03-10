package dcsml.base;

import java.io.IOException;

import cn.hexing.fk.utils.HexDump;

public class DcsmlUnsigned32 extends DcsmlUnsigned{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5699835440185567804L;

	
	public DcsmlUnsigned32(){
		super();
		fixedLength(5);
	}
	
	public DcsmlUnsigned32(int initValue){
		this();
		this.setValue(initValue);
	}
	
	
	public static void main(String[] args) throws IOException {
		DcsmlUnsigned32 du = new DcsmlUnsigned32(18);
		System.out.println(HexDump.toHex(du.encode()));
		
	}
	
	
}
