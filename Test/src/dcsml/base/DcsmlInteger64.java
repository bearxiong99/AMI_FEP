package dcsml.base;

import java.io.IOException;

import cn.hexing.fk.utils.HexDump;

public class DcsmlInteger64 extends DcsmlUnsigned{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5699835440185567804L;

	
	public DcsmlInteger64(){
		super();
		fixedLength(9);
	}
	
	public DcsmlInteger64(int initValue){
		this();
		this.setValue(initValue);
	}
	
	
	public static void main(String[] args) throws IOException {
		DcsmlInteger64 du = new DcsmlInteger64(18);
		System.out.println(HexDump.toHex(du.encode()));
		
	}
	
	
}
