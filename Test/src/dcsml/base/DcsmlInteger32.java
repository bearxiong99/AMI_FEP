package dcsml.base;

import java.io.IOException;

import cn.hexing.fk.utils.HexDump;

public class DcsmlInteger32 extends DcsmlUnsigned{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5699835440185567804L;

	
	public DcsmlInteger32(){
		super();
		fixedLength(5);
	}
	
	public DcsmlInteger32(int initValue){
		this();
		this.setValue(initValue);
	}
	
	
	public static void main(String[] args) throws IOException {
		DcsmlInteger32 du = new DcsmlInteger32(18);
		System.out.println(HexDump.toHex(du.encode()));
		
	}
	
	
}
